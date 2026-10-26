package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.RecordRedLight
import kotlin.math.sin

@Composable
fun WaveformVisualizer(
    amplitude: Float,
    isRecording: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 24,
    height: Dp = 48.dp,
    activeColor: Color = if (isRecording) RecordRedLight else CyanPrimary
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(height),
        horizontalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until barCount) {
            val factor = 0.3f + 0.7f * kotlin.math.abs(sin(i * 0.45 + (amplitude * 5f)).toFloat())
            val targetHeightFraction = if (amplitude > 0.05f) {
                (factor * amplitude).coerceIn(0.15f, 1.0f)
            } else {
                0.12f
            }

            val animatedFraction by animateFloatAsState(
                targetValue = targetHeightFraction,
                animationSpec = tween(durationMillis = 120),
                label = "bar_anim_$i"
            )

            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight(animatedFraction)
                    .background(
                        color = activeColor.copy(alpha = if (animatedFraction > 0.2f) 0.9f else 0.4f),
                        shape = RoundedCornerShape(2.dp)
                    )
            )
        }
    }
}
