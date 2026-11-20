@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class
)

package com.shrivatsav.motionlabdemo

import android.os.Build
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
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Forward
import androidx.compose.material.icons.automirrored.rounded.Reply
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class ClockMailDemoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { ClockMailDemoApp() }
    }
}

private enum class MailTab(val title: String, val subtitle: String, val label: String) {
    INBOX("Inbox", "A calmer place for your conversations", "Inbox"),
    STARRED("Starred", "The messages worth keeping close", "Starred"),
    SNOOZED("Snoozed", "Back when you need them", "Snoozed"),
    SENT("Sent", "Messages you have sent", "Sent"),
    MORE("Your space", "Preferences for your mail", "More")
}

private enum class MotionStyle(val title: String) {
    EXPRESSIVE("Expressive"),
    CALM("Calm"),
    REDUCED("Reduced")
}

private data class MailMessage(
    val id: Int,
    val sender: String,
    val initials: String,
    val subject: String,
    val snippet: String,
    val date: String,
    val unread: Boolean,
    val starred: Boolean,
    val accent: Int,
    val body: String,
    val attachment: String? = null
)

@Composable
private fun ClockMailDemoApp() {
    val context = LocalContext.current
    var darkTheme by remember { mutableStateOf(false) }
    var dynamicColor by remember { mutableStateOf(true) }
    var motionStyle by remember { mutableStateOf(MotionStyle.EXPRESSIVE) }
    var selectedTab by remember { mutableStateOf(MailTab.INBOX) }
    var selectedMessage by remember { mutableStateOf<MailMessage?>(null) }
    var composing by remember { mutableStateOf(false) }
    var searchExpanded by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var unreadOnly by remember { mutableStateOf(false) }
    var accountIndex by remember { mutableStateOf(0) }

    val messages = remember { mutableStateListOf<MailMessage>().apply { addAll(sampleMessages()) } }
    val accounts = listOf("Sumit Kumar", "Work account", "Personal account")
    val scheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        darkTheme -> darkColorScheme()
        else -> lightColorScheme()
    }

    fun closeTopLayer() {
        when {
            composing -> composing = false
            selectedMessage != null -> selectedMessage = null
            searchExpanded -> {
                searchExpanded = false
                query = ""
            }
            selectedTab == MailTab.MORE -> selectedTab = MailTab.INBOX
            else -> Unit
        }
    }

    BackHandler(
        enabled = composing || selectedMessage != null || searchExpanded || selectedTab == MailTab.MORE
    ) { closeTopLayer() }

    MaterialTheme(colorScheme = scheme) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                if (composing) {
                    Spacer(Modifier.height(8.dp))
                } else if (selectedMessage != null) {
                    DetailHeader(
                        onBack = { selectedMessage = null },
                        onArchive = {
                            selectedMessage = null
                            selectedTab = MailTab.INBOX
                        }
                    )
                } else {
                    MainHeader(
                        tab = selectedTab,
                        searchExpanded = searchExpanded,
                        query = query,
                        unreadOnly = unreadOnly,
                        accountInitial = when (accountIndex) { 0 -> "S"; 1 -> "W"; else -> "P" },
                        onOpenSearch = { searchExpanded = true },
                        onCloseSearch = {
                            searchExpanded = false
                            query = ""
                        },
                        onQueryChange = { query = it },
                        onUnreadChange = { unreadOnly = it },
                        onAccountClick = { accountIndex = (accountIndex + 1) % accounts.size },
                        onSettingsClick = { selectedTab = MailTab.MORE }
                    )
                }

                val pageKey = when {
                    composing -> "compose"
                    selectedMessage != null -> "detail-" + selectedMessage!!.id
                    selectedTab == MailTab.MORE -> "settings"
                    else -> "tab-" + selectedTab.name
                }

                AnimatedContent(
                    targetState = pageKey,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    transitionSpec = {
                        (fadeIn(tween(220)) + slideInHorizontally { it / 16 }) togetherWith
                            (fadeOut(tween(150)) + slideOutHorizontally { -it / 24 })
                    },
                    label = "mail-page"
                ) { target ->
                    when {
                        target == "compose" -> ComposeMailScreen(
                            onCancel = { composing = false },
                            onSend = { to, subject, body ->
                                messages.add(
                                    0,
                                    MailMessage(
                                        id = (messages.maxOfOrNull { it.id } ?: 0) + 1,
                                        sender = "To: $to",
                                        initials = "S",
                                        subject = subject.ifBlank { "(no subject)" },
                                        snippet = body.take(90),
                                        date = "Just now",
                                        unread = false,
                                        starred = false,
                                        accent = 1,
                                        body = body.ifBlank { "No message content." }
                                    )
                                )
                                composing = false
                                selectedTab = MailTab.SENT
                                searchExpanded = false
                                query = ""
                            }
                        )
                        target.startsWith("detail-") -> selectedMessage?.let { message ->
                            MailDetailScreen(message = message)
                        }
                        target == "settings" -> SettingsScreen(
                            darkTheme = darkTheme,
                            dynamicColor = dynamicColor,
                            motionStyle = motionStyle,
                            onDarkThemeChange = { darkTheme = it },
                            onDynamicColorChange = { dynamicColor = it },
                            onMotionStyleChange = { motionStyle = it }
                        )
                        else -> {
                            val visibleMessages = messages.filter { message ->
                                val matchesTab = when (selectedTab) {
                                    MailTab.INBOX -> true
                                    MailTab.STARRED -> message.starred
                                    MailTab.SNOOZED -> message.date.contains("Later")
                                    MailTab.SENT -> message.sender.startsWith("To:")
                                    MailTab.MORE -> false
                                }
                                val matchesUnread = !unreadOnly || message.unread
                                val matchesQuery = query.isBlank() ||
                                    message.sender.contains(query, ignoreCase = true) ||
                                    message.subject.contains(query, ignoreCase = true) ||
                                    message.snippet.contains(query, ignoreCase = true)
                                matchesTab && matchesUnread && matchesQuery
                            }
                            MailListScreen(
                                messages = visibleMessages,
                                onOpen = { message ->
                                    val index = messages.indexOfFirst { it.id == message.id }
                                    if (index >= 0) {
                                        val readMessage = messages[index].copy(unread = false)
                                        messages[index] = readMessage
                                        selectedMessage = readMessage
                                    }
                                },
                                onStar = { message ->
                                    val index = messages.indexOfFirst { it.id == message.id }
                                    if (index >= 0) {
                                        val updated = messages[index].copy(starred = !messages[index].starred)
                                        messages[index] = updated
                                    }
                                }
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = !composing,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                enter = fadeIn(tween(180)) + androidx.compose.animation.slideInVertically { it / 2 },
                exit = fadeOut(tween(120)) + androidx.compose.animation.slideOutVertically { it / 2 }
            ) {
                AnimatedContent(
                    targetState = selectedMessage != null,
                    transitionSpec = {
                        fadeIn(tween(180)) togetherWith fadeOut(tween(120))
                    },
                    label = "dock-morph"
                ) { detailMode ->
                    if (detailMode) {
                        DetailActionDock(
                            motionStyle = motionStyle,
                            onReply = { composing = true },
                            onArchive = { selectedMessage = null; selectedTab = MailTab.INBOX }
                        )
                    } else {
                        FloatingMailDock(
                            selectedTab = selectedTab,
                            motionStyle = motionStyle,
                            onSelect = {
                                selectedTab = it
                                selectedMessage = null
                                searchExpanded = false
                                query = ""
                                unreadOnly = false
                            }
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = !composing && selectedMessage == null && selectedTab != MailTab.MORE,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(end = 26.dp, bottom = 108.dp),
                enter = fadeIn(tween(160)) + scaleIn(initialScale = 0.86f),
                exit = fadeOut(tween(120)) + scaleOut(targetScale = 0.9f)
            ) {
                ExtendedFloatingActionButton(
                    onClick = { composing = true },
                    icon = { Icon(Icons.Rounded.Edit, contentDescription = null) },
                    text = { Text("Compose", fontWeight = FontWeight.SemiBold) },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.animateContentSize(
                        spring(dampingRatio = 0.76f, stiffness = 420f)
                    )
                )
            }
        }
    }
}

@Composable
private fun MainHeader(
    tab: MailTab,
    searchExpanded: Boolean,
    query: String,
    unreadOnly: Boolean,
    accountInitial: String,
    onOpenSearch: () -> Unit,
    onCloseSearch: () -> Unit,
    onQueryChange: (String) -> Unit,
    onUnreadChange: (Boolean) -> Unit,
    onAccountClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 22.dp, end = 18.dp, top = 14.dp, bottom = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    "MONOMAIL  /  PERSONAL",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.4.sp
                )
                Text(
                    tab.title,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            IconButton(onClick = onSettingsClick) {
                Icon(Icons.Rounded.Tune, contentDescription = "Settings")
            }
            Surface(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onAccountClick),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.tertiaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        accountInitial,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Text(
            tab.subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 1.dp, bottom = 14.dp)
        )

        AnimatedContent(
            targetState = searchExpanded,
            transitionSpec = { fadeIn(tween(160)) togetherWith fadeOut(tween(100)) },
            label = "search-expand"
        ) { expanded ->
            if (expanded) {
                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(26.dp),
                    placeholder = { Text("Search mail") },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = onCloseSearch) {
                            Icon(Icons.Rounded.Close, contentDescription = "Close search")
                        }
                    }
                )
            } else {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clickable(onClick = onOpenSearch),
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Rounded.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Search in mail",
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 14.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                accountInitial,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = !unreadOnly,
                onClick = { onUnreadChange(false) },
                label = { Text("All mail") }
            )
            FilterChip(
                selected = unreadOnly,
                onClick = { onUnreadChange(!unreadOnly) },
                label = { Text("Unread") }
            )
            FilterChip(
                selected = false,
                onClick = onSettingsClick,
                label = { Text("Manage") },
                leadingIcon = { Icon(Icons.Rounded.Tune, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
        }
    }
}

@Composable
private fun DetailHeader(onBack: () -> Unit, onArchive: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
        }
        Text(
            "Message",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        IconButton(onClick = onArchive) {
            Icon(Icons.Rounded.Archive, contentDescription = "Archive")
        }
        IconButton(onClick = {}) {
            Icon(Icons.Rounded.MoreVert, contentDescription = "More options")
        }
    }
}

