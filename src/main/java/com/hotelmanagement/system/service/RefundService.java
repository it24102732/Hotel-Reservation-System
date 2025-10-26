package com.hotelmanagement.system.service;

import com.hotelmanagement.system.model.Booking;
import com.hotelmanagement.system.model.HotelCard;
import com.hotelmanagement.system.model.Payment;
import com.hotelmanagement.system.model.Refund;
import com.hotelmanagement.system.model.User;
import com.hotelmanagement.system.repository.HotelCardRepository;
import com.hotelmanagement.system.repository.PaymentRepository;
import com.hotelmanagement.system.repository.RefundRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class RefundService {

    private final RefundRepository refundRepository;
    private final PaymentRepository paymentRepository;
    private final HotelCardRepository hotelCardRepository;
    private final EmailService emailService;

    @Autowired
    public RefundService(RefundRepository refundRepository,
                         PaymentRepository paymentRepository,
                         HotelCardRepository hotelCardRepository,
                         EmailService emailService) {
        this.refundRepository = refundRepository;
        this.paymentRepository = paymentRepository;
        this.hotelCardRepository = hotelCardRepository;
        this.emailService = emailService;
    }

    @Transactional
    public Refund processMockRefund(Long refundId) {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new RuntimeException("Refund not found with ID: " + refundId));

        if (!"PENDING".equals(refund.getStatus())) {
            throw new IllegalStateException("Can only process pending refunds. Current status: " + refund.getStatus());
        }

        try {
            User user = refund.getBooking().getUser();

            // Find the default card
            HotelCard cardToCredit = hotelCardRepository.findByUserIdAndIsDefaultTrue(user.getId())
                    .orElseThrow(() -> new RuntimeException("Default card not found for user. Cannot process refund."));

            // Credit the refund amount to the default card
            cardToCredit.setBalance(cardToCredit.getBalance() + refund.getAmount());
            hotelCardRepository.save(cardToCredit);

            // Update refund status
            refund.setStatus("SUCCESSFUL");
            refund.setProcessedAt(LocalDateTime.now());
            refund.setRefundTransactionId("REF_" + UUID.randomUUID().toString());

            Refund savedRefund = refundRepository.save(refund);

            // ✅ SEND SUCCESS EMAIL
            try {
                emailService.sendRefundProcessedEmail(user.getEmail(), savedRefund);
                System.out.println("✅ Refund processed email sent to: " + user.getEmail());
            } catch (Exception emailEx) {
                System.err.println("❌ Failed to send refund processed email: " + emailEx.getMessage());
            }

            return savedRefund;

        } catch (Exception e) {
            // Mark refund as failed
            refund.setStatus("FAILED");
            refund.setProcessedAt(LocalDateTime.now());
            refund.setReason(refund.getReason() + " | ERROR: " + e.getMessage());
            Refund failedRefund = refundRepository.save(refund);

            // ✅ SEND FAILURE EMAIL
            try {
                emailService.sendRefundFailedEmail(refund.getBooking().getUser().getEmail(), failedRefund, e.getMessage());
                System.out.println("⚠️ Refund failed email sent to: " + refund.getBooking().getUser().getEmail());
            } catch (Exception emailEx) {
                System.err.println("❌ Failed to send refund failure email: " + emailEx.getMessage());
            }

            throw new RuntimeException("Refund processing failed: " + e.getMessage(), e);
        }
    }

    /**
     * Called by Finance Officer to reject a pending refund.
     */
    @Transactional
    public Refund rejectRefund(Long refundId, String reason) {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new RuntimeException("Refund not found with ID: " + refundId));

        if (!"PENDING".equals(refund.getStatus())) {
            throw new IllegalStateException("Can only reject pending refunds. Current status: " + refund.getStatus());
        }

        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("A reason must be provided for rejecting a refund.");
        }

        // Update refund status to FAILED (rejected)
        refund.setStatus("FAILED");
        refund.setProcessedAt(LocalDateTime.now());
        refund.setReason(refund.getReason() + " | REJECTED: " + reason);

        Refund rejectedRefund = refundRepository.save(refund);

        // ✅ SEND REJECTION EMAIL
        try {
            if (emailService instanceof EmailServiceImpl) {
                ((EmailServiceImpl) emailService).sendRefundRejectedEmail(
                        refund.getBooking().getUser().getEmail(),
                        rejectedRefund,
                        reason
                );
                System.out.println("❌ Refund rejected email sent to: " + refund.getBooking().getUser().getEmail());
            }
        } catch (Exception emailEx) {
            System.err.println("❌ Failed to send refund rejected email: " + emailEx.getMessage());
        }

        return rejectedRefund;
    }

    /**
     * Initiate a refund for a cancelled booking
     */
    @Transactional
    public Refund initiateRefund(Booking booking, String reason) {
        // Check if refund already exists
        if (refundRepository.findByBookingId(booking.getId()).isPresent()) {
            return refundRepository.findByBookingId(booking.getId()).get();
        }

        // Find the payment for this booking
        Payment payment = paymentRepository.findByBookingId(booking.getId())
                .orElseThrow(() -> new RuntimeException("Cannot initiate refund. No payment found for booking ID: " + booking.getId()));

        if (!"SUCCESSFUL".equals(payment.getStatus())) {
            throw new RuntimeException("Cannot initiate refund. Payment status is: " + payment.getStatus());
        }

        // Calculate refund amount based on cancellation policy
        double refundAmount = calculateRefundAmount(booking);

        // Create refund record
        Refund refund = new Refund();
        refund.setBooking(booking);
        refund.setAmount(refundAmount);
        refund.setReason(reason != null ? reason : "Booking cancelled by customer");
        refund.setStatus("PENDING");
        refund.setRequestedAt(LocalDateTime.now());

        Refund savedRefund = refundRepository.save(refund);

        // ✅ SEND REFUND REQUEST EMAIL
        try {
            if (emailService instanceof EmailServiceImpl) {
                ((EmailServiceImpl) emailService).sendRefundRequestedEmail(
                        booking.getUser().getEmail(),
                        savedRefund
                );
                System.out.println("🔄 Refund requested email sent to: " + booking.getUser().getEmail());
            }
        } catch (Exception emailEx) {
            System.err.println("❌ Failed to send refund requested email: " + emailEx.getMessage());
        }

        return savedRefund;
    }

    /**
     * Calculate refund amount based on cancellation policy
     */
    private double calculateRefundAmount(Booking booking) {
        LocalDate today = LocalDate.now();
        LocalDate checkInDate = booking.getCheckInDate();
        long daysUntilCheckIn = java.time.temporal.ChronoUnit.DAYS.between(today, checkInDate);
        double totalPrice = booking.getTotalPrice();

        if (daysUntilCheckIn <= 0) {
            return 0.0; // No refund after check-in date
        }
        if (daysUntilCheckIn > 7) {
            return totalPrice; // Full refund
        }
        if (daysUntilCheckIn >= 3) {
            return totalPrice * 0.5; // 50% refund
        }
        return totalPrice * 0.2; // 20% refund
    }

    public Refund getRefundByBookingId(Long bookingId) {
        return refundRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new RuntimeException("No refund found for booking ID: " + bookingId));
    }

    public List<Refund> getRefundsByStatus(String status) {
        return refundRepository.findByStatus(status);
    }

    public List<Refund> getAllRefunds() {
        return refundRepository.findAll();
    }

    public Refund getRefundById(Long refundId) {
        return refundRepository.findById(refundId)
                .orElseThrow(() -> new RuntimeException("Refund not found with ID: " + refundId));
    }

    @Transactional
    public Refund cancelRefund(Long refundId, String cancellationReason) {
        Refund refund = getRefundById(refundId);

        if (!"PENDING".equals(refund.getStatus())) {
            throw new IllegalStateException("Can only cancel pending refunds. Current status: " + refund.getStatus());
        }

        refund.setStatus("CANCELLED");
        refund.setProcessedAt(LocalDateTime.now());
        refund.setReason(refund.getReason() + " | Cancellation reason: " + cancellationReason);

        return refundRepository.save(refund);
    }
}