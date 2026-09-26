package com.kidsg.feature.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidsg.core.designsystem.KidsGColors
import com.kidsg.core.designsystem.KidsGShapes
import com.kidsg.core.designsystem.KidsGTypography
import com.kidsg.domain.model.UserProfile
import kotlinx.coroutines.launch

/**
 * KidsG My Profile Screen
 * Built directly adhering to the KidsG Visual Reference:
 * - Header with student mascot doodle, crown and star accents.
 * - Hero Student Profile Card with avatar, camera edit badge, class/school subtitle, and "Edit Profile >".
 * - 4 Quick-Stat / Preference Chips (Class, School, Parent Contact, Preferences).
 * - Promotional Stationery Experience Banner with smiling backpack character.
 * - Grouped "Account & Orders" section.
 * - Grouped "App Settings" section.
 * - Logout pill card with confirmation dialog.
 */
@Composable
fun ProfileScreen(
    userProfile: UserProfile?,
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
        ?: "Veenith Kumar S"
    val studentGrade = userProfile?.studentGrade?.takeIf { it.isNotBlank() } ?: "Class 10"
    val schoolName = userProfile?.schoolName?.takeIf { it.isNotBlank() } ?: "VBC School"
    val gradeNumber = studentGrade.replace("Class ", "").trim().ifBlank { "10" }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 100.dp)
        ) {
            // 1. Header with playful mascot avatar illustration
            ProfileHeaderSection()

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Main Profile Card with Quick-Stat Chips
            StudentHeroProfileCard(
                studentName = studentName,
                studentGrade = studentGrade,
                schoolName = schoolName,
                gradeNumber = gradeNumber,
                onEditProfileClick = { showEditProfileDialog = true },
                onClassClick = { showEditProfileDialog = true },
                onSchoolClick = { showEditProfileDialog = true },
                onParentContactClick = { showParentContactDialog = true },
                onPreferencesClick = { showPreferencesDialog = true },
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 3. Promotional Stationery Experience Banner
            StationeryExperienceBanner(
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 4. "Account & Orders" Section
            Text(
                text = "Account & Orders",
                style = KidsGTypography.TitleMedium.copy(
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111111)
                ),
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            ProfileMenuGroup(
                items = listOf(
                    ProfileMenuItemData(
                        icon = "📍",
                        title = "My Addresses",
                        onClick = onNavigateToAddresses
                    ),
                    ProfileMenuItemData(
                        icon = "📦",
                        title = "My Orders",
                        onClick = onNavigateToOrders
                    ),
                    ProfileMenuItemData(
                        icon = "❤️",
                        title = "My Wishlisted Items",
                        onClick = {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Wishlist contains your saved stationery items!")
                            }
                        }
                    )
                ),
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 5. "App Settings" Section
            Text(
                text = "App Settings",
                style = KidsGTypography.TitleMedium.copy(
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111111)
                ),
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            ProfileMenuGroup(
                items = listOf(
                    ProfileMenuItemData(
                        icon = "🔔",
                        title = "Notifications",
                        onClick = {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Notification preferences: Active order updates enabled.")
                            }
                        }
                    ),
                    ProfileMenuItemData(
                        icon = "⚙️",
                        title = "Settings",
                        onClick = {
                            showPreferencesDialog = true
                        }
                    ),
                    ProfileMenuItemData(
                        icon = "❓",
                        title = "Help & Support",
                        onClick = onNavigateToHelp
                    )
                ),
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 6. Logout Pill Button
            LogoutPillCard(
                onClick = { showLogoutDialog = true },
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Notification Snackbar Host
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)
        )

        // Logout Confirmation Dialog
        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                title = { Text("Log Out?", style = KidsGTypography.TitleMedium) },
                text = { Text("Are you sure you want to log out from KidsG?", style = KidsGTypography.BodyMedium) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showLogoutDialog = false
                            onLogout()
                        }
                    ) {
                        Text("Log Out", color = KidsGColors.Error, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutDialog = false }) {
                        Text("Cancel", color = KidsGColors.TextSecondary)
                    }
                },
                containerColor = KidsGColors.White
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
                        phone = "+91 91484 73131",
                        email = "student@kidsg.app"
                    )).copy(
                        studentName = newName,
                        name = newName,
                        studentGrade = newGrade,
                        schoolName = newSchool
                    )
                    onUpdateProfile?.invoke(updated)
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Profile updated successfully!")
                    }
                }
            )
        }

        // Parent Contact Info Dialog
        if (showParentContactDialog) {
            AlertDialog(
                onDismissRequest = { showParentContactDialog = false },
                title = { Text("Parent Contact", style = KidsGTypography.TitleMedium) },
                text = {
                    Column {
                        Text("Phone: ${userProfile?.phone ?: "+91 91484 73131"}", style = KidsGTypography.BodyMedium)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Email: ${userProfile?.email ?: "parent@kidsg.app"}", style = KidsGTypography.BodyMedium)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Parent Mode: ${if (userProfile?.isParentMode == true) "Active" else "Student Mode"}", style = KidsGTypography.Caption.copy(color = KidsGColors.OrangePrimary))
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showParentContactDialog = false }) {
                        Text("Done", color = KidsGColors.OrangePrimary, fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = KidsGColors.White
            )
        }

        // Stationery Preferences Dialog
        if (showPreferencesDialog) {
            AlertDialog(
                onDismissRequest = { showPreferencesDialog = false },
                title = { Text("Stationery Preferences", style = KidsGTypography.TitleMedium) },
                text = {
                    Column {
                        Text("• Preferred Notebook: Single Line, 200 Pages", style = KidsGTypography.BodySmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("• Pen Grip: 0.7mm Gel / Waterproof", style = KidsGTypography.BodySmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("• Fast Express Delivery: Enabled", style = KidsGTypography.BodySmall)
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showPreferencesDialog = false }) {
                        Text("Save", color = KidsGColors.OrangePrimary, fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = KidsGColors.White
            )
        }
    }
}

/**
 * 1. Screen Header:
 * "My Profile" + Subtitle + Waving student mascot doodle with golden crown.
 */
@Composable
private fun ProfileHeaderSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "My ",
                        style = KidsGTypography.DisplayLarge.copy(
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF111111)
                        )
                    )
                    Text(
                        text = "Profile",
                        style = KidsGTypography.DisplayLarge.copy(
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = KidsGColors.OrangePrimary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Manage your account and preferences",
                    style = KidsGTypography.BodySmall.copy(
                        fontSize = 13.sp,
                        color = Color(0xFF6B7280)
                    )
                )
            }

            // Playful Mascot Student Avatar Illustration
            WavingStudentMascotIllustration(
                modifier = Modifier
                    .size(86.dp)
                    .offset(y = (-4).dp)
            )
        }
    }
}

