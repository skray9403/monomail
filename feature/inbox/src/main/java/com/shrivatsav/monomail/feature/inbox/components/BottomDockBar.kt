package com.shrivatsav.monomail.feature.inbox.components

import com.shrivatsav.monomail.feature.inbox.*
import com.shrivatsav.monomail.model.InboxTab
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.shrivatsav.monomail.core.data.settings.DockConfig
import com.shrivatsav.monomail.core.data.settings.DockTabId
import com.shrivatsav.monomail.core.data.settings.AppSettings

/**
 * Detached Material 3 NavigationBar dock.
 *
 * The NavigationBar remains the stock component, but its container is hosted
 * inside a separate rounded Surface so it reads as a floating navigation dock
 * instead of a full-width bottom strip. The More menu remains anchored to its
 * NavigationBarItem and all tab configuration continues to come from DockConfig.
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

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 2.dp,
        shadowElevation = 6.dp,
    ) {
        NavigationBar(
            modifier = Modifier.fillMaxWidth(),
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface,
            tonalElevation = 0.dp,
            windowInsets = WindowInsets(0.dp),
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
                NavigationBarItem(
                    selected = selectedExtra,
                    onClick = { showRemainingTabs = true },
                    icon = {
                        Box {
                            Icon(Icons.Rounded.MoreHoriz, contentDescription = null)
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
                    },
                    label = { Text("More") },
                    alwaysShowLabel = true,
                )
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
