package com.shrivatsav.monomail.feature.inbox.components

import com.shrivatsav.monomail.ui.components.AvatarCircle
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.shrivatsav.monomail.core.data.auth.UserProfile

@Composable
internal fun SwitchAccountCard(
    userProfile: UserProfile,
    accounts: List<UserProfile>,
    onSwitchAccount: (String) -> Unit,
    onAddAccount: () -> Unit,
    onBack: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column {
            ListItem(
                leadingContent = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                headlineContent = { Text("Accounts") },
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            )
            HorizontalDivider()
            accounts.forEach { account ->
                val isCurrent = account.id == userProfile.id
                ListItem(
                    leadingContent = {
                        AvatarCircle(
                            photoUrl = account.photoUrl,
                            displayName = account.displayName,
                            size = 40.dp,
                            textStyle = MaterialTheme.typography.titleSmall,
                        )
                    },
                    headlineContent = { Text(account.displayName, maxLines = 1) },
                    supportingContent = { Text(account.email, maxLines = 1) },
                    trailingContent = {
                        if (isCurrent) Icon(
                            Icons.Rounded.Check,
                            contentDescription = "Current account",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !isCurrent) { onSwitchAccount(account.id) },
                    colors = ListItemDefaults.colors(
                        containerColor = if (isCurrent)
                            MaterialTheme.colorScheme.secondaryContainer
                        else MaterialTheme.colorScheme.surfaceContainer,
                    ),
                )
            }
            HorizontalDivider()
            ListItem(
                leadingContent = { Icon(Icons.Rounded.Add, contentDescription = null) },
                headlineContent = { Text("Add account") },
                modifier = Modifier.fillMaxWidth().clickable(onClick = onAddAccount),
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            )
        }
    }
}
