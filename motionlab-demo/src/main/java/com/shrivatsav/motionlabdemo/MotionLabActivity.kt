@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class
)

package com.shrivatsav.motionlabdemo

import android.os.Bundle
import androidx.activity.BackEventCompat
import androidx.activity.ComponentActivity
import androidx.activity.compose.PredictiveBackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Forward
import androidx.compose.material.icons.automirrored.rounded.Reply
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.HourglassBottom
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.HourglassBottom
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Dedicated Interactive Motion & Animation Lab for testing, tuning, and approving animations.
 */
class MotionLabActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            val darkTheme = isSystemInDarkTheme()
            MaterialTheme(colorScheme = if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)) {
                MotionLabMainScreen()
            }
        }
    }
}

enum class MorphingOption {
    OPTION_A_CROSSFADE,
    OPTION_B_ELEVATION_GLIDE,
    OPTION_C_ELASTIC_SPLIT,
    OPTION_D_CLOCK_EXPRESSIVE
}

enum class SimulatedScreen {
    HOME,
    THREAD_DETAIL,
    SETTINGS
}

data class DemoAccount(
    val name: String,
    val email: String,
    val avatarLetter: String,
    val containerColor: Color,
    val onContainerColor: Color
)

/**
 * Directional scroll tracking:
 * - Scrolling DOWN into content -> collapses the dock.
 * - Stopping -> REMAINS collapsed (does NOT pop back!).
 * - Scrolling UP (in opposite direction) -> regains full expanded shape!
 */
@Composable
fun rememberDirectionalScrollConnection(
    thresholdPx: Float = 28f
): DirectionalScrollConnection {
    return remember(thresholdPx) {
        DirectionalScrollConnection(thresholdPx)
    }
}

class DirectionalScrollConnection(
    private val thresholdPx: Float
) : NestedScrollConnection {
    var isCollapsed by mutableStateOf(false)
        private set
    private var accumulatedScroll = 0f

    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        val dy = available.y
        accumulatedScroll += dy

        if (accumulatedScroll < -thresholdPx) {
            // Dragging up -> scrolling down into list -> collapse
            isCollapsed = true
            accumulatedScroll = 0f
        } else if (accumulatedScroll > thresholdPx) {
            // Dragging down -> scrolling up towards top (opposite direction) -> expand
            isCollapsed = false
            accumulatedScroll = 0f
        }
        return Offset.Zero
    }

    fun reset() {
        isCollapsed = false
        accumulatedScroll = 0f
    }
}

