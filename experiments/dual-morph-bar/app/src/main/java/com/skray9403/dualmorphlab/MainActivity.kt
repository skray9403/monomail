package com.skray9403.dualmorphlab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.MarkEmailUnread
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { DualMorphLabApp() }
    }
}

private val LabColors = lightColorScheme(
    primary = Color(0xFF315DA8),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD8E2FF),
    onPrimaryContainer = Color(0xFF001A43),
    secondaryContainer = Color(0xFFE8EAF2),
    onSecondaryContainer = Color(0xFF252B36),
    surface = Color(0xFFF8F9FF),
    surfaceContainer = Color(0xFFEEF0F7),
    onSurface = Color(0xFF191B20),
    onSurfaceVariant = Color(0xFF454750)
)

@Composable
private fun DualMorphLabApp() {
    MaterialTheme(colorScheme = LabColors) {
        val messages = remember {
            listOf(
                MailRow("Maya Chen", "The updated mockups are ready. I added the motion study too.", "10:42 AM", true),
                MailRow("Design Systems", "Material 3 Expressive component review", "9:18 AM", true),
                MailRow("Arjun Mehta", "Can we move the review to tomorrow afternoon?", "Yesterday", false),
                MailRow("GitHub", "Your workflow run has completed successfully", "Yesterday", false),
                MailRow("Nina Patel", "Sharing the notes from our last discussion", "Tue", false),
                MailRow("Product Updates", "What is new in your workspace", "Mon", false),
                MailRow("Sam Wilson", "A few references for the new navigation", "Sun", false),
                MailRow("Alex Morgan", "Thanks, this looks good to me", "Sat", false),
                MailRow("Project Room", "Sprint planning and next steps", "Fri", false),
                MailRow("Riya Shah", "One last thing about the prototype", "Thu", false)
            )
        }
        var selectedTab by remember { mutableIntStateOf(0) }
        val selectedMessages = remember { mutableStateListOf<Int>() }
        var isScrolling by remember { mutableStateOf(false) }
        var scrollAccumulator by remember { mutableFloatStateOf(0f) }

        val scrollConnection = remember {
            object : NestedScrollConnection {
                override fun onPostScroll(
                    consumed: androidx.compose.ui.geometry.Offset,
                    available: androidx.compose.ui.geometry.Offset,
                    source: NestedScrollSource
                ): androidx.compose.ui.geometry.Offset {
                    val delta = consumed.y
                    if (delta < 0f) {
                        scrollAccumulator += -delta
                        if (scrollAccumulator >= 28f) {
                            isScrolling = true
                            scrollAccumulator = 0f
                        }
                    } else if (delta > 0f) {
                        scrollAccumulator -= delta
                        if (scrollAccumulator <= -28f) {
                            isScrolling = false
                            scrollAccumulator = 0f
                        }
                    }
                    return androidx.compose.ui.geometry.Offset.Zero
                }
            }
        }

        Box(
            modifier = Modifier.fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .nestedScroll(scrollConnection)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                        .padding(start = 24.dp, end = 24.dp, top = 28.dp, bottom = 18.dp)
                ) {
                    Text("MonoMail", color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 30.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(5.dp))
                    Text("Inbox · 4 unread", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 116.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) { items(messages) { mail ->
                    val index = messages.indexOf(mail)
                    MailCard(
                        mail = mail,
                        selected = index in selectedMessages,
                        onClick = { if (selectedMessages.isNotEmpty()) {
                            if (index in selectedMessages) selectedMessages.remove(index) else selectedMessages.add(index)
                        } else selectedMessages.add(index) },
                        onLongClick = { if (index !in selectedMessages) selectedMessages.add(index) }
                    )
                } }
            }
            MonoMailDock(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                isScrolling = isScrolling,
                selectedCount = selectedMessages.size,
                onClearSelection = { selectedMessages.clear() },
                onArchive = { selectedMessages.clear() },
                onDelete = { selectedMessages.clear() },
                onMarkUnread = { selectedMessages.clear() },
                modifier = Modifier.align(Alignment.BottomCenter)
                    .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 16.dp)
            )
        }
    }
}

