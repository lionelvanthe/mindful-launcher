package com.indie.mindfullauncher.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.roundToInt

/**
 * Applies Niagara-inspired tactile press scale feedback (quick scale-down on press,
 * subtle spring overshoot on release).
 */
fun Modifier.pressScale(
    interactionSource: MutableInteractionSource,
    pressedScale: Float = 0.96f
): Modifier = composed {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "press_scale"
    )
    graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/**
 * Applies soft top and bottom alpha fading edges to scrollable containers using offscreen
 * DstIn blending, preventing hard clipping against OLED black backgrounds.
 */
fun Modifier.verticalFadingEdge(
    topFadeHeight: Dp = 20.dp,
    bottomFadeHeight: Dp = 20.dp
): Modifier = this
    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
    .drawWithContent {
        drawContent()
        val height = size.height
        if (height <= 0f) return@drawWithContent
        val topPx = topFadeHeight.toPx().coerceAtMost(height / 2f)
        val bottomPx = bottomFadeHeight.toPx().coerceAtMost(height / 2f)

        if (topPx > 0f) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color.Black),
                    startY = 0f,
                    endY = topPx
                ),
                blendMode = BlendMode.DstIn
            )
        }
        if (bottomPx > 0f) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Black, Color.Transparent),
                    startY = height - bottomPx,
                    endY = height
                ),
                blendMode = BlendMode.DstIn
            )
        }
    }

private sealed interface WaveSlot {
    data object Favorites : WaveSlot
    data class Letter(val char: Char) : WaveSlot
    data object Search : WaveSlot
}

/**
 * Right-edge vertical alphabet scrubber with a floating handle and continuous Niagara wave
 * animation covering Favorites (★), A–Z letters, and Search (🔍) in a single fluid gesture.
 */
