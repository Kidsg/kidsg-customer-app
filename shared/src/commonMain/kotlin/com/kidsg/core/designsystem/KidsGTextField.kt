package com.kidsg.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun KidsGTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = KidsGTypography.Caption.copy(
                fontSize = 13.sp,
                color = KidsGColors.TextSecondary
            )
        )

        Spacer(modifier = Modifier.height(6.dp))

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .clip(KidsGShapes.Small)
                .background(KidsGColors.White)
                .border(1.dp, KidsGColors.BorderSubtle, KidsGShapes.Small)
                .padding(horizontal = KidsGSpacing.md, vertical = 13.dp),
            textStyle = KidsGTypography.BodyMedium.copy(
                color = KidsGColors.BlackText,
                fontSize = 15.sp
            ),
            cursorBrush = SolidColor(KidsGColors.OrangePrimary),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            decorationBox = { innerTextField ->
                if (value.isEmpty() && placeholder.isNotEmpty()) {
                    Text(
                        text = placeholder,
                        style = KidsGTypography.BodyMedium.copy(
                            color = KidsGColors.TextMuted,
                            fontSize = 15.sp
                        )
                    )
                }
                innerTextField()
            }
        )
    }
}
