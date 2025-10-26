package com.hotelmanagement.system.controller;

import com.hotelmanagement.system.model.MenuItem;
import com.hotelmanagement.system.service.MenuItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/menu-items")
@CrossOrigin(origins = "*")
@Validated
public class MenuItemController {

    @Autowired
    private MenuItemService menuItemService;

    /**
     * Get available menu items with optional category filter
     */
    @GetMapping("/available")
    public ResponseEntity<Map<String, Object>> getAvailableMenuItems(
            @RequestParam(required = false) String category) {
        try {
            List<MenuItem> items;

            if (category != null && !category.isEmpty() && !category.equalsIgnoreCase("all")) {
                items = menuItemService.findAvailableItemsByCategory(category);
            } else {
                items = menuItemService.findAvailableItems();
            }

            Map<String, Object> response = new HashMap<>();
            response.put("count", items.size());
            response.put("items", items);
            response.put("category", category != null ? category : "all");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to retrieve menu items: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Get all menu categories
     */
    @GetMapping("/categories")
    public ResponseEntity<Map<String, Object>> getMenuCategories() {
        try {
            List<String> categories = menuItemService.getAllCategories();

            Map<String, Object> response = new HashMap<>();
            response.put("count", categories.size());
            response.put("categories", categories);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to retrieve categories: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Get a specific menu item by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Object> getMenuItemById(
            @PathVariable @NotNull @Positive(message = "ID must be positive") Long id) {
        try {
            return menuItemService.getMenuItemById(id)
                    .<ResponseEntity<Object>>map(ResponseEntity::ok)
                    .orElseGet(() -> {
                        Map<String, String> error = new HashMap<>();
                        error.put("error", "Menu item not found with ID: " + id);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
                    });
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve menu item: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Get all menu items (including unavailable ones)
     */
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllMenuItems(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean available) {
        try {
            List<MenuItem> menuItems;

            if (category != null && !category.isEmpty()) {
                menuItems = menuItemService.findItemsByCategory(category);
            } else if (available != null) {
                menuItems = available ? menuItemService.findAvailableItems()
                        : menuItemService.findUnavailableItems();
            } else {
                menuItems = menuItemService.getAllMenuItems();
            }

            Map<String, Object> response = new HashMap<>();
            response.put("count", menuItems.size());
            response.put("items", menuItems);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to retrieve menu items: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Create a new menu item with validation
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createMenuItem(@Valid @RequestBody MenuItem menuItem) {
        try {
            // Additional validation
            if (menuItem.getName() == null || menuItem.getName().trim().isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Menu item name is required");
                return ResponseEntity.badRequest().body(error);
            }

            if (menuItem.getPrice() <= 0) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Price must be greater than zero");
                return ResponseEntity.badRequest().body(error);
            }

            MenuItem newMenuItem = menuItemService.createMenuItem(menuItem);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Menu item created successfully");
            response.put("item", newMenuItem);

            return new ResponseEntity<>(response, HttpStatus.CREATED);

        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to create menu item: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Update an existing menu item with validation
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateMenuItem(
            @PathVariable @NotNull @Positive Long id,
            @Valid @RequestBody MenuItem menuItemDetails) {
        try {
            // Validation
            if (menuItemDetails.getName() == null || menuItemDetails.getName().trim().isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Menu item name is required");
                return ResponseEntity.badRequest().body(error);
            }

            if (menuItemDetails.getPrice() <= 0) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Price must be greater than zero");
                return ResponseEntity.badRequest().body(error);
            }

            MenuItem updatedMenuItem = menuItemService.updateMenuItem(id, menuItemDetails);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Menu item updated successfully");
            response.put("item", updatedMenuItem);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Menu item not found with ID: " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to update menu item: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Toggle menu item availability
     */
    @PatchMapping("/{id}/availability")
    public ResponseEntity<Map<String, Object>> toggleAvailability(@PathVariable @NotNull @Positive Long id) {
        try {
            MenuItem updatedItem = menuItemService.toggleAvailability(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Menu item availability updated");
            response.put("item", updatedItem);
            response.put("available", updatedItem.isAvailable());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Menu item not found with ID: " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    /**
     * Delete a menu item
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteMenuItem(@PathVariable @NotNull @Positive Long id) {
        try {
            menuItemService.deleteMenuItem(id);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Menu item deleted successfully");

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Menu item not found with ID: " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    /**
     * Search menu items by name or description
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchMenuItems(@RequestParam @NotBlank String keyword) {
        try {
            List<MenuItem> items = menuItemService.searchMenuItems(keyword);

            Map<String, Object> response = new HashMap<>();
            response.put("count", items.size());
            response.put("items", items);
            response.put("keyword", keyword);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Search failed: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
}