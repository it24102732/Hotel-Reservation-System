// Minimal JavaScript - Most logic handled by backend

document.addEventListener('DOMContentLoaded', function() {
    // Sidebar toggle for mobile
    const sidebarToggle = document.getElementById('toggle-sidebar');
    const closeSidebar = document.getElementById('close-sidebar');
    const sidebar = document.getElementById('sidebar');

    if (sidebarToggle) {
        sidebarToggle.addEventListener('click', function() {
            sidebar.classList.toggle('active');
        });
    }

    if (closeSidebar) {
        closeSidebar.addEventListener('click', function() {
            sidebar.classList.remove('active');
        });
    }

    // Bootstrap form validation
    const forms = document.querySelectorAll('.needs-validation');
    Array.from(forms).forEach(form => {
        form.addEventListener('submit', event => {
            if (!form.checkValidity()) {
                event.preventDefault();
                event.stopPropagation();
            }
            form.classList.add('was-validated');
        }, false);
    });

    // Date validation - checkout must be after checkin
    const checkinInput = document.getElementById('booking-checkin');
    const checkoutInput = document.getElementById('booking-checkout');
    const roomSelect = document.getElementById('booking-room');

    if (checkinInput && checkoutInput) {
        checkinInput.addEventListener('change', function() {
            checkoutInput.min = this.value;
            if (checkoutInput.value && checkoutInput.value <= this.value) {
                checkoutInput.value = '';
            }
            loadAvailableRooms();
        });

        checkoutInput.addEventListener('change', loadAvailableRooms);
    }

    // Load available rooms via AJAX when dates change
    function loadAvailableRooms() {
        const checkin = checkinInput.value;
        const checkout = checkoutInput.value;

        if (checkin && checkout && checkout > checkin) {
            roomSelect.innerHTML = '<option value="">Loading rooms...</option>';

            fetch(`/api/rooms/available?checkInDate=${checkin}&checkOutDate=${checkout}`)
                .then(response => response.json())
                .then(rooms => {
                    roomSelect.innerHTML = '<option value="">Select a room...</option>';
                    if (rooms.length === 0) {
                        roomSelect.innerHTML = '<option value="">No rooms available for these dates</option>';
                    } else {
                        rooms.forEach(room => {
                            const option = document.createElement('option');
                            option.value = room.id;
                            option.textContent = `Room ${room.roomNumber} - ${room.type} ($${room.price}/night)`;
                            option.dataset.price = room.price;
                            roomSelect.appendChild(option);
                        });
                    }
                })
                .catch(error => {
                    console.error('Error loading rooms:', error);
                    roomSelect.innerHTML = '<option value="">Error loading rooms. Please try again.</option>';
                });
        }
    }

    // Show price info when room selected
    if (roomSelect) {
        roomSelect.addEventListener('change', function() {
            const priceInfo = document.getElementById('room-price-info');
            const selectedOption = this.options[this.selectedIndex];

            if (selectedOption.dataset.price && checkinInput.value && checkoutInput.value) {
                const checkin = new Date(checkinInput.value);
                const checkout = new Date(checkoutInput.value);
                const days = Math.ceil((checkout - checkin) / (1000 * 60 * 60 * 24));
                const pricePerNight = parseFloat(selectedOption.dataset.price);
                const total = (pricePerNight * days).toFixed(2);

                priceInfo.innerHTML = `<i class="bi bi-info-circle me-1"></i>${days} night(s) × $${pricePerNight} = <strong>$${total}</strong>`;
                priceInfo.style.display = 'block';
            } else {
                priceInfo.innerHTML = '';
                priceInfo.style.display = 'none';
            }
        });
    }

    // Initialize Bootstrap tooltips
    const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });

    // Auto-dismiss alerts after 5 seconds
    const alerts = document.querySelectorAll('.alert');
    alerts.forEach(alert => {
        setTimeout(() => {
            const bsAlert = new bootstrap.Alert(alert);
            bsAlert.close();
        }, 5000);
    });

    // Close sidebar when clicking outside on mobile
    document.addEventListener('click', function(event) {
        if (window.innerWidth < 992) {
            if (sidebar && sidebar.classList.contains('active')) {
                if (!sidebar.contains(event.target) && !sidebarToggle.contains(event.target)) {
                    sidebar.classList.remove('active');
                }
            }
        }
    });

    // Prevent form resubmission on page refresh
    if (window.history.replaceState) {
        window.history.replaceState(null, null, window.location.href);
    }
});