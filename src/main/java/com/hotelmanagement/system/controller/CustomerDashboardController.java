package com.hotelmanagement.system.controller;

import com.hotelmanagement.system.model.Booking;
import com.hotelmanagement.system.model.Room;
import com.hotelmanagement.system.model.User;
import com.hotelmanagement.system.service.BookingService;
import com.hotelmanagement.system.service.UserService;
import com.hotelmanagement.system.service.WalletService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Controller
@RequestMapping("/customer")
public class CustomerDashboardController {

    @Autowired
    private UserService userService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private WalletService walletService;

    /**
     * Customer Dashboard
     */
    @GetMapping("/dashboard")
    public String showCustomerDashboard(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            redirectAttributes.addFlashAttribute("error", "Please login to access your dashboard.");
            return "redirect:/login";
        }

        try {
            User user = userService.getUserById(userId);
            model.addAttribute("user", user);

            List<Booking> bookings = bookingService.getBookingsByUserId(userId);
            model.addAttribute("bookings", bookings);

            long activeBookings = bookings.stream()
                    .filter(b -> "CONFIRMED".equals(b.getStatus()) || "CHECKED_IN".equals(b.getStatus()))
                    .count();
            long pendingBookings = bookings.stream()
                    .filter(b -> "PENDING".equals(b.getStatus()))
                    .count();
            long completedBookings = bookings.stream()
                    .filter(b -> "CHECKED_OUT".equals(b.getStatus()))
                    .count();

            model.addAttribute("activeBookings", activeBookings);
            model.addAttribute("pendingBookings", pendingBookings);
            model.addAttribute("completedBookings", completedBookings);

            double totalBalance = walletService.getTotalBalance(userId);
            model.addAttribute("totalBalance", totalBalance);

            return "customer-dashboard";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error loading dashboard: " + e.getMessage());
            return "redirect:/login";
        }
    }

    /**
     * Show booking details
     */
    @GetMapping("/bookings/{bookingId}")
    public String showBookingDetails(@PathVariable Long bookingId, HttpSession session,
                                     Model model, RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            redirectAttributes.addFlashAttribute("error", "Please login to view booking details.");
            return "redirect:/login";
        }

        try {
            Booking booking = bookingService.getBookingById(bookingId);

            // Security check
            if (!booking.getUser().getId().equals(userId)) {
                redirectAttributes.addFlashAttribute("error", "You don't have permission to view this booking.");
                return "redirect:/customer/dashboard";
            }

            model.addAttribute("booking", booking);

            // Determine if booking can be cancelled
            boolean canCancel = "PENDING".equals(booking.getStatus()) || "CONFIRMED".equals(booking.getStatus());
            model.addAttribute("canCancel", canCancel);

            return "customer-booking-details";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error loading booking details: " + e.getMessage());
            return "redirect:/customer/dashboard";
        }
    }

    @PostMapping("/book-room-type")
    public String bookRoomType(
            @RequestParam String roomType,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate,
            @RequestParam(required = false) String specialRequests,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            redirectAttributes.addFlashAttribute("error", "Please login to book a room.");
            return "redirect:/login";
        }

        // CRITICAL: Validate dates
        if (checkInDate.isBefore(LocalDate.now())) {
            redirectAttributes.addFlashAttribute("error", "Check-in date cannot be in the past.");
            return "redirect:/customer/dashboard";
        }

        if (checkOutDate.isBefore(checkInDate) || checkOutDate.isEqual(checkInDate)) {
            redirectAttributes.addFlashAttribute("error", "Check-out date must be at least one day after check-in date.");
            return "redirect:/customer/dashboard";
        }

        // CRITICAL: Validate room type
        String normalizedRoomType = roomType.toUpperCase().trim();
        if (!normalizedRoomType.equals("SINGLE") &&
                !normalizedRoomType.equals("DOUBLE") &&
                !normalizedRoomType.equals("SUITE")) {
            redirectAttributes.addFlashAttribute("error", "Invalid room type selected. Please choose Single, Double, or Suite.");
            return "redirect:/customer/dashboard";
        }

        try {
            User user = userService.getUserById(userId);

            // Check availability
            List<Room> availableRooms = bookingService.getAvailableRoomsByDateRangeAndType(
                    checkInDate, checkOutDate, normalizedRoomType);

            if (availableRooms.isEmpty()) {
                redirectAttributes.addFlashAttribute("error",
                        "Sorry, no " + normalizedRoomType + " rooms are available for " +
                                checkInDate + " to " + checkOutDate + ". " +
                                "Please try different dates or choose another room type.");
                return "redirect:/customer/dashboard";
            }

            // Calculate price
            double pricePerNight = getRoomTypePrice(normalizedRoomType);
            long numberOfNights = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
            double totalPrice = pricePerNight * numberOfNights;

            // Create booking
            Booking booking = new Booking();
            booking.setUser(user);
            booking.setRoom(null); // Room will be assigned by staff
            booking.setRoomType(normalizedRoomType); // Store room type
            booking.setCheckInDate(checkInDate);
            booking.setCheckOutDate(checkOutDate);
            booking.setTotalPrice(totalPrice);
            booking.setStatus("PENDING");
            booking.setSpecialRequests(specialRequests);

            Booking savedBooking = bookingService.saveBooking(booking);

            redirectAttributes.addFlashAttribute("success",
                    "Booking created successfully! Booking ID: #" + savedBooking.getId() + ". " +
                            "Room type: " + normalizedRoomType + ". " +
                            "Total: $" + String.format("%.2f", totalPrice) + " for " + numberOfNights + " night(s). " +
                            "Please complete payment to confirm your booking.");

            return "redirect:/customer/payment/booking/" + savedBooking.getId();

        } catch (Exception e) {
            System.err.println("ERROR creating booking: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error creating booking: " + e.getMessage());
            return "redirect:/customer/dashboard";
        }
    }

    /**
     * Helper method to get room type price
     */
    private double getRoomTypePrice(String roomType) {
        switch (roomType.toUpperCase()) {
            case "SINGLE":
                return 50.00;
            case "DOUBLE":
                return 80.00;
            case "SUITE":
                return 150.00;
            default:
                System.err.println("WARNING: Unknown room type: " + roomType + ", using default price");
                return 50.00;
        }
    }
}