package com.shrivatsav.monomail.ui.components

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Contextual state for the single persistent floating dock.
 * All feature screens communicate through this controller so that exactly
 * ONE dock exists in the window tree, smoothly morphing its shape, width,
 * and actions across screens without duplication during predictive back.
 */
enum class DockMode {
    INBOX,
    THREAD,
    COMPOSE,
    SETTINGS,
    HIDDEN
}

class FloatingDockController {
    // Current route mode
    var mode by mutableStateOf(DockMode.HIDDEN)

    // Suppress the dock while a modal or bulk-selection surface owns the bottom edge.
    var isSuppressed by mutableStateOf(false)
    var isSpamTab by mutableStateOf(false)
    var isCollapsed by mutableStateOf(false)
    var isBulkMode by mutableStateOf(false)
    var bulkSelectionCount by mutableIntStateOf(0)
    var onExitBulkMode by mutableStateOf<(() -> Unit)?>(null)
    var bulkActions by mutableStateOf<List<DockBulkAction>>(emptyList())

    // Configurable inbox navigation, preserving the user's primary/overflow tab setup.
    var primaryTabs by mutableStateOf<List<DockTabItem>>(emptyList())
    var overflowTabs by mutableStateOf<List<DockTabItem>>(emptyList())
    var selectedTabId by mutableStateOf("INBOX")

    // Search active state (true when search bar is focused/expanded or query entered)
    var isSearchActive by mutableStateOf(false)

    // Inbox tab state
    var selectedTabIndex by mutableIntStateOf(0)
    var continuousTabPosition by mutableFloatStateOf(0f)
    var onTabSelected by mutableStateOf<(String) -> Unit>({})

    // Detail screen actions
    var onReplyAction by mutableStateOf<(() -> Unit)?>(null)
    var onForwardAction by mutableStateOf<(() -> Unit)?>(null)

    // Inbox primary action: Compose normally, Move Spam to Trash on the Spam tab.
    var onComposeAction by mutableStateOf<(() -> Unit)?>(null)

    // Compose screen actions
    var onAttachAction by mutableStateOf<(() -> Unit)?>(null)
    var onDraftsAction by mutableStateOf<(() -> Unit)?>(null)
    var onSendAction by mutableStateOf<(() -> Unit)?>(null)
    var canSend by mutableStateOf(false)
    var isSending by mutableStateOf(false)

    // Settings screen actions & title
    var settingsTitle by mutableStateOf("Settings")
    var onBackClick by mutableStateOf<(() -> Unit)?>(null)
    var settingsQuery by mutableStateOf("")
    var onSettingsQueryChange by mutableStateOf<(String) -> Unit>({})
}

data class DockTabItem(
    val id: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

val LocalFloatingDockController = compositionLocalOf { FloatingDockController() }


data class DockBulkAction(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val onClick: () -> Unit
)
