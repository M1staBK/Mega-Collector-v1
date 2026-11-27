package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.local.HourlyChargeStatus
import com.example.ui.components.FrostedGlassCard
import com.example.ui.theme.CollectorTheme
import kotlinx.coroutines.delay
import kotlin.random.Random

/** New hourly minigames. Every round uses one shared charge (max 3, +1 per hour) and ends with a payout. */
@Composable
fun ExtraMinigamesSection(
    statuses: Map<String, HourlyChargeStatus>,
    onAward: (gameId: String, title: String, coins: Int, diamonds: Int, label: String) -> Unit,
    onButtonClickSfx: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ComboCrackerGame(
            status = statuses["combo"] ?: HourlyChargeStatus(3, 0L),
            onAward = onAward,
            onButtonClickSfx = onButtonClickSfx
        )
        SpellSequenceGame(
            status = statuses["sequence"] ?: HourlyChargeStatus(3, 0L),
            onAward = onAward,
            onButtonClickSfx = onButtonClickSfx
        )
        MinionRushGame(
            status = statuses["rush"] ?: HourlyChargeStatus(3, 0L),
            onAward = onAward,
            onButtonClickSfx = onButtonClickSfx
        )
    }
}

@Composable
private fun GameCardShell(
    title: String,
    subtitle: String,
    status: HourlyChargeStatus,
    testTag: String,
    content: @Composable ColumnScope.() -> Unit
) {
    val palette = CollectorTheme.colors
    FrostedGlassCard(
        cornerRadius = 22.dp,
        borderColor = palette.glassHighlight,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = palette.ink
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.muted
                    )
                }
                Surface(
                    color = palette.frostSurface,
                    shape = RoundedCornerShape(999.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, palette.glassHighlight)
                ) {
                    val minutes = (status.msUntilNextCharge / 60_000L).toInt() + 1
                    Text(
                        text = if (status.charges < 3 && status.msUntilNextCharge > 0L) {
                            "${status.charges}/3  •  +1 in ${minutes}m"
                        } else {
                            "${status.charges}/3 charges"
                        },
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (status.charges > 0) palette.accentPrimary else palette.muted,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
            content()
        }
    }
}

@Composable
private fun PrimaryGameButton(text: String, enabled: Boolean = true, tag: String, onClick: () -> Unit) {
    val palette = CollectorTheme.colors
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(containerColor = palette.buttonPrimary, contentColor = Color.White),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag(tag)
    ) {
        Text(text = text, style = MaterialTheme.typography.titleSmall)
    }
}

// ================================================================= Combo Cracker

private data class ComboGuess(val digits: List<Int>, val rightSpot: Int, val wrongSpot: Int)

