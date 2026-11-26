package com.indie.mindfullauncher.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
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

/**
 * Right-edge vertical alphabet scrubber with a floating letter handle for rapid A-Z navigation.
 */
@Composable
fun AlphabetScrubber(
    letters: List<Char>,
    onLetterSelected: (Char) -> Unit,
    modifier: Modifier = Modifier,
    onFavoritesSelected: (() -> Unit)? = null,
    onSearchSelected: (() -> Unit)? = null
) {
    if (letters.isEmpty()) return

    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current

    var railHeightPx by remember { mutableIntStateOf(0) }
    var activeLetter by remember { mutableStateOf<Char?>(null) }
    var pointerY by remember { mutableFloatStateOf(0f) }

    fun updateSelection(y: Float) {
        if (railHeightPx <= 0 || letters.isEmpty()) return
        val clampedY = y.coerceIn(0f, railHeightPx.toFloat())
        pointerY = clampedY
        val fraction = (clampedY / railHeightPx).coerceIn(0f, 0.9999f)
        val index = (fraction * letters.size).toInt().coerceIn(0, letters.lastIndex)
        val letter = letters[index]
        if (activeLetter != letter) {
            activeLetter = letter
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onLetterSelected(letter)
        }
    }

    Box(
        modifier = modifier.fillMaxHeight(),
        contentAlignment = Alignment.CenterEnd
    ) {
        // Floating letter handle offset to the left of the scrubber rail
        val handleSizePx = with(density) { 48.dp.toPx() }
        val handleOffsetX = with(density) { (-64).dp.toPx() }.roundToInt()
        val maxHalfRange = ((railHeightPx - handleSizePx) / 2f).coerceAtLeast(0f).roundToInt()
        val handleOffsetY = (pointerY - railHeightPx / 2f).roundToInt()
            .coerceIn(-maxHalfRange, maxHalfRange)

        AnimatedVisibility(
            visible = activeLetter != null,
            enter = fadeIn() + scaleIn(initialScale = 0.8f),
            exit = fadeOut() + scaleOut(targetScale = 0.8f),
            modifier = Modifier.offset { IntOffset(handleOffsetX, handleOffsetY) }
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(colors.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = activeLetter?.toString().orEmpty(),
                    color = colors.onPrimary,
                    style = typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Column(
            modifier = Modifier
                .width(36.dp)
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
        ) {
            if (onFavoritesSelected != null) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Yêu thích",
                    tint = colors.onBackground.copy(alpha = 0.85f),
                    modifier = Modifier
                        .size(16.dp)
                        .clickable(onClick = onFavoritesSelected)
                )
            }

            Column(
                modifier = Modifier
                    .width(36.dp)
                    .onSizeChanged { railHeightPx = it.height }
                    .pointerInput(letters) {
                        detectTapGestures(
                            onPress = { offset ->
                                updateSelection(offset.y)
                                tryAwaitRelease()
                                activeLetter = null
                            }
                        )
                    }
                    .pointerInput(letters) {
                        detectDragGestures(
                            onDragStart = { offset -> updateSelection(offset.y) },
                            onDrag = { change, _ ->
                                change.consume()
                                updateSelection(change.position.y)
                            },
                            onDragEnd = { activeLetter = null },
                            onDragCancel = { activeLetter = null }
                        )
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically)
            ) {
                val activeIndex = activeLetter?.let { letters.indexOf(it) } ?: -1
                val maxWavePx = with(density) { 20.dp.toPx() }
                letters.forEachIndexed { idx, letter ->
                    val isSelected = activeLetter == letter
                    val waveOffset = if (activeIndex >= 0) {
                        val dist = abs(idx - activeIndex).toFloat()
                        (-maxWavePx * exp(-(dist * dist) / 4.5f)).roundToInt()
                    } else {
                        0
                    }
                    Text(
                        text = letter.toString(),
                        color = if (isSelected) {
                            colors.secondary
                        } else {
                            colors.onBackground.copy(alpha = 0.85f)
                        },
                        fontSize = if (isSelected) 15.sp else 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                        style = typography.labelSmall,
                        modifier = Modifier.offset { IntOffset(waveOffset, 0) }
                    )
                }
            }

            if (onSearchSelected != null) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Tìm kiếm",
                    tint = colors.onBackground.copy(alpha = 0.85f),
                    modifier = Modifier
                        .size(16.dp)
                        .clickable(onClick = onSearchSelected)
                )
            }
        }
    }
}
