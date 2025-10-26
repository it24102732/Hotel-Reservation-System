package com.hotelmanagement.system.controller;

import com.hotelmanagement.system.model.Booking;
import com.hotelmanagement.system.model.Room;
import com.hotelmanagement.system.model.User;
import com.hotelmanagement.system.service.BookingService;
import com.hotelmanagement.system.service.UserService;
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
@RequestMapping("/customer/booking")
public class CustomerBookingController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserService userService;

    /**
     * UPDATED: Book room type directly - NO room selection
     * Customer selects room type, dates, and goes straight to payment
     * Room assignment happens later by staff
     */
    @PostMapping("/search-rooms")
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

        // Validation
        if (checkInDate.isBefore(LocalDate.now())) {
            redirectAttributes.addFlashAttribute("error", "Check-in date cannot be in the past.");
            return "redirect:/customer/dashboard";
        }

        if (checkOutDate.isBefore(checkInDate) || checkOutDate.isEqual(checkInDate)) {
            redirectAttributes.addFlashAttribute("error", "Check-out date must be after check-in date.");
            return "redirect:/customer/dashboard";
        }

        try {
            User user = userService.getUserById(userId);

            // Check if there are ANY rooms of this type available for the dates
            List<Room> availableRooms = bookingService.getAvailableRoomsByDateRangeAndType(
                    checkInDate, checkOutDate, roomType);

            if (availableRooms.isEmpty()) {
                redirectAttributes.addFlashAttribute("error",
                        "Sorry, no " + roomType + " rooms are available for the selected dates. " +
                                "Please try different dates or room type.");
                return "redirect:/customer/dashboard";
            }

            // Calculate price based on room type
            double pricePerNight = getRoomTypePrice(roomType);
            long numberOfNights = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
            double totalPrice = pricePerNight * numberOfNights;

            // Create booking WITHOUT specific room assignment (room = null)
            Booking booking = new Booking();
            booking.setUser(user);
            booking.setRoom(null); // NO ROOM ASSIGNED YET - Staff will assign later
            booking.setCheckInDate(checkInDate);
            booking.setCheckOutDate(checkOutDate);
            booking.setTotalPrice(totalPrice);
            booking.setStatus("PENDING");
            booking.setSpecialRequests(specialRequests);

            // Save booking
            Booking savedBooking = bookingService.saveBooking(booking);

            // Store room type in session for display purposes
            session.setAttribute("bookedRoomType", roomType);

            redirectAttributes.addFlashAttribute("success",
                    "Booking created successfully! Booking ID: #" + savedBooking.getId() +
                            ". Room type: " + roomType + ". Please complete payment to confirm.");

            // Go directly to payment - NO room selection page
            return "redirect:/customer/payment/booking/" + savedBooking.getId();

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error creating booking: " + e.getMessage());
            return "redirect:/customer/dashboard";
        }
    }

    /**
     * Cancel booking
     */
    @PostMapping("/cancel/{bookingId}")
    public String cancelBooking(@PathVariable Long bookingId, HttpSession session,
                                RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            redirectAttributes.addFlashAttribute("error", "Please login to book a room.");
            return "redirect:/login";
        }

        try {
            Booking booking = bookingService.getBookingById(bookingId);

            // Security check
            if (!booking.getUser().getId().equals(userId)) {
                redirectAttributes.addFlashAttribute("error", "You don't have permission to cancel this booking.");
                return "redirect:/customer/dashboard";
            }

            // Validation - cannot cancel if checked in or checked out
            if ("CHECKED_IN".equals(booking.getStatus()) || "CHECKED_OUT".equals(booking.getStatus())) {
                redirectAttributes.addFlashAttribute("error",
                        "Cannot cancel booking with status: " + booking.getStatus());
                return "redirect:/customer/bookings/" + bookingId;
            }

            if ("CANCELLED".equals(booking.getStatus())) {
                redirectAttributes.addFlashAttribute("error", "This booking is already cancelled.");
                return "redirect:/customer/bookings/" + bookingId;
            }

            bookingService.cancelBooking(bookingId);
            redirectAttributes.addFlashAttribute("success",
                    "Booking cancelled successfully. Refund will be processed by our finance team.");

            return "redirect:/customer/dashboard";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error cancelling booking: " + e.getMessage());
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
                return 50.00;
        }
    }
}