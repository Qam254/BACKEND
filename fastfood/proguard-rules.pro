# General rules to protect your APK from reverse engineering

# Obfuscate the code (rename classes, methods, and fields to meaningless names)
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Firebase and Google Play Services rules
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# Ktor rules (used for API calls)
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# Kotlin Serialization rules
-keepattributes *Annotation*, EnclosingMethod, InnerClasses
-keep class kotlinx.serialization.json.** { *; }
-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}

# Coil rules (Image loading)
-keep class coil3.** { *; }
-dontwarn coil3.**

# Jetpack Compose and Material3 rules
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Maintain your data models (prevent them from being renamed so Firestore/Serialization works)
-keep class com.example.e_winger.data.model.** { *; }
