document.addEventListener('DOMContentLoaded', () => {

    // --- DOM Elements ---
    const createReportBtn = document.getElementById('create-report-btn');
    const reportModal = document.getElementById('report-modal');
    const viewReportModal = document.getElementById('view-report-modal');
    const entryModal = document.getElementById('entry-modal');
    const modals = document.querySelectorAll('.modal');
    const reportsTableBody = document.getElementById('reports-table-body');

    // Forms
    const reportForm = document.getElementById('report-form');
    const entryForm = document.getElementById('entry-form');

    // API Base URL
    const API_URL = '/api/reports';

    // --- Validation Helper Functions ---

    /**
     * Shows an error message for a specific form field.
     */
    const showError = (inputId, message) => {
        const input = document.getElementById(inputId);
        const error = document.getElementById(`${inputId}-error`);
        if (input) input.classList.add('invalid');
        if (error) {
            error.textContent = message;
            error.style.display = 'block';
        }
    };

    /**
     * Clears all validation errors from a form.
     */
    const clearErrors = (form) => {
        form.querySelectorAll('.invalid').forEach(el => el.classList.remove('invalid'));
        form.querySelectorAll('.error-message').forEach(el => {
            el.textContent = '';
            el.style.display = 'none';
        });
    };

    // --- Helper Functions ---

    /**
     * Formats a date string into a readable format.
     */
    const formatDate = (dateString) => {
        if (!dateString) return 'N/A';
        const date = new Date(dateString);
        return date.toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'short',
            day: 'numeric'
        });
    };

    /**
     * Formats a datetime string for input fields (YYYY-MM-DDTHH:mm).
     */
    const formatDateTimeForInput = (dateString) => {
        if (!dateString) return '';
        const date = new Date(dateString);
        return date.toISOString().slice(0, 16);
    };

    /**
     * Formats a number into currency format.
     */
    const formatCurrency = (amount) => {
        if (typeof amount !== 'number') return '$0.00';
        return amount.toLocaleString('en-US', { style: 'currency', currency: 'USD' });
    };

    /**
     * Shows a notification message.
     */
    const showNotification = (message, type = 'success') => {
        alert(message); // Replace with a better notification library if desired
    };

    /**
     * Shows loading state in the table.
     */
    const showLoadingState = () => {
        reportsTableBody.innerHTML = `
            <tr>
                <td colspan="6" class="text-center" style="padding: 3rem;">
                    <div class="loading-spinner">
                        <i class="fas fa-spinner fa-spin" style="font-size: 2rem; color: var(--primary-color);"></i>
                        <p style="margin-top: 1rem; color: var(--text-secondary);">Loading reports...</p>
                    </div>
                </td>
            </tr>`;
    };

    // --- Modal Management ---

    const openModal = (modal) => modal.style.display = 'block';
    const closeModal = (modal) => modal.style.display = 'none';

    // Close button handlers
    document.querySelectorAll('.close-btn, .close-btn-form').forEach(btn => {
        btn.addEventListener('click', (e) => {
            const modal = e.target.closest('.modal');
            if (modal) closeModal(modal);
        });
    });

    // Close modal on outside click
    window.addEventListener('click', (event) => {
        modals.forEach(modal => {
            if (event.target === modal) {
                closeModal(modal);
            }
        });
    });

    // --- API Fetch Functions ---

    /**
     * Fetches all reports and renders them.
     */
    const fetchAndRenderReports = async () => {
        console.log('Fetching reports from:', API_URL); // Debug log

        showLoadingState();

        try {
            const response = await fetch(API_URL);
            console.log('Response status:', response.status); // Debug log

            if (!response.ok) {
                throw new Error(`Failed to fetch reports: ${response.status} ${response.statusText}`);
            }

            const reports = await response.json();
            console.log('Fetched reports:', reports); // Debug log

            renderReportsTable(reports);
        } catch (error) {
            console.error('Error fetching reports:', error);
            reportsTableBody.innerHTML = `
                <tr>
                    <td colspan="6" class="text-center">
                        <div class="empty-state">
                            <div class="empty-state-icon">❌</div>
                            <p style="color: var(--danger-color); font-weight: 600;">Error loading reports</p>
                            <p style="color: var(--text-secondary); font-size: 0.9rem;">${error.message}</p>
                            <button class="btn primary" onclick="location.reload()" style="margin-top: 1rem;">
                                <i class="fas fa-sync"></i> Retry
                            </button>
                        </div>
                    </td>
                </tr>`;
        }
    };

    // --- Rendering Functions ---

    /**
     * Renders the reports table.
     */
    const renderReportsTable = (reports) => {
        console.log('Rendering reports table with', reports.length, 'reports'); // Debug log

        reportsTableBody.innerHTML = '';

        if (!reports || reports.length === 0) {
            reportsTableBody.innerHTML = `
                <tr>
                    <td colspan="6" class="text-center">
                        <div class="empty-state">
                            <div class="empty-state-icon">📄</div>
                            <p style="font-weight: 600; color: var(--text-color);">No financial reports found</p>
                            <p style="color: var(--text-secondary); font-size: 0.9rem;">Create your first report to get started.</p>
                            <button class="btn primary" id="empty-create-btn" style="margin-top: 1rem;">
                                <i class="fas fa-plus-circle"></i> Create First Report
                            </button>
                        </div>
                    </td>
                </tr>`;

            // Add event listener for the empty state button
            const emptyCreateBtn = document.getElementById('empty-create-btn');
            if (emptyCreateBtn) {
                emptyCreateBtn.addEventListener('click', () => {
                    reportForm.reset();
                    clearErrors(reportForm);
                    document.getElementById('report-modal-title').textContent = 'Create New Report';
                    document.getElementById('report-id').value = '';
                    document.getElementById('report-type').disabled = false;
                    openModal(reportModal);
                });
            }
            return;
        }

        reports.forEach(report => {
            const row = document.createElement('tr');
            const reportTypeIcon =
                report.reportType === 'REVENUE' ? '📈' :
                    report.reportType === 'EXPENSE' ? '📉' : '💰';

            row.innerHTML = `
                <td>
                    <strong>${report.reportName || 'Unnamed Report'}</strong>
                    <br>
                    <small style="color: var(--text-secondary);">By ${report.generatedBy || 'Unknown'}</small>
                </td>
                <td>
                    ${reportTypeIcon} ${(report.reportType || 'UNKNOWN').replace('_', ' ')}
                </td>
                <td>
                    <span class="status ${(report.status || 'DRAFT').toLowerCase()}">${report.status || 'DRAFT'}</span>
                </td>
                <td>
                    ${formatDate(report.startDate)}<br>
                    <small style="color: var(--text-secondary);">to ${formatDate(report.endDate)}</small>
                </td>
                <td><strong>${formatCurrency(report.totalAmount || 0)}</strong></td>
                <td>
                    <button class="btn-action view" data-id="${report.id}" title="View Details">
                        <i class="fas fa-eye"></i>
                    </button>
                    <button class="btn-action edit" data-id="${report.id}"
                        ${report.status === 'FINALIZED' ? 'disabled' : ''} title="Edit Report">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button class="btn-action delete" data-id="${report.id}"
                        ${report.status === 'FINALIZED' ? 'disabled' : ''} title="Delete Report">
                        <i class="fas fa-trash"></i>
                    </button>
                </td>
            `;
            reportsTableBody.appendChild(row);
        });

        console.log('Table rendered successfully'); // Debug log
    };

    /**
     * Renders detailed view of a report.
     */
    const renderReportDetails = (details) => {
        const { report, revenueEntries, expenseEntries, totalRevenue, totalExpense } = details;
        const contentDiv = document.getElementById('report-details-content');

        const reportTypeIcon =
            report.reportType === 'REVENUE' ? '📈' :
                report.reportType === 'EXPENSE' ? '📉' : '💰';

        let revenueHtml = '';
        if (report.reportType === 'REVENUE' || report.reportType === 'PROFIT_LOSS') {
            revenueHtml = `
                <div class="detail-section">
                    <div class="detail-header">
                        <h4><i class="fas fa-arrow-up" style="color: var(--success-color);"></i> Revenue Entries</h4>
                        <button class="btn-add-entry" data-report-id="${report.id}" data-type="revenue"
                            ${report.status === 'FINALIZED' ? 'disabled' : ''}>
                            <i class="fas fa-plus"></i> Add Revenue
                        </button>
                    </div>
                    ${(revenueEntries && revenueEntries.length > 0) ? `
                        <table>
                            <thead>
                                <tr>
                                    <th>Source</th>
                                    <th>Description</th>
                                    <th>Date</th>
                                    <th>Amount</th>
                                    <th>Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                ${revenueEntries.map(entry => `
                                    <tr>
                                        <td><strong>${entry.source}</strong></td>
                                        <td>${entry.description}</td>
                                        <td>${formatDate(entry.entryDate)}</td>
                                        <td><strong style="color: var(--success-color);">${formatCurrency(entry.amount)}</strong></td>
                                        <td>
                                            <button class="btn-action edit-entry" data-id="${entry.id}" data-type="revenue"
                                                ${report.status === 'FINALIZED' ? 'disabled' : ''}>
                                                <i class="fas fa-edit"></i>
                                            </button>
                                            <button class="btn-action delete-entry" data-id="${entry.id}" data-type="revenue"
                                                ${report.status === 'FINALIZED' ? 'disabled' : ''}>
                                                <i class="fas fa-trash"></i>
                                            </button>
                                        </td>
                                    </tr>
                                `).join('')}
                            </tbody>
                        </table>
                    ` : '<p style="color: var(--text-secondary); text-align: center; padding: 2rem;">No revenue entries yet.</p>'}
                    ${report.reportType === 'PROFIT_LOSS' ? `
                        <div class="total">
                            Total Revenue: <strong style="color: var(--success-color);">${formatCurrency(totalRevenue || 0)}</strong>
                        </div>
                    ` : ''}
                </div>`;
        }

        let expenseHtml = '';
        if (report.reportType === 'EXPENSE' || report.reportType === 'PROFIT_LOSS') {
            expenseHtml = `
                <div class="detail-section">
                    <div class="detail-header">
                        <h4><i class="fas fa-arrow-down" style="color: var(--danger-color);"></i> Expense Entries</h4>
                        <button class="btn-add-entry" data-report-id="${report.id}" data-type="expense"
                            ${report.status === 'FINALIZED' ? 'disabled' : ''}>
                            <i class="fas fa-plus"></i> Add Expense
                        </button>
                    </div>
                    ${(expenseEntries && expenseEntries.length > 0) ? `
                        <table>
                            <thead>
                                <tr>
                                    <th>Category</th>
                                    <th>Description</th>
                                    <th>Date</th>
                                    <th>Amount</th>
                                    <th>Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                ${expenseEntries.map(entry => `
                                    <tr>
                                        <td><strong>${entry.category}</strong></td>
                                        <td>${entry.description}</td>
                                        <td>${formatDate(entry.entryDate)}</td>
                                        <td><strong style="color: var(--danger-color);">${formatCurrency(entry.amount)}</strong></td>
                                        <td>
                                            <button class="btn-action edit-entry" data-id="${entry.id}" data-type="expense"
                                                ${report.status === 'FINALIZED' ? 'disabled' : ''}>
                                                <i class="fas fa-edit"></i>
                                            </button>
                                            <button class="btn-action delete-entry" data-id="${entry.id}" data-type="expense"
                                                ${report.status === 'FINALIZED' ? 'disabled' : ''}>
                                                <i class="fas fa-trash"></i>
                                            </button>
                                        </td>
                                    </tr>
                                `).join('')}
                            </tbody>
                        </table>
                    ` : '<p style="color: var(--text-secondary); text-align: center; padding: 2rem;">No expense entries yet.</p>'}
                    ${report.reportType === 'PROFIT_LOSS' ? `
                        <div class="total">
                            Total Expense: <strong style="color: var(--danger-color);">${formatCurrency(totalExpense || 0)}</strong>
                        </div>
                    ` : ''}
                </div>`;
        }

        contentDiv.innerHTML = `
            <div class="report-header">
                <h2>${reportTypeIcon} ${report.reportName}</h2>
                <div class="report-meta">
                    <div class="report-meta-item">
                        <strong>Report Type:</strong>
                        <span>${report.reportType.replace('_', ' ')}</span>
                    </div>
                    <div class="report-meta-item">
                        <strong>Status:</strong>
                        <span class="status ${report.status.toLowerCase()}">${report.status}</span>
                    </div>
                    <div class="report-meta-item">
                        <strong>Date Range:</strong>
                        <span>${formatDate(report.startDate)} to ${formatDate(report.endDate)}</span>
                    </div>
                    <div class="report-meta-item">
                        <strong>Generated By:</strong>
                        <span>${report.generatedBy}</span>
                    </div>
                </div>
                ${report.description ? `<p style="margin-top: 1rem; opacity: 0.9;">${report.description}</p>` : ''}
            </div>

            <div class="final-total">
                <i class="fas fa-chart-line"></i>
                Report Total: ${formatCurrency(report.totalAmount)}
            </div>

            ${revenueHtml}
            ${expenseHtml}

            <div class="report-actions">
                ${report.status !== 'FINALIZED' ? `
                    <button class="btn-finalize" data-id="${report.id}">
                        <i class="fas fa-lock"></i> Finalize Report
                    </button>
                ` : '<p style="color: var(--text-secondary);"><i class="fas fa-check-circle"></i> This report has been finalized and cannot be modified.</p>'}
            </div>
        `;
    };

    // --- Form Handling ---

    /**
     * Report form submission with validation.
     */
    reportForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        clearErrors(reportForm);

        // --- Validation Logic ---
        let isValid = true;
        const reportName = document.getElementById('report-name').value.trim();
        const reportType = document.getElementById('report-type').value;
        const startDateValue = document.getElementById('report-start-date').value;
        const endDateValue = document.getElementById('report-end-date').value;

        if (!reportName) {
            showError('report-name', 'Report name is required.');
            isValid = false;
        }
        if (!reportType) {
            showError('report-type', 'Please select a report type.');
            isValid = false;
        }
        if (!startDateValue) {
            showError('report-start-date', 'Start date is required.');
            isValid = false;
        }
        if (!endDateValue) {
            showError('report-end-date', 'End date is required.');
            isValid = false;
        }

        if (startDateValue && endDateValue) {
            const startDate = new Date(startDateValue);
            const endDate = new Date(endDateValue);
            if (endDate <= startDate) {
                showError('report-end-date', 'End date must be after the start date.');
                isValid = false;
            }
        }

        if (!isValid) return; // Stop submission if validation fails
        // --- End Validation Logic ---


        const id = document.getElementById('report-id').value;
        const formData = new URLSearchParams({
            reportName: reportName,
            reportType: reportType,
            description: document.getElementById('report-description').value.trim(),
            startDate: new Date(startDateValue).toISOString(),
            endDate: new Date(endDateValue).toISOString(),
        });

        const url = id ? `${API_URL}/${id}` : `${API_URL}/create`;
        const method = id ? 'PUT' : 'POST';

        try {
            const response = await fetch(url, {
                method: method,
                body: formData
            });

            if (!response.ok) {
                const error = await response.json();
                throw new Error(error.error || 'Failed to save report');
            }

            closeModal(reportModal);
            await fetchAndRenderReports(); // Await the fetch
            showNotification(id ? 'Report updated successfully!' : 'Report created successfully!');
        } catch (error) {
            console.error('Error saving report:', error);
            showNotification(`Error: ${error.message}`, 'error');
        }
    });

    /**
     * Entry form submission with validation.
     */
    entryForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        clearErrors(entryForm);

        // --- Validation Logic ---
        let isValid = true;
        const categorySource = document.getElementById('entry-category-source').value.trim();
        const description = document.getElementById('entry-description').value.trim();
        const amount = document.getElementById('entry-amount').value;
        const entryDate = document.getElementById('entry-date').value;

        if (!categorySource) {
            showError('entry-category-source', 'Category/Source is required.');
            isValid = false;
        }
        if (!description) {
            showError('entry-description', 'Description is required.');
            isValid = false;
        }
        if (!amount || parseFloat(amount) <= 0) {
            showError('entry-amount', 'Amount must be a positive number.');
            isValid = false;
        }
        if (!entryDate) {
            showError('entry-date', 'Entry date is required.');
            isValid = false;
        }

        if (!isValid) return; // Stop submission if validation fails
        // --- End Validation Logic ---

        const entryId = document.getElementById('entry-id').value;
        const reportId = document.getElementById('entry-report-id').value;
        const type = document.getElementById('entry-type').value;

        const formData = new URLSearchParams();
        if (type === 'revenue') {
            formData.append('source', categorySource);
        } else {
            formData.append('category', categorySource);
        }
        formData.append('description', description);
        formData.append('amount', amount);
        formData.append('entryDate', entryDate);

        let url, method;
        if (entryId) {
            url = `${API_URL}/${type}-entry/${entryId}`;
            method = 'PUT';
        } else {
            url = `${API_URL}/${reportId}/${type}-entry`;
            method = 'POST';
        }

        try {
            const response = await fetch(url, { method: method, body: formData });

            if (!response.ok) {
                const error = await response.json();
                throw new Error(error.error || 'Failed to save entry');
            }

            closeModal(entryModal);

            // Refresh report details
            const detailsResponse = await fetch(`${API_URL}/${reportId}`);
            const details = await detailsResponse.json();
            renderReportDetails(details);

            await fetchAndRenderReports(); // Await the fetch
            showNotification(entryId ? 'Entry updated successfully!' : 'Entry added successfully!');
        } catch (error) {
            console.error('Error saving entry:', error);
            showNotification(`Error: ${error.message}`, 'error');
        }
    });

    // --- Event Delegation ---

    document.body.addEventListener('click', async (e) => {
        const target = e.target.closest('button');
        if (!target) return;

        // View Report
        if (target.classList.contains('view')) {
            const reportId = target.dataset.id;
            try {
                const response = await fetch(`${API_URL}/${reportId}`);
                const details = await response.json();
                renderReportDetails(details);
                openModal(viewReportModal);
            } catch (error) {
                console.error('Error fetching report details:', error);
                showNotification('Could not load report details.', 'error');
            }
        }

        // Edit Report
        if (target.classList.contains('edit')) {
            const reportId = target.dataset.id;
            try {
                const response = await fetch(`${API_URL}/${reportId}/raw`);
                const report = await response.json();

                reportForm.reset();
                clearErrors(reportForm);
                document.getElementById('report-modal-title').textContent = 'Edit Report';
                document.getElementById('report-id').value = report.id;
                document.getElementById('report-name').value = report.reportName;
                document.getElementById('report-type').value = report.reportType;
                document.getElementById('report-description').value = report.description || '';
                document.getElementById('report-start-date').value = formatDateTimeForInput(report.startDate);
                document.getElementById('report-end-date').value = formatDateTimeForInput(report.endDate);
                document.getElementById('report-type').disabled = true;

                openModal(reportModal);
            } catch (error) {
                console.error('Error fetching report for editing:', error);
                showNotification('Could not load report data for editing.', 'error');
            }
        }

        // Delete Report
        if (target.classList.contains('delete')) {
            const reportId = target.dataset.id;
            if (confirm('Are you sure you want to delete this report? This action cannot be undone.')) {
                try {
                    await fetch(`${API_URL}/${reportId}`, { method: 'DELETE' });
                    await fetchAndRenderReports(); // Await the fetch
                    showNotification('Report deleted successfully.');
                } catch (error) {
                    console.error('Error deleting report:', error);
                    showNotification('Could not delete report.', 'error');
                }
            }
        }

        // Finalize Report
        if (target.classList.contains('btn-finalize')) {
            const reportId = target.dataset.id;
            if (confirm('Are you sure you want to finalize this report? No further changes will be possible.')) {
                try {
                    const response = await fetch(`${API_URL}/${reportId}/finalize`, { method: 'PUT' });
                    if (!response.ok) throw new Error('Failed to finalize');

                    closeModal(viewReportModal);
                    await fetchAndRenderReports(); // Await the fetch
                    showNotification('Report finalized successfully.');
                } catch (error) {
                    console.error('Error finalizing report:', error);
                    showNotification('Could not finalize report.', 'error');
                }
            }
        }

        // Add Entry
        if (target.classList.contains('btn-add-entry')) {
            const reportId = target.dataset.reportId;
            const type = target.dataset.type;

            entryForm.reset();
            clearErrors(entryForm);
            document.getElementById('entry-modal-title').textContent =
                `Add New ${type.charAt(0).toUpperCase() + type.slice(1)} Entry`;
            document.getElementById('entry-id').value = '';
            document.getElementById('entry-report-id').value = reportId;
            document.getElementById('entry-type').value = type;
            document.getElementById('entry-category-source-label').innerHTML =
                type === 'revenue' ? '<i class="fas fa-tag"></i> Source' : '<i class="fas fa-tag"></i> Category';

            openModal(entryModal);
        }

        // Edit Entry
        if (target.classList.contains('edit-entry')) {
            const entryId = target.dataset.id;
            const type = target.dataset.type;

            try {
                const response = await fetch(`${API_URL}/${type}-entry/${entryId}`);
                const entry = await response.json();

                entryForm.reset();
                clearErrors(entryForm);
                document.getElementById('entry-modal-title').textContent =
                    `Edit ${type.charAt(0).toUpperCase() + type.slice(1)} Entry`;
                document.getElementById('entry-id').value = entry.id;
                document.getElementById('entry-report-id').value = entry.report.id;
                document.getElementById('entry-type').value = type;
                document.getElementById('entry-category-source-label').innerHTML =
                    type === 'revenue' ? '<i class="fas fa-tag"></i> Source' : '<i class="fas fa-tag"></i> Category';
                document.getElementById('entry-category-source').value =
                    type === 'revenue' ? entry.source : entry.category;
                document.getElementById('entry-description').value = entry.description;
                document.getElementById('entry-amount').value = entry.amount;
                document.getElementById('entry-date').value = entry.entryDate;

                openModal(entryModal);
            } catch (error) {
                console.error('Error fetching entry for editing:', error);
                showNotification('Could not load entry data.', 'error');
            }
        }

        // Delete Entry
        if (target.classList.contains('delete-entry')) {
            const entryId = target.dataset.id;
            const type = target.dataset.type;
            const reportId = document.querySelector('.btn-add-entry').dataset.reportId;

            if (confirm('Are you sure you want to delete this entry?')) {
                try {
                    await fetch(`${API_URL}/${type}-entry/${entryId}`, { method: 'DELETE' });

                    // Refresh details
                    const detailsResponse = await fetch(`${API_URL}/${reportId}`);
                    const details = await detailsResponse.json();
                    renderReportDetails(details);

                    await fetchAndRenderReports(); // Await the fetch
                    showNotification('Entry deleted successfully.');
                } catch (error) {
                    console.error('Error deleting entry:', error);
                    showNotification('Could not delete entry.', 'error');
                }
            }
        }
    });

    // --- Initial Setup ---

    createReportBtn.addEventListener('click', () => {
        reportForm.reset();
        clearErrors(reportForm);
        document.getElementById('report-modal-title').textContent = 'Create New Report';
        document.getElementById('report-id').value = '';
        document.getElementById('report-type').disabled = false;
        openModal(reportModal);
    });

    // --- IMPORTANT: Load reports on page load ---
    console.log('Page loaded, fetching reports...'); // Debug log
    fetchAndRenderReports();
});