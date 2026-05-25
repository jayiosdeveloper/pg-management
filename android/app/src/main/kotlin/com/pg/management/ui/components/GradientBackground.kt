package com.pg.management.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import com.pg.management.ui.theme.BrandDeep
import com.pg.management.ui.theme.BrandDeepDarker
import com.pg.management.ui.theme.BrandPrimary
import com.pg.management.ui.theme.BrandPurple

@Composable
fun GradientBackground(content: @Composable () -> Unit) {
    val brush = Brush.linearGradient(
        colorStops = arrayOf(
            0f to BrandDeepDarker,
            0.45f to BrandDeep,
            0.85f to BrandPurple.copy(alpha = 0.55f),
            1f to BrandPrimary.copy(alpha = 0.85f),
        ),
        start = Offset(0f, 0f),
        end = Offset.Infinite,
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush),
    ) {
        content()
    }
}
