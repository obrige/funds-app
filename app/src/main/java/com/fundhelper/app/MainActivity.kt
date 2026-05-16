package com.fundhelper.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.fundhelper.app.data.PrefsDataStore
import com.fundhelper.app.ui.navigation.FundNavHost
import com.fundhelper.app.ui.theme.FundHelperTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var prefs: PrefsDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val darkMode by prefs.darkMode.collectAsState(initial = false)
            val systemDark = isSystemInDarkTheme()
            val useDark = darkMode || systemDark

            FundHelperTheme(darkTheme = useDark) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FundNavHost()
                }
            }
        }
    }
}
