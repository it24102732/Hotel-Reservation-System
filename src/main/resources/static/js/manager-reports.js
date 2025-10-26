/**
 * Manager Reports JavaScript
 * Handles report generation, data loading, and export functionality
 */

class ManagerReports {
    constructor() {
        this.currentReport = null;
        this.revenueChart = null;
        this.distributionChart = null;
        this.init();
    }

    init() {
        this.setupEventListeners();
        this.initializeDefaultDates();
        this.initializeCharts();
    }

    setupEventListeners() {
        // Generate report button
        const generateBtn = document.querySelector('[onclick="generateReport()"]');
        if (generateBtn) {
            generateBtn.addEventListener('click', () => this.generateReport());
        }

        // Export buttons
        document.querySelectorAll('[onclick*="exportReport"]').forEach(btn => {
            btn.addEventListener('click', (e) => {
                const format = e.target.onclick.toString().match(/exportReport\('(\w+)'\)/)[1];
                this.exportReport(format);
            });
        });

        // Report type change
        document.getElementById('reportType').addEventListener('change', () => {
            this.updateReportFilters();
        });
    }

    initializeDefaultDates() {
        const today = new Date();
        const lastMonth = new Date(today.getFullYear(), today.getMonth() - 1, today.getDate());
        
        document.getElementById('startDate').value = lastMonth.toISOString().split('T')[0];
        document.getElementById('endDate').value = today.toISOString().split('T')[0];
    }

    initializeCharts() {
        // Initialize with default data
        this.updateRevenueChart();
        this.updateDistributionChart();
    }

    async generateReport() {
        const startDate = document.getElementById('startDate').value;
        const endDate = document.getElementById('endDate').value;
        const reportType = document.getElementById('reportType').value;

        if (!startDate || !endDate) {
            this.showNotification('Please select both start and end dates', 'warning');
            return;
        }

        if (new Date(startDate) > new Date(endDate)) {
            this.showNotification('Start date cannot be after end date', 'warning');
            return;
        }

        this.showLoadingState();

        try {
            const reportData = await this.fetchReportData(reportType, startDate, endDate);
            this.currentReport = { type: reportType, data: reportData, startDate, endDate };
            this.displayReport(reportData, reportType);
            this.hideLoadingState();
            this.showNotification('Report generated successfully!', 'success');
        } catch (error) {
            console.error('Error generating report:', error);
            this.showNotification('Error generating report: ' + error.message, 'error');
            this.hideLoadingState();
        }
    }

    async fetchReportData(reportType, startDate, endDate) {
        const endpoints = {
            'revenue': '/manager/api/reports/revenue',
            'occupancy': '/manager/api/reports/occupancy',
            'customers': '/manager/api/reports/customers',
            'food-beverage': '/manager/api/reports/food-beverage',
            'comprehensive': '/manager/api/reports/comprehensive'
        };

        const endpoint = endpoints[reportType];
        if (!endpoint) {
            throw new Error('Invalid report type');
        }

        const response = await fetch(`${endpoint}?startDate=${encodeURIComponent(startDate)}&endDate=${encodeURIComponent(endDate)}`, {
            headers: { 'Accept': 'application/json' }
        });

        const contentType = response.headers.get('content-type') || '';
        if (!response.ok) {
            if (contentType.includes('application/json')) {
                const errorData = await response.json();
                throw new Error(errorData.error || 'Failed to fetch report data');
            } else {
                const text = await response.text();
                throw new Error(text || 'Failed to fetch report data');
            }
        }

        if (contentType.includes('application/json')) {
            return await response.json();
        } else {
            const text = await response.text();
            // Handle unexpected HTML responses gracefully
            throw new Error('Unexpected response format');
        }
    }

    displayReport(data, reportType) {
        const reportResults = document.getElementById('reportResults');
        const reportContent = document.getElementById('reportContent');
        
        reportResults.style.display = 'block';
        
        let html = '';
        
        switch (reportType) {
            case 'revenue':
                html = this.generateRevenueReportHTML(data);
                break;
            case 'occupancy':
                html = this.generateOccupancyReportHTML(data);
                break;
            case 'customers':
                html = this.generateCustomerReportHTML(data);
                break;
            case 'food-beverage':
                html = this.generateFoodBeverageReportHTML(data);
                break;
            case 'comprehensive':
                html = this.generateComprehensiveReportHTML(data);
                break;
            default:
                html = '<p>Invalid report type</p>';
        }
        
        reportContent.innerHTML = html;
        
        // Track in generated list
        this.addGeneratedReportToList(reportType, data.startDate || this.currentReport.startDate, data.endDate || this.currentReport.endDate);

        // Scroll to results
        reportResults.scrollIntoView({ behavior: 'smooth' });
    }

