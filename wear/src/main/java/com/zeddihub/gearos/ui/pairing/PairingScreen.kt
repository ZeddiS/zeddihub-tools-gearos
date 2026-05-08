package com.zeddihub.gearos.ui.pairing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.zeddihub.gearos.R

/**
 * Pairing screen — uživatel zadá 6-digit kód vygenerovaný v admin/web nebo v mobile app.
 *
 * M1 plán:
 *  - Plné napojení na [PairingViewModel] s state machine
 *    (Idle / Entering / Verifying / Success / Error).
 *  - Rotary input (bezel) pro výběr číslice; tap = další pozice.
 *  - Wearable Data Layer cesta: pokud je telefon poblíž, automaticky
 *    se vyžádá token přes `MessageClient` a screen se přeskočí.
 *  - Standalone fallback: HTTP POST na `/api/watch/pair` s 6-digit kódem.
 *
 * Tato verze je M0 placeholder — UI běží, ale `onSubmit` jenom zavolá [onPaired].
 */
@Composable
fun PairingScreen(onPaired: () -> Unit) {
    var digits by remember { mutableStateOf(List(6) { -1 }) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Text(
                text = stringResource(R.string.pairing_title),
                style = MaterialTheme.typography.title3,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = stringResource(R.string.pairing_instruction),
                style = MaterialTheme.typography.caption2,
                color = MaterialTheme.colors.onSurfaceVariant,
            )
            DigitRow(digits = digits)
            Button(
                onClick = {
                    // M1: validuj kód → POST /api/watch/pair → ulož token → onPaired()
                    onPaired()
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.pairing_button_pair))
            }
        }
    }
}

@Composable
private fun DigitRow(digits: List<Int>) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        digits.forEach { d ->
            DigitCell(digit = d)
        }
    }
}

@Composable
private fun DigitCell(digit: Int) {
    Box(
        modifier = Modifier.size(width = 18.dp, height = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = if (digit < 0) "•" else digit.toString(),
            style = MaterialTheme.typography.body1,
            color = if (digit < 0) {
                MaterialTheme.colors.onSurfaceVariant
            } else {
                MaterialTheme.colors.primary
            },
            fontWeight = FontWeight.Bold,
        )
    }
}
