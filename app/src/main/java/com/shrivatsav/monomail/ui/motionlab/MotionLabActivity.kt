@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class
)

package com.shrivatsav.monomail.ui.motionlab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Forward
import androidx.compose.material.icons.automirrored.rounded.Reply
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.shrivatsav.monomail.ui.theme.MonoMailTheme
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Dedicated Interactive Motion & Animation Lab for trying, tweaking, and approving animations.
 */
class MotionLabActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            MonoMailTheme {
                MotionLabMainScreen()
            }
        }
    }
}

enum class MorphingOption {
    OPTION_A_CROSSFADE,
    OPTION_B_ELEVATION_GLIDE,
    OPTION_C_ELASTIC_SPLIT
}

enum class SimulatedScreen {
    HOME,
    THREAD_DETAIL,
    SETTINGS
}

@Composable
fun MotionLabMainScreen() {
    var selectedOption by remember { mutableStateOf(MorphingOption.OPTION_A_CROSSFADE) }
    var activeScreen by remember { mutableStateOf(SimulatedScreen.HOME) }
    var showAddAccountDialog by remember { mutableStateOf(false) }
    var showComposeModal by remember { mutableStateOf(false) }
    var isSearchExpanded by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = 0) { 3 }
    var continuousPos by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage + pagerState.currentPageOffsetFraction }
            .collect { continuousPos = it }
    }

    val settingsListState = rememberLazyListState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.statusBars
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Lab Header & Option Selector
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                "Monomail Motion Lab",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Try animations live & approve",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Screen Simulator Pills
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            SimulatedScreen.entries.forEach { screen ->
                                val isSelected = activeScreen == screen
                                Surface(
                                    shape = CircleShape,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                                    modifier = Modifier.clickable { activeScreen = screen }
                                ) {
                                    Text(
                                        text = when (screen) {
                                            SimulatedScreen.HOME -> "Home"
                                            SimulatedScreen.THREAD_DETAIL -> "Detail"
                                            SimulatedScreen.SETTINGS -> "Settings"
                                        },
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Morphing Style Chips
                    Text(
                        "Morphing Animation Style:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = selectedOption == MorphingOption.OPTION_A_CROSSFADE,
                            onClick = { selectedOption = MorphingOption.OPTION_A_CROSSFADE },
                            label = { Text("A: Crossfade", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = selectedOption == MorphingOption.OPTION_B_ELEVATION_GLIDE,
                            onClick = { selectedOption = MorphingOption.OPTION_B_ELEVATION_GLIDE },
                            label = { Text("B: Glide", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = selectedOption == MorphingOption.OPTION_C_ELASTIC_SPLIT,
                            onClick = { selectedOption = MorphingOption.OPTION_C_ELASTIC_SPLIT },
                            label = { Text("C: Elastic", fontSize = 11.sp) }
                        )
                    }
                }
            }

            // Main Interactive Simulator Viewport
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (activeScreen) {
                    SimulatedScreen.HOME -> {
                        HomeInteractiveDemo(
                            pagerState = pagerState,
                            onOpenAddAccount = { showAddAccountDialog = true },
                            isSearchExpanded = isSearchExpanded,
                            onSearchExpandedChange = { isSearchExpanded = it }
                        )
                    }
                    SimulatedScreen.THREAD_DETAIL -> {
                        ThreadDetailInteractiveDemo(
                            onBackToHome = { activeScreen = SimulatedScreen.HOME }
                        )
                    }
                    SimulatedScreen.SETTINGS -> {
                        SettingsInteractiveDemo(
                            listState = settingsListState,
                            onBackToHome = { activeScreen = SimulatedScreen.HOME }
                        )
                    }
                }

                // Floating Bottom Dock (Driven by the Selected Morphing Option)
                if (!isSearchExpanded) {
                    InteractiveMorphingDock(
                        activeScreen = activeScreen,
                        morphingOption = selectedOption,
                        continuousPosition = continuousPos,
                        onTabClick = { targetIndex ->
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(targetIndex)
                            }
                        },
                        isSettingsScrolling = settingsListState.isScrollInProgress,
                        onComposeClick = { showComposeModal = true },
                        onScreenChange = { activeScreen = it },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .navigationBarsPadding()
                            .imePadding()
                            .padding(bottom = 20.dp)
                    )
                }
            }
        }
    }

    // Modal: Centered Add Account Dialog (Demonstrating Point 5 fix)
    if (showAddAccountDialog) {
        Dialog(onDismissRequest = { showAddAccountDialog = false }) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 16.dp,
                modifier = Modifier.fillMaxWidth(0.92f)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Add Account", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text("Choose your provider", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = { showAddAccountDialog = false },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = CircleShape
                    ) {
                        Text("Sign in with Google")
                    }
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = { showAddAccountDialog = false },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                    ) {
                        Text("Sign in with Microsoft")
                    }
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = { showAddAccountDialog = false },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh, contentColor = MaterialTheme.colorScheme.onSurface)
                    ) {
                        Text("Other (IMAP/SMTP)")
                    }
                }
            }
        }
    }

    // Modal: Compose Modal (Demonstrating Point 6 Slide Up & Down Exit)
    AnimatedVisibility(
        visible = showComposeModal,
        enter = slideInVertically(spring(stiffness = Spring.StiffnessMediumLow)) { it } + fadeIn(),
        exit = slideOutVertically(spring(stiffness = Spring.StiffnessMediumLow)) { it } + fadeOut()
    ) {
        BackHandler { showComposeModal = false }
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(onClick = { showComposeModal = false }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Close")
                    }
                    Text("Compose", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Button(onClick = { showComposeModal = false }, shape = CircleShape) {
                        Text("Send")
                    }
                }
                Spacer(Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Text(
                        "Demonstrating Point 6:\nPage slides UP from bottom on enter, and exits DOWN to bottom on close/back gesture.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

/**
 * Interactive Morphing Dock demonstrating Options A, B, and C.
 */
@Composable
fun InteractiveMorphingDock(
    activeScreen: SimulatedScreen,
    morphingOption: MorphingOption,
    continuousPosition: Float,
    onTabClick: (Int) -> Unit,
    isSettingsScrolling: Boolean,
    onComposeClick: () -> Unit,
    onScreenChange: (SimulatedScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    when (morphingOption) {
        MorphingOption.OPTION_A_CROSSFADE -> {
            // OPTION A: Calm Shared Crossfade (Zero erratic resizing, subtle in-place crossfade)
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Pill
                Surface(
                    shape = CircleShape,
                    color = if (activeScreen == SimulatedScreen.THREAD_DETAIL) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                    shadowElevation = 6.dp
                ) {
                    AnimatedContent(
                        targetState = activeScreen,
                        transitionSpec = {
                            fadeIn(tween(140, delayMillis = 30)).togetherWith(fadeOut(tween(80)))
                        },
                        label = "dockLeftContent"
                    ) { screen ->
                        when (screen) {
                            SimulatedScreen.HOME -> DynamicSwipeTabPill(
                                continuousPosition = continuousPosition,
                                onTabClick = onTabClick
                            )
                            SimulatedScreen.THREAD_DETAIL -> MockReplyPill(onClick = {})
                            SimulatedScreen.SETTINGS -> MockCircleBack(
                                isScrolling = isSettingsScrolling,
                                onClick = { onScreenChange(SimulatedScreen.HOME) }
                            )
                        }
                    }
                }

                Spacer(Modifier.width(12.dp))

                // Right Pill
                Surface(
                    shape = CircleShape,
                    color = if (activeScreen == SimulatedScreen.HOME) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                    shadowElevation = 6.dp
                ) {
                    AnimatedContent(
                        targetState = activeScreen,
                        transitionSpec = {
                            fadeIn(tween(140, delayMillis = 30)).togetherWith(fadeOut(tween(80)))
                        },
                        label = "dockRightContent"
                    ) { screen ->
                        when (screen) {
                            SimulatedScreen.HOME -> MockComposeFab(onClick = onComposeClick)
                            SimulatedScreen.THREAD_DETAIL -> MockForwardPill(onClick = {})
                            SimulatedScreen.SETTINGS -> MockSettingsTitlePill(
                                title = "Settings",
                                isScrolling = isSettingsScrolling
                            )
                        }
                    }
                }
            }
        }

        MorphingOption.OPTION_B_ELEVATION_GLIDE -> {
            // OPTION B: Staggered Elevation Glide (Drops 12dp & dissolves, new buttons rise 12dp into place)
            AnimatedContent(
                targetState = activeScreen,
                transitionSpec = {
                    (fadeIn(tween(160)) + slideInVertically(spring(stiffness = Spring.StiffnessMedium)) { 36 })
                        .togetherWith(fadeOut(tween(90)) + slideOutVertically(spring(stiffness = Spring.StiffnessMedium)) { 36 })
                },
                modifier = modifier.fillMaxWidth(),
                label = "optionBGlide"
            ) { screen ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    when (screen) {
                        SimulatedScreen.HOME -> {
                            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceContainerHigh, shadowElevation = 6.dp) {
                                DynamicSwipeTabPill(
                                    continuousPosition = continuousPosition,
                                    onTabClick = onTabClick
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, shadowElevation = 6.dp) {
                                MockComposeFab(onClick = onComposeClick)
                            }
                        }
                        SimulatedScreen.THREAD_DETAIL -> {
                            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, shadowElevation = 6.dp) {
                                MockReplyPill(onClick = {})
                            }
                            Spacer(Modifier.width(12.dp))
                            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceContainerHigh, shadowElevation = 6.dp) {
                                MockForwardPill(onClick = {})
                            }
                        }
                        SimulatedScreen.SETTINGS -> {
                            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceContainerHigh, shadowElevation = 6.dp) {
                                MockCircleBack(
                                    isScrolling = isSettingsScrolling,
                                    onClick = { onScreenChange(SimulatedScreen.HOME) }
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceContainerHigh, shadowElevation = 6.dp) {
                                MockSettingsTitlePill(
                                    title = "Settings",
                                    isScrolling = isSettingsScrolling
                                )
                            }
                        }
                    }
                }
            }
        }

        MorphingOption.OPTION_C_ELASTIC_SPLIT -> {
            // OPTION C: Elastic Pill Morph (Linear width interpolation with spring dynamics)
            Row(
                modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (activeScreen == SimulatedScreen.THREAD_DETAIL) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                    shadowElevation = 6.dp,
                    modifier = Modifier.animateContentSize(spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioNoBouncy))
                ) {
                    when (activeScreen) {
                        SimulatedScreen.HOME -> DynamicSwipeTabPill(
                            continuousPosition = continuousPosition,
                            onTabClick = onTabClick
                        )
                        SimulatedScreen.THREAD_DETAIL -> MockReplyPill(onClick = {})
                        SimulatedScreen.SETTINGS -> MockCircleBack(
                            isScrolling = isSettingsScrolling,
                            onClick = { onScreenChange(SimulatedScreen.HOME) }
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Surface(
                    shape = CircleShape,
                    color = if (activeScreen == SimulatedScreen.HOME) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                    shadowElevation = 6.dp,
                    modifier = Modifier.animateContentSize(spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioNoBouncy))
                ) {
                    when (activeScreen) {
                        SimulatedScreen.HOME -> MockComposeFab(onClick = onComposeClick)
                        SimulatedScreen.THREAD_DETAIL -> MockForwardPill(onClick = {})
                        SimulatedScreen.SETTINGS -> MockSettingsTitlePill(
                            title = "Settings",
                            isScrolling = isSettingsScrolling
                        )
                    }
                }
            }
        }
    }
}

