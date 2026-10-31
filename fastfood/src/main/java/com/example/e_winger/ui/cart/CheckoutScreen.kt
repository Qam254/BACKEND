package com.example.e_winger.ui.cart

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import android.util.Log
import android.widget.Toast
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.os.Build
import android.os.Environment
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import kotlin.time.Duration.Companion.milliseconds
import com.example.e_winger.R
import com.example.e_winger.data.model.CartItem
import com.example.e_winger.data.remote.EasyPayService
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import java.text.SimpleDateFormat
import java.util.Date
import java.util.UUID
import java.io.File

// Backup list for Dar es Salaam locations
val darLocationsBackup = listOf(
    "Posta", "Kariakoo", "Masaki", "Oysterbay", "Mikocheni A", "Mikocheni B", "Mwananyamala", "Msasani", "Magomeni", "Hanna Nassif", "Kijitonyama", "Makumbusho", "Kawe", "Kawe Kanisani", "Kunduchi", "Bunju A", "Bunju B", "Mbweni", "Wazo", "Ndungumbi", "Mzimuni", "Tandale", "Kigogo", "Mbezi Juu", "Mbezi Beach", "Tegeta", "Boko", "Salasala", "Namanga", "Ada Estate", "Ununio",
    "Ilala", "Upanga East", "Upanga West", "Buguruni", "Tabata", "Tabata Kisukuru", "Tabata Segerea", "Tabata Dampo", "Tabata Kimanga", "Segerea", "Ukonga", "Gerezani", "Kisutu", "Kivukoni", "Jangwani", "Mchafukoge", "Vingunguti", "Kipawa", "Pugu", "Chanika", "Gongo la Mboto", "Kitunda", "Kivule", "Kipunguni", "Majohe", "Msongola", "Bonyokwa", "Banana", "Airwing",
    "Ubungo", "Sinza Mori", "Sinza Palestina", "Sinza Afrika Sana", "Sinza Madukani", "Sinza Mapambano", "Mbezi Luis", "Mbezi Magufully", "Kimara", "Kimara Temboni", "Kimara Stopover", "Kimara Bucha", "Kimara Korogwe", "Manzese", "Mburahati", "Mabibo", "Makuburi", "Saranga", "Goba", "Kwembe", "Msigani", "Kibamba", "Kinyerezi",
    "Temeke", "Buza", "Buza Kanisani", "Buza Malela", "Kongowe", "Mbagala Zakhem", "Mbagala Rangitatu", "Mbagala Majimatitu", "Mbagala Kuu", "Mbagala Mission", "Mbagala Kiburugwa", "Chamazi", "Dovya", "Mbande", "Kurasini", "Chang'ombe", "Yombo Vituka", "Yombo Kilakala", "Yombo Dovya", "Mtoni Kwa Azis Ali", "Mtoni Temeke", "Keko", "Sandali", "Miburani", "Azimio", "Charambe", "Kilungule", "Makangarawe", "Mianzini", "Tandika", "Kisemvule", "Magurumbasi", "Saba Saba",
    "Kigamboni", "Ferry", "Cheka", "Kibada", "Mjimwema", "Kijichi", "Toangoma", "Ungindoni", "Somangila", "Kisarawe II", "Kimbiji", "Pemba Mnazi", "Vijibweni", "Tungi", "Mwasonga", "Amani Gomu", "Gezaulole", "Maweni", "Buyuni", "Mvuti",
    "Ali Hassan Mwinyi Road", "Sam Nujoma Road", "Morogoro Road", "Kilwa Road", "Nelson Mandela Road", "Bagamoyo Road", "Kawawa Road", "Haile Selassie Road", "Chwaaka Street", "Indira Gandhi Street", "Zanaki Street", "Msimbazi Street", "Nyerere Road", "Old Bagamoyo Road", "Kimweri Avenue",
    "Muhimbili National Hospital", "Temeke Hospital", "Mwananyamala Hospital", "Amana Hospital", "JKCI (Jakaya Kikwete Cardiac Institute)", "Ocean Road Hospital", "Mloganzila Hospital", "Regency Medical Centre", "TMJ Hospital", "Kairuki Hospital",
    "University of Dar es Salaam (UDSM)", "MUHAS", "IFM", "TIA", "DIT", "CBE", "Open University",
    "Julius Nyerere International Airport (JNIA)", "Tazara Station", "Kamata", "Magufuli Bus Terminal", "Ubungo Terminal",
    "Mlimani City", "Quality Center", "Diamond Plaza", "Aura Mall", "Palm Village", "Shoppers Plaza"
).asSequence().distinct().sorted().toList()