@Composable
private fun MailListScreen(
    messages: List<MailMessage>,
    onOpen: (MailMessage) -> Unit,
    onStar: (MailMessage) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 10.dp, bottom = 166.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "TODAY",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.2.sp
                )
                Spacer(Modifier.weight(1f))
                Text(
                    messages.size.toString() + " messages",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        items(messages, key = { it.id }) { message ->
            MailCard(
                message = message,
                onClick = { onOpen(message) },
                onStar = { onStar(message) }
            )
        }
        if (messages.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 54.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Rounded.Inbox,
                        contentDescription = null,
                        modifier = Modifier.size(54.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        "Nothing here yet",
                        modifier = Modifier.padding(top = 14.dp),
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        "Your mail will appear here.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun MailCard(message: MailMessage, onClick: () -> Unit, onStar: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.985f else 1f,
        animationSpec = spring(dampingRatio = 0.72f, stiffness = 520f),
        label = "mail-card-press"
    )
    val bg by animateColorAsState(
        targetValue = when {
            pressed -> MaterialTheme.colorScheme.surfaceContainerHighest
            message.unread -> MaterialTheme.colorScheme.surfaceContainerHigh
            else -> MaterialTheme.colorScheme.surfaceContainerLow
        },
        animationSpec = tween(160),
        label = "mail-card-color"
    )
    val accent = accentColor(message.accent)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clickable(
                interactionSource = interaction,
                indication = LocalIndication.current,
                onClick = onClick
            ),
        shape = RoundedCornerShape(26.dp),
        color = bg,
        shadowElevation = if (pressed) 5.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.padding(start = 14.dp, end = 8.dp, top = 15.dp, bottom = 15.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                modifier = Modifier.size(46.dp),
                shape = CircleShape,
                color = accent
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        message.initials,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 13.dp, top = 1.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        message.sender,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (message.unread) FontWeight.Bold else FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        message.date,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    message.subject,
                    modifier = Modifier.padding(top = 4.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (message.unread) FontWeight.SemiBold else FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    message.snippet,
                    modifier = Modifier.padding(top = 3.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(
                onClick = onStar,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    if (message.starred) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                    contentDescription = if (message.starred) "Remove star" else "Star message",
                    tint = if (message.starred) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MailDetailScreen(message: MailMessage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 22.dp, end = 22.dp, top = 8.dp, bottom = 150.dp)
    ) {
        Text(
            message.subject,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(
            modifier = Modifier.padding(top = 20.dp, bottom = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(52.dp),
                shape = CircleShape,
                color = accentColor(message.accent)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        message.initials,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 14.dp)
            ) {
                Text(message.sender, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    "to me  ·  " + message.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = {}) {
                Icon(Icons.Rounded.StarBorder, contentDescription = "Star")
            }
            IconButton(onClick = {}) {
                Icon(Icons.Rounded.MoreVert, contentDescription = "More")
            }
        }
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow
        ) {
            Text(
                message.body,
                modifier = Modifier.padding(20.dp),
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = 28.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        if (message.attachment != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.AttachFile, contentDescription = null)
                    Column(Modifier.weight(1f).padding(start = 12.dp)) {
                        Text(message.attachment, fontWeight = FontWeight.SemiBold)
                        Text("Preview attachment", style = MaterialTheme.typography.bodySmall)
                    }
                    Icon(Icons.Rounded.ChevronRight, contentDescription = null)
                }
            }
        }
    }
}

@Composable
private fun FloatingMailDock(
    selectedTab: MailTab,
    motionStyle: MotionStyle,
    onSelect: (MailTab) -> Unit
) {
    val destinations = listOf(
        MailTab.INBOX to Icons.Rounded.Inbox,
        MailTab.STARRED to Icons.Rounded.StarBorder,
        MailTab.SNOOZED to Icons.Rounded.Schedule,
        MailTab.SENT to Icons.AutoMirrored.Rounded.Send,
        MailTab.MORE to Icons.Rounded.MoreHoriz
    )
    val selectedIndex = destinations.indexOfFirst { it.first == selectedTab }.coerceAtLeast(0)
    val spec = dockSpec(motionStyle)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(78.dp),
        shape = RoundedCornerShape(30.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shadowElevation = 16.dp,
        tonalElevation = 4.dp
    ) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val slotWidth = maxWidth / destinations.size
            val indicatorX by animateDpAsState(
                targetValue = slotWidth * selectedIndex,
                animationSpec = spec,
                label = "dock-indicator-position"
            )
            Box(
                modifier = Modifier
                    .offset(x = indicatorX)
                    .width(slotWidth)
                    .fillMaxHeight()
                    .padding(horizontal = 4.dp, vertical = 5.dp)
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {}
            }
            Row(Modifier.fillMaxSize()) {
                destinations.forEach { (tab, icon) ->
                    val selected = tab == selectedTab
                    DockDestination(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        label = tab.label,
                        icon = icon,
                        selected = selected,
                        motionStyle = motionStyle,
                        onClick = { onSelect(tab) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DockDestination(
    modifier: Modifier,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    motionStyle: MotionStyle,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (pressed) 0.88f else 1f,
        animationSpec = if (motionStyle == MotionStyle.REDUCED) tween(80)
        else spring(dampingRatio = 0.62f, stiffness = 520f),
        label = "dock-touch-scale"
    )
    Column(
        modifier = modifier
            .clickable(
                interactionSource = interaction,
                indication = LocalIndication.current,
                onClick = onClick
            )
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            icon,
            contentDescription = label,
            modifier = Modifier.graphicsLayer { scaleX = pressScale; scaleY = pressScale },
            tint = if (selected) MaterialTheme.colorScheme.onSecondaryContainer
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            label,
            modifier = Modifier.padding(top = 3.dp),
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) MaterialTheme.colorScheme.onSecondaryContainer
            else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1
        )
    }
}

@Composable
private fun DetailActionDock(
    motionStyle: MotionStyle,
    onReply: () -> Unit,
    onArchive: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(78.dp),
        shape = RoundedCornerShape(30.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shadowElevation = 16.dp,
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledTonalButton(
                onClick = onReply,
                modifier = Modifier.weight(1f).height(58.dp),
                shape = RoundedCornerShape(22.dp),
                contentPadding = PaddingValues(horizontal = 14.dp)
            ) {
                Icon(Icons.AutoMirrored.Rounded.Reply, contentDescription = null)
                Text("Reply", modifier = Modifier.padding(start = 8.dp))
            }
            FilledTonalButton(
                onClick = onReply,
                modifier = Modifier.weight(1f).height(58.dp),
                shape = RoundedCornerShape(22.dp),
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                Icon(Icons.AutoMirrored.Rounded.Forward, contentDescription = null)
                Text("Forward", modifier = Modifier.padding(start = 7.dp))
            }
            Surface(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onArchive),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.tertiaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Archive, contentDescription = "Archive")
                }
            }
        }
    }
}

@Composable
private fun ComposeMailScreen(
    onCancel: () -> Unit,
    onSend: (String, String, String) -> Unit
) {
    var recipient by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .padding(horizontal = 22.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 7.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onCancel) {
                Icon(Icons.Rounded.Close, contentDescription = "Discard")
            }
            Text(
                "New message",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            FilledTonalButton(
                onClick = { onSend(recipient, subject, body) },
                enabled = recipient.isNotBlank(),
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = null)
                Text("Send", modifier = Modifier.padding(start = 7.dp))
            }
        }
        OutlinedTextField(
            value = recipient,
            onValueChange = { recipient = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("To") },
            singleLine = true,
            shape = RoundedCornerShape(20.dp)
        )
        OutlinedTextField(
            value = subject,
            onValueChange = { subject = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            label = { Text("Subject") },
            singleLine = true,
            shape = RoundedCornerShape(20.dp)
        )
        OutlinedTextField(
            value = body,
            onValueChange = { body = it },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(top = 10.dp, bottom = 20.dp),
            label = { Text("Write something") },
            shape = RoundedCornerShape(22.dp)
        )
    }
}

