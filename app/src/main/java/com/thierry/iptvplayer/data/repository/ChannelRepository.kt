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

    private val store = PlaylistStore(context)
    private val m3uParser = M3UParser()

    /** Chaînes actuellement en cache local (chargées instantanément, sans réseau) */
    fun getCachedChannels(): List<Channel> = store.loadChannels()

    fun getConfig(): SourceConfig = store.loadConfig()

    fun saveConfig(config: SourceConfig) = store.saveConfig(config)

    /**
     * Recharge les chaînes depuis les sources configurées et les met en cache.
     * Ne récupère PAS l'EPG (guide des programmes) — cf. demande initiale.
     */
    fun refreshChannels(): List<Channel> {
        val config = store.loadConfig()
        val channels = mutableListOf<Channel>()

        config.m3uUrl?.takeIf { it.isNotBlank() }?.let { url ->
            runCatching { channels.addAll(m3uParser.fetchAndParse(url)) }
        }

        if (!config.xtreamHost.isNullOrBlank() && !config.xtreamUser.isNullOrBlank()) {
            runCatching {
                val client = XtreamCodeClient(
                    host = config.xtreamHost,
                    username = config.xtreamUser,
                    password = config.xtreamPass.orEmpty()
                )
                val categories = client.fetchLiveCategories()
                channels.addAll(client.fetchLiveStreams(categories))
            }
        }

        store.saveChannels(channels)
        return channels
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
