package com.hotelmanagement.system.controller;

import com.hotelmanagement.system.model.Booking;
import com.hotelmanagement.system.model.Payment;
import com.hotelmanagement.system.model.Refund;
import com.hotelmanagement.system.repository.BookingRepository;
import com.hotelmanagement.system.repository.PaymentRepository;
import com.hotelmanagement.system.service.RefundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/finance")
public class FinanceController {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private RefundService refundService;

    @Autowired
    private BookingRepository bookingRepository;

    /**
     * Get all transactions (payments) with detailed information
     */
    @GetMapping("/transactions")
    public ResponseEntity<List<Map<String, Object>>> getAllTransactions() {
        List<Payment> payments = paymentRepository.findAll();

        List<Map<String, Object>> transactionDetails = payments.stream().map(payment -> {
            Map<String, Object> detail = new HashMap<>();
            detail.put("id", payment.getId());
            detail.put("amount", payment.getAmount());
            detail.put("paymentMethod", payment.getPaymentMethod());
            detail.put("status", payment.getStatus());
            detail.put("transactionDate", payment.getTransactionDate());

            if (payment.getBooking() != null) {
                detail.put("type", "BOOKING");
                detail.put("bookingId", payment.getBooking().getId());
                detail.put("customerName", payment.getBooking().getUser().getName());
                detail.put("customerEmail", payment.getBooking().getUser().getEmail());
                detail.put("roomNumber", payment.getBooking().getRoom().getRoomNumber());
            } else if (payment.getFoodOrder() != null) {
                detail.put("type", "FOOD_ORDER");
                detail.put("orderId", payment.getFoodOrder().getId());
                detail.put("customerName", payment.getFoodOrder().getUser().getName());
                detail.put("customerEmail", payment.getFoodOrder().getUser().getEmail());
            }

            return detail;
        }).collect(Collectors.toList());

        return new ResponseEntity<>(transactionDetails, HttpStatus.OK);
    }

    /**
     * Get booking transactions only
     */
    @GetMapping("/transactions/bookings")
    public ResponseEntity<List<Payment>> getBookingTransactions() {
        List<Payment> payments = paymentRepository.findAll().stream()
                .filter(p -> p.getBooking() != null)
                .collect(Collectors.toList());
        return new ResponseEntity<>(payments, HttpStatus.OK);
    }

    /**
     * Get food order transactions only
     */
    @GetMapping("/transactions/food-orders")
    public ResponseEntity<List<Payment>> getFoodOrderTransactions() {
        List<Payment> payments = paymentRepository.findAll().stream()
                .filter(p -> p.getFoodOrder() != null)
                .collect(Collectors.toList());
        return new ResponseEntity<>(payments, HttpStatus.OK);
    }

    /**
     * Get all cancelled bookings
     */
    @GetMapping("/bookings/cancelled")
    public ResponseEntity<List<Booking>> getCancelledBookings() {
        List<Booking> cancelledBookings = bookingRepository.findByStatus("CANCELLED");
        return new ResponseEntity<>(cancelledBookings, HttpStatus.OK);
    }

    /**
     * Get all bookings
     */
    @GetMapping("/bookings/all")
    public ResponseEntity<List<Booking>> getAllBookings() {
        List<Booking> bookings = bookingRepository.findAll();
        return new ResponseEntity<>(bookings, HttpStatus.OK);
    }

    /**
     * Get all pending refunds for finance dashboard
     */
    @GetMapping("/refunds/pending")
    public ResponseEntity<List<Refund>> getPendingRefunds() {
        List<Refund> pendingRefunds = refundService.getRefundsByStatus("PENDING");
        return new ResponseEntity<>(pendingRefunds, HttpStatus.OK);
    }

    /**
     * Get all refunds
     */
    @GetMapping("/refunds")
    public ResponseEntity<List<Refund>> getAllRefunds() {
        List<Refund> refunds = refundService.getAllRefunds();
        return new ResponseEntity<>(refunds, HttpStatus.OK);
    }

    /**
     * Get refunds by status
     */
    @GetMapping("/refunds/status/{status}")
    public ResponseEntity<List<Refund>> getRefundsByStatus(@PathVariable String status) {
        List<Refund> refunds = refundService.getRefundsByStatus(status.toUpperCase());
        return new ResponseEntity<>(refunds, HttpStatus.OK);
    }

