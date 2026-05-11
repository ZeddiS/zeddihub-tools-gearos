package com.zeddihub.gearos.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.zeddihub.gearos.R
import com.zeddihub.gearos.data.demo.DemoData
import com.zeddihub.gearos.ui.common.ConnectionBadge
import com.zeddihub.gearos.ui.common.InfoPill
import com.zeddihub.gearos.ui.common.SectionHeader
import com.zeddihub.gearos.ui.common.ServerRow
import com.zeddihub.gearos.ui.common.StatusDot
import com.zeddihub.gearos.ui.common.StatusKind
import com.zeddihub.gearos.ui.common.VerticalGap
import com.zeddihub.gearos.ui.common.ZeddiActionChip
import com.zeddihub.gearos.ui.common.ZeddiCard
import com.zeddihub.gearos.ui.theme.AccentBlue
import com.zeddihub.gearos.ui.theme.AccentEmerald
import com.zeddihub.gearos.ui.theme.StatusOnline
import com.zeddihub.gearos.ui.theme.ZeddiOrange

/**
 * Dashboard page (page 0) — connection + ping + alerts header,
 * pak preview top serverů a quick actions.
 *
 * Wear OS best practice: ScalingLazyColumn s autoCentering aby uživatel
 * bezel-rotated mohl scrollnout přes top/bottom edge. PageIndicator (overlay)
 * vidí v GearOsRoot, nemusíme ho tady řešit.
 */
@Composable
fun DashboardPage(
    listState: ScalingLazyListState = rememberScalingLazyListState(),
) {
    ScalingLazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        // Greeting
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            ) {
                Text(
                    text = "Vítej zpět",
                    style = MaterialTheme.typography.caption2,
                    color = MaterialTheme.colors.onSurfaceVariant,
                )
                Text(
                    text = DemoData.username,
                    style = MaterialTheme.typography.title2,
                    fontWeight = FontWeight.Bold,
                    color = ZeddiOrange,
                )
                VerticalGap(4)
                ConnectionBadge(
                    online = DemoData.phoneConnected,
                    label = if (DemoData.phoneConnected) "Telefon ✓" else "Standalone",
                )
            }
        }

        // Stats strip — 3 piluly
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                InfoPill(
                    value = "${DemoData.servers.size}",
                    label = "SERVERY",
                    accent = AccentEmerald,
                    modifier = Modifier.weight(1f),
                )
                InfoPill(
                    value = "${DemoData.ping}",
                    label = "ms PING",
                    accent = AccentBlue,
                    modifier = Modifier.weight(1f),
                )
                InfoPill(
                    value = "${DemoData.pendingAlerts}",
                    label = "ALERTY",
                    accent = if (DemoData.pendingAlerts > 0) ZeddiOrange else StatusOnline,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        // Top servers preview
        item {
            SectionHeader(
                text = "Top servery",
                modifier = Modifier.fillMaxWidth(),
            )
        }
        items(DemoData.servers.take(3)) { server ->
            ServerRow(
                name = server.name,
                detail = if (server.ping != null) "${server.players} · ${server.ping} ms" else server.players,
                status = server.status,
                onClick = { /* M2: navigate to server detail */ },
            )
        }

        // Quick actions
        item {
            SectionHeader(
                text = "Rychlé akce",
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            )
        }
        item {
            ZeddiActionChip(
                label = "Spustit / restartovat",
                secondaryLabel = "Lifecycle pro vybraný server",
                icon = "▶",
                accent = AccentEmerald,
                onClick = { /* M2 */ },
            )
        }
        item {
            ZeddiActionChip(
                label = "Ping všechny",
                secondaryLabel = "Refresh latency",
                icon = "📡",
                accent = AccentBlue,
                onClick = { /* M2 */ },
            )
        }
    }
}
