package com.studygrove.app.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.studygrove.app.data.model.CharacterCustomization
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt

internal enum class AvatarFacing(val row: Int) {
    Front(0), Back(1), Right(2), Left(2);

    fun toward(dx: Float, dy: Float): AvatarFacing {
        if (dx == 0f && dy == 0f) return this
        // Keep the current axis near a diagonal so small joystick changes do not flicker.
        val horizontal = when {
            abs(dx) > abs(dy) * 1.15f -> true
            abs(dy) > abs(dx) * 1.15f -> false
            else -> this == Right || this == Left
        }
        return if (horizontal) {
            if (dx < 0f) Left else Right
        } else {
            if (dy < 0f) Back else Front
        }
    }
}

// After the three body rows, each wearable has front, back and right profiles.
private enum class Wearable {
    Cap, Brim, Wizard, Crown, Flowers, Cat, Bunny, Pack, Bag, Wings,
    Glasses, Goggles, Scarf, Cape, Pin, Apron, Coat, Stripes, Star;

    fun index(facing: AvatarFacing) = 27 + ordinal * 3 + facing.row
}

private fun CharacterCustomization.layers() = listOfNotNull(
    when {
        shirt == "striped_shirt" -> Wearable.Stripes
        shirt == "star_shirt" -> Wearable.Star
        shirt.contains("apron") || shirt.contains("overalls") -> Wearable.Apron
        listOf("uniform", "cardigan", "coat", "robe").any { it in shirt } -> Wearable.Coat
        else -> null
    },
    when (backpack) {
        null -> null
        "messenger_bag" -> Wearable.Bag
        else -> Wearable.Pack
    },
    when (hat) {
        null -> null
        "bunny_ears" -> Wearable.Bunny
        "cat_ear_hat" -> Wearable.Cat
        "wizard_hat" -> Wearable.Wizard
        "flower_crown" -> Wearable.Flowers
        "tiny_crown", "star_crown" -> Wearable.Crown
        "explorer_hat", "straw_hat" -> Wearable.Brim
        else -> Wearable.Cap
    },
    when {
        accessory == null -> null
        "glasses" in accessory -> Wearable.Glasses
        "goggles" in accessory -> Wearable.Goggles
        "scarf" in accessory -> Wearable.Scarf
        "cape" in accessory -> Wearable.Cape
        "pin" in accessory -> Wearable.Pin
        "wings" in accessory -> Wearable.Wings
        else -> null
    }
)

/** Recolor each material around its midtone, retaining its authored highlights and shadows. */
private fun CharacterCustomization.colorSheet(source: ImageBitmap): ImageBitmap {
    val replacements = mutableMapOf<Int, Int>()
    fun dye(target: Color, outline: Int, shadow: Int, mid: Int, light: Int) {
        val to = target.toArgb()
        for (color in intArrayOf(outline, shadow, mid, light)) {
            fun channel(shift: Int): Int {
                val value = (color shr shift) and 255
                val base = (mid shr shift) and 255
                val chosen = (to shr shift) and 255
                return if (value <= base) chosen * value / base
                else chosen + (255 - chosen) * (value - base) / (255 - base)
            }
            replacements[color or 0xff000000.toInt()] =
                android.graphics.Color.rgb(channel(16), channel(8), channel(0))
        }
    }
    val skin = when (skinTone) {
        "deep" -> Color(0xFF9D684F)
        "light" -> Color(0xFFF2C9A6)
        else -> Color(0xFFE5AD83)
    }
    val hair = when {
        "aurora" in hairStyle -> Color(0xFF79BBC9)
        "leaf" in hairStyle -> Color(0xFF79A462)
        "gold" in hairStyle -> Color(0xFFD3A45D)
        else -> Color(0xFF795139)
    }
    val hatColor = when (hat) {
        "red_cap" -> Color(0xFFC75E55)
        "green_hat" -> Color(0xFF79A169)
        "tiny_crown", "star_crown" -> Color(0xFFDFB758)
        "prism_hat", "wizard_hat" -> Color(0xFF9F89B9)
        else -> Color(0xFFC6A268)
    }
    val boots = when (accessory) {
        "black_boots" -> Color(0xFF52515C)
        "green_boots" -> Color(0xFF6C8564)
        "silver_boots" -> Color(0xFFABB6BE)
        "brown_boots" -> Color(0xFF996647)
        else -> Color(0xFF795747)
    }
    val trim = when (accessory) {
        "golden_glasses", "clockwork_goggles" -> Color(0xFFCEAB6B)
        "garden_cape" -> Color(0xFF769674)
        "eternal_glasses" -> Color(0xFF8E9CB9)
        else -> Color(0xFFBB7980)
    }
    dye(skin, 0x985f50, 0xbf8265, 0xe5ad83, 0xf8cc9f)
    dye(hair, 0x382d32, 0x573c32, 0x795139, 0xa2764c)
    dye(clothingColor(shirt), 0x315456, 0x416f63, 0x5f9475, 0x92b796)
    dye(boots, 0x38353e, 0x574342, 0x795747, 0xac8568)
    dye(hatColor, 0x69503d, 0x98754d, 0xc6a268, 0xe4c68f)
    val bagColor = when (backpack) {
        "backpack", "messenger_bag", null -> Color(0xFFA87C54)
        "dream_backpack" -> Color(0xFF9C85AC)
        else -> clothingColor(backpack)
    }
    dye(bagColor, 0x504348, 0x785745, 0xa87c54, 0xcda978)
    dye(trim, 0x624652, 0x975c62, 0xbb7980, 0xdeb0a0)

    val pixels = IntArray(source.width * source.height)
    source.asAndroidBitmap().getPixels(pixels, 0, source.width, 0, 0, source.width, source.height)
    for (i in pixels.indices) {
        if (pixels[i] != 0) pixels[i] = replacements[pixels[i]] ?: pixels[i]
    }
    return Bitmap.createBitmap(pixels, source.width, source.height, Bitmap.Config.ARGB_8888).asImageBitmap()
}

@Composable
internal fun CharacterSprite(
    character: CharacterCustomization,
    modifier: Modifier = Modifier,
    facing: AvatarFacing = AvatarFacing.Front,
    frame: Int = 0
) {
    val sheet = PixelSheet.Avatar
    val source = rememberSheet(sheet)
    val colored = remember(character, source) { character.colorSheet(source) }
    val gear = remember(character) { character.layers() }
    Canvas(modifier) {
        val pixel = min(size.width / sheet.width, size.height / sheet.height)
        val bounds = IntSize((sheet.width * pixel).roundToInt(), (sheet.height * pixel).roundToInt())
        val origin = IntOffset(((size.width - bounds.width) / 2).roundToInt(), (size.height - bounds.height).roundToInt())
        val rises = frame == 3 || frame == 4 || frame == 7 || frame == 8
        val shoulder = if (rises) origin.copy(y = origin.y - pixel.roundToInt()) else origin
        scale(scaleX = if (facing == AvatarFacing.Left) -1f else 1f, scaleY = 1f) {
            drawPixelFrame(colored, sheet, facing.row * 9 + frame, origin, bounds)
            for (layer in gear) {
                drawPixelFrame(colored, sheet, layer.index(facing), shoulder, bounds)
            }
        }
    }
}
