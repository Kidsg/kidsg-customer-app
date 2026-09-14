package com.kidsg.feature.auth

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidsg.core.designsystem.KidsGColors
import com.kidsg.core.designsystem.KidsGIllustration
import com.kidsg.core.designsystem.KidsGTypography
import com.kidsg.domain.model.UserProfile
import com.kidsg.domain.repository.AuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * KidsG Auth Screen
 * Supports Phone (+91) OTP and Email OTP Login & Signup.
 */
@Composable
fun AuthScreen(
    authRepository: AuthRepository,
    onAuthSuccess: (UserProfile) -> Unit,
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isEmailMode by remember { mutableStateOf(false) }
    var phoneInput by remember { mutableStateOf("9876543210") }
    var emailInput by remember { mutableStateOf("student@kidsg.in") }
    var isOtpSent by remember { mutableStateOf(false) }
    var otpInput by remember { mutableStateOf("123456") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var countdownSeconds by remember { mutableStateOf(30) }

    // Resend countdown timer
    LaunchedEffect(isOtpSent) {
        if (isOtpSent) {
            countdownSeconds = 30
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
            // Top Bar
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
                            if (isOtpSent) {
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

            Spacer(modifier = Modifier.height(24.dp))

            // KidsG Logo / Mascot
            KidsGIllustration.Mascot(modifier = Modifier.size(80.dp))

            Spacer(modifier = Modifier.height(16.dp))

            if (!isOtpSent) {
                // Step 1: Input Phone or Email
                Text(
                    text = "Welcome to KidsG",
                    style = KidsGTypography.DisplaySmall.copy(
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = KidsGColors.BlackText
                    )
                )

                Text(
                    text = "Enter your contact details to setup your student desk",
                    style = KidsGTypography.BodyMedium.copy(
                        color = KidsGColors.TextSecondary,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.padding(top = 6.dp, start = 16.dp, end = 16.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Tab Switcher: Mobile Number vs Email
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
                            .background(if (!isEmailMode) KidsGColors.White else Color.Transparent)
                            .clickable { isEmailMode = false }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Phone Number",
                            style = KidsGTypography.BodyMedium.copy(
                                fontWeight = if (!isEmailMode) FontWeight.Bold else FontWeight.Normal,
                                color = if (!isEmailMode) KidsGColors.BlackText else KidsGColors.TextSecondary
                            )
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isEmailMode) KidsGColors.White else Color.Transparent)
                            .clickable { isEmailMode = true }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Email Address",
                            style = KidsGTypography.BodyMedium.copy(
                                fontWeight = if (isEmailMode) FontWeight.Bold else FontWeight.Normal,
                                color = if (isEmailMode) KidsGColors.BlackText else KidsGColors.TextSecondary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (!isEmailMode) {
                    // Mobile Number Input
                    OutlinedTextField(
                        value = phoneInput,
                        onValueChange = { if (it.length <= 10) phoneInput = it.filter { c -> c.isDigit() } },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        leadingIcon = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(start = 12.dp, end = 8.dp)
                            ) {
                                Text(text = "🇮🇳", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "+91",
                                    fontWeight = FontWeight.Bold,
                                    color = KidsGColors.BlackText
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .height(20.dp)
                                        .background(KidsGColors.BorderSubtle)
                                )
                            }
                        },
                        placeholder = { Text("Enter 10-digit mobile number") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = KidsGColors.OrangePrimary,
                            unfocusedBorderColor = KidsGColors.BorderSubtle,
                            focusedContainerColor = KidsGColors.White,
                            unfocusedContainerColor = KidsGColors.White
                        )
                    )
                } else {
                    // Email Address Input
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        leadingIcon = { Text("✉️", fontSize = 18.sp, modifier = Modifier.padding(start = 12.dp)) },
                        placeholder = { Text("Enter your email address") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = KidsGColors.OrangePrimary,
                            unfocusedBorderColor = KidsGColors.BorderSubtle,
                            focusedContainerColor = KidsGColors.White,
                            unfocusedContainerColor = KidsGColors.White
                        )
                    )
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = errorMessage ?: "",
                        style = KidsGTypography.BodySmall.copy(color = Color(0xFFD32F2F)),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        val contact = if (!isEmailMode) "+91$phoneInput" else emailInput
                        if ((!isEmailMode && phoneInput.length < 10) || (isEmailMode && !emailInput.contains("@"))) {
                            errorMessage = "Please enter a valid " + (if (isEmailMode) "email address" else "phone number")
                            return@Button
                        }
                        errorMessage = null
                        isLoading = true
                        coroutineScope.launch {
                            val result = authRepository.requestOtp(contact)
                            isLoading = false
                            if (result.isSuccess) {
                                isOtpSent = true
                            } else {
                                errorMessage = result.exceptionOrNull()?.message ?: "Failed to send OTP"
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = KidsGColors.OrangePrimary),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp), color = KidsGColors.White, strokeWidth = 2.dp)
                    } else {
                        Text(
                            text = "Send OTP",
                            style = KidsGTypography.TitleMedium.copy(
                                color = KidsGColors.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            } else {
                // Step 2: Enter 6-Digit OTP
                Text(
                    text = "Verify OTP",
                    style = KidsGTypography.DisplaySmall.copy(
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = KidsGColors.BlackText
                    )
                )

                val destination = if (!isEmailMode) "+91 $phoneInput" else emailInput
                Text(
                    text = "Enter the 6-digit code sent to\n$destination",
                    style = KidsGTypography.BodyMedium.copy(
                        color = KidsGColors.TextSecondary,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.padding(top = 6.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 6-Digit OTP Boxes
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (i in 0 until 6) {
                        val digit = otpInput.getOrNull(i)?.toString() ?: ""
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(KidsGColors.White)
                                .border(
                                    width = if (digit.isNotEmpty()) 2.dp else 1.dp,
                                    color = if (digit.isNotEmpty()) KidsGColors.OrangePrimary else KidsGColors.BorderSubtle,
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = digit,
                                style = KidsGTypography.TitleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = KidsGColors.BlackText
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Hidden/compact OTP field for user keyboard input
                OutlinedTextField(
                    value = otpInput,
                    onValueChange = { if (it.length <= 6) otpInput = it.filter { c -> c.isDigit() } },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Enter OTP here (default: 123456)") },
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = KidsGColors.OrangePrimary,
                        unfocusedBorderColor = KidsGColors.BorderSubtle,
                        focusedContainerColor = KidsGColors.White,
                        unfocusedContainerColor = KidsGColors.White
                    )
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = errorMessage ?: "",
                        style = KidsGTypography.BodySmall.copy(color = Color(0xFFD32F2F)),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Resend Timer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (countdownSeconds > 0) {
                        Text(
                            text = "Resend OTP in ${countdownSeconds}s",
                            style = KidsGTypography.BodyMedium.copy(color = KidsGColors.TextSecondary)
                        )
                    } else {
                        Text(
                            text = "Resend OTP",
                            style = KidsGTypography.BodyMedium.copy(
                                color = KidsGColors.OrangePrimary,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.clickable {
                                val contact = if (!isEmailMode) "+91$phoneInput" else emailInput
                                coroutineScope.launch {
                                    authRepository.requestOtp(contact)
                                    countdownSeconds = 30
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        val contact = if (!isEmailMode) "+91$phoneInput" else emailInput
                        if (otpInput.length < 6) {
                            errorMessage = "Please enter 6-digit OTP code"
                            return@Button
                        }
                        errorMessage = null
                        isLoading = true
                        coroutineScope.launch {
                            val result = authRepository.verifyOtp(contact, otpInput)
                            isLoading = false
                            if (result.isSuccess) {
                                val profile = result.getOrNull() ?: UserProfile(
                                    id = "user_${System.currentTimeMillis()}",
                                    name = "Student",
                                    phone = if (!isEmailMode) "+91$phoneInput" else "",
                                    email = if (isEmailMode) emailInput else ""
                                )
                                onAuthSuccess(profile)
                            } else {
                                errorMessage = result.exceptionOrNull()?.message ?: "Invalid OTP"
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = KidsGColors.OrangePrimary),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp), color = KidsGColors.White, strokeWidth = 2.dp)
                    } else {
                        Text(
                            text = "Verify & Continue",
                            style = KidsGTypography.TitleMedium.copy(
                                color = KidsGColors.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
