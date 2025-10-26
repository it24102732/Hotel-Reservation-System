package com.hotelmanagement.system.controller;

import com.hotelmanagement.system.model.Payment;
import com.hotelmanagement.system.service.BookingService;
import com.hotelmanagement.system.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.hotelmanagement.system.model.Booking;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final BookingService bookingService;

    @Autowired
    public PaymentController(PaymentService paymentService, BookingService bookingService) {
        this.paymentService = paymentService;
        this.bookingService = bookingService;
    }

    @PostMapping("/booking")
    public ResponseEntity<?> payForBooking(@RequestBody Map<String, String> payload) {
        try {
            // Validate required fields
            if (payload.get("bookingId") == null || payload.get("bookingId").isEmpty()) {
                return new ResponseEntity<>("Booking ID is required.", HttpStatus.BAD_REQUEST);
            }

            if (payload.get("paymentMethod") == null || payload.get("paymentMethod").isEmpty()) {
                return new ResponseEntity<>("Payment method is required.", HttpStatus.BAD_REQUEST);
            }

            Long bookingId = Long.parseLong(payload.get("bookingId"));
            String paymentMethod = payload.get("paymentMethod");

            Booking booking = bookingService.getBookingById(bookingId);

            // Check if booking is already paid
            if ("CONFIRMED".equals(booking.getStatus()) || "CHECKED_IN".equals(booking.getStatus())) {
                return new ResponseEntity<>("This booking has already been paid.", HttpStatus.BAD_REQUEST);
            }

            // Check if booking is cancelled
            if ("CANCELLED".equals(booking.getStatus())) {
                return new ResponseEntity<>("Cannot process payment for a cancelled booking.", HttpStatus.BAD_REQUEST);
            }

            Payment payment;
            if ("CARD".equalsIgnoreCase(paymentMethod)) {
                String cardNumber = payload.get("cardNumber");
                if (cardNumber == null || cardNumber.isEmpty()) {
                    return new ResponseEntity<>("Card number is required for card payments.", HttpStatus.BAD_REQUEST);
                }
                payment = paymentService.processCardPayment(booking, cardNumber);
            } else if ("CASH".equalsIgnoreCase(paymentMethod)) {
                payment = paymentService.processCashPayment(booking);
            } else {
                return new ResponseEntity<>("Invalid payment method. Use 'CARD' or 'CASH'.", HttpStatus.BAD_REQUEST);
            }

            // After successful payment, update booking status
            bookingService.updateBookingStatus(bookingId, "CONFIRMED");

            return new ResponseEntity<>(payment, HttpStatus.OK);

        } catch (NumberFormatException e) {
            return new ResponseEntity<>("Invalid booking ID format.", HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get payment details by booking ID
     */
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<?> getPaymentByBookingId(@PathVariable Long bookingId) {
        try {
            Payment payment = paymentService.getPaymentByBookingId(bookingId);
            return new ResponseEntity<>(payment, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
}