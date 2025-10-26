document.addEventListener('DOMContentLoaded', function () {
    const menuItemsContainer = document.getElementById('menuItems');
    const menuSearch = document.getElementById('menuSearch');
    // FIXED: This selector now correctly finds your buttons. This was the only error.
    const categoryButtons = document.querySelectorAll('.menu-categories .btn');
    let allMenuItems = [];
    let currentCategory = 'all';

    // Fetch and display available menu items
    function fetchAvailableMenuItems(category = null) {
        // Show loading state
        menuItemsContainer.innerHTML = `
            <div class="col-12 text-center py-5">
                <div class="spinner-border text-primary" role="status">
                    <span class="visually-hidden">Loading menu items...</span>
                </div>
            </div>
        `;

        // Build URL with category parameter if provided
        let url = '/api/menu-items/available';
        if (category && category !== 'all') {
            url += `?category=${encodeURIComponent(category)}`;
        }

        // Fetch menu items from API
        fetch(url)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                return response.json();
            })
            .then(menuItems => {
                allMenuItems = menuItems;
                displayMenuItems(menuItems);
            })
            .catch(error => {
                console.error('Error fetching menu items:', error);
                menuItemsContainer.innerHTML = '<p class="text-center text-danger">Failed to load menu items. Please try again later.</p>';
            });
    }

    // Display menu items with improved layout
    function displayMenuItems(menuItems) {
        menuItemsContainer.innerHTML = '';

        if (menuItems.length === 0) {
            menuItemsContainer.innerHTML = '<p class="text-center">No menu items available at the moment.</p>';
            return;
        }

        // Create a row container for the menu items
        const rowContainer = document.createElement('div');
        rowContainer.className = 'row';
        menuItemsContainer.appendChild(rowContainer);

        menuItems.forEach(item => {
            const menuItemElement = document.createElement('div');
            menuItemElement.className = 'col-md-6 col-lg-4 mb-4';

            // Ensure imageUrl has proper path
            const imageUrl = item.imageUrl ?
                (item.imageUrl.startsWith('/') ? item.imageUrl : '/' + item.imageUrl) :
                '/images/default-food.jpg';

            menuItemElement.innerHTML = `
                <div class="menu-card">
                    <div class="menu-card-img">
                        <img src="${imageUrl}" alt="${item.name}" class="img-fluid rounded" 
                            onerror="this.onerror=null; this.src='/images/default-food.jpg';">
                    </div>
                    <div class="menu-card-body">
                        <h4 class="menu-item-title">${item.name}</h4>
                        <span class="menu-item-category badge bg-secondary mb-2">${item.category || 'Uncategorized'}</span>
                        <p class="menu-item-desc">${item.description || 'No description available'}</p>
                        <div class="d-flex justify-content-between align-items-center">
                            <span class="menu-item-price">$${item.price.toFixed(2)}</span>
                            <button class="btn btn-sm btn-primary order-btn" 
                                data-id="${item.id}" data-name="${item.name}" data-price="${item.price}">
                                Order
                            </button>
                        </div>
                    </div>
                </div>
            `;
            rowContainer.appendChild(menuItemElement);
        });

        // Initialize order buttons
        initOrderButtons();
    }

    // Search functionality
    if (menuSearch) {
        menuSearch.addEventListener('input', function() {
            const searchTerm = this.value.toLowerCase().trim();

            if (searchTerm === '') {
                // If search is cleared, reapply current category filter
                if (currentCategory === 'all') {
                    displayMenuItems(allMenuItems);
                } else {
                    const filteredByCategory = allMenuItems.filter(item =>
                        item.category && item.category.toLowerCase() === currentCategory.toLowerCase()
                    );
                    displayMenuItems(filteredByCategory);
                }
                return;
            }

            // Apply search filter on top of current category filter
            let itemsToSearch = allMenuItems;
            if (currentCategory !== 'all') {
                itemsToSearch = allMenuItems.filter(item =>
                    item.category && item.category.toLowerCase() === currentCategory.toLowerCase()
                );
            }

            const filteredItems = itemsToSearch.filter(item =>
                item.name.toLowerCase().includes(searchTerm) ||
                (item.description && item.description.toLowerCase().includes(searchTerm))
            );

            displayMenuItems(filteredItems);
        });
    }

    // Category filter functionality
    if (categoryButtons) {
        categoryButtons.forEach(button => {
            button.addEventListener('click', function() {
                const category = this.getAttribute('data-category');
                currentCategory = category; // Store current category

                // Update active button
                categoryButtons.forEach(btn => btn.classList.remove('active'));
                this.classList.add('active');

                // Fetch items with category filter
                fetchAvailableMenuItems(category);
            });
        });
    }

    // Handle order button functionality
    function initOrderButtons() {
        document.querySelectorAll('.order-btn').forEach(button => {
            button.addEventListener('click', function() {
                const id = this.dataset.id;
                const name = this.dataset.name;
                const price = parseFloat(this.dataset.price);

                // Add item to cart
                addToCart({id, name, price, quantity: 1});

                // Show toast notification
                showToast(`${name} added to cart`);
            });
        });
    }

    // Simple toast notification
    function showToast(message) {
        const toast = document.createElement('div');
        toast.className = 'menu-toast';
        toast.textContent = message;
        document.body.appendChild(toast);

        // Trigger animation
        setTimeout(() => toast.classList.add('show'), 10);

        // Remove after 3 seconds
        setTimeout(() => {
            toast.classList.remove('show');
            setTimeout(() => toast.remove(), 500);
        }, 3000);
    }

    // Cart functionality (simplified)
    function addToCart(item) {
        let cart = JSON.parse(localStorage.getItem('foodCart')) || [];

        // Check if item already exists
        const existingItemIndex = cart.findIndex(cartItem => cartItem.id === item.id);

        if (existingItemIndex > -1) {
            cart[existingItemIndex].quantity += item.quantity;
        } else {
            cart.push(item);
        }

        localStorage.setItem('foodCart', JSON.stringify(cart));
        updateCartBadge();
    }

    // Update cart counter badge
    function updateCartBadge() {
        const cart = JSON.parse(localStorage.getItem('foodCart')) || [];
        const cartBadge = document.getElementById('cartBadge');

        if (cartBadge) {
            const totalItems = cart.reduce((sum, item) => sum + item.quantity, 0);
            cartBadge.textContent = totalItems;
            cartBadge.style.display = totalItems > 0 ? 'inline' : 'none';
        }
    }

    // Initial data load
    fetchAvailableMenuItems();
    updateCartBadge();
});