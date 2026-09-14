package com.kidsg.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: String,
    val phone: String? = null,
    val email: String? = null,
    val role: String = "CUSTOMER",
    val firstName: String? = null,
    val lastName: String? = null
)

@Serializable
data class ProfileDto(
    val id: String,
    val authUserId: String? = null,
    val firstName: String,
    val lastName: String = "",
    val phone: String? = null,
    val email: String? = null,
    val role: String = "CUSTOMER",
    val onboardingCompleted: Boolean = false,
    val selectedClass: String? = null,
    val selectedSchool: String? = null,
    val avatarUrl: String? = null
)

@Serializable
data class AuthResponseDto(
    val token: String,
    val user: UserDto,
    val profile: ProfileDto? = null
)

@Serializable
data class SendOtpRequestDto(
    val phone: String? = null,
    val email: String? = null
)

@Serializable
data class VerifyOtpRequestDto(
    val phone: String? = null,
    val email: String? = null,
    val otp: String
)

@Serializable
data class SendOtpResponseDto(
    val sent: Boolean,
    val expiresInSeconds: Int = 300
)

@Serializable
data class VerifyOtpResponseDto(
    val verified: Boolean,
    val token: String,
    val user: UserDto,
    val profile: ProfileDto? = null
)

@Serializable
data class MeResponseDto(
    val user: UserDto,
    val profile: ProfileDto? = null,
    val addresses: List<AddressDto> = emptyList()
)
