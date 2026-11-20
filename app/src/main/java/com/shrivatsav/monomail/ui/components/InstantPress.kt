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
