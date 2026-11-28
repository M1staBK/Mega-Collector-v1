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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.Coil
import coil.compose.AsyncImage
import com.example.model.CardBinderDesign
import com.example.model.OwnedCard
import com.example.ui.theme.CollectorTheme
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.key
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex
import kotlin.math.roundToInt

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
    val context = LocalContext.current
    val palette = CollectorTheme.colors
    val dark = palette.isDark
    val titleColor = if (dark) Color.White else palette.ink
    val subColor = if (dark) Color(0xFF9AA7BD) else Color(0xFF6B6558)
    val controlColor = if (dark) Color.White else palette.ink
    val accent = if (dark) binder.trimGold else palette.accentPrimary

    val pages = remember(cards) { cards.chunked(CARDS_PER_PAGE) }
    // Sheet 0 is the cover; every other sheet carries two card pages (front and back).
    val sheetCount = 1 + (pages.size + 1) / 2

    var spread by remember { mutableStateOf(0) }
    val flip = remember { Animatable(0f) }

    // Load the pictures of the pages around the current spread ahead of time.
    LaunchedEffect(spread, pages) {
        val loader = Coil.imageLoader(context.applicationContext)
        val firstPage = ((spread - 2).coerceAtLeast(0)) * 2
        val lastPage = ((spread + 3) * 2 + 1).coerceAtMost(pages.size - 1)
        for (pageIndex in firstPage..lastPage) {
            pages.getOrNull(pageIndex)?.forEach { card ->
                loader.enqueue(buildMlbbCardImageRequest(context, card.imageUrl, crossfadeMs = 0, compact = true))
            }
        }
    }

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
            .drawDesk(binder, dark)
    ) {
        val tabW = 24.dp
        // Size: use all the height and width the screen gives us
        val pageWFromWidth = (maxWidth - tabW * 2 + 8.dp) / 2
        val bookHAvail = (maxHeight - 92.dp).coerceAtLeast(320.dp)
        val cardWFromHeight = ((bookHAvail - 36.dp) / 2) * 0.665f
        val cardW: Dp = minOf(cardWFromHeight, pageWFromWidth - 28.dp)
        val cardH: Dp = cardW / 0.665f
        val pageW: Dp = cardW + 28.dp
        val bookH: Dp = cardH * 2 + 36.dp
        // A closed binder is portrait: its cover is wider than one page
        val coverK: Float = minOf(1.85f, (maxWidth - tabW * 2 - 8.dp) / pageW)

        val pageWpx = with(density) { pageW.toPx() }
        val stageWpx = with(density) { maxWidth.toPx() }
        val tabOverlapPx = with(density) { 8.dp.toPx() }

        // Left edge of the book (px). Closed: the cover is centred. Open: the spread is centred.
        fun bookLeftPx(pos: Float): Float {
            val p = pos.coerceIn(0f, 1f)
            val openLeft = (stageWpx - 2f * pageWpx) / 2f
            val closedLeft = stageWpx / 2f - coverK * pageWpx / 2f - pageWpx
            return closedLeft + (openLeft - closedLeft) * p
        }

        val activeMark = bookmarks.firstOrNull { it.key == selectedBookmark }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ---------------------------------------------------- header line
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = binder.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = titleColor
                    )
                    Text(
                        text = "${activeMark?.label ?: "ALL"}  •  ${cards.size} cards  •  $totalCollected collected",
                        style = MaterialTheme.typography.labelSmall,
                        color = subColor
                    )
                }
                if (spread > 0) {
                    Text(
                        text = "Close binder",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = accent,
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .border(1.dp, accent.copy(alpha = 0.5f), RoundedCornerShape(999.dp))
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

            // ----------------------------------------------------- the stage
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(bookH + 12.dp)
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
                    // soft shadow, page-edge thickness and cover slab under the book
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val pos = spread + flip.value
                        val p = pos.coerceIn(0f, 1f)
                        val bl = bookLeftPx(pos)
                        val left = bl + pageWpx * (1f - p)
                        val right = bl + pageWpx + pageWpx * (coverK + (1f - coverK) * p)
                        val top = 6.dp.toPx()
                        val h = bookH.toPx()
                        for (i in 1..16) {
                            val grow = i * 2.6f.dp.toPx()
                            drawRoundRect(
                                color = Color.Black.copy(alpha = if (dark) 0.030f else 0.020f),
                                topLeft = Offset(left - grow * 0.8f, top - grow * 0.4f + 16.dp.toPx()),
                                size = Size((right - left) + grow * 1.6f, h + grow * 0.8f),
                                cornerRadius = CornerRadius(14.dp.toPx() + grow)
                            )
                        }
                        // cover slab: gives the binder its thickness
                        drawRoundRect(
                            color = binder.coverSecondary,
                            topLeft = Offset(left + 3.dp.toPx(), top + 6.dp.toPx()),
                            size = Size(right - left, h),
                            cornerRadius = CornerRadius(16.dp.toPx())
                        )
                        // stacked page edges on the right and bottom of an open binder
                        val openA = ((p - 0.6f) / 0.4f).coerceIn(0f, 1f)
                        if (openA > 0f) {
                            for (i in 3 downTo 1) {
                                drawRoundRect(
                                    color = Color(0xFFEFE7D2).copy(alpha = openA),
                                    topLeft = Offset(left + i * 1.7.dp.toPx(), top + i * 1.7.dp.toPx()),
                                    size = Size(right - left, h),
                                    cornerRadius = CornerRadius(6.dp.toPx())
                                )
                                drawRoundRect(
                                    color = Color(0x33000000).copy(alpha = 0.2f * openA),
                                    topLeft = Offset(left + i * 1.7.dp.toPx(), top + i * 1.7.dp.toPx()),
                                    size = Size(right - left, h),
                                    cornerRadius = CornerRadius(6.dp.toPx()),
                                    style = Stroke(width = 0.8.dp.toPx())
                                )
                            }
                        }
                    }

                    // ---- the book: every sheet near the current spread is kept composed, so nothing reloads
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .offset { IntOffset(bookLeftPx(spread + flip.value).roundToInt(), 0) }
                            .size(pageW * 2, bookH)
                    ) {
                        if (spread >= sheetCount) {
                            Box(
                                modifier = Modifier
                                    .offset(x = pageW)
                                    .size(pageW, bookH)
                            ) { BackCover(binder = binder) }
                        }
                        val firstSheet = (spread - 2).coerceAtLeast(0)
                        val lastSheet = (spread + 1).coerceAtMost(sheetCount - 1)
                        for (k in firstSheet..lastSheet) {
                            key(k) {
                                BookSheet(
                                    k = k,
                                    spread = spread,
                                    flip = flip,
                                    pageW = pageW,
                                    bookH = bookH,
                                    coverK = coverK,
                                    front = {
                                        if (k == 0) {
                                            LeatherCover(binder = binder, totalCollected = totalCollected)
                                        } else {
                                            val pageIndex = (k - 1) * 2
                                            PaperPage(
                                                cards = pages.getOrNull(pageIndex).orEmpty(),
                                                pageNumber = pageIndex + 1,
                                                spineOnRight = false,
                                                binder = binder,
                                                cardW = cardW,
                                                cardH = cardH,
                                                onCardClick = { card ->
                                                    if (flip.value == 0f && k == spread) {
                                                        onButtonClickSfx()
                                                        onInspectCard(card.instanceId)
                                                    }
                                                }
                                            )
                                        }
                                    },
                                    back = {
                                        if (k == 0) {
                                            InsideCover(binder = binder, totalCollected = totalCollected)
                                        } else {
                                            val pageIndex = (k - 1) * 2 + 1
                                            PaperPage(
                                                cards = pages.getOrNull(pageIndex).orEmpty(),
                                                pageNumber = pageIndex + 1,
                                                spineOnRight = true,
                                                binder = binder,
                                                cardW = cardW,
                                                cardH = cardH,
                                                onCardClick = { card ->
                                                    if (flip.value == 0f && k == spread - 1) {
                                                        onButtonClickSfx()
                                                        onInspectCard(card.instanceId)
                                                    }
                                                }
                                            )
                                        }
                                    }
                                )
                            }
                        }
                        // metal rings along the spine
                        SpineRings(
                            pageH = bookH,
                            ringColor = binder.ringColor,
                            modifier = Modifier
                                .offset(x = pageW - 12.dp)
                                .size(24.dp, bookH)
                                .zIndex(1000f)
                                .graphicsLayer {
                                    val p = (spread + flip.value).coerceIn(0f, 1f)
                                    alpha = ((p - 0.02f) * 8f).coerceIn(0f, 1f)
                                }
                        )
                    }

                    // ---- bookmarks on the right edge of the binder
                    Column(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .offset {
                                val pos = spread + flip.value
                                val p = pos.coerceIn(0f, 1f)
                                val edge = bookLeftPx(pos) + pageWpx + pageWpx * (coverK + (1f - coverK) * p)
                                IntOffset((edge - tabOverlapPx).roundToInt(), 0)
                            }
                            .size(tabW, bookH)
                            .padding(vertical = 6.dp)
                            .zIndex(2000f),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        bookmarks.forEach { mark ->
                            BookmarkTab(
                                mark = mark,
                                dark = dark,
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
            }

            // ------------------------------------------------------ controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
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
                        tint = if (spread > 0) controlColor else controlColor.copy(alpha = 0.25f),
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
                    color = subColor,
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
                        tint = if (spread < sheetCount) controlColor else controlColor.copy(alpha = 0.25f),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

/**
 * One physical sheet of the binder (front face and back face). Sheets keep their content while the
 * binder is turned, so images are never rebuilt mid-flip. Everything that changes per frame is read
 * inside graphicsLayer, so turning pages does not recompose anything.
 */
@Composable
private fun BookSheet(
    k: Int,
    spread: Int,
    flip: Animatable<Float, AnimationVector1D>,
    pageW: Dp,
    bookH: Dp,
    coverK: Float,
    front: @Composable () -> Unit,
    back: @Composable () -> Unit
) {
    val isCover = k == 0
    fun angleOf(f: Float): Float = when {
        k < spread - 1 -> 180f
        k == spread - 1 -> if (f < 0f) 180f + f * 180f else 180f
        k == spread -> if (f > 0f) f * 180f else 0f
        else -> 0f
    }
    val showingBack by remember(k, spread) { derivedStateOf { angleOf(flip.value) > 90f } }
    val backward by remember(spread) { derivedStateOf { flip.value < 0f } }

    // Left stack: higher sheet on top. Right stack: lower sheet on top. The moving sheet is always on top.
    val z = when {
        k < spread - 1 -> k.toFloat()
        k == spread - 1 -> if (backward) 500f else k.toFloat()
        k == spread -> if (!backward) 500f else 100f - k
        else -> 100f - k
    }
    val sheetW = if (isCover) pageW * coverK else pageW

    Box(
        modifier = Modifier
            .offset(x = pageW)
            .size(sheetW, bookH)
            .zIndex(z)
            .graphicsLayer {
                val f = flip.value
                val rad = Math.toRadians(angleOf(f).toDouble()).toFloat()
                transformOrigin = TransformOrigin(0f, 0.5f)
                val widthFactor = if (isCover) {
                    val pos = (spread + f).coerceIn(0f, 1f)
                    1f + (1f / coverK - 1f) * pos
                } else {
                    1f
                }
                scaleX = cos(rad) * widthFactor
                scaleY = 1f + 0.05f * sin(rad)
            }
    ) {
        // back face (mirrored so it reads correctly once the sheet has turned over)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(if (showingBack) 1f else 0f)
                .alpha(if (showingBack) 1f else 0f),
            contentAlignment = Alignment.Center
        ) {
            if (isCover) {
                Box(
                    modifier = Modifier
                        .width(pageW)
                        .fillMaxHeight()
                        .graphicsLayer { scaleX = -coverK }
                ) { back() }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { scaleX = -1f }
                ) { back() }
            }
        }
        // front face
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(if (showingBack) 0f else 1f)
                .alpha(if (showingBack) 0f else 1f)
        ) { front() }
        // shade while the sheet is moving
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(5f)
                .graphicsLayer {
                    val rad = Math.toRadians(angleOf(flip.value).toDouble()).toFloat()
                    alpha = (0.30f * sin(rad)).coerceAtLeast(0f)
                }
                .background(Color.Black)
        )
    }
}

