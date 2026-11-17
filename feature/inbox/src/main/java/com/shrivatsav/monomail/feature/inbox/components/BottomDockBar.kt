package com.shrivatsav.monomail.feature.inbox.components

import com.shrivatsav.monomail.feature.inbox.*
import com.shrivatsav.monomail.model.InboxTab
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shrivatsav.monomail.core.data.settings.DockConfig
import com.shrivatsav.monomail.core.data.settings.DockTabId
import com.shrivatsav.monomail.core.data.settings.AppSettings
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.*

/**
 * Uses the platform Material 3 NavigationBar and NavigationBarItem controls.
 * Additional folders live in the standard Material DropdownMenu rather than a
 * second hand-built floating navigation dock.
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
    var showRemainingTabs by remember { mutableStateOf(false) }
    val allTabs = DockTabId.entries.filter { it != DockTabId.UNIFIED }
    val primaryIds = dockConfig.primaryTabs.take(4)
    val remainingIds = allTabs.filter { it !in primaryIds }

    NavigationBar(
        modifier = modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 3.dp,
    ) {
        primaryIds.forEach { id ->
            val tab = id.toInboxTab()
            NavigationBarItem(
                selected = currentTab == tab,
                onClick = {
                    showRemainingTabs = false
                    onTabClick(tab)
                },
                icon = { Icon(id.icon(unifiedInboxEnabled), contentDescription = null) },
                label = { Text(id.label(unifiedInboxEnabled)) },
                alwaysShowLabel = true,
            )
        }

        if (remainingIds.isNotEmpty()) {
            val selectedExtra = remainingIds.any { it.toInboxTab() == currentTab }
            Box {
                NavigationBarItem(
                    selected = selectedExtra,
                    onClick = { showRemainingTabs = true },
                    icon = { Icon(Icons.Rounded.MoreHoriz, contentDescription = null) },
                    label = { Text("More") },
                    alwaysShowLabel = true,
                )
                DropdownMenu(
                    expanded = showRemainingTabs,
                    onDismissRequest = { showRemainingTabs = false },
                ) {
                    remainingIds.forEach { id ->
                        val tab = id.toInboxTab()
                        DropdownMenuItem(
                            text = { Text(id.label(unifiedInboxEnabled)) },
                            leadingIcon = { Icon(id.icon(unifiedInboxEnabled), contentDescription = null) },
                            trailingIcon = {
                                if (currentTab == tab) {
                                    Icon(
                                        Icons.Rounded.Check,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            },
                            onClick = {
                                showRemainingTabs = false
                                onTabClick(tab)
                            },
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
