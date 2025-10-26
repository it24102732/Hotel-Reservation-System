package com.hotelmanagement.system.repository;

import com.hotelmanagement.system.model.FoodOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface FoodOrderRepository extends JpaRepository<FoodOrder, Long> {
    List<FoodOrder> findByUserId(Long userId);

    List<FoodOrder> findByStatus(String status);

    List<FoodOrder> findByRoomId(Long roomId);

    @Query("SELECT f FROM FoodOrder f WHERE f.user.id = :userId ORDER BY f.orderedAt DESC")
    List<FoodOrder> findByUserIdOrderByOrderedAtDesc(@Param("userId") Long userId);

    @Query("SELECT COUNT(f) FROM FoodOrder f WHERE f.status = :status")
    long countByStatus(@Param("status") String status);
}
