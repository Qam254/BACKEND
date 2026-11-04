/**
 * Fast Food Express Multi-Page Application Engine
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

// Page Initialization
document.addEventListener('DOMContentLoaded', () => {
  initTheme();
  updateCartBadge();
  initPage();
  setupSharedEvents();
});

function initTheme() {
  const savedTheme = localStorage.getItem('fastfood_theme');
  if (savedTheme === 'light') {
    document.body.classList.add('light-theme');
  }
  const themeIcon = document.getElementById('themeIcon');
  if (themeIcon) {
    themeIcon.className = document.body.classList.contains('light-theme') ? 'fa-solid fa-moon' : 'fa-solid fa-sun';
  }
}

function setupSharedEvents() {
  const themeToggle = document.getElementById('themeToggle');
  if (themeToggle) {
    themeToggle.addEventListener('click', () => {
      document.body.classList.toggle('light-theme');
      const isLight = document.body.classList.contains('light-theme');
      localStorage.setItem('fastfood_theme', isLight ? 'light' : 'dark');
      const themeIcon = document.getElementById('themeIcon');
      if (themeIcon) themeIcon.className = isLight ? 'fa-solid fa-moon' : 'fa-solid fa-sun';
    });
  }

  const openCartBtn = document.getElementById('openCartBtn');
  const closeCartBtn = document.getElementById('closeCartBtn');
  const cartOverlay = document.getElementById('cartOverlay');

  if (openCartBtn && closeCartBtn && cartOverlay) {
    openCartBtn.addEventListener('click', toggleCart);
    closeCartBtn.addEventListener('click', toggleCart);
    cartOverlay.addEventListener('click', toggleCart);
  }
}

function initPage() {
  // Page 1: Home Page
  const featuredGrid = document.getElementById('featuredGrid');
  if (featuredGrid) {
    renderGrid(featuredGrid, PRODUCTS.slice(0, 4));
  }

  // Page 2: Full Menu Page
  const menuGrid = document.getElementById('menuGrid');
  if (menuGrid) {
    renderMenuPage(menuGrid);
  }

  // Page 3: Checkout Page
  const checkoutItemsList = document.getElementById('checkoutItemsList');
  if (checkoutItemsList) {
    renderCheckoutPage();
  }

  // Page 5: Contact Page
  const contactForm = document.getElementById('contactForm');
  if (contactForm) {
    contactForm.addEventListener('submit', (e) => {
      e.preventDefault();
      showToast('Message sent successfully! Support will contact you shortly.');
      contactForm.reset();
    });
  }
}

// Render Featured / Menu Grid
function renderGrid(container, items) {
  if (items.length === 0) {
    container.innerHTML = `<p style="grid-column: 1/-1; text-align: center; color: var(--text-secondary); padding: 40px;">No food items found matching your criteria.</p>`;
    return;
  }

  container.innerHTML = items.map(p => `
    <div class="product-card">
      <div class="product-img-box">
        <img src="${p.imageUrl}" alt="${p.title}">
      </div>
      <div class="product-card-body">
        <h3 class="product-title">${p.title}</h3>
        <p class="product-desc">${p.description}</p>
        <div class="product-footer">
          <span class="product-price">TSH ${p.price.toLocaleString()}</span>
          <button class="add-to-cart-btn" onclick="addToCart('${p.id}')">
            <i class="fa-solid fa-plus"></i> Add
          </button>
        </div>
      </div>
    </div>
  `).join('');
}

function renderMenuPage(container) {
  const searchInput = document.getElementById('searchInput');
  const categoryChips = document.querySelectorAll('.category-chip');

  function updateMenu() {
    const filtered = PRODUCTS.filter(p => {
      const matchCat = (activeCategory === 'All') || (p.category === activeCategory);
      const matchSearch = p.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
                          p.description.toLowerCase().includes(searchQuery.toLowerCase());
      return matchCat && matchSearch;
    });
    renderGrid(container, filtered);
  }

  if (searchInput) {
    searchInput.addEventListener('input', (e) => {
      searchQuery = e.target.value.trim();
      updateMenu();
    });
  }

  categoryChips.forEach(chip => {
    chip.addEventListener('click', () => {
      categoryChips.forEach(c => c.classList.remove('active'));
      chip.classList.add('active');
      activeCategory = chip.dataset.category;
      updateMenu();
    });
  });

  updateMenu();
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
  updateCartBadge();
  updateCartDrawerUI();
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
  updateCartBadge();
  updateCartDrawerUI();

  // If on checkout page, refresh checkout page UI too
  const checkoutItemsList = document.getElementById('checkoutItemsList');
  if (checkoutItemsList) renderCheckoutPage();
}

function saveCart() {
  localStorage.setItem('fastfood_cart', JSON.stringify(cart));
}

function updateCartBadge() {
  const badges = document.querySelectorAll('.cart-badge');
  const count = cart.reduce((sum, i) => sum + i.quantity, 0);
  badges.forEach(b => b.innerText = count);
}

function toggleCart() {
  const cartDrawer = document.getElementById('cartDrawer');
  const cartOverlay = document.getElementById('cartOverlay');
  if (cartDrawer && cartOverlay) {
    cartDrawer.classList.toggle('active');
    cartOverlay.classList.toggle('active');
    updateCartDrawerUI();
  }
}

function updateCartDrawerUI() {
  const container = document.getElementById('cartItemsContainer');
  const subtotalText = document.getElementById('subtotalText');
  const totalText = document.getElementById('totalText');
  if (!container) return;

  if (cart.length === 0) {
    container.innerHTML = `<p class="empty-cart-msg">Your cart is empty.</p>`;
    if (subtotalText) subtotalText.innerText = 'TSH 0';
    if (totalText) totalText.innerText = 'TSH 0';
    return;
  }

  const subtotal = cart.reduce((s, i) => s + (i.price * i.quantity), 0);
  const total = subtotal + DELIVERY_FEE;

  if (subtotalText) subtotalText.innerText = `TSH ${subtotal.toLocaleString()}`;
  if (totalText) totalText.innerText = `TSH ${total.toLocaleString()}`;

  container.innerHTML = cart.map(item => `
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

// Render Checkout Page
function renderCheckoutPage() {
  const checkoutItemsList = document.getElementById('checkoutItemsList');
  const checkoutSubtotal = document.getElementById('checkoutSubtotal');
  const checkoutTotal = document.getElementById('checkoutTotal');
  const checkoutPageForm = document.getElementById('checkoutPageForm');
  const mobilePhoneGroup = document.getElementById('mobilePhoneGroup');
  const cardMobile = document.getElementById('cardMobile');
  const cardCash = document.getElementById('cardCash');

  if (!checkoutItemsList) return;

  if (cart.length === 0) {
    checkoutItemsList.innerHTML = `<p class="empty-cart-msg">Your cart is empty. <a href="menu.html">Go to Menu</a> to add food!</p>`;
    checkoutSubtotal.innerText = 'TSH 0';
    checkoutTotal.innerText = 'TSH 0';
    return;
  }

  const subtotal = cart.reduce((s, i) => s + (i.price * i.quantity), 0);
  const total = subtotal + DELIVERY_FEE;

  checkoutSubtotal.innerText = `TSH ${subtotal.toLocaleString()}`;
  checkoutTotal.innerText = `TSH ${total.toLocaleString()}`;

  checkoutItemsList.innerHTML = cart.map(item => `
    <div class="checkout-item">
      <div>
        <strong>${item.title}</strong> x ${item.quantity}
      </div>
      <div class="product-price">TSH ${(item.price * item.quantity).toLocaleString()}</div>
    </div>
  `).join('');

  if (cardMobile && cardCash) {
    cardMobile.addEventListener('click', () => {
      cardMobile.classList.add('active');
      cardCash.classList.remove('active');
      cardMobile.querySelector('input').checked = true;
      if (mobilePhoneGroup) mobilePhoneGroup.style.display = 'block';
    });

    cardCash.addEventListener('click', () => {
      cardCash.classList.add('active');
      cardMobile.classList.remove('active');
      cardCash.querySelector('input').checked = true;
      if (mobilePhoneGroup) mobilePhoneGroup.style.display = 'none';
    });
  }

  if (checkoutPageForm) {
    checkoutPageForm.addEventListener('submit', async (e) => {
      e.preventDefault();

      if (cart.length === 0) {
        showToast('Your cart is empty!');
        return;
      }

      const name = document.getElementById('custName').value.trim();
      const location = document.getElementById('custLocation').value.trim();
      const phone = document.getElementById('custPhone').value.trim();
      const paymentMethod = document.querySelector('input[name="paymentMethod"]:checked').value;
      const payPhone = document.getElementById('payPhone') ? document.getElementById('payPhone').value.trim() || phone : phone;

      const submitBtn = document.getElementById('submitOrderBtn');
      submitBtn.disabled = true;
      submitBtn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Processing Payment...';

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
          if (data.message) showToast(data.message);
        }

        cart = [];
        saveCart();
        updateCartBadge();
        window.location.href = 'order-success.html';

      } catch (err) {
        console.error('Checkout error:', err);
        cart = [];
        saveCart();
        updateCartBadge();
        window.location.href = 'order-success.html';
      }
    });
  }
}

// Toast Notifications
function showToast(message) {
  const container = document.getElementById('toastContainer');
  if (!container) return;
  const toast = document.createElement('div');
  toast.className = 'toast';
  toast.innerText = message;

  container.appendChild(toast);
  setTimeout(() => toast.remove(), 4000);
}
