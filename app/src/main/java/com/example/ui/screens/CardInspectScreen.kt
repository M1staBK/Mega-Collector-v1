package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.OwnedCard
import com.example.ui.components.HolographicCardView
import com.example.ui.sensors.rememberCardTiltState
import com.example.ui.theme.ArenaGold
import com.example.ui.theme.MisprintNeon
import com.example.ui.theme.MythicCrimson
import com.example.ui.theme.OutlineSubtle
import com.example.ui.theme.PrismCyan
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VoidObsidian

@Composable
fun CardInspectScreen(
    card: OwnedCard?,
    duplicateCount: Int,
    onBack: () -> Unit,
    onRecycle: (OwnedCard) -> Unit,
    onMintTradeSlip: (OwnedCard) -> Unit
) {
    val tiltState = rememberCardTiltState()
    var showRecycleConfirm by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        (card?.rarity?.primaryColor ?: PrismCyan).copy(alpha = 0.22f),
                        VoidObsidian
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        if (card == null) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Card no longer in Vault", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onBack) { Text("Back to Vault") }
            }
            return
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("inspect_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }

                Text(
                    text = "3D FOIL INSPECTOR",
                    style = MaterialTheme.typography.titleLarge,
                    color = ArenaGold
                )

                IconButton(
                    onClick = { tiltState.calibrateCenter() },
                    modifier = Modifier.testTag("inspect_recenter_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CenterFocusStrong,
                        contentDescription = "Recenter gyroscope angle",
                        tint = PrismCyan
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3D Gyroscope Card Showcase
            Box(
                modifier = Modifier.width(280.dp),
                contentAlignment = Alignment.Center
            ) {
                HolographicCardView(
                    card = card,
                    tiltState = tiltState,
                    interactiveTilt = true
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Gyroscope Telemetry Bar
            Surface(
                color = SurfaceElevated,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, OutlineSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ScreenRotation,
                            contentDescription = null,
                            tint = PrismCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (tiltState.isGyroAvailable) "Gyroscope Foil Active"
                                else "Touch-Drag Foil Active",
                                style = MaterialTheme.typography.labelLarge,
                                color = TextPrimary
                            )
                            Text(
                                text = stringResource(R.string.gyro_hint),
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }

                    Switch(
                        checked = tiltState.gyroEnabled,
                        onCheckedChange = { tiltState.gyroEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = VoidObsidian,
                            checkedTrackColor = PrismCyan
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Misprint Anomaly Callout (if 1/1)
            if (card.isMisprint && card.misprintAnomaly != null) {
                Surface(
                    color = SurfaceCard,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.5.dp, MisprintNeon),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = MisprintNeon,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "1-OF-1 MISPRINT • ${card.misprintAnomaly.title.uppercase()}",
                                style = MaterialTheme.typography.labelLarge,
                                color = MisprintNeon
                            )
                            Text(
                                text = card.misprintAnomaly.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Card Telemetry Details
            Surface(
                color = SurfaceElevated,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, OutlineSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TelemetryRow("Hero", card.heroName)
                    TelemetryRow("Skin Tier (API)", card.rawTier)
                    TelemetryRow("Collector Rarity", card.rarity.displayName, card.rarity.primaryColor)
                    TelemetryRow("Drop Odds", "${card.pullRatePercent}%")
                    TelemetryRow("Serial Stamp", card.serialCode, if (card.isMisprint) ArenaGold else TextPrimary)
                    TelemetryRow("Vault Copies", "x$duplicateCount")
                    TelemetryRow("Prestige Power", "+${card.prestigeScore}", PrismCyan)
                    TelemetryRow("Recycle Value", "+${card.recycleValueGold} Gold", ArenaGold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { showRecycleConfirm = true },
                    border = BorderStroke(1.dp, MythicCrimson),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("inspect_recycle_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Recycling,
                        contentDescription = null,
                        tint = MythicCrimson,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Recycle", color = MythicCrimson)
                }

                Button(
                    onClick = { onMintTradeSlip(card) },
                    enabled = !card.isListedForTrade,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrismCyan,
                        contentColor = VoidObsidian
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("inspect_trade_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (card.isListedForTrade) "Listed" else "Trade Slip")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    if (showRecycleConfirm && card != null) {
        AlertDialog(
            onDismissRequest = { showRecycleConfirm = false },
            containerColor = SurfaceElevated,
            title = {
                Text(
                    text = if (card.isMisprint) "Recycle 1-of-1 Misprint?" else "Recycle Card?",
                    color = if (card.isMisprint) MythicCrimson else ArenaGold
                )
            },
            text = {
                Text(
                    text = if (card.isMisprint) {
                        "Warning: ${card.skinName} (${card.serialCode}) is an insanely rare 1-of-1 Misprint! Recycling destroys this copy permanently for +${card.recycleValueGold} Arena Gold."
                    } else {
                        "Recycle ${card.heroName} — ${card.skinName} into +${card.recycleValueGold} Arena Gold?"
                    },
                    color = TextPrimary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRecycleConfirm = false
                        onRecycle(card)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MythicCrimson)
                ) {
                    Text("Recycle (+${card.recycleValueGold} Gold)", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRecycleConfirm = false }) {
                    Text("Keep Card", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun TelemetryRow(
    label: String,
    value: String,
    valueColor: Color = TextPrimary
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Text(text = value, style = MaterialTheme.typography.labelLarge, color = valueColor)
    }
}