/**
 * Waving student mascot with warm orange backpack, playful doodles & golden crown.
 */
@Composable
private fun WavingStudentMascotIllustration(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "mascotWaving")
    val waveRotation by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "waveRot"
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // 1. Warm ambient background circle
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFFF2E2), Color(0xFFFFF9F0), Color.Transparent),
                    center = Offset(w * 0.55f, h * 0.55f),
                    radius = w * 0.45f
                ),
                radius = w * 0.45f,
                center = Offset(w * 0.55f, h * 0.55f)
            )

            // 2. Golden crown doodle
            val crown = Path().apply {
                moveTo(w * 0.22f, h * 0.16f)
                lineTo(w * 0.26f, h * 0.06f)
                lineTo(w * 0.34f, h * 0.12f)
                lineTo(w * 0.42f, h * 0.04f)
                lineTo(w * 0.46f, h * 0.16f)
                close()
            }
            drawPath(path = crown, color = Color(0xFFFBBF24), style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))

            // Decorative star
            val star = Path().apply {
                moveTo(w * 0.56f, h * 0.14f)
                lineTo(w * 0.60f, h * 0.20f)
                lineTo(w * 0.68f, h * 0.20f)
                lineTo(w * 0.62f, h * 0.26f)
                lineTo(w * 0.64f, h * 0.32f)
                lineTo(w * 0.56f, h * 0.28f)
                lineTo(w * 0.48f, h * 0.32f)
                lineTo(w * 0.50f, h * 0.26f)
                lineTo(w * 0.44f, h * 0.20f)
                lineTo(w * 0.52f, h * 0.20f)
                close()
            }
            drawPath(path = star, color = Color(0xFFFBBF24), style = Stroke(width = 1.5.dp.toPx()))

            // Burst action lines
            drawLine(color = KidsGColors.OrangePrimary, start = Offset(w * 0.72f, h * 0.12f), end = Offset(w * 0.80f, h * 0.06f), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
            drawLine(color = KidsGColors.OrangePrimary, start = Offset(w * 0.82f, h * 0.20f), end = Offset(w * 0.90f, h * 0.18f), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)

            // 3. Orange Backpack behind student
            drawRoundRect(
                color = Color(0xFFF97316),
                topLeft = Offset(w * 0.52f, h * 0.45f),
                size = Size(w * 0.34f, h * 0.38f),
                cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
            )

            // 4. Student Body & Hoodie
            drawRoundRect(
                color = Color(0xFFFF7A00),
                topLeft = Offset(w * 0.30f, h * 0.62f),
                size = Size(w * 0.48f, h * 0.34f),
                cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
            )

            // 5. Student Head & Hair
            val headCenter = Offset(w * 0.52f, h * 0.46f)
            // Head skin
            drawCircle(color = Color(0xFFFFD1A4), radius = 18.dp.toPx(), center = headCenter)

            // Dark hair
            val hair = Path().apply {
                moveTo(headCenter.x - 18.dp.toPx(), headCenter.y - 4.dp.toPx())
                cubicTo(
                    headCenter.x - 18.dp.toPx(), headCenter.y - 24.dp.toPx(),
                    headCenter.x + 18.dp.toPx(), headCenter.y - 24.dp.toPx(),
                    headCenter.x + 18.dp.toPx(), headCenter.y - 4.dp.toPx()
                )
                cubicTo(headCenter.x + 10.dp.toPx(), headCenter.y - 10.dp.toPx(), headCenter.x - 10.dp.toPx(), headCenter.y - 10.dp.toPx(), headCenter.x - 18.dp.toPx(), headCenter.y - 4.dp.toPx())
                close()
            }
            drawPath(path = hair, color = Color(0xFF3E2723))

            // Eyes
            drawCircle(color = Color(0xFF111111), radius = 2.dp.toPx(), center = Offset(headCenter.x - 6.dp.toPx(), headCenter.y))
            drawCircle(color = Color(0xFF111111), radius = 2.dp.toPx(), center = Offset(headCenter.x + 6.dp.toPx(), headCenter.y))

            // Smile
            val smile = Path().apply {
                moveTo(headCenter.x - 4.dp.toPx(), headCenter.y + 6.dp.toPx())
                cubicTo(headCenter.x - 2.dp.toPx(), headCenter.y + 10.dp.toPx(), headCenter.x + 2.dp.toPx(), headCenter.y + 10.dp.toPx(), headCenter.x + 4.dp.toPx(), headCenter.y + 6.dp.toPx())
            }
            drawPath(path = smile, color = Color(0xFF111111), style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round))

            // Waving Hand
            val hand = Path().apply {
                moveTo(w * 0.22f, h * 0.40f)
                cubicTo(w * 0.16f, h * 0.32f, w * 0.26f, h * 0.26f, w * 0.32f, h * 0.34f)
                close()
            }
            drawPath(path = hand, color = Color(0xFFFFD1A4))
        }
    }
}

