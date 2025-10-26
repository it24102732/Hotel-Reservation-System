/**
 * Restaurant Management Dashboard JavaScript
 * FIX: Replaced all simulated API calls with live fetch() calls to the Spring Boot backend.
 * FIX: Implemented real image uploading functionality.
 */
(function() {
    'use strict';

    const API_BASE_URL = ''; // Keep empty for relative paths

    // DOM Element References
    const DOM = {
        ordersTableBody: document.getElementById('ordersTableBody'),
        orderFilterDropdown: document.getElementById('orderFilterDropdown'),
        menuTableBody: document.getElementById('menuTableBody'),
        addMenuItemBtn: document.getElementById('addMenuItemBtn'),
        menuItemForm: document.getElementById('menuItemForm'),
        menuItemModal: new bootstrap.Modal(document.getElementById('menuItemModal')),
        modalTitle: document.getElementById('modalTitle'),
        menuItemId: document.getElementById('menuItemId'),
        imageFile: document.getElementById('imageFile'),
        imagePreview: document.getElementById('imagePreview'),
        imagePreviewContainer: document.getElementById('imagePreviewContainer'),
        noImagePlaceholder: document.getElementById('noImagePlaceholder'),
        imageUrl: document.getElementById('imageUrl'),
        saveMenuItem: document.getElementById('saveMenuItem'),
        pendingOrdersCount: document.getElementById('pendingOrdersCount'),
        completedOrdersCount: document.getElementById('completedOrdersCount'),
        menuItemsCount: document.getElementById('menuItemsCount'),
        todaysRevenue: document.getElementById('todaysRevenue'),
    };

    // State Management
    const STATE = {
        allOrders: [],
        allMenuItems: [],
    };

    /**
     * Shows a toast notification.
     */
    function showToast(title, message, type = 'info') {
        const toastContainer = document.querySelector('.toast-container') || createToastContainer();
        const toastId = `toast-${Date.now()}`;
        const toastEl = document.createElement('div');
        toastEl.className = `toast align-items-center text-white bg-${type} border-0`;
        toastEl.id = toastId;
        toastEl.innerHTML = `
            <div class="d-flex">
                <div class="toast-body">${message}</div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
            </div>`;
        toastContainer.appendChild(toastEl);
        const toast = new bootstrap.Toast(toastEl, { delay: 5000 });
        toast.show();
        toastEl.addEventListener('hidden.bs.toast', () => toastEl.remove());
    }

    function createToastContainer() {
        const container = document.createElement('div');
        container.className = 'toast-container position-fixed top-0 end-0 p-3';
        container.style.zIndex = '1060';
        document.body.appendChild(container);
        return container;
    }

    /**
     * Fetches data from the API with error handling.
     */
    async function apiFetch(url, options = {}) {
        try {
            const response = await fetch(API_BASE_URL + url, options);
            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(`Network response was not ok: ${response.status} ${errorText}`);
            }
            // Handle cases with no response body (e.g., 204 No Content)
            if (response.status === 204) {
                return null;
            }
            return await response.json();
        } catch (error) {
            console.error('API Fetch Error:', error);
            showToast('API Error', error.message, 'danger');
            throw error;
        }
    }

    // =====================================================================
    // Order Management Functions
    // =====================================================================
    async function fetchFoodOrders() {
        DOM.ordersTableBody.innerHTML = `<tr><td colspan="6" class="text-center"><div class="spinner-border"></div></td></tr>`;
        try {
            const orders = await apiFetch('/api/food-orders/all');
            STATE.allOrders = orders;
            displayOrders();
            updateDashboardCounters();
        } catch (error) {
            DOM.ordersTableBody.innerHTML = `<tr><td colspan="6" class="text-center text-danger">Failed to load orders.</td></tr>`;
        }
    }

    function displayOrders() {
        DOM.ordersTableBody.innerHTML = '';
        if (STATE.allOrders.length === 0) {
            DOM.ordersTableBody.innerHTML = `<tr><td colspan="6" class="text-center">No orders found.</td></tr>`;
            return;
        }
        STATE.allOrders.forEach(order => {
            const itemNames = order.items.map(item => item.name).join(', ');
            const row = document.createElement('tr');
            row.innerHTML = `
                <td><strong>${order.id}</strong></td>
                <td><span class="badge bg-secondary">Room</span> ${order.room.roomNumber}</td>
                <td title="${itemNames}">${itemNames.substring(0, 50)}...</td>
                <td><strong class="text-success">$${order.totalPrice.toFixed(2)}</strong></td>
                <td><span class="badge bg-${getStatusColor(order.status)}">${order.status}</span></td>
                <td>
                    <select class="form-select form-select-sm update-order-status" data-order-id="${order.id}">
                        <option value="PENDING" ${order.status === 'PENDING' ? 'selected' : ''}>Pending</option>
                        <option value="PREPARING" ${order.status === 'PREPARING' ? 'selected' : ''}>Preparing</option>
                        <option value="DELIVERED" ${order.status === 'DELIVERED' ? 'selected' : ''}>Delivered</option>
                        <option value="CANCELLED" ${order.status === 'CANCELLED' ? 'selected' : ''}>Cancelled</option>
                    </select>
                </td>
            `;
            DOM.ordersTableBody.appendChild(row);
        });
    }

    async function handleOrderStatusChange(e) {
        if (!e.target.classList.contains('update-order-status')) return;
        const orderId = e.target.dataset.orderId;
        const newStatus = e.target.value;
        try {
            await apiFetch(`/api/food-orders/${orderId}/status`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ status: newStatus }),
            });
            showToast('Success', `Order ${orderId} status updated to ${newStatus}`, 'success');
            await fetchFoodOrders(); // Refresh orders
        } catch (error) {
            showToast('Error', `Failed to update order ${orderId}`, 'danger');
        }
    }

    // =====================================================================
    // Menu Management Functions
    // =====================================================================
    async function fetchMenuItems() {
        DOM.menuTableBody.innerHTML = `<tr><td colspan="7" class="text-center"><div class="spinner-border"></div></td></tr>`;
        try {
            const items = await apiFetch('/api/menu-items/all');
            STATE.allMenuItems = items;
            displayMenuItems();
            updateDashboardCounters();
        } catch (error) {
            DOM.menuTableBody.innerHTML = `<tr><td colspan="7" class="text-center text-danger">Failed to load menu items.</td></tr>`;
        }
    }

    function displayMenuItems() {
        DOM.menuTableBody.innerHTML = '';
        if (STATE.allMenuItems.length === 0) {
            DOM.menuTableBody.innerHTML = `<tr><td colspan="7" class="text-center">No menu items found.</td></tr>`;
            return;
        }
        STATE.allMenuItems.forEach(item => {
            const row = document.createElement('tr');
            const imageSrc = item.imageUrl ? `${API_BASE_URL}${item.imageUrl}` : 'https://via.placeholder.com/60';
            row.innerHTML = `
                <td><img src="${imageSrc}" class="menu-item-thumbnail rounded" alt="${item.name}"></td>
                <td><strong>${item.name}</strong></td>
                <td title="${item.description}">${(item.description || '').substring(0, 50)}...</td>
                <td><span class="badge bg-secondary">${item.category}</span></td>
                <td><strong class="text-success">$${item.price.toFixed(2)}</strong></td>
                <td>
                    <div class="form-check form-switch"><input class="form-check-input" type="checkbox" ${item.available ? 'checked' : ''} disabled></div>
                </td>
                <td>
                    <button class="btn btn-sm btn-outline-primary edit-item" data-id="${item.id}"><i class="fas fa-edit"></i></button>
                    <button class="btn btn-sm btn-outline-danger delete-item" data-id="${item.id}"><i class="fas fa-trash"></i></button>
                </td>
            `;
            DOM.menuTableBody.appendChild(row);
        });
    }

    function showAddMenuItemModal() {
        DOM.modalTitle.textContent = 'Add Menu Item';
        DOM.menuItemForm.reset();
        DOM.menuItemId.value = '';
        DOM.imageUrl.value = '';
        DOM.imagePreviewContainer.classList.add('d-none');
        DOM.noImagePlaceholder.classList.remove('d-none');
        DOM.menuItemModal.show();
    }

    async function handleMenuItemFormSubmit(e) {
        e.preventDefault();
        const id = DOM.menuItemId.value;
        const isUpdate = !!id;

        // Step 1: Upload image if a new one is selected
        let uploadedImageUrl = DOM.imageUrl.value;
        if (DOM.imageFile.files[0]) {
            const formData = new FormData();
            formData.append('file', DOM.imageFile.files[0]);
            try {
                const uploadResponse = await apiFetch('/api/files/upload', {
                    method: 'POST',
                    body: formData,
                });
                uploadedImageUrl = uploadResponse.url;
            } catch (error) {
                showToast('Error', 'Image upload failed!', 'danger');
                return;
            }
        }

        // Step 2: Prepare menu item data
        const menuItemData = {
            name: document.getElementById('name').value,
            description: document.getElementById('description').value,
            price: parseFloat(document.getElementById('price').value),
            category: document.getElementById('category').value,
            available: document.getElementById('isAvailable').checked,
            imageUrl: uploadedImageUrl,
        };

        // Step 3: Send data to the backend
        try {
            const url = isUpdate ? `/api/menu-items/${id}` : '/api/menu-items';
            const method = isUpdate ? 'PUT' : 'POST';
            await apiFetch(url, {
                method: method,
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(menuItemData),
            });
            showToast('Success', `Menu item ${isUpdate ? 'updated' : 'created'} successfully!`, 'success');
            DOM.menuItemModal.hide();
            await fetchMenuItems(); // Refresh list
        } catch (error) {
            showToast('Error', 'Failed to save menu item.', 'danger');
        }
    }

    function handleEditMenuItem(id) {
        const item = STATE.allMenuItems.find(item => item.id == id);
        if (!item) return;

        DOM.modalTitle.textContent = 'Edit Menu Item';
        DOM.menuItemId.value = item.id;
        document.getElementById('name').value = item.name;
        document.getElementById('description').value = item.description;
        document.getElementById('price').value = item.price;
        document.getElementById('category').value = item.category;
        document.getElementById('isAvailable').checked = item.available;
        DOM.imageUrl.value = item.imageUrl || '';

        if (item.imageUrl) {
            DOM.imagePreview.src = `${API_BASE_URL}${item.imageUrl}`;
            DOM.imagePreviewContainer.classList.remove('d-none');
            DOM.noImagePlaceholder.classList.add('d-none');
        } else {
            DOM.imagePreviewContainer.classList.add('d-none');
            DOM.noImagePlaceholder.classList.remove('d-none');
        }
        DOM.menuItemModal.show();
    }

    async function handleDeleteMenuItem(id) {
        if (!confirm('Are you sure you want to delete this menu item?')) return;
        try {
            await apiFetch(`/api/menu-items/${id}`, { method: 'DELETE' });
            showToast('Success', 'Menu item deleted.', 'success');
            await fetchMenuItems();
        } catch (error) {
            showToast('Error', 'Failed to delete menu item.', 'danger');
        }
    }

    // =====================================================================
    // Utility and Initialization
    // =====================================================================
    function getStatusColor(status) {
        const colors = { PENDING: 'warning', PREPARING: 'primary', DELIVERED: 'success', CANCELLED: 'danger' };
        return colors[status] || 'secondary';
    }

    function updateDashboardCounters() {
        DOM.pendingOrdersCount.textContent = STATE.allOrders.filter(o => o.status === 'PENDING').length;
        DOM.menuItemsCount.textContent = STATE.allMenuItems.length;
        // More complex stats can be added here
        DOM.completedOrdersCount.textContent = 'N/A';
        DOM.todaysRevenue.textContent = 'N/A';
    }

    function setupEventListeners() {
        DOM.addMenuItemBtn.addEventListener('click', showAddMenuItemModal);
        DOM.menuItemForm.addEventListener('submit', handleMenuItemFormSubmit);
        DOM.ordersTableBody.addEventListener('change', handleOrderStatusChange);

        DOM.menuTableBody.addEventListener('click', (e) => {
            if (e.target.closest('.edit-item')) {
                handleEditMenuItem(e.target.closest('.edit-item').dataset.id);
            }
            if (e.target.closest('.delete-item')) {
                handleDeleteMenuItem(e.target.closest('.delete-item').dataset.id);
            }
        });

        DOM.imageFile.addEventListener('change', function() {
            const file = this.files[0];
            if (file) {
                const reader = new FileReader();
                reader.onload = e => {
                    DOM.imagePreview.src = e.target.result;
                    DOM.imagePreviewContainer.classList.remove('d-none');
                    DOM.noImagePlaceholder.classList.add('d-none');
                };
                reader.readAsDataURL(file);
            }
        });
    }

    // Initial Load
    document.addEventListener('DOMContentLoaded', () => {
        setupEventListeners();
        fetchFoodOrders();
        fetchMenuItems();
    });
})();