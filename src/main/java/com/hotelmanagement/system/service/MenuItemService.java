package com.hotelmanagement.system.service;

import com.hotelmanagement.system.model.MenuItem;
import com.hotelmanagement.system.repository.MenuItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MenuItemService {

    @Autowired
    private MenuItemRepository menuItemRepository;

    /**
     * Find all available menu items
     */
    public List<MenuItem> findAvailableItems() {
        return menuItemRepository.findByIsAvailable(true);
    }

    /**
     * Find available items by category
     */
    public List<MenuItem> findAvailableItemsByCategory(String category) {
        if (category == null || category.isEmpty() || category.equalsIgnoreCase("all")) {
            return findAvailableItems();
        }
        return menuItemRepository.findByIsAvailableAndCategoryIgnoreCase(true, category);
    }

    /**
     * Find all items by category (including unavailable)
     */
    public List<MenuItem> findItemsByCategory(String category) {
        if (category == null || category.isEmpty()) {
            throw new IllegalArgumentException("Category cannot be empty");
        }

        return menuItemRepository.findAll().stream()
                .filter(item -> item.getCategory().equalsIgnoreCase(category))
                .collect(Collectors.toList());
    }

    /**
     * Find unavailable items
     */
    public List<MenuItem> findUnavailableItems() {
        return menuItemRepository.findByIsAvailable(false);
    }

    /**
     * Get all menu items
     */
    public List<MenuItem> getAllMenuItems() {
        return menuItemRepository.findAll();
    }

    /**
     * Get menu item by ID
     */
    public Optional<MenuItem> getMenuItemById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid menu item ID");
        }
        return menuItemRepository.findById(id);
    }

    /**
     * Get all unique categories
     */
    public List<String> getAllCategories() {
        return menuItemRepository.findAll().stream()
                .map(MenuItem::getCategory)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Create a new menu item with validation
     */
    @Transactional
    public MenuItem createMenuItem(MenuItem menuItem) {
        // Validation
        if (menuItem.getName() == null || menuItem.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Menu item name is required");
        }

        if (menuItem.getPrice() <= 0) {
            throw new IllegalArgumentException("Price must be greater than zero");
        }

        // Set default category if none provided
        if (menuItem.getCategory() == null || menuItem.getCategory().isEmpty()) {
            menuItem.setCategory("Main Courses");
        }

        // Trim and validate name
        menuItem.setName(menuItem.getName().trim());

        // Check for duplicate names
        Optional<MenuItem> existingItem = menuItemRepository.findAll().stream()
                .filter(item -> item.getName().equalsIgnoreCase(menuItem.getName()))
                .findFirst();

        if (existingItem.isPresent()) {
            throw new IllegalArgumentException("A menu item with this name already exists");
        }

        return menuItemRepository.save(menuItem);
    }

    /**
     * Update a menu item with validation
     */
    @Transactional
    public MenuItem updateMenuItem(Long id, MenuItem menuItemDetails) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid menu item ID");
        }

        // Check if item exists
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu item not found with id: " + id));

        // Validation
        if (menuItemDetails.getName() == null || menuItemDetails.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Menu item name is required");
        }

        if (menuItemDetails.getPrice() <= 0) {
            throw new IllegalArgumentException("Price must be greater than zero");
        }

        // Check for duplicate names (excluding current item)
        Optional<MenuItem> existingItem = menuItemRepository.findAll().stream()
                .filter(item -> !item.getId().equals(id))
                .filter(item -> item.getName().equalsIgnoreCase(menuItemDetails.getName().trim()))
                .findFirst();

        if (existingItem.isPresent()) {
            throw new IllegalArgumentException("A menu item with this name already exists");
        }

        // Set default category if none is provided
        String category = menuItemDetails.getCategory();
        if (category == null || category.isEmpty()) {
            category = "Main Courses";
        }

        // Update using repository method
        menuItemRepository.updateMenuItem(
                id,
                menuItemDetails.getName().trim(),
                menuItemDetails.getDescription(),
                menuItemDetails.getPrice(),
                menuItemDetails.isAvailable(),
                menuItemDetails.getImageUrl(),
                category
        );

        // Return the updated entity
        return menuItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Could not find menu item after update with id: " + id));
    }

    /**
     * Toggle availability of a menu item
     */
    @Transactional
    public MenuItem toggleAvailability(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid menu item ID");
        }

        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu item not found with id: " + id));

        menuItem.setAvailable(!menuItem.isAvailable());
        return menuItemRepository.save(menuItem);
    }

    /**
     * Delete a menu item
     */
    @Transactional
    public void deleteMenuItem(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid menu item ID");
        }

        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu item not found with id: " + id));

        menuItemRepository.delete(menuItem);
    }

    /**
     * Search menu items by keyword in name or description
     */
    public List<MenuItem> searchMenuItems(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("Search keyword is required");
        }

        String lowerKeyword = keyword.toLowerCase().trim();

        return menuItemRepository.findAll().stream()
                .filter(item ->
                        item.getName().toLowerCase().contains(lowerKeyword) ||
                                (item.getDescription() != null && item.getDescription().toLowerCase().contains(lowerKeyword))
                )
                .collect(Collectors.toList());
    }
}