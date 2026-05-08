package com.zeddihub.gearos.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import com.zeddihub.gearos.ui.home.HomeScreen
import com.zeddihub.gearos.ui.pairing.PairingScreen
import com.zeddihub.gearos.ui.theme.GearOsTheme

@Composable
fun GearOsRoot() {
    GearOsTheme {
        val nav = rememberNavController()
        SwipeDismissableNavHost(
            navController = nav,
            startDestination = Destinations.Pairing,
        ) {
            composable(Destinations.Pairing) {
                PairingScreen(
                    onPaired = { nav.navigate(Destinations.Home) }
                )
            }
            composable(Destinations.Home) {
                HomeScreen()
            }
        }
    }
}

object Destinations {
    const val Pairing = "pairing"
    const val Home = "home"
}
