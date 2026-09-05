package com.thierry.iptvplayer.data.parser

import com.thierry.iptvplayer.data.model.Channel
import com.thierry.iptvplayer.data.model.Quality
import okhttp3.OkHttpClient
import okhttp3.Request

/**
 * Parseur de playlists M3U/M3U8 génériques.
 * Lit les attributs standards des lignes #EXTINF : tvg-country, tvg-category / group-title,
 * et déduit la qualité depuis le nom de la chaîne (mots-clés HD/FHD/4K/UHD).
 */
class M3UParser(private val client: OkHttpClient = OkHttpClient()) {

    fun fetchAndParse(url: String): List<Channel> {
        val request = Request.Builder().url(url).build()
        client.newCall(request).execute().use { response ->
            val body = response.body?.string() ?: return emptyList()
            return parse(body)
        }
    }

    fun parse(content: String): List<Channel> {
        val lines = content.lines()
        val channels = mutableListOf<Channel>()
        var pendingName: String? = null
        var pendingCountry = "Inconnu"
        var pendingCategory = "Général"
        var pendingLogo: String? = null
        var counter = 0

        for (rawLine in lines) {
            val line = rawLine.trim()
            when {
                line.startsWith("#EXTINF") -> {
                    pendingName = line.substringAfterLast(",").trim()
                    pendingCountry = extractAttribute(line, "tvg-country") ?: "Inconnu"
                    pendingCategory = extractAttribute(line, "group-title")
                        ?: extractAttribute(line, "tvg-category")
                        ?: "Général"
                    pendingLogo = extractAttribute(line, "tvg-logo")
                }
                line.isNotBlank() && !line.startsWith("#") -> {
                    val name = pendingName ?: "Chaîne inconnue"
                    channels.add(
                        Channel(
                            id = "m3u_${counter++}",
                            name = name,
                            streamUrl = line,
                            logoUrl = pendingLogo,
                            country = pendingCountry,
                            category = pendingCategory,
                            quality = guessQuality(name)
                        )
                    )
                    pendingName = null
                    pendingLogo = null
                }
            }
        }
        return channels
    }

    private fun extractAttribute(line: String, key: String): String? {
        val regex = Regex("""$key="([^"]*)"""")
        return regex.find(line)?.groupValues?.get(1)?.takeIf { it.isNotBlank() }
    }

    private fun guessQuality(name: String): Quality {
        val upper = name.uppercase()
        return when {
            "UHD" in upper || "4K" in upper -> Quality.UHD
            "FHD" in upper || "1080" in upper -> Quality.FHD
            "HD" in upper -> Quality.HD
            "SD" in upper -> Quality.SD
            else -> Quality.INCONNUE
        }
    }
}
