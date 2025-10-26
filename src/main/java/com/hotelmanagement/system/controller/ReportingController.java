package com.hotelmanagement.system.controller;

import com.hotelmanagement.system.model.*;
import com.hotelmanagement.system.service.ReportingService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportingController {

    @Autowired
    private ReportingService reportingService;

    // ==================== NON-FINANCIAL REPORTS ====================

    @GetMapping("/occupancy")
    public ResponseEntity<Map<String, Object>> getOccupancyReport() {
        try {
            Map<String, Object> report = reportingService.generateOccupancyReport();
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/booking-analytics")
    public ResponseEntity<Map<String, Object>> getBookingAnalytics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            Map<String, Object> analytics = reportingService.generateBookingAnalytics(startDate, endDate);
            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getReportsSummary() {
        try {
            Map<String, Object> summary = reportingService.getReportsSummary();
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ==================== REPORT CREATION ====================

    @PostMapping("/create")
    public ResponseEntity<?> createReport(
            @RequestParam String reportType,
            @RequestParam String reportName,
            @RequestParam(required = false) String description,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            HttpSession session) {
        try {
            String generatedBy = (String) session.getAttribute("userName");
            if (generatedBy == null) {
                generatedBy = "System";
            }

            Report report = reportingService.createReport(reportType, reportName, description,
                    startDate, endDate, generatedBy);
            return ResponseEntity.status(HttpStatus.CREATED).body(report);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // ==================== REVENUE ENTRIES ====================

    @PostMapping("/{reportId}/revenue-entry")
    public ResponseEntity<?> addRevenueEntry(
            @PathVariable Long reportId,
            @RequestParam String source,
            @RequestParam String description,
            @RequestParam Double amount,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate entryDate,
            HttpSession session) {
        try {
            String createdBy = (String) session.getAttribute("userName");
            if (createdBy == null) {
                createdBy = "System";
            }

            RevenueEntry entry = reportingService.addRevenueEntry(reportId, source, description,
                    amount, entryDate, createdBy);
            return ResponseEntity.status(HttpStatus.CREATED).body(entry);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @PutMapping("/revenue-entry/{entryId}")
    public ResponseEntity<?> updateRevenueEntry(
            @PathVariable Long entryId,
            @RequestParam String source,
            @RequestParam String description,
            @RequestParam Double amount,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate entryDate) {
        try {
            RevenueEntry entry = reportingService.updateRevenueEntry(entryId, source, description,
                    amount, entryDate);
            return ResponseEntity.ok(entry);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @DeleteMapping("/revenue-entry/{entryId}")
    public ResponseEntity<?> deleteRevenueEntry(@PathVariable Long entryId) {
        try {
            reportingService.deleteRevenueEntry(entryId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Revenue entry deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // NEW ENDPOINT: Get a single revenue entry to simplify editing on the front-end
    @GetMapping("/revenue-entry/{entryId}")
    public ResponseEntity<?> getRevenueEntry(@PathVariable Long entryId) {
        try {
            RevenueEntry entry = reportingService.getRevenueEntryById(entryId);
            return ResponseEntity.ok(entry);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    // ==================== EXPENSE ENTRIES ====================

    @PostMapping("/{reportId}/expense-entry")
    public ResponseEntity<?> addExpenseEntry(
            @PathVariable Long reportId,
            @RequestParam String category,
            @RequestParam String description,
            @RequestParam Double amount,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate entryDate,
            HttpSession session) {
        try {
            String createdBy = (String) session.getAttribute("userName");
            if (createdBy == null) {
                createdBy = "System";
            }

            ExpenseEntry entry = reportingService.addExpenseEntry(reportId, category, description,
                    amount, entryDate, createdBy);
            return ResponseEntity.status(HttpStatus.CREATED).body(entry);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @PutMapping("/expense-entry/{entryId}")
    public ResponseEntity<?> updateExpenseEntry(
            @PathVariable Long entryId,
            @RequestParam String category,
            @RequestParam String description,
            @RequestParam Double amount,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate entryDate) {
        try {
            ExpenseEntry entry = reportingService.updateExpenseEntry(entryId, category, description,
                    amount, entryDate);
            return ResponseEntity.ok(entry);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @DeleteMapping("/expense-entry/{entryId}")
    public ResponseEntity<?> deleteExpenseEntry(@PathVariable Long entryId) {
        try {
            reportingService.deleteExpenseEntry(entryId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Expense entry deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // NEW ENDPOINT: Get a single expense entry to simplify editing on the front-end
    @GetMapping("/expense-entry/{entryId}")
    public ResponseEntity<?> getExpenseEntry(@PathVariable Long entryId) {
        try {
            ExpenseEntry entry = reportingService.getExpenseEntryById(entryId);
            return ResponseEntity.ok(entry);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }


    // ==================== REPORT MANAGEMENT ====================

    @GetMapping
    public ResponseEntity<?> getAllReports() {
        try {
            List<Report> reports = reportingService.getAllReports();
            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/type/{reportType}")
    public ResponseEntity<?> getReportsByType(@PathVariable String reportType) {
        try {
            List<Report> reports = reportingService.getReportsByType(reportType);
            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<?> getReportsByStatus(@PathVariable String status) {
        try {
            List<Report> reports = reportingService.getReportsByStatus(status);
            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @GetMapping("/{reportId}")
    public ResponseEntity<?> getReportDetails(@PathVariable Long reportId) {
        try {
            Map<String, Object> details = reportingService.getReportDetails(reportId);
            return ResponseEntity.ok(details);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    // NEW ENDPOINT: Get a single raw report object to simplify editing on the front-end
    @GetMapping("/{reportId}/raw")
    public ResponseEntity<?> getReportRaw(@PathVariable Long reportId) {
        try {
            Report report = reportingService.getReportById(reportId);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @PutMapping("/{reportId}")
    public ResponseEntity<?> updateReport(
            @PathVariable Long reportId,
            @RequestParam String reportName,
            @RequestParam(required = false) String description,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            Report report = reportingService.updateReport(reportId, reportName, description,
                    startDate, endDate);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @PutMapping("/{reportId}/finalize")
    public ResponseEntity<?> finalizeReport(@PathVariable Long reportId) {
        try {
            Report report = reportingService.finalizeReport(reportId);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Report finalized successfully");
            response.put("report", report);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @DeleteMapping("/{reportId}")
    public ResponseEntity<?> deleteReport(@PathVariable Long reportId) {
        try {
            reportingService.deleteReport(reportId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Report deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @GetMapping("/{reportId}/revenue-entries")
    public ResponseEntity<?> getRevenueEntries(@PathVariable Long reportId) {
        try {
            List<RevenueEntry> entries = reportingService.getRevenueEntriesByReport(reportId);
            return ResponseEntity.ok(entries);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @GetMapping("/{reportId}/expense-entries")
    public ResponseEntity<?> getExpenseEntries(@PathVariable Long reportId) {
        try {
            List<ExpenseEntry> entries = reportingService.getExpenseEntriesByReport(reportId);
            return ResponseEntity.ok(entries);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
}