/**
 * 2. Hero Student Profile Card:
 * - Avatar with orange camera badge
 * - Student Name, Grade & School
 * - "Edit Profile >" button
 * - 4 Quick-Stat / Preference Chips (Class, School, Parent Contact, Preferences)
 */
@Composable
private fun StudentHeroProfileCard(
    studentName: String,
    studentGrade: String,
    schoolName: String,
    gradeNumber: String,
    onEditProfileClick: () -> Unit,
    onClassClick: () -> Unit,
    onSchoolClick: () -> Unit,
    onParentContactClick: () -> Unit,
    onPreferencesClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(KidsGColors.White)
            .border(1.dp, Color(0xFFF1F2F4), RoundedCornerShape(24.dp))
            .shadow(2.dp, RoundedCornerShape(24.dp), spotColor = Color(0x0A000000))
            .padding(18.dp)
    ) {
        Column {
            // Profile Info Top Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar with Camera Edit Badge
                Box(
                    modifier = Modifier.size(76.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFBAE6FD)),
                        contentAlignment = Alignment.Center
                    ) {
                        StudentAvatarFace(modifier = Modifier.size(54.dp))
                    }

                    // Orange Camera Badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(KidsGColors.OrangePrimary)
                            .clickable(onClick = onEditProfileClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "📷", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Name & Subtitles
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = studentName,
                        style = KidsGTypography.TitleLarge.copy(
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111111)
                        ),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "$studentGrade • $schoolName",
                        style = KidsGTypography.BodySmall.copy(
                            fontSize = 12.5.sp,
                            color = Color(0xFF6B7280)
                        ),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // "Edit Profile >" Button
                    Box(
                        modifier = Modifier
                            .clip(KidsGShapes.FullPill)
                            .background(Color(0xFFFFF7ED))
                            .border(1.dp, Color(0xFFFED7AA), KidsGShapes.FullPill)
                            .clickable(onClick = onEditProfileClick)
                            .padding(horizontal = 12.dp, vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "✏️", fontSize = 11.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Edit Profile  ›",
                                style = KidsGTypography.Caption.copy(
                                    color = KidsGColors.OrangePrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 4 Quick-Stat / Feature Cards in a row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 1. Class
                QuickFeatureCard(
                    icon = "🎓",
                    title = "Class",
                    value = gradeNumber,
                    bgColor = Color(0xFFFAF5FF),
                    onClick = onClassClick,
                    modifier = Modifier.weight(1f)
                )

                // 2. School
                QuickFeatureCard(
                    icon = "🏫",
                    title = "School",
                    value = schoolName.take(5),
                    bgColor = Color(0xFFF0F9FF),
                    onClick = onSchoolClick,
                    modifier = Modifier.weight(1f)
                )

                // 3. Parent Contact
                QuickFeatureCard(
                    icon = "👤",
                    title = "Parent\nContact",
                    value = "›",
                    bgColor = Color(0xFFF0FDF4),
                    onClick = onParentContactClick,
                    modifier = Modifier.weight(1.1f)
                )

                // 4. Preferences
                QuickFeatureCard(
                    icon = "💖",
                    title = "Preferences",
                    value = "›",
                    bgColor = Color(0xFFFFF1F2),
                    onClick = onPreferencesClick,
                    modifier = Modifier.weight(1.1f)
                )
            }
        }
    }
}

