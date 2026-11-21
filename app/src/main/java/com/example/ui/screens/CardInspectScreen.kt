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
import androidx.compose.foundation.shape.CircleShape
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
import com.example.ui.theme.CollectorTheme
import com.example.ui.theme.DeepCobalt
import com.example.ui.theme.IceBorder
import com.example.ui.theme.InkSlate
import com.example.ui.theme.MisprintNeon
import com.example.ui.theme.MoonlightAbyss
import com.example.ui.theme.PearlLightInk
import com.example.ui.theme.PureWhiteCard
import com.example.ui.theme.SkyAzure
import com.example.ui.theme.SlateMuted
import com.example.ui.theme.StudioCrimson
import com.example.ui.theme.StudioGoldText

@Composable
fun CardInspectScreen(
    card: OwnedCard?,
    duplicateCount: Int,
    onBack: () -> Unit,
    onRecycle: (OwnedCard) -> Unit,
    onMintTradeSlip: (OwnedCard) -> Unit,
    onButtonClickSfx: () -> Unit
) {
    val palette = CollectorTheme.colors
    val tiltState = rememberCardTiltState()
    var showRecycleConfirm by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.canvas)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        (card?.rarity?.primaryColor ?: SkyAzure).copy(alpha = if (palette.isDark) 0.24f else 0.20f),
                        palette.gloomHalo.copy(alpha = if (palette.isDark) 0.12f else 0.25f),
                        palette.abyss.copy(alpha = 0.84f),
                        palette.canvas
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
                Text("Card no longer in Vault", style = MaterialTheme.typography.titleLarge, color = InkSlate)
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
                Surface(
                    color = PureWhiteCard,
                    shape = CircleShape,
                    border = BorderStroke(1.dp, IceBorder),
                    shadowElevation = 2.dp
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("inspect_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = InkSlate
                        )
                    }
                }

                Text(
                    text = "3D FOIL INSPECTOR",
                    style = MaterialTheme.typography.titleLarge,
                    color = palette.accentPrimary
                )

                Surface(
                    color = PureWhiteCard,
                    shape = CircleShape,
                    border = BorderStroke(1.dp, IceBorder),
                    shadowElevation = 2.dp
                ) {
                    IconButton(
                        onClick = {
                            onButtonClickSfx()
                            tiltState.calibrateCenter()
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("inspect_recenter_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CenterFocusStrong,
                            contentDescription = "Recenter gyroscope angle",
                            tint = palette.accentPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

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

            Spacer(modifier = Modifier.height(14.dp))

            // Gyroscope Telemetry Bar
            Surface(
                color = PureWhiteCard,
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 2.dp,
                border = BorderStroke(1.dp, IceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
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
                            tint = palette.accentPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (tiltState.isGyroAvailable) "Gyroscope Foil Active"
                                else "Touch-Drag Foil Active",
                                style = MaterialTheme.typography.labelLarge,
                                color = InkSlate
                            )
                            Text(
                                text = stringResource(R.string.gyro_hint),
                                style = MaterialTheme.typography.bodyMedium,
                                color = SlateMuted
                            )
                        }
                    }

                    Switch(
                        checked = tiltState.gyroEnabled,
                        onCheckedChange = {
                            onButtonClickSfx()
                            tiltState.gyroEnabled = it
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = DeepCobalt
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Misprint Anomaly Callout (if 1/1)
            if (card.isMisprint && card.misprintAnomaly != null) {
                Surface(
                    color = if (palette.isDark) MoonlightAbyss else PearlLightInk,
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
                                color = Color.White
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Card Telemetry Details
            Surface(
                color = PureWhiteCard,
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 2.dp,
                border = BorderStroke(1.dp, IceBorder),
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
                    TelemetryRow("Serial Stamp", card.serialCode, if (card.isMisprint) StudioGoldText else InkSlate)
                    TelemetryRow("Vault Copies", "x$duplicateCount")
                    TelemetryRow("Prestige Power", "+${card.prestigeScore}", palette.accentPrimary)
                    TelemetryRow("Recycle Value", "+${card.recycleValueGold} Coins", StudioGoldText)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        onButtonClickSfx()
                        showRecycleConfirm = true
                    },
                    border = BorderStroke(1.2.dp, StudioCrimson),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("inspect_recycle_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Recycling,
                        contentDescription = null,
                        tint = StudioCrimson,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Recycle", color = StudioCrimson)
                }

                Button(
                    onClick = { onMintTradeSlip(card) },
                    enabled = !card.isListedForTrade,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DeepCobalt,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp),
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
            containerColor = PureWhiteCard,
            title = {
                Text(
                    text = if (card.isMisprint) "Recycle 1-of-1 Misprint?" else "Recycle Card?",
                    color = if (card.isMisprint) StudioCrimson else InkSlate
                )
            },
            text = {
                Text(
                    text = if (card.isMisprint) {
                        "Warning: ${card.skinName} (${card.serialCode}) is an insanely rare 1-of-1 Misprint! Recycling destroys this copy permanently for +${card.recycleValueGold} Coins."
                    } else {
                        "Recycle ${card.heroName} — ${card.skinName} into +${card.recycleValueGold} Coins?"
                    },
                    color = SlateMuted
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRecycleConfirm = false
                        onRecycle(card)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StudioCrimson)
                ) {
                    Text("Recycle (+${card.recycleValueGold} Coins)", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onButtonClickSfx()
                        showRecycleConfirm = false
                    }
                ) {
                    Text("Keep Card", color = SlateMuted)
                }
            }
        )
    }
}

@Composable
private fun TelemetryRow(
    label: String,
    value: String,
    valueColor: Color = InkSlate
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = SlateMuted)
        Text(text = value, style = MaterialTheme.typography.labelLarge, color = valueColor)
    }
}
