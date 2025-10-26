package com.hotelmanagement.system.controller;

import com.hotelmanagement.system.model.Booking;
import com.hotelmanagement.system.model.Room;
import com.hotelmanagement.system.model.User;
import com.hotelmanagement.system.service.BookingService;
import com.hotelmanagement.system.service.EmailService;
import com.hotelmanagement.system.service.EmailServiceImpl;
import com.hotelmanagement.system.service.UserService;
import com.hotelmanagement.system.util.CalendarDay;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/reservations")
public class ReservationController {

    private final BookingService bookingService;
    private final UserService userService;
    private final EmailService emailService;

    @Autowired
    public ReservationController(BookingService bookingService,
                                 UserService userService,
                                 EmailService emailService) {
        this.bookingService = bookingService;
        this.userService = userService;
        this.emailService = emailService;
    }

    @GetMapping
    public String showReservationsPage(
            @RequestParam(required = false, defaultValue = "list") String view,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) @DateTimeFormat(pattern="yyyy-MM") YearMonth month,
            Model model) {

        List<Booking> bookings = bookingService.getAllBookings();

        if (search != null && !search.isEmpty()) {
            bookings = bookingService.searchBookings(search);
            model.addAttribute("searchTerm", search);
        } else if (status != null && !status.isEmpty() && !status.equalsIgnoreCase("ALL")) {
            final String filterStatus = status;
            bookings = bookings.stream()
                    .filter(b -> filterStatus.equals(b.getStatus()))
                    .collect(Collectors.toList());
        }

        YearMonth currentMonth = (month == null) ? YearMonth.now() : month;
        model.addAttribute("calendarDays", buildCalendar(currentMonth, bookings));
        model.addAttribute("currentMonth", currentMonth.atDay(1));
        model.addAttribute("prevMonth", currentMonth.minusMonths(1));
        model.addAttribute("nextMonth", currentMonth.plusMonths(1));
        model.addAttribute("bookings", bookings);
        model.addAttribute("stats", bookingService.getBookingStatistics());
        model.addAttribute("allUsers", userService.getAllUsers());
        model.addAttribute("selectedStatus", status != null ? status : "ALL");
        model.addAttribute("currentView", view);

        return "reservations";
    }

    @GetMapping("/{id}/details")
    public String showBookingDetailsPage(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Booking booking = bookingService.getBookingById(id);

            // Get available rooms based on whether room is assigned or not
            List<Room> availableRooms;
            if (booking.getRoom() != null) {
                // Room is assigned, get other rooms of same type
                availableRooms = bookingService.getAvailableRoomsForReassignment(booking);
            } else {
                // Room not assigned, get all available rooms of the specified room type
                availableRooms = bookingService.getAvailableRoomsByDateRangeAndType(
                        booking.getCheckInDate(),
                        booking.getCheckOutDate(),
                        booking.getRoomType()
                );
            }

            model.addAttribute("booking", booking);
            model.addAttribute("availableRooms", availableRooms);
            model.addAttribute("needsRoomAssignment", booking.getRoom() == null);

            return "reservation-details";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Booking not found: " + e.getMessage());
            return "redirect:/reservations";
        }
    }

    @PostMapping("/{id}/update")
    public String updateBooking(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate,
            RedirectAttributes redirectAttributes) {

        try {
            bookingService.updateBookingDates(id, checkInDate, checkOutDate);
            redirectAttributes.addFlashAttribute("success", "Booking dates updated successfully.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Error updating booking: " + e.getMessage());
        }

        return "redirect:/reservations/" + id + "/details";
    }

    @PostMapping("/{id}/update-status")
    public String updateBookingStatus(
            @PathVariable Long id,
            @RequestParam String status,
            RedirectAttributes redirectAttributes) {

        try {
            Booking booking = bookingService.getBookingById(id);

            // CRITICAL: Validate room is assigned before checking in
            if ("CHECKED_IN".equals(status) && booking.getRoom() == null) {
                redirectAttributes.addFlashAttribute("error",
                        "Cannot check in guest - No room has been assigned yet. Please assign a room first.");
                return "redirect:/reservations/" + id + "/details";
            }

            bookingService.updateBookingStatus(id, status);
            redirectAttributes.addFlashAttribute("success",
                    "Booking #" + id + " status updated to " + status + ".");

        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Error updating status: " + e.getMessage());
        }

        return "redirect:/reservations/" + id + "/details";
    }

    /**
     * ENHANCED: Handle both room assignment and reassignment
     */
    @PostMapping("/{id}/reassign-room")
    public String assignOrReassignRoom(
            @PathVariable Long id,
            @RequestParam Long newRoomId,
            RedirectAttributes redirectAttributes) {

        try {
            Booking booking = bookingService.getBookingById(id);
            boolean wasUnassigned = (booking.getRoom() == null);

            bookingService.reassignRoom(id, newRoomId);

            // Send appropriate email
            if (wasUnassigned) {
                // This was the first room assignment
                Booking updatedBooking = bookingService.getBookingById(id);
                try {
                    // Cast to EmailServiceImpl to access sendRoomAssignmentEmail method
                    if (emailService instanceof EmailServiceImpl) {
                        ((EmailServiceImpl) emailService).sendRoomAssignmentEmail(
                                updatedBooking.getUser().getEmail(),
                                updatedBooking
                        );
                    }
                } catch (Exception e) {
                    System.err.println("Failed to send room assignment email: " + e.getMessage());
                }

                redirectAttributes.addFlashAttribute("success",
                        "Room successfully assigned! Customer has been notified via email.");
            } else {
                redirectAttributes.addFlashAttribute("success", "Room reassigned successfully.");
            }

        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Error assigning room: " + e.getMessage());
        }

        return "redirect:/reservations/" + id + "/details";
    }

    @PostMapping("/create")
    public String createBooking(
            @RequestParam Long userId,
            @RequestParam Long roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate,
            @RequestParam(required = false) String specialRequests,
            RedirectAttributes redirectAttributes) {

        try {
            bookingService.createBooking(userId, roomId, checkInDate, checkOutDate, specialRequests);
            redirectAttributes.addFlashAttribute("success", "New reservation created successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error creating booking: " + e.getMessage());
        }

        return "redirect:/reservations";
    }

    @PostMapping("/add-guest")
    public String addGuest(
            @RequestParam String name,
            @RequestParam String email,
            RedirectAttributes redirectAttributes) {

        try {
            User newUser = new User();
            newUser.setName(name);
            newUser.setEmail(email);
            newUser.setPassword("defaultPassword");
            newUser.setRole("GUEST");

            userService.registerUser(newUser);
            redirectAttributes.addFlashAttribute("success", "New guest '" + name + "' added successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error adding guest: " + e.getMessage());
        }

        return "redirect:/reservations";
    }

    @PostMapping("/{id}/cancel")
    public String cancelBooking(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            bookingService.cancelBooking(id);
            redirectAttributes.addFlashAttribute("success", "Booking #" + id + " has been cancelled.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error cancelling booking: " + e.getMessage());
        }

        return "redirect:/reservations";
    }

    private List<CalendarDay> buildCalendar(YearMonth month, List<Booking> allBookings) {
        List<CalendarDay> days = new ArrayList<>();
        LocalDate firstOfMonth = month.atDay(1);
        int startDayOfWeek = firstOfMonth.getDayOfWeek().getValue() % 7;
        LocalDate calendarStart = firstOfMonth.minusDays(startDayOfWeek);

        for (int i = 0; i < 42; i++) {
            LocalDate date = calendarStart.plusDays(i);
            CalendarDay day = new CalendarDay(
                    date.getDayOfMonth(),
                    date.equals(LocalDate.now()),
                    date.getMonth().equals(month.getMonth())
            );

            for (Booking booking : allBookings) {
                if (!booking.getCheckInDate().isAfter(date) && !booking.getCheckOutDate().isBefore(date)) {
                    day.getBookings().add(booking);
                }
            }

            days.add(day);
        }

        return days;
    }
}