package com.shrivatsav.monomail.ui.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideOut
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.IntOffset
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
 * Numbers follow Android's published predictive-back spec for full-screen surfaces:
 *   exit scale 100% -> 90%, max x-shift = (width / 20) - 8dp, entering screen 110% -> 100%.
 * Everything is a plain tween so Navigation's seekable transition maps gesture progress
 * to motion predictably, and the commit animation continues from wherever the finger left off.
 * Tune the constants below against the Android 17 Settings app side by side.
 */
private const val BackMs = 300
private const val FadeStartMs = 180          // card stays opaque until ~60% of the timeline
private const val ExitScale = 0.90f
private const val RevealStartScale = 1.10f   // screen underneath settles 110% -> 100%
private const val RevealDim = 0.32f          // scrim over the screen underneath, fades as you commit
private val CardRadius = 28.dp
private val CardElevation = 8.dp
private val BackEasing = LinearOutSlowInEasing

/** Edge the current/last back gesture started from. Read lazily by the slide lambda each frame. */
internal object BackSwipe {
    var edge by mutableIntStateOf(NavigationEvent.EDGE_LEFT)
}

/** Call once, above the NavHost. Mirrors the gesture's swipe edge into [BackSwipe.edge]. */
@Composable
internal fun TrackBackSwipeEdge() {
    val owner = LocalNavigationEventDispatcherOwner.current ?: return
    LaunchedEffect(owner) {
        owner.navigationEventDispatcher.transitionState.collect { state ->
            if (state is NavigationEventTransitionState.InProgress) {
                BackSwipe.edge = state.latestEvent.swipeEdge
            }
        }
    }
}

/**
 * Card leaving: shrinks to 90%, drifts away from the finger's edge (swipe from the right edge
 * moves it left, from the left edge moves it right), then fades out late in the timeline.
 */
internal fun backCardExit(density: Float): ExitTransition =
    slideOut(tween(BackMs, easing = BackEasing)) { size ->
        val shift = (size.width / 20f - 8f * density).coerceAtLeast(0f)
        // Read inside the lambda: re-evaluated while animating, so no stale edge.
        val dir = if (BackSwipe.edge == NavigationEvent.EDGE_RIGHT) -1f else 1f
        IntOffset((dir * shift).roundToInt(), 0)
    } +
        scaleOut(tween(BackMs, easing = BackEasing), targetScale = ExitScale) +
        fadeOut(tween(BackMs - FadeStartMs, delayMillis = FadeStartMs, easing = LinearEasing))

/** Screen underneath: settles from slightly zoomed-in to 100% as the card leaves. */
internal fun backRevealEnter(): EnterTransition =
    scaleIn(tween(BackMs, easing = BackEasing), initialScale = RevealStartScale)

/**
 * Darkens the screen being revealed, lightening as the gesture progresses. Only draws when this
 * entry is *not* the top of the stack, so a forward push never dims the incoming screen.
 */
@Composable
internal fun AnimatedContentScope.revealDimModifier(navController: NavController): Modifier {
    val entry = LocalLifecycleOwner.current as? NavBackStackEntry
    val current by navController.currentBackStackEntryAsState()
    val dim by transition.animateFloat(
        transitionSpec = { tween(BackMs, easing = BackEasing) },
        label = "backRevealDim",
    ) { if (it == EnterExitState.PreEnter) RevealDim else 0f }
    return Modifier.drawWithContent {
        drawContent()
        val alpha = dim
        if (alpha > 0f && entry != null && current?.id != entry.id) {
            drawRect(Color.Black.copy(alpha = alpha))
        }
    }
}

/**
 * Rounded, elevated surface that is square and shadowless at rest, and only rounds while the
 * destination is entering/exiting. Replaces the permanent shadow + 28dp clip on every screen.
 */
@Composable
internal fun AnimatedContentScope.BackCardSurface(
    navController: NavController,
    content: @Composable BoxScope.() -> Unit,
) {
    val radius by transition.animateDp(
        transitionSpec = { tween(BackMs, easing = BackEasing) },
        label = "backCardRadius",
    ) { if (it == EnterExitState.Visible) 0.dp else CardRadius }
    val reveal = revealDimModifier(navController)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                val r = radius
                shape = RoundedCornerShape(r)
                clip = r > 0.dp
                shadowElevation = if (r > 0.dp) CardElevation.toPx() else 0f
            }
            .background(MaterialTheme.colorScheme.background)
            .then(reveal),
        content = content,
    )
}
