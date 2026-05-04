package com.studygrove.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val GroveCream = Color(0xFFFFF2D1)
val GroveMoss = Color(0xFF3F7A45)
val GroveLeaf = Color(0xFF72A35E)
val GroveClay = Color(0xFFC86D43)
val GroveBerry = Color(0xFFB85268)
val GroveInk = Color(0xFF302A2A)
val GrovePanel = Color(0xFFFFDFA9)
val GroveSky = Color(0xFFA9D7D8)
val GroveNight = Color(0xFF243655)

private val LightColors = lightColorScheme(
    primary = GroveMoss,
    secondary = GroveClay,
    tertiary = GroveBerry,
    background = GroveCream,
    surface = GrovePanel,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = GroveInk,
    onSurface = GroveInk
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF9FD18A),
    secondary = Color(0xFFE5A16D),
    tertiary = Color(0xFFE58AA0),
    background = GroveNight,
    surface = Color(0xFF3B3553),
    onPrimary = Color(0xFF183421),
    onSecondary = Color(0xFF3D2417),
    onTertiary = Color(0xFF3B1720),
    onBackground = Color(0xFFFFF2D1),
    onSurface = Color(0xFFFFF2D1)
)

private val GroveShapes = Shapes(
    extraSmall = RoundedCornerShape(2.dp),
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(6.dp),
    large = RoundedCornerShape(8.dp),
    extraLarge = RoundedCornerShape(8.dp)
)

@Composable
fun StudyGroveTheme(isNight: Boolean = false, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isNight) DarkColors else LightColors,
        shapes = GroveShapes,
        content = content
    )
}
