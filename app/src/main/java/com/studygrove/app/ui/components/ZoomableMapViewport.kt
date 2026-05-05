package com.studygrove.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studygrove.app.ui.theme.GroveCream
import com.studygrove.app.ui.theme.GroveInk

@Composable
fun ZoomableMapViewport(
    gridWidth: Int,
    gridHeight: Int,
    panelColor: Color,
    modifier: Modifier = Modifier,
    maxScale: Float = 4f,
    content: @Composable BoxScope.() -> Unit
) {
    var scale by rememberSaveable { mutableStateOf(1f) }
    var offsetX by rememberSaveable { mutableStateOf(0f) }
    var offsetY by rememberSaveable { mutableStateOf(0f) }
    val safeGridWidth = gridWidth.coerceAtLeast(1)
    val safeGridHeight = gridHeight.coerceAtLeast(1)
    val viewportAspect = safeGridWidth.toFloat() / safeGridHeight.toFloat()

    LaunchedEffect(safeGridWidth, safeGridHeight) {
        scale = 1f
        offsetX = 0f
        offsetY = 0f
    }

    PixelPanel(color = panelColor, modifier = modifier) {
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val density = LocalDensity.current
            val viewportWidthPx = with(density) { maxWidth.toPx() }
            val viewportHeightPx = viewportWidthPx / viewportAspect

            fun clampOffset(raw: Offset, nextScale: Float): Offset {
                if (nextScale <= 1.001f) return Offset.Zero
                val maxX = viewportWidthPx * (nextScale - 1f) / 2f
                val maxY = viewportHeightPx * (nextScale - 1f) / 2f
                return Offset(
                    x = raw.x.coerceIn(-maxX, maxX),
                    y = raw.y.coerceIn(-maxY, maxY)
                )
            }

            fun applyTransform(pan: Offset, zoom: Float) {
                val oldScale = scale
                val nextScale = (scale * zoom).coerceIn(1f, maxScale)
                val scaleDelta = if (oldScale == 0f) 1f else nextScale / oldScale
                val nextOffset = clampOffset(
                    raw = Offset(
                        x = (offsetX + pan.x) * scaleDelta,
                        y = (offsetY + pan.y) * scaleDelta
                    ),
                    nextScale = nextScale
                )
                scale = nextScale
                offsetX = nextOffset.x
                offsetY = nextOffset.y
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(viewportAspect)
                    .clip(RoundedCornerShape(4.dp))
                    .clipToBounds()
                    .background(panelColor)
                    .pointerInput(viewportWidthPx, viewportHeightPx, maxScale) {
                        awaitEachGesture {
                            var controllingMap = false
                            do {
                                val event = awaitPointerEvent(PointerEventPass.Initial)
                                val pressedCount = event.changes.count { it.pressed }
                                val zoom = event.calculateZoom()
                                val pan = event.calculatePan()
                                val hasMovement = event.changes.any { it.positionChanged() }
                                val shouldControlMap = pressedCount > 1 ||
                                    controllingMap ||
                                    (scale > 1.01f && pressedCount == 1 && hasMovement)

                                if (shouldControlMap) {
                                    controllingMap = true
                                    applyTransform(pan = pan, zoom = zoom)
                                    event.changes.forEach { change ->
                                        if (change.pressed) change.consume()
                                    }
                                }
                            } while (event.changes.any { it.pressed })
                        }
                    }
            ) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            translationX = offsetX
                            translationY = offsetY
                            transformOrigin = TransformOrigin.Center
                        },
                    content = content
                )
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    MapZoomButton(text = "-") {
                        applyTransform(pan = Offset.Zero, zoom = 0.82f)
                    }
                    MapZoomButton(text = "+") {
                        applyTransform(pan = Offset.Zero, zoom = 1.22f)
                    }
                    if (scale > 1.01f) {
                        MapZoomButton(text = "Reset ${"%.1f".format(scale)}x") {
                            scale = 1f
                            offsetX = 0f
                            offsetY = 0f
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MapZoomButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(GroveInk.copy(alpha = 0.78f))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 5.dp)
    ) {
        Text(
            text = text,
            color = GroveCream,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace
        )
    }
}
