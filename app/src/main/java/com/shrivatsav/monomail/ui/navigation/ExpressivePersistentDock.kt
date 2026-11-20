@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class
)

package com.shrivatsav.monomail.ui.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.shrivatsav.monomail.ui.components.clickableOnPress
import kotlin.math.abs

@Composable
internal fun ExpressivePersistentDock(
    dockController: FloatingDockController,
    onComposeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (dockController.mode == DockMode.HIDDEN) return

    val motionScheme = MaterialTheme.motionScheme
    val isHidden = (dockController.mode == DockMode.INBOX) && dockController.isSearchActive

    val animatedOffsetY by animateDpAsState(
        targetValue = if (isHidden) 140.dp else 0.dp,
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
                    DynamicSwipeTabPill(
                        continuousPosition = dockController.continuousTabPosition,
                        containerHeight = 56.dp,
                        onTabClick = { dockController.onTabSelected(it) }
                    )
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
            DockMode.INBOX -> MaterialTheme.colorScheme.primaryContainer
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
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .clickableOnPress(onPressDown = onComposeClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = "Compose",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
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
    continuousPosition: Float,
    containerHeight: Dp,
    onTabClick: (Int) -> Unit
) {
    val clampedPos = continuousPosition.coerceIn(0f, 2f)
    val motionScheme = MaterialTheme.motionScheme

    data class TabInfo(val label: String, val icon: ImageVector, val index: Int)
    val tabs = remember {
        listOf(
            TabInfo("Inbox", Icons.Rounded.Inbox, 0),
            TabInfo("Sent", Icons.AutoMirrored.Rounded.Send, 1),
            TabInfo("Archived", Icons.Rounded.Archive, 2)
        )
    }

    val itemHeight = containerHeight - 8.dp // 48dp touch target inside a 56dp dock

    Row(
        modifier = Modifier
            .height(containerHeight)
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        tabs.forEach { tab ->
            val dist = abs(clampedPos - tab.index)
            val labelExpansion = (1f - dist * 2.2f).coerceIn(0f, 1f)
            val isCurrentSelected = dist < 0.5f

            val containerColor by animateColorAsState(
                targetValue = if (isCurrentSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                animationSpec = motionScheme.fastEffectsSpec(),
                label = "tabContainerColor"
            )
            val contentColor by animateColorAsState(
                targetValue = if (isCurrentSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                animationSpec = motionScheme.fastEffectsSpec(),
                label = "tabContentColor"
            )

            Box(
                modifier = Modifier
                    .height(itemHeight)
                    .widthIn(min = 48.dp)
                    .clip(CircleShape)
                    .background(containerColor)
                    .clickableOnPress { onTabClick(tab.index) },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = (12 + (8 * labelExpansion)).dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.label,
                        tint = contentColor,
                        modifier = Modifier.size((20 + (2 * labelExpansion)).dp)
                    )

                    if (labelExpansion > 0.05f) {
                        Spacer(Modifier.width((6 * labelExpansion).dp))
                        Text(
                            text = tab.label,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = contentColor,
                            modifier = Modifier.graphicsLayer {
                                alpha = labelExpansion
                                scaleX = 0.85f + (0.15f * labelExpansion)
                                scaleY = 0.85f + (0.15f * labelExpansion)
                            },
                            maxLines = 1
                        )
                    }
                }
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
            .clickableOnPress(onPressDown = onClick)
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
                .clickableOnPress(enabled = !isSending, onPressDown = onDrafts),
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
                .clickableOnPress(enabled = !isSending, onPressDown = onAttach),
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
            .clickableOnPress(
                enabled = canSend && !isSending,
                onPressDown = onSend
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
