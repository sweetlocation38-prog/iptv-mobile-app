package com.thierry.iptvplayer.data.repository

import android.content.Context
import com.thierry.iptvplayer.data.local.PlaylistStore
import com.thierry.iptvplayer.data.model.Channel
import com.thierry.iptvplayer.data.model.ChannelGroup
import com.thierry.iptvplayer.data.model.SourceConfig
import com.thierry.iptvplayer.data.parser.M3UParser
import com.thierry.iptvplayer.data.parser.XtreamCodeClient

/**
 * Point d'entrée unique pour charger / rafraîchir les chaînes, quelle que soit la source
 * (M3U et/ou Xtream Codes peuvent être combinés). C'est ici qu'a lieu la "mise à jour au
 * démarrage" demandée, volontairement sans récupération d'EPG pour rester rapide.
 */
class ChannelRepository(context: Context) {

    data class RefreshResult(
        val channels: List<Channel>,
        val errorMessage: String?,
        val sourceConfigured: Boolean
    )

    private val store = PlaylistStore(context)
    private val m3uParser = M3UParser()

    /** Chaînes actuellement en cache local (chargées instantanément, sans réseau) */
    fun getCachedChannels(): List<Channel> = store.loadChannels()

    fun getConfig(): SourceConfig = store.loadConfig()

    fun saveConfig(config: SourceConfig) = store.saveConfig(config)

    /**
     * Recharge les chaînes depuis les sources configurées et les met en cache.
     * Ne récupère PAS l'EPG (guide des programmes) — cf. demande initiale.
     * Retourne aussi un message d'erreur explicite si une source échoue, pour que
     * l'écran Réglages puisse te dire clairement ce qui s'est passé.
     */
    fun refreshChannels(): RefreshResult {
        val config = store.loadConfig()
        val channels = mutableListOf<Channel>()
        var sourceConfigured = false
        var errorMessage: String? = null

        config.m3uUrl?.takeIf { it.isNotBlank() }?.let { url ->
            sourceConfigured = true
            runCatching { channels.addAll(m3uParser.fetchAndParse(url)) }
                .onFailure { errorMessage = "M3U : ${it.message ?: "échec de connexion"}" }
        }

        if (!config.xtreamHost.isNullOrBlank() && !config.xtreamUser.isNullOrBlank()) {
            sourceConfigured = true
            runCatching {
                val resolvedHost = XtreamCodeClient.resolveBaseUrl(config.xtreamHost)
                    ?: throw Exception("Aucun port trouvé automatiquement pour ce serveur. Vérifie l'adresse ou ajoute le port donné par ton fournisseur (ex: :8080).")
                val client = XtreamCodeClient(
                    host = resolvedHost,
                    username = config.xtreamUser,
                    password = config.xtreamPass.orEmpty()
                )
                val categories = client.fetchLiveCategories()
                channels.addAll(client.fetchLiveStreams(categories))
            }.onFailure { errorMessage = "Xtream Codes : ${it.message ?: "échec de connexion"}" }
        }

        store.saveChannels(channels)
        return RefreshResult(channels, errorMessage, sourceConfigured)
    }

    fun groupByCountry(channels: List<Channel>): List<ChannelGroup> =
        channels.groupBy { it.country }
            .toSortedMap()
            .map { (country, list) -> ChannelGroup(country, list) }

    fun groupByCategory(channels: List<Channel>): List<ChannelGroup> =
        channels.groupBy { it.category }
            .toSortedMap()
            .map { (category, list) -> ChannelGroup(category, list) }

    fun groupByQuality(channels: List<Channel>): List<ChannelGroup> =
        channels.groupBy { it.quality.name }
            .toSortedMap()
            .map { (quality, list) -> ChannelGroup(quality, list) }
}
