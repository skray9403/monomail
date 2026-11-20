package com.shrivatsav.monomail.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Badge
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shrivatsav.monomail.ui.theme.MonoOpacity
import com.shrivatsav.monomail.ui.theme.MonoMailTheme
import com.shrivatsav.monomail.ui.theme.ThemePreviews
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun SectionHeader(
    label: String,
    modifier: Modifier = Modifier,
    count: Int? = null,
    showTopDivider: Boolean = true
) {
    Box(modifier = modifier.fillMaxWidth()) {
        if (showTopDivider) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = MonoOpacity.whisper),
                thickness = 1.dp
            )
        }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (count != null && count > 0) {
                Spacer(modifier = Modifier.width(8.dp))
                Badge(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ) {
                    Text(
                        text = count.toString(),
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }
    }
}

@ThemePreviews
@Composable
private fun SectionHeaderPreview() {
    MonoMailTheme {
        Surface {
            SectionHeader(label = "Primary Inbox", count = 4)
        }
    }
}

@Preview(
    name = "Section Header - Tablet",
    device = "spec:width=1280dp,height=800dp,dpi=240",
    showBackground = true,
)
@Composable
private fun SectionHeaderTabletPreview() {
    MonoMailTheme {
        Surface {
            SectionHeader(label = "Folders")
        }
    }
}
