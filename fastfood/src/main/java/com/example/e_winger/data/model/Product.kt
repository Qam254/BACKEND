package com.example.e_winger.data.model

import com.example.e_winger.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.PropertyName
import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import java.util.Locale

data class Product(
    val id: String = "",
    
    // Resource IDs for local items (default 0)
    val nameRes: Int = 0,
    val descriptionRes: Int = 0,
    val categoryRes: Int = 0,
    val imageRes: Int = 0,
    
    // Firestore fields (English)
    @get:PropertyName("name") @set:PropertyName("name")
    var name: String? = null,
    
    @get:PropertyName("description") @set:PropertyName("description")
    var description: String? = null,

    // Firestore fields (Swahili - Optional from Admin)
    @get:PropertyName("name_sw") @set:PropertyName("name_sw")
    var nameSw: String? = null,

    @get:PropertyName("description_sw") @set:PropertyName("description_sw")
    var descriptionSw: String? = null,
    
    @get:PropertyName("price") @set:PropertyName("price")
    var price: Double = 0.0,
    
    @get:PropertyName("imageUrl") @set:PropertyName("imageUrl")
    var imageUrl: String? = null,
    
    @get:PropertyName("category") @set:PropertyName("category")
    var category: String? = null,
    
    @get:PropertyName("isAvailable") @set:PropertyName("isAvailable")
    @field:PropertyName("isAvailable")
    var isAvailable: Boolean = true,
    
    @get:PropertyName("stockQuantity") @set:PropertyName("stockQuantity")
    var stockQuantity: Long = 0,
    
    @get:PropertyName("rating") @set:PropertyName("rating")
    var rating: Double = 0.0,
    
    @get:PropertyName("reviewCount") @set:PropertyName("reviewCount")
    var reviewCount: Long = 0
) {
    /**
     * Common food terms dictionary for automatic APK-side translation
     */
    private val translationDict = mapOf(
        "juice" to "juisi",
        "burger" to "baga",
        "chicken" to "kuku",
        "chips" to "viazi",
        "water" to "maji",
        "pizza" to "piza",
        "soda" to "vinywaji baridi",
        "rice" to "wali",
        "meat" to "nyama",
        "fish" to "samaki"
    )

    fun getDisplayImage(): Any {
        return when {
            !imageUrl.isNullOrEmpty() -> imageUrl!!
            imageRes != 0 -> imageRes
            id == "1" -> R.drawable.chips_kuku
            id == "2" -> R.drawable.chipsi_mayai
            id == "3" -> R.drawable.chips
            id == "4" -> R.drawable.chips_mishikaki
            id == "5" -> R.drawable.mishikaki
            id == "6" -> R.drawable.water
            else -> R.drawable.fastfood
        }
    }

    /**
     * Returns the localized name.
     * Uses: 1. String Resource (if local) 
     *       2. Admin Swahili input 
     *       3. APK Dictionary translation 
     *       4. English default
     */
    @Composable
    fun getLocalizedName(): String {
        if (nameRes != 0) return stringResource(nameRes)
        
        val isSwahili = Locale.getDefault().language == "sw"
        if (!isSwahili) return name ?: "Unknown"

        // Check if Admin provided Swahili
        if (!nameSw.isNullOrEmpty()) return nameSw!!

        // Automatic APK Dictionary Translation
        val englishName = name?.lowercase() ?: ""
        translationDict.forEach { (eng, swa) ->
            if (englishName.contains(eng)) return englishName.replace(eng, swa).replaceFirstChar { it.uppercase() }
        }

        return name ?: "Unknown"
    }

    /**
     * Returns the localized description.
     */
    @Composable
    fun getLocalizedDescription(): String {
        if (descriptionRes != 0) return stringResource(descriptionRes)
        val isSwahili = Locale.getDefault().language == "sw"
        return if (isSwahili && !descriptionSw.isNullOrEmpty()) descriptionSw!! else description ?: ""
    }
}

val sampleProducts = listOf(
    Product("1", R.string.chips_name, R.string.chips_desc, imageRes = R.drawable.chips_kuku, categoryRes = R.string.cat_main, rating = 4.8, reviewCount = 1250, stockQuantity = 50, price = 6000.0),
    Product("2", R.string.chips_yai_name, R.string.chips_yai_desc, imageRes = R.drawable.chipsi_mayai, categoryRes = R.string.cat_specialty, rating = 4.7, reviewCount = 850, stockQuantity = 30, price = 5000.0),
    Product("3", R.string.chips_regular_name, R.string.chips_regular_desc, imageRes = R.drawable.chips, categoryRes = R.string.cat_sides, rating = 4.9, reviewCount = 2100, stockQuantity = 100, price = 2500.0),
    Product("4", R.string.chips_mshikaki_name, R.string.chips_mshikaki_desc, imageRes = R.drawable.chips_mishikaki, categoryRes = R.string.cat_main, rating = 4.6, reviewCount = 1500, stockQuantity = 25, price = 8000.0),
    Product("5", R.string.mshikaki_name, R.string.mshikaki_desc, imageRes = R.drawable.mishikaki, categoryRes = R.string.cat_main, rating = 4.5, reviewCount = 450, stockQuantity = 40, price = 5000.0),
    Product("6", R.string.water_name, R.string.water_desc, imageRes = R.drawable.water, categoryRes = R.string.cat_drinks, rating = 4.4, reviewCount = 320, stockQuantity = 200, price = 1000.0)
)

fun seedProductsToFirestore(context: Context) {
    val db = FirebaseFirestore.getInstance()
    val collection = db.collection("products")
    
    sampleProducts.forEach { product ->
        val productMap = hashMapOf(
            "id" to product.id,
            "name" to context.getString(product.nameRes),
            "description" to context.getString(product.descriptionRes),
            "price" to product.price,
            "rating" to product.rating,
            "reviewCount" to product.reviewCount,
            "category" to context.getString(product.categoryRes),
            "stockQuantity" to product.stockQuantity,
            "isAvailable" to true
        )
        
        collection.document(product.id).set(productMap)
            .addOnSuccessListener { Log.d("Firestore", "Product ${product.id} seeded") }
            .addOnFailureListener { e -> Log.e("Firestore", "Error seeding product", e) }
    }
}
