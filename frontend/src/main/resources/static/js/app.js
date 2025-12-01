// E-Commerce Demo - Frontend Application

// Configuration
const API_BASE_URL = 'http://localhost:8080/api';
let currentUserId = 'user123';
let cartData = null;

// APM Funnel Tracking: Session and Journey IDs
let sessionId = null;
let journeyId = null;

// Initialize or retrieve session ID (persists across page reloads)
function initializeSessionTracking() {
    // Session ID: Persists across page reloads (localStorage)
    sessionId = localStorage.getItem('sessionId');
    if (!sessionId) {
        sessionId = 'session-' + Date.now() + '-' + Math.random().toString(36).substr(2, 9);
        localStorage.setItem('sessionId', sessionId);
    }
    
    // Journey ID: Unique for each shopping journey (session storage - cleared on tab close)
    journeyId = sessionStorage.getItem('journeyId');
    if (!journeyId) {
        journeyId = 'journey-' + Date.now() + '-' + Math.random().toString(36).substr(2, 9);
        sessionStorage.setItem('journeyId', journeyId);
    }
    
    console.log('Tracking initialized - SessionID:', sessionId, 'JourneyID:', journeyId);
}

// Get tracking headers
function getTrackingHeaders() {
    return {
        'X-Session-ID': sessionId,
        'X-Journey-ID': journeyId
    };
}

// Product icons map
const productIcons = {
    'Laptop': '💻',
    'Mouse': '🖱️',
    'Keyboard': '⌨️',
    'Monitor': '🖥️',
    'Headphones': '🎧',
    'default': '📦'
};

// Initialize app
document.addEventListener('DOMContentLoaded', () => {
    initializeSessionTracking();
    initializeNavigation();
    initializeUserSection();
    loadProducts();
    updateCartBadge();
});

// Navigation
function initializeNavigation() {
    const navButtons = document.querySelectorAll('.nav-btn');
    navButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            const page = btn.dataset.page;
            switchPage(page);
            
            // Update active state
            navButtons.forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
        });
    });
}

function switchPage(page) {
    const pages = document.querySelectorAll('.page');
    pages.forEach(p => p.classList.remove('active'));
    
    const targetPage = document.getElementById(`${page}Page`);
    if (targetPage) {
        targetPage.classList.add('active');
        
        // Load data for the page
        if (page === 'cart') {
            loadCart();
            // APM Funnel Tracking: Track cart view event
            trackCartViewEvent();
        } else if (page === 'orders') {
            loadOrders();
        }
    }
}

// APM Funnel Tracking: Track cart view event
async function trackCartViewEvent() {
    try {
        await fetch(`${API_BASE_URL}/cart/${currentUserId}/view-event`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                ...getTrackingHeaders()
            }
        });
        console.log('FUNNEL_TRACKING: Cart view event tracked');
    } catch (error) {
        console.error('Failed to track cart view event:', error);
    }
}

// User Section
function initializeUserSection() {
    const userIdInput = document.getElementById('userId');
    const loadButton = document.getElementById('loadUserData');
    
    userIdInput.addEventListener('change', (e) => {
        currentUserId = e.target.value || 'user123';
    });
    
    loadButton.addEventListener('click', () => {
        currentUserId = userIdInput.value || 'user123';
        showToast('User changed to: ' + currentUserId, 'success');
        updateCartBadge();
        
        // Reload current page data
        const activePage = document.querySelector('.nav-btn.active').dataset.page;
        if (activePage === 'cart') {
            loadCart();
        } else if (activePage === 'orders') {
            loadOrders();
        }
    });
}

// Products
async function loadProducts() {
    const loading = document.getElementById('productsLoading');
    const grid = document.getElementById('productsGrid');
    
    loading.style.display = 'block';
    grid.innerHTML = '';
    
    try {
        const response = await fetch(`${API_BASE_URL}/products`);
        const products = await response.json();
        
        loading.style.display = 'none';
        
        if (products.length === 0) {
            grid.innerHTML = '<div class="empty-state"><div class="empty-state-icon">📦</div><h3>No products available</h3></div>';
            return;
        }
        
        products.forEach(product => {
            const card = createProductCard(product);
            grid.appendChild(card);
        });
    } catch (error) {
        loading.style.display = 'none';
        showToast('Failed to load products: ' + error.message, 'error');
        console.error('Error loading products:', error);
    }
}

