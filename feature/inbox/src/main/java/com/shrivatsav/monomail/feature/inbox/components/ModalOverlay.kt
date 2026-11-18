package com.shrivatsav.monomail.feature.inbox.components

import com.shrivatsav.monomail.feature.inbox.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Report
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.shrivatsav.monomail.model.InboxTab
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.hilt.navigation.compose.hiltViewModel
import com.shrivatsav.monomail.core.data.auth.UserProfile

internal enum class ModalType { PROFILE, SWITCH_ACCOUNT, FOLDERS, ADD_ACCOUNT }

internal data class ModalCallbacks(
    val onDismiss: () -> Unit,
    val onSignOut: () -> Unit,
    val onSwitchAccount: (String) -> Unit,
    val onAddAccount: () -> Unit,
    val onShowSwitchAccount: () -> Unit,
    val onBackToProfile: () -> Unit,
    val onCycleAccount: (String) -> Unit,
    val onSettings: () -> Unit,
    val onSelectFolder: (InboxTab) -> Unit,
    val onNavigateToImapSetup: (String?, String?) -> Unit = { _, _ -> },
    val onToggleUnified: (Boolean) -> Unit = {},
)

/**
 * Account/profile presentation is hosted by the standard Material 3 modal
 * bottom sheet. Provider selection remains its own destination dialog because
 * it is a separate sign-in flow, not sheet content.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ModalOverlay(
    activeModal: ModalType?,
    userProfile: UserProfile?,
    accounts: List<UserProfile>,
    callbacks: ModalCallbacks,
    unifiedInboxEnabled: Boolean = false,
    currentTab: InboxTab = InboxTab.INBOX,
) {
    when (activeModal) {
        ModalType.ADD_ACCOUNT -> {
            val vm: com.shrivatsav.monomail.feature.auth.SignInViewModel = hiltViewModel()
            com.shrivatsav.monomail.feature.auth.ProviderSelectionDialog(
                viewModel = vm,
                onSuccess = callbacks.onDismiss,
                onNavigateToImapSetup = callbacks.onNavigateToImapSetup,
            )
        }
        ModalType.PROFILE, ModalType.SWITCH_ACCOUNT, ModalType.FOLDERS -> {
            ModalBottomSheet(
                onDismissRequest = callbacks.onDismiss,
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                tonalElevation = 0.dp,
                scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.34f),
            ) {
                key(activeModal) {
                    ModalContentBody(
                        modal = activeModal,
                        userProfile = userProfile,
                        accounts = accounts,
                        callbacks = callbacks,
                        unifiedInboxEnabled = unifiedInboxEnabled,
                        currentTab = currentTab,
                    )
                }
            }
        }
        null -> Unit
    }
}

@Composable
private fun ModalContentBody(
    modal: ModalType?,
    userProfile: UserProfile?,
    accounts: List<UserProfile>,
    callbacks: ModalCallbacks,
    unifiedInboxEnabled: Boolean,
    currentTab: InboxTab,
) {
    when (modal) {
        ModalType.PROFILE -> {
            if (userProfile != null) {
                ProfileCard(
                    userProfile = userProfile,
                    accounts = accounts,
                    callbacks = ProfileCardCallbacks(
                        onSignOut = callbacks.onSignOut,
                        onShowSwitchAccount = callbacks.onShowSwitchAccount,
                        onCycleAccount = callbacks.onCycleAccount,
                        onSettings = callbacks.onSettings,
                        onAddAccount = callbacks.onAddAccount,
                        onToggleUnified = callbacks.onToggleUnified,
                    ),
                    unifiedInboxEnabled = unifiedInboxEnabled,
                )
            }
        }
        ModalType.SWITCH_ACCOUNT -> {
            if (userProfile != null) {
                SwitchAccountCard(
                    userProfile = userProfile,
                    accounts = accounts,
                    onSwitchAccount = callbacks.onSwitchAccount,
                    onAddAccount = callbacks.onAddAccount,
                    onBack = callbacks.onBackToProfile,
                )
            }
        }
        ModalType.FOLDERS -> {
            FoldersCard(
                currentTab = currentTab,
                onSelectFolder = callbacks.onSelectFolder,
            )
        }
        ModalType.ADD_ACCOUNT, null -> Unit
    }
}

private data class FolderDestination(
    val tab: InboxTab,
    val label: String,
    val icon: ImageVector,
)

@Composable
private fun FoldersCard(
    currentTab: InboxTab,
    onSelectFolder: (InboxTab) -> Unit,
) {
    val folders = listOf(
        FolderDestination(InboxTab.SNOOZED, "Snoozed", Icons.Rounded.Schedule),
        FolderDestination(InboxTab.STARRED, "Starred", Icons.Rounded.Star),
        FolderDestination(InboxTab.TRASH, "Trash", Icons.Rounded.Delete),
        FolderDestination(InboxTab.SPAM, "Spam", Icons.Rounded.Report),
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column {
            ListItem(
                leadingContent = {
                    Icon(Icons.Rounded.FolderOpen, contentDescription = null)
                },
                headlineContent = { Text("Mail folders") },
                supportingContent = { Text("Snoozed, Starred, Trash and Spam") },
                colors = ListItemDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
            )
            HorizontalDivider()
            folders.forEach { folder ->
                val selected = currentTab == folder.tab
                ListItem(
                    leadingContent = {
                        Icon(folder.icon, contentDescription = null)
                    },
                    headlineContent = { Text(folder.label) },
                    trailingContent = {
                        if (selected) {
                            Icon(
                                Icons.Rounded.Check,
                                contentDescription = "Current folder",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectFolder(folder.tab) },
                    colors = ListItemDefaults.colors(
                        containerColor = if (selected) {
                            MaterialTheme.colorScheme.secondaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceContainer
                        },
                    ),
                )
            }
        }
    }
}
