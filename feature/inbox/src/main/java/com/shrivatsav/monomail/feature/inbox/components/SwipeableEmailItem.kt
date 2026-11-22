package com.shrivatsav.monomail.feature.inbox.components

import com.shrivatsav.monomail.feature.inbox.*
import com.shrivatsav.monomail.model.InboxTab

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.shrivatsav.monomail.data.model.EmailThread

data class SwipeCallbacks(
    val onThreadToDeleteChange: (EmailThread?) -> Unit,
    val onEmailClick: () -> Unit,
    val onLongClick: () -> Unit,
    val isNested: Boolean = false
)

enum class GroupPosition { SOLO, TOP, MIDDLE, BOTTOM }

private val SoloShape = RoundedCornerShape(16.dp)
private val TopShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 0.dp, bottomEnd = 0.dp)
private val MiddleShape = RoundedCornerShape(0.dp)
private val BottomShape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 16.dp, bottomEnd = 16.dp)

private fun cardShape(position: GroupPosition): RoundedCornerShape = when (position) {
    GroupPosition.SOLO -> SoloShape
    GroupPosition.TOP -> TopShape
    GroupPosition.MIDDLE -> MiddleShape
    GroupPosition.BOTTOM -> BottomShape
}

@Composable
internal fun SwipeableEmailItem(
    modifier: Modifier = Modifier,
    thread: EmailThread,
    tabForSwipe: InboxTab,
    appSettings: com.shrivatsav.monomail.core.data.settings.AppSettings,
    viewModel: InboxViewModel,
    callbacks: SwipeCallbacks,
    selection: SelectionState = SelectionState(),
    unreadPosition: UnreadPosition = UnreadPosition.SOLO,
    groupPosition: GroupPosition = GroupPosition.SOLO,
    is24HourTime: Boolean = false
) {
    val shape = cardShape(groupPosition)
    val topPad = if (groupPosition == GroupPosition.SOLO || groupPosition == GroupPosition.TOP) 2.dp else 0.dp
    val bottomPad = if (groupPosition == GroupPosition.SOLO || groupPosition == GroupPosition.BOTTOM) 4.dp else 0.dp

    Box(
        modifier = modifier
            .padding(horizontal = 12.dp)
            .padding(top = topPad, bottom = bottomPad)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainer),
    ) {
        EmailItem(
            thread = thread,
            onClick = callbacks.onEmailClick,
            onLongClick = callbacks.onLongClick,
            showSnippet = appSettings.showSnippet,
            compactMode = appSettings.compactList,
            selection = selection,
            unreadPosition = unreadPosition,
            is24HourTime = is24HourTime
        )
    }
}
