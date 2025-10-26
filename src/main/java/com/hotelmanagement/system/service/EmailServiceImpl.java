package com.hotelmanagement.system.service;

import org.springframework.scheduling.annotation.Async;
import com.hotelmanagement.system.model.Booking;
import com.hotelmanagement.system.model.Refund;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Autowired
    private JavaMailSender emailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async
    @Override
    public void sendBookingConfirmationEmail(String to, Booking booking) {
        try {
            logger.info("📧 Attempting to send booking confirmation email to: {}", to);

            if (emailSender == null) {
                logger.error("❌ JavaMailSender is NULL!");
                return;
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Booking Confirmation - Hotel Management System");

            String roomInfo = (booking.getRoom() != null)
                    ? "Room: " + booking.getRoom().getRoomNumber() + " (" + booking.getRoom().getType() + ")"
                    : "Room Type: " + booking.getRoomType() + " (Room number will be assigned by our staff)";

            message.setText("Dear " + booking.getUser().getName() + ",\n\n" +
                    "Thank you for your booking at our hotel.\n\n" +
                    "Booking Details:\n" +
                    "Booking ID: #" + booking.getId() + "\n" +
                    roomInfo + "\n" +
                    "Check-in Date: " + booking.getCheckInDate() + "\n" +
                    "Check-out Date: " + booking.getCheckOutDate() + "\n" +
                    "Total Price: $" + String.format("%.2f", booking.getTotalPrice()) + "\n" +
                    "Status: " + booking.getStatus() + "\n\n" +
                    "Please note that your booking is currently pending payment. Once payment is received, " +
                    "your booking will be confirmed.\n\n" +
                    "If you have any questions, please contact us.\n\n" +
                    "Best regards,\n" +
                    "Hotel Management Team");

            emailSender.send(message);
            logger.info("✅ Booking confirmation email sent successfully to: {}", to);
        } catch (Exception e) {
            logger.error("❌ Failed to send booking confirmation email to: {}. Error: {}", to, e.getMessage());
            e.printStackTrace();
        }
    }

    @Async
    @Override
    public void sendBookingConfirmedEmail(String to, Booking booking) {
        try {
            logger.info("📧 Attempting to send booking confirmed email to: {}", to);

            if (emailSender == null) {
                logger.error("❌ JavaMailSender is NULL!");
                return;
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Payment Successful - Booking Confirmed!");

            String roomInfo = (booking.getRoom() != null)
                    ? "Room: " + booking.getRoom().getRoomNumber() + " (" + booking.getRoom().getType() + ")"
                    : "Room Type: " + booking.getRoomType() + " (Room number will be assigned shortly)";

            message.setText("Dear " + booking.getUser().getName() + ",\n\n" +
                    "Great news! Your payment has been successfully processed and your booking is now CONFIRMED!\n\n" +
                    "Booking Details:\n" +
                    "Booking ID: #" + booking.getId() + "\n" +
                    roomInfo + "\n" +
                    "Check-in Date: " + booking.getCheckInDate() + "\n" +
                    "Check-out Date: " + booking.getCheckOutDate() + "\n" +
                    "Total Price Paid: $" + String.format("%.2f", booking.getTotalPrice()) + "\n" +
                    "Status: CONFIRMED\n\n" +
                    "We look forward to welcoming you at our hotel!\n\n" +
                    "Best regards,\n" +
                    "Hotel Management Team");

            emailSender.send(message);
            logger.info("✅ Booking confirmed email sent successfully to: {}", to);
        } catch (Exception e) {
            logger.error("❌ Failed to send booking confirmed email to: {}. Error: {}", to, e.getMessage());
            e.printStackTrace();
        }
    }

    @Async
    @Override
    public void sendBookingCancelledEmail(String to, Booking booking) {
        try {
            logger.info("📧 Attempting to send booking cancelled email to: {}", to);

            if (emailSender == null) {
                logger.error("❌ JavaMailSender is NULL!");
                return;
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Booking Cancelled - Hotel Management System");

            String roomInfo = (booking.getRoom() != null)
                    ? "Room: " + booking.getRoom().getRoomNumber() + " (" + booking.getRoom().getType() + ")"
                    : "Room Type: " + booking.getRoomType();

            message.setText("Dear " + booking.getUser().getName() + ",\n\n" +
                    "Your booking has been cancelled as requested.\n\n" +
                    "Cancelled Booking Details:\n" +
                    "Booking ID: #" + booking.getId() + "\n" +
                    roomInfo + "\n" +
                    "Check-in Date: " + booking.getCheckInDate() + "\n" +
                    "Check-out Date: " + booking.getCheckOutDate() + "\n" +
                    "Original Amount: $" + String.format("%.2f", booking.getTotalPrice()) + "\n\n" +
                    "If a payment was made, a refund request has been submitted to our finance team. " +
                    "You will receive a separate email once the refund is processed.\n\n" +
                    "If you have any questions, please contact us.\n\n" +
                    "Best regards,\n" +
                    "Hotel Management Team");

            emailSender.send(message);
            logger.info("✅ Booking cancelled email sent successfully to: {}", to);
        } catch (Exception e) {
            logger.error("❌ Failed to send booking cancelled email to: {}. Error: {}", to, e.getMessage());
            e.printStackTrace();
        }
    }

    @Async
    @Override
    public void sendCheckinConfirmationEmail(String to, Booking booking) {
        try {
            logger.info("📧 Attempting to send check-in confirmation email to: {}", to);

            if (emailSender == null) {
                logger.error("❌ JavaMailSender is NULL!");
                return;
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Welcome! Check-in Confirmed");

            String roomInfo = (booking.getRoom() != null)
                    ? "Room: " + booking.getRoom().getRoomNumber() + " (" + booking.getRoom().getType() + ")"
                    : "Room Type: " + booking.getRoomType() + " (Please contact reception for room assignment)";

            message.setText("Dear " + booking.getUser().getName() + ",\n\n" +
                    "Welcome to our hotel! This is to confirm your check-in.\n\n" +
                    "Booking Details:\n" +
                    "Booking ID: #" + booking.getId() + "\n" +
                    roomInfo + "\n" +
                    "Check-in Date: " + booking.getCheckInDate() + "\n" +
                    "Check-out Date: " + booking.getCheckOutDate() + "\n" +
                    "Status: CHECKED IN\n\n" +
                    "We hope you enjoy your stay with us. If you need any assistance, " +
                    "please don't hesitate to contact our reception desk.\n\n" +
                    "Best regards,\n" +
                    "Hotel Management Team");

            emailSender.send(message);
            logger.info("✅ Check-in confirmation email sent successfully to: {}", to);
        } catch (Exception e) {
            logger.error("❌ Failed to send check-in confirmation email to: {}. Error: {}", to, e.getMessage());
            e.printStackTrace();
        }
    }

    @Async
    @Override
    public void sendCheckoutConfirmationEmail(String to, Booking booking) {
        try {
            logger.info("📧 Attempting to send check-out confirmation email to: {}", to);

            if (emailSender == null) {
                logger.error("❌ JavaMailSender is NULL!");
                return;
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Thank You! Check-out Confirmation");

            String roomInfo = (booking.getRoom() != null)
                    ? "Room: " + booking.getRoom().getRoomNumber() + " (" + booking.getRoom().getType() + ")"
                    : "Room Type: " + booking.getRoomType();

            message.setText("Dear " + booking.getUser().getName() + ",\n\n" +
                    "Thank you for staying with us! This is to confirm your check-out.\n\n" +
                    "Booking Details:\n" +
                    "Booking ID: #" + booking.getId() + "\n" +
                    roomInfo + "\n" +
                    "Check-in Date: " + booking.getCheckInDate() + "\n" +
                    "Check-out Date: " + booking.getCheckOutDate() + "\n" +
                    "Total Amount: $" + String.format("%.2f", booking.getTotalPrice()) + "\n" +
                    "Status: CHECKED OUT\n\n" +
                    "We hope you had a pleasant stay. We would love to welcome you again!\n\n" +
                    "Best regards,\n" +
                    "Hotel Management Team");

            emailSender.send(message);
            logger.info("✅ Check-out confirmation email sent successfully to: {}", to);
        } catch (Exception e) {
            logger.error("❌ Failed to send check-out confirmation email to: {}. Error: {}", to, e.getMessage());
            e.printStackTrace();
        }
    }

    @Async
    @Override
    public void sendStatusUpdateEmail(String to, Booking booking) {
        try {
            logger.info("📧 Attempting to send status update email to: {}", to);

            if (emailSender == null) {
                logger.error("❌ JavaMailSender is NULL!");
                return;
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Booking Status Update - Hotel Management System");

            String roomInfo = (booking.getRoom() != null)
                    ? "Room: " + booking.getRoom().getRoomNumber() + " (" + booking.getRoom().getType() + ")"
                    : "Room Type: " + booking.getRoomType();

            message.setText("Dear " + booking.getUser().getName() + ",\n\n" +
                    "The status of your booking has been updated.\n\n" +
                    "Booking Details:\n" +
                    "Booking ID: #" + booking.getId() + "\n" +
                    roomInfo + "\n" +
                    "Check-in Date: " + booking.getCheckInDate() + "\n" +
                    "Check-out Date: " + booking.getCheckOutDate() + "\n" +
                    "New Status: " + booking.getStatus() + "\n\n" +
                    "If you have any questions about this update, please contact us.\n\n" +
                    "Best regards,\n" +
                    "Hotel Management Team");

            emailSender.send(message);
            logger.info("✅ Status update email sent successfully to: {}", to);
        } catch (Exception e) {
            logger.error("❌ Failed to send status update email to: {}. Error: {}", to, e.getMessage());
            e.printStackTrace();
        }
    }

    @Async
    public void sendRoomAssignmentEmail(String to, Booking booking) {
        try {
            logger.info("📧 Attempting to send room assignment email to: {}", to);

            if (emailSender == null) {
                logger.error("❌ JavaMailSender is NULL!");
                return;
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Room Assigned to Your Booking!");

            message.setText("Dear " + booking.getUser().getName() + ",\n\n" +
                    "Great news! A room has been assigned to your booking.\n\n" +
                    "Booking Details:\n" +
                    "Booking ID: #" + booking.getId() + "\n" +
                    "Assigned Room: " + booking.getRoom().getRoomNumber() + " (" + booking.getRoom().getType() + ")\n" +
                    "Check-in Date: " + booking.getCheckInDate() + "\n" +
                    "Check-out Date: " + booking.getCheckOutDate() + "\n" +
                    "Total Price: $" + String.format("%.2f", booking.getTotalPrice()) + "\n\n" +
                    "We look forward to welcoming you!\n\n" +
                    "Best regards,\n" +
                    "Hotel Management Team");

            emailSender.send(message);
            logger.info("✅ Room assignment email sent successfully to: {}", to);
        } catch (Exception e) {
            logger.error("❌ Failed to send room assignment email to: {}. Error: {}", to, e.getMessage());
            e.printStackTrace();
        }
    }

    @Async
    @Override
    public void sendRefundProcessedEmail(String to, Refund refund) {
        try {
            logger.info("📧 Attempting to send refund processed email to: {}", to);

            if (emailSender == null) {
                logger.error("❌ JavaMailSender is NULL!");
                return;
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("✅ Refund Processed Successfully");

            message.setText("Dear " + refund.getBooking().getUser().getName() + ",\n\n" +
                    "Great news! Your refund has been successfully processed.\n\n" +
                    "Refund Details:\n" +
                    "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                    "Booking ID: #" + refund.getBooking().getId() + "\n" +
                    "Refund Amount: $" + String.format("%.2f", refund.getAmount()) + "\n" +
                    "Refund Transaction ID: " + refund.getRefundTransactionId() + "\n" +
                    "Processed Date: " + refund.getProcessedAt().toLocalDate() + "\n" +
                    "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                    "The refund amount has been credited to your default card in your wallet.\n" +
                    "You can check your updated balance by logging into your account and " +
                    "navigating to the wallet section.\n\n" +
                    "Original Booking Details:\n" +
                    "Check-in Date: " + refund.getBooking().getCheckInDate() + "\n" +
                    "Check-out Date: " + refund.getBooking().getCheckOutDate() + "\n\n" +
                    "If you have any questions about this refund, please don't hesitate to contact us.\n\n" +
                    "We apologize for any inconvenience and hope to serve you again in the future.\n\n" +
                    "Best regards,\n" +
                    "Hotel Management Finance Team\n" +
                    "Email: " + fromEmail);

            emailSender.send(message);
            logger.info("✅ Refund processed email sent successfully to: {}", to);
        } catch (Exception e) {
            logger.error("❌ Failed to send refund processed email to: {}. Error: {}", to, e.getMessage());
            e.printStackTrace();
        }
    }

    @Async
    @Override
    public void sendRefundFailedEmail(String to, Refund refund, String reason) {
        try {
            logger.info("📧 Attempting to send refund failed email to: {}", to);

            if (emailSender == null) {
                logger.error("❌ JavaMailSender is NULL!");
                return;
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("⚠️ Refund Processing Issue - Action Required");

            message.setText("Dear " + refund.getBooking().getUser().getName() + ",\n\n" +
                    "We regret to inform you that we encountered an issue while processing your refund.\n\n" +
                    "Refund Details:\n" +
                    "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                    "Booking ID: #" + refund.getBooking().getId() + "\n" +
                    "Refund Amount: $" + String.format("%.2f", refund.getAmount()) + "\n" +
                    "Status: FAILED\n" +
                    "Reason: " + reason + "\n" +
                    "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                    "What happens next?\n" +
                    "• Our finance team has been automatically notified\n" +
                    "• A specialist will review your case within 24 hours\n" +
                    "• We will contact you directly to resolve this issue\n\n" +
                    "Need immediate assistance?\n" +
                    "Contact our support team:\n" +
                    "Email: " + fromEmail + "\n\n" +
                    "We sincerely apologize for any inconvenience this may have caused.\n\n" +
                    "Best regards,\n" +
                    "Hotel Management Finance Team");

            emailSender.send(message);
            logger.info("⚠️ Refund failed email sent successfully to: {}", to);
        } catch (Exception e) {
            logger.error("❌ Failed to send refund failed email to: {}. Error: {}", to, e.getMessage());
            e.printStackTrace();
        }
    }

    @Async
    public void sendRefundRejectedEmail(String to, Refund refund, String rejectionReason) {
        try {
            logger.info("📧 Attempting to send refund rejected email to: {}", to);

            if (emailSender == null) {
                logger.error("❌ JavaMailSender is NULL!");
                return;
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("❌ Refund Request Rejected");

            message.setText("Dear " + refund.getBooking().getUser().getName() + ",\n\n" +
                    "After reviewing your refund request, we regret to inform you that " +
                    "your refund application has been rejected.\n\n" +
                    "Refund Request Details:\n" +
                    "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                    "Booking ID: #" + refund.getBooking().getId() + "\n" +
                    "Requested Amount: $" + String.format("%.2f", refund.getAmount()) + "\n" +
                    "Status: REJECTED\n" +
                    "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                    "Reason for Rejection:\n" +
                    rejectionReason + "\n\n" +
                    "Booking Details:\n" +
                    "Check-in Date: " + refund.getBooking().getCheckInDate() + "\n" +
                    "Check-out Date: " + refund.getBooking().getCheckOutDate() + "\n" +
                    "Total Amount: $" + String.format("%.2f", refund.getBooking().getTotalPrice()) + "\n\n" +
                    "If you believe this decision was made in error or you have additional " +
                    "information to support your refund request, please contact us:\n\n" +
                    "Contact Information:\n" +
                    "Email: " + fromEmail + "\n" +
                    "Subject: Refund Appeal - Booking #" + refund.getBooking().getId() + "\n\n" +
                    "Our customer service team will review your appeal within 48 hours.\n\n" +
                    "We appreciate your understanding.\n\n" +
                    "Best regards,\n" +
                    "Hotel Management Finance Team");

            emailSender.send(message);
            logger.info("❌ Refund rejected email sent successfully to: {}", to);
        } catch (Exception e) {
            logger.error("❌ Failed to send refund rejected email to: {}. Error: {}", to, e.getMessage());
            e.printStackTrace();
        }
    }

    @Async
    public void sendRefundRequestedEmail(String to, Refund refund) {
        try {
            logger.info("📧 Attempting to send refund requested email to: {}", to);

            if (emailSender == null) {
                logger.error("❌ JavaMailSender is NULL!");
                return;
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("🔄 Refund Request Received");

            message.setText("Dear " + refund.getBooking().getUser().getName() + ",\n\n" +
                    "We have received your refund request and it is currently being processed.\n\n" +
                    "Refund Request Details:\n" +
                    "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                    "Booking ID: #" + refund.getBooking().getId() + "\n" +
                    "Refund Amount: $" + String.format("%.2f", refund.getAmount()) + "\n" +
                    "Request Date: " + refund.getRequestedAt().toLocalDate() + "\n" +
                    "Status: PENDING REVIEW\n" +
                    "Reason: " + refund.getReason() + "\n" +
                    "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                    "What happens next?\n" +
                    "• Our finance team will review your request\n" +
                    "• Processing typically takes 3-5 business days\n" +
                    "• You will receive an email notification once processed\n\n" +
                    "Booking Details:\n" +
                    "Check-in Date: " + refund.getBooking().getCheckInDate() + "\n" +
                    "Check-out Date: " + refund.getBooking().getCheckOutDate() + "\n\n" +
                    "If you have any questions, please contact us at " + fromEmail + "\n\n" +
                    "Thank you for your patience.\n\n" +
                    "Best regards,\n" +
                    "Hotel Management Finance Team");

            emailSender.send(message);
            logger.info("🔄 Refund requested email sent successfully to: {}", to);
        } catch (Exception e) {
            logger.error("❌ Failed to send refund requested email to: {}. Error: {}", to, e.getMessage());
            e.printStackTrace();
        }
    }
}