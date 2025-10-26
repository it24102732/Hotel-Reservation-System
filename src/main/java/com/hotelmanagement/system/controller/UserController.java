package com.hotelmanagement.system.controller;

import com.hotelmanagement.system.model.LoginRequest;
import com.hotelmanagement.system.model.User;
import com.hotelmanagement.system.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * Register new user via API - DYNAMIC (works with database)
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user, HttpSession session) {
        try {
            // Validate input
            if (user.getName() == null || user.getName().trim().isEmpty()) {
                return new ResponseEntity<>("Name is required", HttpStatus.BAD_REQUEST);
            }

            if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
                return new ResponseEntity<>("Email is required", HttpStatus.BAD_REQUEST);
            }

            if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
                return new ResponseEntity<>("Password is required", HttpStatus.BAD_REQUEST);
            }

            if (user.getPassword().length() < 6) {
                return new ResponseEntity<>("Password must be at least 6 characters long", HttpStatus.BAD_REQUEST);
            }

            // Ensure role is set to GUEST if not specified
            if (user.getRole() == null || user.getRole().trim().isEmpty()) {
                user.setRole("GUEST");
            }

            // Register user DYNAMICALLY - saves to database
            User registeredUser = userService.registerUser(user);

            // Create response WITHOUT exposing password
            Map<String, Object> response = new HashMap<>();
            response.put("id", registeredUser.getId());
            response.put("name", registeredUser.getName());
            response.put("email", registeredUser.getEmail());
            response.put("role", registeredUser.getRole());
            response.put("message", "Registration successful! A default hotel card has been created for you.");

            return new ResponseEntity<>(response, HttpStatus.CREATED);

        } catch (RuntimeException e) {
            // Handle duplicate email, etc.
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("An unexpected error occurred: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Login user via API - DYNAMIC (checks database)
     */
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest, HttpSession session) {
        try {
            // Validate input
            if (loginRequest.getEmail() == null || loginRequest.getEmail().trim().isEmpty()) {
                return new ResponseEntity<>("Email is required", HttpStatus.BAD_REQUEST);
            }

            if (loginRequest.getPassword() == null || loginRequest.getPassword().trim().isEmpty()) {
                return new ResponseEntity<>("Password is required", HttpStatus.BAD_REQUEST);
            }

            // Authenticate user DYNAMICALLY - checks database
            User user = userService.loginUser(loginRequest.getEmail(), loginRequest.getPassword());

            // Store user information in HTTP session (server-side)
            session.setAttribute("userId", user.getId());
            session.setAttribute("userName", user.getName());
            session.setAttribute("userEmail", user.getEmail());
            session.setAttribute("userRole", user.getRole());

            // Create response WITHOUT exposing password
            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId());
            response.put("name", user.getName());
            response.put("email", user.getEmail());
            response.put("role", user.getRole());
            response.put("redirectUrl", getRedirectUrlByRole(user.getRole()));

            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (RuntimeException e) {
            // Handle invalid credentials
            return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            return new ResponseEntity<>("An unexpected error occurred: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Logout user
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(HttpSession session) {
        try {
            session.invalidate();

            Map<String, String> response = new HashMap<>();
            response.put("message", "Logged out successfully");

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Logout failed: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get current logged-in user - DYNAMIC (from session + database)
     */
    @GetMapping("/current")
    public ResponseEntity<?> getCurrentUser(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return new ResponseEntity<>("Not logged in", HttpStatus.UNAUTHORIZED);
        }

        try {
            // Fetch user from database dynamically
            User user = userService.getUserById(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId());
            response.put("name", user.getName());
            response.put("email", user.getEmail());
            response.put("role", user.getRole());
            response.put("redirectUrl", getRedirectUrlByRole(user.getRole()));

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException e) {
            // User not found in database but exists in session
            session.invalidate();
            return new ResponseEntity<>("Session expired", HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Check if user is authenticated - DYNAMIC (from session)
     */
    @GetMapping("/authenticated")
    public ResponseEntity<?> isAuthenticated(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");

        Map<String, Object> response = new HashMap<>();
        response.put("authenticated", userId != null);

        if (userId != null) {
            response.put("userId", userId);
            response.put("userName", session.getAttribute("userName"));
            response.put("userRole", session.getAttribute("userRole"));
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Determine redirect URL based on user role - DYNAMIC
     */
    private String getRedirectUrlByRole(String role) {
        if (role == null) return "/dashboard";

        switch (role) {
            case "GUEST":
                return "/customer/dashboard";
            case "RESERVATION_COORDINATOR":
                return "/reservations";
            case "HOUSEKEEPING_SUPERVISOR":
                return "/housekeeping";
            case "FINANCE_OFFICER":
                return "/finance";
            case "RESTAURANT_MANAGER":
                return "/restaurant";
            case "HOTEL_MANAGER":
                return "/reporting";
            case "ADMIN":
                return "/dashboard";
            default:
                return "/dashboard";
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable Long userId) {
        try {
            User user = userService.getUserById(userId);

            // Return user WITHOUT password
            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId());
            response.put("name", user.getName());
            response.put("email", user.getEmail());
            response.put("role", user.getRole());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}