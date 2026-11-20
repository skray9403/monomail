package com.shrivatsav.monomail.ui.components

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue

/** Shared navigation selection state for the inbox pager and app navigation bar. */
class AppNavigationState {
    var selectedTabIndex by mutableIntStateOf(0)
    var onTabSelected by mutableStateOf<(Int) -> Unit>({})
}

val LocalAppNavigationState = compositionLocalOf { AppNavigationState() }
