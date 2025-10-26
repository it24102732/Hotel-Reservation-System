package com.hotelmanagement.system.service;

import com.hotelmanagement.system.model.Room;
import com.hotelmanagement.system.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class RoomService {

    private final RoomRepository roomRepository;

    @Autowired
    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    /**
     * Get all rooms
     */
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    /**
     * Get room by ID
     */
    public Room getRoomById(Long roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found with ID: " + roomId));
    }

    /**
     * Get rooms by type
     */
    public List<Room> getRoomsByType(String type) {
        return roomRepository.findByType(type);
    }

    /**
     * Get available rooms
     */
    public List<Room> getAvailableRooms() {
        return roomRepository.findByIsAvailable(true);
    }

    /**
     * Get available rooms for date range
     */
    public List<Room> getAvailableRoomsForDateRange(LocalDate checkIn, LocalDate checkOut) {
        return roomRepository.findAvailableRoomsForDateRange(checkIn, checkOut);
    }

    /**
     * Get rooms by price range
     */
    public List<Room> getRoomsByPriceRange(double minPrice, double maxPrice) {
        return roomRepository.findByPriceRange(minPrice, maxPrice);
    }
}