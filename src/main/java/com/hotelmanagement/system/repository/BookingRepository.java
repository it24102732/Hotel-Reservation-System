package com.hotelmanagement.system.repository;

import com.hotelmanagement.system.model.Booking;
import com.hotelmanagement.system.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByStatus(String status);

    // --- THIS IS THE NEW METHOD THAT FIXES THE ERROR ---
    /**
     * Finds all bookings associated with a specific user ID.
     * @param userId The ID of the user.
     * @return A list of bookings for that user.
     */
    List<Booking> findByUserId(Long userId);

    // --- Method for statistics cards ---
    long countByStatus(String status);

    // --- Method for the search bar ---
    @Query("SELECT b FROM Booking b WHERE " +
            "LOWER(b.user.name) LIKE CONCAT('%', :searchTerm, '%') OR " +
            "LOWER(b.room.roomNumber) LIKE CONCAT('%', :searchTerm, '%') OR " +
            "CAST(b.id AS string) LIKE CONCAT('%', :searchTerm, '%')")
    List<Booking> searchBookings(@Param("searchTerm") String searchTerm);

    @Query("SELECT r FROM Room r WHERE r.isAvailable = true AND r.id NOT IN (" +
            "SELECT b.room.id FROM Booking b WHERE b.status NOT IN ('CANCELLED', 'CHECKED_OUT') AND " +
            "((b.checkInDate < :checkOutDate) AND (b.checkOutDate > :checkInDate)))")
    List<Room> findAvailableRoomsForDateRange(@Param("checkInDate") LocalDate checkInDate, @Param("checkOutDate") LocalDate checkOutDate);
}