/** Soft desk: dark slate in dark mode, warm pearl in light mode, with a faint glow in the binder's colour. */
private fun Modifier.drawDesk(binder: CardBinderDesign, dark: Boolean): Modifier =
    this
        .background(
            Brush.verticalGradient(
                if (dark) listOf(Color(0xFF0B0F1C), Color(0xFF070A14), Color(0xFF05070E))
                else listOf(Color(0xFFFBF8F1), Color(0xFFF2EDE0), Color(0xFFEAE4D4))
            )
        )
        .background(
            Brush.radialGradient(
                listOf(binder.coverPrimary.copy(alpha = if (dark) 0.28f else 0.12f), Color.Transparent)
            )
        )

// ------------------------------------------------------------------ faces

@Composable
private fun LeatherCover(binder: CardBinderDesign, totalCollected: Int) {
    val shape = RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp, topEnd = 18.dp, bottomEnd = 18.dp)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(shape)
            .background(Brush.linearGradient(listOf(binder.coverPrimary, binder.coverSecondary)))
    ) {
        // spine strip
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(22.dp)
                .background(binder.spineColor)
        )
        // one clean brass line frame
        Canvas(modifier = Modifier.fillMaxSize()) {
            val inset = 20.dp.toPx()
            drawRoundRect(
                color = binder.trimGold.copy(alpha = 0.85f),
                topLeft = Offset(inset + 22.dp.toPx(), inset),
                size = Size(size.width - inset * 2 - 22.dp.toPx(), size.height - inset * 2),
                cornerRadius = CornerRadius(10.dp.toPx()),
                style = Stroke(width = 1.2.dp.toPx())
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
                modifier = Modifier.size(26.dp)
            )
            Text(
                text = binder.name.uppercase(),
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.4.sp
                ),
                color = binder.trimGold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "$totalCollected CARDS",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                color = Color.White.copy(alpha = 0.7f)
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
                    start = if (spineOnRight) 4.dp else 18.dp,
                    end = if (spineOnRight) 18.dp else 4.dp,
                    top = 10.dp,
                    bottom = 16.dp
                ),
            verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
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
            .size(cardW + 6.dp, cardH)
            .clip(shape)
            .background(Color(0x1F000000))
            .border(1.dp, Color(0x40000000), shape)
            .clickable(enabled = card != null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (card != null) {
            BinderCardFace(
                card = card,
                modifier = Modifier.size(cardW, cardH - 6.dp)
            )
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

/** Static, lightweight card for the binder: just the art, a rarity frame and the name. No animation, so turning pages stays smooth. */
@Composable
private fun BinderCardFace(card: OwnedCard, modifier: Modifier) {
    val context = LocalContext.current
    val request = remember(card.imageUrl) {
        buildMlbbCardImageRequest(context, card.imageUrl, crossfadeMs = 0, compact = true)
    }
    val shape = RoundedCornerShape(7.dp)
    val rarityColor = card.rarity.primaryColor
    Box(
        modifier = modifier
            .clip(shape)
            .background(Color(0xFF111827))
            .border(1.5.dp, rarityColor.copy(alpha = 0.9f), shape)
    ) {
        AsyncImage(
            model = request,
            contentDescription = card.skinName,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.78f))))
                .padding(horizontal = 6.dp, vertical = 5.dp)
        ) {
            Text(
                text = card.heroName,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = card.skinName,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.72f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (card.isMisprint) {
            Text(
                text = "1/1",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .background(Color(0xFFF43F5E), RoundedCornerShape(5.dp))
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            )
        }
    }
}

