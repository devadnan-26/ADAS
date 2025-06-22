package org.tetra.adas.pages

import KottieAnimation
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.painterResource
import adas.composeapp.generated.resources.Res
import adas.composeapp.generated.resources.hmtlogo
import adas.composeapp.generated.resources.logo
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.defaultNavigatorSaver
import de.drick.compose.hotpreview.HotPreview
import kotlinx.coroutines.delay
import kottieComposition.KottieCompositionSpec
import kottieComposition.animateKottieCompositionAsState
import kottieComposition.rememberKottieComposition


class Splash: Screen {
    @Composable
    override fun Content() {
        MaterialTheme {
            var showContent by remember { mutableStateOf(false) }
            val navigator = LocalNavigator.currentOrThrow
            LaunchedEffect(Unit) {
                delay(1000)
                showContent = true
                delay(4000)
                showContent = false
                delay(1000)
                navigator.push(Dashboard())
            }
            Box(
                modifier = Modifier
                    .safeContentPadding()
                    .fillMaxSize()
                    .background(Color(0xFF2b2b2b)),
                contentAlignment = Alignment.Center,
            ) {
                AnimatedVisibility(visible = showContent, enter = fadeIn(animationSpec = tween(durationMillis = 500)), exit = fadeOut(animationSpec = tween(durationMillis = 500))) {
                    Image(
                        painterResource(Res.drawable.hmtlogo),
                        contentDescription = null,
                        modifier = Modifier.size(200.dp)
                    )
                }
            }
        }
    }
}