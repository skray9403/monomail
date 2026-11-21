@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class
)

package com.shrivatsav.monomail.ui.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Forward
import androidx.compose.material.icons.automirrored.rounded.Reply
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.Drafts
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.shrivatsav.monomail.ui.components.DockMode
import com.shrivatsav.monomail.ui.components.FloatingDockController
import com.shrivatsav.monomail.ui.components.DockTabItem
import com.shrivatsav.monomail.ui.components.clickableOnPress
import com.shrivatsav.monomail.ui.components.clickableOnRelease
import kotlin.math.abs

@Composable
internal fun ExpressivePersistentDock(
    dockController: FloatingDockController,
    onComposeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (dockController.mode == DockMode.HIDDEN || dockController.isSuppressed) return

    val motionScheme = MaterialTheme.motionScheme
    val isHidden = (dockController.mode == DockMode.INBOX) && dockController.isSearchActive

    val animatedOffsetY by animateDpAsState(
        targetValue = if (isHidden || dockController.isCollapsed) 140.dp else 0.dp,
        animationSpec = motionScheme.defaultSpatialSpec(),
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
        LeftDockElement(dockController = dockController)
        Spacer(Modifier.width(12.dp))
        RightDockElement(
            dockController = dockController,
            onComposeClick = onComposeClick,
        )
    }
}

@Composable
private fun LeftDockElement(
    dockController: FloatingDockController,
    modifier: Modifier = Modifier
) {
    val mode = dockController.mode
    val motionScheme = MaterialTheme.motionScheme
    val containerColor by animateColorAsState(
        targetValue = when (mode) {
            DockMode.THREAD -> MaterialTheme.colorScheme.primaryContainer
            else -> MaterialTheme.colorScheme.surfaceContainerHigh
        },
        animationSpec = motionScheme.fastEffectsSpec(),
        label = "leftElementColor"
    )

    Surface(
        shape = CircleShape,
        color = containerColor,
        shadowElevation = 4.dp,
        modifier = modifier
    ) {
        AnimatedContent(
            targetState = mode,
            transitionSpec = {
                (fadeIn(motionScheme.fastEffectsSpec()) +
                    scaleIn(motionScheme.fastSpatialSpec(), initialScale = 0.96f))
                    .togetherWith(fadeOut(motionScheme.fastEffectsSpec()))
                    .using(
                        SizeTransform(
                            clip = false,
                            sizeAnimationSpec = { _, _ -> motionScheme.defaultSpatialSpec() }
                        )
                    )
            },
            label = "leftDockContent"
        ) { targetMode ->
            when (targetMode) {
                DockMode.INBOX -> {
                    if (dockController.isBulkMode) {
                        BulkSelectionPill(count = dockController.bulkSelectionCount, onExit = { dockController.onExitBulkMode?.invoke() })
                    } else {
                        DynamicSwipeTabPill(dockController = dockController, containerHeight = 56.dp)
                    }
                }
                DockMode.THREAD -> {
                    ReplyActionPill(
                        onClick = { dockController.onReplyAction?.invoke() }
                    )
                }
                DockMode.SETTINGS -> {
                    SettingsBackBubble(
                        onClick = { dockController.onBackClick?.invoke() }
                    )
                }
                DockMode.COMPOSE -> {
                    ComposeLeftActions(
                        onDrafts = { dockController.onDraftsAction?.invoke() },
                        onAttach = { dockController.onAttachAction?.invoke() },
                        isSending = dockController.isSending
                    )
                }
                else -> Spacer(Modifier.size(0.dp))
            }
        }
    }
}

