package org.tetra.adas.pages

import KottieAnimation
import adas.composeapp.generated.resources.Res
import adas.composeapp.generated.resources.logo
import adas.composeapp.generated.resources.montserrat
import adas.composeapp.generated.resources.navigator
import adas.composeapp.generated.resources.weather
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.delay
import kottieComposition.KottieCompositionSpec
import kottieComposition.animateKottieCompositionAsState
import kottieComposition.rememberKottieComposition
import org.jetbrains.compose.resources.painterResource
import org.tetra.adas.colors.DarkColors
import org.tetra.adas.data.Sign
import java.time.LocalTime
import java.time.format.DateTimeFormatter



fun getCurrentTime(): String {
    val now = LocalTime.now()
    val formatter = DateTimeFormatter.ofPattern("HH:mm")
    return now.format(formatter)
}

class Dashboard : Screen {
    var imageData by  mutableStateOf<MutableList<Sign>>(mutableListOf())
    @Composable
    override fun Content() {



        MaterialTheme {
            // Variable definitions START
            val navigator = LocalNavigator.currentOrThrow
            var showContent by remember { mutableStateOf(false) }
            var time by remember { mutableStateOf(getCurrentTime()) }
            var animation by remember { mutableStateOf("") }
            var playing by remember { mutableStateOf(true) }
            val composition = rememberKottieComposition(
                spec = KottieCompositionSpec.File(animation)
            )

            val animationState by animateKottieCompositionAsState(
                composition = composition,
                isPlaying = playing,
                iterations = Int.MAX_VALUE
            )
            //Variable definitions END

            // Suspend functions block START
            LaunchedEffect(Unit) {
                animation = Res.readBytes("files/road.json").decodeToString()
            }

            LaunchedEffect(Unit) {
                delay(2000)
                showContent = true
                while (true) {
                    delay(1000L)
                    time = getCurrentTime()
                }
            }
            // Suspend functions block END


            // Road and navigator
            Box(
                modifier = Modifier.fillMaxSize().safeContentPadding().background(DarkColors.black)
            ) {
                // Road animation
                Box(modifier = Modifier.fillMaxSize()) {
                    KottieAnimation(
                        composition = composition,
                        progress =
                            {
                                animationState.progress
                            },
                        modifier = Modifier.fillMaxSize()
                    )
                    // Navigator
                    Image(
                        painterResource(Res.drawable.navigator),
                        contentDescription = null,
                        modifier = Modifier.align(Alignment.BottomCenter)
                            .padding(bottom = 24.dp).size(96.dp)
                    )
                }
                // Top panel
                Column(modifier = Modifier.fillMaxSize()) {
                    // The arc (background)
                    Box(modifier = Modifier.height(125.dp).fillMaxWidth()) {
                        Canvas(modifier = Modifier.height(125.dp).fillMaxWidth()) {

                            val expandedHeight = size.height * 2f
                            val arcRect = Rect(
                                offset = Offset(
                                    0f,
                                    -size.height
                                ), // shift it up so bottom half is shown
                                size = Size(size.width, expandedHeight)
                            )

                            drawArc(
                                color = Color(0xFFFFDE21),
                                startAngle = 0f,           // starts at right side
                                sweepAngle = 180f,         // sweeps bottom half clockwise to left
                                useCenter = false,
                                topLeft = Offset(
                                    arcRect.topLeft.x - 100,
                                    arcRect.topLeft.y
                                ),
                                size = Size(arcRect.size.width + 200, arcRect.size.height)
                            )
                        }
                        // Greeting message with logo
                        Column(
                            modifier = Modifier.padding(4.dp).padding(top = 4.dp)
                                .align(Alignment.TopCenter),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            Image(
                                painter = painterResource(Res.drawable.logo),
                                contentDescription = "logo",
                                modifier = Modifier.size(
                                    64.dp
                                )
                            )
                            Spacer(modifier = Modifier.padding(top = 4.dp))
                            Text(
                                "HOŞ GELDİNİZ",
                                fontSize = 28.sp,
                                color = DarkColors.white,
                                fontFamily = FontFamily(
                                    org.jetbrains.compose.resources.Font(
                                        Res.font.montserrat,
                                        weight = FontWeight.Black
                                    )
                                )
                            )
                        }
                        // Time info
                        Text(
                            time,
                            fontSize = 22.sp,
                            color = DarkColors.white,
                            fontFamily = FontFamily(
                                org.jetbrains.compose.resources.Font(
                                    Res.font.montserrat,
                                    weight = FontWeight.Black
                                )
                            ),
                            modifier = Modifier.align(Alignment.TopEnd)
                                .padding(end = 8.dp, top = 8.dp)
                        )
                        // Weather info
                        Row(modifier = Modifier.align(
                            Alignment.TopStart
                        ).padding(start = 8.dp, top = 8.dp), verticalAlignment = Alignment.CenterVertically) {

                            Icon(
                                painterResource(Res.drawable.weather),
                                contentDescription = "weather",
                                modifier = Modifier.size(24.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.padding(2.dp))
                            Text(
                                "23°C",
                                fontSize = 22.sp,
                                color = DarkColors.white,
                                fontFamily = FontFamily(
                                    org.jetbrains.compose.resources.Font(
                                        Res.font.montserrat,
                                        weight = FontWeight.Black
                                    )
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.padding(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                        AnimatedVisibility(visible = imageData.isNotEmpty(), enter = fadeIn(), exit = fadeOut()) {
                            Image(painterResource(imageData[0].image), contentDescription = null, modifier = Modifier.size(72.dp))
                        }
                        Spacer(modifier = Modifier.padding(8.dp))
                        AnimatedVisibility(visible = imageData.size > 1, enter = fadeIn(), exit = fadeOut()) {
                            Image(painterResource(imageData[1].image), contentDescription = null, modifier = Modifier.size(72.dp))
                        }
                    }
                }
            }
        }
    }
}