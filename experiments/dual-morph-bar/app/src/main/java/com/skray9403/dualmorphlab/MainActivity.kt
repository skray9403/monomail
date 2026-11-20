package com.skray9403.dualmorphlab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

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

        var expandedAction by remember { mutableStateOf(ExpandedAction.COMPOSE) }
        var scrollAccumulator by remember { mutableFloatStateOf(0f) }

        val scrollConnection = remember {
            object : NestedScrollConnection {
                override fun onPostScroll(
                    consumed: androidx.compose.ui.geometry.Offset,
                    available: androidx.compose.ui.geometry.Offset,
                    source: NestedScrollSource
                ): androidx.compose.ui.geometry.Offset {
                    val delta = consumed.y
                    if (delta != 0f) {
                        scrollAccumulator += delta
                        if (scrollAccumulator < -7f) {
                            expandedAction = ExpandedAction.SEARCH
                            scrollAccumulator = 0f
                        } else if (scrollAccumulator > 7f) {
                            expandedAction = ExpandedAction.COMPOSE
                            scrollAccumulator = 0f
                        }
                    }
                    return androidx.compose.ui.geometry.Offset.Zero
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .nestedScroll(scrollConnection)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, end = 24.dp, top = 28.dp, bottom = 18.dp)
                ) {
                    Text(
                        text = "MonoMail",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(5.dp))
                    Text(
                        text = "Inbox · 4 unread",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 116.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(messages) { mail ->
                        MailCard(mail)
                    }
                }
            }

            DualMorphActionBar(
                expandedAction = expandedAction,
                onSearch = { expandedAction = ExpandedAction.SEARCH },
                onCompose = { expandedAction = ExpandedAction.COMPOSE },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(
                        end = 20.dp,
                        bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 16.dp
                    )
            )
        }
    }
}

private enum class ExpandedAction { SEARCH, COMPOSE }

private data class MailRow(
    val sender: String,
    val subject: String,
    val time: String,
    val unread: Boolean
)

@Composable
private fun MailCard(mail: MailRow) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = if (mail.unread) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainer
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

@Composable
private fun DualMorphActionBar(
    expandedAction: ExpandedAction,
    onSearch: () -> Unit,
    onCompose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val searchExpanded = expandedAction == ExpandedAction.SEARCH

    val searchWidth by animateDpAsState(
        targetValue = if (searchExpanded) 168.dp else 56.dp,
        animationSpec = tween(420, easing = FastOutSlowInEasing),
        label = "searchWidth"
    )
    val composeWidth by animateDpAsState(
        targetValue = if (searchExpanded) 56.dp else 168.dp,
        animationSpec = tween(420, easing = FastOutSlowInEasing),
        label = "composeWidth"
    )
    val searchX by animateDpAsState(
        targetValue = if (searchExpanded) 0.dp else 176.dp,
        animationSpec = tween(460, easing = FastOutSlowInEasing),
        label = "searchPosition"
    )
    val composeX by animateDpAsState(
        targetValue = if (searchExpanded) 176.dp else 0.dp,
        animationSpec = tween(460, easing = FastOutSlowInEasing),
        label = "composePosition"
    )

    Box(modifier = modifier.width(232.dp).height(64.dp)) {
        MorphAction(
            label = "Search",
            expanded = searchExpanded,
            width = searchWidth,
            x = searchX,
            onClick = onSearch,
            modifier = Modifier.align(Alignment.CenterStart)
        )
        MorphAction(
            label = "Compose",
            expanded = !searchExpanded,
            width = composeWidth,
            x = composeX,
            onClick = onCompose,
            modifier = Modifier.align(Alignment.CenterStart)
        )
    }
}

@Composable
private fun MorphAction(
    label: String,
    expanded: Boolean,
    width: androidx.compose.ui.unit.Dp,
    x: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val corner by animateDpAsState(
        targetValue = if (expanded) 28.dp else 20.dp,
        animationSpec = tween(420, easing = FastOutSlowInEasing),
        label = "${label}Corner"
    )
    val elevation by animateDpAsState(
        targetValue = if (expanded) 5.dp else 2.dp,
        animationSpec = tween(300),
        label = "${label}Elevation"
    )
    val tint by animateFloatAsState(
        targetValue = if (expanded) 1f else 0f,
        animationSpec = tween(360),
        label = "${label}Tint"
    )
    val container = androidx.compose.ui.graphics.lerp(
        MaterialTheme.colorScheme.surfaceContainerHigh,
        MaterialTheme.colorScheme.primaryContainer,
        tint
    )
    val content = androidx.compose.ui.graphics.lerp(
        MaterialTheme.colorScheme.onSurface,
        MaterialTheme.colorScheme.onPrimaryContainer,
        tint
    )

    Surface(
        onClick = onClick,
        modifier = modifier
            .offset { IntOffset(x.roundToPx(), 0) }
            .width(width)
            .height(56.dp)
            .shadow(elevation, RoundedCornerShape(corner)),
        shape = RoundedCornerShape(corner),
        color = container,
        contentColor = content
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.material3.Icon(
                imageVector = if (label == "Search") Icons.Default.Search else Icons.Default.Edit,
                contentDescription = label,
                modifier = Modifier.size(22.dp)
            )
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(tween(180, delayMillis = 70)) + expandHorizontally(tween(260, delayMillis = 40)),
                exit = fadeOut(tween(100)) + shrinkHorizontally(tween(180))
            ) {
                Row {
                    Spacer(Modifier.width(9.dp))
                    Text(label, fontWeight = FontWeight.Medium, fontSize = 14.sp, maxLines = 1)
                }
            }
        }
    }
}