@Composable
private fun QuickFeatureCard(
    icon: String,
    title: String,
    value: String,
    bgColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = icon, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = KidsGTypography.Caption.copy(
                    fontSize = 10.sp,
                    color = Color(0xFF6B7280),
                    lineHeight = 12.sp,
                    textAlign = TextAlign.Center
                ),
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = KidsGTypography.Caption.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111111)
                )
            )
        }
    }
}

/**
 * Native avatar face drawn on canvas.
 */
@Composable
private fun StudentAvatarFace(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Face skin
        drawCircle(color = Color(0xFFFFD1A4), radius = w * 0.40f, center = Offset(w * 0.5f, h * 0.52f))

        // Brown Hair
        val hair = Path().apply {
            moveTo(w * 0.16f, h * 0.44f)
            cubicTo(w * 0.16f, h * 0.12f, w * 0.84f, h * 0.12f, w * 0.84f, h * 0.44f)
            cubicTo(w * 0.70f, h * 0.36f, w * 0.30f, h * 0.36f, w * 0.16f, h * 0.44f)
            close()
        }
        drawPath(path = hair, color = Color(0xFF4E342E))

        // Eyes
        drawCircle(color = Color(0xFF111111), radius = 2.dp.toPx(), center = Offset(w * 0.38f, h * 0.52f))
        drawCircle(color = Color(0xFF111111), radius = 2.dp.toPx(), center = Offset(w * 0.62f, h * 0.52f))

        // Smiling mouth
        val smile = Path().apply {
            moveTo(w * 0.42f, h * 0.64f)
            cubicTo(w * 0.46f, h * 0.72f, w * 0.54f, h * 0.72f, w * 0.58f, h * 0.64f)
        }
        drawPath(path = smile, color = Color(0xFF111111), style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round))
    }
}

/**
 * 3. Promotional Stationery Experience Banner:
 * "A better stationery experience for you!" + 3 benefits + smiling backpack character.
 */
@Composable
private fun StationeryExperienceBanner(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(Color(0xFFFFF7ED), Color(0xFFFFEDD5))
                )
            )
            .border(1.dp, Color(0xFFFED7AA), RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "A better stationery\nexperience for you!",
                    style = KidsGTypography.TitleMedium.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111111),
                        lineHeight = 22.sp
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🚚", fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(text = "Fast Delivery", style = KidsGTypography.Caption.copy(fontSize = 10.sp, color = Color(0xFF4B5563), fontWeight = FontWeight.SemiBold))
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🛡️", fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(text = "Safe & Reliable", style = KidsGTypography.Caption.copy(fontSize = 10.sp, color = Color(0xFF4B5563), fontWeight = FontWeight.SemiBold))
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "⭐", fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(text = "Quality", style = KidsGTypography.Caption.copy(fontSize = 10.sp, color = Color(0xFF4B5563), fontWeight = FontWeight.SemiBold))
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Smiling Yellow Backpack Illustration
            SmilingStationeryBackpackIllustration(
                modifier = Modifier.size(76.dp)
            )
        }
    }
}

/**
 * Smiling Stationery Backpack illustration with pencils and ruler.
 */
