@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class
)

package com.shrivatsav.monomail.ui.navigation

import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Forward
import androidx.compose.material.icons.automirrored.rounded.Reply
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Drafts
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shrivatsav.monomail.ui.components.DockMode
import com.shrivatsav.monomail.ui.components.FloatingDockController

/**
 * Single, persistent, Material 3 Expressive floating bottom dock.
 * Hoisted outside NavHost so it remains consistent across the entire app
 * and smoothly morphs with spring physics between screen contexts:
 *   - Inbox: Tabs with scroll-collapse (full text+icon at rest -> compact icon-only pill on scroll) + Compose FAB
 *   - Detail: Reply & Forward action dock
 *   - Compose: Attachment & action bar
 *   - Settings: Search bar pill
 *
 * Emulates the soft, diffused ambient/spot elevation shadows seen in Google Messages.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun ExpressivePersistentDock(
    dockController: FloatingDockController,
    onComposeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (dockController.mode == DockMode.HIDDEN) return

    val isCollapsed = dockController.isScrolling
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp

    // Expressive dynamic width with spring physics
    val dockWidth by animateDpAsState(
        targetValue = when {
            dockController.mode == DockMode.INBOX && isCollapsed -> 180.dp
            dockController.mode == DockMode.INBOX -> (screenWidth - 32.dp) - 76.dp // Subtract horizontal margins and FAB width
            else -> screenWidth - 32.dp
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "expressiveDockWidth"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Floating pill container with Google Messages diffused elevation
        Surface(
            modifier = Modifier
                .width(dockWidth)
                .height(64.dp)
                .graphicsLayer {
                    shape = RoundedCornerShape(percent = 50)
                    clip = false
                    shadowElevation = 10.dp.toPx()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        ambientShadowColor = Color.Black.copy(alpha = 0.22f)
                        spotShadowColor = Color.Black.copy(alpha = 0.32f)
                    }
                },
            shape = RoundedCornerShape(percent = 50),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.96f),
            contentColor = MaterialTheme.colorScheme.onSurface,
            tonalElevation = 1.dp,
            shadowElevation = 0.dp, // Drawn by graphicsLayer for soft diffused blur
        ) {
            AnimatedContent(
                targetState = dockController.mode,
                transitionSpec = {
                    (fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) + scaleIn(spring(stiffness = Spring.StiffnessMediumLow), initialScale = 0.92f))
                        .togetherWith(fadeOut(spring(stiffness = Spring.StiffnessMediumLow)) + scaleOut(spring(stiffness = Spring.StiffnessMediumLow), targetScale = 0.92f))
                },
                label = "dockModeTransition"
            ) { mode ->
                when (mode) {
                    DockMode.INBOX -> {
                        InboxDockContent(
                            isCollapsed = isCollapsed,
                            selectedIndex = dockController.selectedTabIndex,
                            onTabSelected = dockController.onTabSelected
                        )
                    }
                    DockMode.THREAD -> {
                        ThreadDockContent(
                            onReply = { dockController.onReplyAction?.invoke() },
                            onForward = { dockController.onForwardAction?.invoke() }
                        )
                    }
                    DockMode.COMPOSE -> {
                        ComposeDockContent(
                            canSend = dockController.canSend,
                            isSending = dockController.isSending,
                            onAttach = { dockController.onAttachAction?.invoke() },
                            onDrafts = { dockController.onDraftsAction?.invoke() },
                            onSend = { dockController.onSendAction?.invoke() }
                        )
                    }
                    DockMode.SETTINGS -> {
                        SettingsDockContent(
                            query = dockController.settingsQuery,
                            onQueryChange = dockController.onSettingsQueryChange
                        )
                    }
                    DockMode.HIDDEN -> {
                        Spacer(Modifier.size(0.dp))
                    }
                }
            }
        }

        // Compose FAB (only on Inbox when not scrolling)
        AnimatedVisibility(
            visible = dockController.mode == DockMode.INBOX && !isCollapsed,
            enter = fadeIn(tween(160, delayMillis = 80)) + scaleIn(spring(stiffness = Spring.StiffnessMediumLow), initialScale = 0.8f),
            exit = fadeOut(tween(90)) + scaleOut(spring(stiffness = Spring.StiffnessMediumLow), targetScale = 0.8f)
        ) {
            FilledIconButton(
                onClick = onComposeClick,
                modifier = Modifier
                    .size(64.dp)
                    .graphicsLayer {
                        shape = CircleShape
                        clip = false
                        shadowElevation = 10.dp.toPx()
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            ambientShadowColor = Color.Black.copy(alpha = 0.22f)
                            spotShadowColor = Color.Black.copy(alpha = 0.32f)
                        }
                    },
                shape = CircleShape,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            ) {
                Icon(
                    imageVector = Icons.Rounded.Edit,
                    contentDescription = "Compose",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InboxDockContent(
    isCollapsed: Boolean,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
) {
    val tabs = listOf(
        Triple(0, Icons.Rounded.Inbox, "Inbox"),
        Triple(1, Icons.AutoMirrored.Rounded.Send, "Sent"),
        Triple(2, Icons.Rounded.Archive, "Archived")
    )

    SingleChoiceSegmentedButtonRow(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 10.dp, vertical = 8.dp),
        space = 4.dp,
    ) {
        tabs.forEach { (index, icon, label) ->
            val selected = selectedIndex == index
            SegmentedButton(
                selected = selected,
                onClick = { onTabSelected(index) },
                shape = RoundedCornerShape(percent = 50),
                modifier = Modifier.weight(1f),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    activeContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    inactiveContainerColor = Color.Transparent,
                    inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                border = SegmentedButtonDefaults.borderStroke(Color.Transparent),
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        modifier = Modifier.size(20.dp)
                    )
                },
                label = {
                    AnimatedVisibility(
                        visible = !isCollapsed,
                        enter = fadeIn(tween(140, delayMillis = 80)) + expandHorizontally(),
                        exit = fadeOut(tween(90)) + shrinkHorizontally()
                    ) {
                        Text(
                            text = label,
                            maxLines = 1,
                            softWrap = false,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            )
        }
    }
}

@Composable
private fun ThreadDockContent(
    onReply: () -> Unit,
    onForward: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = onReply,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            shape = RoundedCornerShape(percent = 50),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.Reply,
                contentDescription = "Reply",
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Reply", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        }
        OutlinedButton(
            onClick = onForward,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            shape = RoundedCornerShape(percent = 50),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.Forward,
                contentDescription = "Forward",
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Forward", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ComposeDockContent(
    canSend: Boolean,
    isSending: Boolean,
    onAttach: () -> Unit,
    onDrafts: () -> Unit,
    onSend: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onDrafts,
                enabled = !isSending,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Drafts,
                    contentDescription = "Drafts",
                    modifier = Modifier.size(22.dp)
                )
            }
            IconButton(
                onClick = onAttach,
                enabled = !isSending,
            ) {
                Icon(
                    imageVector = Icons.Rounded.AttachFile,
                    contentDescription = "Attach",
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Button(
            onClick = onSend,
            enabled = canSend && !isSending,
            shape = RoundedCornerShape(percent = 50),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
        ) {
            if (isSending) {
                LoadingIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Send,
                    contentDescription = "Send",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text("Send", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun SettingsDockContent(
    query: String,
    onQueryChange: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.Search,
            contentDescription = "Search settings",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Box(modifier = Modifier.weight(1f)) {
            if (query.isEmpty()) {
                Text(
                    text = "Search settings...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                singleLine = true,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (query.isNotEmpty()) {
            IconButton(onClick = { onQueryChange("") }, modifier = Modifier.size(24.dp)) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Clear search",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
