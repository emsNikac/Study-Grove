package com.studygrove.app.ui.house

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.IntSize
import com.studygrove.app.data.model.FurnitureJoin
import com.studygrove.app.data.model.furnitureAxis
import com.studygrove.app.data.model.HouseObject
import com.studygrove.app.data.model.ShopCatalog
import com.studygrove.app.ui.components.PixelSheet
import com.studygrove.app.ui.components.furnitureFrames
import com.studygrove.app.ui.components.furnitureView
import com.studygrove.app.ui.components.rememberSheet
import kotlin.math.min
import kotlin.math.roundToInt

private const val TilePixels = 48

@Composable
internal fun ConnectedFurnitureLayer(
    joins: List<FurnitureJoin>,
    columns: Int,
    rows: Int,
    modifier: Modifier
) {
    if (joins.isEmpty()) return
    val firstSheet = rememberSheet(PixelSheet.FurnitureTurnsA)
    val secondSheet = rememberSheet(PixelSheet.FurnitureTurnsB)
    val image = remember(joins, columns, rows, firstSheet, secondSheet) {
        val width = columns * TilePixels
        val height = rows * TilePixels
        val pixels = IntArray(width * height)
        val frames = mutableMapOf<Pair<PixelSheet, Int>, IntArray>()
        fun sprite(obj: HouseObject): IntArray? {
            val key = ShopCatalog.byId(obj.shopItemId)?.visualKey ?: return null
            val row = furnitureFrames[key] ?: return null
            val view = furnitureView(row, obj.rotationDegrees)
            return frames.getOrPut(view) {
                val sheet = if (view.first == PixelSheet.FurnitureTurnsA) firstSheet else secondSheet
                val across = sheet.width / TilePixels
                IntArray(TilePixels * TilePixels).also {
                    sheet.asAndroidBitmap().getPixels(it, 0, TilePixels,
                        view.second % across * TilePixels, view.second / across * TilePixels, TilePixels, TilePixels)
                }
            }
        }
        val before = joins.associateBy { it.second }
        val after = joins.associateBy { it.first }
        val docked = (before.keys + after.keys).associateWith { obj ->
            val frame = sprite(obj) ?: return@associateWith null
            val (dx, dy) = furnitureAxis(obj.rotationDegrees)
            dockSprite(frame, dx, dy, obj in before, obj in after)
        }
        for (join in joins) {
            val first = docked[join.first] ?: continue
            val second = docked[join.second] ?: continue
            drawFurnitureSeam(join, first, second) { x, y, color -> pixels[y * width + x] = color }
        }
        for ((obj, frame) in docked) {
            if (frame == null) continue
            for (y in 0 until TilePixels) {
                val start = (obj.y * TilePixels + y) * width + obj.x * TilePixels
                for (x in 0 until TilePixels) {
                    val color = frame[y * TilePixels + x]
                    if (color ushr 24 != 0 && pixels[start + x] == 0) pixels[start + x] = color
                }
            }
        }
        Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888).asImageBitmap()
    }
    Canvas(modifier) {
        drawImage(image, dstSize = IntSize(size.width.roundToInt(), size.height.roundToInt()), filterQuality = FilterQuality.None)
    }
}

/** Anchor a single joining edge, or widen a middle section, within the item's own placement tile. */
internal fun dockSprite(pixels: IntArray, dx: Int, dy: Int, before: Boolean, after: Boolean): IntArray {
    var left = TilePixels
    var right = 0
    var top = TilePixels
    var bottom = 0
    for (i in pixels.indices) {
        if (pixels[i] ushr 24 < 224) continue
        val x = i % TilePixels
        val y = i / TilePixels
        left = minOf(left, x)
        right = maxOf(right, x + 1)
        top = minOf(top, y)
        bottom = maxOf(bottom, y + 1)
    }
    if (right <= left || bottom <= top) return pixels
    fun samples(low: Int, high: Int, direction: Int): IntArray {
        val negative = (before && direction > 0) || (after && direction < 0)
        val positive = (before && direction < 0) || (after && direction > 0)
        val extent = high - low
        val length = if (negative && positive) min(TilePixels, extent * 5 / 4) else extent
        val start = when {
            negative && positive -> (TilePixels - length) / 2
            negative -> 0
            positive -> TilePixels - length
            else -> low
        }
        return IntArray(TilePixels) { low + Math.floorDiv((it - start) * extent, length) }
    }
    val xs = samples(left, right, dx)
    val ys = samples(top, bottom, dy)
    return IntArray(pixels.size) {
        val x = xs[it % TilePixels]
        val y = ys[it / TilePixels]
        if (x in 0 until TilePixels && y in 0 until TilePixels) pixels[y * TilePixels + x] else 0
    }
}

private data class EdgeSpan(val x: Int, val y: Int, val gap: Int, val firstColor: Int, val secondColor: Int)

/** Extend the closest opposing material edges. Transparent margins and cast shadows never form joints. */
internal fun drawFurnitureSeam(join: FurnitureJoin, first: IntArray, second: IntArray, put: (Int, Int, Int) -> Unit) {
    val spans = mutableListOf<EdgeSpan>()
    val dx = join.dx
    val dy = join.dy
    val limit = TilePixels - 1
    fun scan(startX: Int, startY: Int) {
        var x = startX
        var y = startY
        var step = 0
        var farEdge = -1
        var nearEdge = -1
        var firstColor = 0
        var secondColor = 0
        val increment = dx + dy * TilePixels
        while (x in 0..limit && y in 0..limit) {
            val i = y * TilePixels + x
            if (first[i] ushr 24 >= 224) {
                farEdge = step
                firstColor = if (step > 0 && first[i - increment] ushr 24 >= 224) first[i - increment] else first[i]
            }
            if (nearEdge < 0 && second[i] ushr 24 >= 224) {
                nearEdge = step
                secondColor = if (x + dx in 0..limit && y + dy in 0..limit && second[i + increment] ushr 24 >= 224)
                    second[i + increment] else second[i]
            }
            x += dx
            y += dy
            step++
        }
        if (farEdge >= 0 && nearEdge >= 0) {
            spans += EdgeSpan(startX + (farEdge + 1) * dx, startY + (farEdge + 1) * dy,
                TilePixels + nearEdge - farEdge - 1, firstColor, secondColor)
        }
    }
    if (dx != 0) for (y in 0..limit) scan(if (dx > 0) 0 else limit, y)
    if (dy != 0) for (x in 0..limit) {
        if (dx == 0 || x != (if (dx > 0) 0 else limit)) scan(x, 0)
    }
    val shortest = spans.minOfOrNull { it.gap } ?: return
    // A matching suite shares a wider surface. Mixed pieces meet at their closest edges.
    val contactWidth = if (join.first.shopItemId == join.second.shopItemId) 13 else 2
    for (span in spans) {
        if (span.gap > shortest + contactWidth) continue
        for (i in 0 until span.gap) {
            put(join.first.x * TilePixels + span.x + dx * i, join.first.y * TilePixels + span.y + dy * i,
                if (i * 2 < span.gap) span.firstColor else span.secondColor)
        }
    }
}
