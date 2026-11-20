package com.shrivatsav.monomail.ui.navigation

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shrivatsav.monomail.ui.theme.MonoMailTheme
import com.shrivatsav.monomail.ui.theme.ThemePreviews

@Composable
internal fun StockInboxNavigationBar(
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    onCompose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NavigationBar(
            modifier = Modifier.weight(1f),
            windowInsets = WindowInsets(0, 0, 0, 0),
        ) {
            NavigationBarItem(
                selected = selectedIndex == 0,
                onClick = { onTabSelected(0) },
                icon = { Icon(Icons.Rounded.Inbox, contentDescription = null) },
                label = { Text("Inbox") },
            )
            NavigationBarItem(
                selected = selectedIndex == 1,
                onClick = { onTabSelected(1) },
                icon = { Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = null) },
                label = { Text("Sent") },
            )
            NavigationBarItem(
                selected = selectedIndex == 2,
                onClick = { onTabSelected(2) },
                icon = { Icon(Icons.Rounded.Archive, contentDescription = null) },
                label = { Text("Archived") },
            )
        }
        Spacer(Modifier.width(12.dp))
        FloatingActionButton(onClick = onCompose) {
            Icon(Icons.Rounded.Edit, contentDescription = "Compose")
        }
    }
}

@ThemePreviews
@Composable
private fun StockInboxNavigationBarPreview() {
    MonoMailTheme {
        StockInboxNavigationBar(
            selectedIndex = 0,
            onTabSelected = {},
            onCompose = {},
        )
    }
}