    generateRevenueReportHTML(data) {
        return `
            <div class="row">
                <div class="col-md-6">
                    <h6>Revenue Summary</h6>
                    <table class="table table-sm">
                        <tr><td>Total Revenue:</td><td><strong>$${data.totalRevenue.toFixed(2)}</strong></td></tr>
                        <tr><td>Booking Revenue:</td><td>$${data.totalBookingRevenue.toFixed(2)}</td></tr>
                        <tr><td>Food Revenue:</td><td>$${data.totalFoodRevenue.toFixed(2)}</td></tr>
                        <tr><td>Total Bookings:</td><td>${data.totalBookings}</td></tr>
                        <tr><td>Total Food Orders:</td><td>${data.totalFoodOrders}</td></tr>
                    </table>
                </div>
                <div class="col-md-6">
                    <h6>Performance Metrics</h6>
                    <table class="table table-sm">
                        <tr><td>Average Booking Value:</td><td>$${data.averageBookingValue.toFixed(2)}</td></tr>
                        <tr><td>Average Food Order Value:</td><td>$${data.averageFoodOrderValue.toFixed(2)}</td></tr>
                        <tr><td>Report Period:</td><td>${data.startDate} to ${data.endDate}</td></tr>
                    </table>
                </div>
            </div>
        `;
    }

    generateOccupancyReportHTML(data) {
        const dailyOccupancy = data.dailyOccupancy || [];
        let tableRows = '';
        
        dailyOccupancy.forEach(day => {
            tableRows += `
                <tr>
                    <td>${day.dateFormatted}</td>
                    <td>${day.occupiedRooms}</td>
                    <td>${day.totalRooms}</td>
                    <td><span class="badge bg-primary">${day.occupancyRate}%</span></td>
                </tr>
            `;
        });

        return `
            <div class="row">
                <div class="col-md-4">
                    <h6>Occupancy Summary</h6>
                    <table class="table table-sm">
                        <tr><td>Average Occupancy:</td><td><strong>${data.averageOccupancy}%</strong></td></tr>
                        <tr><td>Peak Occupancy:</td><td>${data.peakOccupancy}%</td></tr>
                        <tr><td>Total Days:</td><td>${data.totalDays}</td></tr>
                    </table>
                </div>
                <div class="col-md-8">
                    <h6>Daily Occupancy Details</h6>
                    <div class="table-responsive">
                        <table class="table table-sm table-striped">
                            <thead>
                                <tr>
                                    <th>Date</th>
                                    <th>Occupied</th>
                                    <th>Total</th>
                                    <th>Rate</th>
                                </tr>
                            </thead>
                            <tbody>
                                ${tableRows}
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        `;
    }

    generateCustomerReportHTML(data) {
        const customerStats = data.customerStats || {};
        const topCustomers = data.topCustomers || [];
        
        let customerRows = '';
        topCustomers.forEach(customer => {
            customerRows += `
                <tr>
                    <td>${customer.name}</td>
                    <td>${customer.email}</td>
                    <td>$${customer.totalSpent.toFixed(2)}</td>
                    <td>${customer.bookingCount}</td>
                </tr>
            `;
        });

        return `
            <div class="row">
                <div class="col-md-4">
                    <h6>Customer Statistics</h6>
                    <table class="table table-sm">
                        <tr><td>Total Customers:</td><td><strong>${customerStats.totalCustomers}</strong></td></tr>
                        <tr><td>Active Customers:</td><td>${customerStats.activeCustomers}</td></tr>
                        <tr><td>New Customers:</td><td>${customerStats.newCustomers}</td></tr>
                    </table>
                </div>
                <div class="col-md-8">
                    <h6>Top Customers by Spending</h6>
                    <div class="table-responsive">
                        <table class="table table-sm table-striped">
                            <thead>
                                <tr>
                                    <th>Name</th>
                                    <th>Email</th>
                                    <th>Total Spent</th>
                                    <th>Bookings</th>
                                </tr>
                            </thead>
                            <tbody>
                                ${customerRows}
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        `;
    }

    generateFoodBeverageReportHTML(data) {
        return `
            <div class="row">
                <div class="col-md-6">
                    <h6>Food & Beverage Summary</h6>
                    <table class="table table-sm">
                        <tr><td>Total Revenue:</td><td><strong>$${data.totalRevenue.toFixed(2)}</strong></td></tr>
                        <tr><td>Total Orders:</td><td>${data.totalOrders}</td></tr>
                        <tr><td>Completed Orders:</td><td>${data.completedOrders}</td></tr>
                        <tr><td>Average Order Value:</td><td>$${data.averageOrderValue.toFixed(2)}</td></tr>
                    </table>
                </div>
                <div class="col-md-6">
                    <h6>Order Status Breakdown</h6>
                    <table class="table table-sm">
                        ${Object.entries(data.ordersByStatus || {}).map(([status, count]) => 
                            `<tr><td>${status}:</td><td>${count}</td></tr>`
                        ).join('')}
                    </table>
                </div>
            </div>
        `;
    }

