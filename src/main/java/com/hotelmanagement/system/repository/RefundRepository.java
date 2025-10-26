package com.hotelmanagement.system.repository;

import com.hotelmanagement.system.model.Refund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Long> {
    // UPDATED: Changed to Optional for safer handling
    Optional<Refund> findByBookingId(Long bookingId);

    // ADDED: To find pending refunds
    List<Refund> findByStatus(String status);
}