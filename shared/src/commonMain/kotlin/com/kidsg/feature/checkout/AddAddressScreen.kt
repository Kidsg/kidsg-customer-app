package com.kidsg.feature.checkout

import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.kidsg.core.designsystem.KidsGColors
import com.kidsg.core.designsystem.KidsGPrimaryButton
import com.kidsg.core.designsystem.KidsGSpacing
import com.kidsg.core.designsystem.KidsGTextField
import com.kidsg.core.designsystem.KidsGTypography
import com.kidsg.domain.model.Address

/**
 * Add New Address Screen (Reference Screen 15)
 */
@Composable
fun AddAddressScreen(
    onBack: () -> Unit,
    onAddressSaved: (Address) -> Unit,
    modifier: Modifier = Modifier
) {
    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var addressLine1 by remember { mutableStateOf("") }
    var addressLine2 by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("Bangalore") }
    var state by remember { mutableStateOf("Karnataka") }
    var pinCode by remember { mutableStateOf("") }
    var isDefault by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(KidsGColors.BackgroundPrimary)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(KidsGColors.White)
                .padding(horizontal = KidsGSpacing.lg, vertical = KidsGSpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(KidsGColors.SurfaceElevated)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "←", style = KidsGTypography.TitleSmall)
            }

            Spacer(modifier = Modifier.width(KidsGSpacing.md))

            Text(
                text = "Add New Address",
                style = KidsGTypography.TitleLarge
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(KidsGSpacing.lg)
        ) {
            KidsGTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = "Full Name",
                placeholder = "Aarav Kumar"
            )

            Spacer(modifier = Modifier.height(KidsGSpacing.md))

            KidsGTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = "Phone Number",
                placeholder = "9876543210"
            )

            Spacer(modifier = Modifier.height(KidsGSpacing.md))

            KidsGTextField(
                value = addressLine1,
                onValueChange = { addressLine1 = it },
                label = "Address Line 1",
                placeholder = "House No, Street, Area"
            )

            Spacer(modifier = Modifier.height(KidsGSpacing.md))

            KidsGTextField(
                value = addressLine2,
                onValueChange = { addressLine2 = it },
                label = "Address Line 2 (Optional)",
                placeholder = "Landmark, Apartment"
            )

            Spacer(modifier = Modifier.height(KidsGSpacing.md))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(KidsGSpacing.md)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    KidsGTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = "City",
                        placeholder = "Bangalore"
                    )
                }

                Box(modifier = Modifier.weight(1f)) {
                    KidsGTextField(
                        value = state,
                        onValueChange = { state = it },
                        label = "State",
                        placeholder = "Karnataka"
                    )
                }
            }

            Spacer(modifier = Modifier.height(KidsGSpacing.md))

            KidsGTextField(
                value = pinCode,
                onValueChange = { pinCode = it },
                label = "PIN Code",
                placeholder = "560001"
            )

            Spacer(modifier = Modifier.height(KidsGSpacing.lg))

            // Set as default address switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "✓", color = KidsGColors.OrangePrimary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Set as default address",
                        style = KidsGTypography.BodyMedium
                    )
                }

                Switch(
                    checked = isDefault,
                    onCheckedChange = { isDefault = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = KidsGColors.White,
                        checkedTrackColor = KidsGColors.OrangePrimary
                    )
                )
            }
        }

        // Bottom CTA
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(KidsGColors.White)
                .padding(KidsGSpacing.lg)
        ) {
            KidsGPrimaryButton(
                text = "Save Address",
                onClick = {
                    val addr = Address(
                        id = "addr_${System.currentTimeMillis()}",
                        label = "Home",
                        recipientName = fullName.ifBlank { "Student Desk" },
                        phoneNumber = phoneNumber.ifBlank { "9876543210" },
                        addressLine1 = addressLine1.ifBlank { "Green Park" },
                        addressLine2 = addressLine2,
                        city = city,
                        pincode = pinCode.ifBlank { "560001" },
                        isDefault = isDefault
                    )
                    onAddressSaved(addr)
                }
            )
        }
    }
}