function createProductCard(product) {
    const card = document.createElement('div');
    card.className = 'product-card';
    
    const icon = productIcons[product.name] || productIcons.default;
    
    card.innerHTML = `
        <div class="product-icon">${icon}</div>
        <div class="product-name">${product.name}</div>
        <div class="product-description">${product.description}</div>
        <div class="product-footer">
            <div class="product-price">$${product.price.toFixed(2)}</div>
            <div class="product-stock">Stock: ${product.stock}</div>
        </div>
        <div class="product-actions">
            <input type="number" min="1" max="${product.stock}" value="1" id="qty-${product.id}">
            <button class="btn btn-primary btn-small" onclick="addToCart(${product.id}, '${product.name}', ${product.price})">
                Add to Cart
            </button>
        </div>
    `;
    
    return card;
}

async function addToCart(productId, productName, price) {
    const qtyInput = document.getElementById(`qty-${productId}`);
    const quantity = parseInt(qtyInput.value) || 1;
    
    try {
        const response = await fetch(`${API_BASE_URL}/cart/${currentUserId}/items`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                productId: productId,
                productName: productName,
                price: price,
                quantity: quantity
            })
        });
        
        if (response.ok) {
            showToast(`Added ${quantity}x ${productName} to cart!`, 'success');
            updateCartBadge();
            qtyInput.value = 1;
        } else {
            // Try to parse error message from server
            try {
                const errorData = await response.json();
                const errorMessage = errorData.message || errorData.error || 'Failed to add to cart';
                throw new Error(errorMessage);
            } catch (parseError) {
                throw new Error('Failed to add to cart');
            }
        }
    } catch (error) {
        showToast('Failed to add to cart: ' + error.message, 'error');
        console.error('Error adding to cart:', error);
    }
}

// Cart
async function loadCart() {
    const loading = document.getElementById('cartLoading');
    const cartItems = document.getElementById('cartItems');
    
    loading.style.display = 'block';
    cartItems.innerHTML = '';
    
    try {
        const response = await fetch(`${API_BASE_URL}/cart/${currentUserId}`);
        cartData = await response.json();
        
        loading.style.display = 'none';
        
        if (!cartData.items || cartData.items.length === 0) {
            cartItems.innerHTML = '<div class="empty-state"><div class="empty-state-icon">🛒</div><h3>Your cart is empty</h3><p>Add some products to get started!</p></div>';
            updateCartSummary(0, 0);
            return;
        }
        
        cartData.items.forEach(item => {
            const itemElement = createCartItem(item);
            cartItems.appendChild(itemElement);
        });
        
        updateCartSummary(cartData.items.length, cartData.total);
    } catch (error) {
        loading.style.display = 'none';
        showToast('Failed to load cart: ' + error.message, 'error');
        console.error('Error loading cart:', error);
    }
}

function createCartItem(item) {
    const div = document.createElement('div');
    div.className = 'cart-item';
    
    const icon = productIcons[item.productName] || productIcons.default;
    
    div.innerHTML = `
        <div style="font-size: 2rem;">${icon}</div>
        <div class="cart-item-info">
            <div class="cart-item-name">${item.productName}</div>
            <div class="cart-item-details">
                Quantity: ${item.quantity} × $${item.price.toFixed(2)}
            </div>
        </div>
        <div class="cart-item-actions">
            <div class="cart-item-price">$${(item.price * item.quantity).toFixed(2)}</div>
            <button class="btn btn-danger btn-small" onclick="removeFromCart(${item.id})">Remove</button>
        </div>
    `;
    
    return div;
}

function updateCartSummary(itemCount, total) {
    document.getElementById('totalItems').textContent = itemCount;
    document.getElementById('totalAmount').textContent = `$${(total || 0).toFixed(2)}`;
    
    const checkoutBtn = document.getElementById('checkoutBtn');
    checkoutBtn.disabled = itemCount === 0;
}

async function removeFromCart(itemId) {
    try {
        const response = await fetch(`${API_BASE_URL}/cart/${currentUserId}/items/${itemId}`, {
            method: 'DELETE'
        });
        
        if (response.ok) {
            showToast('Item removed from cart', 'success');
            loadCart();
            updateCartBadge();
        } else {
            throw new Error('Failed to remove item');
        }
    } catch (error) {
        showToast('Failed to remove item: ' + error.message, 'error');
        console.error('Error removing from cart:', error);
    }
}

async function clearCart() {
    if (!confirm('Are you sure you want to clear your cart?')) {
        return;
    }
    
    try {
        const response = await fetch(`${API_BASE_URL}/cart/${currentUserId}`, {
            method: 'DELETE'
        });
        
        if (response.ok) {
            showToast('Cart cleared', 'success');
            loadCart();
            updateCartBadge();
        } else {
            throw new Error('Failed to clear cart');
        }
    } catch (error) {
        showToast('Failed to clear cart: ' + error.message, 'error');
        console.error('Error clearing cart:', error);
    }
}

