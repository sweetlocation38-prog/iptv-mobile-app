package com.thierry.iptvplayer.data.model

enum class Quality { SD, HD, FHD, UHD, INCONNUE }

data class Channel(
    val id: String,
    val name: String,
    val streamUrl: String,
    val logoUrl: String? = null,
    val country: String = "Inconnu",
    val category: String = "Général",
    val quality: Quality = Quality.INCONNUE
)

data class FavoriteTheme(
    val name: String,
    val channelIds: MutableList<String> = mutableListOf()
)

data class ChannelGroup(
    val label: String,
    val channels: List<Channel>
)

data class SourceConfig(
    val m3uUrl: String? = null,
    val xtreamHost: String? = null,
    val xtreamUser: String? = null,
    val xtreamPass: String? = null
)
