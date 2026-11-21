package com.shrivatsav.monomail.ui.navigation

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.layout
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.roundToPx
import com.shrivatsav.monomail.ui.components.AppNavigationState
import com.shrivatsav.monomail.ui.components.AppNavigationTab
import com.shrivatsav.monomail.ui.theme.MonoMailTheme
import com.shrivatsav.monomail.ui.theme.ThemePreviews
import kotlin.math.abs
import kotlin.math.roundToInt

private val ExpandedDockHeight = 80.dp
private val CompactDockHeight = 64.dp

@Composable
internal fun MorphingInboxNavigationBar(
    selectedIndex: Int,
    tabs: List<AppNavigationTab>,
    pagerPosition: () -> Float,
    dockCollapsed: State<Boolean>,
    onTabSelected: (Int) -> Unit,
    onCompose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val compact = dockCollapsed.value
    val barHeight = animateDpAsState(
        targetValue = if (compact) CompactDockHeight else ExpandedDockHeight,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "inboxDockHeight",
    )
    val indicatorHeight = animateDpAsState(
        targetValue = if (compact) 48.dp else 56.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "inboxDockIndicatorHeight",
    )

    Row(
        modifier = modifier
            .widthIn(max = 600.dp)
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier
                .weight(1f)
                .layout { measurable, constraints ->
                    val height = barHeight.value.roundToPx().coerceIn(0, constraints.maxHeight)
                    val placeable = measurable.measure(
                        constraints.copy(minHeight = height, maxHeight = height)
                    )
                    layout(placeable.width, height) { placeable.placeRelative(0, 0) }
                },
            color = MaterialTheme.colorScheme.surfaceContainer,
            shape = CircleShape,
            tonalElevation = 0.dp,
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .selectableGroup(),
            ) {
                val itemWidth = maxWidth / tabs.size.coerceAtLeast(1)
                val itemWidthPx = with(LocalDensity.current) { itemWidth.toPx() }
                val expandedIndicatorWidth = (itemWidth - 8.dp).coerceAtLeast(48.dp).coerceAtMost(itemWidth)
                val indicatorWidth = animateDpAsState(
                    targetValue = if (compact) 48.dp else expandedIndicatorWidth,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "inboxDockIndicatorWidth",
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .offset {
                            val position = pagerPosition().coerceIn(0f, (tabs.size - 1).coerceAtLeast(0).toFloat())
                            val widthPx = indicatorWidth.value.roundToPx()
                            IntOffset(
                                x = (position * itemWidthPx + (itemWidthPx - widthPx) / 2f).roundToInt(),
                                y = 0,
                            )
                        }
                        .layout { measurable, constraints ->
                            val width = indicatorWidth.value.roundToPx().coerceIn(0, constraints.maxWidth)
                            val height = indicatorHeight.value.roundToPx().coerceIn(0, constraints.maxHeight)
                            val placeable = measurable.measure(Constraints.fixed(width, height))
                            layout(width, height) { placeable.placeRelative(0, 0) }
                        }
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                )

                Row(Modifier.fillMaxSize()) {
                    tabs.forEachIndexed { index, tab ->
                        MorphingNavigationItem(
                            index = index,
                            tab = tab,
                            selected = selectedIndex == index,
                            compact = dockCollapsed,
                            pagerPosition = pagerPosition,
                            onClick = { onTabSelected(index) },
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                        )
                    }
                }
            }
        }
        Spacer(Modifier.width(12.dp))
        FloatingActionButton(
            onClick = onCompose,
            modifier = Modifier.padding(vertical = 4.dp),
        ) {
            Icon(Icons.Rounded.Edit, contentDescription = "Compose")
        }
    }
}

@Composable
private fun MorphingNavigationItem(
    index: Int,
    tab: AppNavigationTab,
    selected: Boolean,
    compact: State<Boolean>,
    pagerPosition: () -> Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.selectable(
            selected = selected,
            onClick = onClick,
            role = Role.Tab,
        ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = tab.icon,
            contentDescription = null,
            tint = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .align(Alignment.Center)
                .graphicsLayer {
                    val proximity = (1f - abs(pagerPosition() - index)).coerceIn(0f, 1f)
                    val compactNow = compact.value
                    translationY = if (compactNow) 0f else 8.dp.toPx() * (1f - proximity)
                    val scale = 0.94f + (0.06f * proximity)
                    scaleX = scale
                    scaleY = scale
                },
        )
        Text(
            text = tab.label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 4.dp, vertical = 7.dp)
                .graphicsLayer {
                    val proximity = (1f - abs(pagerPosition() - index)).coerceIn(0f, 1f)
                    alpha = if (compact.value) 0f else proximity
                    val scale = 0.94f + (0.06f * proximity)
                    scaleX = scale
                    scaleY = scale
                },
        )
    }
}

@ThemePreviews
@Composable
private fun MorphingInboxNavigationBarPreview() {
    val navigationState = remember { AppNavigationState() }
    MonoMailTheme {
        MorphingInboxNavigationBar(
            selectedIndex = 0,
            tabs = navigationState.navigationTabs,
            pagerPosition = { 0f },
            dockCollapsed = navigationState.dockCollapsed,
            onTabSelected = {},
            onCompose = {},
        )
    }
}
