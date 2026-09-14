package com.kidsg.data.remote.api

import com.kidsg.core.config.ApiConfig
import com.kidsg.core.network.ApiClient
import com.kidsg.core.network.ApiException
import com.kidsg.core.network.ApiResult
import com.kidsg.core.network.Endpoints
import com.kidsg.data.remote.dto.*
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType

suspend inline fun <reified T, reified B : Any> safeApiCallWithBody(
    method: HttpMethod,
    path: String,
    bodyData: B,
    params: Map<String, String>? = null
): ApiResult<T> {
    return try {
        val client = ApiClient.httpClient
        val url = "${ApiConfig.baseUrl}$path"

        val response: HttpResponse = client.request(url) {
            this.method = method
            contentType(ContentType.Application.Json)
            params?.forEach { (k, v) -> parameter(k, v) }
            setBody(bodyData)
        }

        if (response.status == HttpStatusCode.Unauthorized) {
            return ApiResult.Error(ApiException.Unauthorized())
        }
        if (response.status == HttpStatusCode.Forbidden) {
            return ApiResult.Error(ApiException.Forbidden())
        }
        if (response.status == HttpStatusCode.NotFound) {
            return ApiResult.Error(ApiException.NotFound())
        }

        val apiResponse: ApiResponseDto<T> = response.body()
        if (apiResponse.success && apiResponse.data != null) {
            ApiResult.Success(apiResponse.data, apiResponse.message)
        } else {
            ApiResult.Error(
                ApiException.ValidationError(
                    message = apiResponse.message ?: "Request was not successful",
                    errorCode = apiResponse.errorCode
                )
            )
        }
    } catch (e: io.ktor.client.network.sockets.ConnectTimeoutException) {
        ApiResult.Error(ApiException.Timeout("Connection timed out", cause = e))
    } catch (e: io.ktor.client.plugins.HttpRequestTimeoutException) {
        ApiResult.Error(ApiException.Timeout("Request timed out", cause = e))
    } catch (e: Exception) {
        ApiResult.Error(ApiException.NetworkUnavailable(e.message ?: "Network error", cause = e))
    }
}

suspend inline fun <reified T> safeApiCall(
    method: HttpMethod,
    path: String,
    params: Map<String, String>? = null
): ApiResult<T> {
    return try {
        val client = ApiClient.httpClient
        val url = "${ApiConfig.baseUrl}$path"

        val response: HttpResponse = client.request(url) {
            this.method = method
            params?.forEach { (k, v) -> parameter(k, v) }
        }

        if (response.status == HttpStatusCode.Unauthorized) {
            return ApiResult.Error(ApiException.Unauthorized())
        }
        if (response.status == HttpStatusCode.Forbidden) {
            return ApiResult.Error(ApiException.Forbidden())
        }
        if (response.status == HttpStatusCode.NotFound) {
            return ApiResult.Error(ApiException.NotFound())
        }

        val apiResponse: ApiResponseDto<T> = response.body()
        if (apiResponse.success && apiResponse.data != null) {
            ApiResult.Success(apiResponse.data, apiResponse.message)
        } else {
            ApiResult.Error(
                ApiException.ValidationError(
                    message = apiResponse.message ?: "Request was not successful",
                    errorCode = apiResponse.errorCode
                )
            )
        }
    } catch (e: io.ktor.client.network.sockets.ConnectTimeoutException) {
        ApiResult.Error(ApiException.Timeout("Connection timed out", cause = e))
    } catch (e: io.ktor.client.plugins.HttpRequestTimeoutException) {
        ApiResult.Error(ApiException.Timeout("Request timed out", cause = e))
    } catch (e: Exception) {
        ApiResult.Error(ApiException.NetworkUnavailable(e.message ?: "Network error", cause = e))
    }
}

class ProductApi {
    suspend fun getCategories(): ApiResult<List<CategoryDto>> =
        safeApiCall(HttpMethod.Get, Endpoints.CATEGORIES)

    suspend fun getProducts(search: String? = null, categoryId: String? = null): ApiResult<ProductsResponseDto> {
        val params = mutableMapOf<String, String>()
        if (!search.isNullOrBlank()) params["search"] = search
        if (!categoryId.isNullOrBlank()) params["category"] = categoryId
        return safeApiCall(HttpMethod.Get, Endpoints.PRODUCTS, params = params)
    }

    suspend fun getFeaturedProducts(): ApiResult<List<ProductDto>> =
        safeApiCall(HttpMethod.Get, Endpoints.PRODUCTS_FEATURED)

    suspend fun getProductById(id: String): ApiResult<ProductDto> =
        safeApiCall(HttpMethod.Get, "${Endpoints.PRODUCTS}/$id")

    suspend fun search(query: String): ApiResult<SearchResponseDto> =
        safeApiCall(HttpMethod.Get, Endpoints.SEARCH, params = mapOf("q" to query))

