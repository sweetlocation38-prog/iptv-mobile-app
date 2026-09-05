package com.thierry.iptvplayer.network

import okhttp3.OkHttpClient
import okhttp3.Request
import okio.buffer
import okio.sink
import java.io.ByteArrayOutputStream

data class SpeedTestResult(
    val mbps: Double,
    val success: Boolean,
    val message: String
)

/**
 * Test de débit À LA DEMANDE uniquement (pas de vérification automatique en tâche
 * de fond). Télécharge un fichier de taille connue et mesure le débit réel obtenu
 * sur ta connexion actuelle (via le VPN si Proton VPN est actif au moment du test).
 *
 * Utilise l'endpoint public de test de débit Cloudflare (pas d'inscription requise).
 * L'URL est modifiable si besoin dans les réglages plus tard.
 */
class SpeedTestClient(
    private val client: OkHttpClient = OkHttpClient(),
    private val testUrl: String = "https://speed.cloudflare.com/__down?bytes=25000000"
) {

    fun runTest(): SpeedTestResult {
        return try {
            val request = Request.Builder().url(testUrl).build()
            val startTime = System.nanoTime()
            var totalBytes = 0L

            client.newCall(request).execute().use { response ->
                val body = response.body ?: return SpeedTestResult(0.0, false, "Pas de réponse du serveur de test")
                val source = body.source()
                val buffer = okio.Buffer()
                while (true) {
                    val read = source.read(buffer, 65536)
                    if (read == -1L) break
                    totalBytes += read
                    buffer.clear()
                }
            }

            val elapsedSeconds = (System.nanoTime() - startTime) / 1_000_000_000.0
            if (elapsedSeconds <= 0.0 || totalBytes == 0L) {
                return SpeedTestResult(0.0, false, "Test interrompu, réessaie")
            }

            val mbps = (totalBytes * 8) / elapsedSeconds / 1_000_000.0
            SpeedTestResult(
                mbps = mbps,
                success = true,
                message = "Débit mesuré : %.1f Mbps".format(mbps)
            )
        } catch (e: Exception) {
            SpeedTestResult(0.0, false, "Échec du test : ${e.message ?: "erreur réseau"}")
        }
    }
}
