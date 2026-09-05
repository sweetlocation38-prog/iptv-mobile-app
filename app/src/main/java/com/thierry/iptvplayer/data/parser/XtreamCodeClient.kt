package com.thierry.iptvplayer.data.parser

import com.thierry.iptvplayer.data.model.Channel
import com.thierry.iptvplayer.data.model.Quality
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray

/**
 * Client générique pour le protocole Xtream Codes (player_api.php).
 * Ceci implémente le format d'API standard utilisé par de nombreux panneaux IPTV légaux.
 * L'URL, l'utilisateur et le mot de passe sont fournis par ton fournisseur.
 */
class XtreamCodeClient(
    private val host: String,      // ex: http://exemple.com:8080
    private val username: String,
    private val password: String,
    private val client: OkHttpClient = OkHttpClient()
) {

    private fun apiUrl(action: String, params: String = ""): String =
        "$host/player_api.php?username=$username&password=$password&action=$action$params"

    private fun get(url: String): String {
        val request = Request.Builder().url(url).build()
        client.newCall(request).execute().use { response ->
            return response.body?.string() ?: ""
        }
    }

    /** Récupère les catégories de chaînes live (utile pour le regroupement par catégorie) */
    fun fetchLiveCategories(): Map<String, String> {
        val json = get(apiUrl("get_live_categories"))
        val result = mutableMapOf<String, String>()
        val arr = JSONArray(json)
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            result[obj.getString("category_id")] = obj.getString("category_name")
        }
        return result
    }

    /** Récupère la liste complète des chaînes live et construit les URLs de flux */
    fun fetchLiveStreams(categoriesById: Map<String, String>): List<Channel> {
        val json = get(apiUrl("get_live_streams"))
        val arr = JSONArray(json)
        val channels = mutableListOf<Channel>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val streamId = obj.getInt("stream_id")
            val name = obj.optString("name", "Chaîne $streamId")
            val categoryId = obj.optString("category_id", "")
            val streamUrl = "$host/live/$username/$password/$streamId.m3u8"
            channels.add(
                Channel(
                    id = "xc_$streamId",
                    name = name,
                    streamUrl = streamUrl,
                    logoUrl = obj.optString("stream_icon", null),
                    country = guessCountryFromName(name),
                    category = categoriesById[categoryId] ?: "Général",
                    quality = guessQuality(name)
                )
            )
        }
        return channels
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

    /**
     * Heuristique simple : beaucoup de panneaux Xtream préfixent le nom par le pays
     * entre crochets, ex: "FR | TF1 HD". À affiner selon ton fournisseur réel.
     */
    private fun guessCountryFromName(name: String): String {
        val match = Regex("""^([A-Z]{2,3})\s*[|:\-]""").find(name.trim())
        return match?.groupValues?.get(1) ?: "Inconnu"
    }
}
