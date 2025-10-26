package com.hotelmanagement.system.service;

import com.hotelmanagement.system.model.Booking;
import com.hotelmanagement.system.model.Room;
import com.hotelmanagement.system.model.User;
import com.hotelmanagement.system.repository.BookingRepository;
import com.hotelmanagement.system.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final EmailService emailService;
    private final RefundService refundService;
    private final UserService userService;

    @Autowired
    public BookingService(BookingRepository bookingRepository, RoomRepository roomRepository,
                          EmailService emailService, RefundService refundService,
                          UserService userService) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.emailService = emailService;
        this.refundService = refundService;
        this.userService = userService;
    }

    /**
     * Get bookings by user ID
     */
    public List<Booking> getBookingsByUserId(Long userId) {
        return bookingRepository.findByUserId(userId);
    }

    /**
     * Get available rooms by date range and room type
     */
    public List<Room> getAvailableRoomsByDateRangeAndType(LocalDate checkIn, LocalDate checkOut, String roomType) {
        if (checkIn.isAfter(checkOut)) {
            throw new IllegalArgumentException("Check-out must be after check-in.");
        }

        List<Room> availableRooms = roomRepository.findAvailableRoomsForDateRange(checkIn, checkOut);

        return availableRooms.stream()
                .filter(room -> room.getType().equalsIgnoreCase(roomType))
                .collect(Collectors.toList());
    }

    /**
     * Update booking status with email notification
     */
    @Transactional
    public Booking updateBookingStatus(Long bookingId, String newStatus) {
        Booking booking = getBookingById(bookingId);

        // Validate status transition
        validateStatusTransition(booking.getStatus(), newStatus);

        booking.setStatus(newStatus);
        Booking updatedBooking = bookingRepository.save(booking);

        try {
            if ("CONFIRMED".equalsIgnoreCase(newStatus)) {
                emailService.sendBookingConfirmedEmail(updatedBooking.getUser().getEmail(), updatedBooking);
            } else {
                emailService.sendStatusUpdateEmail(updatedBooking.getUser().getEmail(), updatedBooking);
            }
        } catch (Exception e) {
            System.err.println("Failed to send status update email for booking #" + bookingId + ": " + e.getMessage());
        }
        return updatedBooking;
    }

    /**
     * Validate status transitions
     */
    private void validateStatusTransition(String currentStatus, String newStatus) {
        // PENDING -> CONFIRMED (after payment)
        // CONFIRMED -> CHECKED_IN
        // CHECKED_IN -> CHECKED_OUT
        // Any -> CANCELLED (with restrictions)

        if ("CHECKED_OUT".equals(currentStatus)) {
            throw new IllegalStateException("Cannot change status of a checked-out booking");
        }

        if ("CANCELLED".equals(currentStatus)) {
            throw new IllegalStateException("Cannot change status of a cancelled booking");
        }
    }

    public Map<String, Long> getBookingStatistics() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("pendingCount", bookingRepository.countByStatus("PENDING"));
        stats.put("confirmedCount", bookingRepository.countByStatus("CONFIRMED"));
        stats.put("checkedInCount", bookingRepository.countByStatus("CHECKED_IN"));
        stats.put("cancelledCount", bookingRepository.countByStatus("CANCELLED"));
        return stats;
    }

    public List<Booking> searchBookings(String searchTerm) {
        return bookingRepository.searchBookings(searchTerm.toLowerCase());
    }

    public List<Room> getAvailableRoomsForReassignment(Booking booking) {
        List<Room> availableRooms = roomRepository.findAvailableRoomsForDateRange(
                booking.getCheckInDate(), booking.getCheckOutDate());
        return availableRooms.stream()
                .filter(room -> room.getType().equals(booking.getRoom().getType()))
                .collect(Collectors.toList());
    }

    @Transactional
    public Booking reassignRoom(Long bookingId, Long newRoomId) {
        Booking booking = getBookingById(bookingId);
        Room newRoom = roomRepository.findById(newRoomId)
                .orElseThrow(() -> new RuntimeException("New room not found."));
        booking.setRoom(newRoom);
        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking updateBookingDates(Long bookingId, LocalDate newCheckIn, LocalDate newCheckOut) {
        if (newCheckIn.isAfter(newCheckOut)) {
            throw new RuntimeException("Check-out date must be after check-in date.");
        }

        Booking booking = getBookingById(bookingId);

        if (!"PENDING".equals(booking.getStatus())) {
            throw new RuntimeException("Only PENDING bookings can have their dates changed.");
        }

        booking.setCheckInDate(newCheckIn);
        booking.setCheckOutDate(newCheckOut);
        long numberOfNights = ChronoUnit.DAYS.between(newCheckIn, newCheckOut);
        booking.setTotalPrice(booking.getRoom().getPrice() * numberOfNights);

        return bookingRepository.save(booking);
    }

    @Transactional
    public void cancelBooking(Long bookingId) {
        Booking booking = getBookingById(bookingId);

        // Validate current status
        if ("CANCELLED".equals(booking.getStatus())) {
            throw new RuntimeException("Booking is already cancelled.");
        }

        if ("CHECKED_IN".equals(booking.getStatus())) {
            throw new RuntimeException("Cannot cancel a booking that is already checked in.");
        }

        if ("CHECKED_OUT".equals(booking.getStatus())) {
            throw new RuntimeException("Cannot cancel a booking that is already checked out.");
        }

        // CRITICAL FIX: Store original status BEFORE changing it
        String originalStatus = booking.getStatus();

        // Update booking status to CANCELLED
        booking.setStatus("CANCELLED");
        bookingRepository.save(booking);

        // CRITICAL FIX: Check the ORIGINAL status for refund eligibility
        // Only initiate refund if booking was confirmed (i.e., payment was made)
        if ("CONFIRMED".equals(originalStatus)) {
            try {
                refundService.initiateRefund(booking, "Booking cancelled by customer.");
            } catch (Exception e) {
                System.err.println("Failed to initiate refund for booking #" + bookingId + ": " + e.getMessage());
            }
        } else {
            System.out.println("No refund needed - booking was in " + originalStatus + " status (payment not yet made)");
        }

        // Send cancellation email
        try {
            emailService.sendBookingCancelledEmail(booking.getUser().getEmail(), booking);
        } catch (Exception e) {
            System.err.println("Failed to send cancellation email: " + e.getMessage());
        }
    }

    @Transactional
    public Booking createBooking(Long userId, Long roomId, LocalDate checkInDate,
                                 LocalDate checkOutDate, String specialRequests) {
        User user = userService.getUserById(userId);
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found."));

        if (checkInDate.isAfter(checkOutDate)) {
            throw new IllegalArgumentException("Check-out date must be after check-in date.");
        }

        // Validate room is available for the selected dates
        List<Room> availableRooms = roomRepository.findAvailableRoomsForDateRange(checkInDate, checkOutDate);
        boolean isRoomAvailable = availableRooms.stream().anyMatch(r -> r.getId().equals(roomId));

        if (!isRoomAvailable) {
            throw new RuntimeException("Selected room is not available for the chosen dates.");
        }

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setRoom(room);
        booking.setCheckInDate(checkInDate);
        booking.setCheckOutDate(checkOutDate);
        booking.setStatus("PENDING");
        booking.setSpecialRequests(specialRequests);

        long numberOfNights = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        booking.setTotalPrice(room.getPrice() * numberOfNights);

        Booking savedBooking = bookingRepository.save(booking);

        try {
            emailService.sendBookingConfirmationEmail(savedBooking.getUser().getEmail(), savedBooking);
        } catch (Exception e) {
            System.err.println("Failed to send confirmation email: " + e.getMessage());
        }

        return savedBooking;
    }

    public Booking getBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with ID: " + bookingId));
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public List<Booking> getBookingsByStatus(String status) {
        return bookingRepository.findByStatus(status);
    }

    public List<Room> getAvailableRoomsByDateRange(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn.isAfter(checkOut)) {
            throw new IllegalArgumentException("Check-out must be after check-in.");
        }
        return roomRepository.findAvailableRoomsForDateRange(checkIn, checkOut);
    }

    /**
     * Save booking without room assignment
     */
    @Transactional
    public Booking saveBooking(Booking booking) {
        return bookingRepository.save(booking);
    }
}