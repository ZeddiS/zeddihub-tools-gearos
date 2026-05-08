package com.zeddihub.gearos.ui.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.MaterialTheme

private val ZeddiHubColors = Colors(
    primary = ZeddiOrange,
    primaryVariant = ZeddiOrangeDim,
    secondary = StatusOnline,
    secondaryVariant = StatusWarn,
    background = BgBlack,
    surface = Surface900,
    error = StatusOffline,
    onPrimary = TextPrimary,
    onSecondary = BgBlack,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    onError = TextPrimary,
)

@Composable
fun GearOsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colors = ZeddiHubColors,
        content = content,
    )
}