@Composable
private fun SettingsScreen(
    darkTheme: Boolean,
    dynamicColor: Boolean,
    motionStyle: MotionStyle,
    onDarkThemeChange: (Boolean) -> Unit,
    onDynamicColorChange: (Boolean) -> Unit,
    onMotionStyleChange: (MotionStyle) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 160.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SettingPanel(title = "Appearance", subtitle = "Let the system palette shape your mail") {
            SettingSwitch(
                title = "Dynamic colour",
                subtitle = "Use colours from your wallpaper",
                checked = dynamicColor,
                onCheckedChange = onDynamicColorChange,
                icon = { Icon(Icons.Rounded.Palette, contentDescription = null) }
            )
            SettingSwitch(
                title = "Dark theme",
                subtitle = "Switch the whole demo to a dark palette",
                checked = darkTheme,
                onCheckedChange = onDarkThemeChange,
                icon = { Icon(Icons.Rounded.Settings, contentDescription = null) }
            )
        }
        SettingPanel(title = "Motion and touch", subtitle = "The same surfaces, with different physical character") {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                Text("Motion profile", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    "Changes the navigation spring and touch response.",
                    modifier = Modifier.padding(top = 3.dp, bottom = 12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MotionStyle.values().forEach { style ->
                        FilterChip(
                            selected = motionStyle == style,
                            onClick = { onMotionStyleChange(style) },
                            label = { Text(style.title) }
                        )
                    }
                }
            }
        }
        SettingPanel(title = "Notifications", subtitle = "A quiet inbox, with the important things surfaced") {
            SettingSwitch(
                title = "Gentle notifications",
                subtitle = "A placeholder setting for the demo",
                checked = true,
                onCheckedChange = {},
                icon = { Icon(Icons.Rounded.Notifications, contentDescription = null) }
            )
        }
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow
        ) {
            Column(Modifier.padding(18.dp)) {
                Text("Motion Lab build", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    "Standalone mail-inspired playground. Nothing here connects to an account or sends real mail.",
                    modifier = Modifier.padding(top = 5.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SettingPanel(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column {
            Column(Modifier.padding(start = 18.dp, end = 18.dp, top = 17.dp, bottom = 5.dp)) {
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Text(
                    subtitle,
                    modifier = Modifier.padding(top = 3.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            content()
        }
    }
}

@Composable
private fun SettingSwitch(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) { icon() }
        }
        Column(Modifier.weight(1f).padding(start = 12.dp, end = 8.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

private fun dockSpec(style: MotionStyle) = when (style) {
    MotionStyle.EXPRESSIVE -> spring<Dp>(dampingRatio = 0.68f, stiffness = 390f)
    MotionStyle.CALM -> spring<Dp>(dampingRatio = 0.92f, stiffness = 520f)
    MotionStyle.REDUCED -> tween<Dp>(durationMillis = 1)
}

@Composable
private fun accentColor(index: Int): Color {
    val colors = MaterialTheme.colorScheme
    return when (index % 4) {
        0 -> colors.primaryContainer
        1 -> colors.secondaryContainer
        2 -> colors.tertiaryContainer
        else -> colors.surfaceVariant
    }
}

private fun sampleMessages() = listOf(
    MailMessage(
        1, "Google Account Security", "G", "Security alert · Sign-in from Pixel 8a",
        "A new sign-in was detected on a device you use.", "7:21 AM", true, true, 0,
        "Hi Sumit,\n\nYour Google Account was just signed in to from a Pixel 8a. If this was you, you can safely ignore this message. If not, review your recent security activity.\n\nThis is sample content for the Motion Lab."
    ),
    MailMessage(
        2, "MonoMail Design Notes", "M", "The new navigation study",
        "The floating dock should feel attached to the content, not the screen edge.", "Yesterday", true, false, 1,
        "A note from the design team.\n\nThe dock keeps a stable stadium shape and baseline. Its contents change with context using a quiet crossfade. The selected destination indicator moves with a spring, while pressed controls compress and release."
    ),
    MailMessage(
        3, "Google Photos", "P", "Your weekly memories are ready",
        "A few moments from this week, collected in one place.", "Nov 18", false, true, 2,
        "Your week in photos is ready. Open the collection to revisit the moments you saved."
    ),
    MailMessage(
        4, "Calendar", "C", "Tomorrow · Design review",
        "10:30 AM · Motion and interaction review", "Nov 17", false, false, 3,
        "You have a design review scheduled for tomorrow at 10:30 AM. Bring the latest navigation prototype."
    ),
    MailMessage(
        5, "GitHub", "GH", "Build completed successfully",
        "The standalone demo workflow uploaded a debug APK.", "Nov 16", false, false, 1,
        "Your standalone demo build completed. This is placeholder content for the app layout.",
        "motionlab-demo-debug.apk"
    ),
    MailMessage(
        6, "Notion", "N", "A quieter way to work",
        "Your saved notes and recent pages are ready.", "Nov 15", false, false, 2,
        "A small collection of notes and ideas, ready whenever you need them."
    ),
    MailMessage(
        7, "Spotify", "S", "Your Friday mix",
        "A playlist made from your recent listening.", "Nov 14", false, false, 0,
        "Your Friday mix is ready. This message is only a placeholder."
    )
)
