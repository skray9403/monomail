@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class
)

package com.shrivatsav.monomail.ui.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.shrivatsav.monomail.ui.components.DockMode
import com.shrivatsav.monomail.ui.components.FloatingDockController

/**
 * Single, persistent, Material 3 Expressive floating bottom navigation bar and dock.
 *
 * Implementation Code for Material 3 Expressive Floating Bottom Navigation Bar
 * Author: Er. Mohd Amaan
 *
 * Uses Surface with CircleShape directly to prevent rectangular shadow artifacts,
 * native 8.dp shadow elevation, and smooth animateContentSize transitions.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun ExpressivePersistentDock(
    dockController: FloatingDockController,
    onComposeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (dockController.mode == DockMode.HIDDEN) return

    val isInboxMode = dockController.mode == DockMode.INBOX
    val isScrolling = isInboxMode && dockController.isScrolling

    // Vertical hide/reveal offset on scroll fling (Gmail & M3 FloatingToolbar pattern)
    val animatedOffsetY by animateDpAsState(
        targetValue = if (isScrolling) 100.dp else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "dockOffsetY"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .offset { IntOffset(x = 0, y = animatedOffsetY.roundToPx()) }
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isInboxMode) {
            // Material 3 Expressive Floating Bottom Navigation Bar
            Surface(
                shape = CircleShape, // Prevents rectangular shadow artifacts!
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shadowElevation = 8.dp,
                modifier = Modifier.animateContentSize()
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 6.dp, vertical = 6.dp)
                        .height(52.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    FloatingNavItem(
                        label = "Inbox",
                        icon = Icons.Rounded.Inbox,
                        selected = dockController.selectedTabIndex == 0,
                        onClick = { dockController.onTabSelected(0) }
                    )
                    FloatingNavItem(
                        label = "Sent",
                        icon = Icons.AutoMirrored.Rounded.Send,
                        selected = dockController.selectedTabIndex == 1,
                        onClick = { dockController.onTabSelected(1) }
                    )
                    FloatingNavItem(
                        label = "Archived",
                        icon = Icons.Rounded.Archive,
                        selected = dockController.selectedTabIndex == 2,
                        onClick = { dockController.onTabSelected(2) }
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            // Compose FAB
            FilledIconButton(
                onClick = onComposeClick,
                modifier = Modifier
                    .size(56.dp)
                    .shadow(8.dp, CircleShape),
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
        } else {
            // Mode-specific persistent pill (Detail Reply/Forward, Compose bar, Settings search)
            Surface(
                shape = CircleShape, // Prevents rectangular shadow artifacts!
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .animateContentSize()
            ) {
                AnimatedContent(
                    targetState = dockController.mode,
                    transitionSpec = {
                        (fadeIn(tween(140, delayMillis = 60)) + scaleIn(tween(140, delayMillis = 60), initialScale = 0.95f))
                            .togetherWith(fadeOut(tween(60)) + scaleOut(tween(60), targetScale = 0.95f))
                    },
                    label = "dockModeContent"
                ) { mode ->
                    when (mode) {
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
                        else -> {
                            Spacer(Modifier.size(0.dp))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Expressive floating navigation item that expands its label on selection
 * and gracefully displays the icon on unselected tabs.
 */
@Composable
fun RowScope.FloatingNavItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedBgColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "tabBg"
    )
    val animatedContentColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "tabContent"
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(CircleShape)
            .background(animatedBgColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick
            )
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = animatedContentColor,
                modifier = Modifier.size(20.dp)
            )
            AnimatedVisibility(
                visible = selected,
                enter = expandHorizontally(spring(stiffness = Spring.StiffnessMedium)) + fadeIn(),
                exit = shrinkHorizontally(spring(stiffness = Spring.StiffnessMedium)) + fadeOut()
            ) {
                Row {
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = label,
                        maxLines = 1,
                        softWrap = false,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = animatedContentColor
                    )
                }
            }
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
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = onReply,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            shape = CircleShape,
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
            shape = CircleShape,
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
            .padding(horizontal = 14.dp, vertical = 6.dp),
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
            shape = CircleShape,
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
