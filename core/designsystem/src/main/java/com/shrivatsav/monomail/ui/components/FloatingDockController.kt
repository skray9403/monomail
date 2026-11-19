package com.shrivatsav.monomail.ui.components

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
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
    var mode by mutableStateOf(DockMode.INBOX)

    // Scroll collapse state (true when list is actively scrolling)
    var isScrolling by mutableStateOf(false)

    // Inbox tab state
    var selectedTabIndex by mutableIntStateOf(0)
    var onTabSelected by mutableStateOf<(Int) -> Unit>({})

    // Detail screen actions
    var onReplyAction by mutableStateOf<(() -> Unit)?>(null)
    var onForwardAction by mutableStateOf<(() -> Unit)?>(null)

    // Compose screen actions
    var onAttachAction by mutableStateOf<(() -> Unit)?>(null)
    var onDraftsAction by mutableStateOf<(() -> Unit)?>(null)
    var onSendAction by mutableStateOf<(() -> Unit)?>(null)
    var canSend by mutableStateOf(false)
    var isSending by mutableStateOf(false)

    // Settings screen actions
    var settingsQuery by mutableStateOf("")
    var onSettingsQueryChange by mutableStateOf<(String) -> Unit>({})
}

val LocalFloatingDockController = compositionLocalOf { FloatingDockController() }
