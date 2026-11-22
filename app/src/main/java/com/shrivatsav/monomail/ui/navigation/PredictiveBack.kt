package com.shrivatsav.monomail.ui.navigation

import android.os.Build
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigationevent.NavigationEvent
import androidx.navigationevent.NavigationEventTransitionState
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import kotlin.math.roundToInt

/*
 * Edge-aware predictive back for full-screen "card" destinations.
 *
 * True 1:1 match with Google Android System & Files/Settings predictive back:
 *   - The underlying (background) screen sits recessed at small scale (0.92f) with 28dp
 *     rounded corners, cleanly scaling UP (92% -> 100%) as the user completes the gesture.
 *   - The foreground card shrinks (100% -> 90%), rounds corners (0dp -> 28dp), elevates
 *     with diffused drop shadow, and slides smoothly with touch gesture (following finger
 *     towards the swipe direction, exiting completely on commit).
 * Plain tweens allow Navigation Compose's SeekableTransitionState to map touch seek progress
 * 1:1 without nonlinear spring bounce during finger dragging.
 */
private const val BackMs = 320
private const val FadeStartMs = 260          // card stays physically opaque until ~80% through gesture
private const val ExitScale = 0.90f
private const val RevealStartScale = 0.92f   // background page starts small at 92% and scales UP
private const val RevealDim = 0.06f          // very light subtle tint so background stays crisp
private val CardRadius = 28.dp
private val CardElevation = 12.dp
private val BackEasing = LinearOutSlowInEasing

/** Edge the current/last back gesture started from. Read lazily by the slide lambda each frame. */
internal object BackSwipe {
    var edge by mutableIntStateOf(NavigationEvent.EDGE_LEFT)
    var isBackGestureActive by androidx.compose.runtime.mutableStateOf(false)
}

/** Call once, above the NavHost. Mirrors the gesture's swipe edge into [BackSwipe.edge]. */
@Composable
internal fun TrackBackSwipeEdge() {
    val owner = LocalNavigationEventDispatcherOwner.current ?: return
    LaunchedEffect(owner) {
        owner.navigationEventDispatcher.transitionState.collect { state ->
            if (state is NavigationEventTransitionState.InProgress) {
                BackSwipe.edge = state.latestEvent.swipeEdge
                BackSwipe.isBackGestureActive = true
            } else {
                BackSwipe.isBackGestureActive = false
            }
        }
    }
}

/**
 * Card leaving: shrinks to 90%, tracks touch displacement with horizontal slide in swipe direction,
 * and slides off screen on commit just like Google Files and Android System settings.
 */
internal fun backCardExit(density: Float = 1f): ExitTransition {
    return slideOutHorizontally(
        animationSpec = tween(BackMs, easing = BackEasing),
        targetOffsetX = { fullWidth ->
            if (BackSwipe.edge == NavigationEvent.EDGE_RIGHT) -fullWidth else fullWidth
        }
    ) +
        scaleOut(
            animationSpec = tween(BackMs, easing = BackEasing),
            targetScale = ExitScale,
            transformOrigin = TransformOrigin(0.5f, 0.5f)
        ) +
        fadeOut(tween(BackMs - FadeStartMs, delayMillis = FadeStartMs, easing = LinearEasing))
}

/**
 * Bottom-sheet style leaving (e.g. Compose page): slides downwards out of screen with subtle scale & fade.
 */
internal fun backSheetExit(): ExitTransition {
    return androidx.compose.animation.slideOutVertically(
        animationSpec = tween(BackMs, easing = BackEasing),
        targetOffsetY = { fullHeight -> fullHeight }
    ) +
        scaleOut(
            animationSpec = tween(BackMs, easing = BackEasing),
            targetScale = ExitScale,
            transformOrigin = TransformOrigin(0.5f, 0.5f)
        ) +
        fadeOut(tween(BackMs - FadeStartMs, delayMillis = FadeStartMs, easing = LinearEasing))
}

/** Screen underneath: settles from small recessed depth (92%) up to 100% as the card leaves. */
internal fun backRevealEnter(): EnterTransition =
    scaleIn(
        animationSpec = tween(BackMs, easing = BackEasing),
        initialScale = RevealStartScale,
        transformOrigin = TransformOrigin(0.5f, 0.5f)
    ) +
        fadeIn(tween(BackMs, easing = BackEasing), initialAlpha = 0.95f)

/**
 * Darkens the screen being revealed, lightening as the gesture progresses. Only draws when this
 * entry is being revealed by a pop/back navigation, so a forward push never dims the incoming screen.
 */
@Composable
internal fun AnimatedContentScope.revealDimModifier(navController: NavController): Modifier {
    val entry = LocalLifecycleOwner.current as? NavBackStackEntry
    val current by navController.currentBackStackEntryAsState()
    val isRevealing = BackSwipe.isBackGestureActive || (transition.currentState == EnterExitState.PreEnter && transition.targetState == EnterExitState.Visible && entry != null && current?.id == entry.id)
    val dim by transition.animateFloat(
        transitionSpec = { tween(BackMs, easing = BackEasing) },
        label = "backRevealDim",
    ) { if (it == EnterExitState.PreEnter && isRevealing) RevealDim else 0f }
    return Modifier.drawWithContent {
        drawContent()
        val alpha = dim
        if (alpha > 0f && isRevealing && entry != null && current?.id != entry.id) {
            drawRect(Color.Black.copy(alpha = alpha))
        }
    }
}

/**
 * Rounded, elevated surface that is square and shadowless at rest, and dynamically animates
 * corners (0dp -> 28dp) and elevation (0dp -> 12dp) strictly while navigating or seeking back.
 */
@Composable
internal fun AnimatedContentScope.BackCardSurface(
    navController: NavController,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        content = content
    )
}