@Composable
fun AlphabetScrubber(
    letters: List<Char>,
    onLetterSelected: (Char) -> Unit,
    modifier: Modifier = Modifier,
    isFavoritesActive: Boolean = false,
    onFavoritesSelected: (() -> Unit)? = null,
    onSearchSelected: (() -> Unit)? = null
) {
    if (letters.isEmpty()) return

    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current

    val slots = remember(letters, onFavoritesSelected != null, onSearchSelected != null) {
        buildList {
            if (onFavoritesSelected != null) add(WaveSlot.Favorites)
            letters.forEach { add(WaveSlot.Letter(it)) }
            if (onSearchSelected != null) add(WaveSlot.Search)
        }
    }

    var railHeightPx by remember { mutableIntStateOf(0) }
    var activeIndex by remember { mutableIntStateOf(-1) }
    var pointerY by remember { mutableFloatStateOf(0f) }

    fun updateSelection(y: Float) {
        if (railHeightPx <= 0 || slots.isEmpty()) return
        val clampedY = y.coerceIn(0f, railHeightPx.toFloat())
        pointerY = clampedY
        val fraction = (clampedY / railHeightPx).coerceIn(0f, 0.9999f)
        val index = (fraction * slots.size).toInt().coerceIn(0, slots.lastIndex)
        if (activeIndex != index) {
            activeIndex = index
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            when (val slot = slots[index]) {
                WaveSlot.Favorites -> onFavoritesSelected?.invoke()
                is WaveSlot.Letter -> onLetterSelected(slot.char)
                WaveSlot.Search -> Unit // Triggered on release for smooth search opening
            }
        }
    }

    val activeSlot = slots.getOrNull(activeIndex)

    BoxWithConstraints(
        modifier = modifier.fillMaxHeight(),
        contentAlignment = Alignment.CenterEnd
    ) {
        val availableDp = (maxHeight - 16.dp).coerceAtLeast(160.dp)
        // Niagara alphabet_wave_max_section_size = 19sp
        val slotHeightDp = (availableDp / slots.size).coerceIn(11.dp, 19.dp)
        val letterFontSp = when {
            slotHeightDp < 13.dp -> 10.sp
            slotHeightDp < 16.dp -> 11.sp
            else -> 12.sp
        }

        // Niagara AlphabetWaveHandle: diameter = 44dp, elevation = 4dp, text = 28sp
        val handleDiameterDp = 44.dp
        val handleSizePx = with(density) { handleDiameterDp.toPx() }
        val handleOffsetX = with(density) { (-96).dp.toPx() }.roundToInt()
        val maxHalfRange = ((railHeightPx - handleSizePx) / 2f).coerceAtLeast(0f).roundToInt()
        val targetHandleY = (pointerY - railHeightPx / 2f).roundToInt()
            .coerceIn(-maxHalfRange, maxHalfRange)
        val smoothHandleY by animateIntAsState(
            targetValue = targetHandleY,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessHigh
            ),
            label = "handle_y"
        )

        AnimatedVisibility(
            visible = activeSlot != null,
            enter = fadeIn() + scaleIn(initialScale = 0.8f),
            exit = fadeOut() + scaleOut(targetScale = 0.8f),
            modifier = Modifier.offset { IntOffset(handleOffsetX, smoothHandleY) }
        ) {
            Box(
                modifier = Modifier
                    .size(handleDiameterDp)
                    .shadow(4.dp, CircleShape)
                    .clip(CircleShape)
                    .background(colors.secondary),
                contentAlignment = Alignment.Center
            ) {
                when (activeSlot) {
                    WaveSlot.Favorites -> Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = colors.onSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                    WaveSlot.Search -> Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = colors.onSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                    is WaveSlot.Letter -> Text(
                        text = activeSlot.char.toString(),
                        color = colors.onSecondary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.SemiBold,
                        style = typography.headlineMedium
                    )
                    null -> Unit
                }
            }
        }

        Column(
            modifier = Modifier
                .width(36.dp)
                .onSizeChanged { railHeightPx = it.height }
                .pointerInput(slots) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        down.consume()
                        updateSelection(down.position.y)
                        while (true) {
                            val event = awaitPointerEvent()
                            val activeChange = event.changes.firstOrNull { it.pressed }
                            if (activeChange == null) {
                                if (slots.getOrNull(activeIndex) == WaveSlot.Search) {
                                    onSearchSelected?.invoke()
                                }
                                activeIndex = -1
                                break
                            }
                            activeChange.consume()
                            updateSelection(activeChange.position.y)
                        }
                    }
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Niagara alphabet_wave_pressed_offset = 64dp with smooth Gaussian arc
            val maxWavePx = with(density) { 64.dp.toPx() }
            slots.forEachIndexed { idx, slot ->
                val isSelected = activeIndex == idx
                val targetWaveOffset = if (activeIndex >= 0) {
                    val dist = abs(idx - activeIndex).toFloat()
                    (-maxWavePx * exp(-(dist * dist) / 7.5f)).roundToInt()
                } else {
                    0
                }
                val animatedWaveOffset by animateIntAsState(
                    targetValue = targetWaveOffset,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "wave_$idx"
                )
                Box(
                    modifier = Modifier
                        .height(slotHeightDp)
                        .width(36.dp)
                        .offset { IntOffset(animatedWaveOffset, 0) },
                    contentAlignment = Alignment.Center
                ) {
                    when (slot) {
                        WaveSlot.Favorites -> Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Yêu thích",
                            tint = if (isSelected || isFavoritesActive) {
                                colors.secondary
                            } else {
                                colors.onBackground.copy(alpha = 0.82f)
                            },
                            modifier = Modifier.size(if (isSelected) 17.dp else 14.dp)
                        )
                        WaveSlot.Search -> Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Tìm kiếm",
                            tint = if (isSelected) {
                                colors.secondary
                            } else {
                                colors.onBackground.copy(alpha = 0.82f)
                            },
                            modifier = Modifier.size(if (isSelected) 17.dp else 14.dp)
                        )
                        is WaveSlot.Letter -> Text(
                            text = slot.char.toString(),
                            color = if (isSelected) {
                                colors.secondary
                            } else {
                                colors.onBackground.copy(alpha = 0.82f)
                            },
                            fontSize = if (isSelected) (letterFontSp.value + 2f).sp else letterFontSp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                            style = typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}
