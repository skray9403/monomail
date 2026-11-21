package com.shrivatsav.monomail.ui.components

import androidx.compose.foundation.pager.PagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector

data class AppNavigationTab(
    val id: String,
    val label: String,
    val icon: ImageVector,
)

/** Shared navigation selection state for the inbox pager and app navigation bar. */
class AppNavigationState {
    var selectedTabIndex by mutableIntStateOf(0)
    var onTabSelected by mutableStateOf<(Int) -> Unit>({})
    var navigationTabs by mutableStateOf(
        listOf(
            AppNavigationTab("INBOX", "Inbox", Icons.Rounded.Inbox),
            AppNavigationTab("SENT", "Sent", Icons.AutoMirrored.Rounded.Send),
            AppNavigationTab("ARCHIVED", "Archived", Icons.Rounded.Archive),
        )
    )
    var pagerState by mutableStateOf<PagerState?>(null)
    val dockCollapsed = mutableStateOf(false)
}

val LocalAppNavigationState = compositionLocalOf { AppNavigationState() }
