package com.shrivatsav.monomail.ui.components

import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.DialogProperties

/**
 * Thin Material 3 dialog host for app-specific dialog content.
 * Scrim, window placement and dismissal are provided by BasicAlertDialog.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    if (visible) {
        BasicAlertDialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false),
            content = content,
        )
    }
}
