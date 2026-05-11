package com.zeddihub.gearos.data.demo

import com.zeddihub.gearos.ui.common.StatusKind

/**
 * v0.2.0 — Demo data pro UI scaffold. Slouží jako fallback dokud
 * Wearable Data Layer (M1) nepřivede real-time data z mobile companion.
 *
 * Layout matches what `zeddihub_tools_mobile` Dashboard zobrazuje, aby
 * uživatel viděl stejné servery na zápěstí jako v telefonu.
 */

data class DemoServer(
    val id: String,
    val name: String,
    val players: String,
    val ping: Int?,
    val status: StatusKind,
)

data class DemoAlert(
    val id: String,
    val title: String,
    val ago: String,
    val isCritical: Boolean = false,
)

data class DemoTotpEntry(
    val label: String,
    val code: String,
    val secondsLeft: Int,
)

object DemoData {
    val servers = listOf(
        DemoServer("rust-pve", "Rust PVE", "14 / 60", 28, StatusKind.Online),
        DemoServer("cs2-awp", "CS2 AWP", "8 / 16", 32, StatusKind.Online),
        DemoServer("csgo-surf", "CS:GO Surf", "Offline", null, StatusKind.Offline),
        DemoServer("multigames", "MultiGames", "3 / 32", 41, StatusKind.Warn),
    )

    val alerts = listOf(
        DemoAlert("a1", "Rust PVE: 90% RAM", "2m", isCritical = false),
        DemoAlert("a2", "Login z neznámé IP", "12m", isCritical = true),
        DemoAlert("a3", "GearOS pairing OK", "1h"),
    )

    val totp = listOf(
        DemoTotpEntry("ZeddiHub Admin", "584 219", 17),
        DemoTotpEntry("zeddihub.eu (root)", "902 471", 17),
    )

    const val phoneConnected = true
    const val username = "Zeddi"
    const val ping = 28
    const val pendingAlerts = 2
}
