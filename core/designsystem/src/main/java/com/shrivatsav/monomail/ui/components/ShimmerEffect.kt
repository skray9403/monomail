package com.shrivatsav.monomail.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Compatibility identity modifier; placeholders use the stock Material loader. */
fun Modifier.shimmer(): Modifier = this

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun EmailItemSkeleton(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth().height(76.dp),
        contentAlignment = Alignment.Center,
    ) {
        LoadingIndicator()
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DetailSkeleton(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth().height(180.dp),
        contentAlignment = Alignment.Center,
    ) {
        LoadingIndicator()
    }
}
