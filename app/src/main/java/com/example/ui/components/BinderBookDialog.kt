package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.CardBinderDesign
import com.example.model.OwnedCard
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

private const val POCKETS_PER_PAGE = 6

/**
 * A ring binder you can open and page through: tap the cover (or swipe) to open it, then drag the
 * page across the spine to turn it, exactly like a real two-page binder spread.
 */
@Composable
fun BinderBookDialog(
    binder: CardBinderDesign,
    cards: List<OwnedCard>,
    onInspectCard: (Long) -> Unit,
    onClose: () -> Unit
) {
    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = false)
    ) {
        BinderBookContent(
            binder = binder,
            cards = cards,
            onInspectCard = onInspectCard,
            onClose = onClose
        )
    }
}

@Composable
private fun BinderBookContent(
    binder: CardBinderDesign,
    cards: List<OwnedCard>,
    onInspectCard: (Long) -> Unit,
    onClose: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    val pages = remember(cards) { cards.chunked(POCKETS_PER_PAGE) }
    // Sheet 0 is the cover. Every following sheet carries two card pages (front and back).
    val sheetCount = 1 + (pages.size + 1) / 2

    var spread by remember { mutableStateOf(0) }
    val flip = remember { Animatable(0f) }

    fun goNext() {
        if (spread >= sheetCount || flip.value != 0f) return
        scope.launch {
            flip.animateTo(1f, tween(480, easing = FastOutSlowInEasing))
            spread += 1
            flip.snapTo(0f)
        }
    }

    fun goPrevious() {
        if (spread <= 0 || flip.value != 0f) return
        scope.launch {
            flip.animateTo(-1f, tween(480, easing = FastOutSlowInEasing))
            spread -= 1
            flip.snapTo(0f)
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF0E1426), Color(0xFF05070F))))
    ) {
        val bookW: Dp = minOf(maxWidth - 24.dp, 560.dp)
        val pageW: Dp = bookW / 2
        val pageH: Dp = pageW * 2.05f
        val pageWpx = with(density) { pageW.toPx() }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose, modifier = Modifier.testTag("binder_book_close")) {
                    Icon(Icons.Default.Close, contentDescription = "Close binder", tint = Color.White)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = binder.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = "${cards.size} cards  •  ${pages.size} pages",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // ------------------------------------------------------------- the book
            val position = spread + flip.value
            val shiftPx = -pageWpx / 2f * (1f - position).coerceIn(0f, 1f)

            Box(
                modifier = Modifier
                    .size(bookW, pageH)
                    .graphicsLayer { translationX = shiftPx }
                    .pointerInput(spread, sheetCount, pageWpx) {
                        var total = 0f
                        detectHorizontalDragGestures(
                            onDragStart = { total = 0f },
                            onHorizontalDrag = { change, dragAmount ->
                                change.consume()
                                total += dragAmount
                                val f = when {
                                    total < 0f && spread < sheetCount -> (-total / pageWpx).coerceIn(0f, 1f)
                                    total > 0f && spread > 0 -> -(total / pageWpx).coerceIn(0f, 1f)
                                    else -> 0f
                                }
                                scope.launch { flip.snapTo(f) }
                            },
                            onDragEnd = {
                                val f = flip.value
                                val target = when {
                                    f > 0.3f -> 1f
                                    f < -0.3f -> -1f
                                    else -> 0f
                                }
                                scope.launch {
                                    flip.animateTo(target, tween(260, easing = FastOutSlowInEasing))
                                    if (target != 0f) {
                                        spread += target.toInt()
                                        flip.snapTo(0f)
                                    }
                                }
                            },
                            onDragCancel = {
                                scope.launch { flip.animateTo(0f, tween(200)) }
                            }
                        )
                    }
                    .pointerInput(spread) {
                        detectTapGestures(onTap = { if (spread == 0) goNext() })
                    }
                    .testTag("binder_book")
            ) {
                val f = flip.value
                val leftIndex: Int
                val rightIndex: Int
                val turningSheet: Int
                val angle: Float
                if (f >= 0f) {
                    leftIndex = spread
                    rightIndex = spread + 1
                    turningSheet = spread
                    angle = f * 180f
                } else {
                    leftIndex = spread - 1
                    rightIndex = spread
                    turningSheet = spread - 1
                    angle = 180f + f * 180f
                }

                // static left page
                Box(modifier = Modifier.size(pageW, pageH)) {
                    BookLeftContent(
                        spreadIndex = leftIndex,
                        binder = binder,
                        pages = pages,
                        totalCards = cards.size,
                        onCardClick = { card ->
                            onClose()
                            onInspectCard(card.instanceId)
                        }
                    )
                }
                // static right page (only needed while a sheet is moving, or when nothing sits on top of it)
                if (f != 0f || turningSheet !in 0 until sheetCount) {
                    Box(
                        modifier = Modifier
                            .offset(x = pageW)
                            .size(pageW, pageH)
                    ) {
                        BookRightContent(
                            spreadIndex = rightIndex,
                            sheetCount = sheetCount,
                            binder = binder,
                            pages = pages,
                            onCardClick = { card ->
                                onClose()
                                onInspectCard(card.instanceId)
                            }
                        )
                    }
                }

                // the sheet that is being turned
                if (turningSheet in 0 until sheetCount && angle < 179.5f) {
                    val rad = Math.toRadians(angle.toDouble()).toFloat()
                    val cosA = cos(rad)
                    val sinA = sin(rad)
                    Box(
                        modifier = Modifier
                            .offset(x = pageW)
                            .size(pageW, pageH)
                            .graphicsLayer {
                                transformOrigin = TransformOrigin(0f, 0.5f)
                                scaleX = cosA
                                scaleY = 1f + 0.05f * sinA
                            }
                    ) {
                        if (angle <= 90f) {
                            BookRightContent(
                                spreadIndex = turningSheet,
                                sheetCount = sheetCount,
                                binder = binder,
                                pages = pages,
                                onCardClick = { card ->
                                    if (f == 0f) {
                                        onClose()
                                        onInspectCard(card.instanceId)
                                    }
                                }
                            )
                        } else {
                            Box(modifier = Modifier.fillMaxSize().graphicsLayer { scaleX = -1f }) {
                                BookLeftContent(
                                    spreadIndex = turningSheet + 1,
                                    binder = binder,
                                    pages = pages,
                                    totalCards = cards.size,
                                    onCardClick = {}
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.30f * sinA))
                        )
                    }
                }

                // metal rings along the spine stay put while the pages turn
                if (position > 0.05f || spread > 0) {
                    BinderRings(
                        pageH = pageH,
                        ringColor = binder.ringColor,
                        modifier = Modifier
                            .offset(x = pageW - 11.dp)
                            .size(22.dp, pageH)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // ------------------------------------------------------------- controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { goPrevious() },
                    enabled = spread > 0,
                    modifier = Modifier.testTag("binder_prev")
                ) {
                    Icon(
                        Icons.Default.ChevronLeft,
                        contentDescription = "Previous page",
                        tint = if (spread > 0) Color.White else Color(0x55FFFFFF),
                        modifier = Modifier.size(34.dp)
                    )
                }
                Text(
                    text = when {
                        spread == 0 -> "Tap the cover or swipe to open"
                        spread >= sheetCount -> "The end"
                        else -> {
                            val left = (spread - 1) * 2 + 1
                            "Pages $left – ${minOf(left + 1, maxOf(pages.size, 1))} of ${maxOf(pages.size, 1)}"
                        }
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFFCBD5E1),
                    textAlign = TextAlign.Center
                )
                IconButton(
                    onClick = { goNext() },
                    enabled = spread < sheetCount,
                    modifier = Modifier.testTag("binder_next")
                ) {
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = "Next page",
                        tint = if (spread < sheetCount) Color.White else Color(0x55FFFFFF),
                        modifier = Modifier.size(34.dp)
                    )
                }
            }
        }
    }
}

