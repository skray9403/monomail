package com.shrivatsav.monomail.ui.components

import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.DialogProperties

/**
 * Compatibility wrapper around the Material 3 BasicAlertDialog. Call sites
 * provide their own standard Material dialog surface and controls.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlurredModalOverlay(
    visible: Boolean,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    if (visible) {
        BasicAlertDialog(
            onDismissRequest = onDismiss,
            modifier = Modifier,
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            content()
        }
    }
}
