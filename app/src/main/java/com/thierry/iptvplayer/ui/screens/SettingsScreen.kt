package com.thierry.iptvplayer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.thierry.iptvplayer.data.model.SourceConfig
import com.thierry.iptvplayer.ui.components.DismissibleBanner
import com.thierry.iptvplayer.ui.theme.IndustrialColors

@Composable
fun SettingsScreen(
    currentConfig: SourceConfig,
    onSave: (SourceConfig) -> Unit,
    onForceUpdate: () -> Unit,
    protonVpnDetected: Boolean,
    onOpenProtonVpn: () -> Unit,
    isUpdating: Boolean,
    updateResult: String?,
    updateIsError: Boolean,
    onDismissUpdateResult: () -> Unit
) {
    var m3uUrl by remember { mutableStateOf(currentConfig.m3uUrl ?: "") }
    var xtreamHost by remember { mutableStateOf(currentConfig.xtreamHost ?: "") }
    var xtreamUser by remember { mutableStateOf(currentConfig.xtreamUser ?: "") }
    var xtreamPass by remember { mutableStateOf(currentConfig.xtreamPass ?: "") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IndustrialColors.Background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            "RÉGLAGES",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = IndustrialColors.TextPrimary
        )
        Spacer(Modifier.height(20.dp))

        Text("Source M3U", color = IndustrialColors.TextSecondary)
        OutlinedTextField(
            value = m3uUrl,
            onValueChange = { m3uUrl = it },
            placeholder = { Text("https://.../playlist.m3u8") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))
        Text("Source Xtream Codes", color = IndustrialColors.TextSecondary)
        OutlinedTextField(
            value = xtreamHost,
            onValueChange = { xtreamHost = it },
            placeholder = { Text("http://serveur:port") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = xtreamUser,
            onValueChange = { xtreamUser = it },
            placeholder = { Text("Utilisateur") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = xtreamPass,
            onValueChange = { xtreamPass = it },
            placeholder = { Text("Mot de passe") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(20.dp))
        Text(
            if (protonVpnDetected) "Proton VPN détecté sur ce téléphone ✓"
            else "Proton VPN non détecté sur ce téléphone",
            color = if (protonVpnDetected) IndustrialColors.Accent else IndustrialColors.TextSecondary
        )
        if (protonVpnDetected) {
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = onOpenProtonVpn) { Text("OUVRIR PROTON VPN") }
        }

        Spacer(Modifier.height(28.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                enabled = !isUpdating,
                onClick = {
                    onSave(
                        SourceConfig(
                            m3uUrl = m3uUrl.ifBlank { null },
                            xtreamHost = xtreamHost.ifBlank { null },
                            xtreamUser = xtreamUser.ifBlank { null },
                            xtreamPass = xtreamPass.ifBlank { null }
                        )
                    )
                }
            ) { Text("ENREGISTRER") }

            OutlinedButton(enabled = !isUpdating, onClick = onForceUpdate) {
                Text("METTRE À JOUR")
            }

            if (isUpdating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = IndustrialColors.Accent,
                    strokeWidth = 2.dp
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        updateResult?.let {
            DismissibleBanner(
                message = it,
                onDismiss = onDismissUpdateResult,
                isError = updateIsError
            )
        }
    }
}
