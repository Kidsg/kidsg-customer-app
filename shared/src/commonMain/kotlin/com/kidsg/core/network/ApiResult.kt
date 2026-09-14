package com.kidsg.core.network

sealed interface ApiResult<out T> {
    data class Success<T>(val data: T, val message: String? = null) : ApiResult<T>
    data class Error(val exception: ApiException) : ApiResult<Nothing>
}

sealed class ApiException(
    val userFriendlyMessage: String,
    val errorCode: String? = null,
    val statusCode: Int? = null,
    cause: Throwable? = null
) : Exception(userFriendlyMessage, cause) {

    class NetworkUnavailable(message: String = "No internet connection. Please check your network.", cause: Throwable? = null) :
        ApiException(message, "NETWORK_UNAVAILABLE", 0, cause)

    class Unauthorized(message: String = "Please log in to continue.", cause: Throwable? = null) :
        ApiException(message, "UNAUTHORIZED", 401, cause)

    class Forbidden(message: String = "You don't have permission to access this.", cause: Throwable? = null) :
        ApiException(message, "FORBIDDEN", 403, cause)

    class NotFound(message: String = "The requested item was not found.", cause: Throwable? = null) :
        ApiException(message, "NOT_FOUND", 404, cause)

    class ValidationError(message: String, errorCode: String? = "VALIDATION_ERROR", cause: Throwable? = null) :
        ApiException(message, errorCode, 400, cause)

    class PaymentError(message: String, errorCode: String? = "PAYMENT_FAILED", cause: Throwable? = null) :
        ApiException(message, errorCode, 400, cause)

    class ServerError(message: String = "Something went wrong. Please try again.", cause: Throwable? = null) :
        ApiException(message, "SERVER_ERROR", 500, cause)

    class Timeout(message: String = "The request took too long. Please try again.", cause: Throwable? = null) :
        ApiException(message, "TIMEOUT", 408, cause)

    class UnknownError(message: String = "An unexpected error occurred.", cause: Throwable? = null) :
        ApiException(message, "UNKNOWN", -1, cause)
}
