# MonoMail UI, Layout & Material 3 Expressive Polish Guide

This guide consolidates all architectural findings, motion refinements, touch-down (`onPress`) responsiveness upgrades, and exact Kotlin/Jetpack Compose code edits for **MonoMail**.

---

## 1. Executive Summary of Root Causes

1. **Competing Layout & Layer Animations in the Bottom Dock:**
   * `LeftDockElement` and `RightDockElement` wrapped `AnimatedContent` inside `animateContentSize`, while `DynamicSwipeTabPill` applied a third `animateContentSize` alongside continuous `labelExpansion` padding/width interpolation.
   * Vertical list scrolling collapsed/expanded dock pill dimensions (`56dp` $\leftrightarrow$ `46dp`/`48dp`), causing constant bottom chrome resizing while reading.
2. **Action-on-Release Instead of Touch-Down (`onPress`):**
   * Stationary floating dock tabs, Reply/Forward pills, Settings back/category items, and Compose actions used `Modifier.clickable`, waiting until finger lift (`onUp`) before triggering navigation or tab switching.
3. **Multi-Plane Route Transitions & Intermediate Page Flashes:**
   * `NavGraph.kt` stacked horizontal/vertical slides with simultaneous `scaleIn` + `scaleOut` on standard forward pushes, competing with `PredictiveBack.kt` gestures.
   * Jumping across non-adjacent tabs in `InboxScreen.kt` (`Inbox` $\leftrightarrow$ `Archived`) animated through `Sent`, causing an intermediate tab state flash.
4. **Heavy `WebView` Lifecycle & Main-Thread Image Decoding:**
   * `EmailWebViewCard` keyed `AndroidView` on `htmlContent`, tearing down and recreating the `WebView` every time quoted text was toggled, while reserving a `450.dp` minimum skeleton height.
   * `ImageAttachmentCard` decoded byte arrays synchronously on the UI thread inside composition.
   * `ComposeScreen` held a `1500ms` artificial delay after sending with a bouncy (`dampingRatio = 0.5f`) overlay animation.

---

## 2. Keeping vs. Refining Prior Audit Patches

* **Keep from prior patches:**
  * Moving `MotionLabActivity` into `app/src/debug/AndroidManifest.xml` so release builds do not ship a dev launcher.
  * Untracking `.idea/` in `.gitignore`.
  * Enforcing `48.dp` minimum touch targets (`itemHeight = containerHeight - 8.dp`, `widthIn(min = 48.dp)` on tab items, and `48.dp` Compose action buttons).
* **Skip / Supersede:**
  * Manual reduced-motion wrappers are unnecessary because Jetpack Compose already honors `Settings.Global.ANIMATOR_DURATION_SCALE` natively.
  * Scroll-induced dock resizing (`isScrolling` height/width collapse) is removed in favor of keeping the dock spatially anchored during vertical scrolling.

---

## 3. Exact Code Changes

### 3.1 Touch-Down (`onPress`) Modifier Helper

Create `app/src/main/java/com/shrivatsav/monomail/ui/components/InstantPress.kt`:

```kotlin
package com.shrivatsav.monomail.ui.components

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.material3.ripple
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics

/**
 * Triggers [onPressDown] immediately on finger contact (pointer-down) rather than
 * waiting for finger lift, matching 1st-party navigation bar / floating pill responsiveness.
 * Use ONLY on stationary chrome (dock pills, FABs, non-scrolling hub items), NOT inside
 * vertically scrollable message lists.
 */
fun Modifier.clickableOnPress(
    enabled: Boolean = true,
    haptic: Boolean = true,
    onPressDown: () -> Unit,
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val haptics = LocalHapticFeedback.current

    this
        .semantics(mergeDescendants = true) {
            role = Role.Button
            if (enabled) {
                onClick {
                    onPressDown()
                    true
                }
            }
        }
        .indication(interactionSource, ripple(bounded = true))
        .pointerInput(enabled, onPressDown) {
            if (!enabled) return@pointerInput
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                down.consume()
                val press = PressInteraction.Press(down.position)
                interactionSource.tryEmit(press)
                if (haptic) {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
                onPressDown()
                val up = waitForUpOrCancellation()
                if (up != null) {
                    interactionSource.tryEmit(PressInteraction.Release(press))
                } else {
                    interactionSource.tryEmit(PressInteraction.Cancel(press))
                }
            }
        }
}
```

---

### 3.2 Persistent Bottom Dock (`ExpressivePersistentDock.kt`)

