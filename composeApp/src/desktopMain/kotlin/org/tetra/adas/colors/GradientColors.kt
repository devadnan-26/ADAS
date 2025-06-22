package org.tetra.adas.colors

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.Canvas
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*

class GradientColors {
    // Main modifier function for animated gradients
    fun Modifier.animatedGradient(
        colors: List<Color>,
        gradientType: GradientType = GradientType.LINEAR,
        animationDuration: Int = 3000,
        enableRotation: Boolean = true,
        enableColorCycling: Boolean = true
    ): Modifier = composed {
        val infiniteTransition = rememberInfiniteTransition()

        // Color cycling animation
        val colorProgress by if (enableColorCycling) {
            infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(animationDuration, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
        } else {
            remember { mutableStateOf(0f) }
        }

        // Rotation animation
        val rotation by if (enableRotation) {
            infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(animationDuration * 2, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                )
            )
        } else {
            remember { mutableStateOf(0f) }
        }

        this.drawBehind {
            val animatedColors = if (enableColorCycling) {
                interpolateColors(colors, colorProgress)
            } else {
                colors
            }

            when (gradientType) {
                GradientType.LINEAR -> drawLinearGradient(animatedColors, rotation)
                GradientType.RADIAL -> drawRadialGradient(animatedColors)
                GradientType.SWEEP -> drawSweepGradient(animatedColors, rotation)
                GradientType.DIAMOND -> drawDiamondGradient(animatedColors, rotation)
            }
        }
    }

