package com.zeddihub.gearos.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.HorizontalPageIndicator
import androidx.wear.compose.material.PageIndicatorState
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import com.zeddihub.gearos.ui.actions.ActionsPage
import com.zeddihub.gearos.ui.home.DashboardPage
import com.zeddihub.gearos.ui.pairing.PairingScreen
import com.zeddihub.gearos.ui.servers.ServersPage
import com.zeddihub.gearos.ui.settings.SettingsPage
import com.zeddihub.gearos.ui.theme.GearOsTheme
import kotlinx.coroutines.flow.collectLatest

/**
 * Root composable — wraps celou GearOS aplikaci.
 *
 * Architektura (v0.2.0 modern UI):
 *   - SwipeDismissableNavHost — Wear OS standard, swipe-right-to-back gesture
 *   - Pairing flow (first run) → po úspěchu navigace na "main" paged dashboard
 *   - Main dashboard — HorizontalPager se 4 stránkami:
 *       0. Dashboard — connection + stats + top servers + quick actions
 *       1. Servers — plný seznam s ping + status
 *       2. Actions — TOTP, push, find PC, panic
 *       3. Settings — sync, lang, watch face, logout
 *   - Wear OS chrome (TimeText nahoře, PageIndicator vpravo, Vignette okraj fade)
 *
 * M1+: persistovaný stav (pairing flag v AppPreferences) bude rozhodovat,
 * jestli startovat na Pairing nebo Main. Teď pro demo skipujeme Pairing
 * a rovnou Main.
 */
@Composable
fun GearOsRoot() {
    GearOsTheme {
        val nav = rememberNavController()
        SwipeDismissableNavHost(
            navController = nav,
            startDestination = Destinations.Main,
        ) {
            composable(Destinations.Pairing) {
                PairingScreen(
                    onPaired = {
                        nav.navigate(Destinations.Main) {
                            popUpTo(Destinations.Pairing) { inclusive = true }
                        }
                    }
                )
            }
            composable(Destinations.Main) {
                MainPagedDashboard()
            }
        }
    }
}

@Composable
private fun MainPagedDashboard() {
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 4 })

    // Per-page scroll state — drží scroll pozici i když uživatel přejde na
    // jinou stránku a vrátí se. Důležité protože ScalingLazyColumn defaultně
    // ztratí svůj state při recompose mimo viewport.
    val dashState = rememberScalingLazyListState()
    val serversState = rememberScalingLazyListState()
    val actionsState = rememberScalingLazyListState()
    val settingsState = rememberScalingLazyListState()

    val positionIndicatorState = when (pagerState.currentPage) {
        0 -> dashState
        1 -> serversState
        2 -> actionsState
        else -> settingsState
    }

    val pageIndicatorState = remember(pagerState) {
        object : PageIndicatorState {
            override val pageOffset: Float
                get() = pagerState.currentPageOffsetFraction
            override val selectedPage: Int
                get() = pagerState.currentPage
            override val pageCount: Int
                get() = pagerState.pageCount
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        timeText = { TimeText() },
        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) },
        positionIndicator = {
            PositionIndicator(scalingLazyListState = positionIndicatorState)
        },
        pageIndicator = {
            HorizontalPageIndicator(pageIndicatorState = pageIndicatorState)
        },
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                when (page) {
                    0 -> DashboardPage(listState = dashState)
                    1 -> ServersPage(listState = serversState)
                    2 -> ActionsPage(listState = actionsState)
                    3 -> SettingsPage(listState = settingsState)
                }
            }
        }
    }
}

object Destinations {
    const val Pairing = "pairing"
    const val Main = "main"
}