@Serializable
data class MapboxPrediction(val place_name: String)

@Serializable
data class MapboxResponse(val features: List<MapboxPrediction>)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    cartItems: List<CartItem>,
    onOrderPlaced: (String, String, String, String, String, String, String) -> Unit,
    onComplete: () -> Unit,
    onBackClick: () -> Unit,
) {
    var isOrderSuccess by remember { mutableStateOf(value = false) }
    val total = cartItems.sumOf { it.product.price * it.quantity }
    
    // Automatically generate order details when screen is loaded
    val orderId = remember { UUID.randomUUID().toString().take(8).uppercase() }
    val orderDate = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.US).format(Date()) }

    // Form state
    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var streetName by remember { mutableStateOf("") }
    var houseNumber by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    
    // Autocomplete state
    var areaExpanded by remember { mutableStateOf(false) }
    var areaPredictions by remember { mutableStateOf<List<String>>(emptyList()) }
    var streetExpanded by remember { mutableStateOf(false) }
    var streetPredictions by remember { mutableStateOf<List<String>>(emptyList()) }
    
    val context = LocalContext.current
    val mapboxToken = stringResource(R.string.mapbox_access_token)
    
    val client = remember {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }
    
    val scope = rememberCoroutineScope()

    var searchJob by remember { mutableStateOf<Job?>(null) }

    fun searchLocation(query: String, isArea: Boolean) {
        searchJob?.cancel()
        if (query.length < 2) {
            if (isArea) areaPredictions = emptyList() else streetPredictions = emptyList()
            return
        }
        
        searchJob = scope.launch {
            delay(300.milliseconds) // Faster response
            
            try {
                // Mapbox Geocoding API refined for Dar es Salaam
                val types = if (isArea) "neighborhood,locality,place" else "address,poi"
                
                val url = "https://api.mapbox.com/geocoding/v5/mapbox.places/${Uri.encode(query)}.json" +
                        "?access_token=$mapboxToken" +
                        "&bbox=39.0,-7.1,39.5,-6.6" + // Focused Dar bbox
                        "&proximity=39.2900,-6.8161" + // Center at Posta
                        "&limit=8" +
                        "&types=$types" +
                        "&language=sw,en"

                val response: MapboxResponse = client.get(url).body()
                val apiResults = response.features.map { it.place_name.removeSuffix(", Tanzania") }
                
                val localResults = darLocationsBackup.filter { it.contains(query, ignoreCase = true) }
                val combinedResults = (apiResults + localResults).asSequence().distinct().take(10).toList()
                
                if (isArea) {
                    areaPredictions = combinedResults
                    areaExpanded = areaPredictions.isNotEmpty()
                } else {
                    streetPredictions = combinedResults
                    streetExpanded = streetPredictions.isNotEmpty()
                }
            } catch (e: Exception) {
                Log.e("Mapbox", "Geocoding error", e)
                val results = darLocationsBackup.filter { it.contains(query, ignoreCase = true) }.take(10)
                if (isArea) {
                    areaPredictions = results
                    areaExpanded = areaPredictions.isNotEmpty()
                } else {
                    streetPredictions = results
                    streetExpanded = streetPredictions.isNotEmpty()
                }
            }
        }
    }

    // Payment state
    var selectedPaymentMethod by remember { mutableStateOf("Mobile Payment") }
    var paymentPhoneNumber by remember { mutableStateOf("") }
    var easyPaySecretKey by remember { mutableStateOf(EasyPayService.secretKey) }
    var isProcessingPayment by remember { mutableStateOf(false) }

    Crossfade(targetState = isOrderSuccess, animationSpec = tween(600)) { success ->
        if (success) {
            OrderSuccessView(
                orderId = orderId,
                orderDate = orderDate,
                cartItems = cartItems,
                total = total,
                customerName = name,
                customerPhone = phoneNumber,
                location = location,
                houseNumber = houseNumber,
                onReturnHome = onComplete
            )
        } else {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(stringResource(R.string.finalize_order), fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(onClick = onBackClick) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background
                        )
                    )
                },
                bottomBar = {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding(),
                        shadowElevation = 16.dp,
                        tonalElevation = 4.dp,
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                    ) {
                        Button(
                            onClick = { 
                                if (name.isNotBlank() && location.isNotBlank() && phoneNumber.isNotBlank() && !isProcessingPayment) {
                                    if (selectedPaymentMethod == "Mobile Payment") {
                                        if (paymentPhoneNumber.isBlank()) {
                                            Toast.makeText(context, "Please enter payment phone number", Toast.LENGTH_SHORT).show()
                                        } else {
                                            isProcessingPayment = true
                                            scope.launch {
                                                val uniqueAttemptId = System.currentTimeMillis()
                                                val result = EasyPayService.processPayment(
                                                    reference = EasyPayService.defaultReference,
                                                    payerPhone = paymentPhoneNumber,
                                                    payerName = name,
                                                    amount = total,
                                                    idempotencyKey = "inv-$orderId-$uniqueAttemptId"
                                                )
                                                isProcessingPayment = false
                                                if (result.success) {
                                                    Toast.makeText(context, "Payment verified & initiated!", Toast.LENGTH_SHORT).show()
                                                    onOrderPlaced(orderId, orderDate, name, location, houseNumber, phoneNumber, paymentPhoneNumber)
                                                    isOrderSuccess = true
                                                } else {
                                                    val errorMsg = result.message ?: "Payment verification failed. Please try again."
                                                    Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                                                }
                                            }
                                        }
                                    } else {
                                        // Cash on Delivery
                                        onOrderPlaced(orderId, orderDate, name, location, houseNumber, phoneNumber, paymentPhoneNumber)
                                        isOrderSuccess = true
                                    }
                                }
                            },
                            enabled = name.isNotBlank() && location.isNotBlank() && phoneNumber.isNotBlank() && !isProcessingPayment,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            if (isProcessingPayment) {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.5.dp
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text("Verifying Payment...", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Text(
                                    "${stringResource(R.string.place_order)} (TSH ${String.format(Locale.US, "%,.2f", total)})", 
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Delivery Details Form
                    CheckoutSection(
                        title = stringResource(R.string.delivery_details),
                        icon = Icons.Default.LocalShipping
                    ) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text(stringResource(R.string.full_name)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Location Area with API Search
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = location,
                                onValueChange = { 
                                    location = it
                                    searchLocation(it, true)
                                },
                                placeholder = { Text(stringResource(R.string.location_area)) },
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
                                    unfocusedContainerColor = Color.Gray.copy(alpha = 0.05f),
                                    focusedContainerColor = Color.Transparent
                                ),
                                singleLine = true
                            )
                            
                            DropdownMenu(
                                expanded = areaExpanded,
                                onDismissRequest = { areaExpanded = false },
                                properties = PopupProperties(focusable = false),
                                modifier = Modifier
                                    .fillMaxWidth(0.9f)
                                    .background(MaterialTheme.colorScheme.surface)
                            ) {
                                areaPredictions.forEach { area ->
                                    val parts = area.split(", ", limit = 2)
                                    val mainText = parts[0]
                                    val subText = if (parts.size > 1) parts[1] else "Dar es Salaam"

                                    DropdownMenuItem(
                                        text = { 
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Surface(
                                                    shape = CircleShape,
                                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                                    modifier = Modifier.size(36.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Icon(
                                                            Icons.Default.LocationOn, 
                                                            contentDescription = null, 
                                                            modifier = Modifier.size(20.dp),
                                                            tint = MaterialTheme.colorScheme.primary
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.width(16.dp))
                                                Column {
                                                    Text(
                                                        text = mainText,
                                                        style = MaterialTheme.typography.bodyLarge,
                                                        fontWeight = FontWeight.Bold,
                                                        maxLines = 1,
                                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                                    )
                                                    Text(
                                                        text = subText,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = Color.Gray,
                                                        maxLines = 1,
                                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                        },
                                        onClick = {
                                            location = mainText
                                            areaExpanded = false
                                        }
                                    )
                                    if (area != areaPredictions.last()) {
                                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.LightGray.copy(alpha = 0.3f))
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))

                        // Street Name with API Search
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = streetName,
                                onValueChange = { 
                                    streetName = it
                                    searchLocation(it, false)
                                },
                                placeholder = { Text(stringResource(R.string.street_landmark)) },
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = { Icon(Icons.Default.Map, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) },
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.secondary,
                                    unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
                                    unfocusedContainerColor = Color.Gray.copy(alpha = 0.05f),
                                    focusedContainerColor = Color.Transparent
                                ),
                                singleLine = true
                            )
                            
                            DropdownMenu(
                                expanded = streetExpanded,
                                onDismissRequest = { streetExpanded = false },
                                properties = PopupProperties(focusable = false),
                                modifier = Modifier
                                    .fillMaxWidth(0.9f)
                                    .background(MaterialTheme.colorScheme.surface)
                            ) {
                                streetPredictions.forEach { street ->
                                    val parts = street.split(", ", limit = 2)
                                    val mainText = parts[0]
                                    val subText = if (parts.size > 1) parts[1] else "Dar es Salaam"

                                    DropdownMenuItem(
                                        text = { 
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Surface(
                                                    shape = CircleShape,
                                                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                                                    modifier = Modifier.size(36.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Icon(
                                                            Icons.Default.AddLocation, 
                                                            contentDescription = null, 
                                                            modifier = Modifier.size(20.dp),
                                                            tint = MaterialTheme.colorScheme.secondary
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.width(16.dp))
                                                Column {
                                                    Text(
                                                        text = mainText,
                                                        style = MaterialTheme.typography.bodyLarge,
                                                        fontWeight = FontWeight.Bold,
                                                        maxLines = 1,
                                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                                    )
                                                    Text(
                                                        text = subText,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = Color.Gray,
                                                        maxLines = 1,
                                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                        },
                                        onClick = {
                                            streetName = mainText
                                            streetExpanded = false
                                        }
                                    )
                                    if (street != streetPredictions.last()) {
                                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.LightGray.copy(alpha = 0.3f))
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = houseNumber,
                            onValueChange = { houseNumber = it },
                            label = { Text(stringResource(R.string.house_unit)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { phoneNumber = it },
                            label = { Text(stringResource(R.string.phone_number)) },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true
                        )
                    }

                    // Order Summary
                    CheckoutSection(
                        title = stringResource(R.string.order_summary),
                        icon = Icons.Default.ShoppingBag
                    ) {
                        cartItems.forEach { item ->
                            val productName = item.product.getLocalizedName()
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "$productName x${item.quantity}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    "TSH ${String.format(Locale.US, "%,.2f", item.product.price * item.quantity)}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(stringResource(R.string.total_amount), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text(
                                "TSH ${String.format(Locale.US, "%,.2f", total)}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Payment Method
                    CheckoutSection(
                        title = stringResource(R.string.payment_method),
                        icon = Icons.Default.Payments
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = selectedPaymentMethod == "Mobile Payment",
                                    onClick = { selectedPaymentMethod = "Mobile Payment" }
                                )
                                Text(stringResource(R.string.mobile_payment))
                            }
                            
                            AnimatedVisibility(visible = selectedPaymentMethod == "Mobile Payment") {
                                Column(modifier = Modifier.padding(start = 32.dp, bottom = 8.dp)) {
                                    OutlinedTextField(
                                        value = paymentPhoneNumber,
                                        onValueChange = { paymentPhoneNumber = it },
                                        label = { Text("Transaction Phone Number") },
                                        modifier = Modifier.fillMaxWidth(),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                        singleLine = true,
                                        placeholder = { Text("e.g. 0712345678") }
                                    )
                                    Text(
                                        text = "Enter the number for M-Pesa / Tigo Pesa / Airtel Money",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray,
                                        modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                                    )

                                    OutlinedTextField(
                                        value = easyPaySecretKey,
                                        onValueChange = { 
                                            easyPaySecretKey = it
                                            EasyPayService.secretKey = it.trim()
                                        },
                                        label = { Text("EasyPay Secret Key (ep_sk_...)") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        placeholder = { Text("Enter matching ep_sk_ key") }
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = selectedPaymentMethod == "Cash on Delivery",
                                    onClick = { selectedPaymentMethod = "Cash on Delivery" }
                                )
                                Text(stringResource(R.string.cash_on_delivery))
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun CheckoutSection(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    icon, 
                    contentDescription = null, 
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            content()
        }
    }
}

@Composable
fun OrderSuccessView(
    orderId: String,
    orderDate: String,
    cartItems: List<CartItem>,
    total: Double,
    customerName: String,
    customerPhone: String,
    location: String,
    houseNumber: String,
    onReturnHome: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var startAnimation by remember { mutableStateOf(value = false) }
    var showReceipt by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.5f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "scale"
    )
    val opacity by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(1000),
        label = "opacity"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Upper background color area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.45f)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        )
                    ),
                    shape = RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp)
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .alpha(opacity)
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // Animated Checkbox Circle
            Surface(
                shape = CircleShape,
                color = Color.White,
                modifier = Modifier
                    .size(100.dp)
                    .scale(scale),
                shadowElevation = 8.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(60.dp),
                        tint = Color(0xFF4CAF50)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                "Agizo Limetumwa!", // Swahili for Order Placed
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                "Chakula chako kinaandaliwa sasa.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Order Success Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(scale),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Order Summary",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "#$orderId",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "SUCCESS",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color.LightGray.copy(alpha = 0.3f))
                    
                    ReceiptDetailRow("Total Paid", "TSH ${String.format(Locale.US, "%,.2f", total)}")
                    ReceiptDetailRow("Date", orderDate.substringBefore(" "))
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Delivery Details",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$customerName\n$location${if(houseNumber.isNotBlank()) ", $houseNumber" else ""}\n$customerPhone",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.DarkGray,
                        lineHeight = 18.sp
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Quick Action Buttons inside card
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showReceipt = true },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("View", style = MaterialTheme.typography.labelLarge)
                        }
                        
                        Button(
                            onClick = {
                                val pdfFile = generateReceiptPdf(context, orderId, orderDate, cartItems, total, customerName, customerPhone, true)
                                pdfFile?.let { shareReceipt(context, it) }
                            },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Bottom Buttons
            Button(
                onClick = onReturnHome,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Return to Home", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = { 
                    scope.launch {
                        generateReceiptPdf(context, orderId, orderDate, cartItems, total, customerName, customerPhone, false)
                        Toast.makeText(context, "Risiti imehifadhiwa!", Toast.LENGTH_LONG).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save PDF Receipt", fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    if (showReceipt) {
        ReceiptDialog(
            orderId = orderId,
            orderDate = orderDate,
            cartItems = cartItems,
            total = total,
            customerName = customerName,
            customerPhone = customerPhone,
            onDismiss = { showReceipt = false }
        )
    }
}

@Composable
fun ReceiptDialog(
    orderId: String,
    orderDate: String,
    cartItems: List<CartItem>,
    total: Double,
    customerName: String,
    customerPhone: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Application Logo in Receipt
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = R.drawable.fastfood),
                        contentDescription = null,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.order_receipt),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                
                ReceiptDetailRow(stringResource(R.string.order_id), "#$orderId")
                ReceiptDetailRow(stringResource(R.string.date), orderDate)
                
                Spacer(modifier = Modifier.height(16.dp))

                // Customer Details Section
                Text(
                    text = "Customer Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                ReceiptDetailRow("Name", customerName)
                ReceiptDetailRow("Phone", customerPhone)

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.items),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                cartItems.forEach { item ->
                    val productName = item.product.getLocalizedName()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "$productName x${item.quantity}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "TSH ${String.format(Locale.US, "%,.2f", item.product.price * item.quantity)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.total),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "TSH ${String.format(Locale.US, "%,.2f", total)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.close))
                }
            }
        }
    }
}

@Composable
fun ReceiptDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
        Text(text = value, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.End)
    }
}

private fun shareReceipt(context: android.content.Context, file: File) {
    val uri = FileProvider.getUriForFile(context, "com.example.e_winger.fileprovider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Share Receipt via"))
}

private fun generateReceiptPdf(
    context: android.content.Context,
    orderId: String,
    orderDate: String,
    cartItems: List<CartItem>,
    total: Double,
    customerName: String,
    customerPhone: String,
    forSharing: Boolean = false
): File? {
    val pdfDocument = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(300, 800, 1).create()
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas
    val paint = Paint()

    var y = 40f
    
    // Draw Logo
    try {
        val logoBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.fastfood)
        val scaledBitmap = Bitmap.createScaledBitmap(logoBitmap, 60, 60, false)
        canvas.drawBitmap(scaledBitmap, 120f, y, paint)
        y += 80f
    } catch (e: Exception) {
        y += 20f
    }

    // Header
    paint.textAlign = Paint.Align.CENTER
    paint.textSize = 18f
    paint.isFakeBoldText = true
    canvas.drawText("FAST FOOD RECEIPT", 150f, y, paint)
    
    y += 40f
    paint.textAlign = Paint.Align.LEFT
    paint.textSize = 12f
    paint.isFakeBoldText = false
    canvas.drawText("Order ID: #$orderId", 20f, y, paint)
    y += 20f
    canvas.drawText("Date: $orderDate", 20f, y, paint)
    y += 20f
    canvas.drawLine(20f, y, 280f, y, paint)
    
    // Customer
    y += 30f
    paint.isFakeBoldText = true
    canvas.drawText("Customer Information", 20f, y, paint)
    paint.isFakeBoldText = false
    y += 20f
    canvas.drawText("Name: $customerName", 20f, y, paint)
    y += 20f
    canvas.drawText("Phone: $customerPhone", 20f, y, paint)
    y += 20f
    canvas.drawLine(20f, y, 280f, y, paint)

    // Items
    y += 30f
    paint.isFakeBoldText = true
    canvas.drawText("Items", 20f, y, paint)
    paint.isFakeBoldText = false
    y += 20f
    cartItems.forEach { item ->
        // Note: For PDF generation, we don't have Composable context.
        // We'll use a manual check for now or the default name.
        val isSwahili = Locale.getDefault().language == "sw"
        val name = if (item.product.nameRes != 0) {
            context.getString(item.product.nameRes)
        } else {
            if (isSwahili && !item.product.nameSw.isNullOrEmpty()) item.product.nameSw!! else item.product.name ?: "Unknown"
        }
        val price = "TSH ${String.format(Locale.US, "%,.2f", item.product.price * item.quantity)}"
        canvas.drawText("$name x${item.quantity}", 20f, y, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(price, 280f, y, paint)
        paint.textAlign = Paint.Align.LEFT
        y += 20f
    }
    y += 10f
    canvas.drawLine(20f, y, 280f, y, paint)
    y += 30f
    paint.textSize = 14f
    paint.isFakeBoldText = true
    canvas.drawText("Total:", 20f, y, paint)
    paint.textAlign = Paint.Align.RIGHT
    canvas.drawText("TSH ${String.format(Locale.US, "%,.2f", total)}", 280f, y, paint)
    y += 40f
    paint.textAlign = Paint.Align.CENTER
    paint.textSize = 10f
    paint.isFakeBoldText = false
    canvas.drawText("Thank you for choosing Fast Food!", 150f, y, paint)

    pdfDocument.finishPage(page)
    val fileName = "FastFood_Receipt_$orderId.pdf"
    var resultFile: File? = null

    try {
        if (forSharing) {
            val cachePath = File(context.cacheDir, "receipts")
            cachePath.mkdirs()
            val file = File(cachePath, fileName)
            pdfDocument.writeTo(java.io.FileOutputStream(file))
            resultFile = file
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                uri?.let { context.contentResolver.openOutputStream(it)?.use { pdfDocument.writeTo(it) } }
            } else {
                val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
                pdfDocument.writeTo(java.io.FileOutputStream(file))
            }
        }
    } catch (e: Exception) { e.printStackTrace() } finally { pdfDocument.close() }
    return resultFile
}
