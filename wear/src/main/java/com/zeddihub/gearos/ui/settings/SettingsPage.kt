package com.zeddihub.gearos.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.zeddihub.gearos.BuildConfig
import com.zeddihub.gearos.ui.common.SectionHeader
import com.zeddihub.gearos.ui.common.ZeddiActionChip
import com.zeddihub.gearos.ui.theme.AccentSlate
import com.zeddihub.gearos.ui.theme.StatusOffline
import com.zeddihub.gearos.ui.theme.StatusOnline
import com.zeddihub.gearos.ui.theme.ZeddiOrange

/**
 * Page 3 — Nastavení: profil, sync, vzhled, o aplikaci.
 *
 * Wear OS pattern: Settings je vždy posledná stránka v paged dashboardu.
 * Drží destruktivní akce (logout, factory reset) dole, aby user museli
 * scrollnout — zabráníš accidentnímu logout swipe.
 */
@Composable
fun SettingsPage(
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
                    text = "NASTAVENÍ",
                    style = MaterialTheme.typography.caption2,
                    color = AccentSlate,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "ZeddiHub GearOS",
                    style = MaterialTheme.typography.title3,
                    color = ZeddiOrange,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "v${BuildConfig.VERSION_NAME} (build ${BuildConfig.VERSION_CODE})",
                    style = MaterialTheme.typography.caption3,
                    color = MaterialTheme.colors.onSurfaceVariant,
                )
            }
        }

        item { SectionHeader("Konektivita", modifier = Modifier.fillMaxWidth()) }
        item {
            ZeddiActionChip(
                label = "Synchronizovat s mobilem",
                secondaryLabel = "Refresh tokenu + server cache",
                icon = "🔄",
                accent = StatusOnline,
                onClick = { /* M1: trigger MessageClient sync */ },
            )
        }
        item {
            ZeddiActionChip(
                label = "Re-pair telefonu",
                secondaryLabel = "Zaregistrovat nový 6-digit kód",
                icon = "🔗",
                accent = AccentSlate,
                onClick = { /* M1: forget pairing + navigate to Pairing */ },
            )
        }

        item { SectionHeader("Vzhled", modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) }
        item {
            ZeddiActionChip(
                label = "Jazyk",
                secondaryLabel = "Čeština",
                icon = "🌍",
                accent = AccentSlate,
                onClick = { /* M5: lang picker */ },
            )
        }
        item {
            ZeddiActionChip(
                label = "Watch face",
                secondaryLabel = "ZeddiHub orange (default)",
                icon = "🎨",
                accent = ZeddiOrange,
                onClick = { /* M5: watch face installer */ },
            )
        }

        item { SectionHeader("Soukromí", modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) }
        item {
            ZeddiActionChip(
                label = "Telemetrie",
                secondaryLabel = "Anonymní statistiky · zapnuto",
                icon = "📊",
                accent = AccentSlate,
                onClick = { /* M3: telemetry toggle */ },
            )
        }
        item {
            ZeddiActionChip(
                label = "Geofence auto-mode",
                secondaryLabel = "Vypnuto · opt-in",
                icon = "📍",
                accent = AccentSlate,
                onClick = { /* M4: geofence opt-in */ },
            )
        }

        // Destruktivní akce dole
        item { SectionHeader("Účet", modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) }
        item {
            ZeddiActionChip(
                label = "Odhlásit se",
                secondaryLabel = "Smaže token, zachová pairing",
                icon = "↩",
                accent = StatusOffline,
                onClick = { /* M1: clear token + navigate to Pairing */ },
            )
        }

        item { Spacer(Modifier.height(8.dp)) }
    }
}
