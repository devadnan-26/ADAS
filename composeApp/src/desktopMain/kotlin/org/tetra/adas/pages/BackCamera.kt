package org.tetra.adas.pages

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.tetra.adas.network.Websocket

class BackCamera : Screen {



    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        var cameraPainter by remember { mutableStateOf<Painter?>(null) }
        var isConnecting by remember { mutableStateOf(true) }

        LaunchedEffect(Unit) {
            Websocket().connectToBackCamera { painter ->
                cameraPainter = painter
                isConnecting = false
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            cameraPainter?.let { painter ->
                Image(
                    painter = painter,
                    contentDescription = "Back Camera",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Canvas(modifier = Modifier.fillMaxSize()) {
                safetyLines()
            }
//            Button(onClick = {
//                navigator.push(Dashboard())
//            }, modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp), colors = ButtonDefaults.buttonColors(backgroundColor = Color.Black), border = BorderStroke(3.dp, Color.White)) {
//                Text("Back")
//            }
        }
    }

    private fun DrawScope.safetyLines() {
        val strokeWidth = 5.dp.toPx()

        fun drawTrapezoid(
            topLeft: Offset,
            topRight: Offset,
            bottomRight: Offset,
            bottomLeft: Offset,
            color: Color
        ) {
            val path = Path().apply {
                moveTo(topLeft.x, topLeft.y)
                lineTo(topRight.x, topRight.y)
                lineTo(bottomRight.x, bottomRight.y)
                lineTo(bottomLeft.x, bottomLeft.y)
                close()
            }

            // Fill
            drawPath(path, color.copy(alpha = 0.3f))

            // Outline
            val points = listOf(topLeft, topRight, bottomRight, bottomLeft)
            for (i in points.indices) {
                drawLine(
                    color = color,
                    start = points[i],
                    end = points[(i + 1) % points.size],
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
            }
        }

        // Green trapezoid (inner)
        drawTrapezoid(
            topLeft = Offset(571f * 0.765f, 347f * 0.765f),
            topRight = Offset(721f * 0.765f, 347f * 0.765f),
            bottomRight = Offset(813.5f * 0.765f, 487f * 0.765f),
            bottomLeft = Offset(467f * 0.765f, 487f * 0.765f),
            color = Color.Green
        )

        // Yellow trapezoid (middle)
        drawTrapezoid(
            topLeft = Offset(467f * 0.765f, 487f * 0.765f),
            topRight = Offset(813.5f * 0.765f, 487f * 0.765f),
            bottomRight = Offset(893f * 0.765f, 606f * 0.765f),
            bottomLeft = Offset(386.5f * 0.765f, 606f * 0.765f),
            color = Color.Yellow
        )

        // Red trapezoid (outer)
        drawTrapezoid(
            topLeft = Offset(386.5f * 0.765f, 606f * 0.765f),
            topRight = Offset(893f * 0.765f, 606f * 0.765f),
            bottomRight = Offset(969f * 0.765f, 720f * 0.765f),
            bottomLeft = Offset(311f * 0.765f, 720f * 0.765f),
            color = Color.Red
        )
    }

}