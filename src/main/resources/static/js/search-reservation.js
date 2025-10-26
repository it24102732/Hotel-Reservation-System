document.addEventListener('DOMContentLoaded', function () {
    const searchForm = document.getElementById('searchReservationForm');
    const searchResultContainer = document.getElementById('searchResult');
    const searchButton = searchForm.querySelector('button[type="submit"]');
    const searchSpinner = document.getElementById('searchSpinner');

    // Check if we have saved search parameters
    if (localStorage.getItem('bookingId') && localStorage.getItem('lastName')) {
        document.getElementById('bookingId').value = localStorage.getItem('bookingId');
        document.getElementById('lastName').value = localStorage.getItem('lastName');
        document.getElementById('rememberDetails').checked = true;
    }

    searchForm.addEventListener('submit', function (e) {
        e.preventDefault();

        const bookingId = document.getElementById('bookingId').value;
        const lastName = document.getElementById('lastName').value;

        // Save details if remember checkbox is checked
        if (document.getElementById('rememberDetails').checked) {
            localStorage.setItem('bookingId', bookingId);
            localStorage.setItem('lastName', lastName);
        }

        // Show loading state
        searchButton.disabled = true;
        searchSpinner.classList.remove('d-none');
        searchResultContainer.innerHTML = '<div class="text-center py-4"><div class="spinner-border text-primary" role="status"><span class="visually-hidden">Searching...</span></div><p class="mt-3 text-muted">Searching for your reservation...</p></div>';

        const searchData = {
            bookingId: bookingId,
            lastName: lastName
        };

        fetch('/api/bookings/search', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(searchData)
        })
            .then(response => {
                if (!response.ok) {
                    return response.text().then(text => { throw new Error(text) });
                }
                return response.json();
            })
            .then(booking => {
                // Format dates nicely
                const checkInDate = new Date(booking.checkInDate).toLocaleDateString('en-US', {
                    weekday: 'long',
                    year: 'numeric',
                    month: 'long',
                    day: 'numeric'
                });

                const checkOutDate = new Date(booking.checkOutDate).toLocaleDateString('en-US', {
                    weekday: 'long',
                    year: 'numeric',
                    month: 'long',
                    day: 'numeric'
                });

                // Calculate number of nights
                const nights = Math.round((new Date(booking.checkOutDate) - new Date(booking.checkInDate)) / (1000 * 60 * 60 * 24));

                // Get status badge color
                let statusBadgeClass = 'bg-secondary';
                if (booking.status === 'CONFIRMED') statusBadgeClass = 'bg-success';
                if (booking.status === 'CANCELLED') statusBadgeClass = 'bg-danger';
                if (booking.status === 'PENDING_PAYMENT') statusBadgeClass = 'bg-warning';

                searchResultContainer.innerHTML = `
                <div class="card">
                    <div class="card-header d-flex justify-content-between align-items-center">
                        <span><i class="fas fa-clipboard-check me-2"></i>Booking Details</span>
                        <span class="badge ${statusBadgeClass}">${booking.status}</span>
                    </div>
                    <div class="card-body">
                        <h5 class="card-title">Booking #${booking.id}</h5>
                        
                        <div class="row">
                            <div class="col-md-6">
                                <p class="card-text"><i class="fas fa-door-open me-2 text-primary"></i><strong>Room:</strong> ${booking.room.roomNumber} (${booking.room.type})</p>
                                <p class="card-text"><i class="fas fa-calendar-check me-2 text-primary"></i><strong>Check-in:</strong> ${checkInDate}</p>
                                <p class="card-text"><i class="fas fa-calendar-times me-2 text-primary"></i><strong>Check-out:</strong> ${checkOutDate}</p>
                                <p class="card-text"><i class="fas fa-moon me-2 text-primary"></i><strong>Stay Duration:</strong> ${nights} night${nights !== 1 ? 's' : ''}</p>
                            </div>
                            <div class="col-md-6">
                                <div class="card bg-light border-0">
                                    <div class="card-body">
                                        <h6 class="card-subtitle mb-2 text-muted">Need to make changes?</h6>
                                        <p class="small">If you need to modify or cancel your reservation, please contact our guest services.</p>
                                        <div class="d-grid gap-2">
                                            <button class="btn btn-outline-primary btn-sm">
                                                <i class="fas fa-phone-alt me-2"></i>Contact Us
                                            </button>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                        
                        <hr class="my-4">
                        
                        <div class="text-center">
                            <button class="btn btn-outline-secondary me-2">
                                <i class="fas fa-print me-2"></i>Print Details
                            </button>
                            <button class="btn btn-primary">
                                <i class="fas fa-envelope me-2"></i>Email Confirmation
                            </button>
                        </div>
                    </div>
                </div>
            `;

                // Add event listeners for the new buttons
                const printBtn = searchResultContainer.querySelector('.btn-outline-secondary');
                printBtn.addEventListener('click', function() {
                    window.print();
                });

                const emailBtn = searchResultContainer.querySelector('.btn-primary');
                emailBtn.addEventListener('click', function() {
                    alert('Confirmation email sent to your registered email address.');
                });

                const contactBtn = searchResultContainer.querySelector('.btn-outline-primary');
                contactBtn.addEventListener('click', function() {
                    alert('Please call +1 (555) 123-4567 to speak with our guest services team.');
                });
            })
            .catch(error => {
                searchResultContainer.innerHTML = `
                <div class="alert alert-danger" role="alert">
                    <div class="d-flex align-items-center">
                        <i class="fas fa-exclamation-circle fa-2x me-3"></i>
                        <div>
                            <h5 class="mb-1">Reservation Not Found</h5>
                            <p class="mb-0">${error.message}</p>
                        </div>
                    </div>
                </div>
                <div class="text-center mt-3">
                    <p>Please check the booking ID and last name, or try these options:</p>
                    <button class="btn btn-outline-primary btn-sm me-2" id="contactHelpBtn">
                        <i class="fas fa-headset me-2"></i>Contact Support
                    </button>
                    <button class="btn btn-outline-secondary btn-sm" id="newSearchBtn">
                        <i class="fas fa-search me-2"></i>Try Different Details
                    </button>
                </div>
            `;

                // Add event listeners for error buttons
                document.getElementById('contactHelpBtn').addEventListener('click', function() {
                    alert('Please call +1 (555) 123-4567 for assistance with finding your reservation.');
                });

                document.getElementById('newSearchBtn').addEventListener('click', function() {
                    document.getElementById('bookingId').value = '';
                    document.getElementById('lastName').value = '';
                    document.getElementById('bookingId').focus();
                });
            })
            .finally(() => {
                // Reset loading state
                searchButton.disabled = false;
                searchSpinner.classList.add('d-none');
            });
    });
});