package com.shrivatsav.monomail.feature.inbox

import android.text.format.DateFormat as AndroidDateFormat

import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.asPaddingValues
import com.shrivatsav.monomail.ui.theme.cornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextButton
import com.shrivatsav.monomail.feature.inbox.components.*

import com.shrivatsav.monomail.ui.components.AvatarCircle
import com.shrivatsav.monomail.ui.components.TooltipIconButton
import com.shrivatsav.monomail.model.InboxTab
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import com.shrivatsav.monomail.feature.auth.R as AuthR
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import com.shrivatsav.monomail.core.data.auth.UserProfile
import com.shrivatsav.monomail.data.model.EmailThread
import kotlinx.coroutines.launch
import com.shrivatsav.monomail.core.data.repository.BodyBackfillState
import com.shrivatsav.monomail.core.data.repository.BodyDownloadStats

data class InboxNavActions(
    val onEmailClick: (String, String, String?) -> Unit,
    val onSignOut: () -> Unit,
    val onCompose: () -> Unit = {},
    val onSettings: () -> Unit = {},
    val onAddAccount: () -> Unit = {},
    val onScheduledClick: () -> Unit = {},
    val onNavigateToImapSetup: (String?, String?) -> Unit = { _, _ -> }
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun InboxScreen(
    viewModel: InboxViewModel,
    userProfile: UserProfile?,
    navActions: InboxNavActions
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val unifiedInboxEnabled by viewModel.unifiedInboxEnabled.collectAsStateWithLifecycle()
    val showWelcomePrompt by viewModel.showWelcomePrompt.collectAsStateWithLifecycle()
    val scheduledCount by viewModel.scheduledCount.collectAsStateWithLifecycle()
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val syncState by viewModel.inboxSyncState.collectAsStateWithLifecycle()
    val immediateTab by viewModel.currentTab.collectAsStateWithLifecycle()
    var showBackfillDialog by remember { mutableStateOf(false) }

    val lifecycle = androidx.lifecycle.compose.LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            when (event) {
                androidx.lifecycle.Lifecycle.Event.ON_RESUME -> viewModel.setForegroundPollingEnabled(true)
                androidx.lifecycle.Lifecycle.Event.ON_PAUSE -> viewModel.setForegroundPollingEnabled(false)
                else -> {}
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            viewModel.setForegroundPollingEnabled(false)
            lifecycle.removeObserver(observer)
        }
    }

    var threadToDelete by remember { mutableStateOf<EmailThread?>(null) }
    val appSettings by viewModel.appSettingsState.collectAsStateWithLifecycle()
    val is24HourTime = AndroidDateFormat.is24HourFormat(LocalContext.current)
    val searchFilters by viewModel.searchFilters.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()

    val pullToRefreshState = rememberPullToRefreshState()
    val currentThreads = (state as? InboxState.Success)?.threads ?: emptyList()
    val hapticFeedback = LocalHapticFeedback.current
    val focusManager = LocalFocusManager.current
    var longPressedThread by remember { mutableStateOf<EmailThread?>(null) }
    var activeModal by remember { mutableStateOf<ModalType?>(null) }
    var showSnoozePicker by remember { mutableStateOf(false) }
    var snoozeThread by remember { mutableStateOf<EmailThread?>(null) }
    var showPerformanceWarningDialog by remember { mutableStateOf(false) }
    var showMoveToTrashDialog by remember { mutableStateOf(false) }

    val onSnoozeSelected: (EmailThread) -> Unit = remember { { thread -> snoozeThread = thread; showSnoozePicker = true } }
    val isBulkMode by viewModel.isBulkSelectMode.collectAsStateWithLifecycle()
    val selectedThreadIds by viewModel.selectedThreadIds.collectAsStateWithLifecycle()
    val selectedCount by viewModel.selectedCount.collectAsStateWithLifecycle()
    var isSearchFocused by remember { mutableStateOf(false) }
    val isSearchActive = searchFilters.query.isNotEmpty()
    if (isSearchActive) {
        BackHandler {
            viewModel.clearSearch()
        }
    } else if (isSearchFocused) {
        BackHandler {
            focusManager.clearFocus()
        }
    } else if (isBulkMode) {
        BackHandler { viewModel.exitBulkSelectMode() }
    } else if (immediateTab != InboxTab.INBOX) {
        BackHandler { viewModel.switchTab(InboxTab.INBOX) }
    }

    LaunchedEffect(isSearchActive) {
        if (!isSearchActive) {
            focusManager.clearFocus()
            kotlinx.coroutines.delay(100)
            focusManager.clearFocus()
        }
    }

    val isRefreshing = (state as? InboxState.Success)?.isRefreshing == true
    val navBarHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(viewModel) {
        viewModel.uiError.collect { errorMsg ->
            snackbarHostState.showSnackbar(errorMsg)
        }
    }
    val currentIs24HourTime by rememberUpdatedState(is24HourTime)
    LaunchedEffect(Unit) {
        viewModel.scheduledEmailEvents.collect { event ->
            val pattern = if (currentIs24HourTime) "MMM dd, HH:mm" else "MMM dd, hh:mm a"
            val formatted = java.text.SimpleDateFormat(pattern, java.util.Locale.getDefault())
                .format(java.util.Date(event.scheduledAt))
            snackbarHostState.showSnackbar(
                "Email scheduled for $formatted"
            )
        }
    }


    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) { Snackbar(
            snackbarData = it,
            shape = com.shrivatsav.monomail.ui.theme.cornerShape(12.dp),
            containerColor = MaterialTheme.colorScheme.inverseSurface,
            contentColor = MaterialTheme.colorScheme.inverseOnSurface,
            actionContentColor = MaterialTheme.colorScheme.inverseOnSurface
        ) } },
        contentWindowInsets = WindowInsets(0.dp)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ModalBottomSheet and Material dialogs provide their own scrim.
            // Keep the inbox sharp beneath them; applying RenderEffect blur here
            // made account switching look washed out and added unnecessary GPU work.
            val shouldBlur =
                longPressedThread != null || threadToDelete != null || showWelcomePrompt

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (shouldBlur) Modifier.blur(12.dp) else Modifier)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
                ) {

                    InboxSearchBar(
                        userProfile = userProfile,
                        accounts = accounts,
                        query = searchFilters.query,
                        onQueryChange = { viewModel.updateSearch(searchFilters.copy(query = it)) },
                        onServerSearch = { viewModel.searchServer(it) },
                        onFocusChange = { isSearchFocused = it },
                        actions = SearchBarActions(
                            onMarkAllRead = { viewModel.markAllAsRead() },
                            onScheduledClick = navActions.onScheduledClick,
                            scheduledCount = scheduledCount,
                            onUndo = { viewModel.undoAction() },
                            onOpenProfile = { activeModal = ModalType.PROFILE },
                            onOpenFolders = { activeModal = ModalType.FOLDERS },
                            showMarkAllRead = appSettings.showMarkAllRead
                        ),
                        isRefreshing = isRefreshing,
                        bulkSelection = BulkSelectionState(
                            isBulkMode = isBulkMode,
                            selectedCount = selectedCount,
                            totalCount = if (isSearchActive) searchResults.size else currentThreads.size,
                            onSelectAll = { viewModel.selectAll() },
                            onDeselectAll = { viewModel.deselectAll() },
                            onDone = { viewModel.exitBulkSelectMode() }
                        ),
                        unifiedInboxEnabled = unifiedInboxEnabled
                    )

                    SearchFilterBar(
                        filters = searchFilters,
                        onFiltersChanged = { viewModel.updateSearch(it) },
                        visible = isSearchActive
                    )

                    val isOnline = rememberConnectivityState()
                    AnimatedVisibility(
                        visible = !isOnline,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        OfflineBanner()
                    }
                    AnimatedVisibility(
                        visible = syncState.backfillError != null,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        BackfillErrorBanner(errorMessage = syncState.backfillError ?: "")
                    }
                    AnimatedVisibility(
                        visible = syncState.isBackfilling && syncState.backfillError == null,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        BackfillProgressBanner(
                            progress = syncState.backfillProgress,
                            onClick = { showBackfillDialog = true }
                        )
                    }
                    if (showBackfillDialog && syncState.backfillProgress != null) {
                        BackfillProgressDialog(
                            progress = syncState.backfillProgress!!,
                            onCancel = {
                                viewModel.cancelBodyBackfill()
                                showBackfillDialog = false
                            },
                            onDismiss = { showBackfillDialog = false }
                        )
                    }
                    when (val s = state) {
                        is InboxState.Loading -> {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { LoadingIndicator() }
                            }
                        }

                        is InboxState.Error -> {
                            com.shrivatsav.monomail.ui.components.EmptyStateView(
                                illustration = com.shrivatsav.monomail.ui.components.IllustrationType.ERROR_CLOUD,
                                title = "Something went wrong",
                                subtitle = s.message,
                                ctaText = "Retry",
                                onCtaClick = { viewModel.refresh() }
                            )
                        }

                        is InboxState.Success -> {
                            val currentTab = s.currentTab
                            key(currentTab) {
                            val savedPos = remember(currentTab) { viewModel.getSavedScrollPosition(currentTab) }
                            val listState = rememberLazyListState(
                                initialFirstVisibleItemIndex = savedPos.first,
                                initialFirstVisibleItemScrollOffset = savedPos.second
                            )
                            val shouldLoadMore by remember {
                                derivedStateOf {
                                    val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                                    val totalItems = listState.layoutInfo.totalItemsCount
                                    totalItems > 0 && lastVisible >= totalItems - 5
                                }
                            }
                            LaunchedEffect(Unit) {
                                snapshotFlow { shouldLoadMore }.collect { if (it) viewModel.loadMore() }
                            }
                            LaunchedEffect(listState, currentTab) {
                                snapshotFlow {
                                    listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset
                                }.collect { (index, offset) ->
                                    viewModel.saveScrollState(currentTab, index, offset)
                                }
                            }
                            val threadsToDisplay = if (isSearchActive) searchResults else s.threads
                            var expandedGroupsList by androidx.compose.runtime.saveable.rememberSaveable {
                                mutableStateOf(emptyList<String>())
                            }
                            // Inbox tabs contain at most 500 locally cached rows. Building their
                            // lightweight grouping/sort structure synchronously avoids showing
                            // an empty list for a frame while an async calculation catches up.
                            val inboxStructure = remember(
                                threadsToDisplay,
                                appSettings.smartGroupingEnabled,
                                appSettings.smartGroupingRecentOnly,
                                isSearchActive,
                                currentTab
                            ) {
                                val useGrouping = appSettings.smartGroupingEnabled &&
                                    !isSearchActive && currentTab == InboxTab.INBOX
                                computeInboxStructure(
                                    threadsToDisplay,
                                    useGrouping,
                                    appSettings.smartGroupingRecentOnly
                                )
                            }
                            val displayItems = remember(inboxStructure, expandedGroupsList, currentTab) {
                                flattenDisplayItems(inboxStructure, expandedGroupsList.toSet(), tabPrefix = currentTab.name)
                            }
                            val orderedThreadIds by remember(displayItems) {
                                derivedStateOf {
                                    displayItems.mapNotNull { item ->
                                        when (item) {
                                            is InboxDisplayItem.SingleThread -> item.thread.threadId
                                            is InboxDisplayItem.NestedThread -> item.thread.threadId
                                            else -> null
                                        }
                                    }
                                }
                            }
                            val searchFeatherColor = MaterialTheme.colorScheme.background
                            PullToRefreshBox(
                                isRefreshing = s.isRefreshing,
                                onRefresh = { viewModel.refresh() },
                                state = pullToRefreshState,
                                modifier = Modifier
                                    .fillMaxSize()
                                    // Draw over the first rows, not in the gap above them.
                                    // This makes the header blend into scrolling content.
                                    .drawWithContent {
                                        drawContent()
                                        val featherHeight = 30.dp.toPx()
                                        drawRect(
                                            brush = Brush.verticalGradient(
                                                colorStops = arrayOf(
                                                    0.0f to searchFeatherColor.copy(alpha = 0.96f),
                                                    0.45f to searchFeatherColor.copy(alpha = 0.72f),
                                                    1.0f to searchFeatherColor.copy(alpha = 0.0f),
                                                ),
                                                startY = 0f,
                                                endY = featherHeight,
                                            ),
                                            size = Size(size.width, featherHeight),
                                        )
                                    },
                                indicator = {}
                            ) {
                                if (threadsToDisplay.isEmpty()) {
                                    var illustration: com.shrivatsav.monomail.ui.components.IllustrationType = com.shrivatsav.monomail.ui.components.IllustrationType.INBOX_ZERO
                                    var title = ""
                                    var subtitle = ""
                                    var ctaText: String? = null
                                    var onCtaClick: (() -> Unit)? = null
                                    if (isSearchActive) {
                                        illustration = com.shrivatsav.monomail.ui.components.IllustrationType.SEARCH_EMPTY
                                        title = "No results found"
                                        subtitle = "Try searching on the server instead."
                                        ctaText = "Search server"
                                    } else {
                                        // During an active sync an empty tab is
                                        // usually just "not synced yet" — show a
                                        // cloud + "tab is syncing" instead of the
                                        // misleading "You're all caught up".
                                        val tabFolderName = when (currentTab) {
                                            InboxTab.INBOX -> "Inbox"
                                            InboxTab.SENT -> "Sent"
                                            InboxTab.ARCHIVED -> "Archived"
                                            InboxTab.SPAM -> "Spam"
                                            InboxTab.TRASH -> "Trash"
                                            InboxTab.DRAFTS -> "Drafts"
                                            else -> null
                                        }
                                        val tabIsSyncing = when (currentTab) {
                                            // Aggregate tabs cover every folder's sync.
                                            InboxTab.UNIFIED, InboxTab.STARRED, InboxTab.SNOOZED ->
                                                syncState.deepSyncProgress != null || syncState.isBackfilling
                                            else -> tabFolderName != null && (
                                                syncState.deepSyncProgress?.folder == tabFolderName ||
                                                    syncState.backfillProgress?.folder == tabFolderName
                                                )
                                        }
                                        if (tabIsSyncing) {
                                            val tabLabel = when (currentTab) {
                                                InboxTab.INBOX -> "Inbox"
                                                InboxTab.SENT -> "Sent"
                                                InboxTab.ARCHIVED -> "Archived"
                                                InboxTab.SPAM -> "Spam"
                                                InboxTab.TRASH -> "Trash"
                                                InboxTab.DRAFTS -> "Drafts"
                                                InboxTab.STARRED -> "Starred"
                                                InboxTab.SNOOZED -> "Snoozed"
                                                InboxTab.UNIFIED -> "Unified inbox"
                                            }
                                            illustration = com.shrivatsav.monomail.ui.components.IllustrationType.CLOUD_SYNC
                                            title = "$tabLabel tab is syncing"
                                            subtitle = "Please wait until it completes."
                                            ctaText = null
                                            onCtaClick = null
                                        } else {
                                        when (currentTab) {
                                            InboxTab.SENT -> {
                                                illustration = com.shrivatsav.monomail.ui.components.IllustrationType.PAPER_PLANE
                                                title = "Nothing sent yet"
                                                subtitle = "Your sent emails will appear here."
                                                ctaText = null
                                                onCtaClick = null
                                            }
                                            InboxTab.STARRED -> {
                                                illustration = com.shrivatsav.monomail.ui.components.IllustrationType.INBOX_ZERO
                                                title = "No starred emails"
                                                subtitle = "Star important emails to find them here."
                                                ctaText = null
                                                onCtaClick = null
                                            }
                                            InboxTab.TRASH, InboxTab.SPAM -> {
                                                illustration = com.shrivatsav.monomail.ui.components.IllustrationType.INBOX_ZERO
                                                title = "Empty"
                                                subtitle = "No messages here."
                                                ctaText = null
                                                onCtaClick = null
                                            }
                                            InboxTab.ARCHIVED -> {
                                                illustration = com.shrivatsav.monomail.ui.components.IllustrationType.INBOX_ZERO
                                                title = "No archived emails"
                                                subtitle = "Emails you archive will appear here."
                                                ctaText = null
                                                onCtaClick = null
                                            }
                                            InboxTab.SNOOZED -> {
                                                illustration = com.shrivatsav.monomail.ui.components.IllustrationType.INBOX_ZERO
                                                title = "Nothing snoozed"
                                                subtitle = "Snoozed emails will reappear when ready."
                                                ctaText = null
                                                onCtaClick = null
                                            }
                                            InboxTab.UNIFIED -> {
                                                illustration = com.shrivatsav.monomail.ui.components.IllustrationType.INBOX_ZERO
                                                title = "You're all caught up"
                                                subtitle = "No emails across all your accounts."
                                                ctaText = null
                                                onCtaClick = null
                                            }
                                            else -> {
                                                illustration = com.shrivatsav.monomail.ui.components.IllustrationType.INBOX_ZERO
                                                title = "You're all caught up"
                                                subtitle = "Your inbox is empty."
                                                ctaText = null
                                                onCtaClick = null
                                            }
                                        }
                                    }
                                    }
                                    com.shrivatsav.monomail.ui.components.EmptyStateView(
                                        illustration = illustration,
                                        title = title,
                                        subtitle = subtitle,
                                        ctaText = ctaText,
                                        onCtaClick = onCtaClick
                                    )
                                } else {
                                    val itemKeyFn = { it: InboxDisplayItem -> it.key }
                                    LazyColumn(
                                        state = listState,
                                        modifier = Modifier
                                            .fillMaxSize(),
                                        contentPadding = PaddingValues(bottom = navBarHeight + 112.dp)
                                    ) {
                                        itemsIndexed(
                                            items = displayItems,
                                            key = { _, item -> itemKeyFn(item) },
                                            contentType = { _, item ->
                                                when (item) {
                                                    is InboxDisplayItem.DateHeader -> "date_header"
                                                    is InboxDisplayItem.GroupHeader -> "group_header"
                                                    is InboxDisplayItem.SingleThread -> "email_thread"
                                                    is InboxDisplayItem.NestedThread -> "nested_email_thread"
                                                }
                                            }
                                        ) { index, displayItem ->
                                            when (displayItem) {
                                                is InboxDisplayItem.DateHeader -> {
                                                    com.shrivatsav.monomail.ui.components.SectionHeader(
                                                        label = displayItem.title,
                                                        showTopDivider = false,
                                                        modifier = Modifier
                                                            .animateItem()
                                                            .background(MaterialTheme.colorScheme.background)
                                                    )
                                                }

                                                is InboxDisplayItem.GroupHeader -> {
                                                    GroupHeaderItem(
                                                        modifier = Modifier.animateItem(),
                                                        groupName = displayItem.groupName,
                                                        count = displayItem.count,
                                                        unreadCount = displayItem.unreadCount,
                                                        isExpanded = displayItem.isExpanded,
                                                        onClick = {
                                                            expandedGroupsList =
                                                                if (expandedGroupsList.contains(
                                                                        displayItem.groupName
                                                                    )
                                                                ) {
                                                                    expandedGroupsList - displayItem.groupName
                                                                } else {
                                                                    expandedGroupsList + displayItem.groupName
                                                                }
                                                        }
                                                    )
                                                }
                                                is InboxDisplayItem.SingleThread -> {
                                                    val isUnread = !displayItem.thread.isRead
                                                    val prevIsSingle = index > 0 && displayItems[index - 1] is InboxDisplayItem.SingleThread
                                                    val nextIsSingle = index < displayItems.lastIndex && displayItems[index + 1] is InboxDisplayItem.SingleThread
                                                    val groupPos = when {
                                                        !prevIsSingle && !nextIsSingle -> com.shrivatsav.monomail.feature.inbox.components.GroupPosition.SOLO
                                                        !prevIsSingle -> com.shrivatsav.monomail.feature.inbox.components.GroupPosition.TOP
                                                        !nextIsSingle -> com.shrivatsav.monomail.feature.inbox.components.GroupPosition.BOTTOM
                                                        else -> com.shrivatsav.monomail.feature.inbox.components.GroupPosition.MIDDLE
                                                    }
                                                    val prevUnread = isUnread && index > 0 && displayItems[index - 1].let {
                                                        it is InboxDisplayItem.SingleThread && !it.thread.isRead
                                                    }
                                                    val nextUnread = isUnread && index < displayItems.lastIndex && displayItems[index + 1].let {
                                                        it is InboxDisplayItem.SingleThread && !it.thread.isRead
                                                    }
                                                    val unreadPos = when {
                                                        !isUnread -> com.shrivatsav.monomail.feature.inbox.components.UnreadPosition.SOLO
                                                        prevUnread && nextUnread -> com.shrivatsav.monomail.feature.inbox.components.UnreadPosition.MIDDLE
                                                        prevUnread -> com.shrivatsav.monomail.feature.inbox.components.UnreadPosition.BOTTOM
                                                        nextUnread -> com.shrivatsav.monomail.feature.inbox.components.UnreadPosition.TOP
                                                        else -> com.shrivatsav.monomail.feature.inbox.components.UnreadPosition.SOLO
                                                    }
                                                    SwipeableEmailItem(
                                                        modifier = Modifier.animateItem(),
                                                        thread = displayItem.thread,
                                                        tabForSwipe = currentTab,
                                                        appSettings = appSettings,
                                                        viewModel = viewModel,
                                                        callbacks = SwipeCallbacks(
                                                            onThreadToDeleteChange = { threadToDelete = it },
                                                            onEmailClick = { navActions.onEmailClick(displayItem.thread.accountId, displayItem.thread.threadId, displayItem.thread.latestMessageId) },
                                                            onLongClick = {
                                                                hapticFeedback.performHapticFeedback(
                                                                    HapticFeedbackType.LongPress
                                                                )
                                                                longPressedThread = displayItem.thread
                                                            },
                                                            isNested = false
                                                        ),
                                                        selection = SelectionState(
                                                            isSelected = displayItem.thread.threadId in selectedThreadIds,
                                                            isBulkMode = isBulkMode,
                                                            onSelectToggle = { viewModel.toggleThreadSelection(displayItem.thread.threadId) },
                                                            onRangeSelect = { viewModel.rangeSelectTo(displayItem.thread.threadId, orderedThreadIds) },
                                                            onAvatarLongClick = {
                                                                viewModel.enterBulkSelectMode(displayItem.thread.threadId)
                                                            }
                                                        ),
                                                        unreadPosition = unreadPos,
                                                        groupPosition = groupPos
                                                    )
                                                }

                                                is InboxDisplayItem.NestedThread -> {
                                                    val isUnread = !displayItem.thread.isRead
                                                    val prevIsNested = index > 0 && displayItems[index - 1] is InboxDisplayItem.NestedThread
                                                    val nextIsNested = index < displayItems.lastIndex && displayItems[index + 1] is InboxDisplayItem.NestedThread
                                                    val groupPos = when {
                                                        !prevIsNested && !nextIsNested -> com.shrivatsav.monomail.feature.inbox.components.GroupPosition.SOLO
                                                        !prevIsNested -> com.shrivatsav.monomail.feature.inbox.components.GroupPosition.TOP
                                                        !nextIsNested -> com.shrivatsav.monomail.feature.inbox.components.GroupPosition.BOTTOM
                                                        else -> com.shrivatsav.monomail.feature.inbox.components.GroupPosition.MIDDLE
                                                    }
                                                    val prevUnread = isUnread && index > 0 && displayItems[index - 1].let {
                                                        it is InboxDisplayItem.NestedThread && !it.thread.isRead
                                                    }
                                                    val nextUnread = isUnread && index < displayItems.lastIndex && displayItems[index + 1].let {
                                                        it is InboxDisplayItem.NestedThread && !it.thread.isRead
                                                    }
                                                    val unreadPos = when {
                                                        !isUnread -> com.shrivatsav.monomail.feature.inbox.components.UnreadPosition.SOLO
                                                        prevUnread && nextUnread -> com.shrivatsav.monomail.feature.inbox.components.UnreadPosition.MIDDLE
                                                        prevUnread -> com.shrivatsav.monomail.feature.inbox.components.UnreadPosition.BOTTOM
                                                        nextUnread -> com.shrivatsav.monomail.feature.inbox.components.UnreadPosition.TOP
                                                        else -> com.shrivatsav.monomail.feature.inbox.components.UnreadPosition.SOLO
                                                    }
                                                    Column(modifier = Modifier.animateItem()) {
                                                        AnimatedVisibility(
                                                            visible = displayItem.isVisible,
                                                            enter = expandVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn(),
                                                            exit = shrinkVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeOut()
                                                        ) {
                                                            SwipeableEmailItem(
                                                                modifier = Modifier,
                                                                thread = displayItem.thread,
                                                                tabForSwipe = currentTab,
                                                                appSettings = appSettings,
                                                                viewModel = viewModel,
                                                                callbacks = SwipeCallbacks(
                                                                    onThreadToDeleteChange = { threadToDelete = it },
                                                                    onEmailClick = { navActions.onEmailClick(displayItem.thread.accountId, displayItem.thread.threadId, displayItem.thread.latestMessageId) },
                                                                    onLongClick = {
                                                                        hapticFeedback.performHapticFeedback(
                                                                            HapticFeedbackType.LongPress
                                                                        )
                                                                        longPressedThread = displayItem.thread
                                                                    },
                                                                    isNested = true
                                                                ),
                                                                selection = SelectionState(
                                                                    isSelected = displayItem.thread.threadId in selectedThreadIds,
                                                                    isBulkMode = isBulkMode,
                                                                    onSelectToggle = { viewModel.toggleThreadSelection(displayItem.thread.threadId) },
                                                                    onRangeSelect = { viewModel.rangeSelectTo(displayItem.thread.threadId, orderedThreadIds) },
                                                                    onAvatarLongClick = {
                                                                        viewModel.enterBulkSelectMode(displayItem.thread.threadId)
                                                                    }
                                                                ),
                                                                unreadPosition = unreadPos,
                                                                groupPosition = groupPos
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        
                                        }

                                        if (isSearchActive && s.threads.isNotEmpty()) {
                                            item(key = "search_server_cta") {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(
                                                            horizontal = 24.dp,
                                                            vertical = 28.dp
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    OutlinedButton(
                                                        onClick = { viewModel.searchServer(searchFilters.query) },
                                                        shape = MaterialTheme.shapes.large
                                                    ) { Text("Search server for older emails") }
                                                }
                                            }
                                        }

                                        if (s.isLoadingMore) {
                                            item(key = "loading_more") {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(16.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    CircularProgressIndicator(
                                                        modifier = Modifier.size(24.dp),
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                        strokeWidth = 2.dp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            } // key(currentTab)
                        }
                    }
                }
            }


            // Softly feather the content into the dock area. The brush is a
            // single static draw layer; it performs no blur or bitmap capture.
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(navBarHeight + 124.dp)
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.0f to MaterialTheme.colorScheme.background.copy(alpha = 0.0f),
                                0.58f to MaterialTheme.colorScheme.background.copy(alpha = 0.18f),
                                1.0f to MaterialTheme.colorScheme.background.copy(alpha = 0.92f),
                            )
                        )
                    )
                    // Very subtle side shading, confined to the floating dock zone.
                    .background(
                        Brush.horizontalGradient(
                            colorStops = arrayOf(
                                0.0f to MaterialTheme.colorScheme.onSurface.copy(alpha = 0.035f),
                                0.14f to Color.Transparent,
                                0.86f to Color.Transparent,
                                1.0f to MaterialTheme.colorScheme.onSurface.copy(alpha = 0.035f),
                            )
                        )
                    )
            )

            // Coordinate with the single persistent floating dock outside NavHost
            val dockController = com.shrivatsav.monomail.ui.components.LocalFloatingDockController.current
            val isListScrolling by remember { derivedStateOf { listState.isScrollInProgress } }
            LaunchedEffect(isListScrolling) {
                dockController.isScrolling = isListScrolling
            }
            LaunchedEffect(immediateTab) {
                dockController.mode = com.shrivatsav.monomail.ui.components.DockMode.INBOX
                dockController.selectedTabIndex = when (immediateTab) {
                    InboxTab.SENT -> 1
                    InboxTab.ARCHIVED -> 2
                    else -> 0
                }
            }
            LaunchedEffect(Unit) {
                dockController.onTabSelected = { index ->
                    val tab = when (index) {
                        1 -> InboxTab.SENT
                        2 -> InboxTab.ARCHIVED
                        else -> InboxTab.INBOX
                    }
                    viewModel.switchTab(tab)
                }
            }

            AnimatedVisibility(
                visible = isBulkMode,
                enter = fadeIn(tween(180)) + scaleIn(tween(180), initialScale = 0.9f),
                exit = fadeOut(tween(120)) + scaleOut(tween(120), targetScale = 0.9f),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                BulkActionBar(
                    currentTab = immediateTab,
                    callbacks = BulkActionBarCallbacks(
                        onArchive = { viewModel.bulkArchive() },
                        onDelete = { viewModel.bulkDelete() },
                        onMarkRead = { viewModel.bulkMarkRead() },
                        onMarkUnread = { viewModel.bulkMarkUnread() },
                        onToggleStar = { viewModel.bulkToggleStar() },
                        onUnarchive = { viewModel.bulkUnarchive() },
                        onRestore = { viewModel.bulkRestore() },
                        onReportNotSpam = { viewModel.bulkReportNotSpam() },
                    ),
                    modifier = Modifier.padding(bottom = navBarHeight + 8.dp)
                )
            }

            
            longPressedThread?.let { thread ->
                LongPressMenu(
                    thread = thread,
                    state = state,
                    onDismiss = { longPressedThread = null },
                    actions = LongPressMenuActions(
                        onEmailClick = { navActions.onEmailClick(thread.accountId, thread.threadId, thread.latestMessageId) },
                        onStar = { viewModel.toggleStar(thread.threadId, thread.accountId) },
                        onArchive = { tab -> when (tab) {
                            InboxTab.ARCHIVED -> viewModel.unarchiveThread(thread.threadId, thread.accountId)
                            InboxTab.SPAM -> viewModel.deleteThread(thread.threadId, thread.accountId)
                            else -> viewModel.archiveThread(thread.threadId, thread.accountId)
                        } },
                        onRestore = { viewModel.restoreThread(thread.threadId, thread.accountId) },
                        onToggleRead = {
                            if (thread.isRead) viewModel.markThreadAsUnread(thread.threadId, thread.accountId)
                            else viewModel.markThreadAsRead(thread.threadId, thread.accountId)
                        },
                        onDelete = { threadToDelete = thread },
                        onSnooze = { onSnoozeSelected(thread) },
                        onUnsnooze = { viewModel.unsnoozeThread(thread.threadId, thread.accountId) }
                    )
                )
            }

            ModalOverlay(
                activeModal = activeModal,
                userProfile = userProfile,
                accounts = accounts,
                callbacks = ModalCallbacks(
                    onDismiss = { activeModal = null },
                    onSignOut = { activeModal = null; navActions.onSignOut() },
                    onSwitchAccount = { viewModel.switchAccount(it); activeModal = null },
                    onCycleAccount = { viewModel.switchAccount(it) },
                    onAddAccount = {
                        when {
                            accounts.size >= 10 -> coroutineScope.launch {
                                snackbarHostState.showSnackbar("Maximum limit of 10 accounts reached.")
                            }
                            accounts.size >= 3 && com.shrivatsav.monomail.feature.inbox.BuildConfig.IS_GITHUB_BUILD -> showPerformanceWarningDialog = true
                            else -> activeModal = ModalType.ADD_ACCOUNT
                        }
                    },
                    onShowSwitchAccount = { activeModal = ModalType.SWITCH_ACCOUNT },
                    onBackToProfile = { activeModal = ModalType.PROFILE },
                    onSettings = { activeModal = null; navActions.onSettings() },
                    onSelectFolder = {
                        viewModel.switchTab(it)
                        activeModal = null
                    },
                    onNavigateToImapSetup = navActions.onNavigateToImapSetup,
                    onToggleUnified = { viewModel.setUnifiedInboxEnabled(it) }
                ),
                unifiedInboxEnabled = unifiedInboxEnabled,
                currentTab = immediateTab,
            )
        }
            
            val toastState by viewModel.toastState.collectAsStateWithLifecycle()
            Box {
                TopAlertBanner(
                    toastState = toastState,
                    onUndo = { viewModel.undoAction() }
                )
            }
    }

    // ── Welcome to Monomail modal ────────────────────────────────────

    com.shrivatsav.monomail.ui.components.MaterialDialog(
        visible = showWelcomePrompt,
        onDismiss = { viewModel.dismissWelcomePrompt() }
    ) {
        Surface(
            shape = cornerShape(28.dp),
            color = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground,
            shadowElevation = 32.dp,
            modifier = Modifier.fillMaxWidth(0.88f)
        ) {
            val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
            val context = androidx.compose.ui.platform.LocalContext.current

            val kofiIcon = remember {
                val bmp = android.graphics.BitmapFactory.decodeStream(
                    context.resources.openRawResource(com.shrivatsav.monomail.core.designsystem.R.raw.kofi)
                )
                if (bmp != null) androidx.compose.ui.graphics.painter.BitmapPainter(bmp.asImageBitmap())
                else null
            }

            val versionName = remember {
                try {
                    context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.7.24"
                } catch (_: Exception) { "1.7.24" }
            }

            val matchedNotes = remember(versionName) {
                allReleaseNotes.firstOrNull { it.version == versionName }
            }
            androidx.compose.foundation.layout.BoxWithConstraints {
                val isWide = maxWidth >= 500.dp


            val HeaderBlock: @Composable () -> Unit = {
                Text(
                    text = "Welcome to Monomail",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "v$versionName — An open-source, privacy-first email client",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            val WhatsNewBlock: @Composable () -> Unit = {
                Surface(
                    shape = cornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Rounded.NewReleases,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "What's New",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        val notes = matchedNotes ?: allReleaseNotes.first()
                        notes.changes.forEach { change ->
                            Row(
                                modifier = Modifier.padding(vertical = 2.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    "•",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text(
                                    change,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            val SupportBlock: @Composable () -> Unit = {
                Text(
                    "Support Monomail",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SupportCard(
                        modifier = Modifier.weight(1f),
                        label = "Buy me a coffee",
                        onClick = { uriHandler.openUri("https://ko-fi.com/N4N2W53M5") }
                    ) {
                        if (kofiIcon != null) {
                            Icon(
                                painter = kofiIcon,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp),
                                tint = Color.Unspecified
                            )
                        } else {
                            Icon(Icons.Rounded.FavoriteBorder, contentDescription = null, modifier = Modifier.size(22.dp))
                        }
                    }
                    SupportCard(
                        modifier = Modifier.weight(1f),
                        label = "Star on GitHub",
                        onClick = { uriHandler.openUri("https://github.com/shrivatsav-org/monomail") }
                    ) {
                        Icon(Icons.Rounded.Star, contentDescription = null, modifier = Modifier.size(22.dp))
                    }
                }
                Spacer(Modifier.height(10.dp))

                SupportCard(
                    modifier = Modifier.fillMaxWidth(),
                    label = "Join Discord",
                    onClick = { uriHandler.openUri("https://discord.gg/tZgpycdm") }
                ) {
                    Icon(
                        imageVector = remember {
                            ImageVector.Builder(
                                name = "Discord",
                                defaultWidth = 22.dp,
                                defaultHeight = 22.dp,
                                viewportWidth = 24f,
                                viewportHeight = 24f
                            ).addPath(
                                fill = SolidColor(Color.Black),
                                pathData = addPathNodes(
                                    "M20.317 4.37a19.791 19.791 0 0 0-4.885-1.515.074.074 0 0 0-.079.037c-.21.375-.444.864-.608 1.25a18.27 18.27 0 0 0-5.487 0 12.64 12.64 0 0 0-.617-1.25.077.077 0 0 0-.079-.037A19.736 19.736 0 0 0 3.677 4.37a.07.07 0 0 0-.032.027C.533 9.046-.32 13.58.099 18.057a.082.082 0 0 0 .031.057 19.9 19.9 0 0 0 5.993 3.03.078.078 0 0 0 .084-.028 14.09 14.09 0 0 0 1.226-1.994.076.076 0 0 0-.041-.106 13.107 13.107 0 0 1-1.872-.892.077.077 0 0 1-.008-.128 10.2 10.2 0 0 0 .372-.292.074.074 0 0 1 .077-.01c3.928 1.793 8.18 1.793 12.062 0a.074.074 0 0 1 .078.01c.12.098.246.198.373.292a.077.077 0 0 1-.006.127 12.299 12.299 0 0 1-1.873.892.077.077 0 0 0-.041.107c.36.698.772 1.362 1.225 1.993a.076.076 0 0 0 .084.028 19.839 19.839 0 0 0 6.002-3.03.078.078 0 0 0 .084-.027 14.1 14.1 0 0 0 1.225-1.992.076.076 0 0 0-.041-.107 13.04 13.04 0 0 1-1.873-.892.078.078 0 0 1-.007-.128 10.15 10.15 0 0 0 .372-.292.074.074 0 0 1 .078-.01c3.928 1.793 8.18 1.793 12.062 0a.074.074 0 0 1 .078.01c.12.098.246.198.373.292a.077.077 0 0 1-.006.127 12.299 12.299 0 0 1-1.873.892.077.077 0 0 0-.041.107c.36.698.772 1.362 1.225 1.993a.076.076 0 0 0 .084.028 19.839 19.839 0 0 0 6.002-3.03.074.074 0 0 0 .032-.057c.451-4.78.026-9.313-1.077-13.687a.068.068 0 0 0-.031-.028zM8.02 15.33c-1.183 0-2.157-1.085-2.157-2.419 0-1.333.956-2.419 2.157-2.419 1.21 0 2.176 1.096 2.157 2.42 0 1.333-.956 2.418-2.157 2.418zm7.975 0c-1.183 0-2.157-1.085-2.157-2.419 0-1.333.955-2.419 2.157-2.419 1.21 0 2.176 1.096 2.157 2.42 0 1.333-.946 2.418-2.157 2.418z"
                                )
                            ).build()
                        },
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            val ActionsBlock: @Composable () -> Unit = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TooltipIconButton(
                        icon = Icons.Rounded.Share,
                        contentDescription = "Share Monomail",
                        onClick = {
                            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    android.content.Intent.EXTRA_TEXT,
                                    "Check out Monomail - a private, open-source email client: https://github.com/shrivatsav-org/monomail"
                                )
                            }
                            context.startActivity(android.content.Intent.createChooser(intent, "Share Monomail"))
                        }
                    )
                    TooltipIconButton(
                        icon = Icons.Rounded.BugReport,
                        contentDescription = "Report Issue",
                        onClick = { uriHandler.openUri("https://github.com/shrivatsav-org/monomail/issues") }
                    )
                    TooltipIconButton(
                        icon = Icons.Rounded.AccountBalanceWallet,
                        contentDescription = "Donate Crypto (BASE)",
                        onClick = {
                            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Crypto Address", "0xB27Ba9241de81F6DBCB322aDd76a9d9686462e9E"))
                            android.widget.Toast.makeText(context, "Address copied to clipboard", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                Spacer(Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    FilledTonalButton(
                        onClick = { viewModel.dismissWelcomePrompt() },
                        shape = cornerShape(16.dp)
                    ) {
                        Text(
                            "Get Started",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
            }

                Column(
                    modifier = Modifier
                        .padding(26.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    if (isWide) {
                        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                HeaderBlock()
                                Spacer(Modifier.height(24.dp))
                                WhatsNewBlock()
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                SupportBlock()
                                Spacer(Modifier.height(18.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                                Spacer(Modifier.height(12.dp))
                                ActionsBlock()
                            }
                        }
                    } else {
                        HeaderBlock()
                        Spacer(Modifier.height(24.dp))
                        WhatsNewBlock()
                        Spacer(Modifier.height(20.dp))
                        SupportBlock()
                        Spacer(Modifier.height(18.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        Spacer(Modifier.height(12.dp))
                        ActionsBlock()
                    }
                }
            }
        }
    }


    // Performance warning dialog for adding 4th+ account (github/polling builds only — playstore uses push)
    if (showPerformanceWarningDialog) {
        AlertDialog(
            onDismissRequest = { showPerformanceWarningDialog = false },
            title = {
                Text("Heads up", fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Adding more accounts may affect performance. " +
                    "The app might lag, email delivery may be delayed, " +
                    "and you may experience slower sync times.\n\n" +
                    "You can add up to 10 accounts total."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showPerformanceWarningDialog = false
                    activeModal = ModalType.ADD_ACCOUNT
                }) {
                    Text("I understand, add anyway", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPerformanceWarningDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showMoveToTrashDialog) {
        AlertDialog(
            onDismissRequest = { showMoveToTrashDialog = false },
            title = {
                Text("Move all spam to trash?", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("All spam messages will be moved to the Trash folder. Undo available.")
            },
            confirmButton = {
                TextButton(onClick = {
                    showMoveToTrashDialog = false
                    viewModel.moveSpamToTrash()
                }) {
                    Text("Move", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showMoveToTrashDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }


    threadToDelete?.let { thread ->
        AlertDialog(
            onDismissRequest = { threadToDelete = null },
            title = { Text("Move to Trash?") },
            text = { Text("Are you sure you want to move this conversation to the trash?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteThread(thread.threadId, thread.accountId)
                    threadToDelete = null
                }) {
                    Text("Move to Trash")
                }
            },
            dismissButton = {
                TextButton(onClick = { threadToDelete = null }) {
                    Text("Cancel")
                }
            },
        )
    }


    if (showSnoozePicker && snoozeThread != null) {
        SnoozePickerDialog(
            onDismiss = { showSnoozePicker = false },
            onSnooze = { timestamp ->
                snoozeThread?.let { viewModel.snoozeThread(it.threadId, timestamp, it.accountId) }
                showSnoozePicker = false
            }
        )
    }
}



@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun BottomFabArea(
    immediateTab: InboxTab,
    appSettings: com.shrivatsav.monomail.core.data.settings.AppSettings,
    unifiedInboxEnabled: Boolean,
    navActions: InboxNavActions,
    viewModel: InboxViewModel,
    navBarHeight: Dp,
    onMoveSpamToTrash: () -> Unit,
) {
    // Follow the Google Photos-style floating composition: a destination pill
    // and an independent circular primary action on one horizontal baseline.
    // Keep a 16dp safe-area gap above gesture navigation and 16dp screen gutters.
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 16.dp,
                end = 16.dp,
                bottom = navBarHeight + 16.dp,
            ),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BottomDockBar(
            currentTab = immediateTab,
            dockConfig = appSettings.dockConfig,
            unifiedInboxEnabled = unifiedInboxEnabled,
            appSettings = appSettings,
            onTabClick = { viewModel.switchTab(it) },
            modifier = Modifier.weight(1f),
        )

        AnimatedContent(
            targetState = immediateTab == InboxTab.SPAM,
            label = "InboxFloatingAction",
            transitionSpec = {
                (fadeIn(tween(180)) + scaleIn(tween(180), initialScale = 0.88f)) togetherWith
                    (fadeOut(tween(120)) + scaleOut(tween(120), targetScale = 0.88f))
            },
        ) { isSpam ->
            if (isSpam) {
                MoveToTrashFab(onMoveSpamToTrash)
            } else {
                ComposeFab(navActions.onCompose)
            }
        }
    }
}

@Composable
private fun MoveToTrashFab(onClick: () -> Unit) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        icon = { Icon(Icons.Rounded.Delete, contentDescription = null) },
        text = { Text("Trash") },
        containerColor = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
    )
}

@Composable
private fun ComposeFab(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
    ) {
        Icon(Icons.Rounded.Edit, contentDescription = "Compose")
    }
}

private data class LongPressMenuActions(
    val onEmailClick: () -> Unit,
    val onStar: () -> Unit,
    val onArchive: (InboxTab) -> Unit,
    val onRestore: () -> Unit,
    val onToggleRead: () -> Unit,
    val onDelete: () -> Unit,
    val onSnooze: () -> Unit,
    val onUnsnooze: () -> Unit
)

private data class LongPressMenuEntry(
    val icon: ImageVector,
    val label: String,
    val destructive: Boolean = false,
    val onClick: () -> Unit,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LongPressMenu(
    thread: EmailThread,
    state: InboxState,
    onDismiss: () -> Unit,
    actions: LongPressMenuActions
) {
    val tabForMenu = (state as? InboxState.Success)?.currentTab ?: InboxTab.INBOX
    val entries = buildList {
        if (tabForMenu == InboxTab.SNOOZED) {
            add(LongPressMenuEntry(Icons.Rounded.Restore, "Unsnooze") {
                actions.onUnsnooze()
                onDismiss()
            })
        } else {
            add(LongPressMenuEntry(
                if (thread.isStarred) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                if (thread.isStarred) "Remove star" else "Add star",
            ) {
                actions.onStar()
                onDismiss()
            })
        }

        add(LongPressMenuEntry(
            icon = if (tabForMenu == InboxTab.ARCHIVED) Icons.Rounded.Inbox else Icons.Rounded.Archive,
            label = when (tabForMenu) {
                InboxTab.ARCHIVED -> "Move to Inbox"
                InboxTab.SPAM -> "Move to Trash"
                else -> "Archive"
            },
        ) {
            actions.onArchive(tabForMenu)
            onDismiss()
        })

        add(LongPressMenuEntry(
            icon = if (thread.isRead) Icons.Rounded.MarkEmailUnread else Icons.Rounded.MarkEmailRead,
            label = if (thread.isRead) "Mark as unread" else "Mark as read",
        ) {
            actions.onToggleRead()
            onDismiss()
        })

        if (tabForMenu != InboxTab.TRASH && tabForMenu != InboxTab.SNOOZED && tabForMenu != InboxTab.SPAM) {
            add(LongPressMenuEntry(Icons.Rounded.Schedule, "Snooze") {
                actions.onSnooze()
                onDismiss()
            })
        }

        add(LongPressMenuEntry(
            icon = if (tabForMenu == InboxTab.TRASH) Icons.Rounded.Restore else Icons.Rounded.Delete,
            label = if (tabForMenu == InboxTab.TRASH) "Restore" else "Move to Trash",
            destructive = tabForMenu != InboxTab.TRASH,
        ) {
            if (tabForMenu == InboxTab.TRASH) actions.onRestore() else actions.onDelete()
            onDismiss()
        })
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
            ) {
                EmailItem(
                    thread = thread,
                    onClick = {
                        onDismiss()
                        actions.onEmailClick()
                    },
                    onLongClick = {},
                )
            }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider()

            entries.forEach { entry ->
                val tint = if (entry.destructive) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
                ListItem(
                    leadingContent = {
                        Icon(
                            imageVector = entry.icon,
                            contentDescription = null,
                            tint = tint,
                        )
                    },
                    headlineContent = { Text(entry.label, color = tint) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = entry.onClick),
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SnoozePickerDialog(
    onDismiss: () -> Unit,
    onSnooze: (Long) -> Unit
) {
    val now = System.currentTimeMillis()
    val options = remember {
        val cal = java.util.Calendar.getInstance()
        buildList {
            val c = cal.clone() as java.util.Calendar
            c.timeInMillis = now
            c.set(java.util.Calendar.SECOND, 0)
            c.set(java.util.Calendar.MILLISECOND, 0)
            c.add(java.util.Calendar.HOUR_OF_DAY, 1)
            add(SnoozeOption("In 1 hour", c.timeInMillis))

            c.timeInMillis = now
            c.set(java.util.Calendar.HOUR_OF_DAY, 18)
            c.set(java.util.Calendar.MINUTE, 0)
            if (c.timeInMillis <= now) c.add(java.util.Calendar.DAY_OF_MONTH, 1)
            add(SnoozeOption("Later today", c.timeInMillis))

            c.timeInMillis = now
            c.set(java.util.Calendar.HOUR_OF_DAY, 20)
            c.set(java.util.Calendar.MINUTE, 0)
            if (c.timeInMillis <= now) c.add(java.util.Calendar.DAY_OF_MONTH, 1)
            add(SnoozeOption("This evening", c.timeInMillis))

            c.timeInMillis = now
            c.add(java.util.Calendar.DAY_OF_MONTH, 1)
            c.set(java.util.Calendar.HOUR_OF_DAY, 8)
            c.set(java.util.Calendar.MINUTE, 0)
            add(SnoozeOption("Tomorrow morning", c.timeInMillis))

            c.timeInMillis = now
            val currentDay = c.get(java.util.Calendar.DAY_OF_WEEK)
            var diff = java.util.Calendar.SATURDAY - currentDay
            if (diff <= 0) diff += 7
            c.add(java.util.Calendar.DAY_OF_MONTH, diff)
            c.set(java.util.Calendar.HOUR_OF_DAY, 9)
            c.set(java.util.Calendar.MINUTE, 0)
            add(SnoozeOption("This weekend", c.timeInMillis))

            c.timeInMillis = now
            var mDiff = java.util.Calendar.MONDAY - c.get(java.util.Calendar.DAY_OF_WEEK)
            if (mDiff <= 0) mDiff += 7
            c.add(java.util.Calendar.DAY_OF_MONTH, mDiff)
            c.set(java.util.Calendar.HOUR_OF_DAY, 9)
            c.set(java.util.Calendar.MINUTE, 0)
            add(SnoozeOption("Next week", c.timeInMillis))
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
        ) {
            Text(
                text = "Snooze until",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            )
            options.forEach { option ->
                ListItem(
                    headlineContent = { Text(option.label) },
                    trailingContent = {
                        Icon(Icons.Rounded.ChevronRight, contentDescription = null)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSnooze(option.timestamp) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                )
            }
        }
    }
}

private data class SnoozeOption(val label: String, val timestamp: Long)

@Composable
private fun SupportCard(
    modifier: Modifier = Modifier,
    label: String,
    onClick: () -> Unit,
    icon: @Composable () -> Unit
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = 56.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            icon()
            Text(label, style = MaterialTheme.typography.labelLarge, maxLines = 2)
        }
    }
}

@Composable
private fun GroupHeaderItem(
    modifier: Modifier = Modifier,
    groupName: String,
    count: Int,
    unreadCount: Int,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    ListItem(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        leadingContent = {
            AvatarCircle(
                photoUrl = null,
                displayName = groupName,
                size = 40.dp,
                textStyle = MaterialTheme.typography.titleMedium,
            )
        },
        headlineContent = {
            Text(
                text = groupName,
                fontWeight = if (unreadCount > 0) FontWeight.Bold else FontWeight.Medium,
            )
        },
        supportingContent = { Text("$count emails") },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (unreadCount > 0) {
                    Badge { Text(if (unreadCount > 99) "99+" else unreadCount.toString()) }
                    Spacer(Modifier.width(8.dp))
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                )
            }
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
}

@Composable
private fun rememberConnectivityState(): Boolean {
    val cm = androidx.compose.ui.platform.LocalContext.current
        .getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val currentOnline = remember {
        val activeNetwork = cm.activeNetwork
        val caps = activeNetwork?.let { cm.getNetworkCapabilities(it) }
        caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }
    var isOnline by remember { mutableStateOf(currentOnline) }
    DisposableEffect(Unit) {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) { isOnline = true }
            override fun onLost(network: Network) { isOnline = false }
            override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
                isOnline = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            }
        }
        cm.registerDefaultNetworkCallback(callback)
        onDispose { cm.unregisterNetworkCallback(callback) }
    }
    return isOnline
}

@Composable
private fun OfflineBanner() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        shape = MaterialTheme.shapes.small,
    ) {
        ListItem(
            leadingContent = {
                Icon(Icons.Rounded.CloudOff, contentDescription = null)
            },
            headlineContent = { Text("You are offline") },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        )
    }
}

@Composable
private fun BackfillProgressBanner(progress: BodyBackfillState?, onClick: () -> Unit = {}) {
    val total = progress?.total ?: 0
    val completed = progress?.completed ?: 0
    val pct = if (total > 0) (completed.toFloat() / total * 100).toInt() else 0
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
        shape = MaterialTheme.shapes.medium,
    ) {
        ListItem(
            leadingContent = {
                Icon(Icons.Rounded.CloudDownload, contentDescription = null)
            },
            headlineContent = {
                Text(if (progress?.folder != null) "Downloading ${progress.folder} content" else "Downloading email content")
            },
            supportingContent = {
                Column {
                    Text("$completed of $total emails · $pct%")
                    LinearProgressIndicator(
                        progress = { progress?.progress ?: 0f },
                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        trackColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.25f),
                    )
                }
            },
            trailingContent = {
                CircularProgressIndicator(
                    progress = { if (total > 0) completed.toFloat() / total else 0f },
                    modifier = Modifier.size(28.dp),
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    trackColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.25f),
                    strokeWidth = 3.dp,
                )
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        )
    }
}

/** Material alert dialog showing live backfill progress and cancellation. */
@Composable
private fun BackfillProgressDialog(
    progress: BodyBackfillState,
    onCancel: () -> Unit,
    onDismiss: () -> Unit
) {
    val pct = if (progress.total > 0) (progress.completed.toFloat() / progress.total * 100).toInt() else 0
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Downloading email content") },
        confirmButton = {
            TextButton(onClick = onCancel) { Text("Cancel download") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        },
        text = {
            Column {
                Text(
                    if (progress.folder != null) "Downloading ${progress.folder} content" else "Downloading email content",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
                LinearProgressIndicator(
                    progress = { progress.progress },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                )
                Spacer(Modifier.height(12.dp))
                Text("${progress.completed} of ${progress.total} emails · $pct%")
            }
        },
    )
}

@Composable
private fun BackfillErrorBanner(errorMessage: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        shape = MaterialTheme.shapes.small,
    ) {
        ListItem(
            leadingContent = {
                Icon(Icons.Rounded.CloudOff, contentDescription = null)
            },
            headlineContent = { Text(errorMessage) },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        )
    }
}

private data class ReleaseNotes(
    val version: String,
    val changes: List<String>
)

private val allReleaseNotes = listOf(
    ReleaseNotes(version = "1.7.51", changes = listOf(
        "Introduced a responsive center-split layout for reading emails on tablets",
        "Updated the Welcome and Sign-In screens with a clean two-column layout for wider screens",
        "Fixed a scroll cutoff bug in the Welcome modal on horizontal devices",
        "Optimized layout constraints for smoother visual transitions"
    )),
    ReleaseNotes(version = "1.7.38", changes = listOf(
        "Optimized email detail loading with asynchronous parsing and pre-warmed instances",
        "Completed a major architectural overhaul, modularizing the application",
        "Abstracted authentication logic to support specific flavor builds",
        "Fixed a critical crash on launch related to missing HiltWorker processing",
        "Restored the custom Monomail notification icon",
        "Settings screen now accurately displays the application version"
    )),
    ReleaseNotes(version = "1.7.24", changes = listOf(
        "Optimized build times with configuration caching",
        "Fixed MSAL sign-in for release builds",
        "Enhanced FAB positioning in dock bar",
        "Improved Discord icon theming",
        "Various UI polish and bug fixes"
    )),
    ReleaseNotes(version = "1.7.23", changes = listOf(
        "Redesigned bottom dock bar with custom layout options",
        "Added template-backed scheduled send",
        "Improved swipe gesture responsiveness",
        "Enhanced tablet layout support"
    ))
)

@Composable
private fun TopAlertBanner(
    toastState: InboxViewModel.ToastState?,
    onUndo: () -> Unit
) {
    androidx.compose.animation.AnimatedVisibility(
        visible = toastState != null,
        enter = fadeIn(tween(220)) + slideInVertically(
            initialOffsetY = { -it },
            animationSpec = tween(300, easing = FastOutSlowInEasing)
        ),
        exit = fadeOut(tween(180)) + slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(250)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 8.dp)
    ) {
        toastState?.let { state ->
            Snackbar(
                action = {
                    TextButton(onClick = onUndo) { Text("Undo") }
                },
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            ) {
                Text(state.message, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}
