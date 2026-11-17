package com.shrivatsav.monomail.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class IllustrationType {
    ENVELOPE,
    INBOX_ZERO,
    SEARCH_EMPTY,
    SHIELD,
    PAPER_PLANE,
    CONNECTION,
    ERROR_CLOUD,
    CLOUD_SYNC
}

/** Compatibility API rendered with stock Material icons instead of custom Canvas art. */
@Composable
fun MonoIllustration(
    type: IllustrationType,
    modifier: Modifier = Modifier,
    size: Dp = 160.dp,
    animated: Boolean = true
) {
    val icon: ImageVector = when (type) {
        IllustrationType.ENVELOPE -> Icons.Rounded.Email
        IllustrationType.INBOX_ZERO -> Icons.Rounded.Inbox
        IllustrationType.SEARCH_EMPTY -> Icons.Rounded.Search
        IllustrationType.SHIELD -> Icons.Rounded.Security
        IllustrationType.PAPER_PLANE -> Icons.Rounded.Send
        IllustrationType.CONNECTION,
        IllustrationType.CLOUD_SYNC -> Icons.Rounded.Cloud
        IllustrationType.ERROR_CLOUD -> Icons.Rounded.CloudOff
    }

    Surface(
        modifier = modifier.size(size),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.padding(size * 0.25f).size(size * 0.5f),
        )
    }
}
