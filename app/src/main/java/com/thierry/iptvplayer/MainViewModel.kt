package com.thierry.iptvplayer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.thierry.iptvplayer.data.model.Channel
import com.thierry.iptvplayer.data.model.ChannelGroup
import com.thierry.iptvplayer.data.model.FavoriteTheme
import com.thierry.iptvplayer.data.model.SourceConfig
import com.thierry.iptvplayer.data.repository.ChannelRepository
import com.thierry.iptvplayer.network.SpeedTestClient
import com.thierry.iptvplayer.network.SpeedTestResult
import com.thierry.iptvplayer.vpn.ProtonVpnBridge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class GroupMode { PAYS, CATEGORIE, QUALITE }

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ChannelRepository(application)
    private val vpnBridge = ProtonVpnBridge(application)
    private val speedTestClient = SpeedTestClient()

    private val _channels = MutableStateFlow<List<Channel>>(emptyList())
    val channels: StateFlow<List<Channel>> = _channels

    private val _groups = MutableStateFlow<List<ChannelGroup>>(emptyList())
    val groups: StateFlow<List<ChannelGroup>> = _groups

    private val _favorites = MutableStateFlow<List<FavoriteTheme>>(emptyList())
    val favorites: StateFlow<List<FavoriteTheme>> = _favorites

    private val _isUpdating = MutableStateFlow(false)
    val isUpdating: StateFlow<Boolean> = _isUpdating

    // Résultat de la dernière tentative d'enregistrement/mise à jour des sources,
    // affiché dans un bandeau fermable sur l'écran Réglages.
    private val _updateResult = MutableStateFlow<String?>(null)
    val updateResult: StateFlow<String?> = _updateResult
    private val _updateIsError = MutableStateFlow(false)
    val updateIsError: StateFlow<Boolean> = _updateIsError

    // Résultat du test de débit — rempli UNIQUEMENT quand l'utilisateur appuie sur
    // "Tester maintenant". Aucune vérification automatique, aucune notification.
    private val _speedTestResult = MutableStateFlow<SpeedTestResult?>(null)
    val speedTestResult: StateFlow<SpeedTestResult?> = _speedTestResult

    private val _isTestingSpeed = MutableStateFlow(false)
    val isTestingSpeed: StateFlow<Boolean> = _isTestingSpeed

    private var groupMode = GroupMode.PAYS

    init {
        _channels.value = repository.getCachedChannels()
        applyGrouping()
        refreshChannelsFromSources()
    }

    fun refreshChannelsFromSources() {
        viewModelScope.launch(Dispatchers.IO) {
            _isUpdating.value = true
            val result = runCatching { repository.refreshChannels() }
            result.onSuccess { r ->
                _channels.value = r.channels
                applyGrouping()
                _updateIsError.value = r.errorMessage != null
                _updateResult.value = when {
                    !r.sourceConfigured -> "Aucune source configurée. Renseigne une URL M3U ou des identifiants Xtream Codes."
                    r.errorMessage != null -> "Échec : ${r.errorMessage}"
                    else -> "${r.channels.size} chaînes mises à jour avec succès"
                }
            }.onFailure { e ->
                _updateIsError.value = true
                _updateResult.value = "Erreur inattendue : ${e.message ?: "réessaie"}"
            }
            _isUpdating.value = false
        }
    }

    fun dismissUpdateResult() {
        _updateResult.value = null
    }

    fun setGroupMode(mode: GroupMode) {
        groupMode = mode
        applyGrouping()
    }

    private fun applyGrouping() {
        _groups.value = when (groupMode) {
            GroupMode.PAYS -> repository.groupByCountry(_channels.value)
            GroupMode.CATEGORIE -> repository.groupByCategory(_channels.value)
            GroupMode.QUALITE -> repository.groupByQuality(_channels.value)
        }
    }

    fun saveSourceConfig(config: SourceConfig) {
        repository.saveConfig(config)
        refreshChannelsFromSources()
    }

    fun getSourceConfig(): SourceConfig = repository.getConfig()

    fun addFavorite(themeName: String, channel: Channel) {
        val current = _favorites.value.toMutableList()
        val theme = current.find { it.name == themeName }
        if (theme != null) {
            if (!theme.channelIds.contains(channel.id)) theme.channelIds.add(channel.id)
        } else {
            current.add(FavoriteTheme(themeName, mutableListOf(channel.id)))
        }
        _favorites.value = current
    }

    fun isProtonVpnInstalled(): Boolean = vpnBridge.isProtonVpnInstalled()

    fun openProtonVpn() {
        vpnBridge.launchProtonVpn()
    }

    /** Lancé UNIQUEMENT sur appui du bouton "Tester maintenant". */
    fun runSpeedTestNow() {
        viewModelScope.launch(Dispatchers.IO) {
            _isTestingSpeed.value = true
            _speedTestResult.value = speedTestClient.runTest()
            _isTestingSpeed.value = false
        }
    }

    fun dismissSpeedTestResult() {
        _speedTestResult.value = null
    }
}
