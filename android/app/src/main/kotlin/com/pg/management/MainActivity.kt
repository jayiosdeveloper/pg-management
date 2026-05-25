package com.pg.management

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import com.pg.management.ui.navigation.AppNavigator
import com.pg.management.ui.screens.splash.SplashViewModel
import com.pg.management.ui.theme.PgTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Hold the system splash until the auth state is determined.
        var keepSplash = true
        splash.setKeepOnScreenCondition { keepSplash }

        setContent {
            PgTheme {
                val splashVm: SplashViewModel = hiltViewModel()
                val state by splashVm.state.collectAsState()
                keepSplash = !state.ready
                if (state.ready) {
                    AppNavigator(startDestination = state.startDestination)
                }
            }
        }
    }
}