// ------------------------------------------------------------------ faces

/** Left half of spread [spreadIndex]: the back of the previous sheet (or nothing next to the closed cover). */
@Composable
private fun BookLeftContent(
    spreadIndex: Int,
    binder: CardBinderDesign,
    pages: List<List<OwnedCard>>,
    totalCards: Int,
    onCardClick: (OwnedCard) -> Unit
) {
    when {
        spreadIndex <= 0 -> Unit
        spreadIndex == 1 -> InsideCoverFace(binder = binder, totalCards = totalCards)
        else -> {
            val pageIndex = (spreadIndex - 2) * 2 + 1
            PaperPageFace(
                cards = pages.getOrNull(pageIndex).orEmpty(),
                pageNumber = pageIndex + 1,
                spineOnRight = true,
                binder = binder,
                onCardClick = onCardClick
            )
        }
    }
}

/** Right half of spread [spreadIndex]: the front of that sheet (the cover, a card page, or the back cover). */
@Composable
private fun BookRightContent(
    spreadIndex: Int,
    sheetCount: Int,
    binder: CardBinderDesign,
    pages: List<List<OwnedCard>>,
    onCardClick: (OwnedCard) -> Unit
) {
    when {
        spreadIndex <= 0 -> CoverFace(binder = binder)
        spreadIndex >= sheetCount -> BackCoverFace(binder = binder)
        else -> {
            val pageIndex = (spreadIndex - 1) * 2
            PaperPageFace(
                cards = pages.getOrNull(pageIndex).orEmpty(),
                pageNumber = pageIndex + 1,
                spineOnRight = false,
                binder = binder,
                onCardClick = onCardClick
            )
        }
    }
}

