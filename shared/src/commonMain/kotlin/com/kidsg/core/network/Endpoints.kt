package com.kidsg.core.network

object Endpoints {
    const val HEALTH = "/api/health"

    // Auth
    const val AUTH_SIGNUP = "/api/auth/signup"
    const val AUTH_LOGIN = "/api/auth/login"
    const val AUTH_LOGOUT = "/api/auth/logout"
    const val AUTH_SEND_OTP = "/api/auth/send-otp"
    const val AUTH_VERIFY_OTP = "/api/auth/verify-otp"
    const val AUTH_REFRESH = "/api/auth/refresh"
    const val AUTH_ME = "/api/auth/me"

    // Onboarding & Profile
    const val ONBOARDING = "/api/onboarding"
    const val ONBOARDING_COMPLETE = "/api/onboarding/complete"
    const val LOCATION = "/api/location"
    const val LOCATION_CURRENT = "/api/location/current"
    const val PROFILE = "/api/profile"

    // Catalog & Search
    const val CATEGORIES = "/api/categories"
    const val PRODUCTS = "/api/products"
    const val PRODUCTS_FEATURED = "/api/products/featured"
    const val PRODUCTS_RECOMMENDED = "/api/products/recommended"
    const val SEARCH = "/api/search"
    const val SEARCH_SUGGESTIONS = "/api/search/suggestions"
    const val STORES_NEARBY = "/api/stores/nearby"

    // Bag, Wishlist, Checkout
    const val WISHLIST = "/api/wishlist"
    const val CART = "/api/cart"
    const val CART_ITEMS = "/api/cart/items"
    const val COUPONS = "/api/coupons"
    const val COUPONS_VALIDATE = "/api/coupons/validate"
    const val CHECKOUT_PREVIEW = "/api/checkout/preview"
    const val CHECKOUT_CREATE = "/api/checkout/create"

    // Orders & Payment
    const val ORDERS = "/api/orders"
    const val PAYMENT_CREATE = "/api/payment/create"
    const val PAYMENT_VERIFY = "/api/payment/verify"

    // Account
    const val ADDRESSES = "/api/addresses"
    const val NOTIFICATIONS = "/api/notifications"
    const val SUPPORT_TICKETS = "/api/support/tickets"
}
