package com.thierry.iptvplayer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.thierry.iptvplayer.network.SpeedTestResult
import com.thierry.iptvplayer.ui.components.DismissibleBanner
import com.thierry.iptvplayer.ui.theme.IndustrialColors

/**
 * Onglet "Vitesse VPN". Aucune vérification automatique : le test ne se lance
 * QUE quand tu appuies sur le bouton, typiquement quand tu as un souci de
 * connexion. Le résultat s'affiche dans un message temporaire, fermable.
 */
@Composable
fun VpnSpeedScreen(
    isTesting: Boolean,
    result: SpeedTestResult?,
    onRunTest: () -> Unit,
    onDismissResult: () -> Unit,
    protonVpnDetected: Boolean,
    onOpenProtonVpn: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IndustrialColors.Background)
            .padding(20.dp)
    ) {
        Text(
            "VITESSE VPN",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = IndustrialColors.TextPrimary
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "À utiliser quand tu as des coupures ou des soucis de connexion. " +
                "Le test mesure ton débit réel du moment (à travers Proton VPN si actif).",
            color = IndustrialColors.TextSecondary,
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(Modifier.height(32.dp))
        Button(onClick = onRunTest, enabled = !isTesting) {
            Text(if (isTesting) "TEST EN COURS…" else "TESTER MAINTENANT")
        }

        if (protonVpnDetected) {
            Spacer(Modifier.height(16.dp))
            OutlinedButton(onClick = onOpenProtonVpn) {
                Text("OUVRIR PROTON VPN POUR CHANGER DE PAYS")
            }
        }

        Spacer(Modifier.weight(1f))

        result?.let {
            DismissibleBanner(
                message = it.message,
                onDismiss = onDismissResult,
                isError = !it.success
            )
        }
    }
}
