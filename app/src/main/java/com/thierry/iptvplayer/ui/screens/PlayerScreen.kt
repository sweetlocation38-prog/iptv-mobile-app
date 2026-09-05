package com.thierry.iptvplayer.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.PlayerView
import com.thierry.iptvplayer.ui.components.DismissibleBanner

/**
 * Lecteur réglé pour privilégier systématiquement la MEILLEURE qualité disponible :
 * - forceHighestSupportedBitrate désactive la logique "descendre la qualité pour
 *   fluidifier" du sélecteur de pistes adaptatif.
 * - Le buffer est volontairement plus grand que la valeur par défaut : on préfère
 *   patienter un peu plus au chargement plutôt que de dégrader la qualité en cours
 *   de lecture.
 */
@Composable
fun PlayerScreen(streamUrl: String, onExit: () -> Unit) {
    val context = LocalContext.current
    var errorMessage by remember { mutableStateOf<String?>(null) }

    BackHandler(onBack = onExit)

    val trackSelector = remember {
        DefaultTrackSelector(context).apply {
            parameters = buildUponParameters()
                .setForceHighestSupportedBitrate(true)
                .build()
        }
    }

    val loadControl = remember {
        DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                /* minBufferMs = */ 15_000,
                /* maxBufferMs = */ 50_000,
                /* bufferForPlaybackMs = */ 3_000,
                /* bufferForPlaybackAfterRebufferMs = */ 5_000
            )
            .build()
    }

    val exoPlayer = remember {
        ExoPlayer.Builder(context)
            .setTrackSelector(trackSelector)
            .setLoadControl(loadControl)
            .build()
            .apply {
                setMediaItem(MediaItem.fromUri(streamUrl))
                prepare()
                playWhenReady = true
                addListener(object : Player.Listener {
                    override fun onPlayerError(error: PlaybackException) {
                        errorMessage = "Lecture impossible : ${error.errorCodeName}"
                    }
                })
            }
    }

    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = {
            PlayerView(context).apply {
                player = exoPlayer
                useController = true
            }
        }
    )

    errorMessage?.let { msg ->
        DismissibleBanner(
            message = msg,
            onDismiss = { errorMessage = null },
            isError = true
        )
    }
}
