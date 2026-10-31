package com.example.e_winger.data.model

import java.util.UUID

data class Order(
    val id: String = UUID.randomUUID().toString(),
    val items: List<CartItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val orderDate: String = "",
    val status: String = "Preparing",
    val customerName: String = "",
    val location: String = "",
    val houseNumber: String = "",
    val phoneNumber: String = ""
)
