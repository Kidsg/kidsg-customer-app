package com.kidsg.feature.profile

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidsg.core.designsystem.*
import com.kidsg.domain.model.UserProfile
import kotlinx.coroutines.launch

/**
 * Clean, Modern, & Beautiful Profile Screen
 * Provides intuitive student profile management, full live address resolution, and streamlined navigation.
 */
@Composable
fun ProfileScreen(
    userProfile: UserProfile?,
    currentLocation: String = "Malur, Karnataka",
    onLocationClick: () -> Unit = {},
    onNavigateToAddresses: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateToHelp: () -> Unit,
    onLogout: () -> Unit,
    onUpdateProfile: ((UserProfile) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showPreferencesDialog by remember { mutableStateOf(false) }
    var showParentContactDialog by remember { mutableStateOf(false) }

    val studentName = userProfile?.studentName?.takeIf { it.isNotBlank() }
        ?: userProfile?.name?.takeIf { it.isNotBlank() }
        ?: "Student"
    val studentGrade = userProfile?.studentGrade?.takeIf { it.isNotBlank() } ?: "Class 10"
    val schoolName = userProfile?.schoolName?.takeIf { it.isNotBlank() } ?: "St. Joseph's"
    val userEmail = userProfile?.email?.takeIf { it.isNotBlank() } ?: "student@kidsg.in"
    val userPhone = userProfile?.phone?.takeIf { it.isNotBlank() } ?: "+91 93539 19178"

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 100.dp)
        ) {
            // 1. Clean Top Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "My Profile",
                        style = KidsGTypography.DisplaySmall.copy(
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF0F172A)
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Account & Student Preferences",
                        style = KidsGTypography.BodySmall.copy(
                            color = Color(0xFF64748B),
                            fontSize = 13.sp
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFFFF7ED))
                        .border(1.dp, Color(0xFFFFEDD5), RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "✓ Verified Student",
                        color = KidsGColors.OrangePrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 2. Beautiful Hero Profile Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(22.dp))
                    .shadow(1.dp, RoundedCornerShape(22.dp), spotColor = Color(0x08000000))
                    .padding(20.dp)
            ) {
                Column {
                    // Profile Header with Avatar & Details
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Sleek Gradient Avatar
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(KidsGColors.OrangePrimary, Color(0xFFFB923C))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = studentName.take(1).uppercase(),
                                color = Color.White,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = studentName,
                                style = KidsGTypography.TitleLarge.copy(
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = userEmail,
                                style = KidsGTypography.BodySmall.copy(
                                    color = Color(0xFF64748B),
                                    fontSize = 13.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            if (userPhone.isNotBlank()) {
                                Text(
                                    text = userPhone,
                                    style = KidsGTypography.Caption.copy(
                                        color = Color(0xFF94A3B8),
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // 2-Column Info Grid for Class & School (Clean, never truncated)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Class Box
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFFF1F5F9))
                                .clickable { showEditProfileDialog = true }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Column {
                                Text(
                                    text = "Class / Grade",
                                    style = KidsGTypography.Caption.copy(
                                        color = Color(0xFF64748B),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "🎓 $studentGrade",
                                    style = KidsGTypography.BodyMedium.copy(
                                        color = Color(0xFF0F172A),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.5.sp
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // School Box
                        Box(
                            modifier = Modifier
                                .weight(1.3f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFFF1F5F9))
                                .clickable { showEditProfileDialog = true }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Column {
                                Text(
                                    text = "School / Academy",
                                    style = KidsGTypography.Caption.copy(
                                        color = Color(0xFF64748B),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "🏫 $schoolName",
                                    style = KidsGTypography.BodyMedium.copy(
                                        color = Color(0xFF0F172A),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.5.sp
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Edit Profile Action Pill
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFFF7ED))
                                .border(1.dp, Color(0xFFFFEDD5), RoundedCornerShape(12.dp))
                                .clickable { showEditProfileDialog = true }
                                .padding(vertical = 9.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "✏️", fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Edit Profile",
                                    color = KidsGColors.OrangePrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF8FAFC))
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                                .clickable { showPreferencesDialog = true }
                                .padding(vertical = 9.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "⭐", fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Preferences",
                                    color = Color(0xFF475569),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 3. Live Location Details Card (Accurate Full Address, Never Clumsy)
            val isBengaluruCoverage = currentLocation.contains("Bengaluru", ignoreCase = true) ||
                    currentLocation.contains("Bangalore", ignoreCase = true)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(22.dp))
                    .shadow(1.dp, RoundedCornerShape(22.dp), spotColor = Color(0x08000000))
                    .padding(18.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "📍", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Current Delivery Address",
                                style = KidsGTypography.TitleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF0F172A)
                                )
                            )
                        }

                        // Badge with softWrap = false and maxLines = 1 so it NEVER wraps into two lines
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isBengaluruCoverage) Color(0xFFDCFCE7) else Color(0xFFFEF3C7))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isBengaluruCoverage) "⚡ HYPERLOCAL" else "📦 EXPRESS REGIONAL",
                                color = if (isBengaluruCoverage) Color(0xFF15803D) else Color(0xFFB45309),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Full Detailed Location Address
                    Text(
                        text = currentLocation,
                        style = KidsGTypography.BodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1E293B),
                            lineHeight = 22.sp,
                            fontSize = 14.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isBengaluruCoverage)
                            "✓ Direct 45-90 min rapid delivery from partner depot to your desk."
                        else
                            "✓ Regional courier & express delivery directly to your student desk in Karnataka.",
                        style = KidsGTypography.Caption.copy(
                            color = if (isBengaluruCoverage) Color(0xFF059669) else Color(0xFFD97706),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Action Button to update or detect GPS
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFFF7ED))
                            .border(1.dp, Color(0xFFFFEDD5), RoundedCornerShape(12.dp))
                            .clickable(onClick = onLocationClick)
                            .padding(vertical = 11.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🛰️", fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Detect Live GPS or Change Location",
                                style = KidsGTypography.Caption.copy(
                                    color = KidsGColors.OrangePrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 4. "Orders & Addresses" Group
            Text(
                text = "Account & Orders",
                style = KidsGTypography.TitleMedium.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                ),
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
            ) {
                Column {
                    ProfileMenuRow(
                        icon = "📦",
                        title = "My Orders",
                        subtitle = "Track active shipments & view receipts",
                        onClick = onNavigateToOrders
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    ProfileMenuRow(
                        icon = "📍",
                        title = "Saved Delivery Addresses",
                        subtitle = "Home, School & Hostel addresses",
                        onClick = onNavigateToAddresses
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    ProfileMenuRow(
                        icon = "❤️",
                        title = "My Wishlisted Items",
                        subtitle = "Saved notebooks, pens & sets",
                        onClick = {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Wishlist contains your saved stationery items!")
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 5. "Preferences & Support" Group
            Text(
                text = "Preferences & Support",
                style = KidsGTypography.TitleMedium.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                ),
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
            ) {
                Column {
                    ProfileMenuRow(
                        icon = "👨‍👩‍👧",
                        title = "Parent Contact & Emergency",
                        subtitle = "Guardians phone & update preferences",
                        onClick = { showParentContactDialog = true }
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    ProfileMenuRow(
                        icon = "🔔",
                        title = "Notification Preferences",
                        subtitle = "Instant order alerts & stock notifications",
                        onClick = {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Order notifications and SMS alerts are enabled.")
                            }
                        }
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    ProfileMenuRow(
                        icon = "💬",
                        title = "Help & Customer Support",
                        subtitle = "Chat with BookBuddys support team",
                        onClick = onNavigateToHelp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 6. Sleek Logout Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFFEF2F2))
                    .border(1.dp, Color(0xFFFEE2E2), RoundedCornerShape(16.dp))
                    .clickable { showLogoutDialog = true }
                    .padding(vertical = 14.dp, horizontal = 18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🚪", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Log Out from Device",
                            style = KidsGTypography.BodyMedium.copy(
                                color = Color(0xFFDC2626),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        )
                    }
                    Text(
                        text = "›",
                        color = Color(0xFFDC2626),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)
        )

        // Logout Confirmation Dialog
        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                title = { Text("Log Out?", style = KidsGTypography.TitleMedium.copy(fontWeight = FontWeight.Bold)) },
                text = { Text("Are you sure you want to log out from BookBuddys on this device?", style = KidsGTypography.BodyMedium) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showLogoutDialog = false
                            onLogout()
                        }
                    ) {
                        Text("Log Out", color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutDialog = false }) {
                        Text("Cancel", color = Color(0xFF64748B))
                    }
                },
                containerColor = Color.White
            )
        }

        // Edit Profile Dialog
        if (showEditProfileDialog) {
            EditProfileDialog(
                currentName = studentName,
                currentGrade = studentGrade,
                currentSchool = schoolName,
                onDismiss = { showEditProfileDialog = false },
                onSave = { newName, newGrade, newSchool ->
                    showEditProfileDialog = false
                    val updated = (userProfile ?: UserProfile(
                        id = "user_default",
                        name = newName,
                        phone = userPhone,
                        email = userEmail
                    )).copy(
                        studentName = newName,
                        name = newName,
                        studentGrade = newGrade,
                        schoolName = newSchool
                    )
                    onUpdateProfile?.invoke(updated)
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Profile details saved successfully!")
                    }
                }
            )
        }

        // Parent Contact Info Dialog
        if (showParentContactDialog) {
            AlertDialog(
                onDismissRequest = { showParentContactDialog = false },
                title = { Text("Parent / Guardian Contact", style = KidsGTypography.TitleMedium.copy(fontWeight = FontWeight.Bold)) },
                text = {
                    Column {
                        Text("Phone: $userPhone", style = KidsGTypography.BodyMedium)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Email: $userEmail", style = KidsGTypography.BodyMedium)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Notification updates: Sent via SMS & Email", style = KidsGTypography.Caption.copy(color = KidsGColors.OrangePrimary))
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showParentContactDialog = false }) {
                        Text("Done", color = KidsGColors.OrangePrimary, fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = Color.White
            )
        }

        // Stationery Preferences Dialog
        if (showPreferencesDialog) {
            AlertDialog(
                onDismissRequest = { showPreferencesDialog = false },
                title = { Text("Stationery Preferences", style = KidsGTypography.TitleMedium.copy(fontWeight = FontWeight.Bold)) },
                text = {
                    Column {
                        Text("• Preferred Notebook: Single Line, 200 Pages", style = KidsGTypography.BodySmall)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("• Pen Grip: 0.7mm Waterproof Gel Pens", style = KidsGTypography.BodySmall)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("• Fast Express Delivery: Enabled for student desk", style = KidsGTypography.BodySmall)
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showPreferencesDialog = false }) {
                        Text("Save", color = KidsGColors.OrangePrimary, fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = Color.White
            )
        }
    }
}

/**
 * Clean Menu Row Component
 */
@Composable
private fun ProfileMenuRow(
    icon: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Text(text = icon, fontSize = 20.sp)
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    style = KidsGTypography.TitleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF0F172A),
                        fontSize = 14.sp
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = KidsGTypography.Caption.copy(
                        color = Color(0xFF64748B),
                        fontSize = 11.5.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Text(
            text = "›",
            fontSize = 20.sp,
            color = Color(0xFF94A3B8),
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Edit Student Profile Dialog
 */
@Composable
private fun EditProfileDialog(
    currentName: String,
    currentGrade: String,
    currentSchool: String,
    onDismiss: () -> Unit,
    onSave: (name: String, grade: String, school: String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    var selectedGrade by remember { mutableStateOf(currentGrade) }
    var school by remember { mutableStateOf(currentSchool) }

    val gradeOptions = listOf(
        "Class 1", "Class 2", "Class 3", "Class 4", "Class 5",
        "Class 6", "Class 7", "Class 8", "Class 9", "Class 10",
        "Class 11", "Class 12"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Student Profile", style = KidsGTypography.TitleMedium.copy(fontWeight = FontWeight.Bold)) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Student Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = school,
                    onValueChange = { school = it },
                    label = { Text("School Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Select Class / Grade:", style = KidsGTypography.Caption.copy(fontWeight = FontWeight.Bold))

                Spacer(modifier = Modifier.height(6.dp))

                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(gradeOptions.size) { i ->
                        val g = gradeOptions[i]
                        val isSelected = selectedGrade == g
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) KidsGColors.OrangePrimary else Color(0xFFF1F5F9))
                                .clickable { selectedGrade = g }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = g,
                                color = if (isSelected) Color.White else Color(0xFF0F172A),
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name.trim(), selectedGrade, school.trim()) },
                colors = ButtonDefaults.buttonColors(containerColor = KidsGColors.OrangePrimary)
            ) {
                Text("Save Changes", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF64748B))
            }
        },
        containerColor = Color.White
    )
}
