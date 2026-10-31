package com.example.e_winger.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Splash : Screen("splash", "Splash", Icons.Default.Home)
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Search : Screen("search", "Search", Icons.Default.Search)
    object Cart : Screen("cart", "Cart", Icons.Default.ShoppingCart)
    object Profile : Screen("profile", "Account", Icons.Default.Person)
    
    // Auth screens
    object Login : Screen("login", "Login", Icons.Default.Person)
    object SignUp : Screen("signup", "Sign Up", Icons.Default.Person)
    object ResetPassword : Screen("reset_password", "Reset Password", Icons.Default.Lock)
    object Checkout : Screen("checkout", "Checkout", Icons.Default.ShoppingCart)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    object EditProfile : Screen("edit_profile", "Edit Profile", Icons.Default.Edit)
    object Security : Screen("security", "Security", Icons.Default.Lock)
    object Notifications : Screen("notifications", "Notifications", Icons.Default.Notifications)
    object MyOrders : Screen("my_orders", "My Orders", Icons.AutoMirrored.Filled.List)
    object Wishlist : Screen("wishlist", "Wishlist", Icons.Default.Favorite)
    object Support : Screen("support", "Support", Icons.Default.Call)
    
    // Screens without bottom nav icon
    object ProductDetail : Screen("product_detail/{productId}", "Product Detail", Icons.Default.Info) {
        fun createRoute(productId: String) = "product_detail/$productId"
    }
}
