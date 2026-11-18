package com.shrivatsav.monomail.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Fixed Material 3 Expressive corner tokens; no per-user shape engine. */
fun cornerShape(baseDp: Dp): RoundedCornerShape = RoundedCornerShape(baseDp)

val MonoMailShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)