@Composable
private fun ComboCrackerGame(
    status: HourlyChargeStatus,
    onAward: (String, String, Int, Int, String) -> Unit,
    onButtonClickSfx: () -> Unit
) {
    val palette = CollectorTheme.colors
    var phase by remember { mutableStateOf(0) } // 0 idle, 1 playing, 2 finished
    var secret by remember { mutableStateOf(listOf<Int>()) }
    var current by remember { mutableStateOf(listOf<Int>()) }
    var guesses by remember { mutableStateOf(listOf<ComboGuess>()) }
    var message by remember { mutableStateOf("Crack the 3-digit code (digits 1-6, no repeats) in 5 tries.") }
    val maxTries = 5

    GameCardShell(
        title = "Combo Cracker",
        subtitle = "Find the secret code. Fewer tries pay more.",
        status = status,
        testTag = "minigame_combo"
    ) {
        Text(text = message, style = MaterialTheme.typography.bodySmall, color = palette.ink)

        if (phase == 1) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                for (slot in 0 until 3) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(palette.frostSurface)
                            .border(1.2.dp, palette.accentPrimary.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                    ) {
                        Text(
                            text = current.getOrNull(slot)?.toString() ?: "·",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                            color = palette.ink
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (digit in 1..6) {
                    val used = digit in current
                    Surface(
                        color = if (used) palette.frostSurface else palette.buttonPrimary,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable(enabled = !used && current.size < 3) {
                                onButtonClickSfx()
                                current = current + digit
                            }
                            .testTag("combo_digit_$digit")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "$digit",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (used) palette.muted else Color.White
                            )
                        }
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        onButtonClickSfx()
                        current = emptyList()
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("Clear") }
                Button(
                    onClick = {
                        if (current.size == 3) {
                            val right = current.indices.count { current[it] == secret[it] }
                            val wrong = current.count { it in secret } - right
                            val updated = guesses + ComboGuess(current, right, wrong)
                            guesses = updated
                            current = emptyList()
                            if (right == 3) {
                                val tries = updated.size
                                val (coins, diamonds) = when {
                                    tries <= 2 -> 60 to 3
                                    tries <= 4 -> 40 to 2
                                    else -> 25 to 1
                                }
                                phase = 2
                                message = "Cracked in $tries! +$coins coins, +$diamonds diamonds."
                                onAward("combo", "Combo Cracker", coins, diamonds, "Cracked the code in $tries tries")
                            } else if (updated.size >= maxTries) {
                                phase = 2
                                message = "Out of tries! The code was ${secret.joinToString(" ")}. +10 coins."
                                onAward("combo", "Combo Cracker", 10, 0, "Code not cracked")
                            }
                        }
                    },
                    enabled = current.size == 3,
                    colors = ButtonDefaults.buttonColors(containerColor = palette.buttonPrimary, contentColor = Color.White),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("combo_guess_button")
                ) { Text("Guess") }
            }
        }

        if (guesses.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                guesses.forEachIndexed { index, g ->
                    Text(
                        text = "${index + 1}.  ${g.digits.joinToString(" ")}   →   right spot ${g.rightSpot}  •  wrong spot ${g.wrongSpot}",
                        style = MaterialTheme.typography.labelMedium,
                        color = palette.muted
                    )
                }
            }
        }

        if (phase != 1) {
            PrimaryGameButton(
                text = if (phase == 2) "Play again" else "Start round",
                enabled = status.charges > 0,
                tag = "combo_start_button"
            ) {
                onButtonClickSfx()
                secret = (1..6).shuffled().take(3)
                current = emptyList()
                guesses = emptyList()
                phase = 1
                message = "Guess a code. 'Right spot' = correct digit in the correct place."
            }
            if (status.charges <= 0) {
                Text(
                    text = "Out of charges: one comes back every hour.",
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.muted
                )
            }
        }
    }
}

// ================================================================= Spell Sequence

@Composable
private fun SpellSequenceGame(
    status: HourlyChargeStatus,
    onAward: (String, String, Int, Int, String) -> Unit,
    onButtonClickSfx: () -> Unit
) {
    val palette = CollectorTheme.colors
    val padColors = listOf(Color(0xFF3B82F6), Color(0xFFEF4444), Color(0xFF22C55E), Color(0xFFFACC15))
    var phase by remember { mutableStateOf(0) } // 0 idle, 1 showing, 2 input, 3 over
    var sequence by remember { mutableStateOf(listOf<Int>()) }
    var litPad by remember { mutableStateOf(-1) }
    var inputIndex by remember { mutableStateOf(0) }
    var showToken by remember { mutableStateOf(0) }
    var message by remember { mutableStateOf("Watch the pattern, then repeat it. It grows by one each round.") }

    LaunchedEffect(showToken) {
        if (phase == 1) {
            delay(500)
            for (pad in sequence) {
                litPad = pad
                delay(420)
                litPad = -1
                delay(180)
            }
            inputIndex = 0
            phase = 2
            message = "Your turn!"
        }
    }

    GameCardShell(
        title = "Spell Sequence",
        subtitle = "Repeat the glowing pattern. Longer chains pay more.",
        status = status,
        testTag = "minigame_sequence"
    ) {
        Text(text = message, style = MaterialTheme.typography.bodySmall, color = palette.ink)

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (row in 0 until 2) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (col in 0 until 2) {
                        val pad = row * 2 + col
                        Box(
                            modifier = Modifier
                                .size(78.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(padColors[pad].copy(alpha = if (litPad == pad) 1f else 0.30f))
                                .clickable(enabled = phase == 2) {
                                    if (pad == sequence[inputIndex]) {
                                        litPad = pad
                                        onButtonClickSfx()
                                        inputIndex += 1
                                        if (inputIndex >= sequence.size) {
                                            sequence = sequence + Random.nextInt(4)
                                            phase = 1
                                            message = "Nice! Watch the longer pattern..."
                                            showToken += 1
                                        }
                                    } else {
                                        phase = 3
                                        val completed = (sequence.size - 3).coerceAtLeast(0)
                                        val (coins, diamonds) = when {
                                            completed >= 6 -> 60 to 3
                                            completed >= 4 -> 45 to 2
                                            completed >= 2 -> 30 to 1
                                            completed >= 1 -> 15 to 0
                                            else -> 5 to 0
                                        }
                                        message = "Wrong pad! You cleared $completed round(s). +$coins coins" +
                                            if (diamonds > 0) ", +$diamonds diamonds." else "."
                                        litPad = -1
                                        onAward("sequence", "Spell Sequence", coins, diamonds, "Cleared $completed rounds")
                                    }
                                }
                                .testTag("sequence_pad_$pad")
                        )
                    }
                }
            }
        }

        if (phase == 0 || phase == 3) {
            PrimaryGameButton(
                text = if (phase == 3) "Play again" else "Start round",
                enabled = status.charges > 0,
                tag = "sequence_start_button"
            ) {
                onButtonClickSfx()
                sequence = listOf(Random.nextInt(4), Random.nextInt(4), Random.nextInt(4))
                inputIndex = 0
                phase = 1
                message = "Watch closely..."
                showToken += 1
            }
            if (status.charges <= 0) {
                Text(
                    text = "Out of charges: one comes back every hour.",
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.muted
                )
            }
        }
    }
}

