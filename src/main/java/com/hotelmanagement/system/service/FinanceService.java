package com.hotelmanagement.system.service;

import com.hotelmanagement.system.model.Booking;
import com.hotelmanagement.system.model.Payment;
import com.hotelmanagement.system.repository.BookingRepository;
import com.hotelmanagement.system.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FinanceService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RefundService refundService;

    /**
     * Calculates and returns a summary of all financial data.
     */
    public Map<String, Object> getFinancialSummary() {
        List<Payment> allPayments = paymentRepository.findAll();
        List<Booking> allBookings = bookingRepository.findAll();

        // Calculate total revenue from successful payments
        double totalRevenue = allPayments.stream()
                .filter(p -> "SUCCESSFUL".equals(p.getStatus()))
                .mapToDouble(Payment::getAmount)
                .sum();

        // Calculate total refunded amount from successful refunds
        double totalRefunded = refundService.getAllRefunds().stream()
                .filter(r -> "SUCCESSFUL".equals(r.getStatus()))
                .mapToDouble(r -> r.getAmount())
                .sum();

        // Calculate net revenue
        double netRevenue = totalRevenue - totalRefunded;

        // Count pending refunds
        long pendingRefundCount = refundService.getRefundsByStatus("PENDING").size();

        // Count today's transactions
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        long todayTransactions = allPayments.stream()
                .filter(p -> p.getTransactionDate() != null && p.getTransactionDate().isAfter(todayStart))
                .count();

        // Build the summary map to be sent to the controller
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalRevenue", totalRevenue);
        summary.put("netRevenue", netRevenue);
        summary.put("pendingRefundCount", pendingRefundCount);
        summary.put("todayTransactions", todayTransactions);

        return summary;
    }

    /**
     * Retrieves a list of all transactions with details.
     */
    public List<Map<String, Object>> getAllTransactions() {
        return paymentRepository.findAll().stream().map(payment -> {
            Map<String, Object> detail = new HashMap<>();
            detail.put("id", payment.getId());
            detail.put("amount", payment.getAmount());
            detail.put("status", payment.getStatus());
            detail.put("transactionDate", payment.getTransactionDate());

            if (payment.getBooking() != null) {
                detail.put("type", "BOOKING");
                detail.put("customerName", payment.getBooking().getUser().getName());
            } else if (payment.getFoodOrder() != null) {
                detail.put("type", "FOOD_ORDER");
                detail.put("customerName", payment.getFoodOrder().getUser().getName());
            }
            return detail;
        }).collect(Collectors.toList());
    }

    /**
     * Retrieves a list of all bookings with a "CANCELLED" status.
     */
    public List<Booking> getCancelledBookings() {
        return bookingRepository.findByStatus("CANCELLED");
    }
}