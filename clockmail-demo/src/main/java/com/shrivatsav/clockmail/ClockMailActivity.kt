@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class
)

package com.shrivatsav.clockmail

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
import androidx.compose.foundation.layout.width
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
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material.icons.rounded.Label
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class ClockMailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { ClockMailApp() }
    }
}

private enum class MailDestination(val title: String, val icon: ImageVector) {
    INBOX("Inbox", Icons.Rounded.Inbox),
    STARRED("Starred", Icons.Rounded.StarBorder),
    SNOOZED("Snoozed", Icons.Rounded.Schedule),
    SENT("Sent", Icons.AutoMirrored.Rounded.Send),
    FOLDERS("Folders", Icons.Rounded.Folder)
}

private data class MailItem(
    val id: Int,
    val sender: String,
    val initials: String,
    val subject: String,
    val preview: String,
    val date: String,
    val time: String,
    val read: Boolean,
    val starred: Boolean,
    val accent: Int,
    val body: String,
    val folder: String = "Personal",
    val sent: Boolean = false,
    val snoozedUntil: String? = null,
    val attachment: String? = null
)

@Composable
private fun ClockMailApp() {
    val context = LocalContext.current
    var dark by remember { mutableStateOf(false) }
    var dynamic by remember { mutableStateOf(true) }
    var notifications by remember { mutableStateOf(true) }
    var destination by remember { mutableStateOf(MailDestination.INBOX) }
    var selected by remember { mutableStateOf<MailItem?>(null) }
    var composing by remember { mutableStateOf(false) }
    var searchOpen by remember { mutableStateOf(false) }
    var settingsOpen by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var unreadOnly by remember { mutableStateOf(false) }
    var sender by remember { mutableStateOf("Sumit Kumar") }
    var composeTo by remember { mutableStateOf("") }
    var composeSubject by remember { mutableStateOf("") }
    var composeBody by remember { mutableStateOf("") }
    val mail = remember { mutableStateListOf<MailItem>().apply { addAll(sampleMail()) } }

    val colors = when {
        dynamic && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        dark -> darkColorScheme()
        else -> lightColorScheme()
    }

    fun beginCompose(to: String = "", subject: String = "") {
        composeTo = to
        composeSubject = subject
        composeBody = ""
        composing = true
    }

    fun closeLayer() {
        when {
            composing -> composing = false
            selected != null -> selected = null
            searchOpen -> { searchOpen = false; query = "" }
            searchOpen -> { searchOpen = false; query = "" }
            settingsOpen -> settingsOpen = false
            destination == MailDestination.FOLDERS -> destination = MailDestination.INBOX
            else -> Unit
        }
    }

    BackHandler(
        enabled = composing || selected != null || searchOpen || settingsOpen || destination == MailDestination.FOLDERS
    ) { closeLayer() }

    MaterialTheme(colorScheme = colors) {
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
                when {
                    composing -> ComposeTopBar(
                        onClose = { composing = false },
                        onSend = {
                            val nextId = (mail.maxOfOrNull { it.id } ?: 0) + 1
                            mail.add(
                                0,
                                MailItem(
                                    nextId,
                                    "To: " + composeTo,
                                    "S",
                                    composeSubject.ifBlank { "(no subject)" },
                                    composeBody.take(100),
                                    "Just now",
                                    "Now",
                                    true,
                                    false,
                                    1,
                                    composeBody.ifBlank { "No message content." },
                                    sent = true
                                )
                            )
                            composing = false
                            selected = null
                            destination = MailDestination.SENT
                        },
                        canSend = composeTo.isNotBlank()
                    )
                    settingsOpen -> SettingsTopBar(onBack = { settingsOpen = false })
                    selected != null -> DetailTopBar(
                        onBack = { selected = null },
                        onArchive = {
                            mail.removeAll { it.id == selected?.id }
                            selected = null
                        }
                    )
                    else -> ClockTopBar(
                        title = destination.title,
                        searchOpen = searchOpen,
                        query = query,
                        onQuery = { query = it },
                        onSearch = { searchOpen = true },
                        onCloseSearch = { searchOpen = false; query = "" },
                        sender = sender,
                        onAccount = {
                            sender = if (sender == "Sumit Kumar") "Work account" else "Sumit Kumar"
                        },
                        onSettings = { settingsOpen = true }
                    )
                }

                val page = when {
                    composing -> "compose"
                    selected != null -> "detail-" + selected!!.id
                    settingsOpen -> "settings"
                    destination == MailDestination.FOLDERS -> "folders"
                    else -> destination.name
                }

                AnimatedContent(
                    targetState = page,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    transitionSpec = {
                        (fadeIn(tween(230)) + slideInHorizontally { it / 18 }) togetherWith
                            (fadeOut(tween(150)) + slideOutHorizontally { -it / 28 })
                    },
                    label = "clock-like-page-transition"
                ) { target ->
                    when {
                        target == "compose" -> ComposePage(
                            to = composeTo,
                            subject = composeSubject,
                            body = composeBody,
                            onTo = { composeTo = it },
                            onSubject = { composeSubject = it },
                            onBody = { composeBody = it }
                        )
                        target.startsWith("detail-") -> selected?.let { MailDetailPage(it) }
                        target == "settings" -> SettingsPage(
                            dark = dark,
                            dynamic = dynamic,
                            notifications = notifications,
                            onDark = { dark = it },
                            onDynamic = { dynamic = it },
                            onNotifications = { notifications = it }
                        )
                        target == "folders" -> FolderPage(
                            onSelect = { folder ->
                                query = folder
                                destination = MailDestination.INBOX
                                searchOpen = true
                            }
                        )
                        else -> {
                            val shown = mail.filter { item ->
                                val inDestination = when (destination) {
                                    MailDestination.INBOX -> !item.sent && item.snoozedUntil == null
                                    MailDestination.STARRED -> item.starred
                                    MailDestination.SNOOZED -> item.snoozedUntil != null
                                    MailDestination.SENT -> item.sent
                                    MailDestination.FOLDERS -> true
                                }
                                val unreadMatches = !unreadOnly || !item.read
                                val searchMatches = query.isBlank() ||
                                    item.sender.contains(query, true) ||
                                    item.subject.contains(query, true) ||
                                    item.preview.contains(query, true) ||
                                    item.folder.contains(query, true)
                                inDestination && unreadMatches && searchMatches
                            }
                            MailListPage(
                                destination = destination,
                                mail = shown,
                                onOpen = { item ->
                                    val index = mail.indexOfFirst { it.id == item.id }
                                    if (index >= 0) {
                                        val opened = mail[index].copy(read = true)
                                        mail[index] = opened
                                        selected = opened
                                    }
                                },
                                onStar = { item ->
                                    val index = mail.indexOfFirst { it.id == item.id }
                                    if (index >= 0) mail[index] = mail[index].copy(starred = !mail[index].starred)
                                },
                                onUnreadFilter = { unreadOnly = it },
                                unreadOnly = unreadOnly
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = !composing && !settingsOpen,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(horizontal = 18.dp, vertical = 9.dp),
                enter = fadeIn(tween(170)) + scaleIn(initialScale = 0.96f),
                exit = fadeOut(tween(130)) + scaleOut(targetScale = 0.96f)
            ) {
                ClockDock(
                    destination = destination,
                    detailMode = selected != null,
                    onDestination = {
                        destination = it
                        selected = null
                        searchOpen = false
                        query = ""
                        unreadOnly = false
                    },
                    onReply = {
                        val item = selected
                        if (item != null) beginCompose(item.sender, "Re: " + item.subject)
                    },
                    onForward = {
                        val item = selected
                        if (item != null) beginCompose("", "Fwd: " + item.subject)
                    },
                    onArchive = {
                        mail.removeAll { it.id == selected?.id }
                        selected = null
                    }
                )
            }

            AnimatedVisibility(
                visible = !composing && !settingsOpen && selected == null && destination != MailDestination.FOLDERS,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(end = 26.dp, bottom = 106.dp),
                enter = fadeIn(tween(150)) + scaleIn(initialScale = 0.78f),
                exit = fadeOut(tween(110)) + scaleOut(targetScale = 0.78f)
            ) {
                ClockComposeFab(onClick = { beginCompose() })
            }
        }
    }
}

@Composable
private fun ClockTopBar(
    title: String,
    searchOpen: Boolean,
    query: String,
    onQuery: (String) -> Unit,
    onSearch: () -> Unit,
    onCloseSearch: () -> Unit,
    sender: String,
    onAccount: () -> Unit,
    onSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 22.dp, end = 18.dp, top = 13.dp, bottom = 7.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            IconButton(onClick = onSearch) {
                Icon(Icons.Rounded.Search, contentDescription = "Search")
            }
            IconButton(onClick = onSettings) {
                Icon(Icons.Rounded.MoreVert, contentDescription = "More options")
            }
            Surface(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onAccount),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.tertiaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        sender.take(1),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }
        AnimatedVisibility(
            visible = searchOpen,
            enter = fadeIn(tween(160)) + scaleIn(initialScale = 0.97f),
            exit = fadeOut(tween(100)) + scaleOut(targetScale = 0.97f)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = onQuery,
                modifier = Modifier.fillMaxWidth().padding(top = 11.dp),
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                placeholder = { Text("Search mail") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = onCloseSearch) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close search")
                    }
                }
            )
        }
    }
}

