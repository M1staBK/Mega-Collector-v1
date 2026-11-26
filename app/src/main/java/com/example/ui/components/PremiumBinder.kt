package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CardBinderDesign
import com.example.model.OwnedCard
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

/** A tab on the edge of the binder that filters it to one kind of card. */
data class BinderBookmark(
    val key: String,
    val label: String,
    val color: Color,
    val count: Int
)

private const val CARDS_PER_PAGE = 2

/**
 * The collection, shown as a real ring binder on a soft dark desk. The cover opens to a spread of four
 * large cards (two per page), pages turn by dragging across the spine, and coloured bookmarks on the
 * edge jump straight to one kind of card.
 */
@Composable
fun PremiumBinder(
    binder: CardBinderDesign,
    cards: List<OwnedCard>,
    totalCollected: Int,
    bookmarks: List<BinderBookmark>,
    selectedBookmark: String,
    onSelectBookmark: (String) -> Unit,
    onInspectCard: (Long) -> Unit,
    onButtonClickSfx: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    val pages = remember(cards) { cards.chunked(CARDS_PER_PAGE) }
    // Sheet 0 is the cover; every other sheet carries two card pages (front and back).
    val sheetCount = 1 + (pages.size + 1) / 2

    var spread by remember { mutableStateOf(0) }
    val flip = remember { Animatable(0f) }

    // A new bookmark starts at its first page (or stays closed if the binder is still closed).
    LaunchedEffect(selectedBookmark) {
        flip.snapTo(0f)
        if (spread > 0) spread = 1
    }

    fun goNext() {
        if (spread >= sheetCount || flip.value != 0f) return
        onButtonClickSfx()
        scope.launch {
            flip.animateTo(1f, tween(520, easing = FastOutSlowInEasing))
            spread += 1
            flip.snapTo(0f)
        }
    }

    fun goPrevious() {
        if (spread <= 0 || flip.value != 0f) return
        onButtonClickSfx()
        scope.launch {
            flip.animateTo(-1f, tween(520, easing = FastOutSlowInEasing))
            spread -= 1
            flip.snapTo(0f)
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .drawDesk(binder)
    ) {
        val tabW = 30.dp
        val bookW: Dp = minOf(maxWidth - tabW * 2 - 8.dp, 520.dp)
        val pageW: Dp = bookW / 2
        val pageH: Dp = minOf(pageW * 3f, maxHeight - 150.dp).coerceAtLeast(280.dp)
        val stageW: Dp = bookW + tabW * 2
        val pageWpx = with(density) { pageW.toPx() }

        val cardH: Dp = (pageH - 34.dp - 14.dp) / 2
        val cardW: Dp = minOf(pageW - 26.dp, cardH * 0.665f)

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ---------------------------------------------------- header line
            val activeMark = bookmarks.firstOrNull { it.key == selectedBookmark }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = binder.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = "${activeMark?.label ?: "ALL"}  •  ${cards.size} cards  •  $totalCollected collected",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF9AA7BD)
                    )
                }
                if (spread > 0) {
                    Text(
                        text = "Close binder",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = binder.trimGold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .border(1.dp, binder.trimGold.copy(alpha = 0.5f), RoundedCornerShape(999.dp))
                            .clickable {
                                onButtonClickSfx()
                                scope.launch {
                                    flip.snapTo(0f)
                                    spread = 0
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("binder_close_cover")
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // ----------------------------------------------------- the stage
            val position = spread + flip.value
            val shiftPx = -pageWpx / 2f * (1f - position).coerceIn(0f, 1f)

            Box(
                modifier = Modifier
                    .size(stageW, pageH)
                    .graphicsLayer { translationX = shiftPx }
            ) {
                // soft shadow under and around the book (grows as the cover opens)
                val openAmount = position.coerceIn(0f, 1f)
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val left = tabW.toPx() + pageW.toPx() * (1f - openAmount)
                    val right = tabW.toPx() + bookW.toPx()
                    val top = 10.dp.toPx()
                    for (i in 1..16) {
                        val grow = i * 2.6f.dp.toPx()
                        drawRoundRect(
                            color = Color.Black.copy(alpha = 0.030f),
                            topLeft = Offset(left - grow * 0.8f, top - grow * 0.4f + 16.dp.toPx()),
                            size = Size(
                                (right - left) + grow * 1.6f,
                                pageH.toPx() - top + grow * 0.8f
                            ),
                            cornerRadius = CornerRadius(14.dp.toPx() + grow)
                        )
                    }
                    // contact shadow right under the book
                    drawRoundRect(
                        color = Color.Black.copy(alpha = 0.28f),
                        topLeft = Offset(left + 6.dp.toPx(), top + 14.dp.toPx()),
                        size = Size((right - left) - 12.dp.toPx(), pageH.toPx() - top),
                        cornerRadius = CornerRadius(12.dp.toPx())
                    )
                }

                // ---- the book itself (pages, cover, drag + tap handling)
                Box(
                    modifier = Modifier
                        .offset(x = tabW)
                        .size(bookW, pageH)
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
                                onDragCancel = { scope.launch { flip.animateTo(0f, tween(200)) } }
                            )
                        }
                        .pointerInput(spread) {
                            detectTapGestures(onTap = { if (spread == 0) goNext() })
                        }
                        .testTag("premium_binder")
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

                    val inspect: (OwnedCard) -> Unit = { card ->
                        onButtonClickSfx()
                        onInspectCard(card.instanceId)
                    }

                    Box(modifier = Modifier.size(pageW, pageH)) {
                        LeftHalf(
                            spreadIndex = leftIndex,
                            binder = binder,
                            pages = pages,
                            totalCollected = totalCollected,
                            cardW = cardW,
                            cardH = cardH,
                            onCardClick = inspect
                        )
                    }
                    if (f != 0f || turningSheet !in 0 until sheetCount) {
                        Box(
                            modifier = Modifier
                                .offset(x = pageW)
                                .size(pageW, pageH)
                        ) {
                            RightHalf(
                                spreadIndex = rightIndex,
                                sheetCount = sheetCount,
                                binder = binder,
                                pages = pages,
                                totalCollected = totalCollected,
                                cardW = cardW,
                                cardH = cardH,
                                onCardClick = inspect
                            )
                        }
                    }

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
                                RightHalf(
                                    spreadIndex = turningSheet,
                                    sheetCount = sheetCount,
                                    binder = binder,
                                    pages = pages,
                                    totalCollected = totalCollected,
                                    cardW = cardW,
                                    cardH = cardH,
                                    onCardClick = { card -> if (f == 0f) inspect(card) }
                                )
                            } else {
                                Box(modifier = Modifier.fillMaxSize().graphicsLayer { scaleX = -1f }) {
                                    LeftHalf(
                                        spreadIndex = turningSheet + 1,
                                        binder = binder,
                                        pages = pages,
                                        totalCollected = totalCollected,
                                        cardW = cardW,
                                        cardH = cardH,
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

                    if (position > 0.05f || spread > 0) {
                        SpineRings(
                            pageH = pageH,
                            ringColor = binder.ringColor,
                            modifier = Modifier
                                .offset(x = pageW - 12.dp)
                                .size(24.dp, pageH)
                        )
                    }
                }

                // ---- bookmarks on the right edge of the binder
                Column(
                    modifier = Modifier
                        .offset(x = tabW + bookW - 3.dp)
                        .size(tabW, pageH)
                        .padding(vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    bookmarks.forEach { mark ->
                        BookmarkTab(
                            mark = mark,
                            selected = mark.key == selectedBookmark,
                            enabled = mark.count > 0 || mark.key == "ALL",
                            modifier = Modifier.weight(1f),
                            onClick = {
                                onButtonClickSfx()
                                onSelectBookmark(mark.key)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // ------------------------------------------------------ controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 6.dp),
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
                        tint = if (spread > 0) Color.White else Color(0x44FFFFFF),
                        modifier = Modifier.size(32.dp)
                    )
                }
                Text(
                    text = when {
                        spread == 0 -> if (totalCollected == 0) "Open a pack to fill your binder" else "Tap the cover to open"
                        spread >= sheetCount -> "End of binder"
                        else -> {
                            val first = (spread - 1) * 2 + 1
                            val last = minOf(first + 1, maxOf(pages.size, 1))
                            "Pages $first–$last of ${maxOf(pages.size, 1)}"
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
                        tint = if (spread < sheetCount) Color.White else Color(0x44FFFFFF),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

/** Dark desk with a soft glow in the binder's own colour. */
private fun Modifier.drawDesk(binder: CardBinderDesign): Modifier =
    this
        .background(Brush.verticalGradient(listOf(Color(0xFF0B0F1C), Color(0xFF070A14), Color(0xFF05070E))))
        .background(Brush.radialGradient(listOf(binder.coverPrimary.copy(alpha = 0.30f), Color.Transparent)))

// ------------------------------------------------------------------ halves

@Composable
private fun LeftHalf(
    spreadIndex: Int,
    binder: CardBinderDesign,
    pages: List<List<OwnedCard>>,
    totalCollected: Int,
    cardW: Dp,
    cardH: Dp,
    onCardClick: (OwnedCard) -> Unit
) {
    when {
        spreadIndex <= 0 -> Unit
        spreadIndex == 1 -> InsideCover(binder = binder, totalCollected = totalCollected)
        else -> {
            val pageIndex = (spreadIndex - 2) * 2 + 1
            PaperPage(
                cards = pages.getOrNull(pageIndex).orEmpty(),
                pageNumber = pageIndex + 1,
                spineOnRight = true,
                binder = binder,
                cardW = cardW,
                cardH = cardH,
                onCardClick = onCardClick
            )
        }
    }
}

@Composable
private fun RightHalf(
    spreadIndex: Int,
    sheetCount: Int,
    binder: CardBinderDesign,
    pages: List<List<OwnedCard>>,
    totalCollected: Int,
    cardW: Dp,
    cardH: Dp,
    onCardClick: (OwnedCard) -> Unit
) {
    when {
        spreadIndex <= 0 -> LeatherCover(binder = binder, totalCollected = totalCollected)
        spreadIndex >= sheetCount -> BackCover(binder = binder)
        else -> {
            val pageIndex = (spreadIndex - 1) * 2
            PaperPage(
                cards = pages.getOrNull(pageIndex).orEmpty(),
                pageNumber = pageIndex + 1,
                spineOnRight = false,
                binder = binder,
                cardW = cardW,
                cardH = cardH,
                onCardClick = onCardClick
            )
        }
    }
}

// ------------------------------------------------------------------ faces

@Composable
private fun LeatherCover(binder: CardBinderDesign, totalCollected: Int) {
    val shape = RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp, topEnd = 20.dp, bottomEnd = 20.dp)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(binder.coverPrimary, binder.coverSecondary, binder.coverPrimary.copy(alpha = 0.85f))
                )
            )
    ) {
        // leather sheen and a soft highlight on the top edge
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        listOf(Color.White.copy(alpha = 0.14f), Color.Transparent, Color.Black.copy(alpha = 0.22f))
                    )
                )
        )
        // spine strip
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(22.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(binder.spineColor, binder.spineColor.copy(alpha = 0.7f))
                    )
                )
        )
        // stitched border and embossed frame
        Canvas(modifier = Modifier.fillMaxSize()) {
            val inset = 14.dp.toPx()
            drawRoundRect(
                color = binder.trimGold.copy(alpha = 0.55f),
                topLeft = Offset(inset + 14.dp.toPx(), inset),
                size = Size(size.width - inset * 2 - 14.dp.toPx(), size.height - inset * 2),
                cornerRadius = CornerRadius(14.dp.toPx()),
                style = Stroke(
                    width = 1.4.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(7.dp.toPx(), 5.dp.toPx()))
                )
            )
            val inner = 26.dp.toPx()
            drawRoundRect(
                color = binder.trimGold.copy(alpha = 0.9f),
                topLeft = Offset(inner + 14.dp.toPx(), inner),
                size = Size(size.width - inner * 2 - 14.dp.toPx(), size.height - inner * 2),
                cornerRadius = CornerRadius(10.dp.toPx()),
                style = Stroke(width = 1.6.dp.toPx())
            )
        }
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(start = 30.dp, end = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = binder.trimGold,
                modifier = Modifier.size(30.dp)
            )
            Text(
                text = binder.name.uppercase(),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.4.sp
                ),
                color = binder.trimGold,
                textAlign = TextAlign.Center
            )
            Box(
                modifier = Modifier
                    .width(44.dp)
                    .height(1.dp)
                    .background(binder.trimGold.copy(alpha = 0.7f))
            )
            Text(
                text = "$totalCollected CARDS",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                color = Color.White.copy(alpha = 0.78f)
            )
            Text(
                text = "MOBA COMPANION",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                color = Color.White.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun InsideCover(binder: CardBinderDesign, totalCollected: Int) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp, topEnd = 3.dp, bottomEnd = 3.dp))
            .background(Brush.linearGradient(listOf(binder.coverSecondary, binder.coverPrimary.copy(alpha = 0.9f))))
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
                text = "$totalCollected",
                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Black),
                color = binder.trimGold
            )
            Text(
                text = if (totalCollected == 0) "Open packs to fill it" else "collector cards",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun BackCover(binder: CardBinderDesign) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(topStart = 3.dp, bottomStart = 3.dp, topEnd = 20.dp, bottomEnd = 20.dp))
            .background(Brush.linearGradient(listOf(binder.coverPrimary, binder.coverSecondary)))
    )
}

