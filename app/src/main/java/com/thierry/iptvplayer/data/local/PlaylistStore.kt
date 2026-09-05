package com.thierry.iptvplayer.data.local

import android.content.Context
import com.thierry.iptvplayer.data.model.Channel
import com.thierry.iptvplayer.data.model.FavoriteTheme
import com.thierry.iptvplayer.data.model.Quality
import com.thierry.iptvplayer.data.model.SourceConfig
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * Persistance simple en JSON sur le stockage interne de l'appli (sans dépendance base de données,
 * suffisant pour quelques milliers de chaînes). Permet de garder les chaînes en cache entre les
 * lancements et de ne les re-télécharger qu'à la mise à jour demandée.
 */
class PlaylistStore(private val context: Context) {

    private val channelsFile get() = File(context.filesDir, "channels.json")
    private val favoritesFile get() = File(context.filesDir, "favorites.json")
    private val configFile get() = File(context.filesDir, "source_config.json")

    fun saveChannels(channels: List<Channel>) {
        val arr = JSONArray()
        channels.forEach { c ->
            val obj = JSONObject()
            obj.put("id", c.id)
            obj.put("name", c.name)
            obj.put("streamUrl", c.streamUrl)
            obj.put("logoUrl", c.logoUrl)
            obj.put("country", c.country)
            obj.put("category", c.category)
            obj.put("quality", c.quality.name)
            arr.put(obj)
        }
        channelsFile.writeText(arr.toString())
    }

    fun loadChannels(): List<Channel> {
        if (!channelsFile.exists()) return emptyList()
        val arr = JSONArray(channelsFile.readText())
        val list = mutableListOf<Channel>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            list.add(
                Channel(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    streamUrl = obj.getString("streamUrl"),
                    logoUrl = obj.optString("logoUrl", null),
                    country = obj.optString("country", "Inconnu"),
                    category = obj.optString("category", "Général"),
                    quality = runCatching { Quality.valueOf(obj.optString("quality", "INCONNUE")) }
                        .getOrDefault(Quality.INCONNUE)
                )
            )
        }
        return list
    }

    fun saveFavorites(themes: List<FavoriteTheme>) {
        val arr = JSONArray()
        themes.forEach { t ->
            val obj = JSONObject()
            obj.put("name", t.name)
            obj.put("channelIds", JSONArray(t.channelIds))
            arr.put(obj)
        }
        favoritesFile.writeText(arr.toString())
    }

    fun loadFavorites(): List<FavoriteTheme> {
        if (!favoritesFile.exists()) return emptyList()
        val arr = JSONArray(favoritesFile.readText())
        val list = mutableListOf<FavoriteTheme>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val ids = obj.getJSONArray("channelIds")
            val idList = mutableListOf<String>()
            for (j in 0 until ids.length()) idList.add(ids.getString(j))
            list.add(FavoriteTheme(obj.getString("name"), idList))
        }
        return list
    }

    fun saveConfig(config: SourceConfig) {
        val obj = JSONObject()
        obj.put("m3uUrl", config.m3uUrl)
        obj.put("xtreamHost", config.xtreamHost)
        obj.put("xtreamUser", config.xtreamUser)
        obj.put("xtreamPass", config.xtreamPass)
        configFile.writeText(obj.toString())
    }

    fun loadConfig(): SourceConfig {
        if (!configFile.exists()) return SourceConfig()
        val obj = JSONObject(configFile.readText())
        return SourceConfig(
            m3uUrl = obj.optString("m3uUrl", null),
            xtreamHost = obj.optString("xtreamHost", null),
            xtreamUser = obj.optString("xtreamUser", null),
            xtreamPass = obj.optString("xtreamPass", null)
        )
    }
}
