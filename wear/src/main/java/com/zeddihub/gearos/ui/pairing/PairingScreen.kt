package com.zeddihub.gearos.ui.pairing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Watch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.zeddihub.gearos.R

/**
 * Placeholder pro pairing screen.
 * M1 doplní:
 *  - 6-digit number pad (rotary input pro každou číslici)
 *  - Wearable Data Layer pull existujícího tokenu z mobilu
 *  - PairingViewModel + PairingState (Idle/Entering/Verifying/Success/Error)
 */
@Composable
fun PairingScreen(onPaired: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Watch,
                contentDescription = null,
                tint = MaterialTheme.colors.primary,
            )
            Text(
                text = stringResource(R.string.pairing_title),
                style = MaterialTheme.typography.title3,
            )
            Text(
                text = stringResource(R.string.pairing_instruction),
                style = MaterialTheme.typography.caption2,
                color = MaterialTheme.colors.onSurfaceVariant,
            )
            Button(onClick = onPaired) {
                Text(stringResource(R.string.pairing_button_pair))
            }
        }
    }
}