@Composable
fun MotionLabMainScreen() {
    var selectedOption by remember { mutableStateOf(MorphingOption.OPTION_A_CROSSFADE) }
    var activeScreen by remember { mutableStateOf(SimulatedScreen.HOME) }
    var clockSelectedIndex by remember { mutableIntStateOf(0) }
    var showAddAccountDialog by remember { mutableStateOf(false) }
    var showComposeModal by remember { mutableStateOf(false) }
    var isSearchExpanded by remember { mutableStateOf(false) }
    var showTweakPanel by remember { mutableStateOf(false) }

    // Live Crossfade Tuning Parameters (Point 1)
    var crossfadeDurationMs by remember { mutableIntStateOf(160) }
    var crossfadeDelayMs by remember { mutableIntStateOf(30) }
    var crossfadeScale by remember { mutableFloatStateOf(0.94f) }
    var dockHeightDp by remember { mutableIntStateOf(56) }
    var clockSpringStiffness by remember { mutableIntStateOf(560) }
    var clockSpringDamping by remember { mutableFloatStateOf(0.68f) }
    var clockSelectedScale by remember { mutableFloatStateOf(1.10f) }
    var clockPressedScale by remember { mutableFloatStateOf(0.90f) }

    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = 0) { 3 }
    var continuousPos by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage + pagerState.currentPageOffsetFraction }
            .collect { continuousPos = it }
    }

    // Directional scroll connections for Home and Settings (Point 2)
    val homeScrollConnection = rememberDirectionalScrollConnection()
    val settingsScrollConnection = rememberDirectionalScrollConnection()
    val settingsListState = rememberLazyListState()

    // Accounts for Google Apps avatar swipe demo (Point 3)
    val accounts = remember {
        listOf(
            DemoAccount("sumit Kumar", "skray9403@gmail.com", "S", Color(0xFFE8DEF8), Color(0xFF1D192B)),
            DemoAccount("Work Mail", "dev@monomail.io", "W", Color(0xFFD0E4FF), Color(0xFF001D36)),
            DemoAccount("Personal IMAP", "mehnaz@fastmail.com", "P", Color(0xFFFFD8E4), Color(0xFF31111D))
        )
    }
    var currentAccountIndex by remember { mutableIntStateOf(0) }
    var avatarSwipeDirection by remember { mutableIntStateOf(1) } // 1 for down, -1 for up
    var accountSwitchedToast by remember { mutableStateOf<String?>(null) }

    // Auto-dismiss account toast after 1.8s
    LaunchedEffect(accountSwitchedToast) {
        if (accountSwitchedToast != null) {
            delay(1800)
            accountSwitchedToast = null
        }
    }

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
                        .padding(horizontal = 16.dp, vertical = 10.dp)
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

                        // Screen Simulator Switcher
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            SimulatedScreen.entries.forEach { screen ->
                                val isSelected = activeScreen == screen
                                Surface(
                                    shape = CircleShape,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                                    modifier = Modifier.clickable {
                                        activeScreen = screen
                                        clockSelectedIndex = when (screen) {
                                            SimulatedScreen.HOME -> 0
                                            SimulatedScreen.THREAD_DETAIL -> 2
                                            SimulatedScreen.SETTINGS -> 4
                                        }
                                        homeScrollConnection.reset()
                                        settingsScrollConnection.reset()
                                    }
                                ) {
                                    Text(
                                        text = when (screen) {
                                            SimulatedScreen.HOME -> "Home"
                                            SimulatedScreen.THREAD_DETAIL -> "Detail"
                                            SimulatedScreen.SETTINGS -> "Settings"
                                        },
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Morphing Style Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f).horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
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
                            FilterChip(
                                selected = selectedOption == MorphingOption.OPTION_D_CLOCK_EXPRESSIVE,
                                onClick = { selectedOption = MorphingOption.OPTION_D_CLOCK_EXPRESSIVE },
                                label = { Text("D: Clock", fontSize = 11.sp) }
                            )
                        }

                        IconButton(
                            onClick = { showTweakPanel = !showTweakPanel },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                if (showTweakPanel) Icons.Rounded.ExpandLess else Icons.Rounded.Tune,
                                contentDescription = "Tweak settings",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Collapsible Tweak Panel for Point 1 (Crossfade parameters)
                    AnimatedVisibility(visible = showTweakPanel) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                .padding(12.dp)
                        ) {
                            if (selectedOption == MorphingOption.OPTION_D_CLOCK_EXPRESSIVE) {
                                Text(
                                    "Clock Navigation Motion (Live)",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.height(6.dp))
                                Text("Spring stiffness: $clockSpringStiffness", style = MaterialTheme.typography.labelSmall)
                                Slider(
                                    value = clockSpringStiffness.toFloat(),
                                    onValueChange = { clockSpringStiffness = it.roundToInt() },
                                    valueRange = 280f..900f,
                                    modifier = Modifier.height(28.dp)
                                )
                                Text("Damping: ${"%.2f".format(clockSpringDamping)}", style = MaterialTheme.typography.labelSmall)
                                Slider(
                                    value = clockSpringDamping,
                                    onValueChange = { clockSpringDamping = it },
                                    valueRange = 0.45f..1.0f,
                                    modifier = Modifier.height(28.dp)
                                )
                                Text("Selected icon scale: ${"%.2f".format(clockSelectedScale)}x", style = MaterialTheme.typography.labelSmall)
                                Slider(
                                    value = clockSelectedScale,
                                    onValueChange = { clockSelectedScale = it },
                                    valueRange = 1.0f..1.24f,
                                    modifier = Modifier.height(28.dp)
                                )
                                Text("Pressed scale: ${"%.2f".format(clockPressedScale)}x", style = MaterialTheme.typography.labelSmall)
                                Slider(
                                    value = clockPressedScale,
                                    onValueChange = { clockPressedScale = it },
                                    valueRange = 0.80f..0.98f,
                                    modifier = Modifier.height(28.dp)
                                )
                            } else {
                            Text(
                                "Tweak Crossfade Parameters (Live)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(6.dp))

                            // Duration Slider
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Duration: ${crossfadeDurationMs}ms", style = MaterialTheme.typography.labelSmall)
                                Text("Entry Delay: ${crossfadeDelayMs}ms", style = MaterialTheme.typography.labelSmall)
                            }
                            Slider(
                                value = crossfadeDurationMs.toFloat(),
                                onValueChange = { crossfadeDurationMs = it.roundToInt() },
                                valueRange = 100f..300f,
                                modifier = Modifier.height(28.dp)
                            )

                            // Scale Factor Slider
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Scale Pop: ${"%.2f".format(crossfadeScale)}x", style = MaterialTheme.typography.labelSmall)
                                Text("Dock Height: ${dockHeightDp}dp", style = MaterialTheme.typography.labelSmall)
                            }
                            Slider(
                                value = crossfadeScale,
                                onValueChange = { crossfadeScale = it },
                                valueRange = 0.85f..1.0f,
                                modifier = Modifier.height(28.dp)
                            )
                            }
                        }
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
                            scrollConnection = homeScrollConnection,
                            accounts = accounts,
                            currentAccountIndex = currentAccountIndex,
                            avatarSwipeDirection = avatarSwipeDirection,
                            onAvatarSwipe = { direction ->
                                avatarSwipeDirection = direction
                                currentAccountIndex = if (direction > 0) {
                                    (currentAccountIndex + 1) % accounts.size
                                } else {
                                    (currentAccountIndex - 1 + accounts.size) % accounts.size
                                }
                                accountSwitchedToast = "Switched to ${accounts[currentAccountIndex].name}"
                            },
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
                            scrollConnection = settingsScrollConnection,
                            onBackToHome = { activeScreen = SimulatedScreen.HOME }
                        )
                    }
                }

                // Floating Toast for Account Switch (Google apps style)
                androidx.compose.animation.AnimatedVisibility(
                    visible = accountSwitchedToast != null,
                    enter = fadeIn(tween(120)) + slideInVertically(spring(stiffness = Spring.StiffnessMedium)) { -40 },
                    exit = fadeOut(tween(120)) + slideOutVertically(spring(stiffness = Spring.StiffnessMedium)) { -40 },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 70.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.inverseSurface,
                        shadowElevation = 8.dp
                    ) {
                        Text(
                            text = accountSwitchedToast ?: "",
                            color = MaterialTheme.colorScheme.inverseOnSurface,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Floating Bottom Dock (Driven by Selected Morphing & Directional Scroll)
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
                        isHomeCollapsed = homeScrollConnection.isCollapsed,
                        isSettingsCollapsed = settingsScrollConnection.isCollapsed,
                        durationMs = crossfadeDurationMs,
                        delayMs = crossfadeDelayMs,
                        scaleFactor = crossfadeScale,
                        containerHeight = dockHeightDp.dp,
                        clockSelectedIndex = clockSelectedIndex,
                        onClockDestinationSelected = { index -> clockSelectedIndex = index },
                        clockSpringStiffness = clockSpringStiffness.toFloat(),
                        clockSpringDamping = clockSpringDamping,
                        clockSelectedScale = clockSelectedScale,
                        clockPressedScale = clockPressedScale,
                        onComposeClick = { showComposeModal = true },
                        onScreenChange = { activeScreen = it },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .navigationBarsPadding()
                            .imePadding()
                            .padding(bottom = if (selectedOption == MorphingOption.OPTION_D_CLOCK_EXPRESSIVE) 0.dp else 20.dp)
                    )
                }
            }
        }
    }

    // Modal: Centered Add Account Dialog (Point 5 fix)
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

    // Modal: Compose Modal with True Android 15 Predictive Back Gesture (Point 4)
    var predictiveProgress by remember { mutableFloatStateOf(0f) }
    var isPredictiveActive by remember { mutableStateOf(false) }

    PredictiveBackHandler(enabled = showComposeModal) { progressFlow: Flow<BackEventCompat> ->
        isPredictiveActive = true
        try {
            progressFlow.collect { backEvent ->
                predictiveProgress = backEvent.progress
            }
            // Gesture finished: close modal
            showComposeModal = false
        } catch (e: CancellationException) {
            // Gesture cancelled: springs back
        } finally {
            isPredictiveActive = false
            predictiveProgress = 0f
        }
    }

    AnimatedVisibility(
        visible = showComposeModal,
        enter = slideInVertically(spring(stiffness = Spring.StiffnessMediumLow)) { it } + fadeIn(),
        exit = slideOutVertically(spring(stiffness = Spring.StiffnessMediumLow)) { it } + fadeOut()
    ) {
        val animatedProgress by animateFloatAsState(
            targetValue = if (isPredictiveActive) predictiveProgress else 0f,
            animationSpec = spring(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioNoBouncy),
            label = "predictiveProgressAnim"
        )

        // Real-time predictive physics: scale down, round corners, and downward translation
        val sheetScale = 1f - (0.12f * animatedProgress)
        val sheetCornerRadius = (28 * animatedProgress).dp
        val sheetOffsetY = with(LocalDensity.current) { (90.dp.toPx() * animatedProgress) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f * (1f - animatedProgress)))
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = sheetScale
                        scaleY = sheetScale
                        translationY = sheetOffsetY
                        shape = RoundedCornerShape(sheetCornerRadius)
                        clip = true
                    },
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = (8 * animatedProgress).dp
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
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Live Android 15 Predictive Back Active!",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "Swipe inward from the left or right edge of your phone to see this sheet scale, round its corners, and slide downwards following your thumb in real time!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            if (isPredictiveActive) {
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Finger Progress: ${(predictiveProgress * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Interactive Morphing Dock with configurable crossfade physics and directional scroll reactivity.
 */
@Composable
fun InteractiveMorphingDock(
    activeScreen: SimulatedScreen,
    morphingOption: MorphingOption,
    continuousPosition: Float,
    onTabClick: (Int) -> Unit,
    isHomeCollapsed: Boolean,
    isSettingsCollapsed: Boolean,
    durationMs: Int,
    delayMs: Int,
    scaleFactor: Float,
    containerHeight: androidx.compose.ui.unit.Dp,
    clockSelectedIndex: Int,
    onClockDestinationSelected: (Int) -> Unit,
    clockSpringStiffness: Float,
    clockSpringDamping: Float,
    clockSelectedScale: Float,
    clockPressedScale: Float,
    onComposeClick: () -> Unit,
    onScreenChange: (SimulatedScreen) -> Unit,
    modifier: Modifier = Modifier
) {
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
                            (fadeIn(tween(durationMs, delayMillis = delayMs)) + scaleIn(tween(durationMs, delayMillis = delayMs), initialScale = scaleFactor))
                                .togetherWith(fadeOut(tween(durationMs / 2)) + scaleOut(tween(durationMs / 2), targetScale = scaleFactor))
                        },
                        label = "dockLeftContent"
                    ) { screen ->
                        when (screen) {
                            SimulatedScreen.HOME -> DynamicSwipeTabPill(
                                continuousPosition = continuousPosition,
                                isCollapsed = isHomeCollapsed,
                                containerHeight = containerHeight,
                                onTabClick = onTabClick
                            )
                            SimulatedScreen.THREAD_DETAIL -> MockReplyPill(
                                containerHeight = containerHeight,
                                onClick = {}
                            )
                            SimulatedScreen.SETTINGS -> MockCircleBack(
                                isCollapsed = isSettingsCollapsed,
                                containerHeight = containerHeight,
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
                            (fadeIn(tween(durationMs, delayMillis = delayMs)) + scaleIn(tween(durationMs, delayMillis = delayMs), initialScale = scaleFactor))
                                .togetherWith(fadeOut(tween(durationMs / 2)) + scaleOut(tween(durationMs / 2), targetScale = scaleFactor))
                        },
                        label = "dockRightContent"
                    ) { screen ->
                        when (screen) {
                            SimulatedScreen.HOME -> MockComposeFab(
                                isCollapsed = isHomeCollapsed,
                                containerHeight = containerHeight,
                                onClick = onComposeClick
                            )
                            SimulatedScreen.THREAD_DETAIL -> MockForwardPill(
                                containerHeight = containerHeight,
                                onClick = {}
                            )
                            SimulatedScreen.SETTINGS -> MockSettingsTitlePill(
                                title = "Settings",
                                isCollapsed = isSettingsCollapsed,
                                containerHeight = containerHeight
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
                                    isCollapsed = isHomeCollapsed,
                                    containerHeight = containerHeight,
                                    onTabClick = onTabClick
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, shadowElevation = 6.dp) {
                                MockComposeFab(
                                    isCollapsed = isHomeCollapsed,
                                    containerHeight = containerHeight,
                                    onClick = onComposeClick
                                )
                            }
                        }
                        SimulatedScreen.THREAD_DETAIL -> {
                            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, shadowElevation = 6.dp) {
                                MockReplyPill(containerHeight = containerHeight, onClick = {})
                            }
                            Spacer(Modifier.width(12.dp))
                            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceContainerHigh, shadowElevation = 6.dp) {
                                MockForwardPill(containerHeight = containerHeight, onClick = {})
                            }
                        }
                        SimulatedScreen.SETTINGS -> {
                            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceContainerHigh, shadowElevation = 6.dp) {
                                MockCircleBack(
                                    isCollapsed = isSettingsCollapsed,
                                    containerHeight = containerHeight,
                                    onClick = { onScreenChange(SimulatedScreen.HOME) }
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceContainerHigh, shadowElevation = 6.dp) {
                                MockSettingsTitlePill(
                                    title = "Settings",
                                    isCollapsed = isSettingsCollapsed,
                                    containerHeight = containerHeight
                                )
                            }
                        }
                    }
                }
            }
        }

        MorphingOption.OPTION_C_ELASTIC_SPLIT -> {
            // OPTION C: Elastic Pill Morph
            Row(
                modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (activeScreen == SimulatedScreen.THREAD_DETAIL) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                    shadowElevation = 6.dp
                ) {
                    when (activeScreen) {
                        SimulatedScreen.HOME -> DynamicSwipeTabPill(
                            continuousPosition = continuousPosition,
                            isCollapsed = isHomeCollapsed,
                            containerHeight = containerHeight,
                            onTabClick = onTabClick
                        )
                        SimulatedScreen.THREAD_DETAIL -> MockReplyPill(containerHeight = containerHeight, onClick = {})
                        SimulatedScreen.SETTINGS -> MockCircleBack(
                            isCollapsed = isSettingsCollapsed,
                            containerHeight = containerHeight,
                            onClick = { onScreenChange(SimulatedScreen.HOME) }
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Surface(
                    shape = CircleShape,
                    color = if (activeScreen == SimulatedScreen.HOME) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                    shadowElevation = 6.dp
                ) {
                    when (activeScreen) {
                        SimulatedScreen.HOME -> MockComposeFab(
                            isCollapsed = isHomeCollapsed,
                            containerHeight = containerHeight,
                            onClick = onComposeClick
                        )
                        SimulatedScreen.THREAD_DETAIL -> MockForwardPill(containerHeight = containerHeight, onClick = {})
                        SimulatedScreen.SETTINGS -> MockSettingsTitlePill(
                            title = "Settings",
                            isCollapsed = isSettingsCollapsed,
                            containerHeight = containerHeight
                        )
                    }
                }
            }
        }
        MorphingOption.OPTION_D_CLOCK_EXPRESSIVE -> {
            ClockExpressiveNavigationBar(
                selectedIndex = clockSelectedIndex,
                onDestinationSelected = onClockDestinationSelected,
                springStiffness = clockSpringStiffness,
                springDampingRatio = clockSpringDamping,
                selectedIconScale = clockSelectedScale,
                pressedIconScale = clockPressedScale,
                modifier = modifier.fillMaxWidth()
            )
        }
    }
}


