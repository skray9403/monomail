package com.shrivatsav.monomail.dockdemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Forward
import androidx.compose.material.icons.automirrored.rounded.Reply
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.Drafts
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.ui.text.style.TextAlign

private const val CURRENT_USER_EMAIL = "me@monomail.demo"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MonoMailDemoTheme {
                MonoMailImprovementsDemo()
            }
        }
    }
}

private enum class DemoFolder(val title: String, val icon: ImageVector, val index: Int) {
    INBOX("Inbox", Icons.Rounded.Inbox, 0),
    SENT("Sent", Icons.AutoMirrored.Rounded.Send, 1),
    ARCHIVE("Archive", Icons.Rounded.Archive, 2)
}

internal data class DemoMessage(
    val fromName: String,
    val fromEmail: String,
    val toEmail: String,
    val dateLabel: String,
    val sentAt: Long,
    val body: String
)

private data class DemoThread(
    val id: String,
    val subject: String,
    val preview: String,
    val folder: DemoFolder,
    val unread: Boolean,
    val messages: List<DemoMessage>
) {
    val latestMessage: DemoMessage get() = messages.maxBy { it.sentAt }
    val senderSummary: String get() = messages.map { it.fromName }.distinct().joinToString(", ")
}

internal fun resolveReplyTarget(messages: List<DemoMessage>, currentUserEmail: String): String? =
    messages
        .asSequence()
        .filterNot { it.fromEmail.equals(currentUserEmail, ignoreCase = true) }
        .maxByOrNull { it.sentAt }
        ?.fromEmail
        ?.takeIf { it.isNotBlank() && !it.equals(currentUserEmail, ignoreCase = true) }

private val demoThreads = listOf(
    DemoThread(
        id = "launch",
        subject = "Launch checklist and final review",
        preview = "I sent the updated checklist after our call...",
        folder = DemoFolder.INBOX,
        unread = true,
        messages = listOf(
            DemoMessage("Alex Morgan", "alex@northstar.io", CURRENT_USER_EMAIL, "9:12 AM", 1L,
                "The revised launch checklist is ready. I moved the analytics review ahead of the press preview and added the two sign-off items we discussed."),
            DemoMessage("You", CURRENT_USER_EMAIL, "alex@northstar.io", "9:38 AM", 2L,
                "Looks good. I have added the QA owner and the fallback plan. I will send the final copy after the afternoon review."),
            DemoMessage("Alex Morgan", "alex@northstar.io", CURRENT_USER_EMAIL, "10:04 AM", 3L,
                "Perfect. One small change: can we keep the release notes in the same document? That will make the hand-off easier."),
            DemoMessage("You", CURRENT_USER_EMAIL, "alex@northstar.io", "10:26 AM", 4L,
                "Done. The release notes are now in the same document, and the checklist is ready for sign-off.")
        )
    ),
    DemoThread(
        id = "design",
        subject = "Updated component spec",
        preview = "The new spacing tokens are attached below...",
        folder = DemoFolder.INBOX,
        unread = false,
        messages = listOf(
            DemoMessage("Maya Chen", "maya@studio.example", CURRENT_USER_EMAIL, "Yesterday", 5L,
                "I have updated the component spec with the smaller corner radius and the revised type scale. The interaction notes are included at the end.")
        )
    ),
    DemoThread(
        id = "notes",
        subject = "Notes from Thursday",
        preview = "Sharing the summary and next steps...",
        folder = DemoFolder.INBOX,
        unread = false,
        messages = listOf(
            DemoMessage("Jordan Lee", "jordan@fieldnotes.example", CURRENT_USER_EMAIL, "Wed", 6L,
                "Here is the short summary from Thursday. The next review is on Tuesday, and I have listed the owners beside each action.")
        )
    ),
    DemoThread(
        id = "sent",
        subject = "Re: Quarterly planning",
        preview = "I have added the revised milestones...",
        folder = DemoFolder.SENT,
        unread = false,
        messages = listOf(
            DemoMessage("You", CURRENT_USER_EMAIL, "sam@work.example", "Yesterday", 7L,
                "I have added the revised milestones and moved the risk review to the first week.")
        )
    ),
    DemoThread(
        id = "archive",
        subject = "Travel details",
        preview = "Your itinerary is confirmed...",
        folder = DemoFolder.ARCHIVE,
        unread = false,
        messages = listOf(
            DemoMessage("Casey Rivera", "casey@travel.example", CURRENT_USER_EMAIL, "Mon", 8L,
                "Your itinerary is confirmed. The booking references and hotel address are in the attached summary.")
        )
    )
)

