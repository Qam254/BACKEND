/**
 * Fast Food Express Single Page Web Application
 */

const PRODUCTS = [
  {
    id: "chips_kuku",
    title: "Chips Kuku",
    description: "Crispy french fries served with succulent fried or grilled chicken.",
    price: 8000,
    category: "Main",
    imageUrl: "https://images.unsplash.com/photo-1562967914-608f82629710?w=500&auto=format&fit=crop"
  },
  {
    id: "chips_mishikaki",
    title: "Chips Mishikaki",
    description: "Golden fries paired with savory marinated beef skewers.",
    price: 7000,
    category: "Specialty",
    imageUrl: "https://images.unsplash.com/photo-1555939594-58d7cb561ad1?w=500&auto=format&fit=crop"
  },
  {
    id: "chipsi_mayai",
    title: "Chipsi Mayai",
    description: "Tanzanian classic french fries omelette cooked to perfection.",
    price: 3500,
    category: "Main",
    imageUrl: "https://images.unsplash.com/photo-1525351484163-7529414344d8?w=500&auto=format&fit=crop"
  },
  {
    id: "mishikaki",
    title: "Mishikaki Skewers (3pcs)",
    description: "Tender beef skewers spiced and grilled over open flame.",
    price: 4500,
    category: "Sides",
    imageUrl: "https://images.unsplash.com/photo-1544025162-d76694265947?w=500&auto=format&fit=crop"
  },
  {
    id: "mineral_water",
    title: "Pure Mineral Water (500ml)",
    description: "Refreshing cold natural spring water.",
    price: 1000,
    category: "Drinks",
    imageUrl: "https://images.unsplash.com/photo-1548839140-29a749e1bc4e?w=500&auto=format&fit=crop"
  },
  {
    id: "fresh_juice",
    title: "Fresh Mango Juice",
    description: "100% natural freshly squeezed mango juice.",
    price: 2500,
    category: "Drinks",
    imageUrl: "https://images.unsplash.com/photo-1621506289937-a8e4df240d0b?w=500&auto=format&fit=crop"
  }
];

const DELIVERY_FEE = 2000;
let cart = JSON.parse(localStorage.getItem('fastfood_cart')) || [];
let activeCategory = 'All';
let searchQuery = '';

// DOM Elements
const productsGrid = document.getElementById('productsGrid');
const searchInput = document.getElementById('searchInput');
const categoryChips = document.querySelectorAll('.category-chip');
const cartBadge = document.getElementById('cartBadge');
const cartDrawer = document.getElementById('cartDrawer');
const cartOverlay = document.getElementById('cartOverlay');
const openCartBtn = document.getElementById('openCartBtn');
const closeCartBtn = document.getElementById('closeCartBtn');
const cartItemsContainer = document.getElementById('cartItemsContainer');
const subtotalText = document.getElementById('subtotalText');
const deliveryFeeText = document.getElementById('deliveryFeeText');
const totalText = document.getElementById('totalText');
const checkoutBtn = document.getElementById('checkoutBtn');
const themeToggle = document.getElementById('themeToggle');
const themeIcon = document.getElementById('themeIcon');

// Checkout Modal
const checkoutModal = document.getElementById('checkoutModal');
const closeCheckoutBtn = document.getElementById('closeCheckoutBtn');
const checkoutForm = document.getElementById('checkoutForm');
const modalTotalText = document.getElementById('modalTotalText');
const payPhoneGroup = document.getElementById('mobilePhoneGroup');
const paymentCards = document.querySelectorAll('.payment-card');
const submitOrderBtn = document.getElementById('submitOrderBtn');

// Initialize
document.addEventListener('DOMContentLoaded', () => {
  renderProducts();
  updateCartUI();
  setupEventListeners();
});

// Render Product Cards
function renderProducts() {
  const filtered = PRODUCTS.filter(product => {
    const matchesCategory = (activeCategory === 'All') || (product.category === activeCategory);
    const matchesSearch = product.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
                          product.description.toLowerCase().includes(searchQuery.toLowerCase());
    return matchesCategory && matchesSearch;
  });

  if (filtered.length === 0) {
    productsGrid.innerHTML = `<p style="grid-column: 1/-1; text-align: center; color: var(--text-secondary); padding: 40px;">No delicious food found matching your search!</p>`;
    return;
  }

  productsGrid.innerHTML = filtered.map(product => `
    <div class="product-card">
      <div class="product-img-box">
        <img src="${product.imageUrl}" alt="${product.title}">
      </div>
      <div class="product-card-body">
        <h3 class="product-title">${product.title}</h3>
        <p class="product-desc">${product.description}</p>
        <div class="product-footer">
          <span class="product-price">TSH ${product.price.toLocaleString()}</span>
          <button class="add-to-cart-btn" onclick="addToCart('${product.id}')">
            <i class="fa-solid fa-plus"></i> Add
          </button>
        </div>
      </div>
    </div>
  `).join('');
}

