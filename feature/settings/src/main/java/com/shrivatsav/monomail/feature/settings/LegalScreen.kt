package com.shrivatsav.monomail.feature.settings
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegalScreen(
    type: String,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var content by remember { mutableStateOf("Loading...") }
    val title = if (type == "privacy") "Privacy Policy" else "Terms of Service"
    LaunchedEffect(type) {
        val fileName = if (type == "privacy") "PRIVACY_POLICY.md" else "TERMS_OF_SERVICE.md"
        content = withContext(Dispatchers.IO) {
            try {
                context.assets.open(fileName).bufferedReader().use { it.readText() }
            } catch (e: Exception) {
                "Failed to load $title."
            }
        }
    }
    Scaffold(
        contentWindowInsets = WindowInsets.statusBars
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .padding(bottom = 96.dp)
        ) {
            val textColor = MaterialTheme.colorScheme.onSurface.toArgb()
            androidx.compose.ui.viewinterop.AndroidView(
                factory = { ctx ->
                    android.widget.TextView(ctx).apply {
                        setTextColor(textColor)
                        io.noties.markwon.Markwon.create(ctx).setMarkdown(this, content)
                    }
                },
                update = { textView ->
                    textView.setTextColor(textColor)
                    io.noties.markwon.Markwon.create(textView.context).setMarkdown(textView, content)
                }
            )
        }
    }
}