private data class ClockNavigationItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

/**
 * Clock-inspired compact navigation bar.
 *
 * The indicator tracks the selected destination with a spring and briefly stretches
 * across longer travel distances. Icons compress while pressed and spring into their
 * selected scale on release. Labels stay visible, matching the compact Clock layout.
 */
@Composable
fun ClockExpressiveNavigationBar(
    selectedIndex: Int,
    onDestinationSelected: (Int) -> Unit,
    springStiffness: Float,
    springDampingRatio: Float,
    selectedIconScale: Float,
    pressedIconScale: Float,
    modifier: Modifier = Modifier
) {
    val destinations = remember {
        listOf(
            ClockNavigationItem("Alarms", Icons.Rounded.Alarm, Icons.Outlined.Alarm),
            ClockNavigationItem("World Clock", Icons.Rounded.Public, Icons.Outlined.Public),
            ClockNavigationItem("Timers", Icons.Rounded.HourglassBottom, Icons.Outlined.HourglassBottom),
            ClockNavigationItem("Stopwatch", Icons.Rounded.Timer, Icons.Outlined.Timer),
            ClockNavigationItem("Bedtime", Icons.Rounded.Bedtime, Icons.Outlined.Bedtime)
        )
    }

    val indicatorWidth = 44.dp
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp),
        color = MaterialTheme.colorScheme.surface,
        shape = RectangleShape,
        shadowElevation = 0.dp
    ) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val density = LocalDensity.current
            val totalWidthPx = with(density) { maxWidth.toPx() }
            val indicatorWidthPx = with(density) { indicatorWidth.toPx() }
            val initialIndicatorX = (totalWidthPx / destinations.size - indicatorWidthPx) / 2f
            val indicatorOffset = remember(totalWidthPx) {
                Animatable(initialIndicatorX)
            }
            val indicatorStretch = remember { Animatable(1f) }
            val safeIndex = selectedIndex.coerceIn(destinations.indices)
            val targetIndicatorX =
                (totalWidthPx * (safeIndex + 0.5f) / destinations.size) - indicatorWidthPx / 2f

            LaunchedEffect(targetIndicatorX, springStiffness, springDampingRatio) {
                val travel = abs(targetIndicatorX - indicatorOffset.value)
                if (travel > 0.5f) {
                    launch {
                        val stretch = 1f + (travel / (totalWidthPx / 2f).coerceAtLeast(1f))
                            .coerceIn(0f, 0.18f)
                        indicatorStretch.animateTo(
                            targetValue = stretch,
                            animationSpec = spring(
                                stiffness = springStiffness * 1.35f,
                                dampingRatio = springDampingRatio
                            )
                        )
                        indicatorStretch.animateTo(
                            targetValue = 1f,
                            animationSpec = spring(
                                stiffness = springStiffness,
                                dampingRatio = springDampingRatio
                            )
                        )
                    }
                    indicatorOffset.animateTo(
                        targetValue = targetIndicatorX,
                        animationSpec = spring(
                            stiffness = springStiffness,
                            dampingRatio = springDampingRatio
                        )
                    )
                } else {
                    indicatorOffset.snapTo(targetIndicatorX)
                    indicatorStretch.snapTo(1f)
                }
            }

            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset {
                        IntOffset(
                            x = indicatorOffset.value.roundToInt(),
                            y = with(density) { 6.dp.roundToPx() }
                        )
                    }
                    .width(indicatorWidth)
                    .height(30.dp)
                    .graphicsLayer {
                        scaleX = indicatorStretch.value
                    },
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(50),
                shadowElevation = 0.dp
            ) {}

            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                destinations.forEachIndexed { index, destination ->
                    val isSelected = index == safeIndex
                    val interactionSource = remember { MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()
                    val iconScale by animateFloatAsState(
                        targetValue = when {
                            isPressed -> pressedIconScale
                            isSelected -> selectedIconScale
                            else -> 1f
                        },
                        animationSpec = spring(
                            stiffness = springStiffness,
                            dampingRatio = if (isPressed) {
                                Spring.DampingRatioNoBouncy
                            } else {
                                springDampingRatio
                            }
                        ),
                        label = "clockNavIconScale$index"
                    )
                    val contentColor by animateColorAsState(
                        targetValue = if (isSelected) {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        animationSpec = spring(
                            stiffness = springStiffness,
                            dampingRatio = Spring.DampingRatioNoBouncy
                        ),
                        label = "clockNavContentColor$index"
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable(
                                interactionSource = interactionSource,
                                indication = ripple(bounded = true)
                            ) {
                                onDestinationSelected(index)
                            },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (isSelected) {
                                destination.selectedIcon
                            } else {
                                destination.unselectedIcon
                            },
                            contentDescription = destination.label,
                            tint = contentColor,
                            modifier = Modifier
                                .size(23.dp)
                                .graphicsLayer {
                                    scaleX = iconScale
                                    scaleY = iconScale
                                }
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = destination.label,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Normal,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

/**
 * 1:1 Swipe-Synchronized Dynamic Tab Pill (Point 2 & Point 7).
 * - Generous 56dp height and touch-friendly padding.
 * - At rest: Selected tab = [Icon + Bold Label], unselected = [Icon only].
 * - During swipe: Shifts through compact 3-icon state [Icon] [Icon] [Icon], smoothly expanding target label.
 * - Directional scroll: Collapses into compact 3-icon state when scrolling down; stays collapsed; regains full shape on opposite swipe!
 */
@Composable
fun DynamicSwipeTabPill(
    continuousPosition: Float,
    isCollapsed: Boolean,
    containerHeight: androidx.compose.ui.unit.Dp,
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

    val itemHeight = containerHeight - 12.dp

    Row(
        modifier = Modifier
            .height(containerHeight)
            .animateContentSize(spring(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioNoBouncy))
            .padding(horizontal = 6.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        tabs.forEach { tab ->
            val dist = abs(clampedPos - tab.index)
            // If directionally collapsed from scrolling, label is collapsed completely
            val baseExpansion = (1f - dist * 2.2f).coerceIn(0f, 1f)
            val labelExpansion = if (isCollapsed) 0f else baseExpansion
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
                    .height(itemHeight)
                    .clip(CircleShape)
                    .background(containerColor)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true)
                    ) { onTabClick(tab.index) },
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

/**
 * Home Interactive Demo with Google Apps Avatar Vertical Swipe (Point 3)
 * and Directional Scroll Connection (Point 2).
 */
@Composable
fun HomeInteractiveDemo(
    pagerState: PagerState,
    scrollConnection: DirectionalScrollConnection,
    accounts: List<DemoAccount>,
    currentAccountIndex: Int,
    avatarSwipeDirection: Int,
    onAvatarSwipe: (Int) -> Unit,
    onOpenAddAccount: () -> Unit,
    isSearchExpanded: Boolean,
    onSearchExpandedChange: (Boolean) -> Unit
) {
    var dragAccumulator by remember { mutableFloatStateOf(0f) }

    // Predictive back for Search view
    PredictiveBackHandler(enabled = isSearchExpanded) { progressFlow: Flow<BackEventCompat> ->
        try {
            progressFlow.collect { }
            onSearchExpandedChange(false)
        } catch (e: CancellationException) {
            // Cancelled
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollConnection)
    ) {
        // Search Bar with Google Apps Avatar Vertical Slide (Point 3)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = if (isSearchExpanded) 0.dp else 16.dp, vertical = 8.dp)
        ) {
            val currentAccount = accounts[currentAccountIndex]

            SearchBar(
                inputField = {
                    SearchBarDefaults.InputField(
                        query = "",
                        onQueryChange = {},
                        onSearch = {},
                        expanded = isSearchExpanded,
                        onExpandedChange = onSearchExpandedChange,
                        placeholder = {
                            Text(
                                "Search in mail (${currentAccount.name})",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
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
                                // Google Apps Vertical Slide Avatar (Point 3)
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(currentAccount.containerColor)
                                        .pointerInput(accounts) {
                                            detectVerticalDragGestures(
                                                onVerticalDrag = { change, dragAmount ->
                                                    change.consume()
                                                    dragAccumulator += dragAmount
                                                },
                                                onDragEnd = {
                                                    if (dragAccumulator > 16f) {
                                                        // Swipe DOWN -> Next account (slides in from top)
                                                        onAvatarSwipe(1)
                                                    } else if (dragAccumulator < -16f) {
                                                        // Swipe UP -> Previous account (slides in from bottom)
                                                        onAvatarSwipe(-1)
                                                    }
                                                    dragAccumulator = 0f
                                                }
                                            )
                                        }
                                        .clickable { onOpenAddAccount() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    AnimatedContent(
                                        targetState = currentAccountIndex,
                                        transitionSpec = {
                                            if (avatarSwipeDirection >= 0) {
                                                // Swipe Down: new comes from top (-1), old exits down (+1)
                                                (slideInVertically(spring(stiffness = Spring.StiffnessMediumLow)) { -it } + fadeIn())
                                                    .togetherWith(slideOutVertically(spring(stiffness = Spring.StiffnessMediumLow)) { it } + fadeOut())
                                            } else {
                                                // Swipe Up: new comes from bottom (+1), old exits up (-1)
                                                (slideInVertically(spring(stiffness = Spring.StiffnessMediumLow)) { it } + fadeIn())
                                                    .togetherWith(slideOutVertically(spring(stiffness = Spring.StiffnessMediumLow)) { -it } + fadeOut())
                                            }
                                        },
                                        label = "googleAvatarSwipe"
                                    ) { accIdx ->
                                        val acc = accounts[accIdx]
                                        Text(
                                            text = acc.avatarLetter,
                                            color = acc.onContainerColor,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                    }
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
                    Text("Tap back arrow or swipe back from edge to collapse back into search bar pill.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // Hint Banner
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Rounded.SwapVert, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    "Swipe avatar UP/DOWN to switch accounts (Google style). Scroll list down to collapse dock; swipe up to restore!",
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

        // Email List with Account Transition (Point 3) & 1:1 Horizontal Pager (Point 7)
        AnimatedContent(
            targetState = currentAccountIndex,
            transitionSpec = {
                (fadeIn(tween(180)) + slideInVertically(tween(180)) { 20 })
                    .togetherWith(fadeOut(tween(100)))
            },
            modifier = Modifier.weight(1f),
            label = "accountEmailListTransition"
        ) { accIdx ->
            val account = accounts[accIdx]

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text(
                            when (page) {
                                0 -> "${account.name} · Inbox"
                                1 -> "${account.name} · Sent"
                                else -> "${account.name} · Archived"
                            },
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    items(10) { idx ->
                        InteractiveEmailCard(
                            title = when (accIdx) {
                                0 -> "Security Alert - Sign-in from Pixel 8a #$idx"
                                1 -> "Sprint Review: Q4 Roadmap & Milestones #$idx"
                                else -> "Order Confirmation: Fastmail Payment #$idx"
                            },
                            sender = when (accIdx) {
                                0 -> "Google Account Security"
                                1 -> "Engineering Lead"
                                else -> "Customer Care"
                            },
                            date = "Nov ${20 - idx}"
                        )
                    }
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
                    .size(42.dp)
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
 * Settings Interactive Demo with Directional Scroll Collapse (Point 2)
 * and Uniform 68dp Item Height (Point 9).
 */
@Composable
fun SettingsInteractiveDemo(
    listState: LazyListState,
    scrollConnection: DirectionalScrollConnection,
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollConnection)
    ) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text("Settings", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(
                    "Scroll down to collapse bottom dock; swipe up in opposite direction to restore it!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
            }
            items(settingsCategories.size) { idx ->
                val (title, subtitle) = settingsCategories[idx]
                // Point 9: Uniform Height Item (68dp)
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

// ── Mock Floating Dock Components (56dp Height) ──────────────────────────

@Composable
fun MockComposeFab(
    isCollapsed: Boolean,
    containerHeight: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit
) {
    val fabSize by animateDpAsState(
        targetValue = if (isCollapsed) (containerHeight - 10.dp) else containerHeight,
        animationSpec = spring(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioNoBouncy),
        label = "fabSizeAnim"
    )

    Box(
        modifier = Modifier
            .size(fabSize)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Rounded.Edit,
            contentDescription = "Compose",
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun MockReplyPill(
    containerHeight: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .height(containerHeight)
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(Icons.AutoMirrored.Rounded.Reply, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
        Spacer(Modifier.width(8.dp))
        Text("Reply", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}

@Composable
fun MockForwardPill(
    containerHeight: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .height(containerHeight)
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp),
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
    isCollapsed: Boolean,
    containerHeight: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit
) {
    val backSize by animateDpAsState(
        targetValue = if (isCollapsed) (containerHeight - 10.dp) else containerHeight,
        animationSpec = spring(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioNoBouncy),
        label = "backSizeAnim"
    )

    Box(
        modifier = Modifier
            .size(backSize)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", modifier = Modifier.size(24.dp))
    }
}

@Composable
fun MockSettingsTitlePill(
    title: String,
    isCollapsed: Boolean,
    containerHeight: androidx.compose.ui.unit.Dp
) {
    val collapsePadding by animateDpAsState(
        targetValue = if (isCollapsed) 16.dp else 28.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioNoBouncy),
        label = "settingsTitlePaddingAnim"
    )

    Box(
        modifier = Modifier
            .height(containerHeight)
            .padding(horizontal = collapsePadding),
        contentAlignment = Alignment.Center
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}
