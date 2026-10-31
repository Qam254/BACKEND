package com.example.e_winger.ui.navigation

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import java.util.Locale
import com.example.e_winger.R
import com.example.e_winger.data.model.CartItem
import com.example.e_winger.data.model.Order
import com.example.e_winger.data.model.Product
import com.example.e_winger.data.model.User
import com.example.e_winger.ui.auth.LoginScreen
import com.example.e_winger.ui.auth.SignUpScreen
import com.example.e_winger.ui.auth.ResetPasswordScreen
import com.example.e_winger.ui.auth.SplashScreen
import com.example.e_winger.ui.cart.CartScreen
import com.example.e_winger.ui.cart.CheckoutScreen
import com.example.e_winger.ui.home.HomeScreen
import com.example.e_winger.ui.product.ProductDetailScreen
import com.example.e_winger.ui.profile.EditProfileScreen
import com.example.e_winger.ui.profile.MyOrdersScreen
import com.example.e_winger.ui.profile.ProfileScreen
import com.example.e_winger.ui.profile.SecurityScreen
import com.example.e_winger.ui.profile.SettingsScreen
import com.example.e_winger.ui.profile.WishlistScreen
import com.example.e_winger.ui.search.SearchScreen
import com.example.e_winger.ui.notifications.NotificationItem
import com.example.e_winger.ui.notifications.NotificationsScreen
import java.util.Date
import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.core.os.LocaleListCompat
import coil3.compose.AsyncImage
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.rememberCoroutineScope
import com.example.e_winger.data.remote.EasyPayService
import com.example.e_winger.data.remote.SmsService
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit,
    isFreshLaunch: Boolean = false
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }
    val scope = rememberCoroutineScope()
    
    // User state
    var currentUser by remember { 
        mutableStateOf<User?>(null) 
    }
    
    // Welcome message state (consumed by HomeScreen)
    var welcomeMsg by remember { mutableStateOf<String?>(null) }
    
    // Auth loading state
    var isAuthLoading by remember { mutableStateOf(true) }
    
    // Check for existing session
    LaunchedEffect(Unit) {
        if (isFreshLaunch) {
            auth.signOut()
            currentUser = null
            isAuthLoading = false
        } else {
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                db.collection("users")
                    .document(firebaseUser.uid)
                    .get()
                    .addOnSuccessListener { document ->
                        val user = document.toObject(User::class.java)
                        if (user != null) {
                            currentUser = user
                        }
                        isAuthLoading = false
                    }
                    .addOnFailureListener {
                        isAuthLoading = false
                    }
            } else {
                isAuthLoading = false
            }
        }
    }
    
    // Cart state
    val cartItems = remember { mutableStateListOf<CartItem>() }
    val cartItemCount = cartItems.sumOf { it.quantity }
    
    // Orders state
    val orders = remember { mutableStateListOf<Order>() }
    
    // Wishlist state
    val wishlistItems = remember { mutableStateListOf<Product>() }
    
    val notifications = remember { 
        mutableStateListOf(
            NotificationItem(title = "Welcome to Fast Food!", message = "Start ordering your favorite meals today.", time = "Just now"),
            NotificationItem(title = "Hungry?", message = "Get 20% off on your first food order.", time = "1h ago", fullContent = "Welcome to Fast Food! We deliver hot and fresh meals right to your doorstep. Use code FIRSTFOOD for extra discounts.")
        )
    }
    val unreadNotificationCount = notifications.count { !it.isRead }

    val bottomBarScreens = listOf(
        Screen.Home,
        Screen.Search,
        Screen.Cart,
        Screen.Profile
    )

    Scaffold(
        bottomBar = {
            if (bottomBarScreens.any { it.route == currentRoute }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .navigationBarsPadding()
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .border(
                                width = 1.dp,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.5f),
                                        Color.White.copy(alpha = 0.1f)
                                    )
                                ),
                                shape = RoundedCornerShape(24.dp)
                            ),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                        tonalElevation = 8.dp
                    ) {
                        NavigationBar(
                            containerColor = Color.Transparent,
                            tonalElevation = 0.dp
                        ) {
                            bottomBarScreens.forEach { screen ->
                                NavigationBarItem(
                                    icon = {
                                        when (screen) {
                                            Screen.Cart -> {
                                                BadgedBox(
                                                    badge = {
                                                        if (cartItemCount > 0) {
                                                            Badge {
                                                                Text(text = cartItemCount.toString())
                                                            }
                                                        }
                                                    }
                                                ) {
                                                    Icon(screen.icon, contentDescription = screen.title)
                                                }
                                            }
                                            Screen.Profile -> {
                                                BadgedBox(
                                                    badge = {
                                                        if (unreadNotificationCount > 0) {
                                                            Badge {
                                                                Text(text = unreadNotificationCount.toString())
                                                            }
                                                        }
                                                    }
                                                ) {
                                                    if (currentUser != null && !currentUser?.profileImageUrl.isNullOrEmpty()) {
                                                        AsyncImage(
                                                            model = currentUser?.profileImageUrl,
                                                            contentDescription = "Profile",
                                                            modifier = Modifier
                                                                .size(24.dp)
                                                                .clip(CircleShape),
                                                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                                        )
                                                    } else {
                                                        Icon(screen.icon, contentDescription = screen.title)
                                                    }
                                                }
                                            }
                                            else -> {
                                                Icon(screen.icon, contentDescription = screen.title)
                                            }
                                        }
                                    },
                                    label = { Text(screen.title, style = MaterialTheme.typography.labelSmall) },
                                    selected = currentRoute == screen.route,
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                    ),
                                    onClick = {
                                        if (currentRoute != screen.route) {
                                            navController.navigate(screen.route) {
                                                popUpTo(navController.graph.startDestinationId) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(
                top = innerPadding.calculateTopPadding(),
                bottom = 0.dp // Ignore bottom padding to allow glass effect
            )
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(onAnimationFinished = {
                    // Logic for Session: If logged in, go Home. Otherwise, go to Login for best "session" start.
                    // Or we can go Home as guest. Let's stick to Home but ensure data is ready.
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                })
            }
            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = { user ->
                        currentUser = user
                        welcomeMsg = "Welcome back, ${user.name}!"
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onSignUpClick = {
                        navController.navigate(Screen.SignUp.route)
                    },
                    onForgotPasswordClick = {
                        navController.navigate(Screen.ResetPassword.route)
                    }
                )
            }
            composable(Screen.ResetPassword.route) {
                ResetPasswordScreen(
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
            composable(Screen.SignUp.route) {
                SignUpScreen(
                    onSignUpSuccess = { user ->
                        currentUser = user
                        welcomeMsg = "Welcome to Fast Food, ${user.name}!"
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onLoginClick = {
                        navController.popBackStack()
                    }
                )
            }
            composable(Screen.Home.route) {
                HomeScreen(
                    cartItemCount = cartItemCount,
                    unreadNotificationCount = unreadNotificationCount,
                    onProductClick = { productId ->
                        navController.navigate(Screen.ProductDetail.createRoute(productId))
                    },
                    onCartClick = {
                        navController.navigate(Screen.Cart.route)
                    },
                    onNotificationClick = {
                        navController.navigate(Screen.Notifications.route)
                    },
                    onSearchClick = {
                        navController.navigate(Screen.Search.route)
                    },
                    user = currentUser,
                    welcomeMessage = welcomeMsg
                )
                // Clear the message after it's been passed to HomeScreen
                LaunchedEffect(welcomeMsg) {
                    if (welcomeMsg != null) {
                        // We keep it until HomeScreen consumes it in its own LaunchedEffect
                        // Actually, to ensure it only shows once, we can clear it here after a delay
                        // or just let HomeScreen handle the "only on mount" logic.
                        // Let's clear it here after we know it's set.
                        // Since HomeScreen uses LaunchedEffect(welcomeMessage), it will trigger when we arrive.
                        // We can set it to null after a small delay.
                        kotlinx.coroutines.delay(1000)
                        welcomeMsg = null
                    }
                }
            }
            composable(Screen.Search.route) {
                SearchScreen(onProductClick = { productId ->
                    navController.navigate(Screen.ProductDetail.createRoute(productId))
                })
            }
            composable(Screen.Cart.route) {
                CartScreen(
                    cartItems = cartItems,
                    onIncreaseQuantity = { product ->
                        val item = cartItems.find { it.product.id == product.id }
                        item?.let { it.quantity++ }
                        // Trigger recomposition for stateListOf by reassignment or other means if needed
                        // Actually mutableStateListOf items modification doesn't trigger unless item is a state too
                        // For simplicity in this demo, we'll replace the item
                        val index = cartItems.indexOfFirst { it.product.id == product.id }
                        if (index != -1) {
                            val currentItem = cartItems[index]
                            cartItems[index] = currentItem.copy(quantity = currentItem.quantity + 1)
                        }
                    },
                    onDecreaseQuantity = { product ->
                        val index = cartItems.indexOfFirst { it.product.id == product.id }
                        if (index != -1) {
                            val currentItem = cartItems[index]
                            if (currentItem.quantity > 1) {
                                cartItems[index] = currentItem.copy(quantity = currentItem.quantity - 1)
                            }
                        }
                    },
                    onRemoveItem = { product ->
                        cartItems.removeIf { it.product.id == product.id }
                    },
                    onCheckout = {
                        if (currentUser == null) {
                            navController.navigate(Screen.Login.route)
                        } else {
                            navController.navigate(Screen.Checkout.route)
                        }
                    }
                )
            }
            composable(Screen.Checkout.route) {
                val context = LocalContext.current
                val firestore = FirebaseFirestore.getInstance()
                
                CheckoutScreen(
                    cartItems = cartItems,
                    onOrderPlaced = { orderId, orderDate, customerName, location, houseNumber, phoneNumber, paymentPhoneNumber ->
                        val orderTotal = cartItems.sumOf { it.product.price * it.quantity }
                        
                        val newOrder = Order(
                            id = orderId,
                            items = cartItems.toList(),
                            totalAmount = orderTotal,
                            orderDate = orderDate,
                            customerName = customerName,
                            location = location,
                            houseNumber = houseNumber,
                            phoneNumber = phoneNumber
                        )
                        
                        // Add to local orders list for immediate UI update
                        orders.add(0, newOrder)
                        
                        // Add to Firestore "orders" collection
                        val orderMap = hashMapOf(
                            "orderId" to orderId,
                            "orderDate" to orderDate,
                            "timestamp" to System.currentTimeMillis(), // For Admin sorting
                            "totalAmount" to orderTotal,
                            "status" to "Preparing",
                            "customerEmail" to (currentUser?.email ?: "guest@fastfood.com"),
                            "customerName" to customerName,
                            "location" to location,
                            "houseNumber" to houseNumber,
                            "phoneNumber" to phoneNumber,
                            "paymentPhoneNumber" to paymentPhoneNumber, // Added for transaction
                            "items" to cartItems.map { item ->
                                val isSwahili = Locale.getDefault().language == "sw"
                                val productName = if (item.product.nameRes != 0) {
                                    context.resources.getString(item.product.nameRes)
                                } else {
                                    if (isSwahili && !item.product.nameSw.isNullOrEmpty()) item.product.nameSw!! else item.product.name ?: "Unknown"
                                }
                                hashMapOf(
                                    "productId" to item.product.id,
                                    "productName" to productName,
                                    "quantity" to item.quantity,
                                    "price" to item.product.price
                                )
                            }
                        )
                        
                        // Save to Firestore "orders" collection
                        firestore.collection("orders")
                            .document(orderId)
                            .set(orderMap)
                            .addOnSuccessListener {
                                Log.d("Firestore", "Order $orderId saved successfully!")
                                android.widget.Toast.makeText(context, "Order Saved to Cloud!", android.widget.Toast.LENGTH_SHORT).show()
                                
                                // Send SMS Confirmation via Beem
                                scope.launch {
                                    SmsService.sendOrderConfirmation(phoneNumber, orderId, orderTotal)
                                }

                                // Separately try to deduct stock (optional success)
                                cartItems.forEach { item ->
                                    if (item.product.id.isNotEmpty()) {
                                        val productRef = firestore.collection("products").document(item.product.id)
                                        firestore.runTransaction { transaction ->
                                            val snapshot = transaction.get(productRef)
                                            if (snapshot.exists()) {
                                                val currentStock = snapshot.getLong("stockQuantity") ?: 0L
                                                if (currentStock >= item.quantity) {
                                                    transaction.update(productRef, "stockQuantity", currentStock - item.quantity)
                                                }
                                            }
                                            null
                                        }
                                    }
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.e("Firestore", "Failed to save order $orderId", e)
                                android.widget.Toast.makeText(context, "Firestore Error: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                            }
                        
                        notifications.add(0, NotificationItem(
                            title = context.resources.getString(R.string.notif_order_placed_title),
                            message = context.resources.getString(R.string.notif_order_placed_msg, cartItems.size),
                            time = "Just now",
                            fullContent = context.resources.getString(
                                R.string.notif_order_placed_full, 
                                orderId,
                                String.format(Locale.US, "%,.2f", orderTotal)
                            )
                        ))
                    },
                    onComplete = {
                        cartItems.clear()
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    },
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    user = currentUser,
                    isDarkTheme = isDarkTheme,
                    onThemeToggle = onThemeToggle,
                    onSettingsClick = {
                        navController.navigate(Screen.Settings.route)
                    },
                    onMyOrdersClick = {
                        navController.navigate(Screen.MyOrders.route)
                    },
                    onWishlistClick = {
                        navController.navigate(Screen.Wishlist.route)
                    },
                    onSupportClick = {
                        navController.navigate(Screen.Support.route)
                    },
                    onLogout = {
                        auth.signOut()
                        currentUser = null
                        cartItems.clear()
                        orders.clear()
                        wishlistItems.clear()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.MyOrders.route) {
                MyOrdersScreen(
                    orders = orders,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onLanguageChange = { languageCode ->
                        val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(languageCode)
                        AppCompatDelegate.setApplicationLocales(appLocale)
                    },
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onEditProfileClick = {
                        navController.navigate(Screen.EditProfile.route)
                    },
                    onSecurityClick = {
                        navController.navigate(Screen.Security.route)
                    },
                    onNotificationsClick = {
                        navController.navigate(Screen.Notifications.route)
                    },
                    onSupportClick = {
                        navController.navigate(Screen.Support.route)
                    }
                )
            }
            composable(Screen.EditProfile.route) {
                EditProfileScreen(
                    user = currentUser,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onSaveClick = { updatedUser ->
                        currentUser = updatedUser
                        navController.popBackStack()
                    }
                )
            }
            composable(Screen.Security.route) {
                SecurityScreen(onBackClick = {
                    navController.popBackStack()
                })
            }
            composable(Screen.Notifications.route) {
                NotificationsScreen(
                    notifications = notifications,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onNotificationRead = { notificationId ->
                        val index = notifications.indexOfFirst { it.id == notificationId }
                        if (index != -1) {
                            val notification = notifications[index]
                            notifications[index] = notification.copy(isRead = true)
                        }
                    },
                    onMarkAllRead = {
                        notifications.forEachIndexed { index, notification ->
                            if (!notification.isRead) {
                                notifications[index] = notification.copy(isRead = true)
                            }
                        }
                    }
                )
            }
            composable(Screen.Wishlist.route) {
                WishlistScreen(
                    wishlistItems = wishlistItems,
                    onProductClick = { productId ->
                        navController.navigate(Screen.ProductDetail.createRoute(productId))
                    },
                    onRemoveFromWishlist = { product ->
                        wishlistItems.removeIf { it.id == product.id }
                    },
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
            composable(Screen.Support.route) {
                val context = LocalContext.current
                // Simple Support View
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Customer Support") },
                            navigationIcon = {
                                IconButton(onClick = { navController.popBackStack() }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                }
                            }
                        )
                    }
                ) { padding ->
                    Column(
                        modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Need help?", style = MaterialTheme.typography.headlineMedium)
                        Text("Our support team is available 24/7", color = Color.Gray)
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = { 
                            val intent = Intent(Intent.ACTION_DIAL, "tel:0612826255".toUri())
                            context.startActivity(intent)
                        }) {
                            Text("Call Support: 0612826255")
                        }
                    }
                }
            }
            composable(Screen.ProductDetail.route) { backStackEntry ->
                val productId = backStackEntry.arguments?.getString("productId")
                ProductDetailScreen(
                    productId = productId,
                    isFavorite = wishlistItems.any { it.id == productId },
                    onFavoriteToggle = { product, isFav ->
                        if (isFav) {
                            if (!wishlistItems.any { it.id == product.id }) {
                                wishlistItems.add(product)
                            }
                        } else {
                            wishlistItems.removeIf { it.id == product.id }
                        }
                    },
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onAddToCart = { product ->
                        val index = cartItems.indexOfFirst { it.product.id == product.id }
                        if (index != -1) {
                            val currentItem = cartItems[index]
                            cartItems[index] = currentItem.copy(quantity = currentItem.quantity + 1)
                        } else {
                            cartItems.add(CartItem(product))
                        }
                    },
                    onBuyNow = { product ->
                        val index = cartItems.indexOfFirst { it.product.id == product.id }
                        if (index == -1) {
                            cartItems.add(CartItem(product))
                        }
                        if (currentUser == null) {
                            navController.navigate(Screen.Login.route)
                        } else {
                            navController.navigate(Screen.Checkout.route)
                        }
                    }
                )
            }
        }
    }
}
