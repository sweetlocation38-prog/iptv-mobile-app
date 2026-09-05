package com.thierry.iptvplayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.thierry.iptvplayer.data.model.Channel
import com.thierry.iptvplayer.ui.screens.*
import com.thierry.iptvplayer.ui.theme.IPTVPlayerTheme
import com.thierry.iptvplayer.ui.theme.IndustrialColors

private enum class Tab(val label: String) { CHAINES("Chaînes"), FAVORIS("Favoris"), VITESSE("Vitesse"), REGLAGES("Réglages") }

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IPTVPlayerTheme {
                var currentTab by remember { mutableStateOf(Tab.CHAINES) }
                var playingChannel by remember { mutableStateOf<Channel?>(null) }

                val groups by viewModel.groups.collectAsState()
                val favorites by viewModel.favorites.collectAsState()
                val speedResult by viewModel.speedTestResult.collectAsState()
                val isTestingSpeed by viewModel.isTestingSpeed.collectAsState()
                val protonDetected = remember { viewModel.isProtonVpnInstalled() }

                if (playingChannel != null) {
                    PlayerScreen(
                        streamUrl = playingChannel!!.streamUrl,
                        onExit = { playingChannel = null }
                    )
                } else {
                    Scaffold(
                        containerColor = IndustrialColors.Background,
                        bottomBar = {
                            NavigationBar(containerColor = IndustrialColors.Surface) {
                                NavigationBarItem(
                                    selected = currentTab == Tab.CHAINES,
                                    onClick = { currentTab = Tab.CHAINES },
                                    icon = { Icon(Icons.Filled.List, contentDescription = null) },
                                    label = { Text("Chaînes") }
                                )
                                NavigationBarItem(
                                    selected = currentTab == Tab.FAVORIS,
                                    onClick = { currentTab = Tab.FAVORIS },
                                    icon = { Icon(Icons.Filled.Favorite, contentDescription = null) },
                                    label = { Text("Favoris") }
                                )
                                NavigationBarItem(
                                    selected = currentTab == Tab.VITESSE,
                                    onClick = { currentTab = Tab.VITESSE },
                                    icon = { Icon(Icons.Filled.Speed, contentDescription = null) },
                                    label = { Text("Vitesse") }
                                )
                                NavigationBarItem(
                                    selected = currentTab == Tab.REGLAGES,
                                    onClick = { currentTab = Tab.REGLAGES },
                                    icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                                    label = { Text("Réglages") }
                                )
                            }
                        }
                    ) { padding ->
                        Box(modifier = Modifier.padding(padding)) {
                            when (currentTab) {
                                Tab.CHAINES -> ChannelListScreen(
                                    groups = groups,
                                    onGroupModeChange = { viewModel.setGroupMode(it) },
                                    onChannelClick = { playingChannel = it },
                                    onAddFavorite = { viewModel.addFavorite("Général", it) }
                                )
                                Tab.FAVORIS -> FavoritesScreen(themes = favorites)
                                Tab.VITESSE -> VpnSpeedScreen(
                                    isTesting = isTestingSpeed,
                                    result = speedResult,
                                    onRunTest = { viewModel.runSpeedTestNow() },
                                    onDismissResult = { viewModel.dismissSpeedTestResult() },
                                    protonVpnDetected = protonDetected,
                                    onOpenProtonVpn = { viewModel.openProtonVpn() }
                                )
                                Tab.REGLAGES -> SettingsScreen(
                                    currentConfig = viewModel.getSourceConfig(),
                                    onSave = { viewModel.saveSourceConfig(it) },
                                    onForceUpdate = { viewModel.refreshChannelsFromSources() },
                                    protonVpnDetected = protonDetected,
                                    onOpenProtonVpn = { viewModel.openProtonVpn() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
