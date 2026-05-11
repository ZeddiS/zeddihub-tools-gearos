package com.zeddihub.gearos.ui.servers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.zeddihub.gearos.data.demo.DemoData
import com.zeddihub.gearos.ui.common.ServerRow
import com.zeddihub.gearos.ui.theme.AccentEmerald

/**
 * Page 1 — Plný seznam serverů s ScalingLazyColumn. Status, ping, players.
 * Tap na server → server detail screen (M2 — control actions: restart/stop).
 *
 * Bezel-driven scroll funguje automaticky díky ScalingLazyColumn + Horologist
 * RotaryHandler v parent SwipeDismissableNavHost.
 */
@Composable
fun ServersPage(
    listState: ScalingLazyListState = rememberScalingLazyListState(),
) {
    ScalingLazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        item {
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "SERVERY",
                    style = MaterialTheme.typography.caption2,
                    color = AccentEmerald,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "${DemoData.servers.count { it.players != "Offline" }} / ${DemoData.servers.size} online",
                    style = MaterialTheme.typography.title3,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colors.onSurface,
                )
            }
        }
        items(DemoData.servers) { server ->
            ServerRow(
                name = server.name,
                detail = if (server.ping != null) "${server.players} · ${server.ping} ms" else server.players,
                status = server.status,
                onClick = { /* M2: detail */ },
            )
        }
    }
}
