package org.tetra.adas

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import org.tetra.adas.pages.App

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        resizable = false,
        state = WindowState(width = (1280/ 1.5).dp, height = (720/1.5).dp),
        title = "ADAS",
    ) {
        App()
    }
}