@Composable
private fun BookmarkTab(
    mark: BinderBookmark,
    selected: Boolean,
    enabled: Boolean,
    dark: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val pullOut = if (selected) 8.dp else 0.dp
    val shape = RoundedCornerShape(topEnd = 9.dp, bottomEnd = 9.dp)
    val paper = if (dark) Color(0xFF2A3142) else Color(0xFFF3ECDA)
    val ink = if (dark) Color(0xFFE5E9F2) else Color(0xFF2B2A26)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .offset(x = pullOut)
            .clip(shape)
            .background(if (selected) paper else paper.copy(alpha = 0.92f))
            .border(
                BorderStroke(
                    if (selected) 1.2.dp else 0.5.dp,
                    if (selected) mark.color else ink.copy(alpha = 0.18f)
                ),
                shape
            )
            .clickable(enabled = enabled, onClick = onClick)
            .testTag("binder_bookmark_${mark.key}"),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxHeight()
                .width(if (selected) 7.dp else 4.dp)
                .background(mark.color.copy(alpha = if (enabled) 1f else 0.3f))
        )
        Text(
            text = mark.label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.ExtraBold),
            color = ink.copy(alpha = if (enabled) 1f else 0.4f),
            maxLines = 1,
            softWrap = false,
            modifier = Modifier
                .padding(start = 4.dp)
                .rotate(90f)
        )
    }
}
