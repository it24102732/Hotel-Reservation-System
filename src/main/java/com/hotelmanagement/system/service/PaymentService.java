package com.hotelmanagement.system.service;

import com.hotelmanagement.system.model.Booking;
import com.hotelmanagement.system.model.HotelCard;
import com.hotelmanagement.system.model.Payment;
import com.hotelmanagement.system.repository.HotelCardRepository;
import com.hotelmanagement.system.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private HotelCardRepository hotelCardRepository;

    /**
     * Process card payment with proper validation and error handling
     * Ensures the card belongs to the customer making the payment
     */
    @Transactional
    public Payment processCardPayment(Booking booking, String cardNumber) {
        // Validate card exists
        HotelCard card = hotelCardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new RuntimeException("Invalid card number. Card not found in system."));

        // SECURITY: Validate card belongs to the user making the booking
        if (!card.getUser().getId().equals(booking.getUser().getId())) {
            throw new SecurityException("This card does not belong to you. Please use your own card.");
        }

        // Check card expiry
        if (card.getExpiryDate() != null && card.getExpiryDate().isBefore(java.time.LocalDate.now())) {
            throw new RuntimeException("Card has expired on " + card.getExpiryDate() + ". Please use a valid card.");
        }

        // Check sufficient balance
        if (card.getBalance() < booking.getTotalPrice()) {
            throw new RuntimeException(String.format(
                    "Insufficient balance. Required: $%.2f, Available: $%.2f. Please add funds or use another card.",
                    booking.getTotalPrice(),
                    card.getBalance()
            ));
        }

        // Deduct amount from card
        card.setBalance(card.getBalance() - booking.getTotalPrice());
        hotelCardRepository.save(card);

        // Create payment record
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(booking.getTotalPrice());
        payment.setPaymentMethod("CARD");
        payment.setStatus("SUCCESSFUL");
        payment.setTransactionDate(LocalDateTime.now());
        payment.setPaymentIdentifier(cardNumber); // CRITICAL: Store for refund processing

        Payment savedPayment = paymentRepository.save(payment);

        System.out.println(String.format(
                "Payment processed: $%.2f charged to card ending in %s. Booking ID: %d",
                booking.getTotalPrice(),
                cardNumber.substring(Math.max(0, cardNumber.length() - 4)),
                booking.getId()
        ));

        return savedPayment;
    }

    /**
     * Process cash payment
     */
    @Transactional
    public Payment processCashPayment(Booking booking) {
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(booking.getTotalPrice());
        payment.setPaymentMethod("CASH");
        payment.setStatus("SUCCESSFUL");
        payment.setTransactionDate(LocalDateTime.now());
        payment.setPaymentIdentifier("CASH_" + System.currentTimeMillis()); // Unique identifier

        Payment savedPayment = paymentRepository.save(payment);

        System.out.println(String.format(
                "Cash payment processed: $%.2f for Booking ID: %d",
                booking.getTotalPrice(),
                booking.getId()
        ));

        return savedPayment;
    }

    /**
     * Get payment by booking ID
     */
    public Payment getPaymentByBookingId(Long bookingId) {
        return paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new RuntimeException("No payment found for booking ID: " + bookingId));
    }

    /**
     * Verify if payment exists and was successful
     */
    public boolean isPaymentSuccessful(Long bookingId) {
        return paymentRepository.findByBookingId(bookingId)
                .map(payment -> "SUCCESSFUL".equals(payment.getStatus()))
                .orElse(false);
    }
}