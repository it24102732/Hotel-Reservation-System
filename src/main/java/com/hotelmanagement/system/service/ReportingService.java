package com.hotelmanagement.system.service;

import com.hotelmanagement.system.model.*;
import com.hotelmanagement.system.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private FoodOrderRepository foodOrderRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private RevenueEntryRepository revenueEntryRepository;

    @Autowired
    private ExpenseEntryRepository expenseEntryRepository;

    // ==================== NON-FINANCIAL REPORTS ====================

    public Map<String, Object> generateOccupancyReport() {
        long totalRooms = roomRepository.count();
        long occupiedRooms = roomRepository.findByIsAvailable(false).size();
        long availableRooms = totalRooms - occupiedRooms;
        double occupancyRate = (totalRooms > 0) ? ((double) occupiedRooms / totalRooms) * 100 : 0;

        Map<String, Object> report = new HashMap<>();
        report.put("totalRooms", totalRooms);
        report.put("occupiedRooms", occupiedRooms);
        report.put("availableRooms", availableRooms);
        report.put("occupancyRate", Math.round(occupancyRate * 100.0) / 100.0);

        return report;
    }

    public Map<String, Object> generateBookingAnalytics(LocalDateTime startDate, LocalDateTime endDate) {
        List<Booking> bookings = bookingRepository.findAll().stream()
                .filter(b -> {
                    LocalDateTime checkInDateTime = b.getCheckInDate().atStartOfDay();
                    return !checkInDateTime.isBefore(startDate) && !checkInDateTime.isAfter(endDate);
                })
                .collect(Collectors.toList());

        Map<String, Object> analytics = new HashMap<>();
        analytics.put("totalBookings", bookings.size());
        analytics.put("confirmedBookings", bookings.stream().filter(b -> "CONFIRMED".equalsIgnoreCase(b.getStatus())).count());
        analytics.put("cancelledBookings", bookings.stream().filter(b -> "CANCELLED".equalsIgnoreCase(b.getStatus())).count());
        analytics.put("pendingBookings", bookings.stream().filter(b -> "PENDING".equalsIgnoreCase(b.getStatus())).count());

        return analytics;
    }

    // ==================== REPORT CREATION ====================

    @Transactional
    public Report createReport(String reportType, String reportName, String description,
                               LocalDateTime startDate, LocalDateTime endDate, String generatedBy) {

        if (!Arrays.asList("REVENUE", "EXPENSE", "PROFIT_LOSS", "OCCUPANCY").contains(reportType)) {
            throw new RuntimeException("Invalid report type");
        }

        Report report = new Report();
        report.setReportType(reportType);
        report.setReportName(reportName);
        report.setDescription(description);
        report.setStartDate(startDate);
        report.setEndDate(endDate);
        report.setGeneratedBy(generatedBy != null ? generatedBy : "System");
        report.setStatus("DRAFT");
        report.setTotalAmount(0.0);
        report.setGeneratedDate(LocalDateTime.now());

        return reportRepository.save(report);
    }

    // ==================== REVENUE ENTRIES ====================

    @Transactional
    public RevenueEntry addRevenueEntry(Long reportId, String source, String description,
                                        Double amount, LocalDate entryDate, String createdBy) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found with ID: " + reportId));

        if (!"REVENUE".equals(report.getReportType()) && !"PROFIT_LOSS".equals(report.getReportType())) {
            throw new RuntimeException("Cannot add revenue entry to " + report.getReportType() + " report");
        }

        if ("FINALIZED".equals(report.getStatus())) {
            throw new RuntimeException("Cannot modify finalized report");
        }

        RevenueEntry entry = new RevenueEntry();
        entry.setSource(source);
        entry.setDescription(description);
        entry.setAmount(amount);
        entry.setEntryDate(entryDate);
        entry.setCreatedBy(createdBy != null ? createdBy : "System");
        entry.setReport(report);
        entry.setCreatedAt(LocalDateTime.now());

        RevenueEntry savedEntry = revenueEntryRepository.save(entry);
        updateReportTotal(reportId);

        return savedEntry;
    }

    @Transactional
    public RevenueEntry updateRevenueEntry(Long entryId, String source, String description,
                                           Double amount, LocalDate entryDate) {
        RevenueEntry entry = revenueEntryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Revenue entry not found with ID: " + entryId));

        if (entry.getReport() != null && "FINALIZED".equals(entry.getReport().getStatus())) {
            throw new RuntimeException("Cannot modify entry in finalized report");
        }

        entry.setSource(source);
        entry.setDescription(description);
        entry.setAmount(amount);
        entry.setEntryDate(entryDate);

        RevenueEntry updated = revenueEntryRepository.save(entry);

        if (entry.getReport() != null) {
            updateReportTotal(entry.getReport().getId());
        }

        return updated;
    }

    @Transactional
    public void deleteRevenueEntry(Long entryId) {
        RevenueEntry entry = revenueEntryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Revenue entry not found with ID: " + entryId));

        if (entry.getReport() != null && "FINALIZED".equals(entry.getReport().getStatus())) {
            throw new RuntimeException("Cannot delete entry from finalized report");
        }

        Long reportId = entry.getReport() != null ? entry.getReport().getId() : null;
        revenueEntryRepository.deleteById(entryId);

        if (reportId != null) {
            updateReportTotal(reportId);
        }
    }

    // ==================== EXPENSE ENTRIES ====================

    @Transactional
    public ExpenseEntry addExpenseEntry(Long reportId, String category, String description,
                                        Double amount, LocalDate entryDate, String createdBy) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found with ID: " + reportId));

        if (!"EXPENSE".equals(report.getReportType()) && !"PROFIT_LOSS".equals(report.getReportType())) {
            throw new RuntimeException("Cannot add expense entry to " + report.getReportType() + " report");
        }

        if ("FINALIZED".equals(report.getStatus())) {
            throw new RuntimeException("Cannot modify finalized report");
        }

        ExpenseEntry entry = new ExpenseEntry();
        entry.setCategory(category);
        entry.setDescription(description);
        entry.setAmount(amount);
        entry.setEntryDate(entryDate);
        entry.setCreatedBy(createdBy != null ? createdBy : "System");
        entry.setReport(report);
        entry.setCreatedAt(LocalDateTime.now());

        ExpenseEntry savedEntry = expenseEntryRepository.save(entry);
        updateReportTotal(reportId);

        return savedEntry;
    }

    @Transactional
    public ExpenseEntry updateExpenseEntry(Long entryId, String category, String description,
                                           Double amount, LocalDate entryDate) {
        ExpenseEntry entry = expenseEntryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Expense entry not found with ID: " + entryId));

        if (entry.getReport() != null && "FINALIZED".equals(entry.getReport().getStatus())) {
            throw new RuntimeException("Cannot modify entry in finalized report");
        }

        entry.setCategory(category);
        entry.setDescription(description);
        entry.setAmount(amount);
        entry.setEntryDate(entryDate);

        ExpenseEntry updated = expenseEntryRepository.save(entry);

        if (entry.getReport() != null) {
            updateReportTotal(entry.getReport().getId());
        }

        return updated;
    }

    @Transactional
    public void deleteExpenseEntry(Long entryId) {
        ExpenseEntry entry = expenseEntryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Expense entry not found with ID: " + entryId));

        if (entry.getReport() != null && "FINALIZED".equals(entry.getReport().getStatus())) {
            throw new RuntimeException("Cannot delete entry from finalized report");
        }

        Long reportId = entry.getReport() != null ? entry.getReport().getId() : null;
        expenseEntryRepository.deleteById(entryId);

        if (reportId != null) {
            updateReportTotal(reportId);
        }
    }

    // ==================== REPORT MANAGEMENT ====================

    @Transactional
    public void updateReportTotal(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found with ID: " + reportId));

        double total = 0.0;

        if ("REVENUE".equals(report.getReportType())) {
            List<RevenueEntry> entries = revenueEntryRepository.findByReport(report);
            total = entries.stream().mapToDouble(RevenueEntry::getAmount).sum();
        } else if ("EXPENSE".equals(report.getReportType())) {
            List<ExpenseEntry> entries = expenseEntryRepository.findByReport(report);
            total = entries.stream().mapToDouble(ExpenseEntry::getAmount).sum();
        } else if ("PROFIT_LOSS".equals(report.getReportType())) {
            List<RevenueEntry> revenueEntries = revenueEntryRepository.findByReport(report);
            List<ExpenseEntry> expenseEntries = expenseEntryRepository.findByReport(report);

            double totalRevenue = revenueEntries.stream().mapToDouble(RevenueEntry::getAmount).sum();
            double totalExpense = expenseEntries.stream().mapToDouble(ExpenseEntry::getAmount).sum();
            total = totalRevenue - totalExpense;
        }

        report.setTotalAmount(Math.round(total * 100.0) / 100.0);
        report.setLastModified(LocalDateTime.now());
        reportRepository.save(report);
    }

    @Transactional
    public Report updateReport(Long reportId, String reportName, String description,
                               LocalDateTime startDate, LocalDateTime endDate) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found with ID: " + reportId));

        if ("FINALIZED".equals(report.getStatus())) {
            throw new RuntimeException("Cannot modify finalized report");
        }

        report.setReportName(reportName);
        report.setDescription(description);
        report.setStartDate(startDate);
        report.setEndDate(endDate);
        report.setLastModified(LocalDateTime.now());

        return reportRepository.save(report);
    }

    @Transactional
    public Report finalizeReport(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found with ID: " + reportId));

        if ("FINALIZED".equals(report.getStatus())) {
            throw new RuntimeException("Report is already finalized");
        }

        report.setStatus("FINALIZED");
        report.setLastModified(LocalDateTime.now());
        return reportRepository.save(report);
    }

    @Transactional
    public void deleteReport(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found with ID: " + reportId));

        if ("FINALIZED".equals(report.getStatus())) {
            throw new RuntimeException("Cannot delete finalized report");
        }

        reportRepository.deleteById(reportId);
    }

    // ==================== GETTERS ====================

    public Report getReportById(Long reportId) {
        return reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found with ID: " + reportId));
    }

    public List<Report> getAllReports() {
        return reportRepository.findAllByOrderByGeneratedDateDesc();
    }

    public List<Report> getReportsByType(String reportType) {
        return reportRepository.findByReportTypeOrderByGeneratedDateDesc(reportType);
    }

    public List<Report> getReportsByStatus(String status) {
        return reportRepository.findByStatus(status);
    }

    public List<RevenueEntry> getRevenueEntriesByReport(Long reportId) {
        return revenueEntryRepository.findByReportIdOrderByEntryDateDesc(reportId);
    }

    public List<ExpenseEntry> getExpenseEntriesByReport(Long reportId) {
        return expenseEntryRepository.findByReportIdOrderByEntryDateDesc(reportId);
    }

    public Map<String, Object> getReportDetails(Long reportId) {
        Report report = getReportById(reportId);
        Map<String, Object> details = new HashMap<>();

        details.put("report", report);

        if ("REVENUE".equals(report.getReportType()) || "PROFIT_LOSS".equals(report.getReportType())) {
            List<RevenueEntry> revenueEntries = getRevenueEntriesByReport(reportId);
            details.put("revenueEntries", revenueEntries);

            if ("PROFIT_LOSS".equals(report.getReportType())) {
                double totalRevenue = revenueEntries.stream().mapToDouble(RevenueEntry::getAmount).sum();
                details.put("totalRevenue", Math.round(totalRevenue * 100.0) / 100.0);
            }
        }

        if ("EXPENSE".equals(report.getReportType()) || "PROFIT_LOSS".equals(report.getReportType())) {
            List<ExpenseEntry> expenseEntries = getExpenseEntriesByReport(reportId);
            details.put("expenseEntries", expenseEntries);

            if ("PROFIT_LOSS".equals(report.getReportType())) {
                double totalExpense = expenseEntries.stream().mapToDouble(ExpenseEntry::getAmount).sum();
                details.put("totalExpense", Math.round(totalExpense * 100.0) / 100.0);
            }
        }

        return details;
    }

    public Map<String, Object> getReportsSummary() {
        List<Report> allReports = reportRepository.findAll();

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalReports", allReports.size());
        summary.put("revenueReports", allReports.stream().filter(r -> "REVENUE".equals(r.getReportType())).count());
        summary.put("expenseReports", allReports.stream().filter(r -> "EXPENSE".equals(r.getReportType())).count());
        summary.put("profitLossReports", allReports.stream().filter(r -> "PROFIT_LOSS".equals(r.getReportType())).count());
        summary.put("draftReports", allReports.stream().filter(r -> "DRAFT".equals(r.getStatus())).count());
        summary.put("finalizedReports", allReports.stream().filter(r -> "FINALIZED".equals(r.getStatus())).count());

        return summary;
    }

    // ==================== NEW GETTERS FOR SINGLE ENTRIES ====================

    /**
     * NEW METHOD
     * Fetches a single revenue entry by its ID.
     * @param entryId The ID of the revenue entry.
     * @return The RevenueEntry object.
     */
    public RevenueEntry getRevenueEntryById(Long entryId) {
        return revenueEntryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Revenue entry not found with ID: " + entryId));
    }

    /**
     * NEW METHOD
     * Fetches a single expense entry by its ID.
     * @param entryId The ID of the expense entry.
     * @return The ExpenseEntry object.
     */
    public ExpenseEntry getExpenseEntryById(Long entryId) {
        return expenseEntryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Expense entry not found with ID: " + entryId));
    }
}