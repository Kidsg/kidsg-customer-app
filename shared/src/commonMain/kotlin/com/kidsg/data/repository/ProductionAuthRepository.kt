package com.kidsg.data.repository

import com.kidsg.core.config.ApiConfig
import com.kidsg.core.network.ApiResult
import com.kidsg.core.storage.SessionStorage
import com.kidsg.data.remote.api.AuthApi
import com.kidsg.domain.model.UserProfile
import com.kidsg.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProductionAuthRepository(
    private val authApi: AuthApi = AuthApi()
) : AuthRepository {

    private val _currentUser: MutableStateFlow<UserProfile?>

    init {
        val savedToken = SessionStorage.getAuthToken()
        if (savedToken != null) {
            ApiConfig.authToken = savedToken
        }
        val savedProfile = SessionStorage.getUserProfile()
        _currentUser = MutableStateFlow(savedProfile)
    }

    override val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    override suspend fun checkEmail(email: String): Result<Pair<Boolean, String?>> {
        return when (val res = authApi.checkEmail(email)) {
            is ApiResult.Success -> Result.success(Pair(res.data.exists, res.data.firstName))
            is ApiResult.Error -> Result.failure(Exception(res.exception.userFriendlyMessage))
        }
    }

    override suspend fun loginWithPassword(email: String, password: String): Result<UserProfile> {
        return when (val res = authApi.loginPassword(email, password)) {
            is ApiResult.Success -> {
                ApiConfig.authToken = res.data.token
                SessionStorage.saveAuthToken(res.data.token)
                val profile = UserProfile(
                    id = res.data.user.id,
                    name = "${res.data.user.firstName ?: "Student"} ${res.data.user.lastName ?: ""}".trim(),
                    phone = res.data.user.phone ?: "",
                    email = res.data.user.email ?: email,
                    studentName = res.data.user.firstName ?: "Student",
                    studentGrade = res.data.profile?.selectedClass ?: "Class 7",
                    schoolName = res.data.profile?.selectedSchool ?: "KidsG Partner School"
                )
                _currentUser.value = profile
                SessionStorage.saveUserProfile(profile)
                Result.success(profile)
            }
            is ApiResult.Error -> {
                Result.failure(Exception(res.exception.userFriendlyMessage))
            }
        }
    }

    override suspend fun signup(
        firstName: String,
        lastName: String,
        email: String,
        phone: String,
        password: String,
        selectedClass: String,
        selectedSchool: String
    ): Result<UserProfile> {
        val request = com.kidsg.data.remote.dto.SignupRequestDto(
            firstName = firstName,
            lastName = lastName,
            email = email,
            phone = phone,
            password = password,
            selectedClass = selectedClass,
            selectedSchool = selectedSchool
        )
        return when (val res = authApi.signup(request)) {
            is ApiResult.Success -> {
                ApiConfig.authToken = res.data.token
                SessionStorage.saveAuthToken(res.data.token)
                val profile = UserProfile(
                    id = res.data.user.id,
                    name = "$firstName $lastName".trim(),
                    phone = phone,
                    email = email,
                    studentName = firstName,
                    studentGrade = selectedClass,
                    schoolName = selectedSchool
                )
                _currentUser.value = profile
                SessionStorage.saveUserProfile(profile)
                Result.success(profile)
            }
            is ApiResult.Error -> {
                Result.failure(Exception(res.exception.userFriendlyMessage))
            }
        }
    }

    override suspend fun requestOtp(phoneNumber: String): Result<String> {
        return when (val res = authApi.sendOtp(phoneNumber)) {
            is ApiResult.Success -> Result.success(res.message ?: "Verification code sent")
            is ApiResult.Error -> {
                // If in DEV mode and backend is unreachable from device, provide graceful fallback
                if (ApiConfig.currentEnvironment == com.kidsg.core.config.AppEnvironment.DEV && 
                    res.exception is com.kidsg.core.network.ApiException.NetworkUnavailable) {
                    Result.success("Verification code sent to $phoneNumber")
                } else {
                    Result.failure(Exception(res.exception.userFriendlyMessage))
                }
            }
        }
    }

    override suspend fun verifyOtp(phoneNumber: String, otpCode: String): Result<UserProfile> {
        return when (val res = authApi.verifyOtp(phoneNumber, otpCode)) {
            is ApiResult.Success -> {
                ApiConfig.authToken = res.data.token
                SessionStorage.saveAuthToken(res.data.token)
                val profile = UserProfile(
                    id = res.data.user.id,
                    name = "${res.data.user.firstName ?: "Student"} ${res.data.user.lastName ?: ""}".trim(),
                    phone = res.data.user.phone ?: if (!phoneNumber.contains("@")) phoneNumber else "",
                    email = res.data.user.email ?: if (phoneNumber.contains("@")) phoneNumber else "$phoneNumber@kidsg.in",
                    studentName = res.data.user.firstName ?: "",
                    studentGrade = res.data.profile?.selectedClass ?: "Class 1",
                    schoolName = res.data.profile?.selectedSchool ?: "KidsG Partner School"
                )
                _currentUser.value = profile
                SessionStorage.saveUserProfile(profile)
                Result.success(profile)
            }
            is ApiResult.Error -> {
                Result.failure(Exception(res.exception.userFriendlyMessage))
            }
        }
    }

    override suspend fun updateProfile(profile: UserProfile): Result<UserProfile> {
        _currentUser.value = profile
        SessionStorage.saveUserProfile(profile)
        return Result.success(profile)
    }

    override suspend fun logout() {
        ApiConfig.authToken = null
        SessionStorage.clearSession()
        _currentUser.value = null
    }
}

