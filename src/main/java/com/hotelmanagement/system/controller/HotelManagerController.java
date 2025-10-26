package com.hotelmanagement.system.controller;

import com.hotelmanagement.system.model.Booking;
import com.hotelmanagement.system.model.FoodOrder;
import com.hotelmanagement.system.model.User;
import com.hotelmanagement.system.service.BookingService;
import com.hotelmanagement.system.service.FoodOrderService;
import com.hotelmanagement.system.service.UserService;
import com.hotelmanagement.system.service.ReportsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.ResponseEntity;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/manager")
public class HotelManagerController {

    @Autowired
    private UserService userService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private FoodOrderService foodOrderService;

    @Autowired
    private ReportsService reportsService;

    /**
     * Hotel Manager Dashboard
     */
    @GetMapping("/dashboard")
    public String showManagerDashboard(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            redirectAttributes.addFlashAttribute("error", "Please login to access your dashboard.");
            return "redirect:/login";
        }

        try {
            User user = userService.getUserById(userId);
            
            // Check if user has manager role
            if (!"HOTEL_MANAGER".equals(user.getRole())) {
                redirectAttributes.addFlashAttribute("error", "Access denied. Manager privileges required.");
                return "redirect:/login";
            }

            model.addAttribute("user", user);

            // Get booking statistics
            List<Booking> allBookings = bookingService.getAllBookings();
            long totalBookings = allBookings.size();
            long confirmedBookings = allBookings.stream()
                    .filter(b -> "CONFIRMED".equals(b.getStatus()))
                    .count();
            long checkedInBookings = allBookings.stream()
                    .filter(b -> "CHECKED_IN".equals(b.getStatus()))
                    .count();
            long pendingBookings = allBookings.stream()
                    .filter(b -> "PENDING".equals(b.getStatus()))
                    .count();

            model.addAttribute("totalBookings", totalBookings);
            model.addAttribute("confirmedBookings", confirmedBookings);
            model.addAttribute("checkedInBookings", checkedInBookings);
            model.addAttribute("pendingBookings", pendingBookings);

            // Get recent bookings
            List<Booking> recentBookings = allBookings.stream()
                    .limit(10)
                    .toList();
            model.addAttribute("recentBookings", recentBookings);

            // Get food order statistics
            List<FoodOrder> allOrders = foodOrderService.getAllFoodOrders();
            long totalOrders = allOrders.size();
            long pendingOrders = allOrders.stream()
                    .filter(o -> "PENDING".equals(o.getStatus()))
                    .count();
            long completedOrders = allOrders.stream()
                    .filter(o -> "COMPLETED".equals(o.getStatus()))
                    .count();

            model.addAttribute("totalOrders", totalOrders);
            model.addAttribute("pendingOrders", pendingOrders);
            model.addAttribute("completedOrders", completedOrders);

            // Get recent food orders
            List<FoodOrder> recentOrders = allOrders.stream()
                    .limit(5)
                    .toList();
            model.addAttribute("recentOrders", recentOrders);

            // Calculate revenue (simplified)
            double totalRevenue = allBookings.stream()
                    .filter(b -> "CONFIRMED".equals(b.getStatus()) || "CHECKED_IN".equals(b.getStatus()) || "CHECKED_OUT".equals(b.getStatus()))
                    .mapToDouble(b -> b.getTotalPrice())
                    .sum();

            double foodRevenue = allOrders.stream()
                    .filter(o -> "COMPLETED".equals(o.getStatus()))
                    .mapToDouble(o -> o.getTotalPrice())
                    .sum();

            model.addAttribute("totalRevenue", totalRevenue);
            model.addAttribute("foodRevenue", foodRevenue);

            return "hotel-manager-dashboard";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error loading dashboard: " + e.getMessage());
            return "redirect:/login";
        }
    }

    /**
     * Manager Reports View
     */
    @GetMapping("/reports")
    public String showManagerReports(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            redirectAttributes.addFlashAttribute("error", "Please login to access reports.");
            return "redirect:/login";
        }

        try {
            User user = userService.getUserById(userId);
            
            if (!"HOTEL_MANAGER".equals(user.getRole())) {
                redirectAttributes.addFlashAttribute("error", "Access denied. Manager privileges required.");
                return "redirect:/login";
            }

            model.addAttribute("user", user);
            return "manager-reports";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error loading reports: " + e.getMessage());
            return "redirect:/login";
        }
    }

    /**
     * API endpoint for dashboard statistics (for auto-refresh)
     */
    @GetMapping("/api/manager/dashboard-stats")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getDashboardStats(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        try {
            User user = userService.getUserById(userId);
            
            if (!"HOTEL_MANAGER".equals(user.getRole())) {
                return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
            }

            // Get booking statistics
            List<Booking> allBookings = bookingService.getAllBookings();
            long totalBookings = allBookings.size();
            long confirmedBookings = allBookings.stream()
                    .filter(b -> "CONFIRMED".equals(b.getStatus()))
                    .count();
            long checkedInBookings = allBookings.stream()
                    .filter(b -> "CHECKED_IN".equals(b.getStatus()))
                    .count();
            long pendingBookings = allBookings.stream()
                    .filter(b -> "PENDING".equals(b.getStatus()))
                    .count();

            // Get food order statistics
            List<FoodOrder> allOrders = foodOrderService.getAllFoodOrders();
            long totalOrders = allOrders.size();
            long pendingOrders = allOrders.stream()
                    .filter(o -> "PENDING".equals(o.getStatus()))
                    .count();
            long completedOrders = allOrders.stream()
                    .filter(o -> "COMPLETED".equals(o.getStatus()))
                    .count();

            // Calculate revenue
            double totalRevenue = allBookings.stream()
                    .filter(b -> "CONFIRMED".equals(b.getStatus()) || "CHECKED_IN".equals(b.getStatus()) || "CHECKED_OUT".equals(b.getStatus()))
                    .mapToDouble(b -> b.getTotalPrice())
                    .sum();

            double foodRevenue = allOrders.stream()
                    .filter(o -> "COMPLETED".equals(o.getStatus()))
                    .mapToDouble(o -> o.getTotalPrice())
                    .sum();

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalBookings", totalBookings);
            stats.put("confirmedBookings", confirmedBookings);
            stats.put("checkedInBookings", checkedInBookings);
            stats.put("pendingBookings", pendingBookings);
            stats.put("totalOrders", totalOrders);
            stats.put("pendingOrders", pendingOrders);
            stats.put("completedOrders", completedOrders);
            stats.put("totalRevenue", totalRevenue);
            stats.put("foodRevenue", foodRevenue);

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error fetching statistics: " + e.getMessage()));
        }
    }

    /**
     * Generate revenue report
     */
    @GetMapping("/api/reports/revenue")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> generateRevenueReport(
            @RequestParam String startDate,
            @RequestParam String endDate,
            HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        try {
            User user = userService.getUserById(userId);
            
            if (!"HOTEL_MANAGER".equals(user.getRole())) {
                return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
            }

            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            
            Map<String, Object> report = reportsService.generateRevenueReport(start, end);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error generating revenue report: " + e.getMessage()));
        }
    }

    /**
     * Generate occupancy report
     */
    @GetMapping("/api/reports/occupancy")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> generateOccupancyReport(
            @RequestParam String startDate,
            @RequestParam String endDate,
            HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        try {
            User user = userService.getUserById(userId);
            
            if (!"HOTEL_MANAGER".equals(user.getRole())) {
                return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
            }

            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            
            Map<String, Object> report = reportsService.generateOccupancyReport(start, end);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error generating occupancy report: " + e.getMessage()));
        }
    }

    /**
     * Generate customer report
     */
    @GetMapping("/api/reports/customers")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> generateCustomerReport(
            @RequestParam String startDate,
            @RequestParam String endDate,
            HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        try {
            User user = userService.getUserById(userId);
            
            if (!"HOTEL_MANAGER".equals(user.getRole())) {
                return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
            }

            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            
            Map<String, Object> report = reportsService.generateCustomerReport(start, end);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error generating customer report: " + e.getMessage()));
        }
    }

    /**
     * Generate food and beverage report
     */
    @GetMapping("/api/reports/food-beverage")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> generateFoodBeverageReport(
            @RequestParam String startDate,
            @RequestParam String endDate,
            HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        try {
            User user = userService.getUserById(userId);
            
            if (!"HOTEL_MANAGER".equals(user.getRole())) {
                return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
            }

            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            
            Map<String, Object> report = reportsService.generateFoodBeverageReport(start, end);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error generating food & beverage report: " + e.getMessage()));
        }
    }

    /**
     * Generate comprehensive report
     */
    @GetMapping("/api/reports/comprehensive")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> generateComprehensiveReport(
            @RequestParam String startDate,
            @RequestParam String endDate,
            HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        try {
            User user = userService.getUserById(userId);
            
            if (!"HOTEL_MANAGER".equals(user.getRole())) {
                return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
            }

            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            
            Map<String, Object> report = reportsService.generateComprehensiveReport(start, end);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error generating comprehensive report: " + e.getMessage()));
        }
    }

    /**
     * Export report as CSV
     */
    @GetMapping("/api/reports/export/csv")
    @ResponseBody
    public ResponseEntity<String> exportReportAsCSV(
            @RequestParam String reportType,
            @RequestParam String startDate,
            @RequestParam String endDate,
            HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).body("Not authenticated");
        }

        try {
            User user = userService.getUserById(userId);
            
            if (!"HOTEL_MANAGER".equals(user.getRole())) {
                return ResponseEntity.status(403).body("Access denied");
            }

            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            
            String csvData = generateCSVReport(reportType, start, end);
            return ResponseEntity.ok()
                    .header("Content-Type", "text/csv")
                    .header("Content-Disposition", "attachment; filename=" + reportType + "_report_" + startDate + "_to_" + endDate + ".csv")
                    .body(csvData);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error generating CSV report: " + e.getMessage());
        }
    }

    /**
     * Generate CSV report data
     */
    private String generateCSVReport(String reportType, LocalDate startDate, LocalDate endDate) {
        StringBuilder csv = new StringBuilder();
        
        switch (reportType.toLowerCase()) {
            case "revenue":
                Map<String, Object> revenueReport = reportsService.generateRevenueReport(startDate, endDate);
                csv.append("Report Type,Start Date,End Date,Total Revenue,Booking Revenue,Food Revenue,Total Bookings,Total Orders\n");
                csv.append("Revenue Report,")
                   .append(revenueReport.get("startDate")).append(",")
                   .append(revenueReport.get("endDate")).append(",")
                   .append(revenueReport.get("totalRevenue")).append(",")
                   .append(revenueReport.get("totalBookingRevenue")).append(",")
                   .append(revenueReport.get("totalFoodRevenue")).append(",")
                   .append(revenueReport.get("totalBookings")).append(",")
                   .append(revenueReport.get("totalFoodOrders")).append("\n");
                break;
                
            case "occupancy":
                Map<String, Object> occupancyReport = reportsService.generateOccupancyReport(startDate, endDate);
                csv.append("Date,Occupied Rooms,Total Rooms,Occupancy Rate\n");
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> dailyOccupancy = (List<Map<String, Object>>) occupancyReport.get("dailyOccupancy");
                for (Map<String, Object> day : dailyOccupancy) {
                    csv.append(day.get("date")).append(",")
                       .append(day.get("occupiedRooms")).append(",")
                       .append(day.get("totalRooms")).append(",")
                       .append(day.get("occupancyRate")).append("%\n");
                }
                break;
                
            case "customers":
                Map<String, Object> customerReport = reportsService.generateCustomerReport(startDate, endDate);
                csv.append("Customer Name,Email,Total Spent,Booking Count\n");
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> topCustomers = (List<Map<String, Object>>) customerReport.get("topCustomers");
                for (Map<String, Object> customer : topCustomers) {
                    csv.append(customer.get("name")).append(",")
                       .append(customer.get("email")).append(",")
                       .append(customer.get("totalSpent")).append(",")
                       .append(customer.get("bookingCount")).append("\n");
                }
                break;
                
            case "food-beverage":
                Map<String, Object> foodReport = reportsService.generateFoodBeverageReport(startDate, endDate);
                csv.append("Metric,Value\n");
                csv.append("Total Revenue,").append(foodReport.get("totalRevenue")).append("\n");
                csv.append("Total Orders,").append(foodReport.get("totalOrders")).append("\n");
                csv.append("Completed Orders,").append(foodReport.get("completedOrders")).append("\n");
                csv.append("Average Order Value,").append(foodReport.get("averageOrderValue")).append("\n");
                break;
                
            default:
                csv.append("Invalid report type");
        }
        
        return csv.toString();
    }

    /**
     * Export report as PDF
     */
    @GetMapping("/api/reports/export/pdf")
    @ResponseBody
    public ResponseEntity<byte[]> exportReportAsPDF(
            @RequestParam String reportType,
            @RequestParam String startDate,
            @RequestParam String endDate,
            HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).body(null);
        }

        try {
            User user = userService.getUserById(userId);

            if (!"HOTEL_MANAGER".equals(user.getRole())) {
                return ResponseEntity.status(403).body(null);
            }

            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);

            String html = buildReportHtml(reportType, start, end);

            // Convert HTML to PDF using OpenHTMLtoPDF
            java.io.ByteArrayOutputStream os = new java.io.ByteArrayOutputStream();
            com.openhtmltopdf.pdfboxout.PdfRendererBuilder builder = new com.openhtmltopdf.pdfboxout.PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(os);
            builder.run();
            byte[] pdfBytes = os.toByteArray();

            return ResponseEntity.ok()
                    .header("Content-Type", "application/pdf")
                    .header("Content-Disposition", "attachment; filename=" + reportType + "_report_" + startDate + "_to_" + endDate + ".pdf")
                    .body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    private String buildReportHtml(String reportType, LocalDate startDate, LocalDate endDate) {
        StringBuilder html = new StringBuilder();
        html.append("<html><head><meta charset='UTF-8'><style>")
            .append("body{font-family:Arial,Helvetica,sans-serif;color:#333;padding:20px;}")
            .append("h1{font-size:20px;margin:0 0 10px}")
            .append("h2{font-size:16px;margin:20px 0 8px}")
            .append("table{width:100%;border-collapse:collapse;margin-top:10px}")
            .append("th,td{border:1px solid #ddd;padding:8px;font-size:12px}")
            .append("th{background:#f5f5f5;text-align:left}")
            .append(".muted{color:#777;font-size:12px}")
            .append("</style></head><body>");

        html.append("<h1>").append(reportType.toUpperCase()).append(" Report</h1>");
        html.append("<div class='muted'>Period: ")
            .append(startDate.toString()).append(" to ")
            .append(endDate.toString()).append("</div>");

        switch (reportType.toLowerCase()) {
            case "revenue": {
                Map<String, Object> data = reportsService.generateRevenueReport(startDate, endDate);
                html.append("<h2>Summary</h2>");
                html.append("<table><tbody>")
                    .append(row("Total Revenue", "$" + fmt(data.get("totalRevenue"))))
                    .append(row("Booking Revenue", "$" + fmt(data.get("totalBookingRevenue"))))
                    .append(row("Food Revenue", "$" + fmt(data.get("totalFoodRevenue"))))
                    .append(row("Total Bookings", String.valueOf(data.get("totalBookings"))))
                    .append(row("Total Food Orders", String.valueOf(data.get("totalFoodOrders"))))
                    .append("</tbody></table>");
                break;
            }
            case "occupancy": {
                Map<String, Object> data = reportsService.generateOccupancyReport(startDate, endDate);
                html.append("<h2>Daily Occupancy</h2>");
                html.append("<table><thead><tr><th>Date</th><th>Occupied</th><th>Total</th><th>Rate</th></tr></thead><tbody>");
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> days = (List<Map<String, Object>>) data.get("dailyOccupancy");
                for (Map<String, Object> d : days) {
                    html.append("<tr>")
                        .append(cell(String.valueOf(d.get("dateFormatted"))))
                        .append(cell(String.valueOf(d.get("occupiedRooms"))))
                        .append(cell(String.valueOf(d.get("totalRooms"))))
                        .append(cell(String.valueOf(d.get("occupancyRate")) + "%"))
                        .append("</tr>");
                }
                html.append("</tbody></table>");
                break;
            }
            case "customers": {
                Map<String, Object> data = reportsService.generateCustomerReport(startDate, endDate);
                html.append("<h2>Top Customers</h2>");
                html.append("<table><thead><tr><th>Name</th><th>Email</th><th>Total Spent</th><th>Bookings</th></tr></thead><tbody>");
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> customers = (List<Map<String, Object>>) data.get("topCustomers");
                for (Map<String, Object> c : customers) {
                    html.append("<tr>")
                        .append(cell(String.valueOf(c.get("name"))))
                        .append(cell(String.valueOf(c.get("email"))))
                        .append(cell("$" + fmt(c.get("totalSpent"))))
                        .append(cell(String.valueOf(c.get("bookingCount"))))
                        .append("</tr>");
                }
                html.append("</tbody></table>");
                break;
            }
            case "food-beverage": {
                Map<String, Object> data = reportsService.generateFoodBeverageReport(startDate, endDate);
                html.append("<h2>Summary</h2>");
                html.append("<table><tbody>")
                    .append(row("Total Revenue", "$" + fmt(data.get("totalRevenue"))))
                    .append(row("Total Orders", String.valueOf(data.get("totalOrders"))))
                    .append(row("Completed Orders", String.valueOf(data.get("completedOrders"))))
                    .append(row("Average Order Value", "$" + fmt(data.get("averageOrderValue"))))
                    .append("</tbody></table>");
                break;
            }
            case "comprehensive": {
                Map<String, Object> data = reportsService.generateComprehensiveReport(startDate, endDate);
                html.append("<h2>Revenue</h2>");
                Map<String, Object> rev = (Map<String, Object>) data.get("revenue");
                html.append("<table><tbody>")
                    .append(row("Total Revenue", "$" + fmt(rev.get("totalRevenue"))))
                    .append(row("Booking Revenue", "$" + fmt(rev.get("totalBookingRevenue"))))
                    .append(row("Food Revenue", "$" + fmt(rev.get("totalFoodRevenue"))))
                    .append("</tbody></table>");
                break;
            }
            default:
                html.append("<p>Invalid report type</p>");
        }

        html.append("</body></html>");
        return html.toString();
    }

    private String row(String k, String v) {
        return "<tr><th>" + k + "</th><td>" + v + "</td></tr>";
    }

    private String cell(String v) {
        return "<td>" + v + "</td>";
    }

    private String fmt(Object n) {
        try {
            double d = n instanceof Number ? ((Number) n).doubleValue() : Double.parseDouble(String.valueOf(n));
            return String.format(java.util.Locale.US, "%,.2f", d);
        } catch (Exception e) {
            return String.valueOf(n);
        }
    }

}
