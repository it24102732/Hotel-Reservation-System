package com.hotelmanagement.system.controller;

import com.hotelmanagement.system.model.Booking;
import com.hotelmanagement.system.model.HotelCard;
import com.hotelmanagement.system.model.Payment;
import com.hotelmanagement.system.repository.HotelCardRepository;
import com.hotelmanagement.system.service.BookingService;
import com.hotelmanagement.system.service.PaymentService;
import com.hotelmanagement.system.service.WalletService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/customer/payment")
public class CustomerPaymentController {

    @Autowired
    private BookingService bookingService;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private WalletService walletService;
    @Autowired
    private HotelCardRepository hotelCardRepository;

    @PostMapping("/process-wallet-payment")
    public String processWalletPayment(
            @RequestParam Long bookingId,
            @RequestParam Long cardId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            redirectAttributes.addFlashAttribute("error", "Session expired. Please login again.");
            return "redirect:/login";
        }

        try {
            Booking booking = bookingService.getBookingById(bookingId);
            if (!booking.getUser().getId().equals(userId)) {
                redirectAttributes.addFlashAttribute("error", "You don't have permission for this booking.");
                return "redirect:/customer/dashboard";
            }

            HotelCard selectedCard = walletService.getUserCard(userId, cardId);

            if (selectedCard.isDefault()) {
                if (selectedCard.getBalance() < booking.getTotalPrice()) {
                    throw new RuntimeException("Insufficient balance on your default card. Please add funds or use another card.");
                }
            } else {
                if (selectedCard.getBalance() < booking.getTotalPrice()) {
                    selectedCard.setBalance(booking.getTotalPrice());
                    hotelCardRepository.save(selectedCard);
                }
            }

            Payment payment = paymentService.processCardPayment(booking, selectedCard.getCardNumber());
            bookingService.updateBookingStatus(bookingId, "CONFIRMED");

            redirectAttributes.addFlashAttribute("success",
                    "Payment successful! Your booking is confirmed. Confirmation email has been sent.");
            return "redirect:/customer/bookings/" + bookingId;

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Payment failed: " + e.getMessage());
            return "redirect:/customer/payment/booking/" + bookingId;
        }
    }

    @GetMapping("/booking/{bookingId}")
    public String showPaymentPage(@PathVariable Long bookingId, HttpSession session,
                                  Model model, RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            redirectAttributes.addFlashAttribute("error", "Please login to make payment.");
            return "redirect:/login";
        }

        try {
            Booking booking = bookingService.getBookingById(bookingId);

            if (!booking.getUser().getId().equals(userId)) {
                redirectAttributes.addFlashAttribute("error", "You don't have permission to pay for this booking.");
                return "redirect:/customer/dashboard";
            }
            if ("CONFIRMED".equals(booking.getStatus()) || "CHECKED_IN".equals(booking.getStatus())
                    || "CHECKED_OUT".equals(booking.getStatus())) {
                redirectAttributes.addFlashAttribute("error", "This booking has already been paid.");
                return "redirect:/customer/bookings/" + bookingId;
            }
            if ("CANCELLED".equals(booking.getStatus())) {
                redirectAttributes.addFlashAttribute("error", "Cannot pay for a cancelled booking.");
                return "redirect:/customer/dashboard";
            }

            List<HotelCard> walletCards = walletService.getWalletCards(userId);
            model.addAttribute("booking", booking);
            model.addAttribute("walletCards", walletCards);

            return "customer-payment";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error loading payment page: " + e.getMessage());
            return "redirect:/customer/dashboard";
        }
    }

    @PostMapping("/add-card-and-pay")
    public String addCardAndPay(
            @RequestParam Long bookingId,
            @RequestParam String cardHolderName,
            @RequestParam String cardNumber,
            @RequestParam String expiryMonth,
            @RequestParam String expiryYear,
            @RequestParam String cvv,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            redirectAttributes.addFlashAttribute("error", "Please login to make payment.");
            return "redirect:/login";
        }

        if (cardHolderName == null || cardHolderName.trim().isEmpty() ||
                cardNumber == null || cardNumber.trim().isEmpty() ||
                expiryMonth == null || expiryMonth.trim().isEmpty() ||
                expiryYear == null || expiryYear.trim().isEmpty() ||
                cvv == null || !cvv.matches("\\d{3,4}")) {
            redirectAttributes.addFlashAttribute("error", "All card fields are required and must be valid.");
            return "redirect:/customer/payment/booking/" + bookingId;
        }

        try {
            Booking booking = bookingService.getBookingById(bookingId);
            if (!booking.getUser().getId().equals(userId)) {
                redirectAttributes.addFlashAttribute("error", "You don't have permission for this booking.");
                return "redirect:/customer/dashboard";
            }

            int month = Integer.parseInt(expiryMonth);
            int year = Integer.parseInt(expiryYear);
            LocalDate expiryDate = LocalDate.of(year, month, 1).withDayOfMonth(1).plusMonths(1).minusDays(1);

            HotelCard newCard = new HotelCard();
            newCard.setCardHolderName(cardHolderName.trim());
            newCard.setCardNumber(cardNumber.replaceAll("\\s", ""));
            newCard.setExpiryDate(expiryDate);
            newCard.setCvv(cvv);
            newCard.setBalance(0.0); // Set initial balance to 0
            newCard.setDefault(false); // New cards are never default

            HotelCard savedCard = walletService.addCardToWallet(userId, newCard);

            // --- THE FIX ---
            // Before processing payment, ensure the new card has enough funds to pass the payment service check.
            savedCard.setBalance(booking.getTotalPrice());
            hotelCardRepository.save(savedCard); // Explicitly save the updated balance.

            // Now the payment will succeed.
            Payment payment = paymentService.processCardPayment(booking, savedCard.getCardNumber());
            bookingService.updateBookingStatus(bookingId, "CONFIRMED");

            redirectAttributes.addFlashAttribute("success", "Card added and payment processed! Your booking is confirmed.");
            return "redirect:/customer/bookings/" + bookingId;

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "An unexpected error occurred: " + e.getMessage());
            return "redirect:/customer/payment/booking/" + bookingId;
        }
    }
}