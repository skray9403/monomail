package com.shrivatsav.monomail.feature.inbox.components

import com.shrivatsav.monomail.ui.components.AvatarCircle
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shrivatsav.monomail.core.data.auth.UserProfile

internal data class ProfileCardCallbacks(
    val onSignOut: () -> Unit,
    val onShowSwitchAccount: () -> Unit,
    val onCycleAccount: (String) -> Unit,
    val onSettings: () -> Unit,
    val onAddAccount: () -> Unit,
    val onToggleUnified: (Boolean) -> Unit = {},
)

@Composable
internal fun ProfileCard(
    userProfile: UserProfile,
    accounts: List<UserProfile>,
    callbacks: ProfileCardCallbacks,
    unifiedInboxEnabled: Boolean = false,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column {
            ListItem(
                leadingContent = {
                    AvatarCircle(
                        photoUrl = userProfile.photoUrl,
                        displayName = userProfile.displayName,
                        size = 48.dp,
                        textStyle = MaterialTheme.typography.titleMedium,
                    )
                },
                headlineContent = { Text(userProfile.displayName, maxLines = 1) },
                supportingContent = { Text(userProfile.email, maxLines = 1) },
                trailingContent = {
                    if (accounts.size > 1) {
                        Icon(
                            Icons.Rounded.ChevronRight,
                            contentDescription = "Switch account",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().then(
                    if (accounts.size > 1) Modifier.padding(end = 4.dp) else Modifier
                ),
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            )
            if (accounts.size > 1) {
                ListItem(
                    leadingContent = {
                        Icon(Icons.Rounded.Inbox, contentDescription = null)
                    },
                    headlineContent = { Text("Unified inbox") },
                    supportingContent = {
                        Text(if (unifiedInboxEnabled) "All accounts combined" else "Show one account at a time")
                    },
                    trailingContent = {
                        Switch(
                            checked = unifiedInboxEnabled,
                            onCheckedChange = callbacks.onToggleUnified,
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                )
            }
            HorizontalDivider()
            ListItem(
                leadingContent = { Icon(Icons.Rounded.Settings, contentDescription = null) },
                headlineContent = { Text("Settings") },
                trailingContent = { Icon(Icons.Rounded.ChevronRight, contentDescription = null) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            )
            HorizontalDivider()
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                TextButton(onClick = callbacks.onSignOut, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Rounded.Logout, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Sign out")
                }
                FilledTonalButton(onClick = callbacks.onAddAccount, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Rounded.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Add account")
                }
            }
        }
    }
}
