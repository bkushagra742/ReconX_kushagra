package com.kushagra.reconx

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import com.kushagra.reconx.ui.navigation.AppNavHost
import com.kushagra.reconx.ui.theme.ReconXTheme

/**
 * MainActivity.kt
 * =================
 * Single-Activity host for the entire Compose UI. Reads the user's
 * Appearance preference (system / light / dark) from PreferencesManager
 * (via SettingsViewModel-equivalent flow read directly here to decide the
 * theme before the first frame) and hands off everything else to
 * [AppNavHost].
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as ReconXApplication

        setContent {
            val themeMode by app.preferencesManager.themeMode.collectAsState(initial = "system")
            val useDarkTheme = when (themeMode) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }

            ReconXTheme(darkTheme = useDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    AppNavHost(app)
                }
            }
        }
    }
}