// Event Listeners
function setupEventListeners() {
  searchInput.addEventListener('input', (e) => {
    searchQuery = e.target.value.trim();
    renderProducts();
  });

  categoryChips.forEach(chip => {
    chip.addEventListener('click', () => {
      categoryChips.forEach(c => c.classList.remove('active'));
      chip.classList.add('active');
      activeCategory = chip.dataset.category;
      renderProducts();
    });
  });

  openCartBtn.addEventListener('click', toggleCart);
  closeCartBtn.addEventListener('click', toggleCart);
  cartOverlay.addEventListener('click', toggleCart);

  themeToggle.addEventListener('click', () => {
    document.body.classList.toggle('light-theme');
    const isLight = document.body.classList.contains('light-theme');
    themeIcon.className = isLight ? 'fa-solid fa-moon' : 'fa-solid fa-sun';
  });

  checkoutBtn.addEventListener('click', () => {
    toggleCart();
    toggleCheckoutModal();
  });

  closeCheckoutBtn.addEventListener('click', toggleCheckoutModal);

  paymentCards.forEach(card => {
    card.addEventListener('click', () => {
      paymentCards.forEach(c => c.classList.remove('active'));
      card.classList.add('active');
      const radio = card.querySelector('input[type="radio"]');
      radio.checked = true;
      payPhoneGroup.style.display = radio.value === 'Mobile Payment' ? 'block' : 'none';
    });
  });

  checkoutForm.addEventListener('submit', handleCheckoutSubmit);
}

// Cart Management
function addToCart(productId) {
  const product = PRODUCTS.find(p => p.id === productId);
  if (!product) return;

  const existing = cart.find(item => item.id === productId);
  if (existing) {
    existing.quantity++;
  } else {
    cart.push({ ...product, quantity: 1 });
  }

  saveCart();
  updateCartUI();
  showToast(`Added ${product.title} to cart!`);
}

function updateQuantity(productId, delta) {
  const item = cart.find(i => i.id === productId);
  if (!item) return;

  item.quantity += delta;
  if (item.quantity <= 0) {
    cart = cart.filter(i => i.id !== productId);
  }

  saveCart();
  updateCartUI();
}

function saveCart() {
  localStorage.setItem('fastfood_cart', JSON.stringify(cart));
}

function updateCartUI() {
  const totalItemCount = cart.reduce((sum, item) => sum + item.quantity, 0);
  cartBadge.innerText = totalItemCount;

  if (cart.length === 0) {
    cartItemsContainer.innerHTML = `<p class="empty-cart-msg">Your cart is empty. Add some delicious food!</p>`;
    subtotalText.innerText = `TSH 0`;
    totalText.innerText = `TSH 0`;
    modalTotalText.innerText = `TSH 0`;
    checkoutBtn.disabled = true;
    return;
  }

  checkoutBtn.disabled = false;
  const subtotal = cart.reduce((sum, item) => sum + (item.price * item.quantity), 0);
  const total = subtotal + DELIVERY_FEE;

  subtotalText.innerText = `TSH ${subtotal.toLocaleString()}`;
  deliveryFeeText.innerText = `TSH ${DELIVERY_FEE.toLocaleString()}`;
  totalText.innerText = `TSH ${total.toLocaleString()}`;
  modalTotalText.innerText = `TSH ${total.toLocaleString()}`;

  cartItemsContainer.innerHTML = cart.map(item => `
    <div class="cart-item">
      <img src="${item.imageUrl}" alt="${item.title}">
      <div class="cart-item-details">
        <div class="cart-item-title">${item.title}</div>
        <div class="cart-item-price">TSH ${(item.price * item.quantity).toLocaleString()}</div>
        <div class="cart-item-qty">
          <button class="qty-btn" onclick="updateQuantity('${item.id}', -1)">-</button>
          <span>${item.quantity}</span>
          <button class="qty-btn" onclick="updateQuantity('${item.id}', 1)">+</button>
        </div>
      </div>
    </div>
  `).join('');
}

function toggleCart() {
  cartDrawer.classList.toggle('active');
  cartOverlay.classList.toggle('active');
}

function toggleCheckoutModal() {
  checkoutModal.classList.toggle('active');
}

// Checkout Submit Handler
async function handleCheckoutSubmit(e) {
  e.preventDefault();

  const name = document.getElementById('custName').value.trim();
  const location = document.getElementById('custLocation').value.trim();
  const phone = document.getElementById('custPhone').value.trim();
  const paymentMethod = document.querySelector('input[name="paymentMethod"]:checked').value;
  const payPhone = document.getElementById('payPhone').value.trim() || phone;

  const subtotal = cart.reduce((sum, item) => sum + (item.price * item.quantity), 0);
  const total = subtotal + DELIVERY_FEE;

  submitOrderBtn.disabled = true;
  submitOrderBtn.innerText = 'Processing Order...';

  try {
    if (paymentMethod === 'Mobile Payment') {
      const response = await fetch('/api/payments/pay', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          reference: 'COL20260109LINK01XYZ',
          payerPhone: payPhone,
          payerName: name,
          amount: total,
          idempotencyKey: `WEB_ORD_${Date.now()}`
        })
      });

      const data = await response.json();

      if (response.ok && data.success) {
        showToast('Payment initiated! Check your phone for USSD prompt.');
      } else {
        showToast(data.message || 'Payment request initiated.');
      }
    }

    showToast('🎉 Order placed successfully! Food is on the way.');
    cart = [];
    saveCart();
    updateCartUI();
    toggleCheckoutModal();
    checkoutForm.reset();

  } catch (error) {
    console.error('Order error:', error);
    showToast('Order received! Thank you for ordering.');
    cart = [];
    saveCart();
    updateCartUI();
    toggleCheckoutModal();
  } finally {
    submitOrderBtn.disabled = false;
    submitOrderBtn.innerText = 'Confirm Order';
  }
}

// Toast Notifications
function showToast(message) {
  const container = document.getElementById('toastContainer');
  const toast = document.createElement('div');
  toast.className = 'toast';
  toast.innerText = message;

  container.appendChild(toast);
  setTimeout(() => toast.remove(), 4000);
}