@Composable
private fun CoverFace(binder: CardBinderDesign) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp, topEnd = 18.dp, bottomEnd = 18.dp))
            .background(Brush.linearGradient(listOf(binder.coverPrimary, binder.coverSecondary)))
            .border(
                BorderStroke(2.dp, binder.trimGold.copy(alpha = 0.85f)),
                RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp, topEnd = 18.dp, bottomEnd = 18.dp)
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(18.dp)
                .background(binder.spineColor)
        )
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(start = 26.dp, end = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = binder.trimGold,
                modifier = Modifier.size(34.dp)
            )
            Text(
                text = binder.name.uppercase(),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                ),
                color = binder.trimGold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "MOBA COMPANION",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                color = Color.White.copy(alpha = 0.75f)
            )
        }
    }
}

@Composable
private fun InsideCoverFace(binder: CardBinderDesign, totalCards: Int) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(topStart = 18.dp, bottomStart = 18.dp, topEnd = 2.dp, bottomEnd = 2.dp))
            .background(Brush.linearGradient(listOf(binder.coverSecondary, binder.coverPrimary)))
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "THIS BINDER HOLDS",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                color = binder.trimGold.copy(alpha = 0.85f)
            )
            Text(
                text = "$totalCards",
                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Black),
                color = binder.trimGold
            )
            Text(
                text = "collector cards",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun BackCoverFace(binder: CardBinderDesign) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(topStart = 2.dp, bottomStart = 2.dp, topEnd = 18.dp, bottomEnd = 18.dp))
            .background(Brush.linearGradient(listOf(binder.coverPrimary, binder.coverSecondary)))
    )
}

@Composable
private fun PaperPageFace(
    cards: List<OwnedCard>,
    pageNumber: Int,
    spineOnRight: Boolean,
    binder: CardBinderDesign,
    onCardClick: (OwnedCard) -> Unit
) {
    val paper = Color(0xFFF6F0E2)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.horizontalGradient(
                    if (spineOnRight) listOf(Color(0xFFEFE7D3), paper, Color(0xFFD9CFB5))
                    else listOf(Color(0xFFD9CFB5), paper, Color(0xFFEFE7D3))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = if (spineOnRight) 10.dp else 16.dp,
                    end = if (spineOnRight) 16.dp else 10.dp,
                    top = 12.dp,
                    bottom = 22.dp
                ),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            for (row in 0 until 3) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    for (col in 0 until 2) {
                        val card = cards.getOrNull(row * 2 + col)
                        Pocket(
                            card = card,
                            accent = binder.trimGold,
                            modifier = Modifier.weight(1f),
                            onClick = { if (card != null) onCardClick(card) }
                        )
                    }
                }
            }
        }
        Text(
            text = "$pageNumber",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF8A7F66),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 5.dp)
        )
    }
}

@Composable
private fun Pocket(
    card: OwnedCard?,
    accent: Color,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(7.dp))
            .background(Color(0x1A000000))
            .border(1.dp, Color(0x33000000), RoundedCornerShape(7.dp))
            .clickable(enabled = card != null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (card != null) {
            HolographicCardView(
                card = card,
                compact = true,
                modifier = Modifier.padding(2.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.18f))
            )
        }
        // plastic sleeve gloss
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        listOf(Color.White.copy(alpha = 0.16f), Color.Transparent, Color.White.copy(alpha = 0.06f))
                    )
                )
        )
    }
}

@Composable
private fun BinderRings(
    pageH: Dp,
    ringColor: Color,
    modifier: Modifier
) {
    Box(modifier = modifier) {
        listOf(0.16f, 0.50f, 0.84f).forEach { fraction ->
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = pageH * fraction - 7.dp)
                    .size(22.dp, 14.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(ringColor, ringColor.copy(alpha = 0.55f), Color(0xFF64748B))
                        )
                    )
                    .border(1.dp, Color(0x66000000), RoundedCornerShape(7.dp))
            )
        }
    }
}
