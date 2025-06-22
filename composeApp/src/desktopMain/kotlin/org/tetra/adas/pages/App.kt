package org.tetra.adas.pages

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.Navigator
import java.awt.Toolkit

@Composable
fun App() {
    Navigator(screen = Splash())
}

fun getDesktopDensity(): Float {
    val toolkit = Toolkit.getDefaultToolkit()
    val screenResolution = toolkit.screenResolution // DPI
    return screenResolution / 160f // Android's base DPI is 160
}

fun pxToDpDesktop(px: Float): Dp {
    return (px / getDesktopDensity()).dp
}