package com.thierry.iptvplayer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.thierry.iptvplayer.data.model.FavoriteTheme
import com.thierry.iptvplayer.ui.theme.IndustrialColors

@Composable
fun FavoritesScreen(themes: List<FavoriteTheme>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IndustrialColors.Background)
            .padding(20.dp)
    ) {
        Text(
            "FAVORIS",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = IndustrialColors.TextPrimary
        )
        Spacer(Modifier.height(20.dp))

        if (themes.isEmpty()) {
            Text(
                "Aucun favori pour l'instant. Ajoute des chaînes depuis l'onglet Chaînes.",
                color = IndustrialColors.TextSecondary
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                items(themes) { theme ->
                    Column {
                        Text(
                            theme.name.uppercase(),
                            color = IndustrialColors.Accent,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        theme.channelIds.forEach { id ->
                            Text("• $id", color = IndustrialColors.TextPrimary)
                        }
                    }
                }
            }
        }
    }
}
