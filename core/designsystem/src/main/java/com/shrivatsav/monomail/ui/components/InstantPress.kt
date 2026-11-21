package com.shrivatsav.monomail.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.material3.ripple
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import kotlinx.coroutines.launch

/**
 * Immediate-action variant for navigation tabs and other reversible selection controls.
 * The action runs on pointer-down; use clickableOnRelease for destructive/irreversible actions.
 */
fun Modifier.clickableOnPress(
    enabled: Boolean = true,
    haptic: Boolean = true,
    onPressDown: () -> Unit,
): Modifier = pressFeedback(
    enabled = enabled,
    haptic = haptic,
    fireOnDown = true,
    action = onPressDown
)

/** Press feedback starts on contact, but the action only runs on a successful finger lift. */
fun Modifier.clickableOnRelease(
    enabled: Boolean = true,
    haptic: Boolean = true,
    onClick: () -> Unit,
): Modifier = pressFeedback(
    enabled = enabled,
    haptic = haptic,
    fireOnDown = false,
    action = onClick
)

private fun Modifier.pressFeedback(
    enabled: Boolean,
    haptic: Boolean,
    fireOnDown: Boolean,
    action: () -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val haptics = LocalHapticFeedback.current
    val pressScale = remember { Animatable(1f) }
    val animationScope = rememberCoroutineScope()

    this
        .graphicsLayer {
            scaleX = pressScale.value
            scaleY = pressScale.value
        }
        .semantics(mergeDescendants = true) {
            role = Role.Button
            if (enabled) onClick {
                action()
                true
            }
        }
        .indication(interactionSource, ripple(bounded = true))
        .pointerInput(enabled, fireOnDown, action) {
            if (!enabled) return@pointerInput
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                down.consume()
                val press = PressInteraction.Press(down.position)
                interactionSource.tryEmit(press)
                if (haptic) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                val pressAnimation = animationScope.launch {
                    pressScale.animateTo(
                        targetValue = 0.94f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
                    )
                }
                if (fireOnDown) action()
                val up = waitForUpOrCancellation()
                pressAnimation.cancel()
                if (up != null) {
                    interactionSource.tryEmit(PressInteraction.Release(press))
                    if (!fireOnDown) action()
                } else {
                    interactionSource.tryEmit(PressInteraction.Cancel(press))
                }
                animationScope.launch {
                    pressScale.animateTo(
                        targetValue = 1f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
                    )
                }
            }
        }
}
