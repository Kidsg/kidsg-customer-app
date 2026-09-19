package com.kidsg.feature.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidsg.core.designsystem.KidsGColors
import com.kidsg.core.designsystem.KidsGIllustration
import com.kidsg.core.designsystem.KidsGTypography
import com.kidsg.core.storage.SessionStorage
import com.kidsg.domain.model.UserProfile
import com.kidsg.domain.repository.AuthRepository
import com.kidsg.feature.onboarding.CharacterPose
import com.kidsg.feature.onboarding.CharacterType
import com.kidsg.feature.onboarding.PoseType
import com.kidsg.feature.onboarding.components.KidsGCharacter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class AuthTab {
    SIGN_IN,
    SIGN_UP
}

/**
 * KidsG Comprehensive Authentication Screen
 * Supports:
 * - Returning User: Email + Password Login (instantly restores desk without repeat setup)
 * - Returning User: Login with OTP (if password forgotten)
 * - New User: Email OTP Verification -> Set Password -> Student Setup
 */
@Composable
fun AuthScreen(
    authRepository: AuthRepository,
    onAuthSuccess: (profile: UserProfile, isReturningUser: Boolean) -> Unit,
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var currentTab by remember { mutableStateOf(AuthTab.SIGN_IN) }

    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var confirmPasswordInput by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    // OTP verification states
    var isOtpSent by remember { mutableStateOf(false) }
    var isSettingPasswordStep by remember { mutableStateOf(false) }
    var otpInput by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var infoMessage by remember { mutableStateOf<String?>(null) }
    var countdownSeconds by remember { mutableStateOf(30) }

    val otpFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Resend countdown timer
    LaunchedEffect(isOtpSent) {
        if (isOtpSent) {
            countdownSeconds = 30
            delay(150)
            try {
                otpFocusRequester.requestFocus()
                keyboardController?.show()
            } catch (_: Exception) {}
            while (countdownSeconds > 0) {
                delay(1000)
                countdownSeconds -= 1
            }
        }
    }

    KidsGIllustration.DeskBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Navigation Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(KidsGColors.White)
                        .clickable {
                            if (isSettingPasswordStep) {
                                isSettingPasswordStep = false
                            } else if (isOtpSent) {
                                isOtpSent = false
                                errorMessage = null
                            } else {
                                onBack()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "←", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Mascot / Character Icon (Displaying selected character from onboarding)
            val storedCharName = remember { SessionStorage.getSelectedCharacter() }
            val selectedCharacter = remember(storedCharName) {
                if (storedCharName == CharacterType.GIRL.name) CharacterType.GIRL else CharacterType.BOY
            }

            Box(
                modifier = Modifier
                    .size(92.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFF7ED))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                KidsGCharacter(
                    characterType = selectedCharacter,
                    pose = CharacterPose(type = PoseType.EXCITED, isFacingRight = true),
                    modifier = Modifier.size(76.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Auth Container
            if (isSettingPasswordStep) {
                // STEP 3: SET PASSWORD AFTER OTP VERIFICATION (New User)
                Text(
                    text = "Set Your Password 🔒",
                    style = KidsGTypography.DisplaySmall.copy(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = KidsGColors.BlackText
                    )
                )

                Text(
                    text = "Create a password for quick login to your student desk next time",
                    style = KidsGTypography.BodyMedium.copy(
                        color = KidsGColors.TextSecondary,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.padding(top = 4.dp, start = 16.dp, end = 16.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = { passwordInput = it; errorMessage = null },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    placeholder = { Text("Create Password (min 6 characters)") },
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        Text(
                            text = if (isPasswordVisible) "👁" else "🙈",
                            modifier = Modifier.clickable { isPasswordVisible = !isPasswordVisible }.padding(12.dp)
                        )
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = KidsGColors.OrangePrimary,
                        unfocusedBorderColor = KidsGColors.BorderSubtle
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = confirmPasswordInput,
                    onValueChange = { confirmPasswordInput = it; errorMessage = null },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    placeholder = { Text("Confirm Password") },
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = KidsGColors.OrangePrimary,
                        unfocusedBorderColor = KidsGColors.BorderSubtle
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (passwordInput.length < 6) {
                            errorMessage = "Password must be at least 6 characters long."
                            return@Button
                        }
                        if (passwordInput != confirmPasswordInput) {
                            errorMessage = "Passwords do not match."
                            return@Button
                        }
                        SessionStorage.savePendingPassword(passwordInput)
                        val email = emailInput.trim().lowercase()
                        val newProfile = UserProfile(
                            id = "user_${email.replace("[^a-zA-Z0-9]".toRegex(), "_")}",
                            name = "Student",
                            phone = "",
                            email = email,
                            studentName = "",
                            studentGrade = "Class 7",
                            schoolName = "National Public School"
                        )
                        coroutineScope.launch {
                            authRepository.updateProfile(newProfile)
                            onAuthSuccess(newProfile, false)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = KidsGColors.OrangePrimary)
                ) {
                    Text(
                        text = "Continue to Student Desk Setup →",
                        style = KidsGTypography.TitleSmall.copy(color = Color.White, fontWeight = FontWeight.Bold)
                    )
                }

            } else if (isOtpSent) {
                // STEP 2: ENTER OTP (For Sign Up or OTP Login)
                Text(
                    text = "Verify Email OTP",
                    style = KidsGTypography.DisplaySmall.copy(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = KidsGColors.BlackText
                    )
                )

                Text(
                    text = "We sent a 6-digit code to\n${emailInput.trim()}",
                    style = KidsGTypography.BodyMedium.copy(
                        color = KidsGColors.TextSecondary,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 6-digit OTP input using BasicTextField with decorationBox for full touch target
                BasicTextField(
                    value = otpInput,
                    onValueChange = { newValue ->
                        val digitsOnly = newValue.filter { it.isDigit() }
                        if (digitsOnly.length <= 6) {
                            otpInput = digitsOnly
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { keyboardController?.hide() }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(otpFocusRequester),
                    decorationBox = {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    otpFocusRequester.requestFocus()
                                    keyboardController?.show()
                                },
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            (0 until 6).forEach { index ->
                                val char = otpInput.getOrNull(index)?.toString() ?: ""
                                val isCurrent = otpInput.length == index || (index == 5 && otpInput.length == 6)

                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(KidsGColors.White)
                                        .border(
                                            width = if (isCurrent) 2.dp else 1.dp,
                                            color = if (isCurrent) KidsGColors.OrangePrimary else KidsGColors.BorderSubtle,
                                            shape = RoundedCornerShape(12.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = char,
                                        style = KidsGTypography.TitleLarge.copy(
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = KidsGColors.BlackText
                                        )
                                    )
                                }
                            }
                        }
                    }
                )

                Text(
                    text = "Tap to enter the 6-digit verification code",
                    style = KidsGTypography.BodySmall.copy(
                        color = KidsGColors.TextMuted,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .clickable {
                            otpFocusRequester.requestFocus()
                            keyboardController?.show()
                        }
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (otpInput.length < 6) {
                            errorMessage = "Please enter the complete 6-digit code."
                            return@Button
                        }
                        isLoading = true
                        errorMessage = null
                        coroutineScope.launch {
                            val result = authRepository.verifyOtp(emailInput.trim(), otpInput)
                            isLoading = false
                            result.onSuccess { profile ->
                                // Check if user already exists
                                val existingAccount = SessionStorage.getAccount(emailInput.trim())
                                if (existingAccount != null && existingAccount.profile.studentName.isNotBlank()) {
                                    authRepository.updateProfile(existingAccount.profile)
                                    onAuthSuccess(existingAccount.profile, true)
                                } else if (currentTab == AuthTab.SIGN_UP) {
                                    // New user: proceed to Set Password!
                                    isSettingPasswordStep = true
                                } else {
                                    onAuthSuccess(profile, false)
                                }
                            }.onFailure { err ->
                                errorMessage = err.message ?: "Verification failed. Please try again."
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = KidsGColors.OrangePrimary),
                    enabled = !isLoading && otpInput.length == 6
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                    } else {
                        Text(
                            text = "Verify Code →",
                            style = KidsGTypography.TitleSmall.copy(color = Color.White, fontWeight = FontWeight.Bold)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Resend OTP Countdown
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (countdownSeconds > 0) {
                        Text(
                            text = "Resend code in ${countdownSeconds}s",
                            style = KidsGTypography.BodySmall.copy(color = KidsGColors.TextMuted)
                        )
                    } else {
                        TextButton(
                            onClick = {
                                coroutineScope.launch {
                                    authRepository.requestOtp(emailInput.trim())
                                    countdownSeconds = 30
                                    infoMessage = "New code dispatched to your email."
                                }
                            }
                        ) {
                            Text(
                                text = "Resend Code",
                                style = KidsGTypography.BodySmall.copy(
                                    color = KidsGColors.OrangePrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }

            } else {
                // STEP 1: TAB SWITCHER (SIGN IN vs SIGN UP)
                Text(
                    text = if (currentTab == AuthTab.SIGN_IN) "Welcome Back!" else "Create Your Account",
                    style = KidsGTypography.DisplaySmall.copy(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = KidsGColors.BlackText
                    )
                )

                Text(
                    text = if (currentTab == AuthTab.SIGN_IN)
                        "Sign in to access your student stationery desk"
                    else
                        "Enter your email to get started with KidsG",
                    style = KidsGTypography.BodyMedium.copy(
                        color = KidsGColors.TextSecondary,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.padding(top = 4.dp, start = 16.dp, end = 16.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Tab Switcher Pill
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(KidsGColors.Surface)
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (currentTab == AuthTab.SIGN_IN) KidsGColors.White else Color.Transparent)
                            .clickable {
                                currentTab = AuthTab.SIGN_IN
                                errorMessage = null
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Sign In",
                            style = KidsGTypography.BodyMedium.copy(
                                fontWeight = if (currentTab == AuthTab.SIGN_IN) FontWeight.Bold else FontWeight.Normal,
                                color = if (currentTab == AuthTab.SIGN_IN) KidsGColors.BlackText else KidsGColors.TextSecondary
                            )
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (currentTab == AuthTab.SIGN_UP) KidsGColors.White else Color.Transparent)
                            .clickable {
                                currentTab = AuthTab.SIGN_UP
                                errorMessage = null
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Create Account",
                            style = KidsGTypography.BodyMedium.copy(
                                fontWeight = if (currentTab == AuthTab.SIGN_UP) FontWeight.Bold else FontWeight.Normal,
                                color = if (currentTab == AuthTab.SIGN_UP) KidsGColors.BlackText else KidsGColors.TextSecondary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Email Input
                OutlinedTextField(
                    value = emailInput,
                    onValueChange = { emailInput = it; errorMessage = null },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    placeholder = { Text("Enter your email address") },
                    leadingIcon = { Text(text = "✉", fontSize = 16.sp, color = KidsGColors.TextMuted) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = KidsGColors.OrangePrimary,
                        unfocusedBorderColor = KidsGColors.BorderSubtle
                    )
                )

                if (currentTab == AuthTab.SIGN_IN) {
                    Spacer(modifier = Modifier.height(12.dp))

                    // Password Input for Returning User
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it; errorMessage = null },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        placeholder = { Text("Enter your password") },
                        leadingIcon = { Text(text = "🔒", fontSize = 16.sp, color = KidsGColors.TextMuted) },
                        trailingIcon = {
                            Text(
                                text = if (isPasswordVisible) "👁" else "🙈",
                                modifier = Modifier.clickable { isPasswordVisible = !isPasswordVisible }.padding(12.dp)
                            )
                        },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = KidsGColors.OrangePrimary,
                            unfocusedBorderColor = KidsGColors.BorderSubtle
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Forgot password / Login with OTP option
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "Login with OTP instead",
                            style = KidsGTypography.Caption.copy(
                                color = KidsGColors.OrangePrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            ),
                            modifier = Modifier
                                .clickable {
                                    val email = emailInput.trim()
                                    if (email.isBlank() || !email.contains("@")) {
                                        errorMessage = "Please enter your email above first."
                                        return@clickable
                                    }
                                    isLoading = true
                                    coroutineScope.launch {
                                        val res = authRepository.requestOtp(email)
                                        isLoading = false
                                        res.onSuccess {
                                            isOtpSent = true
                                        }.onFailure {
                                            errorMessage = it.message ?: "Failed to send OTP."
                                        }
                                    }
                                }
                                .padding(4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            val email = emailInput.trim().lowercase()
                            if (email.isBlank() || !email.contains("@")) {
                                errorMessage = "Please enter a valid email address."
                                return@Button
                            }
                            if (passwordInput.isBlank()) {
                                errorMessage = "Please enter your password."
                                return@Button
                            }

                            // Check local persistent account registry
                            val account = SessionStorage.getAccount(email)
                            if (account != null) {
                                if (account.passwordHash == passwordInput) {
                                    coroutineScope.launch {
                                        authRepository.updateProfile(account.profile)
                                        onAuthSuccess(account.profile, true)
                                    }
                                } else {
                                    errorMessage = "Incorrect password. Please check or use 'Login with OTP'."
                                }
                            } else {
                                // If not registered locally, allow login and create profile
                                val profile = UserProfile(
                                    id = "user_${email.replace("[^a-zA-Z0-9]".toRegex(), "_")}",
                                    name = "Student",
                                    phone = "",
                                    email = email,
                                    studentName = "Aarav Sharma",
                                    studentGrade = "Class 7",
                                    schoolName = "National Public School"
                                )
                                SessionStorage.saveAccount(
                                    SessionStorage.AccountRecord(email = email, passwordHash = passwordInput, profile = profile)
                                )
                                coroutineScope.launch {
                                    authRepository.updateProfile(profile)
                                    onAuthSuccess(profile, true)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = KidsGColors.OrangePrimary)
                    ) {
                        Text(
                            text = "Sign In →",
                            style = KidsGTypography.TitleSmall.copy(color = Color.White, fontWeight = FontWeight.Bold)
                        )
                    }

                } else {
                    // SIGN UP (SEND OTP)
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val email = emailInput.trim()
                            if (email.isBlank() || !email.contains("@")) {
                                errorMessage = "Please enter a valid email address."
                                return@Button
                            }
                            isLoading = true
                            errorMessage = null
                            coroutineScope.launch {
                                val result = authRepository.requestOtp(email)
                                isLoading = false
                                result.onSuccess {
                                    isOtpSent = true
                                    otpInput = ""
                                }.onFailure { err ->
                                    errorMessage = err.message ?: "Failed to send verification code."
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = KidsGColors.OrangePrimary),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                        } else {
                            Text(
                                text = "Send 6-Digit OTP →",
                                style = KidsGTypography.TitleSmall.copy(color = Color.White, fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }

            // Error / Info Messages
            AnimatedVisibility(visible = errorMessage != null, enter = fadeIn(), exit = fadeOut()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFFEF2F2))
                        .border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = errorMessage ?: "",
                        style = KidsGTypography.Caption.copy(color = Color(0xFFDC2626), fontWeight = FontWeight.SemiBold)
                    )
                }
            }

            AnimatedVisibility(visible = infoMessage != null, enter = fadeIn(), exit = fadeOut()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFF0FDF4))
                        .border(1.dp, Color(0xFF86EFAC), RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = infoMessage ?: "",
                        style = KidsGTypography.Caption.copy(color = Color(0xFF16A34A), fontWeight = FontWeight.SemiBold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
