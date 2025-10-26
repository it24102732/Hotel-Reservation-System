package com.hotelmanagement.system.repository;

import com.hotelmanagement.system.model.HotelCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HotelCardRepository extends JpaRepository<HotelCard, Long> {
    Optional<HotelCard> findByCardNumber(String cardNumber);
    List<HotelCard> findByUserId(Long userId);

    // This method is critical for finding the default card for refunds
    Optional<HotelCard> findByUserIdAndIsDefaultTrue(Long userId);
}