@Composable
private fun MailCard(mail: MailRow, selected: Boolean, onClick: () -> Unit, onLongClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().combinedClickable(onClick = onClick, onLongClick = onLongClick),
        shape = RoundedCornerShape(22.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else if (mail.unread) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 17.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(mail.sender.first().toString(), fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSecondaryContainer)
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 14.dp, end = 8.dp)
            ) {
                Text(mail.sender, fontWeight = if (mail.unread) FontWeight.SemiBold else FontWeight.Medium, fontSize = 15.sp)
                Spacer(Modifier.height(4.dp))
                Text(mail.subject, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp, maxLines = 2)
            }
            Text(mail.time, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
        }
    }
}

/**
 * Mirrors MonoMail's persistent inbox dock: wide three-tab pill + separate
 * Compose FAB. Scroll collapses the dock in place; the controls never swap.
 */
@Composable
private fun MonoMailDock(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    isScrolling: Boolean,
    selectedCount: Int,
    onClearSelection: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit,
    onMarkUnread: () -> Unit,
    modifier: Modifier = Modifier
) {
    val screenWidth = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp.dp
    val expandedDockWidth = (screenWidth - 32.dp) - 76.dp
    val collapsedDockWidth = 180.dp
    val selectionMode = selectedCount > 0
    val springDp = spring<androidx.compose.ui.unit.Dp>(
        dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium
    )
    val springFloat = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium
    )
    val collapseFraction by animateFloatAsState(
        targetValue = if (isScrolling && !selectionMode) 1f else 0f,
        animationSpec = springFloat, label = "dockCollapseFraction"
    )
    val dockWidth by animateDpAsState(
        targetValue = if (isScrolling && !selectionMode) collapsedDockWidth else expandedDockWidth,
        animationSpec = springDp, label = "dockWidth"
    )
    val fabWidth by animateDpAsState(
        targetValue = if (isScrolling && !selectionMode) 0.dp else 64.dp,
        animationSpec = springDp, label = "fabWidth"
    )
    val fabSpacing by animateDpAsState(
        targetValue = if (isScrolling && !selectionMode) 0.dp else 12.dp,
        animationSpec = springDp, label = "fabSpacing"
    )
    val fabAlpha by animateFloatAsState(
        targetValue = if (isScrolling && !selectionMode) 0f else 1f,
        animationSpec = springFloat, label = "fabAlpha"
    )

    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.width(dockWidth).height(64.dp).graphicsLayer {
                shape = CircleShape
                clip = false
                shadowElevation = 6.dp.toPx()
            },
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.96f),
            contentColor = MaterialTheme.colorScheme.onSurface,
            tonalElevation = 1.dp,
            shadowElevation = 0.dp
        ) {
            // Stable three-slot geometry: only each slot's content changes.
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MorphDockSlot(
                    modifier = Modifier.weight(1f), selectionMode = selectionMode,
                    normalIcon = Icons.Rounded.Inbox, normalLabel = "Inbox",
                    actionIcon = Icons.Rounded.Close,
                    actionLabel = "$selectedCount selected",
                    selected = selectedTab == 0, collapseFraction = collapseFraction,
                    onClick = { if (selectionMode) onClearSelection() else onTabSelected(0) }
                )
                MorphDockSlot(
                    modifier = Modifier.weight(1f), selectionMode = selectionMode,
                    normalIcon = Icons.AutoMirrored.Rounded.Send, normalLabel = "Sent",
                    actionIcon = Icons.Rounded.Archive, actionLabel = "Archive",
                    selected = selectedTab == 1, collapseFraction = collapseFraction,
                    onClick = { if (selectionMode) onArchive() else onTabSelected(1) }
                )
                MorphDockSlot(
                    modifier = Modifier.weight(1f), selectionMode = selectionMode,
                    normalIcon = Icons.Rounded.Archive, normalLabel = "Archived",
                    actionIcon = Icons.Rounded.MarkEmailUnread, actionLabel = "Unread",
                    selected = selectedTab == 2, collapseFraction = collapseFraction,
                    onClick = { if (selectionMode) onMarkUnread() else onTabSelected(2) }
                )
            }
        }

        Spacer(Modifier.width(fabSpacing))
        // Never remove the FAB from composition. It morphs into Delete.
        FilledIconButton(
            onClick = { if (selectionMode) onDelete() },
            modifier = Modifier.size(64.dp).graphicsLayer {
                alpha = fabAlpha
                val scale = (fabWidth / 64.dp).coerceIn(0f, 1f)
                scaleX = scale
                scaleY = scale
                shape = CircleShape
                clip = false
                shadowElevation = (6.dp * fabAlpha).toPx()
            },
            enabled = selectionMode || !isScrolling,
            shape = CircleShape,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = if (selectionMode) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                contentColor = if (selectionMode) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            androidx.compose.animation.AnimatedContent(
                targetState = selectionMode,
                transitionSpec = {
                    (androidx.compose.animation.fadeIn(tween(150)) +
                        androidx.compose.animation.scaleIn(initialScale = 0.8f, animationSpec = tween(180)))
                        .togetherWith(androidx.compose.animation.fadeOut(tween(90)))
                },
                label = "fabContextMorph"
            ) { contextual ->
                Icon(
                    if (contextual) Icons.Rounded.Delete else Icons.Rounded.Edit,
                    contentDescription = if (contextual) "Delete selected" else "Compose",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun MorphDockSlot(
    modifier: Modifier,
    selectionMode: Boolean,
    normalIcon: ImageVector,
    normalLabel: String,
    actionIcon: ImageVector,
    actionLabel: String,
    selected: Boolean,
    collapseFraction: Float,
    onClick: () -> Unit
) {
    val background by androidx.compose.animation.animateColorAsState(
        targetValue = if (selected && !selectionMode) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
        animationSpec = tween(180), label = "dockSlotBackground"
    )
    val contentColor by androidx.compose.animation.animateColorAsState(
        targetValue = if (selected && !selectionMode) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(180), label = "dockSlotContent"
    )
    Box(
        modifier = modifier.fillMaxHeight().clip(CircleShape).background(background).clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = ripple(bounded = true), onClick = onClick
        ),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.animation.AnimatedContent(
            targetState = selectionMode,
            transitionSpec = {
                (androidx.compose.animation.fadeIn(tween(150)) +
                    androidx.compose.animation.scaleIn(initialScale = 0.88f, animationSpec = tween(180)))
                    .togetherWith(androidx.compose.animation.fadeOut(tween(90)))
            },
            label = "dockSlotMorph"
        ) { contextual ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                Icon(
                    if (contextual) actionIcon else normalIcon,
                    contentDescription = if (contextual) actionLabel else normalLabel,
                    tint = if (contextual) MaterialTheme.colorScheme.onSurface else contentColor,
                    modifier = Modifier.size(20.dp)
                )
                val labelAlpha = if (contextual) 1f else (1f - collapseFraction * 2.5f).coerceIn(0f, 1f)
                if (labelAlpha > 0.01f) {
                    Spacer(Modifier.width(5.dp * labelAlpha))
                    Text(
                        text = if (contextual) actionLabel else normalLabel,
                        maxLines = 1, softWrap = false,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (selected && !contextual) FontWeight.SemiBold else FontWeight.Medium
                        ),
                        color = (if (contextual) MaterialTheme.colorScheme.onSurface else contentColor).copy(alpha = labelAlpha),
                        modifier = Modifier.graphicsLayer { alpha = labelAlpha }
                    )
                }
            }
        }
    }
}

private data class MailRow(
    val sender: String,
    val subject: String,
    val time: String,
    val unread: Boolean
)

private data class DockTab(val label: String, val icon: ImageVector)

private val dockTabs = listOf(
    DockTab("Inbox", Icons.Rounded.Inbox),
    DockTab("Sent", Icons.AutoMirrored.Rounded.Send),
    DockTab("Archived", Icons.Rounded.Archive)
)
