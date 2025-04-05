package com.robinhagolani.meditationtimer.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun CircularTimer(
    modifier: Modifier = Modifier,
    progress: Float,
    time: String
) {
    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val animatedProgress by animateFloatAsState(
            targetValue = progress,
            label = "Timer Progress"
        )

        Canvas(modifier = Modifier.size(300.dp)) {
            // Background circle
            drawCircle(
                color = colorScheme.surfaceVariant,
                style = Stroke(width = 12f, cap = StrokeCap.Round),
            )

            // Progress arc
            drawArc(
                color = colorScheme.primary,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                style = Stroke(width = 12f, cap = StrokeCap.Round)
            )
        }

        Text(
            text = time,
            style = MaterialTheme.typography.displayLarge
        )
    }
}