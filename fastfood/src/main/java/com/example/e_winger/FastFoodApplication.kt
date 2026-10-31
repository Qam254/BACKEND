package com.example.e_winger

import android.app.Application
import com.google.firebase.FirebaseApp

class FastFoodApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Firebase
        // This will automatically initialize all Firebase services including Firestore
        // provided the google-services.json file is present and the plugin is applied.
        FirebaseApp.initializeApp(this)
    }
}