/**
 * Real 1:1 Swipe-Synchronized Dynamic Tab Pill (Point 7).
 * At rest: Selected tab = [Icon + Label], unselected tabs = [Icon only].
 * During swipe: Label collapses, transitioning through compact 3-icon state [Icon] [Icon] [Icon],
 * and target tab's label smoothly expands on arrival.
 */
@Composable
fun DynamicSwipeTabPill(
    continuousPosition: Float,
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

    Row(
        modifier = Modifier
            .height(52.dp)
            .animateContentSize(spring(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioNoBouncy))
            .padding(horizontal = 6.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        tabs.forEach { tab ->
            val dist = abs(clampedPos - tab.index)
            // Label expands when distance < 0.45; collapses to 0 by distance >= 0.5 (forming 3-icon state)
            val labelExpansion = (1f - dist * 2.2f).coerceIn(0f, 1f)
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
                    .height(40.dp)
                    .clip(CircleShape)
                    .background(containerColor)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true)
                    ) { onTabClick(tab.index) },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = (10 + (6 * labelExpansion)).dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.label,
                        tint = contentColor,
                        modifier = Modifier.size((18 + (2 * labelExpansion)).dp)
                    )

                    if (labelExpansion > 0.05f) {
                        Spacer(Modifier.width((6 * labelExpansion).dp))
                        Text(
                            text = tab.label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = contentColor,
                            modifier = Modifier.graphicsLayer {
                                alpha = labelExpansion
                                scaleX = 0.8f + (0.2f * labelExpansion)
                                scaleY = 0.8f + (0.2f * labelExpansion)
                            },
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

/**
 * Home Interactive Demo with:
 * 1. 1:1 Swipe-Synchronized Tab Bar.
 * 2. Signature Avatar Swipe Gesture to switch accounts.
 * 3. Search Bar expand / predictive collapse.
 * 4. Email list with native selection (no blur).
 */
@Composable
fun HomeInteractiveDemo(
    pagerState: PagerState,
    onOpenAddAccount: () -> Unit,
    isSearchExpanded: Boolean,
    onSearchExpandedChange: (Boolean) -> Unit
) {
    // Avatar swipe account switcher state
    val accounts = remember { listOf("sumit Kumar (Google)", "Work Mail (Exchange)", "Personal (IMAP)") }
    var currentAccountIndex by remember { mutableIntStateOf(0) }
    var avatarFlipAngle by remember { mutableFloatStateOf(0f) }

    val animatedFlip by animateFloatAsState(
        targetValue = avatarFlipAngle,
        animationSpec = spring(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioNoBouncy),
        label = "avatarFlip"
    )

    Column(modifier = Modifier.fillMaxSize()) {
        // Search Bar with Avatar Vertical Drag Listener (Point 11)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = if (isSearchExpanded) 0.dp else 16.dp, vertical = 8.dp)
        ) {
            SearchBar(
                inputField = {
                    SearchBarDefaults.InputField(
                        query = "",
                        onQueryChange = {},
                        onSearch = {},
                        expanded = isSearchExpanded,
                        onExpandedChange = onSearchExpandedChange,
                        placeholder = { Text("Search in mail (${accounts[currentAccountIndex]})", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        leadingIcon = {
                            if (isSearchExpanded) {
                                IconButton(onClick = { onSearchExpandedChange(false) }) {
                                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                                }
                            } else {
                                Icon(Icons.Rounded.Search, contentDescription = "Search")
                            }
                        },
                        trailingIcon = {
                            if (!isSearchExpanded) {
                                // Avatar circle with Vertical Drag gesture (Point 11)
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .graphicsLayer { rotationX = animatedFlip }
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary)
                                        .pointerInput(Unit) {
                                            detectVerticalDragGestures { _, dragAmount ->
                                                if (dragAmount > 8f) {
                                                    // Swipe Down -> Next account
                                                    currentAccountIndex = (currentAccountIndex + 1) % accounts.size
                                                    avatarFlipAngle += 360f
                                                } else if (dragAmount < -8f) {
                                                    // Swipe Up -> Previous account
                                                    currentAccountIndex = (currentAccountIndex - 1 + accounts.size) % accounts.size
                                                    avatarFlipAngle -= 360f
                                                }
                                            }
                                        }
                                        .clickable { onOpenAddAccount() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = accounts[currentAccountIndex].first().toString(),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }
                            }
                        }
                    )
                },
                expanded = isSearchExpanded,
                onExpandedChange = onSearchExpandedChange,
                modifier = Modifier.fillMaxWidth(),
                shape = if (isSearchExpanded) RoundedCornerShape(0.dp) else CircleShape,
                windowInsets = SearchBarDefaults.windowInsets
            ) {
                // Expanded search view content
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Search Suggestions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("Tap back arrow or swipe back from edge to collapse into search bar pill.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // Swipe Hint Banner
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Rounded.SwapVert, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    "Swipe UP or DOWN on profile avatar to cycle accounts like Gmail! Swipe list to test 1:1 tab morph.",
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

        // Real HorizontalPager demonstrating 1:1 Swipe Morphing (Point 7)
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        when (page) {
                            0 -> "Inbox (Swipe horizontally below to test 1:1 tab morph)"
                            1 -> "Sent Mail"
                            else -> "Archived Messages"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                items(8) { idx ->
                    InteractiveEmailCard(
                        title = when (page) {
                            0 -> "Security Alert - Sign-in from Pixel 8a #$idx"
                            1 -> "Reply: Quarterly Roadmap Review #$idx"
                            else -> "Archived Notification #$idx"
                        },
                        sender = "Google Account Security",
                        date = "Nov ${18 - idx}"
                    )
                }
            }
        }
    }
}

/**
 * Interactive Email Card with contextual selection (Point 12: Zero Blur).
 */
@Composable
fun InteractiveEmailCard(title: String, sender: String, date: String) {
    var isSelected by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isSelected = !isSelected },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(Icons.Rounded.Check, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp))
                } else {
                    Icon(Icons.Rounded.AccountCircle, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(sender, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Text(date, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(title, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
fun ThreadDetailInteractiveDemo(onBackToHome: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onBackToHome) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
            }
            Text("Email Thread Detail", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Subject: Security Activity Report", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("From: no-reply@accounts.google.com", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(12.dp))
                Text(
                    "Notice the bottom controls below:\nTwo independent floating pills (Reply + Forward) with matching 12dp spacing and uniform architecture, without an enclosing grey container bar.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

/**
 * Settings Interactive Demo with Scroll Collapse (Point 8 & Point 9: Uniform Height).
 */
@Composable
fun SettingsInteractiveDemo(
    listState: LazyListState,
    onBackToHome: () -> Unit
) {
    val settingsCategories = listOf(
        "Accounts & Cloud" to "Manage accounts, providers, connection status",
        "Appearance" to "Theme, typography, and list presentation",
        "Inbox" to "Threading, grouping, list density, swipe actions",
        "Reading" to "Inline attachments, markdown renderer",
        "Navigation" to "Dock tabs and mail folders",
        "Compose" to "Reply defaults, confirm send, undo send",
        "Privacy & Security" to "PGP encryption keys, remote images",
        "Notifications" to "Alerts, sync schedules, quiet hours",
        "About & System" to "Version, open source licenses, diagnostics"
    )

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text("Settings", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text("All items below have uniform 68dp height (Point 9)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
            }
            items(settingsCategories.size) { idx ->
                val (title, subtitle) = settingsCategories[idx]
                // Point 9: Uniform Height Item
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(68.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    ListItem(
                        headlineContent = { Text(title, fontWeight = FontWeight.SemiBold, maxLines = 1) },
                        supportingContent = { Text(subtitle, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            }
        }
    }
}

// ── Mock Floating Dock Components (52dp Height) ──────────────────────────

@Composable
fun MockComposeFab(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Rounded.Edit, contentDescription = "Compose", tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(22.dp))
    }
}

@Composable
fun MockReplyPill(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .height(52.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 22.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(Icons.AutoMirrored.Rounded.Reply, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
        Spacer(Modifier.width(8.dp))
        Text("Reply", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}

@Composable
fun MockForwardPill(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .height(52.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 22.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(Icons.AutoMirrored.Rounded.Forward, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(8.dp))
        Text("Forward", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun MockCircleBack(
    isScrolling: Boolean,
    onClick: () -> Unit
) {
    val collapseFraction by animateFloatAsState(
        targetValue = if (isScrolling) 0.88f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "backCollapse"
    )

    Box(
        modifier = Modifier
            .size((52 * collapseFraction).dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", modifier = Modifier.size(22.dp))
    }
}

@Composable
fun MockSettingsTitlePill(
    title: String,
    isScrolling: Boolean
) {
    val collapsePadding by animateDpAsState(
        targetValue = if (isScrolling) 16.dp else 26.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "titleCollapse"
    )

    Box(
        modifier = Modifier
            .height(52.dp)
            .padding(horizontal = collapsePadding),
        contentAlignment = Alignment.Center
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}
