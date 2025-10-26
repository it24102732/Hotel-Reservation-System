package com.hotelmanagement.system.controller;

import com.hotelmanagement.system.model.FoodOrder;
import com.hotelmanagement.system.service.FoodOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/food-orders")
@CrossOrigin(origins = "*")
@Validated
public class FoodOrderController {

    @Autowired
    private FoodOrderService foodOrderService;

    /**
     * Create a new food order with validation
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createFoodOrder(@Valid @RequestBody FoodOrder foodOrder) {
        try {
            // Additional validation
            if (foodOrder.getItems() == null || foodOrder.getItems().isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Order must contain at least one item");
                return ResponseEntity.badRequest().body(error);
            }

            if (foodOrder.getUser() == null || foodOrder.getUser().getId() == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "User information is required");
                return ResponseEntity.badRequest().body(error);
            }

            if (foodOrder.getRoom() == null || foodOrder.getRoom().getId() == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Room information is required");
                return ResponseEntity.badRequest().body(error);
            }

            FoodOrder newFoodOrder = foodOrderService.createFoodOrder(foodOrder);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Order created successfully");
            response.put("order", newFoodOrder);

            return new ResponseEntity<>(response, HttpStatus.CREATED);

        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to create order: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Get all food orders for a specific user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getFoodOrdersByUserId(
            @PathVariable @NotNull @Positive(message = "User ID must be positive") Long userId) {
        try {
            List<FoodOrder> foodOrders = foodOrderService.getFoodOrdersByUserId(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("count", foodOrders.size());
            response.put("orders", foodOrders);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to retrieve orders: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Get all food orders with optional filtering
     */
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllFoodOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long roomId) {
        try {
            List<FoodOrder> orders;

            if (status != null && !status.trim().isEmpty()) {
                orders = foodOrderService.getFoodOrdersByStatus(status.toUpperCase());
            } else if (roomId != null) {
                orders = foodOrderService.getFoodOrdersByRoomId(roomId);
            } else {
                orders = foodOrderService.getAllFoodOrders();
            }

            Map<String, Object> response = new HashMap<>();
            response.put("count", orders.size());
            response.put("orders", orders);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to retrieve orders: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Get a specific food order by ID
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<Object> getFoodOrderById(
            @PathVariable @NotNull @Positive Long orderId) {
        try {
            return foodOrderService.getFoodOrderById(orderId)
                    .<ResponseEntity<Object>>map(ResponseEntity::ok)
                    .orElseGet(() -> {
                        Map<String, String> error = new HashMap<>();
                        error.put("error", "Order not found with ID: " + orderId);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
                    });
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve order: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Update food order status with validation
     */
    @PutMapping("/{orderId}/status")
    public ResponseEntity<Map<String, Object>> updateFoodOrderStatus(
            @PathVariable @NotNull @Positive Long orderId,
            @RequestBody Map<String, String> payload) {
        try {
            String status = payload.get("status");

            // Validation
            if (status == null || status.trim().isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Status is required");
                return ResponseEntity.badRequest().body(error);
            }

            // Validate status values
            List<String> validStatuses = List.of("PENDING", "PREPARING", "DELIVERED", "CANCELLED");
            if (!validStatuses.contains(status.toUpperCase())) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Invalid status. Must be one of: " + String.join(", ", validStatuses));
                return ResponseEntity.badRequest().body(error);
            }

            FoodOrder updatedOrder = foodOrderService.updateFoodOrderStatus(orderId, status.toUpperCase());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Order status updated successfully");
            response.put("order", updatedOrder);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Order not found with ID: " + orderId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to update order status: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Cancel a food order
     */
    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<Map<String, Object>> cancelFoodOrder(@PathVariable @NotNull @Positive Long orderId) {
        try {
            FoodOrder cancelledOrder = foodOrderService.cancelFoodOrder(orderId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Order cancelled successfully");
            response.put("order", cancelledOrder);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    /**
     * Get order statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getOrderStatistics() {
        try {
            Map<String, Object> stats = foodOrderService.getOrderStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to retrieve statistics: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Delete a food order (Admin only)
     */
    @DeleteMapping("/{orderId}")
    public ResponseEntity<Map<String, String>> deleteFoodOrder(@PathVariable @NotNull @Positive Long orderId) {
        try {
            foodOrderService.deleteFoodOrder(orderId);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Order deleted successfully");

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Order not found with ID: " + orderId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
}