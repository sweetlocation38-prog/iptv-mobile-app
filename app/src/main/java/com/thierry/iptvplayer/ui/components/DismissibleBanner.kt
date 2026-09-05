package com.thierry.iptvplayer.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.thierry.iptvplayer.ui.theme.IndustrialColors

/**
 * Message temporaire (erreur, résultat de test de débit, etc.).
 * Toujours fermable : au clic sur la croix (télécommande TV = OK sur la croix,
 * téléphone = tap), ou avec le bouton "retour" (télécommande TV ou téléphone).
 * Jamais bloquant, jamais permanent.
 */
@Composable
fun DismissibleBanner(
    message: String,
    onDismiss: () -> Unit,
    isError: Boolean = false
) {
    BackHandler(onBack = onDismiss)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isError) IndustrialColors.SurfaceVariant else IndustrialColors.Surface
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = message,
            color = if (isError) IndustrialColors.Accent else IndustrialColors.TextPrimary,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Filled.Close,
            contentDescription = "Fermer",
            tint = IndustrialColors.TextSecondary,
            modifier = Modifier
                .clickable { onDismiss() }
                .padding(start = 12.dp)
        )
    }
}