    suspend fun getStoresNearby(): ApiResult<List<StoreDto>> =
        safeApiCall(HttpMethod.Get, Endpoints.STORES_NEARBY)
}

class CartApi {
    suspend fun getCart(): ApiResult<CartResponseDto> =
        safeApiCall(HttpMethod.Get, Endpoints.CART)

    suspend fun addToCart(productId: String, quantity: Int = 1, variant: String? = null): ApiResult<CartResponseDto> {
        return safeApiCallWithBody(
            HttpMethod.Post,
            Endpoints.CART_ITEMS,
            bodyData = AddToCartRequestDto(productId, quantity, variant)
        )
    }

    suspend fun updateCartItem(cartItemId: String, quantity: Int): ApiResult<CartResponseDto> {
        return safeApiCallWithBody(
            HttpMethod.Patch,
            "${Endpoints.CART_ITEMS}/$cartItemId",
            bodyData = UpdateCartItemRequestDto(quantity)
        )
    }

    suspend fun removeFromCart(cartItemId: String): ApiResult<CartResponseDto> =
        safeApiCall(HttpMethod.Delete, "${Endpoints.CART_ITEMS}/$cartItemId")

    suspend fun clearCart(): ApiResult<CartResponseDto> =
        safeApiCall(HttpMethod.Delete, Endpoints.CART)

    suspend fun validateCoupon(code: String): ApiResult<CouponValidationResponseDto> {
        return safeApiCallWithBody(
            HttpMethod.Post,
            Endpoints.COUPONS_VALIDATE,
            bodyData = ValidateCouponRequestDto(code)
        )
    }

    suspend fun getAvailableCoupons(): ApiResult<List<CouponDto>> =
        safeApiCall(HttpMethod.Get, Endpoints.COUPONS)
}

class CheckoutApi {
    suspend fun preview(couponCode: String? = null): ApiResult<CheckoutPreviewDto> {
        return safeApiCallWithBody(
            HttpMethod.Post,
            Endpoints.CHECKOUT_PREVIEW,
            bodyData = CheckoutPreviewRequestDto(couponCode)
        )
    }

    suspend fun createOrder(addressId: String, couponCode: String? = null, paymentMethod: String = "UPI"): ApiResult<CheckoutCreateResponseDto> {
        return safeApiCallWithBody(
            HttpMethod.Post,
            Endpoints.CHECKOUT_CREATE,
            bodyData = CheckoutCreateRequestDto(addressId, paymentMethod, couponCode)
        )
    }
}

class OrderApi {
    suspend fun getOrders(): ApiResult<List<OrderDto>> =
        safeApiCall(HttpMethod.Get, Endpoints.ORDERS)

    suspend fun getOrderById(id: String): ApiResult<OrderDto> =
        safeApiCall(HttpMethod.Get, "${Endpoints.ORDERS}/$id")

    suspend fun getTracking(orderId: String): ApiResult<OrderTrackingDto> =
        safeApiCall(HttpMethod.Get, "${Endpoints.ORDERS}/$orderId/tracking")

    suspend fun cancelOrder(orderId: String): ApiResult<OrderDto> =
        safeApiCall(HttpMethod.Post, "${Endpoints.ORDERS}/$orderId/cancel")
}

class AuthApi {
    suspend fun sendOtp(contact: String): ApiResult<SendOtpResponseDto> {
        val trimmed = contact.trim()
        val request = if (trimmed.contains("@")) {
            SendOtpRequestDto(email = trimmed)
        } else {
            SendOtpRequestDto(phone = trimmed)
        }
        return safeApiCallWithBody(HttpMethod.Post, Endpoints.AUTH_SEND_OTP, bodyData = request)
    }

    suspend fun verifyOtp(contact: String, otp: String): ApiResult<VerifyOtpResponseDto> {
        val trimmed = contact.trim()
        val request = if (trimmed.contains("@")) {
            VerifyOtpRequestDto(email = trimmed, otp = otp.trim())
        } else {
            VerifyOtpRequestDto(phone = trimmed, otp = otp.trim())
        }
        return safeApiCallWithBody(HttpMethod.Post, Endpoints.AUTH_VERIFY_OTP, bodyData = request)
    }

    suspend fun getMe(): ApiResult<MeResponseDto> =
        safeApiCall(HttpMethod.Get, Endpoints.AUTH_ME)
}

class AddressApi {
    suspend fun getAddresses(): ApiResult<List<AddressDto>> =
        safeApiCall(HttpMethod.Get, Endpoints.ADDRESSES)

    suspend fun addAddress(address: AddressDto): ApiResult<AddressDto> =
        safeApiCallWithBody(HttpMethod.Post, Endpoints.ADDRESSES, bodyData = address)
}