// Checkout
async function checkout() {
    if (!cartData || !cartData.items || cartData.items.length === 0) {
        showToast('Your cart is empty', 'error');
        return;
    }
    
    try {
        // APM Funnel Tracking: Track checkout initiated event
        console.log('FUNNEL_TRACKING: Checkout initiated');
        await fetch(`${API_BASE_URL}/cart/${currentUserId}/checkout-initiated`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                ...getTrackingHeaders()
            }
        });
        
        const checkoutData = {
            userId: currentUserId,
            items: cartData.items.map(item => ({
                productId: item.productId,
                productName: item.productName,
                price: item.price,
                quantity: item.quantity
            }))
        };
        
        const response = await fetch(`${API_BASE_URL}/orders/checkout`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                ...getTrackingHeaders()
            },
            body: JSON.stringify(checkoutData)
        });
        
        if (response.ok) {
            const order = await response.json();
            console.log('FUNNEL_TRACKING: Checkout completed successfully');
            showOrderConfirmation(order);
            // Note: Cart is now automatically cleared by the backend after successful checkout
            // Reload cart to reflect this
            loadCart();
            updateCartBadge();
        } else {
            console.log('FUNNEL_DROP_OFF: Checkout failed');
            throw new Error('Checkout failed');
        }
    } catch (error) {
        console.log('FUNNEL_DROP_OFF: Checkout error -', error.message);
        showToast('Checkout failed: ' + error.message, 'error');
        console.error('Error during checkout:', error);
    }
}

function showOrderConfirmation(order) {
    const modalBody = document.getElementById('modalBody');
    modalBody.innerHTML = `
        <div style="text-align: center;">
            <div style="font-size: 4rem; margin-bottom: 1rem;">✅</div>
            <h2>Order Confirmed!</h2>
            <p style="color: var(--gray); margin: 1rem 0;">
                Your order #${order.id} has been placed successfully.
            </p>
            <div style="background: var(--light); padding: 1rem; border-radius: 0.5rem; margin: 1rem 0;">
                <strong>Total: $${order.totalAmount.toFixed(2)}</strong>
            </div>
            <button class="btn btn-primary" onclick="closeModal(); switchPage('orders'); document.querySelector('[data-page=orders]').click();">
                View Orders
            </button>
        </div>
    `;
    
    showModal();
}

// Orders
let loadingStartTime = null;
let loadingTimerInterval = null;

async function loadOrders() {
    // Default to user orders
    loadMyOrders();
}

async function loadMyOrders() {
    const loading = document.getElementById('ordersLoading');
    const ordersList = document.getElementById('ordersList');
    const loadingTimer = document.getElementById('loadingTimer');
    
    clearLoadingTimer();
    loading.style.display = 'block';
    ordersList.innerHTML = '';
    loadingTimer.textContent = '';
    
    loadingStartTime = Date.now();
    startLoadingTimer();
    
    try {
        const response = await fetch(`${API_BASE_URL}/orders/user/${currentUserId}`);
        const orders = await response.json();
        
        clearLoadingTimer();
        loading.style.display = 'none';
        
        if (orders.length === 0) {
            ordersList.innerHTML = '<div class="empty-state"><div class="empty-state-icon">📋</div><h3>No orders yet</h3><p>Start shopping to see your orders here!</p></div>';
            return;
        }
        
        // Sort by date (newest first)
        orders.sort((a, b) => new Date(b.orderDate) - new Date(a.orderDate));
        
        orders.forEach(order => {
            const card = createOrderCard(order);
            ordersList.appendChild(card);
        });
        
        const elapsed = ((Date.now() - loadingStartTime) / 1000).toFixed(2);
        showToast(`Loaded ${orders.length} orders in ${elapsed}s ⚡`, 'success');
    } catch (error) {
        clearLoadingTimer();
        loading.style.display = 'none';
        showToast('Failed to load orders: ' + error.message, 'error');
        console.error('Error loading orders:', error);
    }
}

