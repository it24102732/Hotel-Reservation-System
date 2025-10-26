package com.hotelmanagement.system.service;

import com.hotelmanagement.system.model.FoodOrder;
import com.hotelmanagement.system.model.MenuItem;
import com.hotelmanagement.system.model.Room;
import com.hotelmanagement.system.model.User;
import com.hotelmanagement.system.repository.FoodOrderRepository;
import com.hotelmanagement.system.repository.MenuItemRepository;
import com.hotelmanagement.system.repository.RoomRepository;
import com.hotelmanagement.system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FoodOrderService {

    @Autowired
    private FoodOrderRepository foodOrderRepository;

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Get all available menu items
     */
    public List<MenuItem> getAvailableMenuItems() {
        return menuItemRepository.findByIsAvailable(true);
    }

    /**
     * Create a new food order with comprehensive validation
     */
    @Transactional
    public FoodOrder createFoodOrder(FoodOrder foodOrder) {
        // Validate user
        if (foodOrder.getUser() == null || foodOrder.getUser().getId() == null) {
            throw new IllegalArgumentException("User information is required");
        }

        User user = userRepository.findById(foodOrder.getUser().getId())
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + foodOrder.getUser().getId()));
        foodOrder.setUser(user);

        // Validate room
        if (foodOrder.getRoom() == null || foodOrder.getRoom().getId() == null) {
            throw new IllegalArgumentException("Room information is required");
        }

        Room room = roomRepository.findById(foodOrder.getRoom().getId())
                .orElseThrow(() -> new RuntimeException("Room not found with ID: " + foodOrder.getRoom().getId()));
        foodOrder.setRoom(room);

        // Validate items
        if (foodOrder.getItems() == null || foodOrder.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }

        // Calculate total price and validate items
        double totalPrice = 0;
        for (MenuItem item : foodOrder.getItems()) {
            if (item.getId() == null) {
                throw new IllegalArgumentException("Menu item ID is required");
            }

            MenuItem menuItem = menuItemRepository.findById(item.getId())
                    .orElseThrow(() -> new RuntimeException("Menu item not found with ID: " + item.getId()));

            if (!menuItem.isAvailable()) {
                throw new RuntimeException("Menu item '" + menuItem.getName() + "' is not available");
            }

            totalPrice += menuItem.getPrice();
        }

        // Validate total price
        if (totalPrice <= 0) {
            throw new IllegalArgumentException("Order total must be greater than zero");
        }

        foodOrder.setTotalPrice(totalPrice);
        foodOrder.setStatus("PENDING");
        foodOrder.setOrderedAt(LocalDateTime.now());

        return foodOrderRepository.save(foodOrder);
    }

    /**
     * Get food orders by user ID
     */
    public List<FoodOrder> getFoodOrdersByUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }
        return foodOrderRepository.findByUserId(userId);
    }

    /**
     * Get all food orders
     */
    public List<FoodOrder> getAllFoodOrders() {
        return foodOrderRepository.findAll();
    }

    /**
     * Get food order by ID
     */
    public Optional<FoodOrder> getFoodOrderById(Long orderId) {
        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("Invalid order ID");
        }
        return foodOrderRepository.findById(orderId);
    }

    /**
     * Get food orders by status
     */
    public List<FoodOrder> getFoodOrdersByStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status is required");
        }

        return foodOrderRepository.findAll().stream()
                .filter(order -> order.getStatus().equalsIgnoreCase(status))
                .collect(Collectors.toList());
    }

    /**
     * Get food orders by room ID
     */
    public List<FoodOrder> getFoodOrdersByRoomId(Long roomId) {
        if (roomId == null || roomId <= 0) {
            throw new IllegalArgumentException("Invalid room ID");
        }

        return foodOrderRepository.findAll().stream()
                .filter(order -> order.getRoom().getId().equals(roomId))
                .collect(Collectors.toList());
    }

    /**
     * Update food order status with validation
     */
    @Transactional
    public FoodOrder updateFoodOrderStatus(Long orderId, String status) {
        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("Invalid order ID");
        }

        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status is required");
        }

        // Validate status
        List<String> validStatuses = List.of("PENDING", "PREPARING", "DELIVERED", "CANCELLED");
        if (!validStatuses.contains(status.toUpperCase())) {
            throw new IllegalArgumentException("Invalid status. Must be one of: " + String.join(", ", validStatuses));
        }

        FoodOrder order = foodOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Food order not found with id: " + orderId));

        // Business logic: prevent status change if already delivered or cancelled
        if (order.getStatus().equals("DELIVERED") || order.getStatus().equals("CANCELLED")) {
            throw new IllegalArgumentException("Cannot change status of " + order.getStatus().toLowerCase() + " orders");
        }

        order.setStatus(status.toUpperCase());
        return foodOrderRepository.save(order);
    }

    /**
     * Cancel a food order
     */
    @Transactional
    public FoodOrder cancelFoodOrder(Long orderId) {
        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("Invalid order ID");
        }

        FoodOrder order = foodOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Food order not found with id: " + orderId));

        if (order.getStatus().equals("DELIVERED")) {
            throw new RuntimeException("Cannot cancel a delivered order");
        }

        if (order.getStatus().equals("CANCELLED")) {
            throw new RuntimeException("Order is already cancelled");
        }

        order.setStatus("CANCELLED");
        return foodOrderRepository.save(order);
    }

    /**
     * Delete a food order
     */
    @Transactional
    public void deleteFoodOrder(Long orderId) {
        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("Invalid order ID");
        }

        FoodOrder order = foodOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Food order not found with id: " + orderId));

        foodOrderRepository.delete(order);
    }

    /**
     * Get order statistics
     */
    public Map<String, Object> getOrderStatistics() {
        List<FoodOrder> allOrders = foodOrderRepository.findAll();
        LocalDate today = LocalDate.now();

        Map<String, Object> stats = new HashMap<>();

        // Total orders
        stats.put("totalOrders", allOrders.size());

        // Orders by status
        long pendingCount = allOrders.stream().filter(o -> o.getStatus().equals("PENDING")).count();
        long preparingCount = allOrders.stream().filter(o -> o.getStatus().equals("PREPARING")).count();
        long deliveredCount = allOrders.stream().filter(o -> o.getStatus().equals("DELIVERED")).count();
        long cancelledCount = allOrders.stream().filter(o -> o.getStatus().equals("CANCELLED")).count();

        stats.put("pendingOrders", pendingCount);
        stats.put("preparingOrders", preparingCount);
        stats.put("deliveredOrders", deliveredCount);
        stats.put("cancelledOrders", cancelledCount);

        // Today's orders
        long todayOrders = allOrders.stream()
                .filter(o -> o.getOrderedAt().toLocalDate().equals(today))
                .count();
        stats.put("todayOrders", todayOrders);

        // Today's revenue
        double todayRevenue = allOrders.stream()
                .filter(o -> o.getOrderedAt().toLocalDate().equals(today))
                .filter(o -> o.getStatus().equals("DELIVERED"))
                .mapToDouble(FoodOrder::getTotalPrice)
                .sum();
        stats.put("todayRevenue", todayRevenue);

        // Total revenue
        double totalRevenue = allOrders.stream()
                .filter(o -> o.getStatus().equals("DELIVERED"))
                .mapToDouble(FoodOrder::getTotalPrice)
                .sum();
        stats.put("totalRevenue", totalRevenue);

        return stats;
    }
}