Replace `app/src/main/java/com/shrivatsav/monomail/ui/navigation/ExpressivePersistentDock.kt`:
* Removes triple-nested `animateContentSize` wrappers in favor of a single `AnimatedContent` + `SizeTransform`.
* Keeps `48.dp` minimum touch targets inside a stable `56.dp` dock without scroll-driven resizing.
* Wires `clickableOnPress` across all dock pills and action buttons.

```kotlin
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
```

---

### 3.3 Navigation Transitions (`NavGraph.kt`)

In `app/src/main/java/com/shrivatsav/monomail/ui/navigation/NavGraph.kt`, remove unused `NavSpatialSpec`/`NavFloatSpec` and update `slideInForRoute` and `slideOutForRoute` so forward pushes use clean directional motion without double-scaling:

```kotlin
private fun AnimatedContentTransitionScope<androidx.navigation.NavBackStackEntry>.slideInForRoute(
    motionScheme: MotionScheme
): EnterTransition = when (routeType(targetState.destination.route)) {
    "compose" ->
        slideIntoContainer(
            AnimatedContentTransitionScope.SlideDirection.Up,
            animationSpec = motionScheme.defaultSpatialSpec(),
        ) + fadeIn(animationSpec = motionScheme.fastEffectsSpec())
    "drill" ->
        slideIntoContainer(
            AnimatedContentTransitionScope.SlideDirection.Start,
            animationSpec = motionScheme.defaultSpatialSpec(),
        ) + fadeIn(animationSpec = motionScheme.fastEffectsSpec())
    else -> fadeIn(animationSpec = motionScheme.fastEffectsSpec())
}

private fun AnimatedContentTransitionScope<androidx.navigation.NavBackStackEntry>.slideOutForRoute(
    motionScheme: MotionScheme
): ExitTransition = when (routeType(targetState.destination.route)) {
    "compose", "drill" ->
        fadeOut(
            animationSpec = motionScheme.fastEffectsSpec(),
            targetAlpha = 0.92f,
        )
    else -> fadeOut(animationSpec = motionScheme.fastEffectsSpec())
}
```

---

### 3.4 Inbox Tab Switching & Dead Code Cleanup (`InboxScreen.kt` & `InboxSearchBar.kt`)

1. **In `feature/inbox/src/main/java/com/shrivatsav/monomail/feature/inbox/InboxScreen.kt`:**
   * Switch `viewModel.switchTab` immediately on touch-down and jump directly (`scrollToPage`) when skipping across non-adjacent tabs (`0` $\leftrightarrow$ `2`) so `Sent` does not flash in between:

```kotlin
val motionScheme = MaterialTheme.motionScheme

LaunchedEffect(Unit) {
    dockController.onTabSelected = { targetIdx ->
        val targetTab = primaryTabs.getOrNull(targetIdx) ?: InboxTab.INBOX
        if (immediateTab != targetTab) {
            viewModel.switchTab(targetTab)
        }
        coroutineScope.launch {
            if (kotlin.math.abs(pagerState.currentPage - targetIdx) > 1) {
                pagerState.scrollToPage(targetIdx)
            } else {
                pagerState.animateScrollToPage(
                    page = targetIdx,
                    animationSpec = motionScheme.defaultSpatialSpec()
                )
            }
        }
    }
}

LaunchedEffect(isSearchExpanded, isSearchFocused, isSearchActive, searchFilters.query) {
    dockController.isSearchActive =
        isSearchExpanded || isSearchFocused || isSearchActive || searchFilters.query.isNotEmpty()
}

LaunchedEffect(isSearchActive, isSearchExpanded) {
    if (!isSearchActive && !isSearchExpanded) {
        focusManager.clearFocus()
    }
}
```

   * Delete unused `BottomFabArea`, `MoveToTrashFab`, and `ComposeFab` in `InboxScreen.kt`.
   * Delete unused files `BottomDockBar.kt` and `DockTab.kt` in `feature/inbox/components/`.
2. **In `feature/inbox/src/main/java/com/shrivatsav/monomail/feature/inbox/components/InboxSearchBar.kt`:**
   * Delete the unused `SearchInputContent` private composable.

---

### 3.5 Settings Theme Harmonization & Spring Alignment (`SettingsScreen.kt` & `InboxSettingsScreen.kt`)

