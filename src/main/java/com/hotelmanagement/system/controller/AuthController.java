package com.hotelmanagement.system.controller;

import com.hotelmanagement.system.model.User;
import com.hotelmanagement.system.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    /**
     * Show login page
     */
    @GetMapping("/login")
    public String showLoginPage(HttpSession session, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        // Check if user is already logged in
        Long userId = (Long) session.getAttribute("userId");
        if (userId != null) {
            String userRole = (String) session.getAttribute("userRole");
            return "redirect:" + getRedirectUrlByRole(userRole);
        }

        // Load "Remember Me" email from cookie
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("rememberedEmail".equals(cookie.getName())) {
                    redirectAttributes.addFlashAttribute("savedEmail", cookie.getValue());
                    break;
                }
            }
        }

        return "login";
    }

    /**
     * Handle login form submission - UNIFIED (works for all user types)
     */
    @PostMapping("/login")
    public String login(
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam(required = false) String rememberMe,
            HttpSession session,
            HttpServletResponse response,
            RedirectAttributes redirectAttributes) {

        // Validation
        if (email == null || email.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Email is required.");
            return "redirect:/login";
        }

        if (password == null || password.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Password is required.");
            return "redirect:/login";
        }

        try {
            // Authenticate user from database
            User user = userService.loginUser(email, password);

            // Store user information in session
            session.setAttribute("userId", user.getId());
            session.setAttribute("userName", user.getName());
            session.setAttribute("userEmail", user.getEmail());
            session.setAttribute("userRole", user.getRole());

            // Handle "Remember Me" functionality
            if ("on".equals(rememberMe)) {
                Cookie emailCookie = new Cookie("rememberedEmail", email);
                emailCookie.setMaxAge(30 * 24 * 60 * 60); // 30 days
                emailCookie.setPath("/");
                response.addCookie(emailCookie);
            } else {
                // Remove the cookie if "Remember Me" is not checked
                Cookie emailCookie = new Cookie("rememberedEmail", "");
                emailCookie.setMaxAge(0);
                emailCookie.setPath("/");
                response.addCookie(emailCookie);
            }

            // Redirect based on user role
            String redirectUrl = getRedirectUrlByRole(user.getRole());

            // Add success message for customers
            if ("GUEST".equals(user.getRole())) {
                redirectAttributes.addFlashAttribute("success", "Welcome back, " + user.getName() + "!");
            }

            return "redirect:" + redirectUrl;

        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("savedEmail", email);
            return "redirect:/login";
        }
    }

    /**
     * Show registration page
     */
    @GetMapping("/register")
    public String showRegistrationPage(HttpSession session) {
        // Check if user is already logged in
        Long userId = (Long) session.getAttribute("userId");
        if (userId != null) {
            String userRole = (String) session.getAttribute("userRole");
            return "redirect:" + getRedirectUrlByRole(userRole);
        }

        return "register";
    }

    /**
     * Handle registration form submission - UNIFIED (creates GUEST users)
     */
    @PostMapping("/register")
    public String register(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            @RequestParam(required = false) String agreeTerms,
            RedirectAttributes redirectAttributes) {

        // Validation
        if (name == null || name.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Name is required.");
            return "redirect:/register";
        }

        if (name.length() < 3) {
            redirectAttributes.addFlashAttribute("error", "Name must be at least 3 characters long.");
            return "redirect:/register";
        }

        if (email == null || email.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Email is required.");
            return "redirect:/register";
        }

        // Email format validation
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            redirectAttributes.addFlashAttribute("error", "Please enter a valid email address.");
            return "redirect:/register";
        }

        if (password == null || password.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Password is required.");
            return "redirect:/register";
        }

        if (password.length() < 6) {
            redirectAttributes.addFlashAttribute("error", "Password must be at least 6 characters long.");
            return "redirect:/register";
        }

        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Passwords do not match.");
            return "redirect:/register";
        }

        if (!"on".equals(agreeTerms)) {
            redirectAttributes.addFlashAttribute("error", "You must agree to the Terms of Service and Privacy Policy.");
            return "redirect:/register";
        }

        try {
            // Create new user with GUEST role (customer)
            User newUser = new User();
            newUser.setName(name.trim());
            newUser.setEmail(email.trim().toLowerCase());
            newUser.setPassword(password);
            newUser.setRole("GUEST"); // All registrations create GUEST users

            // Register user (saves to database + creates default wallet card)
            userService.registerUser(newUser);

            redirectAttributes.addFlashAttribute("success",
                    "Registration successful! A default hotel card has been created for you. Please login.");
            return "redirect:/login";

        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }

    /**
     * Handle logout
     */
    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("success", "You have been logged out successfully.");
        return "redirect:/login";
    }

    /**
     * Determine redirect URL based on user role
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
                return "/manager/dashboard";
            case "ADMIN":
                return "/dashboard";
            default:
                return "/dashboard";
        }
    }
}