async function loadAllOrders() {
    const loading = document.getElementById('ordersLoading');
    const ordersList = document.getElementById('ordersList');
    const loadingTimer = document.getElementById('loadingTimer');
    
    clearLoadingTimer();
    loading.style.display = 'block';
    ordersList.innerHTML = '';
    loadingTimer.textContent = '';
    
    loadingStartTime = Date.now();
    startLoadingTimer();
    
    try {
        // This endpoint is affected by slow mode
        const response = await fetch(`${API_BASE_URL}/orders`);
        
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }
        
        const data = await response.json();
        
        // Handle both array and wrapped responses
        let orders = Array.isArray(data) ? data : (data.orders || []);
        
        clearLoadingTimer();
        loading.style.display = 'none';
        
        if (!Array.isArray(orders)) {
            console.error('Invalid response format:', data);
            throw new Error('Invalid response format - expected array');
        }
        
        if (orders.length === 0) {
            ordersList.innerHTML = '<div class="empty-state"><div class="empty-state-icon">📋</div><h3>No orders in system</h3><p>No orders have been placed yet.</p></div>';
            return;
        }
        
        // Sort by date (newest first)
        orders.sort((a, b) => new Date(b.orderDate) - new Date(a.orderDate));
        
        orders.forEach(order => {
            const card = createOrderCard(order);
            ordersList.appendChild(card);
        });
    } catch (error) {
        clearLoadingTimer();
        loading.style.display = 'none';
        showToast('Failed to load all orders: ' + error.message, 'error');
        console.error('Error loading all orders:', error);
    }
}

function startLoadingTimer() {
    loadingTimerInterval = setInterval(() => {
        if (loadingStartTime) {
            const elapsed = ((Date.now() - loadingStartTime) / 1000).toFixed(1);
            const loadingTimer = document.getElementById('loadingTimer');
            if (loadingTimer) {
                loadingTimer.textContent = `⏱️ ${elapsed}s`;
                if (elapsed > 2) {
                    loadingTimer.style.color = 'var(--danger)';
                }
            }
        }
    }, 100);
}

function clearLoadingTimer() {
    if (loadingTimerInterval) {
        clearInterval(loadingTimerInterval);
        loadingTimerInterval = null;
    }
    loadingStartTime = null;
}

function createOrderCard(order) {
    const card = document.createElement('div');
    card.className = 'order-card';
    
    const orderDate = new Date(order.orderDate).toLocaleString();
    const statusClass = order.status.toLowerCase();
    
    let itemsHtml = '';
    order.items.forEach(item => {
        const icon = productIcons[item.productName] || productIcons.default;
        itemsHtml += `
            <div class="order-item">
                <div>
                    <span style="font-size: 1.5rem; margin-right: 0.5rem;">${icon}</span>
                    <span class="order-item-name">${item.productName}</span>
                    <span class="order-item-details"> × ${item.quantity}</span>
                </div>
                <div>$${(item.price * item.quantity).toFixed(2)}</div>
            </div>
        `;
    });
    
    card.innerHTML = `
        <div class="order-header">
            <div>
                <div class="order-id">Order #${order.id}</div>
                <div class="order-date">${orderDate}</div>
            </div>
            <div class="order-status ${statusClass}">${order.status}</div>
        </div>
        <div class="order-items">
            ${itemsHtml}
        </div>
        <div class="order-total">
            <span>Total:</span>
            <span>$${order.totalAmount.toFixed(2)}</span>
        </div>
    `;
    
    return card;
}

// Cart Badge
async function updateCartBadge() {
    try {
        const response = await fetch(`${API_BASE_URL}/cart/${currentUserId}`);
        const cart = await response.json();
        
        const badge = document.getElementById('cartBadge');
        const itemCount = cart.items ? cart.items.length : 0;
        badge.textContent = itemCount;
        badge.style.display = itemCount > 0 ? 'flex' : 'none';
    } catch (error) {
        console.error('Error updating cart badge:', error);
    }
}

// Toast Notifications
function showToast(message, type = 'success') {
    const container = document.getElementById('toastContainer');
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    
    const icon = type === 'success' ? '✅' : '❌';
    
    toast.innerHTML = `
        <div class="toast-icon">${icon}</div>
        <div class="toast-message">${message}</div>
    `;
    
    container.appendChild(toast);
    
    setTimeout(() => {
        toast.style.animation = 'slideOut 0.3s ease-out';
        setTimeout(() => {
            container.removeChild(toast);
        }, 300);
    }, 3000);
}

// Modal
function showModal() {
    const modal = document.getElementById('modal');
    modal.classList.add('show');
}

function closeModal() {
    const modal = document.getElementById('modal');
    modal.classList.remove('show');
}

// Event Listeners
document.getElementById('checkoutBtn').addEventListener('click', checkout);
document.getElementById('clearCartBtn').addEventListener('click', clearCart);
document.getElementById('loadMyOrdersBtn').addEventListener('click', loadMyOrders);
document.getElementById('loadAllOrdersBtn').addEventListener('click', loadAllOrders);
document.querySelector('.modal-close').addEventListener('click', closeModal);
document.getElementById('modal').addEventListener('click', (e) => {
    if (e.target.id === 'modal') {
        closeModal();
    }
});