    /**
     * Get comprehensive financial summary
     */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getFinancialSummary() {
        List<Payment> allPayments = paymentRepository.findAll();
        List<Refund> allRefunds = refundService.getAllRefunds();
        List<Booking> allBookings = bookingRepository.findAll();

        // Calculate revenues
        double totalRevenue = allPayments.stream()
                .filter(p -> "SUCCESSFUL".equals(p.getStatus()))
                .mapToDouble(Payment::getAmount)
                .sum();

        double bookingRevenue = allPayments.stream()
                .filter(p -> "SUCCESSFUL".equals(p.getStatus()) && p.getBooking() != null)
                .mapToDouble(Payment::getAmount)
                .sum();

        double foodRevenue = allPayments.stream()
                .filter(p -> "SUCCESSFUL".equals(p.getStatus()) && p.getFoodOrder() != null)
                .mapToDouble(Payment::getAmount)
                .sum();

        // Calculate refunds
        double totalRefunded = allRefunds.stream()
                .filter(r -> "SUCCESSFUL".equals(r.getStatus()))
                .mapToDouble(Refund::getAmount)
                .sum();

        double pendingRefundAmount = allRefunds.stream()
                .filter(r -> "PENDING".equals(r.getStatus()))
                .mapToDouble(Refund::getAmount)
                .sum();

        // Calculate net revenue
        double netRevenue = totalRevenue - totalRefunded;

        // Count statistics
        long totalTransactions = allPayments.size();
        long successfulTransactions = allPayments.stream()
                .filter(p -> "SUCCESSFUL".equals(p.getStatus()))
                .count();

        long pendingRefundCount = allRefunds.stream()
                .filter(r -> "PENDING".equals(r.getStatus()))
                .count();

        long cancelledBookingsCount = allBookings.stream()
                .filter(b -> "CANCELLED".equals(b.getStatus()))
                .count();

        long activeBookingsCount = allBookings.stream()
                .filter(b -> "CONFIRMED".equals(b.getStatus()) || "CHECKED_IN".equals(b.getStatus()))
                .count();

        // Today's statistics
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        double todayRevenue = allPayments.stream()
                .filter(p -> "SUCCESSFUL".equals(p.getStatus()) &&
                        p.getTransactionDate() != null &&
                        p.getTransactionDate().isAfter(todayStart))
                .mapToDouble(Payment::getAmount)
                .sum();

        long todayTransactions = allPayments.stream()
                .filter(p -> p.getTransactionDate() != null &&
                        p.getTransactionDate().isAfter(todayStart))
                .count();

        // Build response
        Map<String, Object> summary = new HashMap<>();

        // Revenue data
        summary.put("totalRevenue", totalRevenue);
        summary.put("bookingRevenue", bookingRevenue);
        summary.put("foodRevenue", foodRevenue);
        summary.put("totalRefunded", totalRefunded);
        summary.put("pendingRefundAmount", pendingRefundAmount);
        summary.put("netRevenue", netRevenue);

        // Transaction counts
        summary.put("totalTransactions", totalTransactions);
        summary.put("successfulTransactions", successfulTransactions);
        summary.put("pendingRefundCount", pendingRefundCount);

        // Booking statistics
        summary.put("totalBookings", allBookings.size());
        summary.put("activeBookings", activeBookingsCount);
        summary.put("cancelledBookings", cancelledBookingsCount);

        // Today's statistics
        summary.put("todayRevenue", todayRevenue);
        summary.put("todayTransactions", todayTransactions);

        return new ResponseEntity<>(summary, HttpStatus.OK);
    }

    /**
     * Get revenue trend data for charts (last 7 days)
     */
    @GetMapping("/revenue/trend")
    public ResponseEntity<List<Map<String, Object>>> getRevenueTrend() {
        List<Payment> allPayments = paymentRepository.findAll();
        List<Map<String, Object>> trendData = new java.util.ArrayList<>();

        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();

            double dayRevenue = allPayments.stream()
                    .filter(p -> "SUCCESSFUL".equals(p.getStatus()) &&
                            p.getTransactionDate() != null &&
                            p.getTransactionDate().isAfter(dayStart) &&
                            p.getTransactionDate().isBefore(dayEnd))
                    .mapToDouble(Payment::getAmount)
                    .sum();

            Map<String, Object> dataPoint = new HashMap<>();
            dataPoint.put("date", date.toString());
            dataPoint.put("revenue", dayRevenue);
            trendData.add(dataPoint);
        }

        return new ResponseEntity<>(trendData, HttpStatus.OK);
    }
}