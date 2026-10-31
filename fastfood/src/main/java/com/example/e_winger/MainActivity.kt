package com.example.e_winger

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.e_winger.ui.navigation.MainScreen
import com.example.e_winger.ui.theme.FastFoodTheme
import com.google.android.libraries.places.api.Places
import com.example.e_winger.BuildConfig
import com.example.e_winger.data.model.seedProductsToFirestore

class MainActivity : AppCompatActivity() {
    
    companion object {
        // Flag to track if the session should be reset (ends on app close)
        var isFreshLaunch = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, BuildConfig.GOOGLE_MAPS_API_KEY)
        }

        // Seeding finished. Products are now managed via Admin Panel.
        // seedProductsToFirestore(this) 

        enableEdgeToEdge()
        setContent {
            val systemInDarkTheme = isSystemInDarkTheme()
            var isDarkTheme by remember { mutableStateOf(systemInDarkTheme) }

            FastFoodTheme(darkTheme = isDarkTheme) {
                MainScreen(
                    isDarkTheme = isDarkTheme,
                    onThemeToggle = { isDarkTheme = !isDarkTheme },
                    isFreshLaunch = isFreshLaunch
                )
                // Once MainScreen has handled the fresh launch, set it to false
                isFreshLaunch = false
            }
        }
    }
}