@Composable
private fun RightDockElement(
    dockController: FloatingDockController,
    onComposeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val mode = dockController.mode
    val motionScheme = MaterialTheme.motionScheme
    val containerColor by animateColorAsState(
        targetValue = when (mode) {
            DockMode.INBOX -> if (dockController.isSpamTab) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
            DockMode.COMPOSE -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.surfaceContainerHigh
        },
        animationSpec = motionScheme.fastEffectsSpec(),
        label = "rightElementColor"
    )

    Surface(
        shape = CircleShape,
        color = containerColor,
        shadowElevation = 4.dp,
        modifier = modifier
    ) {
        AnimatedContent(
            targetState = mode,
            transitionSpec = {
                (fadeIn(motionScheme.fastEffectsSpec()) +
                    scaleIn(motionScheme.fastSpatialSpec(), initialScale = 0.96f))
                    .togetherWith(fadeOut(motionScheme.fastEffectsSpec()))
                    .using(
                        SizeTransform(
                            clip = false,
                            sizeAnimationSpec = { _, _ -> motionScheme.defaultSpatialSpec() }
                        )
                    )
            },
            label = "rightDockContent"
        ) { targetMode ->
            when (targetMode) {
                DockMode.INBOX -> {
                    if (dockController.isBulkMode) {
                        BulkActionsPill(actions = dockController.bulkActions)
                    } else Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .clickableOnRelease {
                                dockController.onComposeAction?.invoke() ?: onComposeClick()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (dockController.isSpamTab) Icons.Rounded.Delete else Icons.Rounded.Edit,
                            contentDescription = if (dockController.isSpamTab) "Move spam to trash" else "Compose",
                            tint = if (dockController.isSpamTab) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                DockMode.THREAD -> {
                    ForwardActionPill(
                        onClick = { dockController.onForwardAction?.invoke() }
                    )
                }
                DockMode.SETTINGS -> {
                    SettingsTitlePill(title = dockController.settingsTitle)
                }
                DockMode.COMPOSE -> {
                    ComposeSendPill(
                        canSend = dockController.canSend,
                        isSending = dockController.isSending,
                        onSend = { dockController.onSendAction?.invoke() }
                    )
                }
                else -> Spacer(Modifier.size(0.dp))
            }
        }
    }
}

@Composable
private fun DynamicSwipeTabPill(
    dockController: FloatingDockController,
    containerHeight: Dp
) {
    val tabs = dockController.primaryTabs
    var showOverflow by remember { mutableStateOf(false) }
    val motionScheme = MaterialTheme.motionScheme

    Box {
        Row(
            modifier = Modifier
                .height(containerHeight)
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            tabs.forEach { tab ->
                val selected = dockController.selectedTabId == tab.id
                val containerColor by animateColorAsState(
                    targetValue = if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
                    animationSpec = motionScheme.fastEffectsSpec(),
                    label = "dockTabContainer"
                )
                val contentColor by animateColorAsState(
                    targetValue = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                    animationSpec = motionScheme.fastEffectsSpec(),
                    label = "dockTabContent"
                )
                Row(
                    modifier = Modifier
                        .height(48.dp)
                        .animateContentSize(motionScheme.defaultSpatialSpec())
                        .clip(CircleShape)
                        .background(containerColor)
                        .clickableOnPress { dockController.onTabSelected(tab.id) }
                        .padding(horizontal = if (selected) 13.dp else 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.label,
                        tint = contentColor,
                        modifier = Modifier.size(19.dp)
                    )
                    if (selected) {
                        Spacer(Modifier.width(7.dp))
                        Text(
                            text = tab.label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = contentColor,
                            maxLines = 1,
                            overflow = TextOverflow.Clip
                        )
                    }
                }
            }
            if (dockController.overflowTabs.isNotEmpty()) {
                Box {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .clickableOnPress { showOverflow = !showOverflow },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MoreHoriz,
                            contentDescription = "More folders",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    DropdownMenu(
                        expanded = showOverflow,
                        onDismissRequest = { showOverflow = false }
                    ) {
                        dockController.overflowTabs.forEach { tab ->
                            DropdownMenuItem(
                                text = { Text(tab.label) },
                                leadingIcon = { Icon(tab.icon, contentDescription = null) },
                                onClick = {
                                    showOverflow = false
                                    dockController.onTabSelected(tab.id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun BulkSelectionPill(count: Int, onExit: () -> Unit) {
    Row(
        modifier = Modifier.height(56.dp).padding(start = 16.dp, end = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "${count} selected",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        IconButton(onClick = onExit, modifier = Modifier.size(40.dp)) {
            Icon(Icons.Rounded.Close, contentDescription = "Exit selection", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun BulkActionsPill(actions: List<com.shrivatsav.monomail.ui.components.DockBulkAction>) {
    Row(
        modifier = Modifier.height(56.dp).padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        actions.forEach { action ->
            IconButton(onClick = action.onClick, modifier = Modifier.size(40.dp)) {
                Icon(action.icon, contentDescription = action.label, tint = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
private fun ReplyActionPill(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .height(56.dp)
            .clip(CircleShape)
            .clickableOnRelease(onClick = onClick)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.Reply,
                contentDescription = "Reply",
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Reply",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun ForwardActionPill(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .height(56.dp)
            .clip(CircleShape)
            .clickableOnPress(onPressDown = onClick)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.Forward,
                contentDescription = "Forward",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Forward",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SettingsBackBubble(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .clickableOnPress(onPressDown = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
            contentDescription = "Back",
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun SettingsTitlePill(title: String) {
    Box(
        modifier = Modifier
            .height(56.dp)
            .padding(horizontal = 28.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title.ifEmpty { "Settings" },
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ComposeLeftActions(
    onDrafts: () -> Unit,
    onAttach: () -> Unit,
    isSending: Boolean
) {
    Row(
        modifier = Modifier
            .height(56.dp)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .clickableOnRelease(enabled = !isSending, onClick = onDrafts),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Drafts,
                contentDescription = "Drafts",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .clickableOnRelease(enabled = !isSending, onClick = onAttach),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.AttachFile,
                contentDescription = "Attach",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun ComposeSendPill(
    canSend: Boolean,
    isSending: Boolean,
    onSend: () -> Unit
) {
    Box(
        modifier = Modifier
            .height(56.dp)
            .clip(CircleShape)
            .clickableOnRelease(
                enabled = canSend && !isSending,
                onClick = onSend
            )
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isSending) {
            LoadingIndicator(
                modifier = Modifier.size(22.dp),
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Send,
                    contentDescription = "Send",
                    tint = if (canSend) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Send",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (canSend) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
                )
            }
        }
    }
}
