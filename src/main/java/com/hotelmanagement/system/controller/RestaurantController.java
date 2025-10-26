package com.hotelmanagement.system.controller;

import com.hotelmanagement.system.model.FoodOrder;
import com.hotelmanagement.system.model.MenuItem;
import com.hotelmanagement.system.service.FileUploadService;
import com.hotelmanagement.system.service.FoodOrderService;
import com.hotelmanagement.system.service.MenuItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/restaurant")
public class RestaurantController {

    @Autowired
    private FoodOrderService foodOrderService;

    @Autowired
    private MenuItemService menuItemService;

    @Autowired
    private FileUploadService fileUploadService;

    /**
     * Display restaurant management dashboard
     */
    @GetMapping
    public String restaurantDashboard(
            @RequestParam(required = false) String orderStatus,
            @RequestParam(required = false) String menuSearch,
            @RequestParam(required = false) String category,
            Model model) {

        try {
            // Get orders based on filter
            List<FoodOrder> orders;
            if (orderStatus != null && !orderStatus.isEmpty() && !orderStatus.equalsIgnoreCase("ALL")) {
                orders = foodOrderService.getFoodOrdersByStatus(orderStatus);
            } else {
                orders = foodOrderService.getAllFoodOrders();
            }

            // Get menu items based on search/filter
            List<MenuItem> menuItems;
            if (menuSearch != null && !menuSearch.isEmpty()) {
                menuItems = menuItemService.searchMenuItems(menuSearch);
            } else if (category != null && !category.isEmpty()) {
                menuItems = menuItemService.findItemsByCategory(category);
            } else {
                menuItems = menuItemService.getAllMenuItems();
            }

            // Get statistics
            Map<String, Object> statistics = foodOrderService.getOrderStatistics();

            // Get categories for dropdown
            List<String> categories = menuItemService.getAllCategories();

            // Add to model
            model.addAttribute("orders", orders);
            model.addAttribute("menuItems", menuItems);
            model.addAttribute("statistics", statistics);
            model.addAttribute("categories", categories);
            model.addAttribute("currentOrderFilter", orderStatus != null ? orderStatus : "ALL");
            model.addAttribute("currentMenuSearch", menuSearch);
            model.addAttribute("currentCategory", category);
            model.addAttribute("newMenuItem", new MenuItem());

            return "restaurant";

        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error loading dashboard: " + e.getMessage());
            return "restaurant";
        }
    }

    /**
     * Create new menu item
     */
    @PostMapping("/menu-items/create")
    public String createMenuItem(
            @Valid @ModelAttribute("menuItem") MenuItem menuItem,
            BindingResult result,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Validation failed: " + result.getAllErrors());
            return "redirect:/restaurant";
        }

        try {
            // Upload image if provided
            if (imageFile != null && !imageFile.isEmpty()) {
                String imageUrl = fileUploadService.uploadFile(imageFile);
                menuItem.setImageUrl(imageUrl);
            }

            menuItemService.createMenuItem(menuItem);
            redirectAttributes.addFlashAttribute("successMessage", "Menu item created successfully!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to create menu item: " + e.getMessage());
        }

        return "redirect:/restaurant";
    }

    /**
     * Update existing menu item
     */
    @PostMapping("/menu-items/update/{id}")
    public String updateMenuItem(
            @PathVariable Long id,
            @Valid @ModelAttribute("menuItem") MenuItem menuItem,
            BindingResult result,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            @RequestParam(value = "removeImage", required = false) Boolean removeImage,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Validation failed");
            return "redirect:/restaurant";
        }

        try {
            // Handle image upload
            if (imageFile != null && !imageFile.isEmpty()) {
                String imageUrl = fileUploadService.uploadFile(imageFile);
                menuItem.setImageUrl(imageUrl);
            } else if (removeImage != null && removeImage) {
                menuItem.setImageUrl(null);
            }

            menuItemService.updateMenuItem(id, menuItem);
            redirectAttributes.addFlashAttribute("successMessage", "Menu item updated successfully!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update menu item: " + e.getMessage());
        }

        return "redirect:/restaurant";
    }

    /**
     * Delete menu item
     */
    @PostMapping("/menu-items/delete/{id}")
    public String deleteMenuItem(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            menuItemService.deleteMenuItem(id);
            redirectAttributes.addFlashAttribute("successMessage", "Menu item deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete menu item: " + e.getMessage());
        }

        return "redirect:/restaurant";
    }

    /**
     * Toggle menu item availability
     */
    @PostMapping("/menu-items/toggle-availability/{id}")
    public String toggleAvailability(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            menuItemService.toggleAvailability(id);
            redirectAttributes.addFlashAttribute("successMessage", "Menu item availability updated!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update availability: " + e.getMessage());
        }

        return "redirect:/restaurant";
    }

    /**
     * Update order status
     */
    @PostMapping("/orders/update-status/{orderId}")
    public String updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam String status,
            RedirectAttributes redirectAttributes) {

        try {
            foodOrderService.updateFoodOrderStatus(orderId, status);
            redirectAttributes.addFlashAttribute("successMessage", "Order #" + orderId + " status updated to " + status);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update order status: " + e.getMessage());
        }

        return "redirect:/restaurant";
    }

    /**
     * Cancel order
     */
    @PostMapping("/orders/cancel/{orderId}")
    public String cancelOrder(@PathVariable Long orderId, RedirectAttributes redirectAttributes) {
        try {
            foodOrderService.cancelFoodOrder(orderId);
            redirectAttributes.addFlashAttribute("successMessage", "Order #" + orderId + " cancelled successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to cancel order: " + e.getMessage());
        }

        return "redirect:/restaurant";
    }

    /**
     * View order details
     */
    @GetMapping("/orders/view/{orderId}")
    public String viewOrderDetails(@PathVariable Long orderId, Model model, RedirectAttributes redirectAttributes) {
        try {
            FoodOrder order = foodOrderService.getFoodOrderById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found"));

            model.addAttribute("order", order);
            return "restaurant-order-details";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to load order details: " + e.getMessage());
            return "redirect:/restaurant";
        }
    }

    /**
     * Edit menu item form
     */
    @GetMapping("/menu-items/edit/{id}")
    public String editMenuItemForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            MenuItem menuItem = menuItemService.getMenuItemById(id)
                    .orElseThrow(() -> new RuntimeException("Menu item not found"));

            List<String> categories = menuItemService.getAllCategories();

            model.addAttribute("menuItem", menuItem);
            model.addAttribute("categories", categories);
            model.addAttribute("isEdit", true);

            return "restaurant-menu-form";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to load menu item: " + e.getMessage());
            return "redirect:/restaurant";
        }
    }

    /**
     * New menu item form
     */
    @GetMapping("/menu-items/new")
    public String newMenuItemForm(Model model) {
        List<String> categories = menuItemService.getAllCategories();

        model.addAttribute("menuItem", new MenuItem());
        model.addAttribute("categories", categories);
        model.addAttribute("isEdit", false);

        return "restaurant-menu-form";
    }
}