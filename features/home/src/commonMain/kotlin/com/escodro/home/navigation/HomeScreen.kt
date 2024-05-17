package com.escodro.home.navigation

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import com.escodro.appstate.AppState
import com.escodro.home.presentation.Home

/**
 * Alkaa's Home Screen.
 */
class HomeScreen(private val appState: AppState) : Screen {

    override val key: ScreenKey = uniqueScreenKey

    @Composable
    override fun Content() {
        Home(appState = appState)
    }
}
