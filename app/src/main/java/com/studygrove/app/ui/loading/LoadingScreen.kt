package com.studygrove.app.ui.loading

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoadingScreen() {
    val transition = rememberInfiniteTransition(label = "study_grove_loading")
    val glow by transition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1100), RepeatMode.Reverse),
        label = "lantern_glow"
    )
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1500), RepeatMode.Restart),
        label = "loading_progress"
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF14243D))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawLoadingBackdrop(glow)
            drawStudyGroveLogo(glow)
        }
        Column(
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 38.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Loading your grove...",
                color = Color(0xFFFFF0C4),
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                fontSize = 15.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))
            Canvas(Modifier.fillMaxWidth(0.72f).height(18.dp)) {
                val border = Color(0xFF0B1324)
                val track = Color(0xFF6F4528)
                val fill = Color(0xFFB9D95B)
                drawRect(border, size = size)
                drawRect(track, topLeft = Offset(2f, 2f), size = Size(size.width - 4f, size.height - 4f))
                val filledWidth = (size.width - 8f) * progress
                drawRect(fill, topLeft = Offset(4f, 4f), size = Size(filledWidth, size.height - 8f))
                drawRect(Color.White.copy(alpha = 0.28f), topLeft = Offset(4f, 4f), size = Size(filledWidth, 3f))
            }
        }
    }
}

private fun DrawScope.drawLoadingBackdrop(glow: Float) {
    val star = Color(0xFFFFD66D)
    val w = size.width
    val h = size.height
    listOf(
        Offset(w * 0.18f, h * 0.24f),
        Offset(w * 0.32f, h * 0.16f),
        Offset(w * 0.74f, h * 0.20f),
        Offset(w * 0.86f, h * 0.34f),
        Offset(w * 0.12f, h * 0.62f),
        Offset(w * 0.79f, h * 0.67f)
    ).forEachIndexed { index, offset ->
        val unit = if (index % 2 == 0) 5f else 8f
        drawRect(star.copy(alpha = 0.72f + glow * 0.22f), topLeft = Offset(offset.x - unit / 2f, offset.y - unit / 2f), size = Size(unit, unit))
        drawRect(star.copy(alpha = 0.35f), topLeft = Offset(offset.x - unit * 1.4f, offset.y - 1f), size = Size(unit * 2.8f, 2f))
        drawRect(star.copy(alpha = 0.35f), topLeft = Offset(offset.x - 1f, offset.y - unit * 1.4f), size = Size(2f, unit * 2.8f))
    }
}

