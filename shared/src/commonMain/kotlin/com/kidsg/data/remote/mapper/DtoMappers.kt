package com.kidsg.data.remote.mapper

import com.kidsg.data.remote.dto.*
import com.kidsg.domain.model.*

fun ProductDto.toDomain(): Product {
    return Product(
        id = id,
        name = name,
        brand = brand,
        price = price,
        mrp = mrp,
        rating = 4.8,
        reviewCount = 145,
        categoryId = categoryId,
        description = description,
        imageUrl = imageUrl,
        specifications = specs,
        variants = if (specs.containsKey("Ruling")) listOf("Single Line", "Four Line", "Square Grid") else listOf("Standard"),
        isAvailable = isActive && stock > 0,
        stockQuantity = stock,
        isBestSeller = isFeatured,
        tags = listOf(brand, categoryName ?: "Stationery"),
        intentModes = listOf("School", "Exam")
    )
}

fun CategoryDto.toDomain(): Category {
    return Category(
        id = id,
        name = name,
        slug = slug,
        description = "School supplies for classes",
        iconName = iconName,
        accentHex = "#FF7A00"
    )
}

fun StoreDto.toDomain(): Store {
    return Store(
        id = id,
        name = name,
        locality = address,
        distanceKm = distanceKm,
        rating = rating,
        prepTimeMinutes = estimatedDeliveryMinutes,
        address = "$address, $city",
        isOpen = isOpen
    )
}

fun AddressDto.toDomain(): Address {
    return Address(
        id = id,
        label = label,
        recipientName = name,
        phoneNumber = phone,
        addressLine1 = addressLine1,
        addressLine2 = addressLine2,
        city = city,
        pincode = postalCode,
        deliveryInstructions = deliveryInstructions ?: "",
        isDefault = isDefault
    )
}

fun Address.toDto(): AddressDto {
    return AddressDto(
        id = id,
        userId = "",
        label = label,
        name = recipientName,
        phone = phoneNumber,
        addressLine1 = addressLine1,
        addressLine2 = addressLine2,
        city = city,
        postalCode = pincode,
        deliveryInstructions = deliveryInstructions,
        isDefault = isDefault
    )
}

fun CartItemDto.toDomain(): CartItem {
    val prod = product?.toDomain() ?: Product(
        id = productId,
        name = "Stationery Item",
        brand = "KidsG",
        price = 50.0,
        mrp = 60.0,
        categoryId = "cat_general",
        description = "",
        imageUrl = ""
    )
    return CartItem(
        product = prod,
        quantity = quantity,
        selectedVariant = selectedVariant
    )
}

fun OrderDto.toDomain(): Order {
    val domainStatus = try {
        OrderStatus.valueOf(status)
    } catch (_: Exception) {
        OrderStatus.CONFIRMED
    }

    val domainStore = Store(
        id = storeId,
        name = storeName,
        locality = "Local Partner Store",
        distanceKm = 0.8,
        rating = 4.8,
        prepTimeMinutes = 12,
        address = "Bangalore"
    )

    return Order(
        id = id,
        displayOrderId = orderNumber,
        createdAtEpochMs = 1718000000000L,
        status = domainStatus,
        items = items.map { item ->
            CartItem(
                product = Product(
                    id = item.productId,
                    name = item.productName,
                    brand = "KidsG",
                    price = item.price,
                    mrp = item.mrp,
                    categoryId = "cat_general",
                    description = "",
                    imageUrl = ""
                ),
                quantity = item.quantity,
                selectedVariant = item.variant
            )
        },
        store = domainStore,
        deliveryAddress = addressSnapshot.toDomain(),
        subtotal = subtotal,
        discount = discount + couponDiscount,
        deliveryFee = deliveryFee,
        taxes = tax,
        totalAmount = total,
        paymentMethod = paymentMethod
    )
}