@Composable
private fun MonoMailDemoTheme(content: @Composable () -> Unit) {
    val colors = if (androidx.compose.foundation.isSystemInDarkTheme()) {
        darkColorScheme(
            primary = Color(0xFFD0BCFF),
            primaryContainer = Color(0xFF4F378B),
            onPrimaryContainer = Color(0xFFEADDFF),
            background = Color(0xFF141218),
            surface = Color(0xFF141218),
            surfaceContainerLow = Color(0xFF211E26),
            surfaceContainerHigh = Color(0xFF302C36),
            outlineVariant = Color(0xFF494451)
        )
    } else {
        lightColorScheme(
            primary = Color(0xFF6250A4),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFE9DFFF),
            onPrimaryContainer = Color(0xFF281653),
            secondaryContainer = Color(0xFFE9E2F1),
            background = Color(0xFFFAF8FC),
            surface = Color(0xFFFAF8FC),
            surfaceContainerLow = Color(0xFFF3F0F7),
            surfaceContainerHigh = Color(0xFFECE7F1),
            outlineVariant = Color(0xFFDCD5E4)
        )
    }
    MaterialTheme(colorScheme = colors, content = content)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MonoMailImprovementsDemo() {
    var folder by rememberSaveable { mutableStateOf(DemoFolder.INBOX) }
    var selectedThreadId by rememberSaveable { mutableStateOf<String?>(null) }
    var isComposing by rememberSaveable { mutableStateOf(false) }
    var composeTo by rememberSaveable { mutableStateOf("") }
    var composeSubject by rememberSaveable { mutableStateOf("") }
    var composeBody by rememberSaveable { mutableStateOf("") }
    var composeNotice by rememberSaveable { mutableStateOf("") }
    var detailTitleCollapsed by rememberSaveable { mutableStateOf(false) }
    var isStarred by rememberSaveable { mutableStateOf(false) }
    var attachmentAdded by rememberSaveable { mutableStateOf(false) }

    val selectedThread = demoThreads.firstOrNull { it.id == selectedThreadId }
    val visibleThreads = demoThreads.filter { it.folder == folder }.sortedByDescending { it.latestMessage.sentAt }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val isWideLayout = maxWidth >= 840.dp

        LaunchedEffect(selectedThreadId) {
            detailTitleCollapsed = false
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        val title = when {
                            isComposing -> "New message"
                            selectedThread != null && detailTitleCollapsed -> selectedThread.subject
                            selectedThread != null && isWideLayout -> ""
                            selectedThread != null -> ""
                            else -> folder.title
                        }
                        Text(
                            text = title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    navigationIcon = {
                        if (isComposing || (selectedThread != null && !isWideLayout)) {
                            IconButton(
                                onClick = {
                                    if (isComposing) {
                                        isComposing = false
                                        composeNotice = ""
                                    } else {
                                        selectedThreadId = null
                                        detailTitleCollapsed = false
                                    }
                                }
                            ) {
                                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                            }
                        }
                    },
                    actions = {
                        if (selectedThread != null && !isComposing) {
                            IconButton(onClick = { isStarred = !isStarred }) {
                                Icon(
                                    imageVector = if (isStarred) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                                    contentDescription = if (isStarred) "Remove star" else "Star conversation"
                                )
                            }
                        }
                    }
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { scaffoldPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(scaffoldPadding)
            ) {
                when {
                    isComposing -> {
                        ComposerScreen(
                            to = composeTo,
                            onToChange = { composeTo = it },
                            subject = composeSubject,
                            onSubjectChange = { composeSubject = it },
                            body = composeBody,
                            onBodyChange = { composeBody = it },
                            notice = composeNotice,
                            attachmentAdded = attachmentAdded,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    isWideLayout -> {
                        Row(Modifier.fillMaxSize()) {
                            InboxScreen(
                                folder = folder,
                                threads = visibleThreads,
                                selectedThreadId = selectedThreadId,
                                onOpenThread = { selectedThreadId = it },
                                modifier = Modifier.width(360.dp).fillMaxHeight()
                            )
                            VerticalDivider(
                                modifier = Modifier.fillMaxHeight().width(1.dp),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                            Box(Modifier.weight(1f).fillMaxHeight()) {
                                if (selectedThread == null) {
                                    EmptyDetailPane()
                                } else {
                                    ThreadScreen(
                                        thread = selectedThread,
                                        onCollapsedChange = { detailTitleCollapsed = it },
                                        modifier = Modifier.fillMaxSize(),
                                        showReplyDebug = true
                                    )
                                }
                            }
                        }
                    }
                    selectedThread != null -> {
                        ThreadScreen(
                            thread = selectedThread,
                            onCollapsedChange = { detailTitleCollapsed = it },
                            modifier = Modifier.fillMaxSize(),
                            showReplyDebug = true
                        )
                    }
                    else -> {
                        InboxScreen(
                            folder = folder,
                            threads = visibleThreads,
                            selectedThreadId = null,
                            onOpenThread = { selectedThreadId = it },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                if (isComposing) {
                    DemoDock(
                        composing = true,
                        onDrafts = { composeNotice = "Draft saved locally in this demo." },
                        onAttach = { attachmentAdded = !attachmentAdded },
                        onSend = {
                            composeNotice = "Demo only: this message has not been sent."
                        },
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                } else if (selectedThread != null) {
                    DemoDock(
                        threadMode = true,
                        onReply = {
                            composeTo = resolveReplyTarget(selectedThread.messages, CURRENT_USER_EMAIL).orEmpty()
                            composeSubject = "Re: ${selectedThread.subject}"
                            composeBody = ""
                            composeNotice = ""
                            attachmentAdded = false
                            isComposing = true
                        },
                        onForward = {
                            composeTo = ""
                            composeSubject = "Fwd: ${selectedThread.subject}"
                            composeBody = selectedThread.messages.lastOrNull()?.body.orEmpty()
                            composeNotice = "Forward draft placeholder"
                            attachmentAdded = false
                            isComposing = true
                        },
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                } else {
                    DemoDock(
                        selectedFolder = folder,
                        onFolderSelected = {
                            folder = it
                            selectedThreadId = null
                            detailTitleCollapsed = false
                        },
                        onCompose = {
                            composeTo = ""
                            composeSubject = ""
                            composeBody = ""
                            composeNotice = ""
                            attachmentAdded = false
                            isComposing = true
                        },
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        }
    }
}

@Composable
private fun InboxScreen(
    folder: DemoFolder,
    threads: List<DemoThread>,
    selectedThreadId: String?,
    onOpenThread: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Column(Modifier.padding(start = 4.dp, bottom = 8.dp)) {
                Text(folder.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    text = when (folder) {
                        DemoFolder.INBOX -> "A quieter place for your conversations"
                        DemoFolder.SENT -> "Messages you have sent"
                        DemoFolder.ARCHIVE -> "Out of the inbox, still within reach"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (threads.isEmpty()) {
            item { Text("Nothing here yet.", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(20.dp)) }
        } else {
            items(threads, key = { it.id }) { thread ->
                ThreadListItem(
                    thread = thread,
                    selected = selectedThreadId == thread.id,
                    onClick = { onOpenThread(thread.id) }
                )
            }
        }
    }
}

@Composable
private fun ThreadListItem(thread: DemoThread, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        color = when {
            selected -> MaterialTheme.colorScheme.secondaryContainer
            thread.unread -> MaterialTheme.colorScheme.surfaceContainerLow
            else -> MaterialTheme.colorScheme.surface
        },
        border = if (selected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)) else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 15.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Avatar(name = thread.senderSummary, size = 42.dp)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        thread.senderSummary,
                        modifier = Modifier.weight(1f),
                        fontWeight = if (thread.unread) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(thread.latestMessage.dateLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(
                    thread.subject,
                    fontWeight = if (thread.unread) FontWeight.SemiBold else FontWeight.Normal,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    thread.preview,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (thread.unread) {
                Box(
                    Modifier
                        .padding(top = 7.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}

@Composable
private fun ThreadScreen(
    thread: DemoThread,
    onCollapsedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    showReplyDebug: Boolean
) {
    val listState = rememberLazyListState()
    val isCollapsed by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 96 }
    }
    LaunchedEffect(thread.id) {
        listState.scrollToItem(0)
    }
    LaunchedEffect(isCollapsed, thread.id) {
        onCollapsedChange(isCollapsed)
    }

    LazyColumn(
        state = listState,
        modifier = modifier,
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 116.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(start = 2.dp, end = 2.dp, bottom = 6.dp)
            ) {
                Text(thread.subject, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    DemoTag(text = "${thread.messages.size} messages")
                    DemoTag(text = "Inbox")
                }
                if (showReplyDebug) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "Reply recipient  ·  ${resolveReplyTarget(thread.messages, CURRENT_USER_EMAIL) ?: "not available"}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        items(thread.messages.sortedBy { it.sentAt }, key = { "${thread.id}-${it.sentAt}" }) { message ->
            MessageCard(message)
        }
        item {
            Text(
                "You’re all caught up in this conversation.",
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 12.dp),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun MessageCard(message: DemoMessage) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Avatar(message.fromName, size = 38.dp)
                Column(Modifier.weight(1f)) {
                    Text(message.fromName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text(message.fromEmail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Text(message.dateLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f))
            Text(message.body, style = MaterialTheme.typography.bodyLarge, lineHeight = 24.sp)
        }
    }
}

@Composable
private fun ComposerScreen(
    to: String,
    onToChange: (String) -> Unit,
    subject: String,
    onSubjectChange: (String) -> Unit,
    body: String,
    onBodyChange: (String) -> Unit,
    notice: String,
    attachmentAdded: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Compose", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
        Text("A local interaction preview. No email account or network connection is used.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        OutlinedTextField(
            value = to,
            onValueChange = onToChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("To") },
            singleLine = true,
            shape = RoundedCornerShape(18.dp)
        )
        OutlinedTextField(
            value = subject,
            onValueChange = onSubjectChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Subject") },
            singleLine = true,
            shape = RoundedCornerShape(18.dp)
        )
        OutlinedTextField(
            value = body,
            onValueChange = onBodyChange,
            modifier = Modifier.fillMaxWidth().heightIn(min = 220.dp),
            label = { Text("Message") },
            shape = RoundedCornerShape(18.dp)
        )
        if (attachmentAdded) {
            DemoTag(text = "Attachment placeholder added")
        }
        if (notice.isNotBlank()) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(notice, modifier = Modifier.padding(14.dp), color = MaterialTheme.colorScheme.onSecondaryContainer)
            }
        }
        Spacer(Modifier.height(90.dp))
    }
}

@Composable
private fun EmptyDetailPane() {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.size(76.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.Inbox, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(32.dp))
            }
        }
        Spacer(Modifier.height(18.dp))
        Text("Choose a conversation", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Text(
            "The reading pane and contextual dock will update together.",
            modifier = Modifier.padding(top = 6.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun DemoDock(
    modifier: Modifier = Modifier,
    selectedFolder: DemoFolder = DemoFolder.INBOX,
    threadMode: Boolean = false,
    composing: Boolean = false,
    onFolderSelected: (DemoFolder) -> Unit = {},
    onCompose: () -> Unit = {},
    onReply: () -> Unit = {},
    onForward: () -> Unit = {},
    onDrafts: () -> Unit = {},
    onAttach: () -> Unit = {},
    onSend: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 12.dp, end = 12.dp, bottom = 10.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        AnimatedContent(
            targetState = when {
                composing -> "compose"
                threadMode -> "thread"
                else -> "inbox"
            },
            transitionSpec = { fadeIn(tween(150)).togetherWith(fadeOut(tween(110))) },
            label = "contextualDock"
        ) { mode ->
            when (mode) {
                "thread" -> Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ActionPill("Reply", Icons.AutoMirrored.Rounded.Reply, onReply)
                    ActionPill("Forward", Icons.AutoMirrored.Rounded.Forward, onForward)
                }
                "compose" -> Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ActionPill("Drafts", Icons.Rounded.Drafts, onDrafts)
                    ActionPill("Attach", Icons.Rounded.AttachFile, onAttach)
                    ActionPill("Send", Icons.AutoMirrored.Rounded.Send, onSend, emphasized = true)
                }
                else -> Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FolderDock(selectedFolder = selectedFolder, onSelected = onFolderSelected)
                    Surface(
                        modifier = Modifier.size(56.dp).clickable(onClick = onCompose),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shadowElevation = 6.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Rounded.Edit, contentDescription = "Compose email", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FolderDock(selectedFolder: DemoFolder, onSelected: (DemoFolder) -> Unit) {
    val itemWidth = 76.dp
    val indicatorOffset by animateDpAsState(
        targetValue = itemWidth * selectedFolder.index + 4.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "dockSelectionOffset"
    )
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shadowElevation = 6.dp
    ) {
        Box(Modifier.width(itemWidth * 3).height(56.dp)) {
            Surface(
                modifier = Modifier.offset(x = indicatorOffset).padding(4.dp).width(itemWidth - 8.dp).height(48.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {}
            Row(Modifier.fillMaxSize()) {
                DemoFolder.entries.forEach { item ->
                    val selected = item == selectedFolder
                    Box(
                        modifier = Modifier
                            .width(itemWidth)
                            .fillMaxHeight()
                            .selectable(selected = selected, onClick = { onSelected(item) }, role = Role.Tab)
                            .semantics { contentDescription = "${item.title} tab" },
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedContent(
                            targetState = selected,
                            transitionSpec = { fadeIn(tween(130)).togetherWith(fadeOut(tween(90))) },
                            label = "tabContent-${item.name}"
                        ) { isSelected ->
                            if (isSelected) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Icon(item.icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                    Text(item.title, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimaryContainer, maxLines = 1)
                                }
                            } else {
                                Icon(item.icon, contentDescription = null, modifier = Modifier.size(21.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionPill(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    emphasized: Boolean = false
) {
    Surface(
        modifier = Modifier
            .height(56.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        shape = CircleShape,
        color = if (emphasized) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (emphasized) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )
            Text(
                label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (emphasized) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun Avatar(name: String, size: Dp) {
    val initials = name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("")
    Surface(
        modifier = Modifier.size(size),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.tertiaryContainer
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(initials, color = MaterialTheme.colorScheme.onTertiaryContainer, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun DemoTag(text: String) {
    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}
