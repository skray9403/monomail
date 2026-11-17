package com.shrivatsav.monomail.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.FolderZip
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.VideoFile
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.shrivatsav.monomail.ui.theme.cornerShape

/**
 * Shared preview card for attachments, used in both Compose-screen (THUMBNAIL mode)
 * and Detail-screen (DETAIL / FILE_GRID modes).
 *
 * @param category classified attachment type — controls icon, color, thumbnail behavior
 * @param mode THUMBNAIL = 100dp square for compose LazyRow; DETAIL = full-width card for detail screen
 * @param thumbnailUri optional image URI for thumbnail preview (images, video frames)
 * @param onRemove optional — shows X button (compose mode)
 * @param onClick optional — tap action (detail mode preview or external open)
 */
@Composable
fun AttachmentPreviewCard(
    name: String,
    mimeType: String,
    size: Long,
    category: AttachmentCategory = classifyAttachment(mimeType, name),
    mode: PreviewMode = PreviewMode.THUMBNAIL,
    thumbnailUri: Any? = null,
    isFetching: Boolean = false,
    onRemove: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    when (mode) {
        PreviewMode.THUMBNAIL -> ThumbnailPreview(
            name = name,
            category = category,
            thumbnailUri = thumbnailUri,
            isFetching = isFetching,
            onRemove = onRemove,
            onClick = onClick,
            modifier = modifier
        )

        PreviewMode.DETAIL -> DetailPreview(
            name = name,
            size = size,
            category = category,
            isFetching = isFetching,
            onClick = onClick,
            modifier = modifier
        )
    }
}

enum class PreviewMode { THUMBNAIL, DETAIL }

// ─── Thumbnail (100dp square for compose row) ─────────────────────────────────

@Composable
private fun ThumbnailPreview(
    name: String,
    category: AttachmentCategory,
    thumbnailUri: Any?,
    isFetching: Boolean,
    onRemove: (() -> Unit)?,
    onClick: (() -> Unit)?,
    modifier: Modifier
) {
    Card(
        modifier = modifier
            .size(100.dp)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Box(Modifier.fillMaxSize()) {
            when {
                category == AttachmentCategory.IMAGE && thumbnailUri != null -> {
                    AsyncImage(
                        model = thumbnailUri,
                        contentDescription = name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                category == AttachmentCategory.VIDEO -> {
                    if (thumbnailUri != null) {
                        AsyncImage(
                            model = thumbnailUri,
                            contentDescription = name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        CategoryIconTile(category = category, iconSize = 36.dp)
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AttachFile,
                            contentDescription = "Play",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                else -> CategoryIconTile(category = category, iconSize = 36.dp)
            }

            if (category != AttachmentCategory.IMAGE) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.88f))
                        .padding(6.dp)
                )
            }

            if (onRemove != null) {
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(32.dp)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.88f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Remove",
                        modifier = Modifier.size(16.dp),
                    )
                }
            }

            if (isFetching) {
                Box(
                    modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                    )
                }
            }
        }
    }
}

// ─── Detail (full-width card for detail screen) ───────────────────────────────

@Composable
private fun DetailPreview(
    name: String,
    size: Long,
    category: AttachmentCategory,
    isFetching: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier
) {
    val (icon, color) = categoryIconAndColor(category)
    ListItem(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        leadingContent = {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            ) {
                Icon(icon, contentDescription = null, modifier = Modifier.padding(10.dp).size(22.dp))
            }
        },
        headlineContent = {
            Text(name, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        supportingContent = {
            Text(formatFileSize(size), color = MaterialTheme.colorScheme.onSurfaceVariant)
        },
        trailingContent = {
            if (isFetching) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
        },
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    )
}

// ─── Category icon tile (for THUMBNAIL mode fallback) ────────────────────────

@Composable
private fun CategoryIconTile(
    category: AttachmentCategory,
    iconSize: Dp
) {
    val (icon, color) = categoryIconAndColor(category)
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}

// ─── Icon + colour mapping ────────────────────────────────────────────────────

private fun categoryIconAndColor(category: AttachmentCategory): Pair<ImageVector, Color> = when (category) {
    AttachmentCategory.IMAGE -> Icons.Rounded.Image to Color(0xFF4CAF50)
    AttachmentCategory.VIDEO -> Icons.Rounded.VideoFile to Color(0xFFE53935)
    AttachmentCategory.PDF -> Icons.Rounded.PictureAsPdf to Color(0xFFE53935)
    AttachmentCategory.ARCHIVE -> Icons.Rounded.FolderZip to Color(0xFFFF9800)
    AttachmentCategory.CODE -> Icons.Rounded.Description to Color(0xFF2196F3)
    AttachmentCategory.UNKNOWN -> Icons.Rounded.AttachFile to Color(0xFF9E9E9E)
}
