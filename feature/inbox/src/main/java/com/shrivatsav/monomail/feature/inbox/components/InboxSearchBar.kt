package com.shrivatsav.monomail.feature.inbox.components

import com.shrivatsav.monomail.feature.inbox.*

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.shrivatsav.monomail.ui.theme.cornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import com.shrivatsav.monomail.core.data.auth.UserProfile
import com.shrivatsav.monomail.core.data.settings.SwipeAction

data class SearchBarActions(
    val onMarkAllRead: () -> Unit,
    val onScheduledClick: () -> Unit = {},
    val scheduledCount: Int = 0,
    val onUndo: () -> Unit,
    val onOpenProfile: () -> Unit,
    val onOpenFolders: () -> Unit = {},
    val showMarkAllRead: Boolean = true,
    val onCycleAccount: ((Int) -> Unit)? = null,
)

data class BulkSelectionState(
    val isBulkMode: Boolean = false,
    val selectedCount: Int = 0,
    val totalCount: Int = 0,
    val onSelectAll: () -> Unit = {},
    val onDeselectAll: () -> Unit = {},
    val onDone: () -> Unit = {}
)

data class SearchDisplayState(
    val isRefreshing: Boolean = false,
    val unifiedInboxEnabled: Boolean = false,
    val accounts: List<UserProfile> = emptyList(),
    val userProfile: UserProfile? = null,
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun InboxSearchBar(
    userProfile: UserProfile?,
    accounts: List<UserProfile>,
    query: String,
    onQueryChange: (String) -> Unit,
    onServerSearch: (String) -> Unit,
    actions: SearchBarActions,
    isRefreshing: Boolean,
    bulkSelection: BulkSelectionState = BulkSelectionState(),
    unifiedInboxEnabled: Boolean = false,
    onFocusChange: ((Boolean) -> Unit)? = null,
) {
    val containerColor by animateColorAsState(
        targetValue = when {
            bulkSelection.isBulkMode -> MaterialTheme.colorScheme.secondaryContainer
            else -> MaterialTheme.colorScheme.surfaceContainer
        },
        animationSpec = tween(300),
        label = "SearchBarColor"
    )

    var isExpanded by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(false) }

    // Fluid collapse into search bar on back gesture / button
    androidx.activity.compose.BackHandler(enabled = isExpanded) {
        isExpanded = false
        onQueryChange("")
        onFocusChange?.invoke(false)
    }

    // If query is cleared externally, collapse if not focused
    LaunchedEffect(query) {
        if (query.isNotEmpty() && !isExpanded) {
            isExpanded = true
        }
    }

    val display = remember(isRefreshing, unifiedInboxEnabled, accounts, userProfile) {
        SearchDisplayState(isRefreshing, unifiedInboxEnabled, accounts, userProfile)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = if (isExpanded) 0.dp else 16.dp, vertical = if (isExpanded) 0.dp else 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SearchBar(
            inputField = {
                if (bulkSelection.isBulkMode) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(horizontal = 4.dp)
                    ) {
                        BulkSelectionContent(bulkSelection)
                    }
                } else {
                    SearchBarDefaults.InputField(
                        query = query,
                        onQueryChange = onQueryChange,
                        onSearch = {
                            onServerSearch(query)
                        },
                        expanded = isExpanded,
                        onExpandedChange = {
                            isExpanded = it
                            onFocusChange?.invoke(it)
                        },
                        placeholder = {
                            Text(
                                if (display.isRefreshing) "Syncing..." else if (display.unifiedInboxEnabled) "Search all accounts..." else "Search in mail",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            )
                        },
                        leadingIcon = {
                            if (isExpanded) {
                                IconButton(onClick = {
                                    isExpanded = false
                                    onQueryChange("")
                                    onFocusChange?.invoke(false)
                                }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                        contentDescription = "Back",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            } else if (display.isRefreshing) {
                                LoadingIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Icon(
                                    painter = painterResource(com.shrivatsav.monomail.feature.inbox.R.drawable.ic_monomail_mark),
                                    contentDescription = "Monomail",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        },
                        trailingIcon = {
                            if (isExpanded && query.isNotEmpty()) {
                                IconButton(onClick = { onQueryChange("") }) {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = "Clear search",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            } else {
                                SearchTrailingIcon(actions, display)
                            }
                        }
                    )
                }
            },
            expanded = isExpanded && !bulkSelection.isBulkMode,
            onExpandedChange = {
                if (!bulkSelection.isBulkMode) {
                    isExpanded = it
                    onFocusChange?.invoke(it)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = SearchBarDefaults.colors(containerColor = containerColor),
            shape = if (isExpanded) RoundedCornerShape(0.dp) else CircleShape,
            windowInsets = SearchBarDefaults.windowInsets
        ) {
            if (query.isNotEmpty()) {
                ListItem(
                    headlineContent = { Text("Search server for \"$query\"") },
                    leadingContent = {
                        Icon(Icons.Rounded.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                    modifier = Modifier.clickable {
                        onServerSearch(query)
                    }
                )
            }
        }
    }
}

@Composable
private fun BulkSelectionContent(bulkSelection: BulkSelectionState) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 20.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AnimatedContent(
            targetState = bulkSelection.selectedCount,
            label = "selectedCount",
            modifier = Modifier.weight(1f),
            transitionSpec = {
                (fadeIn(tween(200)) + scaleIn(tween(200))).togetherWith(
                    fadeOut(tween(150)) + scaleOut(tween(150))
                )
            }
        ) { count ->
            Text(
                text = if (count == 1) "1 selected" else "$count selected",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (bulkSelection.selectedCount < bulkSelection.totalCount) {
                TextButton(
                    onClick = bulkSelection.onSelectAll,
                    shape = cornerShape(24.dp),
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) { Text("Select all") }
            } else {
                TextButton(
                    onClick = bulkSelection.onDeselectAll,
                    shape = cornerShape(24.dp),
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) { Text("Deselect all") }
            }
            Spacer(Modifier.width(4.dp))
            IconButton(onClick = bulkSelection.onDone, modifier = Modifier.size(40.dp)) {
                Icon(Icons.Rounded.Close, contentDescription = "Done", tint = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SearchInputContent(
    query: String,
    onQueryChange: (String) -> Unit,
    onServerSearch: (String) -> Unit,
    actions: SearchBarActions,
    display: SearchDisplayState,
    onFocusChange: ((Boolean) -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is FocusInteraction.Focus -> onFocusChange?.invoke(true)
                is FocusInteraction.Unfocus -> onFocusChange?.invoke(false)
            }
        }
    }
    SearchBarDefaults.InputField(
        query = query,
        onQueryChange = onQueryChange,
        onSearch = { onServerSearch(query) },
        expanded = false,
        onExpandedChange = {},
        interactionSource = interactionSource,
        placeholder = {
            Text(
                if (display.isRefreshing) "Syncing..." else if (display.unifiedInboxEnabled) "Search all accounts..." else "Search in mail",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
        },
        leadingIcon = {
            if (display.isRefreshing) {
                LoadingIndicator(modifier = Modifier.size(40.dp), color = MaterialTheme.colorScheme.onSurface)
            } else {
                Icon(
                    painter = painterResource(com.shrivatsav.monomail.feature.inbox.R.drawable.ic_monomail_mark),
                    contentDescription = "Monomail",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        trailingIcon = { SearchTrailingIcon(actions, display) }
    )
}

@Composable
private fun SearchTrailingIcon(actions: SearchBarActions, display: SearchDisplayState) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 4.dp)) {
        if (actions.scheduledCount > 0) {
            BadgedBox(badge = {
                Badge(containerColor = MaterialTheme.colorScheme.error, contentColor = MaterialTheme.colorScheme.onError) {
                    Text(if (actions.scheduledCount > 99) "99+" else actions.scheduledCount.toString(), style = MaterialTheme.typography.labelSmall)
                }
            }) {
                IconButton(onClick = actions.onScheduledClick, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Rounded.CalendarMonth, contentDescription = "Scheduled", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), modifier = Modifier.size(25.dp))
                }
            }
        }
        if (actions.showMarkAllRead) {
            IconButton(onClick = actions.onMarkAllRead, modifier = Modifier.size(40.dp)) {
                Icon(Icons.Rounded.CheckCircle, contentDescription = "Mark all as read", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), modifier = Modifier.size(25.dp))
            }
        }
        IconButton(onClick = actions.onOpenFolders, modifier = Modifier.size(40.dp)) {
            Icon(
                imageVector = Icons.Rounded.FolderOpen,
                contentDescription = "Mail folders",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                modifier = Modifier.size(24.dp)
            )
        }
        if (display.unifiedInboxEnabled && display.accounts.size > 1) {
            StackedAccountAvatars(accounts = display.accounts, onClick = actions.onOpenProfile)
        } else {
            AvatarButton(
                userProfile = display.userProfile,
                onClick = actions.onOpenProfile,
                onCycleAccount = actions.onCycleAccount
            )
        }
    }
}

@Composable
private fun StackedAccountAvatars(
    accounts: List<UserProfile>,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val maxVisible = 3
    val visible = accounts.take(maxVisible)
    val overflow = accounts.size - maxVisible

    Box(
        modifier = modifier
            .size(30.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        visible.forEachIndexed { index, account ->
            val offsetX = index * 10
            val size = 22
            Box(
                modifier = Modifier
                    .offset(x = offsetX.dp)
                    .size(size.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape)
            ) {
                if (!account.photoUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = account.photoUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                    )
                } else {
                    val initial = account.displayName.firstOrNull()?.uppercase() ?: "?"
                    Box(
                        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.onSurface),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            initial,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.background,
                            fontSize = MaterialTheme.typography.labelSmall.fontSize * 0.7f
                        )
                    }
                }
            }
        }
        if (overflow > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = (-2).dp, y = 2.dp)
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurface),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "+$overflow",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = MaterialTheme.typography.labelSmall.fontSize * 0.6f,
                    color = MaterialTheme.colorScheme.background
                )
            }
        }
    }
}