@Composable
private fun PaperPage(
    cards: List<OwnedCard>,
    pageNumber: Int,
    spineOnRight: Boolean,
    binder: CardBinderDesign,
    cardW: Dp,
    cardH: Dp,
    onCardClick: (OwnedCard) -> Unit
) {
    val paper = Color(0xFFF7F1E3)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(paper)
    ) {
        // gutter shadow next to the rings, like the fold of a real page
        Box(
            modifier = Modifier
                .align(if (spineOnRight) Alignment.CenterEnd else Alignment.CenterStart)
                .fillMaxHeight()
                .width(34.dp)
                .background(
                    Brush.horizontalGradient(
                        if (spineOnRight) listOf(Color.Transparent, Color.Black.copy(alpha = 0.20f))
                        else listOf(Color.Black.copy(alpha = 0.20f), Color.Transparent)
                    )
                )
        )
        // page-stack lines on the outer edge
        Canvas(modifier = Modifier.fillMaxSize()) {
            val x0 = if (spineOnRight) 3.dp.toPx() else size.width - 3.dp.toPx()
            for (i in 0 until 3) {
                val dx = (if (spineOnRight) 1 else -1) * i * 2.5.dp.toPx()
                drawLine(
                    color = Color(0x22000000),
                    start = Offset(x0 + dx, 0f),
                    end = Offset(x0 + dx, size.height),
                    strokeWidth = 1f
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = if (spineOnRight) 6.dp else 22.dp,
                    end = if (spineOnRight) 22.dp else 6.dp,
                    top = 14.dp,
                    bottom = 20.dp
                ),
            verticalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            for (slot in 0 until CARDS_PER_PAGE) {
                val card = cards.getOrNull(slot)
                Sleeve(
                    card = card,
                    accent = binder.trimGold,
                    cardW = cardW,
                    cardH = cardH,
                    onClick = { if (card != null) onCardClick(card) }
                )
            }
        }
        Text(
            text = "$pageNumber",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF8A7F66),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 4.dp)
        )
    }
}

