package com.zeddihub.gearos.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.zeddihub.gearos.ui.theme.StatusOffline
import com.zeddihub.gearos.ui.theme.StatusOnline
import com.zeddihub.gearos.ui.theme.StatusWarn
import com.zeddihub.gearos.ui.theme.Surface800
import com.zeddihub.gearos.ui.theme.TextSecondary

/**
 * Common UI komponenty pro GearOS — drží sjednocený look napříč všemi
 * dashboard stránkami. Wear OS Compose má omezené set built-in komponent,
 * takže pár custom helperů pro typical patterns:
 *   - StatusDot — kruhový indikátor (online/offline/warn)
 *   - SectionHeader — label nad seznamem
 *   - InfoChip — kompaktní pill s ikonou + textem
 *   - ZeddiCard — surface card s rounded rohem + okrajem
 *   - ZeddiActionChip — primary akční řádek (jeden tap pro akci)
 */

enum class StatusKind { Online, Warn, Offline, Unknown }

fun StatusKind.toColor(): Color = when (this) {
    StatusKind.Online -> StatusOnline
    StatusKind.Warn -> StatusWarn
    StatusKind.Offline -> StatusOffline
    StatusKind.Unknown -> TextSecondary
}

@Composable
fun StatusDot(kind: StatusKind, size: Int = 8) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(kind.toColor()),
    )
}

@Composable
fun SectionHeader(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.caption3,
        color = MaterialTheme.colors.onSurfaceVariant,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier.padding(start = 8.dp, top = 4.dp, bottom = 4.dp),
    )
}

@Composable
fun ZeddiCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Surface800,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(backgroundColor)
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        content()
    }
}

/**
 * Kompaktní info pill: ikona vlevo + text vpravo. Pro stat strip nahoře.
 *
 * Příklad:
 *   InfoPill("12", "SERVERS", AccentEmerald)
 */
@Composable
fun InfoPill(
    value: String,
    label: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(accent.copy(alpha = 0.14f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            color = accent,
            style = MaterialTheme.typography.title3,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = label,
            color = MaterialTheme.colors.onSurfaceVariant,
            style = MaterialTheme.typography.caption3,
        )
    }
}

/**
 * Primary action row — celá šířka, jeden tap, label + optional sub-label.
 * Tap target je celá karta (Wear best practice — žádné drobné buttons).
 */
@Composable
fun ZeddiActionChip(
    label: String,
    secondaryLabel: String? = null,
    icon: String? = null,
    accent: Color = MaterialTheme.colors.primary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Chip(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = ChipDefaults.chipColors(
            backgroundColor = Surface800,
            contentColor = MaterialTheme.colors.onSurface,
            secondaryContentColor = MaterialTheme.colors.onSurfaceVariant,
        ),
        icon = if (icon != null) {
            {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.20f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = icon, style = MaterialTheme.typography.title3)
                }
            }
        } else null,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.button,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colors.onSurface,
            )
        },
        secondaryLabel = if (secondaryLabel != null) {
            {
                Text(
                    text = secondaryLabel,
                    style = MaterialTheme.typography.caption2,
                    color = MaterialTheme.colors.onSurfaceVariant,
                )
            }
        } else null,
    )
}

@Composable
fun ServerRow(
    name: String,
    detail: String,
    status: StatusKind,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Chip(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = ChipDefaults.chipColors(
            backgroundColor = Surface800,
            contentColor = MaterialTheme.colors.onSurface,
            secondaryContentColor = MaterialTheme.colors.onSurfaceVariant,
        ),
        icon = {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(status.toColor().copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center,
            ) {
                StatusDot(kind = status, size = 10)
            }
        },
        label = {
            Text(
                text = name,
                style = MaterialTheme.typography.button,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colors.onSurface,
            )
        },
        secondaryLabel = {
            Text(
                text = detail,
                style = MaterialTheme.typography.caption2,
                color = MaterialTheme.colors.onSurfaceVariant,
            )
        },
    )
}

@Composable
fun ConnectionBadge(
    online: Boolean,
    label: String,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (online) StatusOnline.copy(alpha = 0.15f) else StatusOffline.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        StatusDot(kind = if (online) StatusKind.Online else StatusKind.Offline, size = 6)
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.caption2,
            color = if (online) StatusOnline else StatusOffline,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
fun VerticalGap(dp: Int = 6) {
    Spacer(Modifier.height(dp.dp))
}
