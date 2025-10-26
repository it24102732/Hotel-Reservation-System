package com.hotelmanagement.system.service;

import com.hotelmanagement.system.model.Booking;
import com.hotelmanagement.system.model.FoodOrder;
import com.hotelmanagement.system.model.User;
import com.hotelmanagement.system.repository.BookingRepository;
import com.hotelmanagement.system.repository.FoodOrderRepository;
import com.hotelmanagement.system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportsService {

    private final BookingRepository bookingRepository;
    private final FoodOrderRepository foodOrderRepository;
    private final UserRepository userRepository;

    @Autowired
    public ReportsService(BookingRepository bookingRepository, 
                         FoodOrderRepository foodOrderRepository,
                         UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.foodOrderRepository = foodOrderRepository;
        this.userRepository = userRepository;
    }

    /**
     * Generate revenue report for a date range
     */
    public Map<String, Object> generateRevenueReport(LocalDate startDate, LocalDate endDate) {
        List<Booking> bookings = bookingRepository.findAll().stream()
                .filter(b -> isBookingInDateRange(b, startDate, endDate))
                .collect(Collectors.toList());

        List<FoodOrder> foodOrders = foodOrderRepository.findAll().stream()
                .filter(o -> isOrderInDateRange(o, startDate, endDate))
                .collect(Collectors.toList());

        double totalBookingRevenue = bookings.stream()
                .filter(b -> "CONFIRMED".equals(b.getStatus()) || "CHECKED_IN".equals(b.getStatus()) || "CHECKED_OUT".equals(b.getStatus()))
                .mapToDouble(Booking::getTotalPrice)
                .sum();

        double totalFoodRevenue = foodOrders.stream()
                .filter(o -> "COMPLETED".equals(o.getStatus()))
                .mapToDouble(FoodOrder::getTotalPrice)
                .sum();

        double totalRevenue = totalBookingRevenue + totalFoodRevenue;

        Map<String, Object> report = new HashMap<>();
        report.put("startDate", startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        report.put("endDate", endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        report.put("totalBookingRevenue", totalBookingRevenue);
        report.put("totalFoodRevenue", totalFoodRevenue);
        report.put("totalRevenue", totalRevenue);
        report.put("totalBookings", bookings.size());
        report.put("totalFoodOrders", foodOrders.size());
        report.put("averageBookingValue", bookings.isEmpty() ? 0 : totalBookingRevenue / bookings.size());
        report.put("averageFoodOrderValue", foodOrders.isEmpty() ? 0 : totalFoodRevenue / foodOrders.size());

        return report;
    }

    /**
     * Generate occupancy report for a date range
     */
    public Map<String, Object> generateOccupancyReport(LocalDate startDate, LocalDate endDate) {
        List<Booking> bookings = bookingRepository.findAll().stream()
                .filter(b -> isBookingInDateRange(b, startDate, endDate))
                .collect(Collectors.toList());

        // Calculate daily occupancy
        List<Map<String, Object>> dailyOccupancy = new ArrayList<>();
        Map<String, Double> dailyRates = new HashMap<>();
        
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            final LocalDate currentDate = date; // capture effectively final for lambda
            long occupiedRooms = bookings.stream()
                    .filter(b -> isBookingActiveOnDate(b, currentDate))
                    .count();
            
            // Assuming 50 total rooms (you can make this dynamic)
            int totalRooms = 50;
            double occupancyRate = totalRooms > 0 ? (double) occupiedRooms / totalRooms * 100 : 0;
            
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            dayData.put("dateFormatted", date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
            dayData.put("occupiedRooms", occupiedRooms);
            dayData.put("totalRooms", totalRooms);
            dayData.put("occupancyRate", Math.round(occupancyRate * 100.0) / 100.0);
            
            dailyOccupancy.add(dayData);
            dailyRates.put(date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), occupancyRate);
        }

        double averageOccupancy = dailyRates.values().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        double peakOccupancy = dailyRates.values().stream()
                .mapToDouble(Double::doubleValue)
                .max()
                .orElse(0.0);

        Map<String, Object> report = new HashMap<>();
        report.put("startDate", startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        report.put("endDate", endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        report.put("dailyOccupancy", dailyOccupancy);
        report.put("averageOccupancy", Math.round(averageOccupancy * 100.0) / 100.0);
        report.put("peakOccupancy", Math.round(peakOccupancy * 100.0) / 100.0);
        report.put("totalDays", (int) startDate.until(endDate).getDays() + 1);

        return report;
    }

    /**
     * Generate customer report
     */
    public Map<String, Object> generateCustomerReport(LocalDate startDate, LocalDate endDate) {
        List<Booking> bookings = bookingRepository.findAll().stream()
                .filter(b -> isBookingInDateRange(b, startDate, endDate))
                .collect(Collectors.toList());

        List<User> customers = userRepository.findAll().stream()
                .filter(u -> "GUEST".equals(u.getRole()))
                .collect(Collectors.toList());

        // Customer statistics
        Map<String, Object> customerStats = new HashMap<>();
        customerStats.put("totalCustomers", customers.size());
        customerStats.put("activeCustomers", bookings.stream()
                .map(Booking::getUser)
                .distinct()
                .count());
        customerStats.put("newCustomers", customers.stream()
                .filter(u -> u.getId() != null) // Assuming new customers have recent IDs
                .count());

        // Top customers by spending
        Map<User, Double> customerSpending = bookings.stream()
                .filter(b -> "CONFIRMED".equals(b.getStatus()) || "CHECKED_IN".equals(b.getStatus()) || "CHECKED_OUT".equals(b.getStatus()))
                .collect(Collectors.groupingBy(
                    Booking::getUser,
                    Collectors.summingDouble(Booking::getTotalPrice)
                ));

        List<Map<String, Object>> topCustomers = customerSpending.entrySet().stream()
                .sorted(Map.Entry.<User, Double>comparingByValue().reversed())
                .limit(10)
                .map(entry -> {
                    Map<String, Object> customer = new HashMap<>();
                    customer.put("name", entry.getKey().getName());
                    customer.put("email", entry.getKey().getEmail());
                    customer.put("totalSpent", entry.getValue());
                    customer.put("bookingCount", bookings.stream()
                            .filter(b -> b.getUser().equals(entry.getKey()))
                            .count());
                    return customer;
                })
                .collect(Collectors.toList());

        Map<String, Object> report = new HashMap<>();
        report.put("startDate", startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        report.put("endDate", endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        report.put("customerStats", customerStats);
        report.put("topCustomers", topCustomers);

        return report;
    }

    /**
     * Generate food and beverage report
     */
    public Map<String, Object> generateFoodBeverageReport(LocalDate startDate, LocalDate endDate) {
        List<FoodOrder> orders = foodOrderRepository.findAll().stream()
                .filter(o -> isOrderInDateRange(o, startDate, endDate))
                .collect(Collectors.toList());

        double totalRevenue = orders.stream()
                .filter(o -> "COMPLETED".equals(o.getStatus()))
                .mapToDouble(FoodOrder::getTotalPrice)
                .sum();

        long totalOrders = orders.size();
        long completedOrders = orders.stream()
                .filter(o -> "COMPLETED".equals(o.getStatus()))
                .count();

        double averageOrderValue = completedOrders > 0 ? totalRevenue / completedOrders : 0;

        // Orders by status
        Map<String, Long> ordersByStatus = orders.stream()
                .collect(Collectors.groupingBy(
                    FoodOrder::getStatus,
                    Collectors.counting()
                ));

        // Daily order trends
        Map<String, Long> dailyOrders = orders.stream()
                .collect(Collectors.groupingBy(
                    o -> o.getOrderedAt().toLocalDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                    Collectors.counting()
                ));

        Map<String, Object> report = new HashMap<>();
        report.put("startDate", startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        report.put("endDate", endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        report.put("totalRevenue", totalRevenue);
        report.put("totalOrders", totalOrders);
        report.put("completedOrders", completedOrders);
        report.put("averageOrderValue", Math.round(averageOrderValue * 100.0) / 100.0);
        report.put("ordersByStatus", ordersByStatus);
        report.put("dailyOrders", dailyOrders);

        return report;
    }

    /**
     * Generate comprehensive hotel report
     */
    public Map<String, Object> generateComprehensiveReport(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> revenueReport = generateRevenueReport(startDate, endDate);
        Map<String, Object> occupancyReport = generateOccupancyReport(startDate, endDate);
        Map<String, Object> customerReport = generateCustomerReport(startDate, endDate);
        Map<String, Object> foodReport = generateFoodBeverageReport(startDate, endDate);

        Map<String, Object> comprehensiveReport = new HashMap<>();
        comprehensiveReport.put("reportPeriod", startDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) + 
                " - " + endDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
        comprehensiveReport.put("generatedAt", LocalDate.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")));
        comprehensiveReport.put("revenue", revenueReport);
        comprehensiveReport.put("occupancy", occupancyReport);
        comprehensiveReport.put("customers", customerReport);
        comprehensiveReport.put("foodBeverage", foodReport);

        return comprehensiveReport;
    }

    // Helper methods
    private boolean isBookingInDateRange(Booking booking, LocalDate startDate, LocalDate endDate) {
        return !booking.getCheckInDate().isAfter(endDate) && !booking.getCheckOutDate().isBefore(startDate);
    }

    private boolean isOrderInDateRange(FoodOrder order, LocalDate startDate, LocalDate endDate) {
        LocalDate orderDate = order.getOrderedAt().toLocalDate();
        return !orderDate.isBefore(startDate) && !orderDate.isAfter(endDate);
    }

    private boolean isBookingActiveOnDate(Booking booking, LocalDate date) {
        return ("CONFIRMED".equals(booking.getStatus()) || "CHECKED_IN".equals(booking.getStatus())) &&
                !booking.getCheckInDate().isAfter(date) &&
                !booking.getCheckOutDate().isBefore(date);
    }
}
