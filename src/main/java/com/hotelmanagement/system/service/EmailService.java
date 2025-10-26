package com.hotelmanagement.system.service;

import com.hotelmanagement.system.model.Booking;
import com.hotelmanagement.system.model.Refund;

public interface EmailService {
    void sendBookingConfirmationEmail(String to, Booking booking);
    void sendBookingConfirmedEmail(String to, Booking booking);
    void sendBookingCancelledEmail(String to, Booking booking);
    void sendCheckinConfirmationEmail(String to, Booking booking);
    void sendCheckoutConfirmationEmail(String to, Booking booking);
    void sendStatusUpdateEmail(String to, Booking booking);

    // Refund email methods
    void sendRefundProcessedEmail(String to, Refund refund);
    void sendRefundFailedEmail(String to, Refund refund, String reason);
}