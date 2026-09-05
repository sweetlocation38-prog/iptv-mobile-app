package com.thierry.iptvplayer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.thierry.iptvplayer.GroupMode
import com.thierry.iptvplayer.data.model.Channel
import com.thierry.iptvplayer.data.model.ChannelGroup
import com.thierry.iptvplayer.ui.theme.IndustrialColors

@Composable
fun ChannelListScreen(
    groups: List<ChannelGroup>,
    onGroupModeChange: (GroupMode) -> Unit,
    onChannelClick: (Channel) -> Unit,
    onAddFavorite: (Channel) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IndustrialColors.Background)
            .padding(20.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChipButton("PAYS") { onGroupModeChange(GroupMode.PAYS) }
            FilterChipButton("CATÉGORIE") { onGroupModeChange(GroupMode.CATEGORIE) }
            FilterChipButton("QUALITÉ") { onGroupModeChange(GroupMode.QUALITE) }
        }
        Spacer(Modifier.height(16.dp))

        if (groups.isEmpty()) {
            Text(
                "Aucune chaîne pour l'instant. Configure une source M3U ou Xtream Codes dans Réglages.",
                color = IndustrialColors.TextSecondary
            )
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            items(groups) { group ->
                Column {
                    Text(
                        text = group.label.uppercase(),
                        color = IndustrialColors.Accent,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    group.channels.forEach { channel ->
                        ChannelRow(channel, onChannelClick, onAddFavorite)
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterChipButton(label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = IndustrialColors.SurfaceVariant,
        shape = MaterialTheme.shapes.extraSmall
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            color = IndustrialColors.TextPrimary,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
private fun ChannelRow(
    channel: Channel,
    onChannelClick: (Channel) -> Unit,
    onAddFavorite: (Channel) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onChannelClick(channel) }
            .background(IndustrialColors.Surface)
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(channel.name, color = IndustrialColors.TextPrimary)
            Text(
                "${channel.country} · ${channel.category} · ${channel.quality}",
                color = IndustrialColors.TextSecondary,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Text(
            "+ FAVORI",
            color = IndustrialColors.Accent,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.clickable { onAddFavorite(channel) }
        )
    }
}
