package com.hotelmanagement.system.repository;

import com.hotelmanagement.system.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByIsAvailable(boolean isAvailable);

    // Find rooms by type
    List<Room> findByType(String type);

    // Find rooms available for a specific date range
    @Query("SELECT r FROM Room r WHERE r.isAvailable = true AND NOT EXISTS (" +
            "SELECT b FROM Booking b WHERE b.room = r AND b.status NOT IN ('CANCELLED', 'CHECKED_OUT') " +
            "AND ((b.checkInDate <= :checkOutDate) AND (b.checkOutDate >= :checkInDate)))")
    List<Room> findAvailableRoomsForDateRange(@Param("checkInDate") LocalDate checkInDate,
                                              @Param("checkOutDate") LocalDate checkOutDate);

    // Find rooms by price range
    @Query("SELECT r FROM Room r WHERE r.price BETWEEN :minPrice AND :maxPrice ORDER BY r.price")
    List<Room> findByPriceRange(@Param("minPrice") double minPrice, @Param("maxPrice") double maxPrice);
}