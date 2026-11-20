package com.shrivatsav.monomail.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AnimatedListItem(
    index: Int,
    itemKey: String = "",
    animatedItemsTracker: MutableSet<String>? = null,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    if (itemKey.isNotEmpty() && animatedItemsTracker?.contains(itemKey) == true) {
        Box(modifier = modifier) { content() }
        return
    }

    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        val delayTime = (index * 30).coerceAtMost(300).toLong()
        delay(delayTime)
        isVisible = true
        if (itemKey.isNotEmpty()) {
            animatedItemsTracker?.add(itemKey)
        }
    }
    
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = MaterialTheme.motionScheme.fastEffectsSpec(),
        label = "listItemAlpha"
    )
    
    Box(
        modifier = modifier
            .alpha(alpha)
    ) {
        content()
    }
}