@Composable
private fun Sleeve(
    card: OwnedCard?,
    accent: Color,
    cardW: Dp,
    cardH: Dp,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier = Modifier
            .size(cardW + 8.dp, cardH)
            .clip(shape)
            .background(Color(0x1F000000))
            .border(1.dp, Color(0x40000000), shape)
            .clickable(enabled = card != null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (card != null) {
            Box(
                modifier = Modifier
                    .size(cardW, cardH - 8.dp)
            ) {
                HolographicCardView(
                    card = card,
                    compact = true,
                    modifier = Modifier.fillMaxSize()
                )
            }
        } else {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = accent.copy(alpha = 0.25f),
                modifier = Modifier.size(22.dp)
            )
        }
        // plastic gloss across the sleeve
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        listOf(Color.White.copy(alpha = 0.18f), Color.Transparent, Color.White.copy(alpha = 0.07f))
                    )
                )
        )
    }
}

@Composable
private fun SpineRings(
    pageH: Dp,
    ringColor: Color,
    modifier: Modifier
) {
    Box(modifier = modifier) {
        listOf(0.17f, 0.50f, 0.83f).forEach { fraction ->
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = pageH * fraction - 8.dp)
                    .size(24.dp, 16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.White.copy(alpha = 0.9f), ringColor, Color(0xFF475569))
                        )
                    )
                    .border(1.dp, Color(0x80000000), RoundedCornerShape(8.dp))
            )
        }
    }
}

@Composable
private fun BookmarkTab(
    mark: BinderBookmark,
    selected: Boolean,
    enabled: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val pullOut = if (selected) 8.dp else 0.dp
    Box(
        modifier = modifier
            .fillMaxWidth()
            .offset(x = pullOut)
            .clip(RoundedCornerShape(topEnd = 9.dp, bottomEnd = 9.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(mark.color.copy(alpha = if (enabled) 0.95f else 0.30f), mark.color.copy(alpha = if (enabled) 0.75f else 0.22f))
                )
            )
            .border(
                BorderStroke(if (selected) 1.5.dp else 0.5.dp, Color.White.copy(alpha = if (selected) 0.9f else 0.25f)),
                RoundedCornerShape(topEnd = 9.dp, bottomEnd = 9.dp)
            )
            .clickable(enabled = enabled, onClick = onClick)
            .testTag("binder_bookmark_${mark.key}"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = mark.label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.ExtraBold),
            color = Color(0xFF0B1020).copy(alpha = if (enabled) 1f else 0.5f),
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.rotate(90f)
        )
    }
}
