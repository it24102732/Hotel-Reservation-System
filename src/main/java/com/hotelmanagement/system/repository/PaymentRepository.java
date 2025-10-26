package com.hotelmanagement.system.repository;

import com.hotelmanagement.system.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    // Find payment by booking ID
    Optional<Payment> findByBookingId(Long bookingId);

    // ✅ ADD THIS METHOD for food orders
    Optional<Payment> findByFoodOrderId(Long foodOrderId);
}