    // Pulsing gradient modifier with intensity animation
    fun Modifier.pulsingGradient(
        colors: List<Color> = listOf(
            Color(0xFF667eea),
            Color(0xFF764ba2)
        ),
        animationDuration: Int = 2000,
        minIntensity: Float = 0.3f,
        maxIntensity: Float = 1f
    ): Modifier = composed {
        val infiniteTransition = rememberInfiniteTransition()

        val intensity by infiniteTransition.animateFloat(
            initialValue = minIntensity,
            targetValue = maxIntensity,
            animationSpec = infiniteRepeatable(
                animation = tween(animationDuration, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        )

        this.drawBehind {
            val adjustedColors = colors.map { color ->
                Color(
                    red = color.red * intensity,
                    green = color.green * intensity,
                    blue = color.blue * intensity,
                    alpha = color.alpha
                )
            }

            val brush = Brush.linearGradient(
                colors = adjustedColors,
                start = Offset.Zero,
                end = Offset(size.width, size.height)
            )

            drawRect(brush = brush, size = size)
        }
    }

    // Shimmer effect modifier
    fun Modifier.shimmerGradient(
        colors: List<Color> = listOf(
            Color.White.copy(alpha = 0.3f),
            Color.White.copy(alpha = 0.1f),
            Color.White.copy(alpha = 0.3f)
        ),
        animationDuration: Int = 1500
    ): Modifier = composed {
        val infiniteTransition = rememberInfiniteTransition()

        val offset by infiniteTransition.animateFloat(
            initialValue = -1f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(animationDuration, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )

        this.drawBehind {
            val width = size.width
            val shimmerWidth = width * 0.3f
            val startX = width * offset - shimmerWidth
            val endX = startX + shimmerWidth

            val brush = Brush.linearGradient(
                colors = colors,
                start = Offset(startX, 0f),
                end = Offset(endX, 0f)
            )

            drawRect(brush = brush, size = size)
        }
    }

    // Canvas drawing functions
    private fun DrawScope.drawLinearGradient(colors: List<Color>, rotation: Float) {
        val angleRad = (rotation * PI / 180).toFloat()
        val startX = center.x - cos(angleRad) * size.width / 2
        val startY = center.y - sin(angleRad) * size.height / 2
        val endX = center.x + cos(angleRad) * size.width / 2
        val endY = center.y + sin(angleRad) * size.height / 2

        val brush = Brush.linearGradient(
            colors = colors,
            start = Offset(startX, startY),
            end = Offset(endX, endY)
        )

        drawRect(brush = brush, size = size)
    }

    private fun DrawScope.drawRadialGradient(colors: List<Color>) {
        val brush = Brush.radialGradient(
            colors = colors,
            center = center,
            radius = size.minDimension / 2
        )

        drawRect(brush = brush, size = size)
    }

    private fun DrawScope.drawSweepGradient(colors: List<Color>, rotation: Float) {
        val brush = Brush.sweepGradient(
            colors = colors,
            center = center
        )

        drawRect(brush = brush, size = size)
    }

    private fun DrawScope.drawDiamondGradient(colors: List<Color>, rotation: Float) {
        val brush = Brush.radialGradient(
            colors = colors,
            center = center,
            radius = size.maxDimension
        )

        drawRect(brush = brush, size = size)
    }

    // Helper functions
    private fun interpolateColors(colors: List<Color>, progress: Float): List<Color> {
        if (colors.size < 2) return colors

        return colors.mapIndexed { index, color ->
            val nextIndex = (index + 1) % colors.size
            val nextColor = colors[nextIndex]
            val t = (sin(progress * PI * 2 + index * PI / colors.size) + 1) / 2
            lerp(color, nextColor, t.toFloat())
        }
    }

    enum class GradientType {
        LINEAR,
        RADIAL,
        SWEEP,
        DIAMOND
    }

    // Usage Examples
    @Composable
    fun GradientModifierExamples() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Animated gradient box
            Box(
                modifier = Modifier
                    .size(200.dp, 100.dp)
                    .animatedGradient(
                        colors = listOf(Color.Red, Color.Blue, Color.Green),
                        gradientType = GradientType.LINEAR
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Animated Box",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            // Gradient card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .animatedGradient(
                        colors = listOf(Color(0xFF667eea), Color(0xFF764ba2)),
                        gradientType = GradientType.RADIAL,
                        animationDuration = 4000
                    ),
                elevation = 8.dp
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Gradient Card",
                        fontSize = 20.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Pulsing gradient button
            Box(
                modifier = Modifier
                    .size(150.dp, 60.dp)
                    .pulsingGradient(
                        colors = listOf(Color.Magenta, Color.Cyan),
                        animationDuration = 1500
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Pulsing Button",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            // Shimmer effect
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(Color.Gray.copy(alpha = 0.3f), CircleShape)
                            .shimmerGradient()
                    )
                }
            }

            // Multiple modifiers combined
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .animatedGradient(
                        colors = listOf(
                            Color(0xFF1e3c72),
                            Color(0xFF2a5298),
                            Color(0xFFf093fb)
                        ),
                        gradientType = GradientType.SWEEP,
                        enableRotation = false
                    )
                    .shimmerGradient(
                        animationDuration = 2000
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Combined Effects",
                    fontSize = 18.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // Simple usage examples
    @Composable
    fun SimpleExamples() {
        // Apply to any Box
        Box(
            modifier = Modifier
                .size(100.dp)
                .animatedGradient(
                    colors = listOf(Color.Red, Color.Blue)
                )
        )

        // Apply to Text background
        Text(
            text = "Gradient Text",
            modifier = Modifier
                .animatedGradient(
                    colors = listOf(Color.Yellow, Color.Blue)
                )
                .padding(16.dp),
            color = Color.White
        )

        // Apply to Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .pulsingGradient()
        ) {
            Text(
                text = "Content",
                modifier = Modifier.padding(16.dp)
            )
        }
    }
    @Composable
    fun rememberAnimatedLinearBrush(
        colors: List<Color>,
        animationDuration: Int = 3000,
        enableColorCycling: Boolean = true,
        enableRotation: Boolean = true
    ): Brush {
        val infiniteTransition = rememberInfiniteTransition()

        val colorProgress by if (enableColorCycling) {
            infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(animationDuration, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
        } else {
            remember { mutableStateOf(0f) }
        }

        val rotation by if (enableRotation) {
            infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(animationDuration * 2, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                )
            )
        } else {
            remember { mutableStateOf(0f) }
        }

        val animatedColors = if (enableColorCycling) {
            interpolateColors(colors, colorProgress)
        } else {
            colors
        }

        val angleRad = (rotation * PI / 180).toFloat()

        return Brush.linearGradient(
            colors = animatedColors,
            start = Offset(
                x = 0.5f + 0.5f * cos(angleRad),
                y = 0.5f + 0.5f * sin(angleRad)
            ),
            end = Offset(
                x = 0.5f - 0.5f * cos(angleRad),
                y = 0.5f - 0.5f * sin(angleRad)
            )
        )
    }

    @Composable
    fun rememberAnimatedRadialBrush(
        colors: List<Color>,
        animationDuration: Int = 3000,
        enableColorCycling: Boolean = true,
        enablePulsing: Boolean = true
    ): Brush {
        val infiniteTransition = rememberInfiniteTransition()

        val colorProgress by if (enableColorCycling) {
            infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(animationDuration, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
        } else {
            remember { mutableStateOf(0f) }
        }

        val radiusMultiplier by if (enablePulsing) {
            infiniteTransition.animateFloat(
                initialValue = 0.5f,
                targetValue = 1.5f,
                animationSpec = infiniteRepeatable(
                    animation = tween(animationDuration / 2, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
        } else {
            remember { mutableStateOf(1f) }
        }

        val animatedColors = if (enableColorCycling) {
            interpolateColors(colors, colorProgress)
        } else {
            colors
        }

        return Brush.radialGradient(
            colors = animatedColors,
            center = Offset(0.5f, 0.5f),
            radius = radiusMultiplier
        )
    }

    @Composable
    fun rememberAnimatedSweepBrush(
        colors: List<Color>,
        animationDuration: Int = 3000,
        enableColorCycling: Boolean = true
    ): Brush {
        val infiniteTransition = rememberInfiniteTransition()

        val colorProgress by if (enableColorCycling) {
            infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(animationDuration, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
        } else {
            remember { mutableStateOf(0f) }
        }

        val animatedColors = if (enableColorCycling) {
            interpolateColors(colors, colorProgress)
        } else {
            colors
        }

        return Brush.sweepGradient(
            colors = animatedColors,
            center = Offset(0.5f, 0.5f)
        )
    }

    fun lerp(start: Color, stop: Color, fraction: Float): Color {
        return Color(
            red = lerp(start.red, stop.red, fraction),
            green = lerp(start.green, stop.green, fraction),
            blue = lerp(start.blue, stop.blue, fraction),
            alpha = lerp(start.alpha, stop.alpha, fraction)
        )
    }

    fun lerp(start: Float, stop: Float, fraction: Float): Float {
        return start + fraction * (stop - start)
    }

    // Example usage with drawArc()
    @Composable
    fun AnimatedArcExamples() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            val brush = rememberAnimatedLinearBrush(
                colors = listOf(Color.Red, Color.Blue, Color.Green),
                animationDuration = 2000
            )
            val brush_1 = rememberAnimatedSweepBrush(
                colors = listOf(Color(0xFF667eea), Color(0xFF764ba2), Color(0xFFf093fb)),
                animationDuration = 4000
            )
            val brush_2 = rememberAnimatedRadialBrush(
                colors = listOf(Color.Magenta, Color.Cyan, Color.Yellow),
                animationDuration = 3000
            )
            // Linear gradient arc
            androidx.compose.foundation.Canvas(
                modifier = Modifier.size(200.dp)
            ) {
                drawArc(
                    brush = brush,
                    startAngle = -90f,
                    sweepAngle = 270f,
                    useCenter = false,
                    style = Stroke(width = 20.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // Radial gradient arc
            Canvas(
                modifier = Modifier.size(200.dp)
            ) {

                drawArc(
                    brush = brush_2,
                    startAngle = 0f,
                    sweepAngle = 180f,
                    useCenter = true
                )
            }

            // Sweep gradient arc
            Canvas(
                modifier = Modifier.size(200.dp)
            ) {
                drawArc(
                    brush = brush_1,
                    startAngle = 45f,
                    sweepAngle = 270f,
                    useCenter = false,
                    style = Stroke(width = 15.dp.toPx())
                )
            }
        }
    }

    // Advanced example: Progress indicator with animated gradient
    @Composable
    fun AnimatedProgressArc(
        progress: Float, // 0f to 1f
        modifier: Modifier = Modifier,
        colors: List<Color> = listOf(Color.Green, Color.Blue),
        strokeWidth: Float = 12f,
        animationDuration: Int = 2000
    ) {
        val animatedProgress by animateFloatAsState(
            targetValue = progress,
            animationSpec = tween(1000, easing = FastOutSlowInEasing)
        )
        val brush = rememberAnimatedLinearBrush(
            colors = colors,
            animationDuration = animationDuration
        )
        Canvas(modifier = modifier.size(120.dp)) {
            drawArc(
                color = Color.Gray.copy(alpha = 0.3f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth.dp.toPx(), cap = StrokeCap.Round)
            )

            // Progress arc with animated gradient
            drawArc(
                brush = brush,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                style = Stroke(width = strokeWidth.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }

    // Multiple arcs with different gradients
    @Composable
    fun MultipleAnimatedArcs() {
        val radialBrush = rememberAnimatedRadialBrush(
            colors = listOf(Color.Cyan, Color.Green),
            animationDuration = 2000
        )
        val sweepBrush = rememberAnimatedSweepBrush(
            colors = listOf(Color.Red, Color.Blue, Color.Yellow),
            animationDuration = 3000
        )
        val linearBrush = rememberAnimatedLinearBrush(
            colors = listOf(Color.Blue, Color.Cyan, Color.Magenta),
            animationDuration = 2500
        )
        Canvas(
            modifier = Modifier.size(250.dp)
        ) {
            val center = Offset(size.width / 2, size.height / 2)
            val maxRadius = size.minDimension / 2

            drawArc(
                brush = sweepBrush,
                startAngle = 0f,
                sweepAngle = 300f,
                useCenter = false,
                style = Stroke(width = 16.dp.toPx()),
                size = Size(maxRadius * 1.8f, maxRadius * 1.8f),
                topLeft = Offset(
                    center.x - maxRadius * 0.9f,
                    center.y - maxRadius * 0.9f
                )
            )


            drawArc(
                brush = linearBrush,
                startAngle = 45f,
                sweepAngle = 270f,
                useCenter = false,
                style = Stroke(width = 12.dp.toPx()),
                size = Size(maxRadius * 1.4f, maxRadius * 1.4f),
                topLeft = Offset(
                    center.x - maxRadius * 0.7f,
                    center.y - maxRadius * 0.7f
                )
            )

            // Inner arc - Radial gradient

            drawArc(
                brush = radialBrush,
                startAngle = 90f,
                sweepAngle = 180f,
                useCenter = true,
                size = Size(maxRadius, maxRadius),
                topLeft = Offset(
                    center.x - maxRadius * 0.5f,
                    center.y - maxRadius * 0.5f
                )
            )
        }
    }

    // Usage examples showcase
    @Composable
    fun DrawArcShowcase() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AnimatedArcExamples()

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AnimatedProgressArc(
                    progress = 0.75f,
                    colors = listOf(Color.Green, Color.Blue)
                )

                AnimatedProgressArc(
                    progress = 0.5f,
                    colors = listOf(Color.Red, Color.Yellow)
                )
            }

            MultipleAnimatedArcs()
        }
    }
    @Composable
    fun rememberPulsatingMixedBrush(
        baseColors: List<Color>,
        animationDuration: Int = 5000,
        pulsationIntensity: Float = 0.8f,
        mixingSpeed: Float = 1f,
        gradientType: GradientType = GradientType.RADIAL
    ): Brush {
        val infiniteTransition = rememberInfiniteTransition()

        // Main pulsation wave
        val pulsation by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 2f * PI.toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(animationDuration, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )

        // Secondary mixing wave (different frequency)
        val mixingWave by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 4f * PI.toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween((animationDuration * (2f / mixingSpeed)).toInt(), easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )

        // Color shifting wave
        val colorShift by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = baseColors.size.toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(animationDuration * 3, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )

        val mixedColors = remember(pulsation, mixingWave, colorShift) {
            createDynamicMixedColors(
                baseColors = baseColors,
                pulsation = pulsation,
                mixingWave = mixingWave,
                colorShift = colorShift,
                intensity = pulsationIntensity
            )
        }

        return when (gradientType) {
            GradientType.LINEAR -> Brush.linearGradient(
                colors = mixedColors,
                start = Offset(0f, 0f),
                end = Offset(1f, 1f)
            )
            GradientType.RADIAL -> {
                val radiusPulse = 0.5f + 0.3f * sin(pulsation * 2f)
                Brush.radialGradient(
                    colors = mixedColors,
                    center = Offset(0.5f, 0.5f),
                    radius = radiusPulse
                )
            }
            GradientType.SWEEP -> Brush.sweepGradient(
                colors = mixedColors,
                center = Offset(0.5f, 0.5f)
            )

            GradientType.DIAMOND -> TODO()
        }
    }

    // Advanced color mixing with multiple wave functions
    private fun createDynamicMixedColors(
        baseColors: List<Color>,
        pulsation: Float,
        mixingWave: Float,
        colorShift: Float,
        intensity: Float
    ): List<Color> {
        val numOutputColors = 5 // Generate 5 mixed colors for smooth gradient

        return (0 until numOutputColors).map { index ->
            val t = index.toFloat() / (numOutputColors - 1)

            // Multiple wave functions for complex mixing
            val wave1 = sin(pulsation + t * PI * 2) * intensity
            val wave2 = cos(mixingWave * 0.7f + t * PI * 3) * intensity * 0.6f
            val wave3 = sin(colorShift * PI + t * PI * 1.5f) * intensity * 0.4f

            // Combine waves
            val combinedWave = (wave1 + wave2 + wave3) / 3f
            val normalizedWave = (combinedWave + 1f) / 2f // Normalize to 0-1

            // Select base colors to mix
            val colorIndex1 = ((colorShift + t * baseColors.size) % baseColors.size).toInt()
            val colorIndex2 = ((colorIndex1 + 1) % baseColors.size)
            val colorIndex3 = ((colorIndex1 + 2) % baseColors.size)

            val color1 = baseColors[colorIndex1]
            val color2 = baseColors[colorIndex2]
            val color3 = baseColors[colorIndex3]

            // Dynamic mixing ratios based on waves
            val mix1 = 0.4f + normalizedWave * 0.4f
            val mix2 = 0.3f + sin(pulsation * 1.3f + t * PI) * 0.2f
            val mix3 = 0.3f + cos(mixingWave * 0.8f + t * PI * 2) * 0.2f

            // Normalize mixing ratios
            val totalMix = mix1 + mix2 + mix3
            val normalizedMix1 = (mix1 / totalMix).toFloat()
            val normalizedMix2 = (mix2 / totalMix).toFloat()
            val normalizedMix3 = (mix3 / totalMix).toFloat()

            // Mix the colors
            mixColors(
                listOf(color1, color2, color3),
                listOf(normalizedMix1, normalizedMix2, normalizedMix3)
            )
        }
    }
    private fun mixColors(colors: List<Color>, weights: List<Float>): Color {
        var red = 0f
        var green = 0f
        var blue = 0f
        var alpha = 0f

        colors.forEachIndexed { index, color ->
            val weight = weights.getOrElse(index) { 0f }
            red += color.red * weight
            green += color.green * weight
            blue += color.blue * weight
            alpha += color.alpha * weight
        }

        return Color(
            red = red.coerceIn(0f, 1f),
            green = green.coerceIn(0f, 1f),
            blue = blue.coerceIn(0f, 1f),
            alpha = alpha.coerceIn(0f, 1f)
        )
    }
}