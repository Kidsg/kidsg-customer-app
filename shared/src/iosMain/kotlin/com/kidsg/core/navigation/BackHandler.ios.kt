package com.kidsg.core.navigation

import androidx.compose.runtime.Composable

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    // Handled natively by iOS interactive pop gesture
}