1. **In `feature/settings/src/main/java/com/shrivatsav/monomail/feature/settings/SettingsScreen.kt`:**
   * Derive `sectionColors` from `MaterialTheme.colorScheme` container roles so settings icon badges harmonize with dynamic wallpaper colors, and use `clickableOnPress` on `SettingsHubItem`:

```kotlin
import com.shrivatsav.monomail.ui.components.clickableOnPress

@Composable
private fun sectionColors(section: SettingsSection): Pair<Color, Color> {
    val scheme = MaterialTheme.colorScheme
    return when (section) {
        SettingsSection.ACCOUNTS,
        SettingsSection.INBOX,
        SettingsSection.NAVIGATION -> scheme.primaryContainer to scheme.onPrimaryContainer

        SettingsSection.APPEARANCE,
        SettingsSection.COMPOSE,
        SettingsSection.PRIVACY -> scheme.secondaryContainer to scheme.onSecondaryContainer

        SettingsSection.READING,
        SettingsSection.NOTIFICATIONS,
        SettingsSection.SUPPORT -> scheme.tertiaryContainer to scheme.onTertiaryContainer

        SettingsSection.DEVELOPER -> scheme.surfaceContainerHighest to scheme.onSurfaceVariant
    }
}

@Composable
private fun SettingsHubItem(
    section: SettingsSection,
    onClick: (SettingsSection) -> Unit,
) {
    val (containerColor, iconColor) = sectionColors(section)

    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickableOnPress { onClick(section) },
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(containerColor),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = section.icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp),
                )
            }
        },
        headlineContent = {
            Text(section.title, style = MaterialTheme.typography.bodyLarge)
        },
        supportingContent = {
            Text(
                section.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        trailingContent = {
            Icon(
                Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
}
```

2. **In `feature/settings/src/main/java/com/shrivatsav/monomail/feature/settings/InboxSettingsScreen.kt`:**
   * Replace `Spring.DampingRatioMediumBouncy` on `AnimatedVisibility` with `MaterialTheme.motionScheme.defaultSpatialSpec()` + `fastEffectsSpec()`.

---

### 3.6 Email Detail `WebView` Stability & Off-Thread Image Decoding (`EmailDetailScreen.kt`)

In `feature/detail/src/main/java/com/shrivatsav/monomail/feature/detail/EmailDetailScreen.kt`:
* Key `EmailWebViewCard` on `(emailId, allowRemoteImages, renderGeneration)` instead of `htmlContent` so toggling quoted text reloads HTML in-place without destroying/recreating the `WebView` or reserving a `450.dp` empty block.
* Decode image attachment bytes on `Dispatchers.Default` inside `LaunchedEffect` instead of synchronously in composition.

