package com.zeddihub.gearos.ui.pairing

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.zeddihub.gearos.R
import com.zeddihub.gearos.ui.theme.StatusOffline
import com.zeddihub.gearos.ui.theme.Surface700
import com.zeddihub.gearos.ui.theme.Surface800
import com.zeddihub.gearos.ui.theme.ZeddiOrange

/**
 * Pairing screen (v0.2.0 redesign).
 *
 * UX:
 *   1. Welcome card (logo + slogan)
 *   2. 6 prázdných pozic pro pairing code — uživatel zadá z mobile/web
 *   3. Numerický keypad pro tap input (bezel rotary input integrovaný v M1)
 *   4. "Spárovat" tlačítko aktivuje se až po všech 6 číslicích
 *
 * V M1 dorefactor:
 *   - Wearable Data Layer auto-fetch z mobile (pokud BT range)
 *   - Bezel rotary input pro každou číslici (Horologist RotaryHandler)
 *   - State machine: Idle → Entering → Verifying → Success → Error
 *   - Real API call POST /api/watch/pair s 6-digit kódem
 */
@Composable
fun PairingScreen(onPaired: () -> Unit) {
    var digits by remember { mutableStateOf(List(6) { -1 }) }
    var currentSlot by remember { mutableStateOf(0) }

    val allFilled = digits.all { it >= 0 }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
        ) {
            // Hero
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(ZeddiOrange.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "⌚",
                    style = MaterialTheme.typography.title2,
                )
            }
            Text(
                text = stringResource(R.string.pairing_title),
                style = MaterialTheme.typography.title3,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colors.onSurface,
            )
            Text(
                text = stringResource(R.string.pairing_instruction),
                style = MaterialTheme.typography.caption2,
                color = MaterialTheme.colors.onSurfaceVariant,
            )

            Spacer(Modifier.height(2.dp))
            DigitRow(digits = digits, currentSlot = currentSlot)

            Spacer(Modifier.height(2.dp))
            CompactKeypad(
                onDigit = { d ->
                    if (currentSlot < 6) {
                        digits = digits.toMutableList().also { it[currentSlot] = d }
                        currentSlot++
                    }
                },
                onBackspace = {
                    if (currentSlot > 0) {
                        currentSlot--
                        digits = digits.toMutableList().also { it[currentSlot] = -1 }
                    }
                },
            )

            Spacer(Modifier.height(2.dp))
            Button(
                onClick = {
                    // M1: real pair API call.
                    // Pro demo: jakýkoli plný kód projde.
                    if (allFilled) onPaired()
                },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = if (allFilled) ZeddiOrange else Surface700,
                    contentColor = if (allFilled) MaterialTheme.colors.onPrimary else MaterialTheme.colors.onSurfaceVariant,
                ),
                modifier = Modifier.size(width = 96.dp, height = 32.dp),
            ) {
                Text(
                    text = stringResource(R.string.pairing_button_pair),
                    style = MaterialTheme.typography.caption1,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun DigitRow(digits: List<Int>, currentSlot: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        digits.forEachIndexed { idx, d ->
            DigitCell(digit = d, active = idx == currentSlot)
        }
    }
}

@Composable
private fun DigitCell(digit: Int, active: Boolean) {
    val color = when {
        digit >= 0 -> ZeddiOrange
        active -> MaterialTheme.colors.primary
        else -> Surface700
    }
    Box(
        modifier = Modifier
            .size(width = 20.dp, height = 28.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (digit >= 0) ZeddiOrange.copy(alpha = 0.18f) else Surface800)
            .padding(2.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = if (digit < 0) (if (active) "_" else "·") else digit.toString(),
            style = MaterialTheme.typography.title3,
            color = color,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
        )
    }
}

/**
 * Mini numerický keypad — 3 řady po 3 číslicích + spodní řada (0, backspace).
 * Vejde se na 1.5"/1.3" obrazovku Galaxy Watch.
 *
 * M1 doplní bezel RotaryHandler — uživatel točí bezelem pro výběr číslice
 * v aktuální pozici (jako iOS PIN input), tap potvrzuje.
 */
@Composable
private fun CompactKeypad(
    onDigit: (Int) -> Unit,
    onBackspace: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        KeypadRow(listOf(1, 2, 3), onDigit)
        KeypadRow(listOf(4, 5, 6), onDigit)
        KeypadRow(listOf(7, 8, 9), onDigit)
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            KeyButton(label = "←", onClick = onBackspace, accent = StatusOffline)
            KeyButton(label = "0", onClick = { onDigit(0) })
            // Empty third cell pro alignment.
            Spacer(Modifier.size(width = 22.dp, height = 18.dp))
        }
    }
}

@Composable
private fun KeypadRow(digits: List<Int>, onDigit: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        digits.forEach { d ->
            KeyButton(label = d.toString(), onClick = { onDigit(d) })
        }
    }
}

@Composable
private fun KeyButton(
    label: String,
    onClick: () -> Unit,
    accent: androidx.compose.ui.graphics.Color = MaterialTheme.colors.onSurface,
) {
    Box(
        modifier = Modifier
            .size(width = 22.dp, height = 18.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(Surface800)
            .padding(1.dp),
        contentAlignment = Alignment.Center,
    ) {
        // Tap target via Button wrapping
        Button(
            onClick = onClick,
            modifier = Modifier.fillMaxSize(),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Surface800,
                contentColor = accent,
            ),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.caption2,
                fontWeight = FontWeight.SemiBold,
                color = accent,
            )
        }
    }
}
