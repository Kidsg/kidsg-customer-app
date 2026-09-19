package com.kidsg.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.kidsg.core.designsystem.KidsGColors
import com.kidsg.core.designsystem.KidsGPrimaryButton
import com.kidsg.core.designsystem.KidsGShapes
import com.kidsg.core.designsystem.KidsGSpacing
import com.kidsg.core.designsystem.KidsGTypography
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Location Selection & Live GPS Dialog
 * Allows detecting live location and saving preferred address label (Home, School, Work).
 */
@Composable
fun LocationSelectionDialog(
    currentLocation: String,
    onDismiss: () -> Unit,
    onLocationSelected: (locationName: String, label: String) -> Unit
) {
    var isDetectingGps by remember { mutableStateOf(false) }
    var selectedLabel by remember { mutableStateOf("Home") }
    var detectedLocation by remember { mutableStateOf(currentLocation) }
    val coroutineScope = rememberCoroutineScope()

    val quickAddresses = listOf(
        "HSR Layout, Sector 3, Bengaluru",
        "Koramangala 4th Block, Bengaluru",
        "Indiranagar, 100ft Road, Bengaluru",
        "Whitefield, ITPL Main Road, Bengaluru"
    )

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(KidsGColors.White)
                .padding(KidsGSpacing.xl)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Delivery Location",
                        style = KidsGTypography.TitleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "✕",
                        fontSize = 18.sp,
                        color = KidsGColors.TextSecondary,
                        modifier = Modifier.clickable(onClick = onDismiss)
                    )
                }

                Spacer(modifier = Modifier.height(KidsGSpacing.md))

                // GPS Detection Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(KidsGShapes.Medium)
                        .background(KidsGColors.AccentYellowLight)
                        .border(1.dp, KidsGColors.AccentYellow, KidsGShapes.Medium)
                        .clickable {
                            if (!isDetectingGps) {
                                isDetectingGps = true
                                coroutineScope.launch {
                                    delay(800)
                                    detectedLocation = "HSR Layout, 14th Main (Live GPS)"
                                    isDetectingGps = false
                                }
                            }
                        }
                        .padding(KidsGSpacing.md)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "📍", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Detect Live Location",
                                    style = KidsGTypography.TitleSmall.copy(color = KidsGColors.OrangePrimary)
                                )
                                Text(
                                    text = "Using Device GPS Services",
                                    style = KidsGTypography.Caption.copy(color = KidsGColors.TextSecondary)
                                )
                            }
                        }

                        if (isDetectingGps) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = KidsGColors.OrangePrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(KidsGSpacing.lg))

                // Label Tagging (Home, School, Work, Other)
                Text(
                    text = "Save this location as:",
                    style = KidsGTypography.Caption.copy(fontWeight = FontWeight.Bold, color = KidsGColors.TextSecondary)
                )

                Spacer(modifier = Modifier.height(KidsGSpacing.sm))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(KidsGSpacing.sm)
                ) {
                    listOf("Home 🏠", "School 🎒", "Work 🏢").forEach { tag ->
                        val isSelected = selectedLabel == tag.split(" ")[0]
                        Box(
                            modifier = Modifier
                                .clip(KidsGShapes.FullPill)
                                .background(if (isSelected) KidsGColors.OrangePrimary else KidsGColors.SurfaceElevated)
                                .clickable { selectedLabel = tag.split(" ")[0] }
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = tag,
                                style = KidsGTypography.Caption.copy(
                                    color = if (isSelected) KidsGColors.White else KidsGColors.TextPrimary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(KidsGSpacing.lg))

                // Quick Saved Locations list
                Text(
                    text = "Saved Localities:",
                    style = KidsGTypography.Caption.copy(fontWeight = FontWeight.Bold, color = KidsGColors.TextSecondary)
                )

                Spacer(modifier = Modifier.height(KidsGSpacing.sm))

                quickAddresses.forEach { addr ->
                    val isSelected = detectedLocation == addr
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(KidsGShapes.Small)
                            .clickable { detectedLocation = addr }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isSelected) "● " else "○ ",
                            color = if (isSelected) KidsGColors.OrangePrimary else KidsGColors.TextMuted,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = addr,
                            style = KidsGTypography.BodySmall.copy(
                                color = if (isSelected) KidsGColors.BlackText else KidsGColors.TextSecondary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(KidsGSpacing.lg))

                KidsGPrimaryButton(
                    text = "Confirm Location",
                    onClick = {
                        onLocationSelected(detectedLocation, selectedLabel)
                        onDismiss()
                    }
                )
            }
        }
    }
}