@Composable
private fun SmilingStationeryBackpackIllustration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Pencils & ruler sticking out from backpack
        // Green pencil
        drawRoundRect(
            color = Color(0xFF10B981),
            topLeft = Offset(w * 0.32f, h * 0.08f),
            size = Size(8.dp.toPx(), 28.dp.toPx()),
            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
        )
        // Blue ruler
        drawRoundRect(
            color = Color(0xFF38BDF8),
            topLeft = Offset(w * 0.46f, h * 0.04f),
            size = Size(10.dp.toPx(), 32.dp.toPx()),
            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
        )
        // Pink pencil
        drawRoundRect(
            color = Color(0xFFF472B6),
            topLeft = Offset(w * 0.62f, h * 0.10f),
            size = Size(8.dp.toPx(), 26.dp.toPx()),
            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
        )

        // Yellow Backpack Body
        drawRoundRect(
            color = Color(0xFFFBBF24),
            topLeft = Offset(w * 0.18f, h * 0.28f),
            size = Size(w * 0.68f, h * 0.68f),
            cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
        )

        // Front pocket
        drawRoundRect(
            color = Color(0xFFF59E0B),
            topLeft = Offset(w * 0.26f, h * 0.58f),
            size = Size(w * 0.52f, h * 0.34f),
            cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
        )

        // Cute smiling face on backpack
        drawCircle(color = Color(0xFF111111), radius = 2.dp.toPx(), center = Offset(w * 0.42f, h * 0.44f))
        drawCircle(color = Color(0xFF111111), radius = 2.dp.toPx(), center = Offset(w * 0.62f, h * 0.44f))
        val smile = Path().apply {
            moveTo(w * 0.48f, h * 0.50f)
            cubicTo(w * 0.50f, h * 0.54f, w * 0.54f, h * 0.54f, w * 0.56f, h * 0.50f)
        }
        drawPath(path = smile, color = Color(0xFF111111), style = Stroke(width = 1.4.dp.toPx(), cap = StrokeCap.Round))
    }
}

/**
 * Reusable Grouped Profile Menu Section Card
 */
data class ProfileMenuItemData(
    val icon: String,
    val title: String,
    val titleColor: Color = Color(0xFF111111),
    val onClick: () -> Unit
)

@Composable
private fun ProfileMenuGroup(
    items: List<ProfileMenuItemData>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(KidsGColors.White)
            .border(1.dp, Color(0xFFF3F4F6), RoundedCornerShape(18.dp))
            .shadow(1.dp, RoundedCornerShape(18.dp), spotColor = Color(0x06000000))
    ) {
        Column {
            items.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = item.onClick)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = item.icon, fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = item.title,
                            style = KidsGTypography.BodyLarge.copy(
                                fontSize = 14.5.sp,
                                color = item.titleColor,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }

                    Text(
                        text = "›",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF9CA3AF)
                    )
                }

                if (index < items.lastIndex) {
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFF8F9FA))
                }
            }
        }
    }
}

/**
 * 6. Logout Pill Button:
 * Soft red container, door icon, red text, and chevron.
 */
@Composable
private fun LogoutPillCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFFFEF2F2))
            .border(1.dp, Color(0xFFFEE2E2), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "🚪", fontSize = 18.sp)
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = "Logout",
                    style = KidsGTypography.BodyLarge.copy(
                        fontSize = 15.sp,
                        color = Color(0xFFEF4444),
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Text(
                text = "›",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFEF4444)
            )
        }
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
    var grade by remember { mutableStateOf(currentGrade) }
    var school by remember { mutableStateOf(currentSchool) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Student Profile", style = KidsGTypography.TitleMedium.copy(fontWeight = FontWeight.Bold)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Student Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = KidsGColors.OrangePrimary,
                        unfocusedBorderColor = Color(0xFFE5E7EB)
                    )
                )

                OutlinedTextField(
                    value = grade,
                    onValueChange = { grade = it },
                    label = { Text("Grade / Class (e.g. Class 10)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = KidsGColors.OrangePrimary,
                        unfocusedBorderColor = Color(0xFFE5E7EB)
                    )
                )

                OutlinedTextField(
                    value = school,
                    onValueChange = { school = it },
                    label = { Text("School Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = KidsGColors.OrangePrimary,
                        unfocusedBorderColor = Color(0xFFE5E7EB)
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(name.trim(), grade.trim(), school.trim()) }
            ) {
                Text("Save Profile", color = KidsGColors.OrangePrimary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = KidsGColors.TextSecondary)
            }
        },
        containerColor = KidsGColors.White
    )
}
