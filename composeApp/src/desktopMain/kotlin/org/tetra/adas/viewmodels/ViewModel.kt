package org.tetra.adas.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

class ViewModel {
    var isDark by mutableStateOf(false)
        private set
    fun changeColorMode(value: Boolean) {
        isDark = value
    }
}