@Composable
private fun DetailTopBar(onBack: () -> Unit, onArchive: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 7.dp),
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
            Icon(Icons.Rounded.MoreVert, contentDescription = "More")
        }
    }
}

@Composable
private fun SettingsTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
        }
        Text("Settings", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ComposeTopBar(onClose: () -> Unit, onSend: () -> Unit, canSend: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 12.dp, end = 18.dp, top = 7.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onClose) {
            Icon(Icons.Rounded.Close, contentDescription = "Discard")
        }
        Text(
            "Compose",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        FilledTonalButton(
            onClick = onSend,
            enabled = canSend,
            shape = RoundedCornerShape(18.dp)
        ) {
            Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = null)
            Text("Send", modifier = Modifier.padding(start = 7.dp))
        }
    }
}

@Composable
private fun MailListPage(
    destination: MailDestination,
    mail: List<MailItem>,
    onOpen: (MailItem) -> Unit,
    onStar: (MailItem) -> Unit,
    unreadOnly: Boolean,
    onUnreadFilter: (Boolean) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 7.dp, bottom = 164.dp),
        verticalArrangement = Arrangement.spacedBy(11.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 3.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        when (destination) {
                            MailDestination.INBOX -> "Your mail"
                            MailDestination.STARRED -> "Kept close"
                            MailDestination.SNOOZED -> "Back later"
                            MailDestination.SENT -> "Out in the world"
                            MailDestination.FOLDERS -> "Your folders"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        mail.size.toString() + " conversations",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (destination == MailDestination.INBOX) {
                    FilterChip(
                        selected = unreadOnly,
                        onClick = { onUnreadFilter(!unreadOnly) },
                        label = { Text("Unread") }
                    )
                }
            }
        }

        if (destination == MailDestination.INBOX && mail.any { !it.read }) {
            item {
                InboxPulseCard(unreadCount = mail.count { !it.read })
            }
        }

        if (mail.isEmpty()) {
            item { EmptyMailState(destination) }
        } else {
            val grouped = mail.groupBy { it.date }
            grouped.forEach { (date, entries) ->
                item(key = "group-" + date) {
                    Text(
                        date.uppercase(),
                        modifier = Modifier.padding(start = 5.dp, top = 8.dp, bottom = 1.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                items(entries, key = { it.id }) { item ->
                    ClockMailCard(
                        item = item,
                        onOpen = { onOpen(item) },
                        onStar = { onStar(item) }
                    )
                }
            }
        }
    }
}

@Composable
private fun InboxPulseCard(unreadCount: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(27.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(43.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Rounded.Inbox,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            Column(Modifier.weight(1f).padding(start = 13.dp)) {
                Text(
                    unreadCount.toString() + " waiting for you",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    "A few things need your attention",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f)
                )
            }
            Icon(
                Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun ClockMailCard(item: MailItem, onOpen: () -> Unit, onStar: () -> Unit) {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.982f else 1f,
        animationSpec = spring(dampingRatio = 0.62f, stiffness = 520f),
        label = "email-card-press"
    )
    val container by animateColorAsState(
        targetValue = when {
            pressed -> MaterialTheme.colorScheme.surfaceContainerHighest
            !item.read -> MaterialTheme.colorScheme.primaryContainer
            else -> MaterialTheme.colorScheme.surfaceContainerLow
        },
        animationSpec = tween(160),
        label = "email-card-tone"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clickable(
                interactionSource = source,
                indication = LocalIndication.current,
                onClick = onOpen
            ),
        shape = RoundedCornerShape(26.dp),
        color = container,
        shadowElevation = if (pressed) 5.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.padding(start = 14.dp, end = 7.dp, top = 15.dp, bottom = 15.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                modifier = Modifier.size(46.dp),
                shape = CircleShape,
                color = mailAccent(item.accent)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        item.initials,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f).padding(start = 13.dp, top = 1.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        item.sender,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (item.read) FontWeight.Medium else FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        item.time,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    item.subject,
                    modifier = Modifier.padding(top = 4.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (item.read) FontWeight.Normal else FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    item.preview,
                    modifier = Modifier.padding(top = 3.dp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (item.snoozedUntil != null) {
                    Text(
                        "Snoozed · " + item.snoozedUntil,
                        modifier = Modifier.padding(top = 7.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            IconButton(onClick = onStar, modifier = Modifier.size(39.dp)) {
                Icon(
                    if (item.starred) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                    contentDescription = if (item.starred) "Unstar" else "Star",
                    tint = if (item.starred) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MailDetailPage(item: MailItem) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 21.dp, end = 21.dp, top = 12.dp, bottom = 145.dp)
    ) {
        Text(
            item.subject,
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
                color = mailAccent(item.accent)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(item.initials, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
            }
            Column(Modifier.weight(1f).padding(start = 13.dp)) {
                Text(item.sender, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    item.date + " · " + item.time + " · to me",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = {}) {
                Icon(Icons.Rounded.StarBorder, contentDescription = "Star message")
            }
            IconButton(onClick = {}) {
                Icon(Icons.Rounded.MoreVert, contentDescription = "More")
            }
        }
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(27.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow
        ) {
            Text(
                item.body,
                modifier = Modifier.padding(20.dp),
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = 28.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        if (item.attachment != null) {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(top = 15.dp),
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.AttachFile, contentDescription = null)
                    Column(Modifier.weight(1f).padding(start = 12.dp)) {
                        Text(item.attachment, fontWeight = FontWeight.SemiBold)
                        Text("Attachment · Preview", style = MaterialTheme.typography.bodySmall)
                    }
                    Icon(Icons.Rounded.ChevronRight, contentDescription = null)
                }
            }
        }
        Surface(
            modifier = Modifier.fillMaxWidth().padding(top = 15.dp),
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Rounded.Label, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(
                    item.folder,
                    modifier = Modifier.padding(start = 11.dp),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun ComposePage(
    to: String,
    subject: String,
    body: String,
    onTo: (String) -> Unit,
    onSubject: (String) -> Unit,
    onBody: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .padding(horizontal = 20.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(top = 7.dp, bottom = 12.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow
        ) {
            Column(Modifier.padding(horizontal = 17.dp, vertical = 8.dp)) {
                OutlinedTextField(
                    value = to,
                    onValueChange = onTo,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("To") },
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp)
                )
                OutlinedTextField(
                    value = subject,
                    onValueChange = onSubject,
                    modifier = Modifier.fillMaxWidth().padding(top = 9.dp),
                    label = { Text("Subject") },
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp)
                )
            }
        }
        OutlinedTextField(
            value = body,
            onValueChange = onBody,
            modifier = Modifier.fillMaxWidth().weight(1f).padding(bottom = 18.dp),
            label = { Text("Write your message") },
            shape = RoundedCornerShape(25.dp)
        )
    }
}

@Composable
private fun FolderPage(onSelect: (String) -> Unit) {
    val folders = listOf(
        "Personal" to "Your everyday conversations",
        "Work" to "Projects, colleagues and updates",
        "Receipts" to "Orders and payment confirmations",
        "Social" to "People and communities",
        "Travel" to "Bookings and itineraries"
    )
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 13.dp, bottom = 150.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                "Collections",
                modifier = Modifier.padding(start = 5.dp, bottom = 5.dp),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
        items(folders) { (name, description) ->
            Surface(
                modifier = Modifier.fillMaxWidth().clickable { onSelect(name) },
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                Row(
                    modifier = Modifier.padding(17.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(43.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Rounded.Folder, contentDescription = null)
                        }
                    }
                    Column(Modifier.weight(1f).padding(start = 13.dp)) {
                        Text(name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.Rounded.ChevronRight, contentDescription = null)
                }
            }
        }
    }
}

@Composable
private fun EmptyMailState(destination: MailDestination) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            when (destination) {
                MailDestination.INBOX -> Icons.Rounded.Inbox
                MailDestination.STARRED -> Icons.Rounded.StarBorder
                MailDestination.SNOOZED -> Icons.Rounded.Schedule
                MailDestination.SENT -> Icons.AutoMirrored.Rounded.Send
                MailDestination.FOLDERS -> Icons.Rounded.Folder
            },
            contentDescription = null,
            modifier = Modifier.size(54.dp),
            tint = MaterialTheme.colorScheme.secondary
        )
        Text(
            "Nothing here for now",
            modifier = Modifier.padding(top = 14.dp),
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            "When there is something to see, it will appear here.",
            modifier = Modifier.padding(top = 4.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ClockComposeFab(onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.9f else 1f,
        animationSpec = spring(dampingRatio = 0.58f, stiffness = 500f),
        label = "clock-fab-press"
    )
    Surface(
        modifier = Modifier
            .size(64.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(22.dp))
            .clickable(
                interactionSource = interaction,
                indication = LocalIndication.current,
                onClick = onClick
            ),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        shadowElevation = if (pressed) 4.dp else 8.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(Icons.Rounded.Edit, contentDescription = "Compose", tint = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}

@Composable
private fun ClockDock(
    destination: MailDestination,
    detailMode: Boolean,
    onDestination: (MailDestination) -> Unit,
    onReply: () -> Unit,
    onForward: () -> Unit,
    onArchive: () -> Unit
) {
    val entries = MailDestination.values().toList()
    val selectedIndex = entries.indexOf(destination).coerceAtLeast(0)
    Surface(
        modifier = Modifier.fillMaxWidth().height(72.dp),
        shape = RoundedCornerShape(27.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 4.dp,
        shadowElevation = 14.dp
    ) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val itemWidth = maxWidth / 5
            val indicatorX by animateDpAsState(
                targetValue = itemWidth * selectedIndex,
                animationSpec = spring(dampingRatio = 0.72f, stiffness = 420f),
                label = "clock-navigation-indicator"
            )
            val indicatorScale by animateFloatAsState(
                targetValue = if (detailMode) 0.92f else 1f,
                animationSpec = spring(dampingRatio = 0.7f, stiffness = 440f),
                label = "dock-context-scale"
            )
            AnimatedVisibility(
                visible = !detailMode,
                enter = fadeIn(tween(180)) + scaleIn(initialScale = 0.96f),
                exit = fadeOut(tween(100)) + scaleOut(targetScale = 0.96f)
            ) {
                Box(
                    modifier = Modifier
                        .offset(x = indicatorX)
                        .width(itemWidth)
                        .fillMaxHeight()
                        .padding(horizontal = 4.dp, vertical = 5.dp)
                        .graphicsLayer { scaleX = indicatorScale; scaleY = indicatorScale }
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {}
                }
            }
            AnimatedContent(
                targetState = detailMode,
                modifier = Modifier.fillMaxSize(),
                transitionSpec = {
                    (fadeIn(tween(190)) + scaleIn(initialScale = 0.96f)) togetherWith
                        (fadeOut(tween(120)) + scaleOut(targetScale = 0.96f))
                },
                label = "dock-content-morph"
            ) { detail ->
                if (detail) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 7.dp),
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DockAction(
                            modifier = Modifier.weight(1f),
                            icon = Icons.AutoMirrored.Rounded.Reply,
                            label = "Reply",
                            onClick = onReply
                        )
                        DockAction(
                            modifier = Modifier.weight(1f),
                            icon = Icons.AutoMirrored.Rounded.Forward,
                            label = "Forward",
                            onClick = onForward
                        )
                        DockAction(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Rounded.Archive,
                            label = "Archive",
                            onClick = onArchive
                        )
                    }
                } else {
                    Row(Modifier.fillMaxSize()) {
                        entries.forEach { entry ->
                            DockItem(
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                entry = entry,
                                selected = entry == destination,
                                onClick = { onDestination(entry) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DockItem(
    modifier: Modifier,
    entry: MailDestination,
    selected: Boolean,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.84f else if (selected) 1.04f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 520f),
        label = "dock-icon-touch"
    )
    Column(
        modifier = modifier.clickable(
            interactionSource = interaction,
            indication = LocalIndication.current,
            onClick = onClick
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            entry.icon,
            contentDescription = entry.title,
            modifier = Modifier.graphicsLayer { scaleX = scale; scaleY = scale },
            tint = if (selected) MaterialTheme.colorScheme.onSecondaryContainer
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            entry.title,
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
private fun DockAction(
    modifier: Modifier,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.93f else 1f,
        animationSpec = spring(dampingRatio = 0.62f, stiffness = 520f),
        label = "dock-action-touch"
    )
    Surface(
        modifier = modifier
            .fillMaxHeight()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(19.dp))
            .clickable(
                interactionSource = source,
                indication = LocalIndication.current,
                onClick = onClick
            ),
        shape = RoundedCornerShape(19.dp),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.onSecondaryContainer)
            Text(
                label,
                modifier = Modifier.padding(top = 2.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun SettingsPage(
    dark: Boolean,
    dynamic: Boolean,
    notifications: Boolean,
    onDark: (Boolean) -> Unit,
    onDynamic: (Boolean) -> Unit,
    onNotifications: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 145.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SettingsPanel("Appearance", "A palette that belongs to your device") {
            SettingsSwitch("Dynamic colour", "Use your wallpaper palette", dynamic, onDynamic) {
                Icon(Icons.Rounded.Palette, contentDescription = null)
            }
            SettingsSwitch("Dark theme", "Use a darker surface system", dark, onDark) {
                Icon(Icons.Rounded.Settings, contentDescription = null)
            }
        }
        SettingsPanel("Notifications", "Choose how mail reaches you") {
            SettingsSwitch("New message alerts", "Show notifications for new conversations", notifications, onNotifications) {
                Icon(Icons.Rounded.Notifications, contentDescription = null)
            }
        }
        Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surfaceContainerLow) {
            Column(Modifier.padding(18.dp)) {
                Text("MonoMail · Clock Edition", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    "A standalone interface experiment. Sample messages only; no account is connected.",
                    modifier = Modifier.padding(top = 5.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SettingsPanel(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Surface(shape = RoundedCornerShape(26.dp), color = MaterialTheme.colorScheme.surfaceContainerLow) {
        Column {
            Column(Modifier.padding(start = 18.dp, end = 18.dp, top = 17.dp, bottom = 5.dp)) {
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Text(subtitle, modifier = Modifier.padding(top = 3.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            content()
        }
    }
}

@Composable
private fun SettingsSwitch(
    title: String,
    subtitle: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
    icon: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(modifier = Modifier.size(40.dp), shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer) {
            Box(contentAlignment = Alignment.Center) { icon() }
        }
        Column(Modifier.weight(1f).padding(start = 12.dp, end = 8.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun mailAccent(index: Int): Color {
    val scheme = MaterialTheme.colorScheme
    return when (index % 4) {
        0 -> scheme.primaryContainer
        1 -> scheme.secondaryContainer
        2 -> scheme.tertiaryContainer
        else -> scheme.surfaceVariant
    }
}

private fun sampleMail() = listOf(
    MailItem(
        1, "Google Account Security", "G", "Security alert · Sign-in from Pixel 8a",
        "A new sign-in was detected on a device you use.", "Today", "7:21 AM",
        false, true, 0,
        "Hi Sumit,\n\nYour Google Account was just signed in to from a Pixel 8a. If this was you, you can safely ignore this message. If not, review your recent security activity.\n\nThis is sample mail content for the Clock-inspired MonoMail experience."
    ),
    MailItem(
        2, "MonoMail Design Notes", "M", "The expressive navigation study",
        "A calmer interface, with motion that gives each action a sense of weight.", "Today", "6:48 AM",
        false, false, 1,
        "The new design uses the same expressive language across the entire mail experience: large readable type, distinct active states, tactile controls, and motion that preserves spatial continuity."
    ),
    MailItem(
        3, "Google Photos", "P", "Your weekly memories are ready",
        "A few moments from this week, collected in one place.", "Yesterday", "8:12 PM",
        true, true, 2,
        "Your week in photos is ready. Open the collection to revisit the moments you saved."
    ),
    MailItem(
        4, "Calendar", "C", "Tomorrow · Design review",
        "10:30 AM · Motion and interaction review", "Yesterday", "4:35 PM",
        true, false, 3,
        "You have a design review scheduled for tomorrow at 10:30 AM. Bring the latest navigation prototype.",
        folder = "Work"
    ),
    MailItem(
        5, "GitHub", "GH", "The standalone build is ready",
        "A new build was generated for the Clock-inspired mail interface.", "Nov 18", "11:02 AM",
        true, false, 1,
        "The standalone Clock Mail build completed. This is placeholder content for the app layout.",
        folder = "Work",
        attachment = "clock-mail-expressive-debug.apk"
    ),
    MailItem(
        6, "Travel Desk", "T", "Your hotel confirmation",
        "Your booking details are ready when you need them.", "Nov 17", "9:40 AM",
        true, false, 2,
        "Your stay is confirmed. This message is snoozed as a placeholder for the mail interaction."
    ),
    MailItem(
        7, "Notion", "N", "A quieter way to work",
        "Your saved notes and recent pages are ready.", "Nov 16", "7:16 PM",
        true, false, 0,
        "A small collection of notes and ideas, ready whenever you need them."
    ),
    MailItem(
        8, "Design Team", "D", "Prototype feedback",
        "The updated cards feel clearer and the selected states are easier to scan.", "Nov 15", "2:18 PM",
        true, true, 3,
        "The motion should remain intentional and consistent across the app."
    ),
    MailItem(
        9, "Travel Desk", "T", "Boarding pass and itinerary",
        "A reminder for your upcoming trip.", "Nov 14", "9:00 AM",
        true, false, 2,
        "Your travel information is saved here.",
        folder = "Travel",
        snoozedUntil = "Tomorrow, 8:00 AM"
    ),
    MailItem(
        10, "Receipt Centre", "R", "Your order receipt",
        "The receipt for your recent order is attached.", "Nov 13", "1:22 PM",
        true, false, 1,
        "Thank you for your order.",
        folder = "Receipts"
    ),
    MailItem(
        11, "Studio Updates", "S", "Project handoff",
        "The final files are ready for review.", "Nov 12", "10:14 AM",
        true, false, 0,
        "The project handoff package is available.",
        sent = true,
        folder = "Work"
    )
)
