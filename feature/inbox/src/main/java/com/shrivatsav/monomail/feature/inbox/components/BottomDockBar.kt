package com.shrivatsav.monomail.feature.inbox.components

import com.shrivatsav.monomail.feature.inbox.*
import com.shrivatsav.monomail.model.InboxTab
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.semantics.Role
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shrivatsav.monomail.core.data.settings.DockConfig
import com.shrivatsav.monomail.core.data.settings.DockTabId
import com.shrivatsav.monomail.core.data.settings.AppSettings

/**
 * Compact floating destination pill inspired by the Google Photos navigation
 * redesign: only the selected destination receives an icon + filled indicator;
 * sibling destinations remain lightweight text actions.
 */
@Composable
internal fun BottomDockBar(
    currentTab: InboxTab,
    dockConfig: DockConfig,
    unifiedInboxEnabled: Boolean,
    appSettings: AppSettings,
    onTabClick: (InboxTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val primaryIds = dockConfig.primaryTabs.take(4)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp),
        shape = RoundedCornerShape(percent = 50),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        tonalElevation = 1.dp,
        shadowElevation = 5.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            primaryIds.forEach { id ->
                val tab = id.toInboxTab()
                val selected = currentTab == tab
                Row(
                    modifier = Modifier
                        .weight(if (selected) 1.45f else 1f)
                        .fillMaxHeight()
                        .selectable(
                            selected = selected,
                            onClick = { onTabClick(tab) },
                            role = Role.Tab,
                        ),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (selected) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(percent = 50),
                            color = MaterialTheme.colorScheme.primaryContainer,
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight()
                                    .padding(horizontal = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = id.icon(unifiedInboxEnabled),
                                    contentDescription = null,
                                    modifier = Modifier.size(22.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                                Text(
                                    text = id.label(unifiedInboxEnabled),
                                    modifier = Modifier.padding(start = 7.dp),
                                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    } else {
                        Text(
                            text = id.label(unifiedInboxEnabled),
                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

private fun DockTabId.toInboxTab(): InboxTab = when (this) {
    DockTabId.UNIFIED -> InboxTab.UNIFIED
    DockTabId.INBOX -> InboxTab.INBOX
    DockTabId.SENT -> InboxTab.SENT
    DockTabId.ARCHIVED -> InboxTab.ARCHIVED
    DockTabId.SNOOZED -> InboxTab.SNOOZED
    DockTabId.STARRED -> InboxTab.STARRED
    DockTabId.TRASH -> InboxTab.TRASH
    DockTabId.SPAM -> InboxTab.SPAM
}

internal fun DockTabId.icon(unifiedInboxEnabled: Boolean): androidx.compose.ui.graphics.vector.ImageVector = when (this) {
    DockTabId.UNIFIED -> Icons.Rounded.Inbox
    DockTabId.INBOX -> if (unifiedInboxEnabled) Icons.Rounded.Home else Icons.Rounded.Inbox
    DockTabId.SENT -> Icons.AutoMirrored.Rounded.Send
    DockTabId.ARCHIVED -> Icons.Rounded.Archive
    DockTabId.SNOOZED -> Icons.Rounded.Schedule
    DockTabId.STARRED -> Icons.Rounded.Star
    DockTabId.TRASH -> Icons.Rounded.Delete
    DockTabId.SPAM -> Icons.Rounded.Report
}

internal fun DockTabId.label(unifiedInboxEnabled: Boolean): String = when (this) {
    DockTabId.UNIFIED -> "Unified"
    DockTabId.INBOX -> if (unifiedInboxEnabled) "All Mail" else "Inbox"
    DockTabId.SENT -> "Sent"
    DockTabId.ARCHIVED -> "Archived"
    DockTabId.SNOOZED -> "Snoozed"
    DockTabId.STARRED -> "Starred"
    DockTabId.TRASH -> "Trash"
    DockTabId.SPAM -> "Spam"
}