private fun DrawScope.drawStudyGroveLogo(glow: Float) {
    val u = size.minDimension / 100f
    val centerX = size.width / 2f
    val signWidth = 74f * u
    val signHeight = 32f * u
    val signX = centerX - signWidth / 2f
    val signY = size.height * 0.34f
    val ink = Color(0xFF0B1324)
    val woodDark = Color(0xFF5F371F)
    val wood = Color(0xFF8A552E)
    val woodLight = Color(0xFFC98244)
    val cream = Color(0xFFFFE8AE)
    val green = Color(0xFFB9D95B)
    val leaf = Color(0xFF73B24F)
    val leafLight = Color(0xFFA8D95F)

    drawRect(ink.copy(alpha = 0.55f), topLeft = Offset(signX + 3f * u, signY + signHeight + 2f * u), size = Size(signWidth - 6f * u, 3f * u))
    drawRect(woodDark, topLeft = Offset(signX - 2f * u, signY - 2f * u), size = Size(signWidth + 4f * u, signHeight + 4f * u))
    drawRect(wood, topLeft = Offset(signX, signY), size = Size(signWidth, signHeight))
    repeat(4) { index ->
        drawRect(woodLight.copy(alpha = 0.34f), topLeft = Offset(signX + 4f * u, signY + (5f + index * 6f) * u), size = Size(signWidth - 8f * u, 1.2f * u))
    }

    drawPixelText("Study", signX + 9f * u, signY + 5f * u, 3.0f * u, cream, ink)
    drawPixelText("Grove", signX + 10f * u, signY + 18f * u, 3.15f * u, green, ink)

    drawRect(ink, topLeft = Offset(centerX - 1f * u, signY - 11f * u), size = Size(2f * u, 10f * u))
    drawRect(leaf, topLeft = Offset(centerX - 8f * u, signY - 14f * u), size = Size(8f * u, 5f * u))
    drawRect(leafLight, topLeft = Offset(centerX - 7f * u, signY - 13f * u), size = Size(4f * u, 2f * u))
    drawRect(leaf, topLeft = Offset(centerX + 1f * u, signY - 14f * u), size = Size(8f * u, 5f * u))
    drawRect(leafLight, topLeft = Offset(centerX + 2f * u, signY - 13f * u), size = Size(4f * u, 2f * u))
    drawRect(Color(0xFF4F2E1D), topLeft = Offset(centerX - 8f * u, signY - 3f * u), size = Size(16f * u, 4f * u))
    drawRect(Color(0xFFC06C34), topLeft = Offset(centerX - 6f * u, signY - 5f * u), size = Size(12f * u, 4f * u))

    drawVine(signX - 7f * u, signY + 4f * u, u, leaf, leafLight)
    drawVine(signX + signWidth + 4f * u, signY + 5f * u, -u, leaf, leafLight)

    val lanternX = signX + signWidth - 8f * u
    val lanternY = signY - 13f * u
    drawRect(Color(0xFFFFC94A).copy(alpha = 0.18f * glow), topLeft = Offset(lanternX - 6f * u, lanternY - 2f * u), size = Size(14f * u, 20f * u))
    drawRect(ink, topLeft = Offset(lanternX, lanternY), size = Size(7f * u, 12f * u))
    drawRect(Color(0xFF34334F), topLeft = Offset(lanternX + 1f * u, lanternY + 1f * u), size = Size(5f * u, 10f * u))
    drawRect(Color(0xFFFFC94A), topLeft = Offset(lanternX + 2f * u, lanternY + 4f * u), size = Size(3f * u, 5f * u))
    drawRect(Color(0xFFFFF4BD).copy(alpha = glow), topLeft = Offset(lanternX + 3f * u, lanternY + 5f * u), size = Size(1f * u, 3f * u))

    val bookX = centerX - 15f * u
    val bookY = signY + signHeight + 3f * u
    drawRect(ink, topLeft = Offset(bookX - 1f * u, bookY + 1f * u), size = Size(30f * u, 13f * u))
    drawRect(Color(0xFFE9C68D), topLeft = Offset(bookX, bookY), size = Size(13f * u, 12f * u))
    drawRect(Color(0xFFFFE5A9), topLeft = Offset(bookX + 2f * u, bookY + 2f * u), size = Size(10f * u, 8f * u))
    drawRect(Color(0xFFD9A866), topLeft = Offset(bookX + 14f * u, bookY), size = Size(13f * u, 12f * u))
    drawRect(Color(0xFFFFE5A9), topLeft = Offset(bookX + 15f * u, bookY + 2f * u), size = Size(10f * u, 8f * u))
    drawRect(Color(0xFF5D8D3F), topLeft = Offset(bookX + 17f * u, bookY), size = Size(4f * u, 9f * u))

    val coinX = bookX + 32f * u
    drawCircle(Color(0xFF8C541E), radius = 6.2f * u, center = Offset(coinX, bookY + 6f * u))
    drawCircle(Color(0xFFFFC94A), radius = 5.2f * u, center = Offset(coinX, bookY + 6f * u))
    drawRect(Color(0xFFB66B1E), topLeft = Offset(coinX - 0.8f * u, bookY + 2f * u), size = Size(1.6f * u, 8f * u))
    drawRect(Color(0xFFB66B1E), topLeft = Offset(coinX - 3.8f * u, bookY + 5.2f * u), size = Size(7.6f * u, 1.6f * u))
}

private fun DrawScope.drawVine(x: Float, y: Float, direction: Float, leaf: Color, leafLight: Color) {
    val u = size.minDimension / 100f
    repeat(5) { index ->
        val baseY = y + index * 6f * u
        drawRect(Color(0xFF356837), topLeft = Offset(x + direction * index * 1.3f, baseY), size = Size(2f * u, 6f * u))
        drawRect(leaf, topLeft = Offset(x + direction * (2f + index), baseY + 1f * u), size = Size(5f * u, 3f * u))
        drawRect(leafLight, topLeft = Offset(x + direction * (2.5f + index), baseY + 1.5f * u), size = Size(2.5f * u, 1f * u))
    }
}

private fun DrawScope.drawPixelText(text: String, x: Float, y: Float, unit: Float, color: Color, shadow: Color) {
    var cursor = x
    text.uppercase().forEach { char ->
        if (char == ' ') {
            cursor += 4f * unit
        } else {
            drawPixelGlyph(char, cursor + unit * 0.45f, y + unit * 0.45f, unit, shadow)
            drawPixelGlyph(char, cursor, y, unit, color)
            cursor += 4f * unit
        }
    }
}

private fun DrawScope.drawPixelGlyph(char: Char, x: Float, y: Float, unit: Float, color: Color) {
    val rows = when (char) {
        'S' -> listOf("111", "100", "111", "001", "111")
        'T' -> listOf("111", "010", "010", "010", "010")
        'U' -> listOf("101", "101", "101", "101", "111")
        'D' -> listOf("110", "101", "101", "101", "110")
        'Y' -> listOf("101", "101", "010", "010", "010")
        'G' -> listOf("111", "100", "101", "101", "111")
        'R' -> listOf("110", "101", "110", "101", "101")
        'O' -> listOf("111", "101", "101", "101", "111")
        'V' -> listOf("101", "101", "101", "101", "010")
        'E' -> listOf("111", "100", "111", "100", "111")
        else -> listOf("111", "101", "101", "101", "111")
    }
    rows.forEachIndexed { rowIndex, row ->
        row.forEachIndexed { columnIndex, pixel ->
            if (pixel == '1') {
                drawRect(
                    color,
                    topLeft = Offset(x + columnIndex * unit, y + rowIndex * unit),
                    size = Size(unit, unit)
                )
            }
        }
    }
}
