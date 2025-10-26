package com.hotelmanagement.system.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/financial-reports") // Changed from /reporting
public class ReportingViewController {

    @GetMapping
    public String showReportsDashboard(HttpSession session, RedirectAttributes redirectAttributes) {
        // Check if user is logged in
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            redirectAttributes.addFlashAttribute("error", "Please login to access this page.");
            return "redirect:/login";
        }

        // Check if user has proper role
        String userRole = (String) session.getAttribute("userRole");
        if (!"FINANCE_OFFICER".equals(userRole) && !"ADMIN".equals(userRole)) {
            redirectAttributes.addFlashAttribute("error", "You don't have permission to access this page.");
            return "redirect:/dashboard";
        }

        return "financial-reports";
    }
}