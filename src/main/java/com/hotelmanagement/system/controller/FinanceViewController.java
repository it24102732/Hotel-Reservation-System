package com.hotelmanagement.system.controller;

import com.hotelmanagement.system.model.Refund;
import com.hotelmanagement.system.service.FinanceService;
import com.hotelmanagement.system.service.RefundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/finance")
public class FinanceViewController {

    @Autowired
    private FinanceService financeService;

    @Autowired
    private RefundService refundService;

    /**
     * Handles GET requests for the finance dashboard.
     * It fetches all data and adds it to the model for Thymeleaf to render.
     */
    @GetMapping
    public String showFinanceDashboard(@RequestParam(value = "filter", defaultValue = "PENDING") String filter, Model model) {
        // 1. Get summary data
        Map<String, Object> summary = financeService.getFinancialSummary();
        model.addAttribute("summary", summary);

        // 2. Get refunds based on the filter
        List<Refund> refunds = "ALL".equalsIgnoreCase(filter) ?
                refundService.getAllRefunds() :
                refundService.getRefundsByStatus(filter.toUpperCase());
        model.addAttribute("refunds", refunds);
        model.addAttribute("currentFilter", filter);

        // 3. Get all transactions and cancelled bookings for other tabs
        model.addAttribute("transactions", financeService.getAllTransactions());
        model.addAttribute("cancelledBookings", financeService.getCancelledBookings());

        return "finance"; // Renders finance.html
    }

    /**
     * Handles the form submission to PROCESS a refund.
     */
    @PostMapping("/refunds/{id}/process")
    public String processRefund(@PathVariable("id") Long refundId, RedirectAttributes redirectAttributes) {
        try {
            Refund processedRefund = refundService.processMockRefund(refundId);
            redirectAttributes.addFlashAttribute("success",
                    "Refund processed successfully! Amount $" + String.format("%.2f", processedRefund.getAmount()) +
                            " has been credited to customer's default card.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Error processing refund: " + e.getMessage());
        }
        return "redirect:/finance";
    }

    /**
     * Handles the form submission to REJECT a refund.
     */
    @PostMapping("/refunds/{id}/reject")
    public String rejectRefund(@PathVariable("id") Long refundId,
                               @RequestParam("reason") String reason,
                               RedirectAttributes redirectAttributes) {
        try {
            if (reason == null || reason.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Rejection reason is required.");
                return "redirect:/finance";
            }
            refundService.rejectRefund(refundId, reason);
            redirectAttributes.addFlashAttribute("success", "Refund rejected successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error rejecting refund: " + e.getMessage());
        }
        return "redirect:/finance";
    }
}