// ================================================================= Minion Rush

@Composable
private fun MinionRushGame(
    status: HourlyChargeStatus,
    onAward: (String, String, Int, Int, String) -> Unit,
    onButtonClickSfx: () -> Unit
) {
    val palette = CollectorTheme.colors
    var phase by remember { mutableStateOf(0) } // 0 idle, 1 running, 2 finished
    var score by remember { mutableStateOf(0) }
    var active by remember { mutableStateOf(4) }
    var remainingMs by remember { mutableStateOf(12_000) }
    var message by remember { mutableStateOf("Tap the glowing minion as fast as you can for 12 seconds.") }

    LaunchedEffect(phase) {
        if (phase == 1) {
            remainingMs = 12_000
            while (remainingMs > 0) {
                delay(100)
                remainingMs -= 100
            }
            val finalScore = score
            val (coins, diamonds) = when {
                finalScore >= 24 -> 60 to 3
                finalScore >= 18 -> 40 to 2
                finalScore >= 12 -> 25 to 1
                else -> 10 to 0
            }
            phase = 2
            message = "Time! You tapped $finalScore minions. +$coins coins" +
                if (diamonds > 0) ", +$diamonds diamonds." else "."
            onAward("rush", "Minion Rush", coins, diamonds, "Tapped $finalScore minions")
        }
    }

    GameCardShell(
        title = "Minion Rush",
        subtitle = "Fast taps. A wrong tap costs you a point.",
        status = status,
        testTag = "minigame_rush"
    ) {
        Text(text = message, style = MaterialTheme.typography.bodySmall, color = palette.ink)

        if (phase == 1) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Score: $score",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = palette.ink
                )
                Text(
                    text = "%.1fs".format(remainingMs / 1000f),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = palette.accentPrimary
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (row in 0 until 3) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (col in 0 until 3) {
                        val tile = row * 3 + col
                        val isActive = phase == 1 && tile == active
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(66.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isActive) palette.accentPrimary else palette.frostSurface)
                                .border(
                                    1.dp,
                                    if (isActive) Color.White.copy(alpha = 0.8f) else palette.glassHighlight,
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable(enabled = phase == 1) {
                                    if (tile == active) {
                                        score += 1
                                        var next = Random.nextInt(9)
                                        while (next == active) next = Random.nextInt(9)
                                        active = next
                                    } else {
                                        score = (score - 1).coerceAtLeast(0)
                                    }
                                }
                                .testTag("rush_tile_$tile")
                        ) {
                            if (isActive) {
                                Text(
                                    text = "★",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        if (phase != 1) {
            Spacer(modifier = Modifier.height(2.dp))
            PrimaryGameButton(
                text = if (phase == 2) "Play again" else "Start round",
                enabled = status.charges > 0,
                tag = "rush_start_button"
            ) {
                onButtonClickSfx()
                score = 0
                active = Random.nextInt(9)
                message = "Go go go!"
                phase = 1
            }
            if (status.charges <= 0) {
                Text(
                    text = "Out of charges: one comes back every hour.",
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.muted
                )
            }
        }
    }
}
