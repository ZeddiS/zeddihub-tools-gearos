package com.zeddihub.gearos.ui.actions

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.zeddihub.gearos.data.demo.DemoData
import com.zeddihub.gearos.data.demo.DemoTotpEntry
import com.zeddihub.gearos.ui.common.SectionHeader
import com.zeddihub.gearos.ui.common.VerticalGap
import com.zeddihub.gearos.ui.common.ZeddiActionChip
import com.zeddihub.gearos.ui.common.ZeddiCard
import com.zeddihub.gearos.ui.theme.AccentPurple
import com.zeddihub.gearos.ui.theme.StatusInfo
import com.zeddihub.gearos.ui.theme.StatusOffline
import com.zeddihub.gearos.ui.theme.StatusWarn
import com.zeddihub.gearos.ui.theme.Surface700

/**
 * Page 2 — Rychlé akce: TOTP kódy, push, find my desktop, panic.
 *
 * TOTP karty: 6-digit kód s 30s rotation timer. M4 doplní real backing
 * store v EncryptedSharedPrefs synced z mobile, teď ukazujeme DemoTotpEntry.
 *
 * Panic button: dlouhý stisk (3s) + biometric confirm. Teď single tap pro demo.
 */
@Composable
fun ActionsPage(
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
                    text = "RYCHLÉ AKCE",
                    style = MaterialTheme.typography.caption2,
                    color = AccentPurple,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Power-user shortcuts",
                    style = MaterialTheme.typography.caption3,
                    color = MaterialTheme.colors.onSurfaceVariant,
                )
            }
        }

        // TOTP section
        item { SectionHeader("2FA Kódy", modifier = Modifier.fillMaxWidth()) }
        items(DemoData.totp) { entry -> TotpCard(entry) }

        // Other actions
        item { SectionHeader("Push & alerts", modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) }
        item {
            ZeddiActionChip(
                label = "Push notifikace",
                secondaryLabel = "${DemoData.pendingAlerts} nepřečtené",
                icon = "🔔",
                accent = StatusWarn,
                onClick = { /* M3: navigate to alerts feed */ },
            )
        }

        item { SectionHeader("Power tools", modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) }
        item {
            ZeddiActionChip(
                label = "Najdi PC",
                secondaryLabel = "Loud sound + flash na desktop",
                icon = "📡",
                accent = StatusInfo,
                onClick = { /* M5: POST /api/desktop/locate */ },
            )
        }
        item {
            ZeddiActionChip(
                label = "Macro trigger",
                secondaryLabel = "Spustit makro na PC",
                icon = "⚙",
                accent = AccentPurple,
                onClick = { /* M5: macro picker */ },
            )
        }

        // Panic — visually distinct (red bordered card)
        item { SectionHeader("Nouzové", modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) }
        item {
            ZeddiActionChip(
                label = "Panic — kill switch",
                secondaryLabel = "Long-press 3s + biometric",
                icon = "🆘",
                accent = StatusOffline,
                onClick = { /* M4: panic with biometric gate */ },
            )
        }

        // Bottom spacer pro safe scroll
        item { VerticalGap(8) }
    }
}

@Composable
private fun TotpCard(entry: DemoTotpEntry) {
    ZeddiCard {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = entry.label,
                    style = MaterialTheme.typography.caption2,
                    color = MaterialTheme.colors.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "${entry.secondsLeft}s",
                    style = MaterialTheme.typography.caption3,
                    color = if (entry.secondsLeft < 5) StatusOffline else MaterialTheme.colors.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(
                text = entry.code,
                style = MaterialTheme.typography.display3,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = AccentPurple,
            )
            // Progress strip
            ProgressStrip(progress = entry.secondsLeft / 30f)
        }
    }
}

@Composable
private fun ProgressStrip(progress: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(3.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(Surface700),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(if (progress < 0.16f) StatusOffline else AccentPurple),
        )
    }
}
