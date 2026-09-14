package com.kidsg.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidsg.core.designsystem.KidsGColors
import com.kidsg.core.designsystem.KidsGIllustration
import com.kidsg.core.designsystem.KidsGTypography
import com.kidsg.domain.model.UserProfile
import com.kidsg.domain.repository.AuthRepository
import kotlinx.coroutines.launch

/**
 * KidsG Student Setup Screen
 * First-launch profile customization: Student Name, Class 1 to 12 Chips, School, and Board.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StudentSetupScreen(
    authRepository: AuthRepository,
    initialProfile: UserProfile?,
    onSetupComplete: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    var studentName by remember { mutableStateOf(initialProfile?.studentName?.ifEmpty { "Aarav Sharma" } ?: "Aarav Sharma") }
    var selectedGrade by remember { mutableStateOf(initialProfile?.studentGrade ?: "Class 7") }
    var schoolName by remember { mutableStateOf(initialProfile?.schoolName ?: "National Public School") }
    var selectedBoard by remember { mutableStateOf("CBSE") }
    var isParentMode by remember { mutableStateOf(initialProfile?.isParentMode ?: false) }

    val primaryClasses = listOf("Class 1", "Class 2", "Class 3", "Class 4", "Class 5")
    val middleClasses = listOf("Class 6", "Class 7", "Class 8")
    val secondaryClasses = listOf("Class 9", "Class 10")
    val seniorClasses = listOf("Class 11", "Class 12")

    val boards = listOf("CBSE", "ICSE", "State Board", "IB / Cambridge")
    val popularSchools = listOf("Delhi Public School", "National Public School", "Kendriya Vidyalaya", "St. Joseph's", "DAV Public")

    KidsGIllustration.DeskBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(28.dp))

            // Mascot Header
            KidsGIllustration.Mascot(modifier = Modifier.size(72.dp))

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Setup Your Student Desk",
                style = KidsGTypography.DisplaySmall.copy(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = KidsGColors.BlackText
                )
            )

            Text(
                text = "KidsG customizes stationery recommendations for your class curriculum.",
                style = KidsGTypography.BodyMedium.copy(
                    color = KidsGColors.TextSecondary,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.padding(top = 4.dp, start = 8.dp, end = 8.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Student Name Input
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = KidsGColors.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Student Name",
                        style = KidsGTypography.TitleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = KidsGColors.BlackText
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = studentName,
                        onValueChange = { studentName = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        placeholder = { Text("e.g. Aarav Sharma") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = KidsGColors.OrangePrimary,
                            unfocusedBorderColor = KidsGColors.BorderSubtle
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Standard / Class Selection Chips (Class 1 to 12)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = KidsGColors.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Select Standard / Class",
                            style = KidsGTypography.TitleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = KidsGColors.BlackText
                            )
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(KidsGColors.OrangePrimary.copy(alpha = 0.12f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = selectedGrade,
                                style = KidsGTypography.BodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = KidsGColors.OrangePrimary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Primary (Classes 1 - 5)",
                        style = KidsGTypography.BodySmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = KidsGColors.TextSecondary
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        primaryClasses.forEach { cls ->
                            GradeChip(
                                label = cls,
                                isSelected = selectedGrade == cls,
                                onClick = { selectedGrade = cls }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Middle School (Classes 6 - 8)",
                        style = KidsGTypography.BodySmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = KidsGColors.TextSecondary
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        middleClasses.forEach { cls ->
                            GradeChip(
                                label = cls,
                                isSelected = selectedGrade == cls,
                                onClick = { selectedGrade = cls }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Secondary & Senior (Classes 9 - 12)",
                        style = KidsGTypography.BodySmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = KidsGColors.TextSecondary
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        (secondaryClasses + seniorClasses).forEach { cls ->
                            GradeChip(
                                label = cls,
                                isSelected = selectedGrade == cls,
                                onClick = { selectedGrade = cls }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // School & Education Board
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = KidsGColors.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "School & Education Board",
                        style = KidsGTypography.TitleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = KidsGColors.BlackText
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = schoolName,
                        onValueChange = { schoolName = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        placeholder = { Text("Enter School Name") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = KidsGColors.OrangePrimary,
                            unfocusedBorderColor = KidsGColors.BorderSubtle
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Popular School Suggestions
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        popularSchools.forEach { sch ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(KidsGColors.Surface)
                                    .clickable { schoolName = sch }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = sch,
                                    style = KidsGTypography.BodySmall.copy(
                                        fontSize = 11.sp,
                                        color = KidsGColors.TextSecondary
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Board",
                        style = KidsGTypography.BodySmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = KidsGColors.TextSecondary
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        boards.forEach { b ->
                            GradeChip(
                                label = b,
                                isSelected = selectedBoard == b,
                                onClick = { selectedBoard = b }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Primary CTA: Enter KidsG Desk
            Button(
                onClick = {
                    coroutineScope.launch {
                        val baseProfile = initialProfile ?: UserProfile(
                            id = "user_${System.currentTimeMillis()}",
                            name = studentName,
                            phone = "+91 98765 43210",
                            email = "student@kidsg.in"
                        )
                        val updated = baseProfile.copy(
                            name = studentName,
                            studentName = studentName,
                            studentGrade = selectedGrade,
                            schoolName = schoolName,
                            isParentMode = isParentMode
                        )
                        authRepository.updateProfile(updated)
                        onSetupComplete()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = KidsGColors.OrangePrimary)
            ) {
                Text(
                    text = "Enter KidsG Desk 🎒",
                    style = KidsGTypography.TitleMedium.copy(
                        color = KidsGColors.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}

@Composable
private fun GradeChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (isSelected) KidsGColors.OrangePrimary else KidsGColors.Surface
            )
            .border(
                width = 1.dp,
                color = if (isSelected) KidsGColors.OrangePrimary else KidsGColors.BorderSubtle,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = KidsGTypography.BodyMedium.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) KidsGColors.White else KidsGColors.BlackText,
                fontSize = 13.sp
            )
        )
    }
}
