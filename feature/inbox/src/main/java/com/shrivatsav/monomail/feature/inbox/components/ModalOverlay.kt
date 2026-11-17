package com.shrivatsav.monomail.feature.inbox.components

import com.shrivatsav.monomail.feature.inbox.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.hilt.navigation.compose.hiltViewModel
import com.shrivatsav.monomail.core.data.auth.UserProfile

internal enum class ModalType { PROFILE, SWITCH_ACCOUNT, ADD_ACCOUNT }

internal data class ModalCallbacks(
    val onDismiss: () -> Unit,
    val onSignOut: () -> Unit,
    val onSwitchAccount: (String) -> Unit,
    val onAddAccount: () -> Unit,
    val onShowSwitchAccount: () -> Unit,
    val onBackToProfile: () -> Unit,
    val onCycleAccount: (String) -> Unit,
    val onSettings: () -> Unit,
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
        ModalType.PROFILE, ModalType.SWITCH_ACCOUNT -> {
            ModalBottomSheet(
                onDismissRequest = callbacks.onDismiss,
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            ) {
                key(activeModal) {
                    ModalContentBody(
                        modal = activeModal,
                        userProfile = userProfile,
                        accounts = accounts,
                        callbacks = callbacks,
                        unifiedInboxEnabled = unifiedInboxEnabled,
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
        ModalType.ADD_ACCOUNT, null -> Unit
    }
}
