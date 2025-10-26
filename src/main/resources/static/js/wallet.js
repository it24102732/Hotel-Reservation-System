document.addEventListener('DOMContentLoaded', function () {
    const userId = 1;
    let cards = [];

    // DOM Elements
    const elements = {
        cardList: document.getElementById('card-list'),
        emptyState: document.getElementById('empty-state'),
        loadingOverlay: document.getElementById('loadingOverlay'),
        userName: document.getElementById('userName'),
        userAccountId: document.getElementById('userAccountId'),
        totalCards: document.getElementById('totalCards'),
        activeCards: document.getElementById('activeCards'),
        expiringCards: document.getElementById('expiringCards'),
        accountBalance: document.getElementById('accountBalance'),
        searchCards: document.getElementById('searchCards'),
        refreshBtn: document.getElementById('refreshBtn'),
        addCardBtn: document.getElementById('addCardBtn'),
        addCardBtnEmpty: document.getElementById('addCardBtnEmpty'),
        cardForm: document.getElementById('cardForm'),
        cardId: document.getElementById('cardId'),
        cardHolderName: document.getElementById('cardHolderName'),
        cardNumber: document.getElementById('cardNumber'),
        expiryDate: document.getElementById('expiryDate'),
        cvv: document.getElementById('cvv'),
        cardModalTitle: document.getElementById('cardModalTitle'),
        deleteCardName: document.getElementById('deleteCardName'),
        deleteCardNumber: document.getElementById('deleteCardNumber'),
        confirmDeleteBtn: document.getElementById('confirmDeleteBtn')
    };

    // Modals
    const cardModal = new bootstrap.Modal(document.getElementById('cardModal'));
    const deleteModal = new bootstrap.Modal(document.getElementById('deleteModal'));
    const toast = new bootstrap.Toast(document.getElementById('notificationToast'));

    // Utility Functions
    const showLoading = (show = true) => elements.loadingOverlay.style.display = show ? 'flex' : 'none';

    const showToast = (message, type = 'success') => {
        const toastBody = document.getElementById('toastBody');
        const toastIcon = document.getElementById('notificationToast').querySelector('.toast-icon');
        toastBody.textContent = message;
        document.getElementById('notificationToast').className = `toast align-items-center border-0 toast-${type}`;

        const icons = { success: 'fa-check-circle', error: 'fa-exclamation-circle', warning: 'fa-exclamation-triangle' };
        toastIcon.className = `fas ${icons[type] || 'fa-check-circle'} me-2 toast-icon`;
        toast.show();
    };

    const maskCardNumber = (number) => {
        if (!number) return '•••• •••• •••• ••••';
        const cleaned = number.replace(/\s/g, '');
        return `•••• •••• •••• ${cleaned.slice(-4)}`;
    };

    const formatCardNumber = (value) => {
        const cleaned = value.replace(/\D/g, '');
        const parts = [];
        for (let i = 0; i < cleaned.length; i += 4) {
            parts.push(cleaned.substring(i, i + 4));
        }
        return parts.join(' ');
    };

    const formatExpiryDisplay = (dateString) => {
        if (!dateString) return 'MM/YY';
        const date = new Date(dateString);
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const year = String(date.getFullYear()).slice(-2);
        return `${month}/${year}`;
    };

    const getCardGradient = (index) => {
        const gradients = ['card-gradient-1', 'card-gradient-2', 'card-gradient-3', 'card-gradient-4', 'card-gradient-5', 'card-gradient-6'];
        return gradients[index % gradients.length];
    };

    const isCardExpired = (expiryDate) => new Date(expiryDate) < new Date();

    const isCardExpiringSoon = (expiryDate) => {
        const expiry = new Date(expiryDate);
        const threeMonthsFromNow = new Date();
        threeMonthsFromNow.setMonth(threeMonthsFromNow.getMonth() + 3);
        return expiry <= threeMonthsFromNow && expiry > new Date();
    };

    // API Calls
    const fetchUserData = async () => {
        try {
            const response = await fetch(`/api/users/${userId}`);
            if (!response.ok) throw new Error('Could not fetch user data');
            const userData = await response.json();
            elements.userName.textContent = userData.name || 'Guest';
            elements.userAccountId.textContent = userData.id || userId;
        } catch (error) {
            console.error('User fetch error:', error);
        }
    };

    const fetchCards = async () => {
        showLoading(true);
        try {
            const response = await fetch(`/api/users/${userId}/wallet/cards`);
            if (!response.ok) throw new Error('Could not fetch cards');
            cards = await response.json();
            renderCards();
            await fetchStatistics();
        } catch (error) {
            showToast(error.message, 'error');
        } finally {
            showLoading(false);
        }
    };

    const fetchStatistics = async () => {
        try {
            const response = await fetch(`/api/users/${userId}/wallet/statistics`);
            if (!response.ok) return;
            const stats = await response.json();

            elements.totalCards.textContent = stats.totalCards || 0;
            elements.activeCards.textContent = stats.activeCards || 0;
            elements.expiringCards.textContent = stats.expiringCards || 0;
            elements.accountBalance.textContent = (stats.totalBalance || 0).toFixed(2);
        } catch (error) {
            console.error('Statistics fetch error:', error);
        }
    };

    // Render Cards
    const renderCards = () => {
        elements.cardList.innerHTML = '';

        if (cards.length === 0) {
            elements.emptyState.style.display = 'block';
            return;
        }

        elements.emptyState.style.display = 'none';

        cards.forEach((card, index) => {
            const expired = isCardExpired(card.expiryDate);
            const expiring = isCardExpiringSoon(card.expiryDate);
            const gradientClass = getCardGradient(index);

            let statusHTML = expired ? '<span class="card-status status-expired">Expired</span>' :
                expiring ? '<span class="card-status status-expiring">Expiring Soon</span>' :
                    '<span class="card-status status-active">Active</span>';

            const cardElement = document.createElement('div');
            cardElement.className = 'fade-in';
            cardElement.innerHTML = `
                <div class="card-visual ${gradientClass} ${expired ? 'card-expired' : ''}" data-card-id="${card.id}">
                    ${card.isDefault ? '<span class="card-status" style="right: 1.5rem; left: auto; background: rgba(255, 215, 0, 0.3); border-color: gold;">Default</span>' : ''}
                    ${statusHTML}
                    <div class="card-chip"></div>
                    <div class="card-number">${maskCardNumber(card.cardNumber)}</div>
                    <div class="d-flex justify-content-between card-footer-details">
                        <div>
                            <small>Card Holder</small>
                            <div>${card.cardHolderName}</div>
                        </div>
                        <div class="text-end">
                            <small>Expires</small>
                            <div>${formatExpiryDisplay(card.expiryDate)}</div>
                        </div>
                    </div>
                    <div class="card-balance-display">
                        <small>Balance</small>
                        <div class="fw-bold">$${(card.balance || 0).toFixed(2)}</div>
                    </div>
                    <div class="card-brand"><i class="fab fa-cc-visa"></i></div>
                    <div class="card-actions">
                        <button class="btn edit-btn" data-card-id="${card.id}" title="Edit Card" ${expired || card.isDefault ? 'disabled' : ''}>
                            <i class="fas fa-edit"></i>
                        </button>
                        ${!card.isDefault ? `<button class="btn delete-btn" data-card-id="${card.id}" title="Delete Card">
                            <i class="fas fa-trash"></i>
                        </button>` : ''}
                    </div>
                </div>
            `;
            elements.cardList.appendChild(cardElement);
        });
    };

    // Event Handlers
    const openAddModal = () => {
        elements.cardForm.reset();
        elements.cardId.value = '';
        elements.cardModalTitle.textContent = 'Add New Card';
        elements.cardNumber.disabled = false;
        elements.cvv.disabled = false;
        cardModal.show();
    };

    elements.addCardBtn.addEventListener('click', openAddModal);
    elements.addCardBtnEmpty?.addEventListener('click', openAddModal);

    elements.refreshBtn.addEventListener('click', async () => {
        await fetchCards();
        await fetchUserData();
        showToast('Data refreshed successfully!', 'success');
    });

    elements.cardList.addEventListener('click', (e) => {
        const editBtn = e.target.closest('.edit-btn');
        const deleteBtn = e.target.closest('.delete-btn');

        if (editBtn && !editBtn.disabled) {
            e.stopPropagation();
            const card = cards.find(c => c.id == editBtn.dataset.cardId);
            if (card) {
                elements.cardId.value = card.id;
                elements.cardHolderName.value = card.cardHolderName;
                elements.cardNumber.value = card.cardNumber;
                elements.cardNumber.disabled = true;
                elements.cvv.disabled = true;
                elements.expiryDate.value = card.expiryDate.substring(0, 7);
                elements.cardModalTitle.textContent = 'Edit Card';
                cardModal.show();
            }
        } else if (deleteBtn) {
            e.stopPropagation();
            const card = cards.find(c => c.id == deleteBtn.dataset.cardId);
            if (card) {
                elements.deleteCardName.textContent = card.cardHolderName;
                elements.deleteCardNumber.textContent = maskCardNumber(card.cardNumber);
                elements.confirmDeleteBtn.onclick = () => deleteCard(deleteBtn.dataset.cardId);
                deleteModal.show();
            }
        }
    });

    // Form Submission
    elements.cardForm.addEventListener('submit', async (e) => {
        e.preventDefault();

        const cardId = elements.cardId.value;
        const isEditing = !!cardId;

        const cardData = {
            cardHolderName: elements.cardHolderName.value.trim(),
            cardNumber: elements.cardNumber.value.replace(/\s/g, ''),
            expiryDate: elements.expiryDate.value + "-01"
        };

        const url = isEditing ? `/api/users/${userId}/wallet/cards/${cardId}` : `/api/users/${userId}/wallet/cards`;
        const method = isEditing ? 'PUT' : 'POST';

        showLoading(true);
        try {
            const response = await fetch(url, {
                method,
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(cardData)
            });

            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(errorText || 'Failed to save card');
            }

            showToast(`Card successfully ${isEditing ? 'updated' : 'added'}!`, 'success');
            cardModal.hide();
            await fetchCards();
        } catch (error) {
            showToast(error.message, 'error');
        } finally {
            showLoading(false);
        }
    });

    const deleteCard = async (cardId) => {
        showLoading(true);
        try {
            const response = await fetch(`/api/users/${userId}/wallet/cards/${cardId}`, { method: 'DELETE' });

            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(errorText || 'Failed to delete card');
            }

            showToast('Card deleted successfully!', 'success');
            deleteModal.hide();
            await fetchCards();
        } catch (error) {
            showToast(error.message, 'error');
        } finally {
            showLoading(false);
        }
    };

    // Card number formatting
    elements.cardNumber.addEventListener('input', (e) => {
        e.target.value = formatCardNumber(e.target.value);
    });

    // Set minimum expiry date
    elements.expiryDate.min = new Date().toISOString().substring(0, 7);

    // Initialize
    fetchUserData();
    fetchCards();
});