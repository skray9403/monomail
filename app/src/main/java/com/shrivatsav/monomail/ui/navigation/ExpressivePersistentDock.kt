@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class
)

package com.shrivatsav.monomail.ui.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.shrivatsav.monomail.ui.components.DockMode
import com.shrivatsav.monomail.ui.components.FloatingDockController
import kotlin.math.roundToInt

/**
 * Single, persistent, Material 3 Expressive floating dual-element dock architecture.
 *
 * Implements:
 * 1. Dual separate floating elements without background container clutter.
 * 2. 1:1 continuous touch-synchronized sliding tab indicator for Inbox, Sent, and Archived.
 * 3. Surface with CircleShape & 6.dp soft shadow elevation, perfectly elevated above system insets.
 * 4. Smooth morphing transitions across Home, Thread Detail, Settings, and Compose.
 * 5. Automatic fluid slide-down dismissal when search is active or list is scrolling.
 */
@Composable
internal fun ExpressivePersistentDock(
    dockController: FloatingDockController,
    onComposeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (dockController.mode == DockMode.HIDDEN) return

    val isHidden = dockController.isSearchActive
    val isScrolling = (dockController.mode == DockMode.INBOX) && dockController.isScrolling

    // Vertical hide/reveal offset on scroll fling or search focus
    val animatedOffsetY by animateDpAsState(
        targetValue = if (isHidden || isScrolling) 140.dp else 0.dp,
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
        // LEFT FLOATING ELEMENT
        LeftDockElement(
            dockController = dockController,
        )

        Spacer(Modifier.width(12.dp))

        // RIGHT FLOATING ELEMENT
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
    val containerColor by animateColorAsState(
        targetValue = when (mode) {
            DockMode.THREAD -> MaterialTheme.colorScheme.primaryContainer
            else -> MaterialTheme.colorScheme.surfaceContainerHigh
        },
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "leftElementColor"
    )

    Surface(
        shape = CircleShape,
        color = containerColor,
        shadowElevation = 6.dp,
        modifier = modifier.animateContentSize(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        )
    ) {
        AnimatedContent(
            targetState = mode,
            transitionSpec = {
                (fadeIn(tween(160, delayMillis = 40)) + scaleIn(tween(160, delayMillis = 40), initialScale = 0.92f))
                    .togetherWith(fadeOut(tween(90)) + scaleOut(tween(90), targetScale = 0.92f))
            },
            label = "leftDockContent"
        ) { targetMode ->
            when (targetMode) {
                DockMode.INBOX -> {
                    InboxSlidingTabs(
                        continuousPosition = dockController.continuousTabPosition,
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
    val containerColor by animateColorAsState(
        targetValue = when (mode) {
            DockMode.INBOX -> MaterialTheme.colorScheme.primaryContainer
            DockMode.COMPOSE -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.surfaceContainerHigh
        },
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "rightElementColor"
    )

    Surface(
        shape = CircleShape,
        color = containerColor,
        shadowElevation = 6.dp,
        modifier = modifier.animateContentSize(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        )
    ) {
        AnimatedContent(
            targetState = mode,
            transitionSpec = {
                (fadeIn(tween(160, delayMillis = 40)) + scaleIn(tween(160, delayMillis = 40), initialScale = 0.92f))
                    .togetherWith(fadeOut(tween(90)) + scaleOut(tween(90), targetScale = 0.92f))
            },
            label = "rightDockContent"
        ) { targetMode ->
            when (targetMode) {
                DockMode.INBOX -> {
                    IconButton(
                        onClick = onComposeClick,
                        modifier = Modifier.size(48.dp)
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
                    SettingsTitlePill(
                        title = dockController.settingsTitle
                    )
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

/**
 * 1:1 Continuous Sliding Tab Indicator matching Google Messages.
 * Tracks pager touch swipes in real time via continuous position interpolation.
 */
@Composable
private fun InboxSlidingTabs(
    continuousPosition: Float,
    onTabClick: (Int) -> Unit
) {
    val density = androidx.compose.ui.platform.LocalDensity.current
    val tabWidthDp = 76.dp
    val tabHeightDp = 40.dp
    val tabWidthPx = with(density) { tabWidthDp.toPx() }

    val tabs = remember {
        listOf(
            Triple("Inbox", Icons.Rounded.Inbox, 0),
            Triple("Sent", Icons.AutoMirrored.Rounded.Send, 1),
            Triple("Archived", Icons.Rounded.Archive, 2),
        )
    }

    Box(
        modifier = Modifier
            .padding(horizontal = 4.dp, vertical = 4.dp)
            .height(tabHeightDp),
        contentAlignment = Alignment.CenterStart
    ) {
        // Continuous 1:1 Sliding Pill Indicator
        val clampedPosition = continuousPosition.coerceIn(0f, 2f)
        val indicatorOffsetPx = clampedPosition * tabWidthPx

        Box(
            modifier = Modifier
                .offset { IntOffset(x = indicatorOffsetPx.roundToInt(), y = 0) }
                .width(tabWidthDp)
                .height(tabHeightDp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
        )

        // Tab items on top of sliding indicator
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEach { (label, icon, index) ->
                val distance = kotlin.math.abs(clampedPosition - index)
                val activeFraction = (1f - distance).coerceIn(0f, 1f)

                val contentColor = androidx.compose.ui.graphics.lerp(
                    MaterialTheme.colorScheme.onSurfaceVariant,
                    MaterialTheme.colorScheme.onPrimaryContainer,
                    activeFraction
                )

                Box(
                    modifier = Modifier
                        .width(tabWidthDp)
                        .height(tabHeightDp)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true),
                            onClick = { onTabClick(index) }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = contentColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = label,
                            maxLines = 1,
                            softWrap = false,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (activeFraction > 0.5f) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = contentColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReplyActionPill(
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .height(48.dp)
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick
            )
            .padding(horizontal = 20.dp),
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
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun ForwardActionPill(
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .height(48.dp)
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick
            )
            .padding(horizontal = 20.dp),
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
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SettingsBackBubble(
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(48.dp)
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
private fun SettingsTitlePill(
    title: String
) {
    Box(
        modifier = Modifier
            .height(48.dp)
            .padding(horizontal = 24.dp),
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
            .height(48.dp)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        IconButton(onClick = onDrafts, enabled = !isSending, modifier = Modifier.size(40.dp)) {
            Icon(
                imageVector = Icons.Rounded.Drafts,
                contentDescription = "Drafts",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
        IconButton(onClick = onAttach, enabled = !isSending, modifier = Modifier.size(40.dp)) {
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
            .height(48.dp)
            .clip(CircleShape)
            .clickable(
                enabled = canSend && !isSending,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onSend
            )
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isSending) {
            LoadingIndicator(
                modifier = Modifier.size(20.dp),
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
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Send",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = if (canSend) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
                )
            }
        }
    }
}