    generateComprehensiveReportHTML(data) {
        return `
            <div class="alert alert-info">
                <h6><i class="fas fa-info-circle me-2"></i>Comprehensive Report</h6>
                <p>Report Period: ${data.reportPeriod}</p>
                <p>Generated: ${data.generatedAt}</p>
            </div>
            <div class="row">
                <div class="col-md-6">
                    <h6>Revenue Summary</h6>
                    <p>Total Revenue: <strong>$${data.revenue.totalRevenue.toFixed(2)}</strong></p>
                    <p>Booking Revenue: $${data.revenue.totalBookingRevenue.toFixed(2)}</p>
                    <p>Food Revenue: $${data.revenue.totalFoodRevenue.toFixed(2)}</p>
                </div>
                <div class="col-md-6">
                    <h6>Occupancy Summary</h6>
                    <p>Average Occupancy: <strong>${data.occupancy.averageOccupancy}%</strong></p>
                    <p>Peak Occupancy: ${data.occupancy.peakOccupancy}%</p>
                    <p>Total Days: ${data.occupancy.totalDays}</p>
                </div>
            </div>
        `;
    }

    updateRevenueChart() {
        const ctx = document.getElementById('revenueChart').getContext('2d');
        
        if (this.revenueChart) {
            this.revenueChart.destroy();
        }

        this.revenueChart = new Chart(ctx, {
            type: 'line',
            data: {
                labels: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun'],
                datasets: [{
                    label: 'Revenue',
                    data: [12000, 19000, 15000, 25000, 22000, 30000],
                    borderColor: '#3498db',
                    backgroundColor: 'rgba(52, 152, 219, 0.1)',
                    tension: 0.4
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        display: false
                    }
                }
            }
        });
    }

    updateDistributionChart() {
        const ctx = document.getElementById('revenueDistributionChart').getContext('2d');
        
        if (this.distributionChart) {
            this.distributionChart.destroy();
        }

        this.distributionChart = new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels: ['Room Revenue', 'Food & Beverage', 'Services', 'Other'],
                datasets: [{
                    data: [60, 25, 10, 5],
                    backgroundColor: [
                        '#3498db',
                        '#e74c3c',
                        '#f39c12',
                        '#27ae60'
                    ]
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'bottom'
                    }
                }
            }
        });
    }

    updateReportFilters() {
        const reportType = document.getElementById('reportType').value;
        // You can add specific filter logic based on report type
        console.log('Report type changed to:', reportType);
    }

    async exportReport(format) {
        if (!this.currentReport) {
            this.showNotification('Please generate a report first', 'warning');
            return;
        }

        try {
            if (format === 'pdf') {
                await this.exportAsPDF();
            } else {
                this.showNotification('Unsupported export format', 'warning');
            }
        } catch (error) {
            console.error('Export error:', error);
            this.showNotification('Error exporting report: ' + error.message, 'error');
        }
    }

    async exportAsPDF() {
        const { type, startDate, endDate } = this.currentReport;

        const response = await fetch(`/manager/api/reports/export/pdf?reportType=${encodeURIComponent(type)}&startDate=${encodeURIComponent(startDate)}&endDate=${encodeURIComponent(endDate)}`);

        if (!response.ok) {
            const text = await response.text();
            throw new Error(text || 'Failed to export PDF');
        }

        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `${type}_report_${startDate}_to_${endDate}.pdf`;
        a.click();
        window.URL.revokeObjectURL(url);

        this.showNotification('PDF report exported successfully!', 'success');
    }

    addGeneratedReportToList(type, start, end) {
        const tbody = document.getElementById('generatedReportsBody');
        const tr = document.createElement('tr');
        const generatedAt = new Date().toLocaleString();
        tr.innerHTML = `
            <td>${type}</td>
            <td>${start} to ${end}</td>
            <td>${generatedAt}</td>
            <td>
                <button class="btn btn-sm btn-outline-danger" onclick="exportReport('pdf')">
                    <i class="fas fa-file-pdf"></i>
                </button>
            </td>
        `;
        tbody.prepend(tr);
    }

    showLoadingState() {
        const btn = document.querySelector('[onclick="generateReport()"]');
        if (btn) {
            btn.disabled = true;
            btn.innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i>Generating...';
        }
    }

    hideLoadingState() {
        const btn = document.querySelector('[onclick="generateReport()"]');
        if (btn) {
            btn.disabled = false;
            btn.innerHTML = '<i class="fas fa-chart-line me-2"></i>Generate Report';
        }
    }

    showNotification(message, type = 'info') {
        const notification = document.createElement('div');
        notification.className = `alert alert-${type} alert-dismissible fade show position-fixed`;
        notification.style.cssText = 'top: 20px; right: 20px; z-index: 9999; min-width: 300px;';
        notification.innerHTML = `
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        `;

        document.body.appendChild(notification);

        setTimeout(() => {
            if (notification.parentNode) {
                notification.remove();
            }
        }, 5000);
    }
}

// Global functions for backward compatibility
function generateReport() {
    if (window.managerReports) {
        window.managerReports.generateReport();
    }
}

function exportReport(format) {
    if (window.managerReports) {
        window.managerReports.exportReport(format);
    }
}

function initializeReportsPage() {
    window.managerReports = new ManagerReports();
}

// Export for potential module usage
if (typeof module !== 'undefined' && module.exports) {
    module.exports = ManagerReports;
}
