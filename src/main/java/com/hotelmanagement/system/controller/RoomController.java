package com.hotelmanagement.system.controller;

import com.hotelmanagement.system.model.Room;
import com.hotelmanagement.system.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    @Autowired
    private BookingService bookingService;

    /**
     * This is the REST API endpoint that your reservations.js file calls
     * to dynamically load available rooms when the dates change.
     */
    @GetMapping("/available")
    public ResponseEntity<List<Room>> getAvailableRooms(
            @RequestParam("checkInDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,
            @RequestParam("checkOutDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate) {

        List<Room> rooms = bookingService.getAvailableRoomsByDateRange(checkInDate, checkOutDate);
        return ResponseEntity.ok(rooms);
    }
}