package com.kidsg.feature.shoppartner

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidsg.core.designsystem.KidsGColors
import com.kidsg.core.designsystem.KidsGTypography
import kotlinx.coroutines.launch

private enum class AuthView {
    WELCOME,
    LOGIN,
    REGISTER
}

@Composable
fun ShopPartnerAuthScreen(
    onAuthSuccess: (ShopPartnerProfile) -> Unit,
    onBackToCustomerApp: () -> Unit
) {
    var currentView by remember { mutableStateOf(AuthView.WELCOME) }
    var isOtpTab by remember { mutableStateOf(false) }

    // Login state
    var emailInput by remember { mutableStateOf("shop@kidsg.in") }
    var passwordInput by remember { mutableStateOf("Partner123!") }

    // Register state
    var registerOwnerName by remember { mutableStateOf("Ramesh Kumar") }
    var registerShopName by remember { mutableStateOf("KidsG Stationery") }
    var registerPhone by remember { mutableStateOf("+91 98765 43210") }
    var registerEmail by remember { mutableStateOf("rameshshop@gmail.com") }
    var registerPassword by remember { mutableStateOf("Partner123!") }
    var registerAddress by remember { mutableStateOf("No. 42, 12th Main Road") }
    var registerArea by remember { mutableStateOf("Malleshwaram") }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(KidsGColors.BackgroundSoft)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Top Bar
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentView != AuthView.WELCOME) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(KidsGColors.White)
                            .clickable { currentView = AuthView.WELCOME },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("←", fontSize = 20.sp, color = KidsGColors.BlackText, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Spacer(modifier = Modifier.size(40.dp))
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = KidsGColors.OrangePrimary.copy(alpha = 0.12f),
                    modifier = Modifier.clickable { onBackToCustomerApp() }
                ) {
                    Text(
                        text = "🎒 Switch to Customer App",
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        style = KidsGTypography.BodySmall.copy(
                            color = KidsGColors.OrangePrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            // ================================================================
            // VIEW 1: WELCOME SCREEN
            // ================================================================
            if (currentView == AuthView.WELCOME) {
                Spacer(modifier = Modifier.height(20.dp))

                // Brand Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "BookBuddys",
                        style = KidsGTypography.DisplaySmall.copy(
                            color = KidsGColors.OrangePrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 32.sp
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Shop Partner",
                        style = KidsGTypography.TitleLarge.copy(
                            color = KidsGColors.BlackText,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Shop Storefront Illustration Card
                Surface(
                    modifier = Modifier.size(240.dp),
                    shape = RoundedCornerShape(28.dp),
                    color = KidsGColors.White,
                    shadowElevation = 4.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🏪", fontSize = 80.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Vidya Book Depot",
                                style = KidsGTypography.TitleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                "Stationery & School Supplies",
                                style = KidsGTypography.BodySmall.copy(color = KidsGColors.TextSecondary)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(36.dp))

                Text(
                    text = "Grow your business\nwith KidsG",
                    style = KidsGTypography.DisplaySmall.copy(
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = KidsGColors.BlackText,
                        textAlign = TextAlign.Center
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Get more orders. Reach more students.\nManage everything in one app.",
                    style = KidsGTypography.BodyMedium.copy(
                        color = KidsGColors.TextSecondary,
                        textAlign = TextAlign.Center
                    )
                )

                Spacer(modifier = Modifier.height(36.dp))

                // Action Buttons
                Button(
                    onClick = { currentView = AuthView.LOGIN },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = KidsGColors.OrangePrimary)
                ) {
                    Text(
                        text = "Get Started →",
                        style = KidsGTypography.TitleSmall.copy(color = Color.White, fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.clickable { currentView = AuthView.LOGIN },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Already a shop partner? ", style = KidsGTypography.BodyMedium.copy(color = KidsGColors.TextSecondary))
                    Text("Login", style = KidsGTypography.BodyMedium.copy(color = KidsGColors.OrangePrimary, fontWeight = FontWeight.Bold))
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.clickable { currentView = AuthView.REGISTER },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Want to partner? ", style = KidsGTypography.BodyMedium.copy(color = KidsGColors.TextSecondary))
                    Text("Register Your Shop", style = KidsGTypography.BodyMedium.copy(color = KidsGColors.GreenFresh, fontWeight = FontWeight.Bold))
                }
            }

            // ================================================================
            // VIEW 2: LOGIN SCREEN
            // ================================================================
            else if (currentView == AuthView.LOGIN) {
                Text(
                    text = "Welcome Back!",
                    style = KidsGTypography.DisplaySmall.copy(
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = KidsGColors.BlackText
                    )
                )

                Text(
                    text = "Login to your shop partner account",
                    style = KidsGTypography.BodyMedium.copy(color = KidsGColors.TextSecondary),
                    modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                )

                // Password vs OTP Switcher
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(KidsGColors.White)
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (!isOtpTab) KidsGColors.OrangePrimary else Color.Transparent)
                            .clickable { isOtpTab = false }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Password",
                            style = KidsGTypography.BodyMedium.copy(
                                color = if (!isOtpTab) Color.White else KidsGColors.TextSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isOtpTab) KidsGColors.OrangePrimary else Color.Transparent)
                            .clickable { isOtpTab = true }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "OTP",
                            style = KidsGTypography.BodyMedium.copy(
                                color = if (isOtpTab) Color.White else KidsGColors.TextSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Email Field
                OutlinedTextField(
                    value = emailInput,
                    onValueChange = { emailInput = it },
                    label = { Text("Email Address") },
                    placeholder = { Text("shopowner@gmail.com") },
                    leadingIcon = { Text("✉️", fontSize = 16.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = KidsGColors.OrangePrimary,
                        unfocusedContainerColor = KidsGColors.White,
                        focusedContainerColor = KidsGColors.White
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (!isOtpTab) {
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("Password") },
                        leadingIcon = { Text("🔒", fontSize = 16.sp) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = KidsGColors.OrangePrimary,
                            unfocusedContainerColor = KidsGColors.White,
                            focusedContainerColor = KidsGColors.White
                        )
                    )
                } else {
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("Enter 6-digit Partner OTP") },
                        leadingIcon = { Text("🔢", fontSize = 16.sp) },
                        placeholder = { Text("Enter code") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = KidsGColors.OrangePrimary,
                            unfocusedContainerColor = KidsGColors.White,
                            focusedContainerColor = KidsGColors.White
                        )
                    )
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = errorMessage ?: "",
                        style = KidsGTypography.BodySmall.copy(color = KidsGColors.RedPrimary, fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Login Button
                Button(
                    onClick = {
                        isLoading = true
                        errorMessage = null
                        coroutineScope.launch {
                            val res = ShopPartnerService.login(emailInput, passwordInput)
                            isLoading = false
                            res.onSuccess {
                                onAuthSuccess(it)
                            }.onFailure {
                                errorMessage = it.message ?: "Login failed"
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
                            text = "Login →",
                            style = KidsGTypography.TitleSmall.copy(color = Color.White, fontWeight = FontWeight.Bold)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Quick Demo Accounts Fillers for fast testing
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = KidsGColors.White,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "⚡ Quick Demo Login Credentials",
                            style = KidsGTypography.LabelMedium.copy(fontWeight = FontWeight.Bold, color = KidsGColors.BlackText)
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    emailInput = "shop@kidsg.in"
                                    passwordInput = "Partner123!"
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("🏪 Shop Owner", style = KidsGTypography.LabelSmall.copy(color = KidsGColors.OrangePrimary, fontWeight = FontWeight.Bold))
                            }

                            OutlinedButton(
                                onClick = {
                                    emailInput = "rider@kidsg.in"
                                    passwordInput = "Rider123!"
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("🛵 Rider Demo", style = KidsGTypography.LabelSmall.copy(color = KidsGColors.BlueAction, fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.clickable { currentView = AuthView.REGISTER },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Don't have a shop account? ", style = KidsGTypography.BodyMedium.copy(color = KidsGColors.TextSecondary))
                    Text("Register", style = KidsGTypography.BodyMedium.copy(color = KidsGColors.OrangePrimary, fontWeight = FontWeight.Bold))
                }
            }

            // ================================================================
            // VIEW 3: REGISTER SCREEN
            // ================================================================
            else if (currentView == AuthView.REGISTER) {
                Text(
                    text = "Register Your Shop",
                    style = KidsGTypography.DisplaySmall.copy(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = KidsGColors.BlackText
                    )
                )

                Text(
                    text = "Join KidsG and start receiving orders",
                    style = KidsGTypography.BodyMedium.copy(color = KidsGColors.TextSecondary),
                    modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                )

                // 3-step indicator matching design: 1 Owner, 2 Shop, 3 Details
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(shape = CircleShape, color = KidsGColors.OrangePrimary) {
                        Text("1", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Text(" Owner ", style = KidsGTypography.BodySmall.copy(fontWeight = FontWeight.Bold, color = KidsGColors.OrangePrimary))
                    Box(modifier = Modifier.width(20.dp).height(1.dp).background(KidsGColors.BorderSubtle))
                    Surface(shape = CircleShape, color = KidsGColors.BorderSubtle) {
                        Text("2", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), color = KidsGColors.BlackText)
                    }
                    Text(" Shop ", style = KidsGTypography.BodySmall.copy(color = KidsGColors.TextSecondary))
                    Box(modifier = Modifier.width(20.dp).height(1.dp).background(KidsGColors.BorderSubtle))
                    Surface(shape = CircleShape, color = KidsGColors.BorderSubtle) {
                        Text("3", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), color = KidsGColors.BlackText)
                    }
                    Text(" Details", style = KidsGTypography.BodySmall.copy(color = KidsGColors.TextSecondary))
                }

                OutlinedTextField(
                    value = registerOwnerName,
                    onValueChange = { registerOwnerName = it },
                    label = { Text("Owner Name") },
                    placeholder = { Text("Ramesh Kumar") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = KidsGColors.OrangePrimary, unfocusedContainerColor = KidsGColors.White, focusedContainerColor = KidsGColors.White)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = registerShopName,
                    onValueChange = { registerShopName = it },
                    label = { Text("Shop Name") },
                    placeholder = { Text("KidsG Stationery Depot") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = KidsGColors.OrangePrimary, unfocusedContainerColor = KidsGColors.White, focusedContainerColor = KidsGColors.White)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = registerPhone,
                    onValueChange = { registerPhone = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = KidsGColors.OrangePrimary, unfocusedContainerColor = KidsGColors.White, focusedContainerColor = KidsGColors.White)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = registerEmail,
                    onValueChange = { registerEmail = it },
                    label = { Text("Email Address") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = KidsGColors.OrangePrimary, unfocusedContainerColor = KidsGColors.White, focusedContainerColor = KidsGColors.White)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = registerAddress,
                    onValueChange = { registerAddress = it },
                    label = { Text("Shop Address") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = KidsGColors.OrangePrimary, unfocusedContainerColor = KidsGColors.White, focusedContainerColor = KidsGColors.White)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = registerPassword,
                    onValueChange = { registerPassword = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = KidsGColors.OrangePrimary, unfocusedContainerColor = KidsGColors.White, focusedContainerColor = KidsGColors.White)
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = errorMessage ?: "",
                        style = KidsGTypography.BodySmall.copy(color = KidsGColors.RedPrimary, fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        isLoading = true
                        errorMessage = null
                        coroutineScope.launch {
                            val res = ShopPartnerService.register(
                                ownerName = registerOwnerName,
                                shopName = registerShopName,
                                phone = registerPhone,
                                email = registerEmail,
                                pass = registerPassword,
                                address = registerAddress,
                                area = registerArea
                            )
                            isLoading = false
                            res.onSuccess {
                                onAuthSuccess(it)
                            }.onFailure {
                                errorMessage = it.message ?: "Failed to register"
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
                            text = "Next: Submit Application →",
                            style = KidsGTypography.TitleSmall.copy(color = Color.White, fontWeight = FontWeight.Bold)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.clickable { currentView = AuthView.LOGIN },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Already registered? ", style = KidsGTypography.BodyMedium.copy(color = KidsGColors.TextSecondary))
                    Text("Login", style = KidsGTypography.BodyMedium.copy(color = KidsGColors.OrangePrimary, fontWeight = FontWeight.Bold))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
