package com.shrivatsav.monomail.feature.inbox.components

import android.text.format.DateFormat as AndroidDateFormat

import com.shrivatsav.monomail.feature.inbox.*
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shrivatsav.monomail.data.model.EmailThread
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Where an unread item sits in a consecutive run, retained for grouping logic. */
enum class UnreadPosition { SOLO, TOP, MIDDLE, BOTTOM }

data class SelectionState(
    val isSelected: Boolean = false,
    val isBulkMode: Boolean = false,
    val onSelectToggle: () -> Unit = {},
    val onRangeSelect: () -> Unit = {},
    val onAvatarLongClick: () -> Unit = {}
)

/**
 * Email-specific content displayed through Material 3 ListItem. The row keeps
 * Gmail-style sender/subject/snippet semantics, while selection and gestures
 * remain application behaviour rather than a custom row layout system.
 */
@Composable
fun EmailItem(
    thread: EmailThread,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    showSnippet: Boolean = true,
    compactMode: Boolean = false,
    selection: SelectionState = SelectionState(),
    unreadPosition: UnreadPosition = UnreadPosition.SOLO,
    is24HourTime: Boolean = false,
    modifier: Modifier = Modifier
) {
    val isUnread = !thread.isRead
    val haptics = LocalHapticFeedback.current
    val rowClick = if (selection.isBulkMode) selection.onRangeSelect else onClick
    val rowLongClick = if (selection.isBulkMode) selection.onSelectToggle else {
        {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            onLongClick()
        }
    }

    ListItem(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .combinedClickable(
                onClick = rowClick,
                onLongClick = rowLongClick,
            ),
        leadingContent = {
            SenderAvatar(
                senderInitial = thread.from.firstOrNull()?.uppercase() ?: "?",
                isSelected = selection.isSelected,
                isBulkMode = selection.isBulkMode,
                onClick = if (selection.isBulkMode) selection.onSelectToggle else onClick,
                onLongClick = {
                    if (!selection.isBulkMode) {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        selection.onAvatarLongClick()
                    }
                },
            )
        },
        headlineContent = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = displayName(thread.from),
                    modifier = Modifier.weight(1f, fill = false),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = if (isUnread) FontWeight.Bold else FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (thread.messageCount > 1) {
                    Spacer(Modifier.width(6.dp))
                    Badge {
                        Text(thread.messageCount.toString())
                    }
                }
            }
        },
        supportingContent = {
            Column(verticalArrangement = Arrangement.spacedBy(if (compactMode) 0.dp else 2.dp)) {
                Text(
                    text = thread.subject,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isUnread) FontWeight.SemiBold else FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (showSnippet) {
                    Text(
                        text = thread.snippet,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = if (compactMode) 1 else 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        },
        trailingContent = {
            Text(
                text = formatTimestamp(thread.date, is24HourTime),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isUnread) FontWeight.SemiBold else FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        },
        colors = ListItemDefaults.colors(
            containerColor = if (isUnread) {
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.38f)
            } else {
                Color.Transparent
            }
        ),
    )
}

@Composable
private fun SenderAvatar(
    senderInitial: String,
    isSelected: Boolean = false,
    isBulkMode: Boolean = false,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        contentAlignment = Alignment.Center,
    ) {
        if (!isBulkMode) {
            Text(
                text = senderInitial,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        } else if (isSelected) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(3.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(28.dp),
                )
            }
        } else {
            Icon(
                imageVector = Icons.Rounded.RadioButtonUnchecked,
                contentDescription = "Not selected",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(30.dp),
            )
        }
    }
}

private val displayNameRegex = Regex("""^"?([^"<]+?)"?\s*<""")
private fun displayName(from: String): String =
    displayNameRegex.find(from)?.groupValues?.get(1)?.trim() ?: from.trim()

private val time12Formatter = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
private val time24Formatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
private val dayFormatter = DateTimeFormatter.ofPattern("EEE", Locale.getDefault())
private val dateFormatter = DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())
private val fullDateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault())

private val systemZone: ZoneId get() = ZoneId.systemDefault()
private var cachedTodayDayOfYear: Int = -1
private var cachedTodayDate: LocalDate? = null

private fun getTodayDate(): LocalDate {
    val now = LocalDate.now(systemZone)
    if (now.dayOfYear != cachedTodayDayOfYear) {
        cachedTodayDayOfYear = now.dayOfYear
        cachedTodayDate = now
    }
    return cachedTodayDate ?: now
}

private fun formatTimestamp(epochMillis: Long, is24HourTime: Boolean): String {
    if (epochMillis == 0L) return ""
    val formatter = if (is24HourTime) time24Formatter else time12Formatter
    val zoned = Instant.ofEpochMilli(epochMillis).atZone(systemZone)
    val date = zoned.toLocalDate()
    val today = getTodayDate()
    return when {
        date == today -> formatter.format(zoned)
        date.isAfter(today.minusDays(7)) -> dayFormatter.format(zoned)
        date.year == today.year -> dateFormatter.format(zoned)
        else -> fullDateFormatter.format(zoned)
    }
}
