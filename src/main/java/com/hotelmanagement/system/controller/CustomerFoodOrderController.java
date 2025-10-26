package com.hotelmanagement.system.controller;

import com.hotelmanagement.system.model.*;
import com.hotelmanagement.system.repository.PaymentRepository;
import com.hotelmanagement.system.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping("/customer/food-order")
public class CustomerFoodOrderController {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private FoodOrderService foodOrderService;

    @Autowired
    private MenuItemService menuItemService;

    @Autowired
    private WalletService walletService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserService userService;

    @Autowired
    private PaymentService paymentService;

    /**
     * Display food menu with cart
     */
    @GetMapping("/menu")
    public String showFoodMenu(
            @RequestParam(required = false) String category,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            redirectAttributes.addFlashAttribute("error", "Please login to order food.");
            return "redirect:/login";
        }

        try {
            User user = userService.getUserById(userId);
            model.addAttribute("user", user);

            // Get menu items by category
            List<MenuItem> menuItems;
            if (category != null && !category.isEmpty() && !category.equalsIgnoreCase("all")) {
                menuItems = menuItemService.findAvailableItemsByCategory(category);
            } else {
                menuItems = menuItemService.findAvailableItems();
            }
            model.addAttribute("menuItems", menuItems);
            model.addAttribute("selectedCategory", category != null ? category : "all");

            // Get all categories
            List<String> categories = menuItemService.getAllCategories();
            model.addAttribute("categories", categories);

            // Get cart from session
            Map<Long, CartItem> cart = getCartFromSession(session);
            model.addAttribute("cart", cart);
            model.addAttribute("cartTotal", calculateCartTotal(cart));
            model.addAttribute("cartItemCount", cart.size());

            // CRITICAL: Get user's active bookings with ASSIGNED ROOMS ONLY
            List<Booking> activeBookings = bookingService.getBookingsByUserId(userId).stream()
                    .filter(b -> ("CONFIRMED".equals(b.getStatus()) || "CHECKED_IN".equals(b.getStatus())))
                    .filter(b -> b.getRoom() != null) // MUST have assigned room
                    .toList();
            model.addAttribute("activeBookings", activeBookings);

            // Add warning if no eligible bookings
            if (activeBookings.isEmpty()) {
                model.addAttribute("noActiveBookings", true);
            }

            return "food-menu";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error loading menu: " + e.getMessage());
            return "redirect:/customer/dashboard";
        }
    }

    /**
     * Add item to cart
     */
    @PostMapping("/cart/add")
    public String addToCart(
            @RequestParam Long itemId,
            @RequestParam(defaultValue = "1") int quantity,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            redirectAttributes.addFlashAttribute("error", "Please login to add items to cart.");
            return "redirect:/login";
        }

        try {
            MenuItem menuItem = menuItemService.getMenuItemById(itemId)
                    .orElseThrow(() -> new RuntimeException("Menu item not found"));

            if (!menuItem.isAvailable()) {
                redirectAttributes.addFlashAttribute("error", "This item is currently unavailable.");
                return "redirect:/customer/food-order/menu";
            }

            Map<Long, CartItem> cart = getCartFromSession(session);

            if (cart.containsKey(itemId)) {
                CartItem existingItem = cart.get(itemId);
                existingItem.setQuantity(existingItem.getQuantity() + quantity);
            } else {
                CartItem cartItem = new CartItem();
                cartItem.setMenuItem(menuItem);
                cartItem.setQuantity(quantity);
                cart.put(itemId, cartItem);
            }

            session.setAttribute("foodCart", cart);
            redirectAttributes.addFlashAttribute("success",
                    menuItem.getName() + " added to cart!");

            return "redirect:/customer/food-order/menu";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error adding item: " + e.getMessage());
            return "redirect:/customer/food-order/menu";
        }
    }

    /**
     * Update cart item quantity
     */
    @PostMapping("/cart/update")
    public String updateCart(
            @RequestParam Long itemId,
            @RequestParam int quantity,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            Map<Long, CartItem> cart = getCartFromSession(session);

            if (quantity <= 0) {
                cart.remove(itemId);
                redirectAttributes.addFlashAttribute("success", "Item removed from cart.");
            } else if (cart.containsKey(itemId)) {
                cart.get(itemId).setQuantity(quantity);
                redirectAttributes.addFlashAttribute("success", "Cart updated.");
            }

            session.setAttribute("foodCart", cart);
            return "redirect:/customer/food-order/menu";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating cart: " + e.getMessage());
            return "redirect:/customer/food-order/menu";
        }
    }

    /**
     * Remove item from cart
     */
    @PostMapping("/cart/remove")
    public String removeFromCart(
            @RequestParam Long itemId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            Map<Long, CartItem> cart = getCartFromSession(session);
            cart.remove(itemId);
            session.setAttribute("foodCart", cart);

            redirectAttributes.addFlashAttribute("success", "Item removed from cart.");
            return "redirect:/customer/food-order/menu";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error removing item: " + e.getMessage());
            return "redirect:/customer/food-order/menu";
        }
    }

    /**
     * Clear entire cart
     */
    @PostMapping("/cart/clear")
    public String clearCart(HttpSession session, RedirectAttributes redirectAttributes) {
        session.removeAttribute("foodCart");
        redirectAttributes.addFlashAttribute("success", "Cart cleared.");
        return "redirect:/customer/food-order/menu";
    }

    /**
     * Show checkout page - Similar to booking payment page
     */
    @GetMapping("/checkout")
    public String showCheckout(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            redirectAttributes.addFlashAttribute("error", "Please login to checkout.");
            return "redirect:/login";
        }

        try {
            Map<Long, CartItem> cart = getCartFromSession(session);
            if (cart.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Your cart is empty.");
                return "redirect:/customer/food-order/menu";
            }

            User user = userService.getUserById(userId);
            model.addAttribute("user", user);

            // CRITICAL: Get user's active bookings with ASSIGNED ROOMS
            List<Booking> activeBookings = bookingService.getBookingsByUserId(userId).stream()
                    .filter(b -> ("CONFIRMED".equals(b.getStatus()) || "CHECKED_IN".equals(b.getStatus())))
                    .filter(b -> b.getRoom() != null) // MUST have assigned room
                    .toList();

            if (activeBookings.isEmpty()) {
                redirectAttributes.addFlashAttribute("error",
                        "You must have an active booking with an assigned room to order food. " +
                                "Please wait for staff to assign a room to your booking, or book a new room.");
                return "redirect:/customer/dashboard";
            }

            model.addAttribute("activeBookings", activeBookings);
            model.addAttribute("cart", cart);
            model.addAttribute("cartTotal", calculateCartTotal(cart));

            // Get wallet cards for payment
            List<HotelCard> walletCards = walletService.getWalletCards(userId);
            model.addAttribute("walletCards", walletCards);

            return "food-checkout";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error loading checkout: " + e.getMessage());
            return "redirect:/customer/food-order/menu";
        }
    }

    /**
     * Place order - NOW WITH PAYMENT PROCESSING AND DATABASE SAVE
     */
    @PostMapping("/place-order")
    public String placeOrder(
            @RequestParam Long bookingId,
            @RequestParam Long cardId,
            @RequestParam(required = false) String specialInstructions,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            redirectAttributes.addFlashAttribute("error", "Please login to place order.");
            return "redirect:/login";
        }

        try {
            Map<Long, CartItem> cart = getCartFromSession(session);
            if (cart.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Your cart is empty.");
                return "redirect:/customer/food-order/menu";
            }

            // Get and validate booking
            Booking booking = bookingService.getBookingById(bookingId);

            // SECURITY: Validate booking belongs to user
            if (!booking.getUser().getId().equals(userId)) {
                throw new SecurityException("This booking doesn't belong to you.");
            }

            // CRITICAL: Validate booking status
            if (!"CONFIRMED".equals(booking.getStatus()) && !"CHECKED_IN".equals(booking.getStatus())) {
                redirectAttributes.addFlashAttribute("error",
                        "Food orders can only be placed for confirmed or checked-in bookings. " +
                                "Current status: " + booking.getStatus());
                return "redirect:/customer/food-order/menu";
            }

            // CRITICAL: Validate room is assigned
            if (booking.getRoom() == null) {
                redirectAttributes.addFlashAttribute("error",
                        "Room not yet assigned to your booking. Please wait for staff to assign a room.");
                return "redirect:/customer/food-order/menu";
            }

            // Calculate order details
            List<MenuItem> orderItems = new ArrayList<>();
            Map<Long, Integer> quantities = new HashMap<>();
            double totalPrice = 0.0;

            for (Map.Entry<Long, CartItem> entry : cart.entrySet()) {
                CartItem cartItem = entry.getValue();
                MenuItem menuItem = cartItem.getMenuItem();
                int quantity = cartItem.getQuantity();

                // Add items to list (for order record)
                for (int i = 0; i < quantity; i++) {
                    orderItems.add(menuItem);
                }

                quantities.put(menuItem.getId(), quantity);
                totalPrice += menuItem.getPrice() * quantity;
            }

            // STEP 1: Create food order with PENDING status (like booking system)
            FoodOrder foodOrder = new FoodOrder();
            foodOrder.setUser(booking.getUser());
            foodOrder.setRoom(booking.getRoom());
            foodOrder.setItems(orderItems);
            foodOrder.setItemQuantities(quantities);
            foodOrder.setTotalPrice(totalPrice);
            foodOrder.setStatus("PENDING"); // PENDING until payment is confirmed
            foodOrder.setOrderedAt(LocalDateTime.now());

            FoodOrder savedOrder = foodOrderService.createFoodOrder(foodOrder);

            // STEP 2: Process payment (like booking system)
            try {
                HotelCard selectedCard = walletService.getUserCard(userId, cardId);

                // Validate sufficient balance
                if (selectedCard.getBalance() < totalPrice) {
                    // Delete the pending order if payment fails
                    foodOrderService.deleteFoodOrder(savedOrder.getId());

                    throw new RuntimeException(String.format(
                            "Insufficient balance. Required: $%.2f, Available: $%.2f. " +
                                    "Please add funds to your card or use another card.",
                            totalPrice,
                            selectedCard.getBalance()));
                }

                // ✅ CREATE PAYMENT RECORD
                Payment payment = new Payment();
                payment.setFoodOrder(savedOrder);
                payment.setAmount(totalPrice);
                payment.setPaymentMethod("CARD");
                payment.setStatus("SUCCESSFUL");
                payment.setTransactionDate(LocalDateTime.now());
                payment.setPaymentIdentifier(selectedCard.getCardNumber());

                // ✅ CRITICAL FIX: SAVE PAYMENT TO DATABASE
                Payment savedPayment = paymentRepository.save(payment);
                System.out.println(String.format(
                        "✅ Food order payment saved successfully! Payment ID: #%d, Order ID: #%d, Amount: $%.2f",
                        savedPayment.getId(),
                        savedOrder.getId(),
                        totalPrice
                ));

                // Deduct balance
                selectedCard.setBalance(selectedCard.getBalance() - totalPrice);
                walletService.updateCardBalance(selectedCard.getId(), selectedCard.getBalance());

                // STEP 3: Update order status to PREPARING (like booking confirmation)
                savedOrder.setStatus("PREPARING");
                foodOrderService.updateFoodOrderStatus(savedOrder.getId(), "PREPARING");

                // Clear cart
                session.removeAttribute("foodCart");

                redirectAttributes.addFlashAttribute("success",
                        String.format("Order placed successfully! Order ID: #%d. " +
                                        "Payment of $%.2f processed (Payment ID: #%d). Your food is being prepared.",
                                savedOrder.getId(), totalPrice, savedPayment.getId()));

                return "redirect:/customer/food-order/order-details/" + savedOrder.getId();

            } catch (RuntimeException e) {
                // If payment fails, delete the pending order
                try {
                    foodOrderService.deleteFoodOrder(savedOrder.getId());
                } catch (Exception deleteEx) {
                    System.err.println("Failed to delete pending order after payment failure: " + deleteEx.getMessage());
                }
                throw e; // Re-throw to show error to user
            }

        } catch (SecurityException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/customer/dashboard";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error placing order: " + e.getMessage());
            return "redirect:/customer/food-order/checkout";
        }
    }

    /**
     * View order details
     */
    @GetMapping("/order-details/{orderId}")
    public String viewOrderDetails(
            @PathVariable Long orderId,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            redirectAttributes.addFlashAttribute("error", "Please login to view order.");
            return "redirect:/login";
        }

        try {
            FoodOrder order = foodOrderService.getFoodOrderById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found"));

            // SECURITY: Validate order belongs to user
            if (!order.getUser().getId().equals(userId)) {
                throw new SecurityException("You don't have permission to view this order.");
            }

            model.addAttribute("order", order);

            // Create display items with quantities
            List<OrderItemDisplay> orderItemsDisplay = new ArrayList<>();
            Map<Long, Integer> quantities = order.getItemQuantities();

            if (quantities != null && !quantities.isEmpty()) {
                Set<Long> processedIds = new HashSet<>();
                for (MenuItem item : order.getItems()) {
                    if (!processedIds.contains(item.getId())) {
                        OrderItemDisplay display = new OrderItemDisplay();
                        display.setMenuItem(item);
                        display.setQuantity(quantities.getOrDefault(item.getId(), 1));
                        display.setSubtotal(item.getPrice() * display.getQuantity());
                        orderItemsDisplay.add(display);
                        processedIds.add(item.getId());
                    }
                }
            } else {
                // Fallback for old orders
                Map<Long, Integer> itemCounts = new HashMap<>();
                for (MenuItem item : order.getItems()) {
                    itemCounts.put(item.getId(), itemCounts.getOrDefault(item.getId(), 0) + 1);
                }

                Set<Long> processedIds = new HashSet<>();
                for (MenuItem item : order.getItems()) {
                    if (!processedIds.contains(item.getId())) {
                        OrderItemDisplay display = new OrderItemDisplay();
                        display.setMenuItem(item);
                        display.setQuantity(itemCounts.get(item.getId()));
                        display.setSubtotal(item.getPrice() * display.getQuantity());
                        orderItemsDisplay.add(display);
                        processedIds.add(item.getId());
                    }
                }
            }

            model.addAttribute("orderItems", orderItemsDisplay);

            return "food-order-details";
        } catch (SecurityException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/customer/dashboard";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error loading order: " + e.getMessage());
            return "redirect:/customer/dashboard";
        }
    }

    /**
     * View order history
     */
    @GetMapping("/history")
    public String viewOrderHistory(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            redirectAttributes.addFlashAttribute("error", "Please login to view history.");
            return "redirect:/login";
        }

        try {
            List<FoodOrder> orders = foodOrderService.getFoodOrdersByUserId(userId);
            model.addAttribute("orders", orders);

            // Calculate statistics
            long totalOrders = orders.size();
            long deliveredOrders = orders.stream()
                    .filter(order -> "DELIVERED".equals(order.getStatus()))
                    .count();
            double totalSpent = orders.stream()
                    .filter(order -> !"CANCELLED".equals(order.getStatus())) // Only count non-cancelled orders
                    .mapToDouble(FoodOrder::getTotalPrice)
                    .sum();

            model.addAttribute("totalOrders", totalOrders);
            model.addAttribute("deliveredOrders", deliveredOrders);
            model.addAttribute("totalSpent", totalSpent);

            return "food-order-history";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error loading history: " + e.getMessage());
            return "redirect:/customer/dashboard";
        }
    }

    // Helper methods
    @SuppressWarnings("unchecked")
    private Map<Long, CartItem> getCartFromSession(HttpSession session) {
        Map<Long, CartItem> cart = (Map<Long, CartItem>) session.getAttribute("foodCart");
        if (cart == null) {
            cart = new HashMap<>();
            session.setAttribute("foodCart", cart);
        }
        return cart;
    }

    private double calculateCartTotal(Map<Long, CartItem> cart) {
        return cart.values().stream()
                .mapToDouble(item -> item.getMenuItem().getPrice() * item.getQuantity())
                .sum();
    }

    // Inner class for cart items
    public static class CartItem {
        private MenuItem menuItem;
        private int quantity;

        public MenuItem getMenuItem() {
            return menuItem;
        }

        public void setMenuItem(MenuItem menuItem) {
            this.menuItem = menuItem;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public double getSubtotal() {
            return menuItem.getPrice() * quantity;
        }
    }

    // Inner class for order item display
    public static class OrderItemDisplay {
        private MenuItem menuItem;
        private int quantity;
        private double subtotal;

        public MenuItem getMenuItem() {
            return menuItem;
        }

        public void setMenuItem(MenuItem menuItem) {
            this.menuItem = menuItem;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public double getSubtotal() {
            return subtotal;
        }

        public void setSubtotal(double subtotal) {
            this.subtotal = subtotal;
        }
    }
}