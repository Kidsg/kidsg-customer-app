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

enum class AuthStep {
    ENTER_EMAIL,
    EXISTING_USER_OPTIONS,
    PASSWORD_LOGIN,
    OTP_VERIFICATION,
    NEW_USER_SIGNUP
}

/**
 * KidsG Strict Email-First Authentication Flow:
 * SCREEN 1: Enter Email -> Continue
 * CASE A (Existing User): "Welcome back!" -> Continue with Password OR Continue with OTP
 * CASE B (New User): Full sign up (First name, Last name, Email, Phone, Password, Grade, School)
 */
@Composable
fun AuthScreen(
    authRepository: AuthRepository,
    onAuthSuccess: (profile: UserProfile, isReturningUser: Boolean) -> Unit,
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var currentStep by remember { mutableStateOf(AuthStep.ENTER_EMAIL) }

    var emailInput by remember { mutableStateOf("") }
    var existingUserName by remember { mutableStateOf("") }

    // Password Login state
    var passwordInput by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    // Signup form state
    var firstNameInput by remember { mutableStateOf("") }
    var lastNameInput by remember { mutableStateOf("") }
    var phoneInput by remember { mutableStateOf("") }
    var signupPasswordInput by remember { mutableStateOf("") }
    var confirmPasswordInput by remember { mutableStateOf("") }
    var selectedGrade by remember { mutableStateOf("Class 7") }
    var schoolNameInput by remember { mutableStateOf("National Public School") }

    // OTP verification state
    var otpInput by remember { mutableStateOf("") }
    var countdownSeconds by remember { mutableStateOf(30) }

    // Status state
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var infoMessage by remember { mutableStateOf<String?>(null) }

    val otpFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Resend countdown timer for OTP step
    LaunchedEffect(currentStep) {
        if (currentStep == AuthStep.OTP_VERIFICATION) {
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
                            errorMessage = null
                            infoMessage = null
                            when (currentStep) {
                                AuthStep.ENTER_EMAIL -> onBack()
                                AuthStep.EXISTING_USER_OPTIONS -> currentStep = AuthStep.ENTER_EMAIL
                                AuthStep.PASSWORD_LOGIN -> currentStep = AuthStep.EXISTING_USER_OPTIONS
                                AuthStep.OTP_VERIFICATION -> currentStep = AuthStep.EXISTING_USER_OPTIONS
                                AuthStep.NEW_USER_SIGNUP -> currentStep = AuthStep.ENTER_EMAIL
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "←", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Mascot Character
            val storedCharName = remember { SessionStorage.getSelectedCharacter() }
            val selectedCharacter = remember(storedCharName) {
                if (storedCharName == CharacterType.GIRL.name) CharacterType.GIRL else CharacterType.BOY
            }

            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFF7ED))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                KidsGCharacter(
                    characterType = selectedCharacter,
                    pose = CharacterPose(type = PoseType.EXCITED, isFacingRight = true),
                    modifier = Modifier.size(72.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Error / Info banners
            AnimatedVisibility(visible = errorMessage != null, enter = fadeIn(), exit = fadeOut()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFEE2E2))
                        .border(1.dp, Color(0xFFEF4444), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = errorMessage ?: "",
                        style = KidsGTypography.BodySmall.copy(color = Color(0xFFB91C1C), fontWeight = FontWeight.Medium),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            AnimatedVisibility(visible = infoMessage != null, enter = fadeIn(), exit = fadeOut()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFDEF7EC))
                        .border(1.dp, Color(0xFF31C48D), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = infoMessage ?: "",
                        style = KidsGTypography.BodySmall.copy(color = Color(0xFF03543F), fontWeight = FontWeight.Medium),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // ==========================================
            // SCREEN 1: ENTER EMAIL
            // ==========================================
            if (currentStep == AuthStep.ENTER_EMAIL) {
                Text(
                    text = "Welcome to KidsG 🎒",
                    style = KidsGTypography.DisplaySmall.copy(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = KidsGColors.BlackText
                    )
                )

                Text(
                    text = "Enter your email to access your stationery desk or get started",
                    style = KidsGTypography.BodyMedium.copy(
                        color = KidsGColors.TextSecondary,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.padding(top = 4.dp, start = 16.dp, end = 16.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = emailInput,
                    onValueChange = { emailInput = it; errorMessage = null },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    placeholder = { Text("student@school.com") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { keyboardController?.hide() }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = KidsGColors.OrangePrimary,
                        unfocusedBorderColor = KidsGColors.BorderSubtle
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        val email = emailInput.trim().lowercase()
                        if (email.isBlank() || !email.contains("@") || !email.contains(".")) {
                            errorMessage = "Please enter a valid email address."
                            return@Button
                        }

                        isLoading = true
                        errorMessage = null
                        coroutineScope.launch {
                            val res = authRepository.checkEmail(email)
                            isLoading = false
                            res.onSuccess { (exists, name) ->
                                if (exists) {
                                    existingUserName = name ?: ""
                                    currentStep = AuthStep.EXISTING_USER_OPTIONS
                                } else {
                                    currentStep = AuthStep.NEW_USER_SIGNUP
                                }
                            }.onFailure {
                                errorMessage = it.message ?: "Failed to verify email. Please try again."
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = KidsGColors.OrangePrimary)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text(
                            text = "Continue →",
                            style = KidsGTypography.TitleSmall.copy(color = Color.White, fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            // ==========================================
            // CASE A: EXISTING USER OPTIONS
            // ==========================================
            else if (currentStep == AuthStep.EXISTING_USER_OPTIONS) {
                Text(
                    text = "Welcome back${if (existingUserName.isNotBlank()) ", $existingUserName" else ""}! 👋",
                    style = KidsGTypography.DisplaySmall.copy(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = KidsGColors.BlackText
                    ),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Your student desk is ready. How would you like to sign in?",
                    style = KidsGTypography.BodyMedium.copy(
                        color = KidsGColors.TextSecondary,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.padding(top = 4.dp, start = 16.dp, end = 16.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Option 1: Continue with Password
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(KidsGColors.White)
                        .border(1.5.dp, KidsGColors.OrangePrimary, RoundedCornerShape(16.dp))
                        .clickable {
                            errorMessage = null
                            currentStep = AuthStep.PASSWORD_LOGIN
                        }
                        .padding(18.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🔑", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Continue with Password",
                                style = KidsGTypography.TitleMedium.copy(fontWeight = FontWeight.Bold, color = KidsGColors.BlackText)
                            )
                            Text(
                                text = "Sign in directly with your account password",
                                style = KidsGTypography.BodySmall.copy(color = KidsGColors.TextSecondary)
                            )
                        }
                        Text(text = "→", fontSize = 18.sp, color = KidsGColors.OrangePrimary, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Option 2: Continue with OTP
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(KidsGColors.White)
                        .border(1.dp, KidsGColors.BorderSubtle, RoundedCornerShape(16.dp))
                        .clickable {
                            errorMessage = null
                            isLoading = true
                            coroutineScope.launch {
                                val res = authRepository.requestOtp(emailInput.trim().lowercase())
                                isLoading = false
                                res.onSuccess { msg ->
                                    infoMessage = msg
                                    currentStep = AuthStep.OTP_VERIFICATION
                                }.onFailure { err ->
                                    errorMessage = err.message ?: "Failed to dispatch verification code."
                                }
                            }
                        }
                        .padding(18.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "📲", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Continue with OTP",
                                style = KidsGTypography.TitleMedium.copy(fontWeight = FontWeight.Bold, color = KidsGColors.BlackText)
                            )
                            Text(
                                text = "Receive a 6-digit code on your email",
                                style = KidsGTypography.BodySmall.copy(color = KidsGColors.TextSecondary)
                            )
                        }
                        Text(text = "→", fontSize = 18.sp, color = KidsGColors.TextSecondary, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // ==========================================
            // PASSWORD LOGIN STEP
            // ==========================================
            else if (currentStep == AuthStep.PASSWORD_LOGIN) {
                Text(
                    text = "Enter Password 🔒",
                    style = KidsGTypography.DisplaySmall.copy(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = KidsGColors.BlackText
                    )
                )

                Text(
                    text = "Sign in to ${emailInput.trim()}",
                    style = KidsGTypography.BodyMedium.copy(
                        color = KidsGColors.TextSecondary,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = { passwordInput = it; errorMessage = null },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    placeholder = { Text("Enter your account password") },
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        Text(
                            text = if (isPasswordVisible) "👁" else "🙈",
                            modifier = Modifier.clickable { isPasswordVisible = !isPasswordVisible }.padding(12.dp)
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = KidsGColors.OrangePrimary,
                        unfocusedBorderColor = KidsGColors.BorderSubtle
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Text(
                        text = "Login with OTP instead →",
                        style = KidsGTypography.Caption.copy(
                            color = KidsGColors.OrangePrimary,
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier
                            .clickable {
                                errorMessage = null
                                isLoading = true
                                coroutineScope.launch {
                                    val res = authRepository.requestOtp(emailInput.trim().lowercase())
                                    isLoading = false
                                    res.onSuccess { msg ->
                                        infoMessage = msg
                                        currentStep = AuthStep.OTP_VERIFICATION
                                    }.onFailure { err ->
                                        errorMessage = err.message ?: "Failed to dispatch verification code."
                                    }
                                }
                            }
                            .padding(4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val email = emailInput.trim().lowercase()
                        if (passwordInput.isBlank()) {
                            errorMessage = "Please enter your password."
                            return@Button
                        }
                        isLoading = true
                        errorMessage = null
                        coroutineScope.launch {
                            val res = authRepository.loginWithPassword(email, passwordInput)
                            isLoading = false
                            res.onSuccess { profile ->
                                onAuthSuccess(profile, true)
                            }.onFailure { err ->
                                errorMessage = err.message ?: "Invalid email or password. Please try again."
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = KidsGColors.OrangePrimary)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text(
                            text = "Sign In →",
                            style = KidsGTypography.TitleSmall.copy(color = Color.White, fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            // ==========================================
            // OTP VERIFICATION STEP
            // ==========================================
            else if (currentStep == AuthStep.OTP_VERIFICATION) {
                Text(
                    text = "Verify Email OTP ✉️",
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

                // 6-digit OTP input boxes
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
                                onAuthSuccess(profile, true)
                            }.onFailure { err ->
                                errorMessage = err.message ?: "Invalid or expired verification code."
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = KidsGColors.OrangePrimary)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text(
                            text = "Verify & Sign In →",
                            style = KidsGTypography.TitleSmall.copy(color = Color.White, fontWeight = FontWeight.Bold)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (countdownSeconds > 0) {
                        Text(
                            text = "Resend code in ${countdownSeconds}s",
                            style = KidsGTypography.BodySmall.copy(color = KidsGColors.TextMuted)
                        )
                    } else {
                        Text(
                            text = "Resend OTP",
                            style = KidsGTypography.BodySmall.copy(
                                color = KidsGColors.OrangePrimary,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.clickable {
                                countdownSeconds = 30
                                coroutineScope.launch {
                                    val res = authRepository.requestOtp(emailInput.trim())
                                    res.onSuccess { msg -> infoMessage = msg }
                                }
                            }
                        )
                    }
                }
            }

            // ==========================================
            // CASE B: NEW USER SIGNUP
            // ==========================================
            else if (currentStep == AuthStep.NEW_USER_SIGNUP) {
                Text(
                    text = "Create Your Account 🎓",
                    style = KidsGTypography.DisplaySmall.copy(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = KidsGColors.BlackText
                    )
                )

                Text(
                    text = "Sign up for quick school stationery delivery to your desk",
                    style = KidsGTypography.BodyMedium.copy(
                        color = KidsGColors.TextSecondary,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Name Fields Row
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = firstNameInput,
                        onValueChange = { firstNameInput = it; errorMessage = null },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        placeholder = { Text("First Name *") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = KidsGColors.OrangePrimary,
                            unfocusedBorderColor = KidsGColors.BorderSubtle
                        )
                    )

                    OutlinedTextField(
                        value = lastNameInput,
                        onValueChange = { lastNameInput = it; errorMessage = null },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        placeholder = { Text("Last Name") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = KidsGColors.OrangePrimary,
                            unfocusedBorderColor = KidsGColors.BorderSubtle
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Email Field (prefilled)
                OutlinedTextField(
                    value = emailInput,
                    onValueChange = { emailInput = it; errorMessage = null },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    placeholder = { Text("Email *") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = KidsGColors.OrangePrimary,
                        unfocusedBorderColor = KidsGColors.BorderSubtle
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Mobile Number Field
                OutlinedTextField(
                    value = phoneInput,
                    onValueChange = { phoneInput = it; errorMessage = null },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    placeholder = { Text("Mobile Number (optional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = KidsGColors.OrangePrimary,
                        unfocusedBorderColor = KidsGColors.BorderSubtle
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Password Field
                OutlinedTextField(
                    value = signupPasswordInput,
                    onValueChange = { signupPasswordInput = it; errorMessage = null },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    placeholder = { Text("Create Password (min 6 characters) *") },
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

                Spacer(modifier = Modifier.height(10.dp))

                // Confirm Password Field
                OutlinedTextField(
                    value = confirmPasswordInput,
                    onValueChange = { confirmPasswordInput = it; errorMessage = null },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    placeholder = { Text("Confirm Password *") },
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = KidsGColors.OrangePrimary,
                        unfocusedBorderColor = KidsGColors.BorderSubtle
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // School Name Field
                OutlinedTextField(
                    value = schoolNameInput,
                    onValueChange = { schoolNameInput = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    placeholder = { Text("School / College Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = KidsGColors.OrangePrimary,
                        unfocusedBorderColor = KidsGColors.BorderSubtle
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (firstNameInput.isBlank()) {
                            errorMessage = "Please enter your first name."
                            return@Button
                        }
                        if (emailInput.isBlank() || !emailInput.contains("@")) {
                            errorMessage = "Please enter a valid email address."
                            return@Button
                        }
                        if (signupPasswordInput.length < 6) {
                            errorMessage = "Password must be at least 6 characters long."
                            return@Button
                        }
                        if (signupPasswordInput != confirmPasswordInput) {
                            errorMessage = "Passwords do not match."
                            return@Button
                        }

                        isLoading = true
                        errorMessage = null
                        coroutineScope.launch {
                            val res = authRepository.signup(
                                firstName = firstNameInput.trim(),
                                lastName = lastNameInput.trim(),
                                email = emailInput.trim().lowercase(),
                                phone = phoneInput.trim(),
                                password = signupPasswordInput,
                                selectedClass = selectedGrade,
                                selectedSchool = schoolNameInput.trim().ifBlank { "KidsG Partner School" }
                            )
                            isLoading = false
                            res.onSuccess { profile ->
                                onAuthSuccess(profile, true)
                            }.onFailure { err ->
                                errorMessage = err.message ?: "Failed to create account. Please try again."
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = KidsGColors.OrangePrimary)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text(
                            text = "Create Account & Start Learning →",
                            style = KidsGTypography.TitleSmall.copy(color = Color.White, fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
