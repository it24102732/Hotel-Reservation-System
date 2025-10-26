package com.hotelmanagement.system.service;

import com.hotelmanagement.system.model.HotelCard;
import com.hotelmanagement.system.model.User;
import com.hotelmanagement.system.repository.HotelCardRepository;
import com.hotelmanagement.system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class WalletService {

    private final HotelCardRepository hotelCardRepository;
    private final UserRepository userRepository;

    // Add configuration to enable/disable strict Luhn validation
    @Value("${wallet.strict-validation:false}")
    private boolean strictValidation;

    @Autowired
    public WalletService(HotelCardRepository hotelCardRepository, UserRepository userRepository) {
        this.hotelCardRepository = hotelCardRepository;
        this.userRepository = userRepository;
    }

    /**
     * Get total balance for a user
     */
    public double getTotalBalance(Long userId) {
        List<HotelCard> cards = hotelCardRepository.findByUserId(userId);
        return cards.stream().mapToDouble(HotelCard::getBalance).sum();
    }

    /**
     * Create default card for new user registration
     */
    @Transactional
    public void createDefaultCardForUser(User user) {
        HotelCard defaultCard = new HotelCard();
        defaultCard.setUser(user);
        defaultCard.setCardHolderName(user.getName());

        // Generate unique card number that passes Luhn check
        String cardNumber = generateValidCardNumber();
        defaultCard.setCardNumber(cardNumber);
        defaultCard.setCvv("123"); // Default CVV
        defaultCard.setBalance(100.00); // Initial balance for default card only
        defaultCard.setExpiryDate(LocalDate.now().plusYears(5));
        defaultCard.setIssueDate(LocalDate.now());
        defaultCard.setDefault(true);

        hotelCardRepository.save(defaultCard);
    }

    /**
     * Add new card to wallet with enhanced validation
     */
    @Transactional
    public HotelCard addCardToWallet(Long userId, HotelCard newCard) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // Validate card holder name
        String cardHolderName = newCard.getCardHolderName();
        if (cardHolderName == null || cardHolderName.trim().isEmpty()) {
            throw new RuntimeException("Card holder name is required.");
        }

        cardHolderName = cardHolderName.trim();
        if (cardHolderName.length() < 3 || cardHolderName.length() > 50) {
            throw new RuntimeException("Card holder name must be between 3 and 50 characters.");
        }

        if (!Pattern.matches("^[A-Za-z\\s]+$", cardHolderName)) {
            throw new RuntimeException("Card holder name must contain only letters and spaces.");
        }

        // Validate and clean card number
        String cardNumber = newCard.getCardNumber().replaceAll("\\s", "").replaceAll("-", "");
        if (cardNumber.length() < 13 || cardNumber.length() > 19) {
            throw new RuntimeException("Card number must be between 13 and 19 digits.");
        }

        if (!cardNumber.matches("^\\d+$")) {
            throw new RuntimeException("Card number must contain only digits.");
        }

        // Validate using Luhn algorithm (only if strict validation is enabled)
        if (strictValidation && !validateCardNumberLuhn(cardNumber)) {
            throw new RuntimeException("Invalid card number. Failed Luhn check.");
        }

        // Log warning if Luhn check fails but strict validation is disabled
        if (!strictValidation && !validateCardNumberLuhn(cardNumber)) {
            System.out.println("⚠️ WARNING: Card number failed Luhn check but strict validation is disabled.");
        }

        // Validate card type (IIN/BIN validation)
        String cardType = detectCardType(cardNumber);
        if (cardType.equals("Unknown")) {
            System.out.println("⚠️ WARNING: Card type not recognized, but allowing registration.");
        }

        // Check if card already exists
        if (hotelCardRepository.findByCardNumber(cardNumber).isPresent()) {
            throw new RuntimeException("This card is already registered in the system.");
        }

        // Validate CVV
        String cvv = newCard.getCvv();
        if (cvv == null || cvv.trim().isEmpty()) {
            throw new RuntimeException("CVV is required.");
        }

        cvv = cvv.trim();
        if (!cvv.matches("^\\d{3,4}$")) {
            throw new RuntimeException("CVV must be 3 or 4 digits.");
        }

        // Validate expiry date
        if (newCard.getExpiryDate() == null) {
            throw new RuntimeException("Expiry date is required.");
        }

        // Check if card has expired
        if (newCard.getExpiryDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Card has expired. Please use a valid card.");
        }

        // Check if expiry date is too far in future (more than 10 years)
        if (newCard.getExpiryDate().isAfter(LocalDate.now().plusYears(10))) {
            throw new RuntimeException("Invalid expiry date. Card expiry cannot be more than 10 years in the future.");
        }

        // Validate expiry month (must be end of month)
        YearMonth expiryYearMonth = YearMonth.from(newCard.getExpiryDate());
        LocalDate lastDayOfMonth = expiryYearMonth.atEndOfMonth();
        if (!newCard.getExpiryDate().equals(lastDayOfMonth)) {
            // Auto-correct to last day of the month
            newCard.setExpiryDate(lastDayOfMonth);
        }

        // Set card properties
        newCard.setUser(user);
        newCard.setCardNumber(cardNumber);
        newCard.setCardHolderName(cardHolderName);
        newCard.setCvv(cvv);
        newCard.setDefault(false); // New cards are not default
        newCard.setIssueDate(LocalDate.now());
        newCard.setBalance(0.0); // All new cards start with 0 balance

        return hotelCardRepository.save(newCard);
    }

    /**
     * Delete card from wallet (cannot delete default card)
     */
    @Transactional
    public void deleteCardFromWallet(Long userId, Long cardId) {
        HotelCard cardToDelete = hotelCardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found with ID: " + cardId));

        // Security check
        if (!cardToDelete.getUser().getId().equals(userId)) {
            throw new SecurityException("You do not have permission to delete this card.");
        }

        // Cannot delete default card
        if (cardToDelete.isDefault()) {
            throw new RuntimeException("The default hotel card cannot be deleted. Please set another card as default first.");
        }

        hotelCardRepository.delete(cardToDelete);
    }

    /**
     * Get all wallet cards for a user
     */
    public List<HotelCard> getWalletCards(Long userId) {
        return hotelCardRepository.findByUserId(userId);
    }

    /**
     * Update card details (cardholder name only, cannot update default card)
     */
    @Transactional
    public HotelCard updateCardDetails(Long userId, Long cardId, String cardHolderName) {
        HotelCard card = getUserCard(userId, cardId);

        // Cannot edit default card
        if (card.isDefault()) {
            throw new RuntimeException("The default hotel card cannot be edited.");
        }

        // Validate cardholder name
        if (cardHolderName == null || cardHolderName.trim().isEmpty()) {
            throw new RuntimeException("Card holder name is required.");
        }

        cardHolderName = cardHolderName.trim();
        if (cardHolderName.length() < 3 || cardHolderName.length() > 50) {
            throw new RuntimeException("Card holder name must be between 3 and 50 characters.");
        }

        if (!Pattern.matches("^[A-Za-z\\s]+$", cardHolderName)) {
            throw new RuntimeException("Card holder name must contain only letters and spaces.");
        }

        card.setCardHolderName(cardHolderName);
        return hotelCardRepository.save(card);
    }

    /**
     * Get a specific user's card
     */
    public HotelCard getUserCard(Long userId, Long cardId) {
        HotelCard card = hotelCardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found with ID: " + cardId));

        if (!card.getUser().getId().equals(userId)) {
            throw new SecurityException("This card does not belong to the specified user.");
        }

        return card;
    }

    /**
     * Update card balance (internal use only - for booking transactions)
     */
    @Transactional
    public void updateCardBalance(Long cardId, double newBalance) {
        HotelCard card = hotelCardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found with ID: " + cardId));

        if (newBalance < 0) {
            throw new RuntimeException("Balance cannot be negative.");
        }

        card.setBalance(newBalance);
        hotelCardRepository.save(card);
    }

    /**
     * Set a card as default
     */
    @Transactional
    public void setDefaultCard(Long userId, Long cardId) {
        HotelCard card = getUserCard(userId, cardId);

        // Check if card is expired
        if (card.getExpiryDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Cannot set an expired card as default.");
        }

        // Remove default from all other cards
        List<HotelCard> userCards = hotelCardRepository.findByUserId(userId);
        for (HotelCard userCard : userCards) {
            if (userCard.isDefault()) {
                userCard.setDefault(false);
                hotelCardRepository.save(userCard);
            }
        }

        // Set this card as default
        card.setDefault(true);
        hotelCardRepository.save(card);
    }

    /**
     * Get wallet statistics
     */
    public Map<String, Object> getWalletStatistics(Long userId) {
        List<HotelCard> cards = hotelCardRepository.findByUserId(userId);
        LocalDate today = LocalDate.now();
        LocalDate threeMonthsFromNow = today.plusMonths(3);

        long totalCards = cards.size();
        long activeCards = cards.stream()
                .filter(card -> card.getExpiryDate() != null && card.getExpiryDate().isAfter(today))
                .count();

        long expiringCards = cards.stream()
                .filter(card -> card.getExpiryDate() != null &&
                        card.getExpiryDate().isAfter(today) &&
                        card.getExpiryDate().isBefore(threeMonthsFromNow))
                .count();

        double totalBalance = cards.stream()
                .mapToDouble(HotelCard::getBalance)
                .sum();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCards", totalCards);
        stats.put("activeCards", activeCards);
        stats.put("expiringCards", expiringCards);
        stats.put("totalBalance", totalBalance);

        return stats;
    }

    /**
     * Validate card number using Luhn algorithm
     */
    private boolean validateCardNumberLuhn(String cardNumber) {
        if (cardNumber == null || !cardNumber.matches("^\\d{13,19}$")) {
            return false;
        }

        int sum = 0;
        boolean isEven = false;

        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(cardNumber.charAt(i));

            if (isEven) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }

            sum += digit;
            isEven = !isEven;
        }

        return (sum % 10) == 0;
    }

    /**
     * Generate a valid card number that passes Luhn check
     */
    private String generateValidCardNumber() {
        // Generate a random 15-digit number starting with 4 (Visa)
        String base = "4111" + UUID.randomUUID().toString().replaceAll("[^0-9]", "").substring(0, 11);

        // Calculate Luhn check digit
        int sum = 0;
        boolean isEven = false;

        for (int i = base.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(base.charAt(i));

            if (isEven) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }

            sum += digit;
            isEven = !isEven;
        }

        int checkDigit = (10 - (sum % 10)) % 10;
        return base + checkDigit;
    }

    /**
     * Detect card type based on IIN/BIN (Issuer Identification Number)
     */
    private String detectCardType(String cardNumber) {
        if (cardNumber == null || cardNumber.isEmpty()) {
            return "Unknown";
        }

        // Visa: starts with 4
        if (cardNumber.startsWith("4") && (cardNumber.length() == 13 || cardNumber.length() == 16 || cardNumber.length() == 19)) {
            return "Visa";
        }

        // MasterCard: starts with 51-55 or 2221-2720
        if (cardNumber.length() == 16) {
            try {
                int firstTwo = Integer.parseInt(cardNumber.substring(0, 2));
                int firstFour = Integer.parseInt(cardNumber.substring(0, 4));

                if (firstTwo >= 51 && firstTwo <= 55) {
                    return "MasterCard";
                }
                if (firstFour >= 2221 && firstFour <= 2720) {
                    return "MasterCard";
                }
            } catch (NumberFormatException e) {
                // Invalid format
            }
        }

        // American Express: starts with 34 or 37
        if ((cardNumber.startsWith("34") || cardNumber.startsWith("37")) && cardNumber.length() == 15) {
            return "American Express";
        }

        // Discover: starts with 6011, 622126-622925, 644-649, or 65
        if (cardNumber.length() == 16) {
            try {
                if (cardNumber.startsWith("6011") || cardNumber.startsWith("65")) {
                    return "Discover";
                }
                int firstSix = Integer.parseInt(cardNumber.substring(0, 6));
                if (firstSix >= 622126 && firstSix <= 622925) {
                    return "Discover";
                }
                int firstThree = Integer.parseInt(cardNumber.substring(0, 3));
                if (firstThree >= 644 && firstThree <= 649) {
                    return "Discover";
                }
            } catch (NumberFormatException e) {
                // Invalid format
            }
        }

        return "Unknown";
    }

    /**
     * Get card type for display purposes
     */
    public String getCardType(Long cardId) {
        HotelCard card = hotelCardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));
        return detectCardType(card.getCardNumber());
    }
}