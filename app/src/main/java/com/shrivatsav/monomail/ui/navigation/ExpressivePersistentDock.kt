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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.shrivatsav.monomail.ui.components.DockMode
import com.shrivatsav.monomail.ui.components.FloatingDockController
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Single, persistent, Material 3 Expressive floating dual-element dock architecture.
 *
 * Implements:
 * 1. Dual separate floating elements without background container clutter.
 * 2. 56dp height substantial architecture across all destinations.
 * 3. Dynamic 1:1 continuous swipe tab pill: selected = [Icon + Bold Label], unselected = [Icon].
 * 4. Directional scroll contraction: scrolling down into content contracts tabs into compact 3-icon pill
 *    [Icon] [Icon] [Icon] and FAB to 46dp; stopping retains collapsed shape; scrolling in opposite direction expands.
 * 5. Surface with CircleShape & 6.dp soft shadow elevation, perfectly elevated above system insets.
 * 6. Calm Crossfade morphing transitions across Home, Thread Detail, Settings, and Compose.
 * 7. Clean slide-down dismissal strictly when search is active.
 */
@Composable
internal fun ExpressivePersistentDock(
    dockController: FloatingDockController,
    onComposeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (dockController.mode == DockMode.HIDDEN) return

    val isHidden = (dockController.mode == DockMode.INBOX) && dockController.isSearchActive

    // Vertical hide offset ONLY when search is active (scrolling contracts rather than hides)
    val animatedOffsetY by animateDpAsState(
        targetValue = if (isHidden) 140.dp else 0.dp,
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
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        ) {
            AnimatedContent(
                targetState = mode,
                transitionSpec = {
                    (fadeIn(tween(160, delayMillis = 30)) + scaleIn(tween(160, delayMillis = 30), initialScale = 0.94f))
                        .togetherWith(fadeOut(tween(100)) + scaleOut(tween(100), targetScale = 0.94f))
                },
                label = "leftDockContent"
            ) { targetMode ->
                when (targetMode) {
                    DockMode.INBOX -> {
                        DynamicSwipeTabPill(
                            continuousPosition = dockController.continuousTabPosition,
                            isCollapsed = dockController.isScrolling,
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
                            isCollapsed = dockController.isScrolling,
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
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        ) {
            AnimatedContent(
                targetState = mode,
                transitionSpec = {
                    (fadeIn(tween(160, delayMillis = 30)) + scaleIn(tween(160, delayMillis = 30), initialScale = 0.94f))
                        .togetherWith(fadeOut(tween(100)) + scaleOut(tween(100), targetScale = 0.94f))
                },
                label = "rightDockContent"
            ) { targetMode ->
                when (targetMode) {
                    DockMode.INBOX -> {
                        val fabSize by animateDpAsState(
                            targetValue = if (dockController.isScrolling) 46.dp else 56.dp,
                            animationSpec = spring(stiffness = Spring.StiffnessMedium),
                            label = "composeFabSize"
                        )
                        IconButton(
                            onClick = onComposeClick,
                            modifier = Modifier.size(fabSize)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = "Compose",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(if (dockController.isScrolling) 20.dp else 22.dp)
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
                            title = dockController.settingsTitle,
                            isCollapsed = dockController.isScrolling
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
}

/**
 * Dynamic 1:1 Continuous Swipe Tab Pill:
 * - At rest: Selected tab = [Icon + Bold Label], unselected = [Icon only].
 * - During swipe: Shifts through compact 3-icon state [Icon] [Icon] [Icon], smoothly expanding target label.
 * - Directional scroll: Collapses into compact 3-icon state when scrolling down; stays collapsed; regains full shape on opposite swipe!
 */
@Composable
private fun DynamicSwipeTabPill(
    continuousPosition: Float,
    isCollapsed: Boolean,
    containerHeight: androidx.compose.ui.unit.Dp,
    onTabClick: (Int) -> Unit
) {
    val clampedPos = continuousPosition.coerceIn(0f, 2f)

    data class TabInfo(val label: String, val icon: ImageVector, val index: Int)
    val tabs = remember {
        listOf(
            TabInfo("Inbox", Icons.Rounded.Inbox, 0),
            TabInfo("Sent", Icons.AutoMirrored.Rounded.Send, 1),
            TabInfo("Archived", Icons.Rounded.Archive, 2)
        )
    }

    val itemHeight = containerHeight - 12.dp

    Row(
        modifier = Modifier
            .height(containerHeight)
            .animateContentSize(spring(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioNoBouncy))
            .padding(horizontal = 6.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        tabs.forEach { tab ->
            val dist = abs(clampedPos - tab.index)
            val baseExpansion = (1f - dist * 2.2f).coerceIn(0f, 1f)
            val labelExpansion = if (isCollapsed) 0f else baseExpansion
            val isCurrentSelected = dist < 0.5f

            val containerColor by animateColorAsState(
                targetValue = if (isCurrentSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                animationSpec = spring(stiffness = Spring.StiffnessMedium),
                label = "tabContainerColor"
            )
            val contentColor by animateColorAsState(
                targetValue = if (isCurrentSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                animationSpec = spring(stiffness = Spring.StiffnessMedium),
                label = "tabContentColor"
            )

            Box(
                modifier = Modifier
                    .height(itemHeight)
                    .clip(CircleShape)
                    .background(containerColor)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true)
                    ) { onTabClick(tab.index) },
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
private fun ReplyActionPill(
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .height(56.dp)
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick
            )
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
private fun ForwardActionPill(
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .height(56.dp)
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick
            )
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
private fun SettingsBackBubble(
    isCollapsed: Boolean,
    onClick: () -> Unit
) {
    val bubbleSize by animateDpAsState(
        targetValue = if (isCollapsed) 46.dp else 56.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "settingsBubbleSize"
    )
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(bubbleSize)
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
    title: String,
    isCollapsed: Boolean
) {
    val horizPadding by animateDpAsState(
        targetValue = if (isCollapsed) 16.dp else 28.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "settingsTitlePadding"
    )
    Box(
        modifier = Modifier
            .height(56.dp)
            .padding(horizontal = horizPadding),
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
        IconButton(onClick = onDrafts, enabled = !isSending, modifier = Modifier.size(44.dp)) {
            Icon(
                imageVector = Icons.Rounded.Drafts,
                contentDescription = "Drafts",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
        IconButton(onClick = onAttach, enabled = !isSending, modifier = Modifier.size(44.dp)) {
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
            .clickable(
                enabled = canSend && !isSending,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
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
