package com.shrivatsav.monomail.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * Compatibility wrapper around the standard Compose Dialog. The app supplies
 * Material surfaces and controls as content; this wrapper adds no custom scrim
 * or modal transition.
 */
@Composable
fun BlurredModalOverlay(
    visible: Boolean,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    if (visible) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            content()
        }
    }
}
