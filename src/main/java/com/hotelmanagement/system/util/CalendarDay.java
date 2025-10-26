package com.hotelmanagement.system.util;

import com.hotelmanagement.system.model.Booking;
import java.util.ArrayList;
import java.util.List;

public class CalendarDay {
    private int dayNumber;
    private boolean isToday;
    private boolean isCurrentMonth;
    private List<Booking> bookings = new ArrayList<>();

    public CalendarDay(int dayNumber, boolean isToday, boolean isCurrentMonth) {
        this.dayNumber = dayNumber;
        this.isToday = isToday;
        this.isCurrentMonth = isCurrentMonth;
    }

    // Getters and Setters
    public int getDayNumber() { return dayNumber; }
    public void setDayNumber(int dayNumber) { this.dayNumber = dayNumber; }
    public boolean isToday() { return isToday; }
    public void setToday(boolean today) { isToday = today; }
    public boolean isCurrentMonth() { return isCurrentMonth; }
    public void setCurrentMonth(boolean currentMonth) { isCurrentMonth = currentMonth; }
    public List<Booking> getBookings() { return bookings; }
    public void setBookings(List<Booking> bookings) { this.bookings = bookings; }
}