package com.kidsg.data.repository

import com.kidsg.core.config.ApiConfig
import com.kidsg.core.network.ApiResult
import com.kidsg.data.remote.api.AuthApi
import com.kidsg.domain.model.UserProfile
import com.kidsg.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProductionAuthRepository(
    private val authApi: AuthApi = AuthApi()
) : AuthRepository {

    private val _currentUser = MutableStateFlow<UserProfile?>(
        UserProfile(
            id = "user_dev_default",
            name = "Aarav Sharma",
            phone = "+91 98765 43210",
            email = "student@kidsg.in",
            studentName = "Aarav Sharma",
            studentGrade = "Class 7",
            schoolName = "National Public School, Koramangala",
            isParentMode = false
        )
    )
    override val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    override suspend fun requestOtp(phoneNumber: String): Result<Boolean> {
        return when (val res = authApi.sendOtp(phoneNumber)) {
            is ApiResult.Success -> Result.success(true)
            is ApiResult.Error -> Result.failure(Exception(res.exception.userFriendlyMessage))
        }
    }

    override suspend fun verifyOtp(phoneNumber: String, otpCode: String): Result<UserProfile> {
        return when (val res = authApi.verifyOtp(phoneNumber, otpCode)) {
            is ApiResult.Success -> {
                ApiConfig.authToken = res.data.token
                val profile = UserProfile(
                    id = res.data.user.id,
                    name = "${res.data.user.firstName ?: "Student"} ${res.data.user.lastName ?: ""}".trim(),
                    phone = res.data.user.phone ?: phoneNumber,
                    email = res.data.user.email ?: "$phoneNumber@kidsg.in",
                    studentGrade = res.data.profile?.selectedClass ?: "Class 7",
                    schoolName = res.data.profile?.selectedSchool ?: "National Public School"
                )
                _currentUser.value = profile
                Result.success(profile)
            }
            is ApiResult.Error -> {
                Result.failure(Exception(res.exception.userFriendlyMessage))
            }
        }
    }

    override suspend fun updateProfile(profile: UserProfile): Result<UserProfile> {
        _currentUser.value = profile
        return Result.success(profile)
    }

    override suspend fun logout() {
        ApiConfig.authToken = null
        _currentUser.value = null
    }
}