```kotlin
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun EmailWebViewCard(
    emailId: String,
    htmlContent: String,
    useOverviewScaling: Boolean,
    allowRemoteImages: Boolean
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var pendingLink by remember(emailId) { mutableStateOf<Uri?>(null) }
    var renderGeneration by remember(emailId, allowRemoteImages) { mutableStateOf(0) }
    var rendererRetryCount by remember(emailId, allowRemoteImages) { mutableStateOf(0) }
    var isLoaded by remember(emailId, allowRemoteImages, renderGeneration) { mutableStateOf(false) }
    var loadError by remember(emailId, allowRemoteImages, renderGeneration) { mutableStateOf<String?>(null) }
    val alpha by animateFloatAsState(
        targetValue = if (isLoaded) 1f else 0f,
        animationSpec = MaterialTheme.motionScheme.fastEffectsSpec(),
        label = "WebViewFade"
    )
    var emailContentWebView by remember(emailId, allowRemoteImages, renderGeneration) { mutableStateOf<WebView?>(null) }
    var lastLoadedHtml by remember(emailId, allowRemoteImages, renderGeneration) { mutableStateOf("") }

    DisposableEffect(emailId, allowRemoteImages, renderGeneration) {
        onDispose {
            emailContentWebView?.apply { removeAllViews(); destroy() }
            emailContentWebView = null
        }
    }
    val useDarkening = isSystemInDarkTheme()

    Column {
        if (htmlContent.isBlank()) {
            EmailBodyLoadingSkeleton(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).heightIn(min = 140.dp)
            )
        } else {
            androidx.compose.runtime.key(emailId, allowRemoteImages, renderGeneration) {
                val callbackGeneration = renderGeneration
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .heightIn(min = 120.dp)
                        .clip(cornerShape(16.dp))
                ) {
                    AndroidView(
                        modifier = Modifier.fillMaxWidth().padding(12.dp).alpha(alpha),
                        factory = { ctx ->
                            WebView(ctx).apply {
                                emailContentWebView = this
                                configureWebView(this, allowRemoteImages)
                                setOnTouchListener(createTouchHandler())
                                webViewClient = createWebViewClient(
                                    allowRemoteImages = allowRemoteImages,
                                    onLinkClick = { pendingLink = it },
                                    onPageFinished = {
                                        if (callbackGeneration == renderGeneration) {
                                            loadError = null
                                            isLoaded = true
                                        }
                                    },
                                    onMainFrameError = { message ->
                                        if (callbackGeneration == renderGeneration) {
                                            isLoaded = false
                                            loadError = message
                                        }
                                    },
                                    onRenderProcessGone = {
                                        if (callbackGeneration == renderGeneration) {
                                            emailContentWebView = null
                                            isLoaded = false
                                            if (rendererRetryCount < 1) {
                                                rendererRetryCount++
                                                renderGeneration++
                                            } else {
                                                loadError = "Email renderer stopped unexpectedly."
                                            }
                                        }
                                    }
                                )
                            }
                        },
                        update = { webView ->
                            webView.settings.loadWithOverviewMode = false
                            webView.settings.useWideViewPort = false
                            webView.isHorizontalScrollBarEnabled = !useOverviewScaling
                            webView.settings.blockNetworkImage = !allowRemoteImages
                            webView.settings.textZoom = (context.resources.configuration.fontScale * 100f)
                                .toInt()
                                .coerceIn(50, 300)
                            if (WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) {
                                WebSettingsCompat.setAlgorithmicDarkeningAllowed(webView.settings, useDarkening)
                            }
                            val patched = patchEmailHtml(htmlContent)
                            if (patched != lastLoadedHtml) {
                                lastLoadedHtml = patched
                                try {
                                    webView.loadDataWithBaseURL(
                                        "file:///android_asset/",
                                        patched,
                                        "text/html",
                                        "UTF-8",
                                        null
                                    )
                                } catch (e: Exception) {
                                    android.util.Log.e("EmailWebView", "Failed to load email HTML content", e)
                                    if (callbackGeneration == renderGeneration) {
                                        loadError = "Could not render this email."
                                    }
                                }
                            }
                        }
                    )
                    if (!isLoaded && loadError == null) {
                        EmailBodyLoadingSkeleton(modifier = Modifier.fillMaxWidth().padding(12.dp))
                    }
                }
            }
            loadError?.let { message ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(message, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                    TextButton(onClick = {
                        loadError = null
                        isLoaded = false
                        lastLoadedHtml = ""
                        renderGeneration++
                    }) { Text("Retry") }
                }
            }
        }
    }

    pendingLink?.let { uri ->
        LinkPreviewDialog(
            uri = uri,
            onDismiss = { pendingLink = null },
            onOpen = {
                pendingLink = null
                openExternalLink(context, uri)
            }
        )
    }
}

@Composable
private fun ImageAttachmentCard(
    attachment: EmailAttachmentInfo,
    onFetchAttachment: suspend (String, String) -> ByteArray?,
    onNavigateToAttachmentViewer: (messageId: String, attachmentId: String, mimeType: String, name: String) -> Unit = { _, _, _, _ -> }
) {
    var decodedBitmap by remember(attachment.id) {
        mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null)
    }
    var isLoaded by remember(attachment.id) { mutableStateOf(false) }

    LaunchedEffect(attachment.id) {
        val bytes = try {
            onFetchAttachment(attachment.messageId, attachment.id)
        } catch (e: Exception) {
            android.util.Log.e("EmailDetailScreen", "Failed to fetch image attachment ${attachment.id}", e)
            null
        }
        decodedBitmap = if (bytes != null) {
            withContext(Dispatchers.Default) {
                val opts = android.graphics.BitmapFactory.Options().apply { inSampleSize = 2 }
                android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)?.asImageBitmap()
            }
        } else null
        isLoaded = true
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .clickable(enabled = decodedBitmap != null) {
                onNavigateToAttachmentViewer(attachment.messageId, attachment.id, attachment.mimeType, attachment.name)
            }
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            val bitmap = decodedBitmap
            when {
                bitmap != null -> {
                    androidx.compose.foundation.Image(
                        bitmap = bitmap,
                        contentDescription = attachment.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 280.dp),
                        contentScale = androidx.compose.ui.layout.ContentScale.FillWidth
                    )