@Composable
private fun AvatarButton(
    userProfile: UserProfile?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onCycleAccount: ((Int) -> Unit)? = null,
) {
    var dragAccumulator by remember { mutableFloatStateOf(0f) }
    var swipeDirection by remember { mutableIntStateOf(1) }

    Box(
        modifier = modifier
            .size(34.dp)
            .clip(CircleShape)
            .pointerInput(userProfile) {
                if (onCycleAccount != null) {
                    detectVerticalDragGestures(
                        onVerticalDrag = { change, dragAmount ->
                            change.consume()
                            dragAccumulator += dragAmount
                        },
                        onDragEnd = {
                            if (dragAccumulator > 16f) {
                                swipeDirection = 1
                                onCycleAccount(1)
                            } else if (dragAccumulator < -16f) {
                                swipeDirection = -1
                                onCycleAccount(-1)
                            }
                            dragAccumulator = 0f
                        }
                    )
                }
            }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = userProfile?.id ?: "",
            transitionSpec = {
                if (swipeDirection >= 0) {
                    (slideInVertically(spring(stiffness = Spring.StiffnessMediumLow)) { -it } + fadeIn())
                        .togetherWith(slideOutVertically(spring(stiffness = Spring.StiffnessMediumLow)) { it } + fadeOut())
                } else {
                    (slideInVertically(spring(stiffness = Spring.StiffnessMediumLow)) { it } + fadeIn())
                        .togetherWith(slideOutVertically(spring(stiffness = Spring.StiffnessMediumLow)) { -it } + fadeOut())
                }
            },
            label = "googleAvatarSwipe"
        ) { _ ->
            if (userProfile != null && !userProfile.photoUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = userProfile.photoUrl,
                    contentDescription = "Profile",
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                )
            } else {
                val initial = userProfile?.displayName?.firstOrNull()?.uppercase() ?: "?"
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        initial,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}
