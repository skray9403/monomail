@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class
)

package com.shrivatsav.monomail.ui.navigation

import android.os.Build
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
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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

    val isInboxMode = dockController.mode == DockMode.INBOX
    val isScrolling = isInboxMode && dockController.isScrolling
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp

    val expandedDockWidth = (screenWidth - 32.dp) - 76.dp
    val collapsedDockWidth = 180.dp
    val fullScreenWidth = screenWidth - 32.dp

    // Unified collapse progress (0f = full tabs + FAB at rest, 1f = compact icon pill while scrolling)
    // Synchronizes text alpha and tab spacing strictly for Inbox tab contents
    val collapseFraction by animateFloatAsState(
        targetValue = if (isScrolling) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "dockCollapseFraction"
    )

    // Animated container dimensions across BOTH scroll collapse and route morphing
    val targetDockWidth = when {
        !isInboxMode -> fullScreenWidth
        isScrolling -> collapsedDockWidth
        else -> expandedDockWidth
    }

    val targetFabWidth = if (isInboxMode && !isScrolling) 64.dp else 0.dp
    val targetFabSpacing = if (isInboxMode && !isScrolling) 12.dp else 0.dp
    val targetFabAlpha = if (isInboxMode && !isScrolling) 1f else 0f

    val springDp = spring<androidx.compose.ui.unit.Dp>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow
    )
    val springFloat = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow
    )

    val animatedDockWidth by animateDpAsState(
        targetValue = targetDockWidth,
        animationSpec = springDp,
        label = "dockWidth"
    )

    val animatedFabWidth by animateDpAsState(
        targetValue = targetFabWidth,
        animationSpec = springDp,
        label = "fabWidth"
    )

    val animatedFabSpacing by animateDpAsState(
        targetValue = targetFabSpacing,
        animationSpec = springDp,
        label = "fabSpacing"
    )

    val animatedFabAlpha by animateFloatAsState(
        targetValue = targetFabAlpha,
        animationSpec = springFloat,
        label = "fabAlpha"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Floating pill container with Google Messages diffused elevation
        Surface(
            modifier = Modifier
                .width(animatedDockWidth)
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
                    // Sequence outgoing exit (fast 80ms fade/scale) followed by incoming entrance (delay 80ms)
                    // so outgoing and incoming screens never draw on top of each other inside the dock container
                    (fadeIn(tween(140, delayMillis = 80)) + scaleIn(tween(140, delayMillis = 80), initialScale = 0.94f))
                        .togetherWith(fadeOut(tween(80)) + scaleOut(tween(80), targetScale = 0.94f))
                },
                label = "dockModeTransition"
            ) { mode ->
                when (mode) {
                    DockMode.INBOX -> {
                        InboxDockContent(
                            collapseFraction = collapseFraction,
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

        // Compose FAB (synchronized with identical spring physics, continuous width & alpha)
        if (animatedFabWidth > 0.5.dp) {
            Spacer(Modifier.width(animatedFabSpacing))
            FilledIconButton(
                onClick = onComposeClick,
                modifier = Modifier
                    .size(animatedFabWidth)
                    .graphicsLayer {
                        alpha = animatedFabAlpha
                        val scale = (animatedFabWidth / 64.dp).coerceIn(0f, 1f)
                        scaleX = scale
                        scaleY = scale
                        shape = CircleShape
                        clip = false
                        shadowElevation = (10.dp * animatedFabAlpha).toPx()
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            ambientShadowColor = Color.Black.copy(alpha = 0.22f * animatedFabAlpha)
                            spotShadowColor = Color.Black.copy(alpha = 0.32f * animatedFabAlpha)
                        }
                    },
                shape = CircleShape,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            ) {
                if (animatedFabAlpha > 0.15f && animatedFabWidth > 24.dp) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = "Compose",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun InboxDockContent(
    collapseFraction: Float,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
) {
    val tabs = remember {
        listOf(
            Triple(0, Icons.Rounded.Inbox, "Inbox"),
            Triple(1, Icons.AutoMirrored.Rounded.Send, "Sent"),
            Triple(2, Icons.Rounded.Archive, "Archived")
        )
    }

    // Text alpha drops quickly as soon as collapse starts, and only fades in once container is wide
    val textAlpha = (1f - collapseFraction * 2.5f).coerceIn(0f, 1f)

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        tabs.forEach { (index, icon, label) ->
            val selected = selectedIndex == index
            val animatedBgColor by animateColorAsState(
                targetValue = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                animationSpec = tween(180),
                label = "tabBg_$index"
            )
            val animatedContentColor by animateColorAsState(
                targetValue = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                animationSpec = tween(180),
                label = "tabContent_$index"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(percent = 50))
                    .background(animatedBgColor)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true)
                    ) { onTabSelected(index) },
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
                    if (textAlpha > 0.01f) {
                        Spacer(Modifier.width((6.dp * (1f - collapseFraction)).coerceAtLeast(0.dp)))
                        Text(
                            text = label,
                            maxLines = 1,
                            softWrap = false,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
                            ),
                            color = animatedContentColor.copy(alpha = animatedContentColor.alpha * textAlpha),
                            modifier = Modifier.graphicsLayer { alpha = textAlpha }
                        )
                    }
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
