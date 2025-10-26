package com.hotelmanagement.system.controller;

import com.hotelmanagement.system.model.HotelCard;
import com.hotelmanagement.system.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users/{userId}/wallet")
public class WalletRestController {

    @Autowired
    private WalletService walletService;

    /**
     * Get all cards for a user's wallet
     */
    @GetMapping("/cards")
    public ResponseEntity<?> getWalletCards(@PathVariable Long userId) {
        try {
            List<HotelCard> cards = walletService.getWalletCards(userId);
            return ResponseEntity.ok(cards);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching cards: " + e.getMessage());
        }
    }

    /**
     * Add a new card to wallet
     */
    @PostMapping("/cards")
    public ResponseEntity<?> addCard(@PathVariable Long userId, @RequestBody HotelCard cardRequest) {
        try {
            if (cardRequest.getCardHolderName() == null || cardRequest.getCardHolderName().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Card holder name is required");
            }
            if (cardRequest.getCardNumber() == null || cardRequest.getCardNumber().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Card number is required");
            }
            if (cardRequest.getCvv() == null || cardRequest.getCvv().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("CVV is required");
            }
            if (cardRequest.getExpiryDate() == null) {
                return ResponseEntity.badRequest().body("Expiry date is required");
            }

            HotelCard newCard = walletService.addCardToWallet(userId, cardRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(newCard);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error adding card: " + e.getMessage());
        }
    }

    /**
     * Update card details (only cardholder name, cannot update default card)
     */
    @PutMapping("/cards/{cardId}")
    public ResponseEntity<?> updateCard(@PathVariable Long userId,
                                        @PathVariable Long cardId,
                                        @RequestBody Map<String, String> updates) {
        try {
            String cardHolderName = updates.get("cardHolderName");
            if (cardHolderName == null || cardHolderName.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Card holder name is required");
            }

            HotelCard updatedCard = walletService.updateCardDetails(userId, cardId, cardHolderName);
            return ResponseEntity.ok(updatedCard);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating card: " + e.getMessage());
        }
    }

    /**
     * Delete a card from wallet (cannot delete default card)
     */
    @DeleteMapping("/cards/{cardId}")
    public ResponseEntity<?> deleteCard(@PathVariable Long userId, @PathVariable Long cardId) {
        try {
            walletService.deleteCardFromWallet(userId, cardId);
            return ResponseEntity.ok().body("Card deleted successfully");
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting card: " + e.getMessage());
        }
    }

    /**
     * Set a card as default
     */
    @PostMapping("/cards/{cardId}/set-default")
    public ResponseEntity<?> setDefaultCard(@PathVariable Long userId, @PathVariable Long cardId) {
        try {
            walletService.setDefaultCard(userId, cardId);
            return ResponseEntity.ok().body("Default card updated successfully");
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error setting default card: " + e.getMessage());
        }
    }

    /**
     * Get wallet statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<?> getWalletStatistics(@PathVariable Long userId) {
        try {
            Map<String, Object> stats = walletService.getWalletStatistics(userId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching statistics: " + e.getMessage());
        }
    }
}