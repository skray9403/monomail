package com.shrivatsav.monomail.feature.settings

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shrivatsav.monomail.core.data.settings.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun InboxSettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val settings by viewModel.settings.collectAsState()
    val motionScheme = MaterialTheme.motionScheme

    ScrollableSettingsScaffold(title = "Inbox", onBack = onBack) {
        SettingsCard {
            SettingsToggleRow(
                icon = Icons.Rounded.Forum,
                title = if (settings.organizeByThread) "Conversation View" else "Message Chain",
                subtitle = if (settings.organizeByThread) "Collapsible grouped threads in detail view" else "All messages expanded as a chain",
                checked = settings.organizeByThread,
                onCheckedChange = { viewModel.setOrganizeByThread(it) }
            )
            CardDivider()
            SettingsToggleRow(
                icon = Icons.Rounded.FolderSpecial,
                title = "Smart Grouping",
                subtitle = "Group frequent senders into folders",
                checked = settings.smartGroupingEnabled,
                onCheckedChange = { viewModel.setSmartGroupingEnabled(it) }
            )
            AnimatedVisibility(
                visible = settings.smartGroupingEnabled,
                enter = expandVertically(animationSpec = motionScheme.defaultSpatialSpec()) + fadeIn(motionScheme.fastEffectsSpec()),
                exit = shrinkVertically(animationSpec = motionScheme.defaultSpatialSpec()) + fadeOut(motionScheme.fastEffectsSpec())
            ) {
                Column {
                    CardDivider()
                    SettingsToggleRow(
                        icon = Icons.Rounded.Schedule,
                        title = "Group Recent Only",
                        subtitle = "Only group emails from the last 24 hours",
                        checked = settings.smartGroupingRecentOnly,
                        onCheckedChange = { viewModel.setSmartGroupingRecentOnly(it) },
                        indented = true
                    )
                }
            }
            CardDivider()
            BottomSheetPickerRow(
                icon = Icons.Rounded.SwipeLeft,
                title = "Swipe Left",
                currentValue = settings.swipeLeftAction.displayName(),
                options = SwipeAction.entries.map { it.displayName() },
                onSelected = { idx -> viewModel.setSwipeLeftAction(SwipeAction.entries[idx]) }
            )
            CardDivider()
            BottomSheetPickerRow(
                icon = Icons.Rounded.SwipeRight,
                title = "Swipe Right",
                currentValue = settings.swipeRightAction.displayName(),
                options = SwipeAction.entries.map { it.displayName() },
                onSelected = { idx -> viewModel.setSwipeRightAction(SwipeAction.entries[idx]) }
            )
        }
    }
}
