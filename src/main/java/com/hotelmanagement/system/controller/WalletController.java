package com.hotelmanagement.system.controller;

import com.hotelmanagement.system.model.HotelCard;
import com.hotelmanagement.system.model.User;
import com.hotelmanagement.system.service.UserService;
import com.hotelmanagement.system.service.WalletService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/customer/wallet")
public class WalletController {

    @Autowired
    private WalletService walletService;

    @Autowired
    private UserService userService;

    /**
     * Get logged-in user from session
     */
    private User getLoggedInUser(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            throw new SecurityException("User not logged in");
        }
        return userService.getUserById(userId);
    }

    /**
     * Show wallet page
     */
    @GetMapping
    public String showWallet(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        try {
            User loggedInUser = getLoggedInUser(session);
            List<HotelCard> cards = walletService.getWalletCards(loggedInUser.getId());
            Map<String, Object> statistics = walletService.getWalletStatistics(loggedInUser.getId());

            model.addAttribute("loggedInUser", loggedInUser);
            model.addAttribute("cards", cards);
            model.addAttribute("statistics", statistics);
            model.addAttribute("totalBalance", statistics.get("totalBalance"));

            return "wallet";
        } catch (SecurityException e) {
            redirectAttributes.addFlashAttribute("error", "Please login to access your wallet.");
            return "redirect:/customer/login";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error loading wallet: " + e.getMessage());
            return "redirect:/customer/dashboard";
        }
    }

    /**
     * Add new card to wallet - FIXED VERSION
     */
    @PostMapping("/cards/add")
    public String addCard(
            @RequestParam String cardHolderName,
            @RequestParam String cardNumber,
            @RequestParam String cvv,  // ✅ ADDED CVV PARAMETER
            @RequestParam String expiryDate,  // ✅ Changed to String to handle Vue.js format
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            User loggedInUser = getLoggedInUser(session);

            System.out.println("=== ADD CARD DEBUG ===");
            System.out.println("Card Holder: " + cardHolderName);
            System.out.println("Card Number: " + cardNumber);
            System.out.println("CVV: " + cvv);
            System.out.println("Expiry Date: " + expiryDate);

            // Validate inputs
            if (cardHolderName == null || cardHolderName.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Card holder name is required.");
                return "redirect:/customer/wallet";
            }

            if (cardNumber == null || cardNumber.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Card number is required.");
                return "redirect:/customer/wallet";
            }

            if (cvv == null || cvv.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "CVV is required.");
                return "redirect:/customer/wallet";
            }

            if (expiryDate == null || expiryDate.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Expiry date is required.");
                return "redirect:/customer/wallet";
            }

            // Parse expiry date
            LocalDate parsedExpiryDate;
            try {
                parsedExpiryDate = LocalDate.parse(expiryDate);
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Invalid expiry date format.");
                return "redirect:/customer/wallet";
            }

            // Check if card is expired
            if (parsedExpiryDate.isBefore(LocalDate.now())) {
                redirectAttributes.addFlashAttribute("error", "Card has expired. Please use a valid card.");
                return "redirect:/customer/wallet";
            }

            HotelCard newCard = new HotelCard();
            newCard.setCardHolderName(cardHolderName.trim().toUpperCase());
            newCard.setCardNumber(cardNumber.replaceAll("\\s", ""));
            newCard.setCvv(cvv.trim());  // ✅ SET CVV
            newCard.setExpiryDate(parsedExpiryDate);

            walletService.addCardToWallet(loggedInUser.getId(), newCard);

            System.out.println("Card added successfully!");

            redirectAttributes.addFlashAttribute("success", "Card added successfully! Your new card has $0.00 balance.");

            return "redirect:/customer/wallet";
        } catch (SecurityException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Please login to add a card.");
            return "redirect:/customer/login";
        } catch (RuntimeException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/customer/wallet";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "An unexpected error occurred: " + e.getMessage());
            return "redirect:/customer/wallet";
        }
    }

    /**
     * Delete card from wallet
     */
    @PostMapping("/cards/{cardId}/delete")
    public String deleteCard(
            @PathVariable Long cardId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            User loggedInUser = getLoggedInUser(session);
            walletService.deleteCardFromWallet(loggedInUser.getId(), cardId);
            redirectAttributes.addFlashAttribute("success", "Card deleted successfully.");

            return "redirect:/customer/wallet";
        } catch (SecurityException e) {
            redirectAttributes.addFlashAttribute("error", "Security error: " + e.getMessage());
            return "redirect:/customer/wallet";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/customer/wallet";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "An unexpected error occurred: " + e.getMessage());
            return "redirect:/customer/wallet";
        }
    }

    /**
     * Update card details (cardholder name only)
     */
    @PostMapping("/cards/{cardId}/edit")
    public String editCard(
            @PathVariable Long cardId,
            @RequestParam String cardHolderName,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            User loggedInUser = getLoggedInUser(session);

            if (cardHolderName == null || cardHolderName.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Card holder name is required.");
                return "redirect:/customer/wallet";
            }

            walletService.updateCardDetails(loggedInUser.getId(), cardId, cardHolderName.trim());
            redirectAttributes.addFlashAttribute("success", "Card updated successfully.");

            return "redirect:/customer/wallet";
        } catch (SecurityException e) {
            redirectAttributes.addFlashAttribute("error", "Security error: " + e.getMessage());
            return "redirect:/customer/wallet";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/customer/wallet";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "An unexpected error occurred: " + e.getMessage());
            return "redirect:/customer/wallet";
        }
    }

    /**
     * Set card as default
     */
    @PostMapping("/cards/{cardId}/set-default")
    public String setDefaultCard(
            @PathVariable Long cardId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            User loggedInUser = getLoggedInUser(session);
            walletService.setDefaultCard(loggedInUser.getId(), cardId);
            redirectAttributes.addFlashAttribute("success", "Default card updated successfully.");

            return "redirect:/customer/wallet";
        } catch (SecurityException e) {
            redirectAttributes.addFlashAttribute("error", "Please login to update default card.");
            return "redirect:/customer/login";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/customer/wallet";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "An unexpected error occurred: " + e.getMessage());
            return "redirect:/customer/wallet";
        }
    }
}