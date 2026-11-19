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
 *   - The underlying (background) screen sits recessed at small scale (0.90f) with 28dp
 *     rounded corners and dark scrim, smoothly scaling UP (90% -> 100%) while un-rounding.
 *   - The foreground card shrinks (100% -> 90%), rounds corners (0dp -> 28dp), elevates
 *     with diffused drop shadow, and drifts slightly with touch (24dp) while remaining
 *     centered as a floating card preview.
 * Plain tweens allow Navigation Compose's SeekableTransitionState to map touch seek progress
 * 1:1 without nonlinear spring bounce during finger dragging.
 */
private const val BackMs = 320
private const val FadeStartMs = 260          // card stays physically opaque until ~80% through gesture
private const val ExitScale = 0.90f
private const val RevealStartScale = 0.90f   // background page starts small at 90% and scales UP
private const val RevealDim = 0.28f          // scrim over the screen underneath, fades as you commit
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
 * Card leaving: shrinks to 90%, tracks touch displacement with a subtle drift (24dp),
 * remaining centered as a floating card preview with all 4 rounded corners visible.
 */
internal fun backCardExit(density: Float): ExitTransition {
    val driftPx = (24f * density).roundToInt()
    return slideOutHorizontally(
        animationSpec = tween(BackMs, easing = BackEasing),
        targetOffsetX = { _ ->
            if (BackSwipe.edge == NavigationEvent.EDGE_RIGHT) -driftPx else driftPx
        }
    ) +
        scaleOut(
            animationSpec = tween(BackMs, easing = BackEasing),
            targetScale = ExitScale,
            transformOrigin = TransformOrigin(0.5f, 0.5f)
        ) +
        fadeOut(tween(BackMs - FadeStartMs, delayMillis = FadeStartMs, easing = LinearEasing))
}

/** Screen underneath: settles from small recessed depth (90%) up to 100% as the card leaves. */
internal fun backRevealEnter(): EnterTransition =
    scaleIn(
        animationSpec = tween(BackMs, easing = BackEasing),
        initialScale = RevealStartScale,
        transformOrigin = TransformOrigin(0.5f, 0.5f)
    ) +
        fadeIn(tween(BackMs, easing = BackEasing), initialAlpha = 0.85f)

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
 * Rounded, elevated surface that is square and shadowless at rest, and dynamically animates
 * corners (0dp -> 28dp) and elevation (0dp -> 8dp) strictly while navigating or seeking back.
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
    val elevation by transition.animateDp(
        transitionSpec = { tween(BackMs, easing = BackEasing) },
        label = "backCardElevation",
    ) { if (it == EnterExitState.Visible) 0.dp else CardElevation }
    val reveal = revealDimModifier(navController)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                val r = radius
                val elev = elevation
                shape = RoundedCornerShape(r)
                clip = r > 0.dp
                shadowElevation = elev.toPx()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ambientShadowColor = Color.Black.copy(alpha = 0.20f)
                    spotShadowColor = Color.Black.copy(alpha = 0.35f)
                }
            }
            .background(MaterialTheme.colorScheme.background)
            .then(reveal),
        content = content
    )
}
