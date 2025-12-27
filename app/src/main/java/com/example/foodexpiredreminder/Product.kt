package com.example.foodexpiredreminder

import java.io.Serializable
import java.util.UUID

data class Product(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: ProductType,
    val quantity: Int,
    val purchaseDate: Long,
    val expiryDate: Long,
    var status: ProductStatus
): Serializable

enum class ProductType : java.io.Serializable {
    BASAH, KERING, BEKU
}

enum class ProductStatus : java.io.Serializable {
    AMAN, HAMPIR_KADALUWARSA, KADALUWARSA
}
