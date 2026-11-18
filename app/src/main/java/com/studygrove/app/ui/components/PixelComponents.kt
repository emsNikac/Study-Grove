package com.studygrove.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studygrove.app.data.model.Achievement
import com.studygrove.app.data.model.CosmeticSlot
import com.studygrove.app.data.model.DailyQuest
import com.studygrove.app.data.model.GardenObject
import com.studygrove.app.data.model.GardenObjectType
import com.studygrove.app.data.model.HouseObject
import com.studygrove.app.data.model.PlacementTarget
import com.studygrove.app.data.model.ShopCatalog
import com.studygrove.app.data.model.ShopItem
import com.studygrove.app.ui.StudyGroveScreen
import com.studygrove.app.ui.theme.GroveBerry
import com.studygrove.app.ui.theme.GroveClay
import com.studygrove.app.ui.theme.GroveCream
import com.studygrove.app.ui.theme.GroveInk
import com.studygrove.app.ui.theme.GroveLeaf
import com.studygrove.app.ui.theme.GroveMoss
import com.studygrove.app.ui.theme.GrovePanel
import kotlin.math.min

@Composable
fun PixelPanel(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surface,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color)
            .border(2.dp, GroveInk.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
            .padding(12.dp)
    ) {
        content()
    }
}

@Composable
fun PixelButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    color: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    TextButton(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        },
        enabled = enabled,
        colors = ButtonDefaults.textButtonColors(
            containerColor = if (enabled) color else Color(0xFFB9B09D),
            contentColor = Color.White,
            disabledContentColor = Color.White.copy(alpha = 0.7f)
        ),
        shape = RoundedCornerShape(4.dp),
        modifier = modifier
            .height(46.dp)
            .border(2.dp, GroveInk.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Black,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun StatChip(label: String, value: String, modifier: Modifier = Modifier) {
    PixelPanel(modifier = modifier, color = GroveCream.copy(alpha = 0.88f)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, fontSize = 11.sp, color = GroveInk.copy(alpha = 0.68f), fontWeight = FontWeight.Bold)
            Text(value, fontSize = 16.sp, color = GroveInk, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier.padding(vertical = 6.dp),
        color = MaterialTheme.colorScheme.onBackground,
        fontWeight = FontWeight.Black,
        fontSize = 18.sp,
        fontFamily = FontFamily.Monospace
    )
}

@Composable
fun EmptyState(text: String, modifier: Modifier = Modifier) {
    PixelPanel(modifier = modifier.fillMaxWidth(), color = GroveCream.copy(alpha = 0.78f)) {
        Text(text, color = GroveInk.copy(alpha = 0.72f), fontWeight = FontWeight.Bold)
    }
}

@Composable
fun PixelProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = 16.dp,
    fill: Color = GroveMoss,
    track: Color = GroveCream
) {
    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(3.dp))
            .background(track)
            .border(2.dp, GroveInk.copy(alpha = 0.25f), RoundedCornerShape(3.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(height)
                .background(fill)
        )
    }
}

@Composable
fun QuestList(quests: List<DailyQuest>, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        quests.forEach { quest ->
            val completed = quest.completed
            val panelColor = if (completed) Color(0xFF2F6845) else GroveCream.copy(alpha = 0.92f)
            val primaryText = if (completed) Color.White else GroveInk
            val secondaryText = if (completed) Color(0xFFE8F8D8) else GroveInk.copy(alpha = 0.72f)
            val rewardText = if (completed) Color(0xFFFFE08A) else GroveClay
            PixelPanel(color = panelColor) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(quest.title, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace, color = primaryText)
                        Text("+${quest.rewardCoins}", fontWeight = FontWeight.Black, color = rewardText)
                    }
                    Text(quest.description, fontSize = 12.sp, color = secondaryText)
                    PixelProgressBar(
                        progress = quest.progress.toFloat() / quest.target.toFloat(),
                        fill = if (completed) Color(0xFFFFE08A) else GroveBerry,
                        track = if (completed) Color(0xFF224C34) else GroveCream
                    )
                }
            }
        }
    }
}

@Composable
fun AchievementRow(achievement: Achievement) {
    PixelPanel(
        color = if (achievement.unlocked) Color(0xFFFFE6A8) else Color(0xFFE0D7C7),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Canvas(Modifier.size(28.dp)) {
                drawPixelBadge(achievement.unlocked)
            }
            Column {
                Text(achievement.title, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                Text(achievement.description, fontSize = 12.sp, color = GroveInk.copy(alpha = 0.72f))
            }
        }
    }
}

@Composable
fun NavPixelIcon(screen: StudyGroveScreen, selected: Boolean, modifier: Modifier = Modifier) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    Canvas(modifier.size(24.dp)) {
        val ink = if (selected) GroveInk else onSurface.copy(alpha = 0.86f)
        val accentAlpha = if (selected) 1f else 0.78f
        val leaf = GroveLeaf.copy(alpha = accentAlpha)
        val clay = GroveClay.copy(alpha = accentAlpha)
        val moss = GroveMoss.copy(alpha = accentAlpha)
        val cream = if (selected) GroveCream else onSurface.copy(alpha = 0.68f)
        val u = size.minDimension / 8f
        when (screen) {
            StudyGroveScreen.GARDEN -> {
                drawRect(leaf, topLeft = androidx.compose.ui.geometry.Offset(2 * u, 4 * u), size = androidx.compose.ui.geometry.Size(4 * u, 3 * u))
                drawRect(ink, topLeft = androidx.compose.ui.geometry.Offset(3 * u, 1 * u), size = androidx.compose.ui.geometry.Size(2 * u, 4 * u))
            }
            StudyGroveScreen.TIMER -> {
                drawRect(ink, topLeft = androidx.compose.ui.geometry.Offset(2 * u, u), size = androidx.compose.ui.geometry.Size(4 * u, 6 * u))
                drawRect(cream, topLeft = androidx.compose.ui.geometry.Offset(3 * u, 2 * u), size = androidx.compose.ui.geometry.Size(2 * u, 3 * u))
            }
            StudyGroveScreen.SHOP -> {
                drawRect(clay, topLeft = androidx.compose.ui.geometry.Offset(2 * u, 3 * u), size = androidx.compose.ui.geometry.Size(4 * u, 4 * u))
                drawRect(ink, topLeft = androidx.compose.ui.geometry.Offset(3 * u, u), size = androidx.compose.ui.geometry.Size(2 * u, 3 * u))
            }
            StudyGroveScreen.HOUSE -> {
                drawRect(clay, topLeft = androidx.compose.ui.geometry.Offset(2 * u, 3 * u), size = androidx.compose.ui.geometry.Size(4 * u, 4 * u))
                drawRect(ink, topLeft = androidx.compose.ui.geometry.Offset(u, 2 * u), size = androidx.compose.ui.geometry.Size(6 * u, u))
            }
            StudyGroveScreen.PROFILE -> {
                drawRect(ink, topLeft = androidx.compose.ui.geometry.Offset(3 * u, u), size = androidx.compose.ui.geometry.Size(2 * u, 2 * u))
                drawRect(leaf, topLeft = androidx.compose.ui.geometry.Offset(2 * u, 4 * u), size = androidx.compose.ui.geometry.Size(4 * u, 3 * u))
            }
            StudyGroveScreen.SETTINGS -> {
                drawSettingsGear(ink = ink, accent = moss)
            }
        }
    }
}

@Composable
fun SettingsGearIcon(modifier: Modifier = Modifier, color: Color = GroveInk, accent: Color = GroveMoss) {
    Canvas(modifier.size(28.dp)) {
        drawSettingsGear(ink = color, accent = accent)
    }
}

private fun DrawScope.drawSettingsGear(ink: Color, accent: Color) {
    val u = size.minDimension / 8f
    drawRect(ink, topLeft = androidx.compose.ui.geometry.Offset(3 * u, u), size = androidx.compose.ui.geometry.Size(2 * u, u))
    drawRect(ink, topLeft = androidx.compose.ui.geometry.Offset(3 * u, 6 * u), size = androidx.compose.ui.geometry.Size(2 * u, u))
    drawRect(ink, topLeft = androidx.compose.ui.geometry.Offset(u, 3 * u), size = androidx.compose.ui.geometry.Size(u, 2 * u))
    drawRect(ink, topLeft = androidx.compose.ui.geometry.Offset(6 * u, 3 * u), size = androidx.compose.ui.geometry.Size(u, 2 * u))
    drawRect(ink, topLeft = androidx.compose.ui.geometry.Offset(2 * u, 2 * u), size = androidx.compose.ui.geometry.Size(4 * u, 4 * u))
    drawRect(accent, topLeft = androidx.compose.ui.geometry.Offset(3 * u, 3 * u), size = androidx.compose.ui.geometry.Size(2 * u, 2 * u))
    drawRect(GroveCream.copy(alpha = 0.9f), topLeft = androidx.compose.ui.geometry.Offset(3.5f * u, 3.5f * u), size = androidx.compose.ui.geometry.Size(u, u))
}

private val PixelItemPalette = listOf(
    Color(0xFFE46D6D),
    Color(0xFFF0B64E),
    Color(0xFF70B86B),
    Color(0xFF55A6A8),
    Color(0xFF5F8FCB),
    Color(0xFF8B72D6),
    Color(0xFFD66CA8),
    Color(0xFFD48952),
    Color(0xFF91A85F),
    Color(0xFF7E8AA3)
)

private fun keyedColor(key: String, offset: Int = 0): Color {
    val hash = key.hashCode() and Int.MAX_VALUE
    return PixelItemPalette[(hash + offset).mod(PixelItemPalette.size)]
}

private fun itemIdentity(itemId: String, key: String): String = itemId.ifBlank { key }

private fun itemVariant(itemId: String, key: String, modulo: Int): Int {
    if (modulo <= 1) return 0
    return ((itemIdentity(itemId, key).hashCode() and Int.MAX_VALUE) % modulo)
}

private fun itemAccent(itemId: String, key: String, offset: Int = 0): Color =
    keyedColor(itemIdentity(itemId, key), offset)

private fun String.hasAny(vararg needles: String): Boolean = needles.any { contains(it) }

private fun Color.shaded(amount: Float): Color =
    Color(
        red = (red * amount).coerceIn(0f, 1f),
        green = (green * amount).coerceIn(0f, 1f),
        blue = (blue * amount).coerceIn(0f, 1f),
        alpha = alpha
    )

private fun Color.lit(amount: Float): Color =
    Color(
        red = (red + (1f - red) * amount).coerceIn(0f, 1f),
        green = (green + (1f - green) * amount).coerceIn(0f, 1f),
        blue = (blue + (1f - blue) * amount).coerceIn(0f, 1f),
        alpha = alpha
    )

private fun plantAccent(key: String): Color = when {
    key.hasAny("moon", "snow", "pearl", "cloud", "silver") -> Color(0xFFE9F4FF)
    key.hasAny("blue", "sapphire", "lotus") -> Color(0xFF5CA7DD)
    key.hasAny("ruby", "rose", "pepper", "tomato", "ember") -> Color(0xFFE2564D)
    key.hasAny("lavender", "aurora", "orchid", "iris", "galaxy", "prism") -> Color(0xFF9B7AE5)
    key.hasAny("gold", "sun", "honey", "corn", "lantern", "star") -> Color(0xFFFFC94A)
    key.hasAny("melon", "mint", "fern", "sage", "willow") -> Color(0xFF73B86D)
    else -> keyedColor(key)
}

private fun woodLikeColor(key: String): Color = when {
    key.contains("white") -> Color(0xFFF6EBD0)
    key.contains("bamboo") -> Color(0xFFD4B95F)
    key.hasAny("iron", "clockwork") -> Color(0xFF788087)
    key.hasAny("gold", "royal") -> Color(0xFFDDAA39)
    key.hasAny("crystal", "celestial", "dream") -> Color(0xFFA7D9DD)
    key.hasAny("hedge", "ivy", "bonsai") -> Color(0xFF4E8B4E)
    key.contains("dark") -> Color(0xFF6F432B)
    key.contains("cherry") -> Color(0xFFC27678)
    else -> Color(0xFFB97948)
}

private fun pathBaseColor(key: String): Color = when {
    key.contains("dirt") -> Color(0xFF9B6942)
    key.contains("grass") || key.contains("leafy") -> Color(0xFF70A95C)
    key.contains("sand") -> Color(0xFFE2C876)
    key.contains("snow") || key.contains("cloud") -> Color(0xFFEAF2F0)
    key.contains("brick") -> Color(0xFFC5654B)
    key.contains("clay") -> Color(0xFFC28255)
    key.contains("blue") || key.contains("waterline") -> Color(0xFF5F9CC7)
    key.contains("rainbow") -> Color(0xFFD66CA8)
    key.contains("carpet") -> Color(0xFF7B5EA8)
    key.contains("star") || key.contains("gold") || key.contains("mastery") -> Color(0xFFE1B748)
    key.hasAny("moon", "marble", "diamond", "stone", "pebble", "slate", "ruin") -> Color(0xFF9EA3A1)
    key.hasAny("aurora", "planet", "celestial", "eternal") -> Color(0xFF6976A7)
    key.contains("dark_wood") -> Color(0xFF7A4D31)
    key.contains("cherry") -> Color(0xFFC27678)
    else -> Color(0xFFB97948)
}

internal fun clothingColor(key: String?): Color {
    val safeKey = key.orEmpty()
    return when {
        safeKey.contains("red") -> Color(0xFFD95757)
        safeKey.contains("yellow") -> Color(0xFFF0C85A)
        safeKey.contains("purple") || safeKey.contains("lavender") -> Color(0xFF8E72D8)
        safeKey.contains("blue") || safeKey.contains("cloud") -> Color(0xFF4B8FC9)
        safeKey.contains("green") || safeKey.contains("garden") -> GroveMoss
        safeKey.hasAny("gold", "star") -> Color(0xFFE7B742)
        safeKey.hasAny("galaxy", "celestial", "mastery") -> Color(0xFF52517C)
        safeKey.contains("robe") -> Color(0xFF7051A4)
        safeKey.contains("raincoat") -> Color(0xFFF2C84B)
        else -> GroveMoss
    }
}

@Composable
fun GardenObjectSprite(
    gardenObject: GardenObject,
    modifier: Modifier = Modifier,
    fenceMask: Int = 0,
    animationFrame: (() -> Int)? = null
) {
    val item = ShopCatalog.byId(gardenObject.shopItemId)
    PixelItemSprite(
        item = item,
        type = gardenObject.type,
        growthReady = gardenObject.sessionsRemaining == 0,
        modifier = modifier,
        growthStage = gardenObject.growthStage,
        fenceMask = fenceMask,
        animationFrame = animationFrame
    )
}

@Composable
fun InventoryItemSprite(item: ShopItem?, modifier: Modifier = Modifier) {
    PixelItemSprite(item = item, type = item?.gardenObjectType, growthReady = true, modifier = modifier)
}

@Composable
fun HouseObjectSprite(houseObject: HouseObject, modifier: Modifier = Modifier) {
    val item = ShopCatalog.byId(houseObject.shopItemId)
    PixelItemSprite(item = item, type = item?.gardenObjectType, growthReady = true, modifier = modifier)
}

private fun DrawScope.drawPixelBadge(unlocked: Boolean) {
    val u = size.minDimension / 8f
    val color = if (unlocked) Color(0xFFFFC94A) else Color(0xFF9F978B)
    drawRect(color, topLeft = androidx.compose.ui.geometry.Offset(2 * u, u), size = androidx.compose.ui.geometry.Size(4 * u, 6 * u))
    drawRect(Color.White.copy(alpha = 0.8f), topLeft = androidx.compose.ui.geometry.Offset(3 * u, 2 * u), size = androidx.compose.ui.geometry.Size(2 * u, u))
}

@Composable
private fun PixelItemSprite(
    item: ShopItem?,
    type: GardenObjectType?,
    growthReady: Boolean,
    modifier: Modifier = Modifier,
    growthStage: Int = 0,
    fenceMask: Int = 0,
    animationFrame: (() -> Int)? = null
) {
    val key = item?.visualKey.orEmpty()
    if (item?.placementTarget == PlacementTarget.HOUSE) {
        val frame = furnitureFrames[key]
        if (frame != null) {
            PixelTile(PixelSheet.Furniture, frame, modifier)
            return
        }
    }
    if (type != null) {
        val fenceRow = GardenArt.fences[key]
        if (fenceRow != null) {
            PixelTile(PixelSheet.Fences, fenceRow * 16 + fenceMask, modifier)
            return
        }
        val motionRow = GardenArt.animated[key]
        if (motionRow != null && animationFrame != null) {
            PixelTile(PixelSheet.Motion, motionRow * 8, modifier, animationFrame)
            return
        }
        val index = GardenArt.items[key]
        if (index != null) {
            if (item?.gardenObjectType == GardenObjectType.CROP && !growthReady) {
                // Each growth phase follows the same crop order as the mature item sheet.
                val phaseOffset = if (growthStage > 0) GardenArt.cropCount else 0
                PixelTile(PixelSheet.Seedlings, phaseOffset + index, modifier)
            } else {
                PixelTile(PixelSheet.Items, index, modifier)
            }
            return
        }
    }
    Canvas(modifier) {
        val u = min(size.width, size.height) / 10f
        val itemId = item?.id.orEmpty()
        when {
            type == GardenObjectType.PATH || key.contains("path") -> drawPathSprite(key, itemId, u)
            type == GardenObjectType.FENCE -> drawFenceSprite(key, itemId, u)
            type == GardenObjectType.WATER || key.contains("pond") || key.contains("water") || key.contains("fountain") -> drawWaterSprite(key, itemId, u)
            type == GardenObjectType.TREE || key.contains("tree") -> drawTreeSprite(key, itemId, u)
            type == GardenObjectType.CROP -> drawCropSprite(key, itemId, growthReady, u)
            item != null && item.placementTarget == PlacementTarget.HOUSE -> drawFurnitureSprite(key, itemId, u)
            item != null && item.placementTarget == PlacementTarget.CHARACTER -> drawCosmeticSprite(item.cosmeticSlot, key, itemId, u)
            else -> drawDecorationSprite(key, itemId, u)
        }
    }
}

private fun DrawScope.drawCosmeticSprite(slot: CosmeticSlot?, key: String, itemId: String, u: Float) {
    val identity = itemIdentity(itemId, key)
    drawRect(GroveInk.copy(alpha = 0.15f), topLeft = Offset(1.8f * u, 8.2f * u), size = Size(6.4f * u, 0.8f * u))
    when (slot) {
        CosmeticSlot.SHIRT -> {
            val shirt = clothingColor(key)
            drawRect(shirt.shaded(0.58f), topLeft = Offset(2.6f * u, 3.1f * u), size = Size(4.9f * u, 4.8f * u))
            drawRect(shirt, topLeft = Offset(2.1f * u, 2.7f * u), size = Size(5.2f * u, 4.8f * u))
            drawRect(shirt.copy(alpha = 0.88f), topLeft = Offset(1.1f * u, 3.2f * u), size = Size(1.7f * u, 1.9f * u))
            drawRect(shirt.copy(alpha = 0.75f), topLeft = Offset(7.2f * u, 3.2f * u), size = Size(1.7f * u, 1.9f * u))
            if (key.hasAny("striped", "cardigan", "robe", "uniform", "coat")) {
                val stripe = if (key.contains("striped")) Color.White.copy(alpha = 0.75f) else shirt.shaded(0.65f)
                drawRect(stripe, topLeft = Offset(2.2f * u, 4.1f * u), size = Size(5f * u, 0.55f * u))
                drawRect(stripe, topLeft = Offset(2.2f * u, 5.5f * u), size = Size(5f * u, 0.55f * u))
            } else if (key.hasAny("apron", "overalls")) {
                drawRect(Color(0xFFFFF2D1), topLeft = Offset(3.4f * u, 3.6f * u), size = Size(2.6f * u, 3.4f * u))
            } else {
                drawRect(Color.White.copy(alpha = 0.24f), topLeft = Offset(3.1f * u, 3.2f * u), size = Size(2.8f * u, 0.6f * u))
            }
            when {
                key.contains("cloud_hoodie") -> {
                    drawRect(Color.White.copy(alpha = 0.85f), topLeft = Offset(3.2f * u, 4.2f * u), size = Size(1.2f * u, 0.8f * u))
                    drawRect(Color.White.copy(alpha = 0.85f), topLeft = Offset(4.1f * u, 3.8f * u), size = Size(1.3f * u, 1.1f * u))
                    drawRect(Color.White.copy(alpha = 0.85f), topLeft = Offset(5.1f * u, 4.3f * u), size = Size(1.1f * u, 0.75f * u))
                    drawRect(shirt.shaded(0.56f), topLeft = Offset(3.4f * u, 6.1f * u), size = Size(2.6f * u, 0.6f * u))
                }
                key.contains("study_hoodie") -> {
                    drawRect(shirt.shaded(0.58f), topLeft = Offset(3.35f * u, 3.15f * u), size = Size(2.7f * u, 1.05f * u))
                    drawRect(Color(0xFFFFF2D1), topLeft = Offset(3.5f * u, 6.05f * u), size = Size(2.3f * u, 0.65f * u))
                }
                key.contains("study_grove_master_outfit") -> {
                    drawRect(Color(0xFFFFD96A), topLeft = Offset(2.35f * u, 3.25f * u), size = Size(0.55f * u, 4.2f * u))
                    drawRect(Color(0xFFFFD96A), topLeft = Offset(6.55f * u, 3.25f * u), size = Size(0.55f * u, 4.2f * u))
                    drawRect(GroveLeaf.lit(0.18f), topLeft = Offset(4.25f * u, 4.45f * u), size = Size(1.2f * u, 1.2f * u))
                }
                key.contains("library_cardigan") -> {
                    drawRect(Color(0xFFFFF2D1), topLeft = Offset(4.45f * u, 3.45f * u), size = Size(0.42f * u, 3.7f * u))
                    repeat(3) { index -> drawRect(Color(0xFFFFD96A), topLeft = Offset(4.9f * u, (4.1f + index * 0.9f) * u), size = Size(0.42f * u, 0.42f * u)) }
                }
                key.contains("scholar_uniform") -> {
                    drawRect(Color.White, topLeft = Offset(3.25f * u, 3.15f * u), size = Size(3.1f * u, 0.7f * u))
                    drawRect(GroveBerry, topLeft = Offset(4.55f * u, 3.8f * u), size = Size(0.7f * u, 2.2f * u))
                }
                key.contains("royal_scholar_coat") -> {
                    drawRect(Color(0xFFFFD96A), topLeft = Offset(3.05f * u, 3.45f * u), size = Size(0.55f * u, 3.5f * u))
                    drawRect(Color(0xFFFFD96A), topLeft = Offset(5.95f * u, 3.45f * u), size = Size(0.55f * u, 3.5f * u))
                    drawRect(GroveBerry, topLeft = Offset(4.25f * u, 4.05f * u), size = Size(1.25f * u, 0.8f * u))
                }
                key.contains("lavender_dress") -> {
                    drawRect(shirt.lit(0.22f), topLeft = Offset(2.55f * u, 5.55f * u), size = Size(4.6f * u, 1.7f * u))
                    drawRect(Color.White.copy(alpha = 0.45f), topLeft = Offset(4.55f * u, 3.45f * u), size = Size(0.55f * u, 3.6f * u))
                }
                key.contains("celestial_robe") || key.contains("mastery_robe") -> {
                    val trim = if (key.contains("mastery")) Color(0xFFFFD96A) else Color(0xFFBDE7E7)
                    drawRect(trim, topLeft = Offset(2.75f * u, 3.35f * u), size = Size(0.55f * u, 4f * u))
                    drawRect(trim, topLeft = Offset(6.1f * u, 3.35f * u), size = Size(0.55f * u, 4f * u))
                    drawRect(trim.lit(0.18f), topLeft = Offset(4.5f * u, 4.55f * u), size = Size(0.75f * u, 0.75f * u))
                }
                key.contains("purple_shirt") -> drawRect(Color(0xFFCDBDFF), topLeft = Offset(3.1f * u, 5.1f * u), size = Size(3.3f * u, 0.5f * u))
            }
            drawRect(GroveInk.copy(alpha = 0.32f), topLeft = Offset(4.2f * u, 2.7f * u), size = Size(1.2f * u, 0.8f * u))
        }
        CosmeticSlot.HAT -> {
            val hat = if (key.contains("green")) GroveLeaf else woodLikeColor(key).let { if (key.contains("red")) GroveBerry else it }
            when {
                key.contains("crown") -> {
                    val crown = if (key.contains("flower")) GroveLeaf else Color(0xFFFFD96A)
                    drawRect(crown.shaded(0.78f), topLeft = Offset(2.2f * u, 5.3f * u), size = Size(5.8f * u, 1.2f * u))
                    drawRect(crown, topLeft = Offset(1.8f * u, 4.8f * u), size = Size(6.2f * u, 1.1f * u))
                    repeat(3) { index ->
                        val spikeHeight = if (key.contains("tiny")) 1.5f else 2.7f
                        drawRect(crown, topLeft = Offset((2.1f + index * 2f) * u, (2.4f - index % 2 * 0.5f) * u), size = Size(1.1f * u, spikeHeight * u))
                    }
                    if (key.contains("flower")) {
                        repeat(3) { index ->
                            drawRect(itemAccent(itemId, key, index + 1), topLeft = Offset((2.5f + index * 1.8f) * u, 4.05f * u), size = Size(0.9f * u, 0.9f * u))
                        }
                    } else if (key.contains("star")) {
                        drawRect(Color(0xFFFFF4C0), topLeft = Offset(4.55f * u, 3.65f * u), size = Size(0.85f * u, 1.45f * u))
                        drawRect(Color(0xFFFFF4C0), topLeft = Offset(4.25f * u, 4.0f * u), size = Size(1.45f * u, 0.75f * u))
                    } else {
                        drawRect(itemAccent(itemId, key, 2), topLeft = Offset(4.55f * u, 4.9f * u), size = Size(0.8f * u, 0.65f * u))
                    }
                }
                key.hasAny("ears", "cat") -> {
                    drawRect(hat.shaded(0.72f), topLeft = Offset(2f * u, 5.1f * u), size = Size(6.5f * u, 1.1f * u))
                    drawRect(hat, topLeft = Offset(1.8f * u, 4.5f * u), size = Size(6.6f * u, 1.1f * u))
                    if (key.contains("cat")) {
                        drawRect(hat, topLeft = Offset(2.2f * u, 2.2f * u), size = Size(1.6f * u, 2.4f * u))
                        drawRect(hat, topLeft = Offset(6.2f * u, 2.2f * u), size = Size(1.6f * u, 2.4f * u))
                        drawRect(Color(0xFFE889A0), topLeft = Offset(2.7f * u, 3.0f * u), size = Size(0.7f * u, 1.2f * u))
                        drawRect(Color(0xFFE889A0), topLeft = Offset(6.6f * u, 3.0f * u), size = Size(0.7f * u, 1.2f * u))
                    } else {
                        drawRect(Color(0xFFFFF2D1), topLeft = Offset(2.3f * u, 1.7f * u), size = Size(1.2f * u, 3.4f * u))
                        drawRect(Color(0xFFFFF2D1), topLeft = Offset(6.4f * u, 1.7f * u), size = Size(1.2f * u, 3.4f * u))
                        drawRect(Color(0xFFE889A0), topLeft = Offset(2.65f * u, 2.4f * u), size = Size(0.45f * u, 1.9f * u))
                        drawRect(Color(0xFFE889A0), topLeft = Offset(6.75f * u, 2.4f * u), size = Size(0.45f * u, 1.9f * u))
                    }
                }
                key.contains("wizard") -> {
                    drawRect(hat.shaded(0.68f), topLeft = Offset(2.5f * u, 5.8f * u), size = Size(5.4f * u, 1.1f * u))
                    drawRect(hat, topLeft = Offset(1.7f * u, 5.1f * u), size = Size(6.8f * u, 1.1f * u))
                    drawRect(hat, topLeft = Offset(3.2f * u, 1.3f * u), size = Size(3.4f * u, 4.2f * u))
                    drawRect(Color(0xFFFFE08A), topLeft = Offset(4.1f * u, 3.6f * u), size = Size(1.3f * u, 0.55f * u))
                }
                else -> {
                    drawRect(hat.shaded(0.7f), topLeft = Offset(2f * u, 5.1f * u), size = Size(6.7f * u, 1.1f * u))
                    drawRect(hat, topLeft = Offset(1.4f * u, 4.4f * u), size = Size(7f * u, 1.1f * u))
                    drawRect(hat.lit(0.08f), topLeft = Offset(3f * u, 2.1f * u), size = Size(4.1f * u, 2.5f * u))
                    drawRect(Color.White.copy(alpha = 0.18f), topLeft = Offset(3.5f * u, 2.6f * u), size = Size(2.8f * u, 0.55f * u))
                    drawRect(GroveInk.copy(alpha = 0.18f), topLeft = Offset(3f * u, 4.1f * u), size = Size(4.1f * u, 0.5f * u))
                    when {
                        key.contains("straw") -> {
                            repeat(3) { index -> drawRect(Color(0xFF8D5A35).copy(alpha = 0.6f), topLeft = Offset((3.3f + index) * u, 2.3f * u), size = Size(0.25f * u, 2f * u)) }
                        }
                        key.contains("explorer") -> {
                            drawRect(Color(0xFF6B513D), topLeft = Offset(3.4f * u, 3.4f * u), size = Size(3.3f * u, 0.6f * u))
                            drawRect(Color(0xFFFFD96A), topLeft = Offset(4.55f * u, 4f * u), size = Size(0.75f * u, 0.45f * u))
                        }
                        key.contains("prism") -> {
                            repeat(3) { index -> drawRect(PixelItemPalette[(index + 4) % PixelItemPalette.size], topLeft = Offset((3.15f + index * 1.05f) * u, 2.55f * u), size = Size(0.75f * u, 1.7f * u)) }
                        }
                    }
                }
            }
        }
        CosmeticSlot.BACKPACK -> {
            val pack = clothingColor(key)
            drawRect(pack.shaded(0.58f), topLeft = Offset(3.1f * u, 2.2f * u), size = Size(4.4f * u, 6.2f * u))
            drawRect(pack, topLeft = Offset(2.5f * u, 2f * u), size = Size(4.4f * u, 6.1f * u))
            drawRect(pack.lit(0.28f), topLeft = Offset(3.1f * u, 2.6f * u), size = Size(3.1f * u, 0.7f * u))
            drawRect(pack.shaded(0.7f), topLeft = Offset(3.3f * u, 5.1f * u), size = Size(2.7f * u, 2.1f * u))
            drawRect(Color(0xFFFFDFA9), topLeft = Offset(4.2f * u, 5.7f * u), size = Size(0.9f * u, 0.5f * u))
            drawRect(pack.shaded(0.5f), topLeft = Offset(2f * u, 3f * u), size = Size(0.7f * u, 4.2f * u))
            drawRect(pack.shaded(0.5f), topLeft = Offset(6.8f * u, 3f * u), size = Size(0.7f * u, 4.2f * u))
            if (key.contains("wings")) {
                drawRect(Color(0xFFBDE7E7), topLeft = Offset(1.2f * u, 3.2f * u), size = Size(1.8f * u, 3.3f * u))
                drawRect(Color(0xFFBDE7E7), topLeft = Offset(6.9f * u, 3.2f * u), size = Size(1.8f * u, 3.3f * u))
            }
            when {
                key.contains("star_backpack") -> {
                    drawRect(Color(0xFFFFF4C0), topLeft = Offset(4.15f * u, 4.1f * u), size = Size(1.1f * u, 1.8f * u))
                    drawRect(Color(0xFFFFF4C0), topLeft = Offset(3.65f * u, 4.55f * u), size = Size(2.1f * u, 0.8f * u))
                }
                key.contains("golden_backpack") -> {
                    drawRect(Color(0xFFFFD96A), topLeft = Offset(3.05f * u, 3.15f * u), size = Size(3.2f * u, 3.8f * u))
                    drawRect(Color.White.copy(alpha = 0.5f), topLeft = Offset(3.45f * u, 3.55f * u), size = Size(1.9f * u, 0.55f * u))
                }
                key.contains("messenger") -> {
                    drawRect(pack.shaded(0.45f), topLeft = Offset(2.4f * u, 2.0f * u), size = Size(0.55f * u, 6.2f * u))
                    drawRect(Color(0xFFFFDFA9), topLeft = Offset(5.5f * u, 4.9f * u), size = Size(0.65f * u, 0.65f * u))
                }
                key.contains("firefly") -> {
                    drawRect(Color(0xFFFFF4C0), topLeft = Offset(4.05f * u, 4.3f * u), size = Size(1.25f * u, 1.25f * u))
                    drawRect(Color(0xFFFFF4C0).copy(alpha = 0.42f), topLeft = Offset(3.5f * u, 3.8f * u), size = Size(2.4f * u, 2.3f * u))
                }
                key.contains("dream") -> {
                    drawRect(Color(0xFFCDBDFF), topLeft = Offset(3.45f * u, 3.45f * u), size = Size(2.3f * u, 1.1f * u))
                    drawRect(Color(0xFFBDE7E7), topLeft = Offset(4.45f * u, 5.1f * u), size = Size(0.85f * u, 0.85f * u))
                }
                key.contains("canvas") || identity.contains("canvas") -> {
                    drawRect(Color(0xFF6B513D), topLeft = Offset(3.3f * u, 3.75f * u), size = Size(2.7f * u, 0.45f * u))
                    drawRect(Color(0xFF6B513D), topLeft = Offset(3.3f * u, 6.75f * u), size = Size(2.7f * u, 0.45f * u))
                }
            }
        }
        CosmeticSlot.HAIR -> {
            val hair = if (key.contains("aurora")) Color(0xFF7ECFE0) else plantAccent(key)
            drawRect(hair.shaded(0.72f), topLeft = Offset(2.5f * u, 4.8f * u), size = Size(5.4f * u, 2.7f * u))
            drawRect(hair, topLeft = Offset(2.2f * u, 3.3f * u), size = Size(5.6f * u, 2.7f * u))
            drawRect(hair.lit(0.28f), topLeft = Offset(3.2f * u, 2.1f * u), size = Size(3.6f * u, 1.8f * u))
            drawRect(keyedColor(key, 2), topLeft = Offset(6.6f * u, 3.3f * u), size = Size(1.1f * u, 1.1f * u))
        }
        else -> {
            val accent = clothingColor(key).let { if (it == GroveMoss) keyedColor(key) else it }
            when {
                key.hasAny("glasses", "goggles") -> {
                    val frame = when {
                        key.contains("golden") -> Color(0xFFDDAA39)
                        key.contains("clockwork") -> Color(0xFF788087)
                        key.contains("eternal") -> Color(0xFF6976A7)
                        else -> GroveInk
                    }
                    val tall = if (key.contains("goggles")) 1.5f else if (key.contains("round")) 1.25f else 1.0f
                    drawRect(frame, topLeft = Offset(2.2f * u, 3.8f * u), size = Size(2.1f * u, tall * u))
                    drawRect(frame, topLeft = Offset(5.6f * u, 3.8f * u), size = Size(2.1f * u, tall * u))
                    drawRect(Color(0xFFE7FFFF), topLeft = Offset(2.55f * u, 4.1f * u), size = Size(1.35f * u, 0.6f * u))
                    drawRect(Color(0xFFE7FFFF), topLeft = Offset(5.95f * u, 4.1f * u), size = Size(1.35f * u, 0.6f * u))
                    drawRect(frame, topLeft = Offset(4.3f * u, 4.2f * u), size = Size(1.3f * u, 0.35f * u))
                    if (key.contains("scholar")) drawRect(Color(0xFFFFD96A), topLeft = Offset(4.55f * u, 3.45f * u), size = Size(0.75f * u, 0.45f * u))
                    if (key.contains("clockwork")) drawRect(Color(0xFFFFD96A), topLeft = Offset(6.95f * u, 3.25f * u), size = Size(0.55f * u, 0.55f * u))
                    if (key.contains("eternal")) drawRect(Color(0xFFBDE7E7), topLeft = Offset(4.55f * u, 5.0f * u), size = Size(0.8f * u, 0.45f * u))
                }
                key.hasAny("boots", "shoes") -> {
                    drawRect(accent.shaded(0.65f), topLeft = Offset(2.1f * u, 5.3f * u), size = Size(2.7f * u, 2.5f * u))
                    drawRect(accent, topLeft = Offset(1.6f * u, 6.7f * u), size = Size(3.4f * u, 1.3f * u))
                    drawRect(accent.shaded(0.65f), topLeft = Offset(5.1f * u, 5.3f * u), size = Size(2.7f * u, 2.5f * u))
                    drawRect(accent, topLeft = Offset(4.9f * u, 6.7f * u), size = Size(3.4f * u, 1.3f * u))
                }
                key.hasAny("scarf", "cape", "wings") -> {
                    drawRect(accent.shaded(0.66f), topLeft = Offset(3.1f * u, 3.2f * u), size = Size(4.4f * u, 5.3f * u))
                    drawRect(accent, topLeft = Offset(2.6f * u, 2.7f * u), size = Size(4.5f * u, 1.5f * u))
                    drawRect(accent.lit(0.22f), topLeft = Offset(4.8f * u, 4.3f * u), size = Size(1.3f * u, 3.1f * u))
                }
                else -> {
                    drawRect(GroveCream, topLeft = Offset(3.1f * u, 2.2f * u), size = Size(3.8f * u, 5.9f * u))
                    drawRect(accent, topLeft = Offset(3.6f * u, 3f * u), size = Size(2.8f * u, 3.5f * u))
                    drawRect(accent.lit(0.32f), topLeft = Offset(4.05f * u, 3.5f * u), size = Size(1.8f * u, 0.7f * u))
                }
            }
        }
    }
}

private fun DrawScope.drawPathSprite(key: String, itemId: String, u: Float) {
    val color = pathBaseColor(key)
    drawRect(color.shaded(0.72f), topLeft = Offset(1.3f * u, 4.6f * u), size = Size(7.7f * u, 2.5f * u))
    drawRect(color, topLeft = Offset(u, 3.8f * u), size = Size(8 * u, 2.4f * u))
    when {
        key.hasAny("pebble", "stone", "marble", "slate", "moonstone", "diamond") -> repeat(4) { index ->
            drawRect(color.lit(0.25f), topLeft = Offset((1.5f + index * 1.8f) * u, (4.15f + index % 2 * 0.8f) * u), size = Size(1.15f * u, 0.75f * u))
        }
        key.hasAny("brick", "tile", "checker", "rune", "carpet") -> {
            repeat(3) { row ->
                drawRect(color.shaded(0.65f), topLeft = Offset(u, (4.2f + row * 0.7f) * u), size = Size(8f * u, 0.18f * u))
            }
            repeat(4) { col ->
                drawRect(color.shaded(0.65f), topLeft = Offset((1.5f + col * 1.8f) * u, 3.9f * u), size = Size(0.18f * u, 2.1f * u))
            }
        }
        key.hasAny("flower", "leaf", "firefly", "star", "planet", "aurora") -> repeat(3) { index ->
            drawRect(keyedColor(key, index + 2).lit(0.1f), topLeft = Offset((2f + index * 2.2f) * u, (4.35f + index % 2 * 0.75f) * u), size = Size(0.85f * u, 0.85f * u))
        }
        else -> {
            drawRect(color.lit(0.22f), topLeft = Offset(2.1f * u, 4.25f * u), size = Size(5.6f * u, 0.45f * u))
            drawRect(color.shaded(0.7f), topLeft = Offset(2.4f * u, 5.5f * u), size = Size(5f * u, 0.45f * u))
        }
    }
    when {
        key.contains("rainbow_brick") -> repeat(4) { index ->
            drawRect(PixelItemPalette[(index + 3) % PixelItemPalette.size], topLeft = Offset((1.3f + index * 1.8f) * u, 4.45f * u), size = Size(1.1f * u, 0.55f * u))
        }
        key.contains("brick") -> {
            drawRect(color.shaded(0.54f), topLeft = Offset(2.1f * u, 4.95f * u), size = Size(1.8f * u, 0.24f * u))
            drawRect(color.shaded(0.54f), topLeft = Offset(5.4f * u, 5.5f * u), size = Size(1.8f * u, 0.24f * u))
        }
        key.contains("checker") -> {
            drawRect(Color(0xFFFFF4C0), topLeft = Offset(1.5f * u, 4.0f * u), size = Size(1.2f * u, 0.9f * u))
            drawRect(Color(0xFFFFF4C0), topLeft = Offset(4.0f * u, 5.2f * u), size = Size(1.2f * u, 0.9f * u))
            drawRect(Color(0xFF4A3B30), topLeft = Offset(6.4f * u, 4.0f * u), size = Size(1.2f * u, 0.9f * u))
        }
        key.contains("rune") -> {
            drawRect(Color(0xFFBDE7E7), topLeft = Offset(4.45f * u, 4.1f * u), size = Size(0.55f * u, 1.9f * u))
            drawRect(Color(0xFFBDE7E7), topLeft = Offset(3.8f * u, 4.65f * u), size = Size(1.8f * u, 0.45f * u))
        }
        key.contains("pebble") -> repeat(5) { index ->
            drawRect(Color(0xFFEAF2F0), topLeft = Offset((1.45f + index * 1.35f) * u, (4.45f + index % 2 * 0.65f) * u), size = Size(0.65f * u, 0.5f * u))
        }
        key.contains("mossy") -> {
            drawRect(GroveMoss, topLeft = Offset(1.2f * u, 5.45f * u), size = Size(2.1f * u, 0.45f * u))
            drawRect(GroveLeaf, topLeft = Offset(6.1f * u, 4.25f * u), size = Size(1.8f * u, 0.4f * u))
        }
        key.contains("marble") -> {
            drawRect(Color.White.copy(alpha = 0.72f), topLeft = Offset(2.2f * u, 4.25f * u), size = Size(3.8f * u, 0.24f * u))
            drawRect(Color.White.copy(alpha = 0.72f), topLeft = Offset(5.2f * u, 4.55f * u), size = Size(0.28f * u, 1.4f * u))
        }
        key.contains("slate") -> {
            drawRect(Color(0xFF4F5C74), topLeft = Offset(1.6f * u, 4.25f * u), size = Size(6.6f * u, 0.35f * u))
            drawRect(Color(0xFF4F5C74), topLeft = Offset(1.6f * u, 5.45f * u), size = Size(6.6f * u, 0.35f * u))
        }
        key.contains("moonstone") -> {
            drawRect(Color(0xFFE9F4FF), topLeft = Offset(4.1f * u, 4.25f * u), size = Size(1.5f * u, 1.5f * u))
            drawRect(color, topLeft = Offset(4.75f * u, 4.15f * u), size = Size(0.95f * u, 1.25f * u))
        }
        key.contains("diamond") -> {
            drawRect(Color(0xFFBDE7E7), topLeft = Offset(4.35f * u, 4.05f * u), size = Size(1.15f * u, 1.9f * u))
            drawRect(Color.White.copy(alpha = 0.7f), topLeft = Offset(4.65f * u, 4.35f * u), size = Size(0.45f * u, 0.45f * u))
        }
        key.contains("snow") -> repeat(5) { index ->
            drawRect(Color.White, topLeft = Offset((1.6f + index * 1.35f) * u, (4.1f + index % 3 * 0.55f) * u), size = Size(0.45f * u, 0.45f * u))
        }
        key.contains("cloud") -> {
            drawRect(Color.White.copy(alpha = 0.84f), topLeft = Offset(2.1f * u, 4.55f * u), size = Size(1.8f * u, 0.9f * u))
            drawRect(Color.White.copy(alpha = 0.84f), topLeft = Offset(3.4f * u, 4.25f * u), size = Size(1.9f * u, 1.2f * u))
            drawRect(Color.White.copy(alpha = 0.84f), topLeft = Offset(5.0f * u, 4.65f * u), size = Size(2.1f * u, 0.8f * u))
        }
        key.contains("light_wood") -> repeat(3) { index ->
            drawRect(Color(0xFFF6E0B7), topLeft = Offset((1.5f + index * 2.1f) * u, 4f * u), size = Size(0.3f * u, 2.1f * u))
        }
        key.contains("wood") -> repeat(3) { index ->
            drawRect(color.shaded(0.55f), topLeft = Offset((2f + index * 2.0f) * u, 4.25f * u), size = Size(0.25f * u, 1.65f * u))
        }
        key.contains("lantern") -> {
            drawRect(Color(0xFFFFE08A), topLeft = Offset(2.1f * u, 4.35f * u), size = Size(0.8f * u, 0.9f * u))
            drawRect(Color(0xFFFFE08A), topLeft = Offset(7.0f * u, 4.85f * u), size = Size(0.8f * u, 0.9f * u))
        }
        key.contains("observatory") -> {
            drawRect(Color(0xFFFFF4C0), topLeft = Offset(3.1f * u, 4.45f * u), size = Size(0.6f * u, 0.6f * u))
            drawRect(Color(0xFFBDE7E7), topLeft = Offset(5.4f * u, 4.1f * u), size = Size(1.6f * u, 0.45f * u))
            drawRect(Color(0xFFBDE7E7), topLeft = Offset(6.5f * u, 4.1f * u), size = Size(0.35f * u, 1.4f * u))
        }
        key.contains("study_streak") -> repeat(3) { index ->
            drawRect(Color(0xFFFFD96A), topLeft = Offset((2.25f + index * 1.8f) * u, 4.4f * u), size = Size(0.55f * u, 1.3f * u))
        }
        key.contains("flower_edge") -> repeat(3) { index ->
            drawRect(itemAccent(itemId, key, index + 1), topLeft = Offset((2f + index * 2.2f) * u, 3.35f * u), size = Size(0.85f * u, 0.85f * u))
        }
        key.contains("firefly") -> repeat(4) { index ->
            drawRect(Color(0xFFFFF4C0), topLeft = Offset((1.8f + index * 1.55f) * u, (4.05f + index % 2 * 1.0f) * u), size = Size(0.45f * u, 0.45f * u))
        }
        key.contains("mastery") || key.contains("eternal") -> {
            drawRect(itemAccent(itemId, key, 2).lit(0.22f), topLeft = Offset(3.6f * u, 4.35f * u), size = Size(2.5f * u, 1.1f * u))
            drawRect(Color.White.copy(alpha = 0.45f), topLeft = Offset(4.35f * u, 4.65f * u), size = Size(1.0f * u, 0.45f * u))
        }
        else -> {
            val variant = itemVariant(itemId, key, 4)
            drawRect(itemAccent(itemId, key, 3).copy(alpha = 0.7f), topLeft = Offset((2.3f + variant) * u, 4.45f * u), size = Size(0.65f * u, 0.65f * u))
        }
    }
}

private fun DrawScope.drawFenceSprite(key: String, itemId: String, u: Float) {
    val rail = woodLikeColor(key)
    val side = rail.shaded(0.72f)
    drawRect(GroveInk.copy(alpha = 0.16f), topLeft = Offset(1.5f * u, 8.1f * u), size = Size(7.3f * u, 0.7f * u))
    if (key.contains("rope")) {
        drawRect(side, topLeft = Offset(2 * u, 2 * u), size = Size(1.05f * u, 6f * u))
        drawRect(side, topLeft = Offset(7 * u, 2 * u), size = Size(1.05f * u, 6f * u))
        drawRect(Color(0xFFD8B06E), topLeft = Offset(1.3f * u, 4.3f * u), size = Size(7.4f * u, 0.65f * u))
        drawRect(Color(0xFFD8B06E), topLeft = Offset(1.3f * u, 5.8f * u), size = Size(7.4f * u, 0.55f * u))
    } else {
        drawRect(side, topLeft = Offset(2 * u, 2 * u), size = Size(1.1f * u, 6 * u))
        drawRect(side, topLeft = Offset(7 * u, 2 * u), size = Size(1.1f * u, 6 * u))
        drawRect(rail, topLeft = Offset(1.7f * u, 1.6f * u), size = Size(1.7f * u, 0.9f * u))
        drawRect(rail, topLeft = Offset(6.7f * u, 1.6f * u), size = Size(1.7f * u, 0.9f * u))
        drawRect(rail, topLeft = Offset(u, 4 * u), size = Size(8 * u, u))
        drawRect(side, topLeft = Offset(u, 5f * u), size = Size(8 * u, 0.65f * u))
        if (key.hasAny("picket", "bamboo", "crystal", "celestial")) {
            repeat(3) { index ->
                drawRect(rail.lit(0.12f), topLeft = Offset((3.4f + index * 1.2f) * u, 2.5f * u), size = Size(0.5f * u, 4.8f * u))
            }
        }
    }
    when {
        key.contains("crystal") -> {
            repeat(3) { index ->
                drawRect(Color(0xFFBDE7E7), topLeft = Offset((2.7f + index * 1.6f) * u, (2.0f + index % 2 * 0.4f) * u), size = Size(0.85f * u, 2.1f * u))
                drawRect(Color.White.copy(alpha = 0.65f), topLeft = Offset((2.95f + index * 1.6f) * u, (2.3f + index % 2 * 0.4f) * u), size = Size(0.25f * u, 0.8f * u))
            }
        }
        key.contains("celestial") -> {
            drawRect(Color(0xFFFFF4C0), topLeft = Offset(3.4f * u, 2.45f * u), size = Size(0.65f * u, 0.65f * u))
            drawRect(Color(0xFFFFF4C0), topLeft = Offset(6.1f * u, 3.05f * u), size = Size(0.55f * u, 0.55f * u))
            drawRect(Color(0xFFBDE7E7), topLeft = Offset(4.55f * u, 2.65f * u), size = Size(0.95f * u, 0.55f * u))
        }
        key.contains("royal") -> {
            repeat(3) { index ->
                drawRect(Color(0xFFFFD96A), topLeft = Offset((2.5f + index * 2.0f) * u, 1.15f * u), size = Size(0.85f * u, 0.85f * u))
            }
            drawRect(GroveBerry, topLeft = Offset(4.4f * u, 3.55f * u), size = Size(1.1f * u, 0.65f * u))
        }
        key.contains("golden_rail") -> {
            drawRect(Color(0xFFFFF4C0), topLeft = Offset(1.5f * u, 4.2f * u), size = Size(7.1f * u, 0.32f * u))
            drawRect(Color(0xFF8D5A35), topLeft = Offset(3.25f * u, 2.3f * u), size = Size(0.55f * u, 5f * u))
            drawRect(Color(0xFF8D5A35), topLeft = Offset(6.1f * u, 2.3f * u), size = Size(0.55f * u, 5f * u))
        }
        key.contains("hedge") -> repeat(5) { index ->
            drawRect(GroveLeaf.lit(index * 0.03f), topLeft = Offset((1.35f + index * 1.45f) * u, (2.7f + index % 2 * 0.5f) * u), size = Size(1.25f * u, 2.2f * u))
        }
        key.contains("ivy") -> {
            repeat(4) { index ->
                drawRect(GroveLeaf, topLeft = Offset((2.2f + index * 1.3f) * u, (2.5f + index % 2 * 1.1f) * u), size = Size(0.75f * u, 0.75f * u))
                drawRect(GroveLeaf.shaded(0.72f), topLeft = Offset((2.55f + index * 1.3f) * u, 3.2f * u), size = Size(0.28f * u, 2.2f * u))
            }
        }
        key.contains("bonsai") -> repeat(2) { index ->
            drawRect(Color(0xFF7A4D31), topLeft = Offset((2.45f + index * 4.8f) * u, 1.45f * u), size = Size(0.65f * u, 1.7f * u))
            drawRect(GroveLeaf, topLeft = Offset((1.9f + index * 4.8f) * u, 1.05f * u), size = Size(1.75f * u, 1.25f * u))
        }
        key.contains("iron") -> {
            repeat(4) { index ->
                drawRect(Color(0xFF4F5C74), topLeft = Offset((2.35f + index * 1.35f) * u, 2.05f * u), size = Size(0.4f * u, 5.5f * u))
                drawRect(Color(0xFFEAF2F0), topLeft = Offset((2.25f + index * 1.35f) * u, 1.75f * u), size = Size(0.6f * u, 0.45f * u))
            }
        }
        key.contains("clockwork") -> {
            repeat(2) { index ->
                drawRect(Color(0xFFFFD96A), topLeft = Offset((3.0f + index * 2.7f) * u, 2.45f * u), size = Size(1.1f * u, 1.1f * u))
                drawRect(side, topLeft = Offset((3.3f + index * 2.7f) * u, 2.75f * u), size = Size(0.45f * u, 0.45f * u))
            }
        }
        key.contains("garden_gate") -> {
            drawRect(Color(0xFFFFD96A), topLeft = Offset(4.65f * u, 4.35f * u), size = Size(0.55f * u, 0.55f * u))
            drawRect(side, topLeft = Offset(4.8f * u, 2.0f * u), size = Size(0.35f * u, 5.7f * u))
        }
        key.contains("scholar") -> {
            drawRect(Color(0xFFFFF2D1), topLeft = Offset(4.0f * u, 2.55f * u), size = Size(1.55f * u, 1.15f * u))
            drawRect(Color(0xFF5F8FCB), topLeft = Offset(4.65f * u, 2.75f * u), size = Size(0.28f * u, 0.8f * u))
        }
        key.contains("small") -> {
            drawRect(rail.lit(0.18f), topLeft = Offset(4.1f * u, 2.1f * u), size = Size(0.75f * u, 4.9f * u))
            drawRect(rail.lit(0.18f), topLeft = Offset(5.35f * u, 2.1f * u), size = Size(0.75f * u, 4.9f * u))
        }
        else -> {
            val accent = itemAccent(itemId, key, 2)
            drawRect(accent.copy(alpha = 0.75f), topLeft = Offset(4.55f * u, 2.2f * u), size = Size(0.7f * u, 0.7f * u))
        }
    }
}

private fun DrawScope.drawWaterSprite(key: String, itemId: String, u: Float) {
    val water = if (key.contains("dream")) Color(0xFF7E78C8) else Color(0xFF5DB7C8)
    drawRect(water.shaded(0.62f), topLeft = Offset(1.8f * u, 3.2f * u), size = Size(6.8f * u, 4.6f * u))
    drawRect(water, topLeft = Offset(2 * u, 2.8f * u), size = Size(6 * u, 4 * u))
    drawRect(Color(0xFFBDE7E7), topLeft = Offset(4 * u, 4 * u), size = Size(2 * u, u))
    when {
        key.contains("fountain") -> {
            drawRect(Color(0xFFB9C2BD), topLeft = Offset(3.2f * u, 5.7f * u), size = Size(3.7f * u, 1.4f * u))
            drawRect(Color(0xFFEAF2F0), topLeft = Offset(4.5f * u, 2.1f * u), size = Size(0.9f * u, 3.6f * u))
            drawRect(Color(0xFF8ED5D6), topLeft = Offset(3.5f * u, 2.8f * u), size = Size(2.7f * u, 0.55f * u))
            if (key.contains("knowledge")) {
                drawRect(Color(0xFFFFF2D1), topLeft = Offset(3.55f * u, 5.05f * u), size = Size(2.6f * u, 0.75f * u))
                drawRect(Color(0xFF5F8FCB), topLeft = Offset(4.65f * u, 5.05f * u), size = Size(0.35f * u, 0.75f * u))
            } else if (key.contains("study")) {
                drawRect(Color(0xFFFFD96A), topLeft = Offset(4.45f * u, 1.75f * u), size = Size(1.05f * u, 0.65f * u))
            }
        }
        key.contains("waterfall") -> {
            drawRect(Color(0xFF6D7776), topLeft = Offset(2.3f * u, 1.7f * u), size = Size(5.4f * u, 2.4f * u))
            drawRect(Color(0xFFE7FFFF), topLeft = Offset(4f * u, 1.7f * u), size = Size(1.8f * u, 5.4f * u))
        }
        key.contains("koi") -> {
            drawRect(Color(0xFFFF8B5C), topLeft = Offset(3.1f * u, 4.8f * u), size = Size(1.1f * u, 0.7f * u))
            drawRect(Color(0xFFFFE08A), topLeft = Offset(6f * u, 5.1f * u), size = Size(1f * u, 0.65f * u))
        }
        else -> {
            drawRect(GroveLeaf, topLeft = Offset(2.4f * u, 6.1f * u), size = Size(1.4f * u, 0.8f * u))
            drawRect(Color(0xFFFFE08A), topLeft = Offset(2.9f * u, 5.6f * u), size = Size(0.55f * u, 0.55f * u))
        }
    }
    when {
        key.contains("bird_bath") -> {
            drawRect(Color(0xFF9EA3A1), topLeft = Offset(4.25f * u, 5.0f * u), size = Size(1.1f * u, 3f * u))
            drawRect(Color(0xFFEAF2F0), topLeft = Offset(3.1f * u, 3.1f * u), size = Size(3.4f * u, 1.2f * u))
            drawRect(Color(0xFF5DB7C8), topLeft = Offset(3.45f * u, 3.25f * u), size = Size(2.6f * u, 0.6f * u))
        }
        key.contains("watering_can") || key.contains("crop_watering") -> {
            drawRect(Color(0xFF9EA3A1), topLeft = Offset(2.7f * u, 3.6f * u), size = Size(3.4f * u, 2.0f * u))
            drawRect(Color(0xFF9EA3A1), topLeft = Offset(5.8f * u, 3.9f * u), size = Size(1.6f * u, 0.55f * u))
            drawRect(Color(0xFFBDE7E7), topLeft = Offset(6.9f * u, 4.55f * u), size = Size(0.42f * u, 0.65f * u))
        }
        key.contains("frog") -> {
            drawRect(Color(0xFF70B86B), topLeft = Offset(3.55f * u, 4.75f * u), size = Size(2.4f * u, 1.25f * u))
            drawRect(Color(0xFFEAF2F0), topLeft = Offset(3.95f * u, 4.45f * u), size = Size(0.45f * u, 0.45f * u))
            drawRect(Color(0xFFEAF2F0), topLeft = Offset(5.15f * u, 4.45f * u), size = Size(0.45f * u, 0.45f * u))
        }
        key.contains("dream") -> {
            drawRect(Color(0xFFFFF4C0), topLeft = Offset(3.65f * u, 3.75f * u), size = Size(1.45f * u, 1.45f * u))
            drawRect(water, topLeft = Offset(4.2f * u, 3.65f * u), size = Size(0.9f * u, 1.25f * u))
            drawRect(Color(0xFFCDBDFF), topLeft = Offset(6.15f * u, 5.45f * u), size = Size(0.55f * u, 0.55f * u))
        }
        key.contains("pond") && !key.contains("koi") -> {
            drawRect(GroveLeaf, topLeft = Offset(2.6f * u, 5.85f * u), size = Size(1.45f * u, 0.75f * u))
            drawRect(itemAccent(itemId, key, 2).lit(0.2f), topLeft = Offset(3.1f * u, 5.35f * u), size = Size(0.55f * u, 0.55f * u))
        }
    }
}

private fun DrawScope.drawTreeSprite(key: String, itemId: String, u: Float) {
    val leaves = when {
        key.hasAny("golden", "sun") -> Color(0xFFFFC94A)
        key.contains("rainbow") -> Color(0xFFD66CA8)
        key.hasAny("cloud", "snow") -> Color(0xFFEAF2F0)
        key.contains("aurora") -> Color(0xFF7ECFE0)
        key.hasAny("willow", "sage") -> Color(0xFF83B46A)
        else -> GroveLeaf
    }
    drawRect(GroveInk.copy(alpha = 0.16f), topLeft = Offset(2.5f * u, 8.5f * u), size = Size(5.2f * u, 0.7f * u))
    if (key.contains("berry_bush")) {
        drawRect(leaves.shaded(0.7f), topLeft = Offset(2.4f * u, 4.4f * u), size = Size(5.7f * u, 3.5f * u))
        drawRect(leaves, topLeft = Offset(1.9f * u, 3.7f * u), size = Size(5.9f * u, 3.4f * u))
        repeat(5) { index ->
            drawRect(itemAccent(itemId, key, index), topLeft = Offset((2.4f + index * 1.05f) * u, (4.3f + index % 2 * 0.9f) * u), size = Size(0.65f * u, 0.65f * u))
        }
        return
    }
    drawRect(Color(0xFF7B4E2D), topLeft = Offset(4 * u, 5 * u), size = Size(2 * u, 4 * u))
    drawRect(Color(0xFF9C6B3F), topLeft = Offset(4.2f * u, 5.2f * u), size = Size(0.55f * u, 3.2f * u))
    if (key.contains("spruce")) {
        drawRect(leaves.shaded(0.82f), topLeft = Offset(3.1f * u, 1.2f * u), size = Size(3.9f * u, 5.2f * u))
        drawRect(leaves, topLeft = Offset(2.2f * u, 3.1f * u), size = Size(5.7f * u, 1.8f * u))
        drawRect(leaves, topLeft = Offset(1.7f * u, 5f * u), size = Size(6.7f * u, 1.7f * u))
    } else if (key.contains("willow")) {
        drawRect(leaves, topLeft = Offset(2f * u, 1.2f * u), size = Size(6 * u, 4.5f * u))
        repeat(5) { index ->
            drawRect(leaves.shaded(0.75f), topLeft = Offset((2.2f + index * 1.1f) * u, 4.1f * u), size = Size(0.55f * u, 3.5f * u))
        }
    } else {
        drawRect(leaves.shaded(0.82f), topLeft = Offset(2.4f * u, 1.4f * u), size = Size(6 * u, 5 * u))
        drawRect(leaves, topLeft = Offset(2 * u, u), size = Size(6 * u, 5 * u))
        drawRect(leaves.lit(0.16f), topLeft = Offset(3.2f * u, 1.6f * u), size = Size(2.5f * u, 0.8f * u))
    }
    when {
        key.hasAny("apple", "berry", "rainbow") -> {
            val fruit = if (key.contains("rainbow")) Color(0xFF8E72D8) else GroveBerry
            drawRect(fruit, topLeft = Offset(3 * u, 3 * u), size = Size(u, u))
            drawRect(fruit.lit(0.15f), topLeft = Offset(6 * u, 2 * u), size = Size(u, u))
            if (key.contains("apple")) {
                drawRect(Color(0xFFE2564D), topLeft = Offset(4.7f * u, 4.15f * u), size = Size(0.9f * u, 0.9f * u))
                drawRect(GroveLeaf.lit(0.18f), topLeft = Offset(5.2f * u, 3.85f * u), size = Size(0.55f * u, 0.35f * u))
            }
        }
        key.hasAny("golden", "star", "aurora") -> {
            drawRect(Color(0xFFFFF4C0), topLeft = Offset(3.2f * u, 2.3f * u), size = Size(0.8f * u, 0.8f * u))
            drawRect(Color(0xFFFFF4C0), topLeft = Offset(6.2f * u, 3.3f * u), size = Size(0.65f * u, 0.65f * u))
        }
    }
    if (key.contains("golden")) {
        drawRect(Color(0xFFFFF4C0), topLeft = Offset(4.55f * u, 0.8f * u), size = Size(0.85f * u, 1.35f * u))
        drawRect(Color(0xFFFFF4C0), topLeft = Offset(4.2f * u, 1.15f * u), size = Size(1.5f * u, 0.65f * u))
    }
}

private fun DrawScope.drawCropSprite(key: String, itemId: String, growthReady: Boolean, u: Float) {
    drawRect(GroveInk.copy(alpha = 0.18f), topLeft = androidx.compose.ui.geometry.Offset(2.2f * u, 8.1f * u), size = androidx.compose.ui.geometry.Size(5.8f * u, 0.75f * u))
    drawRect(Color(0xFF654328), topLeft = androidx.compose.ui.geometry.Offset(2 * u, 7.2f * u), size = androidx.compose.ui.geometry.Size(6 * u, 1.15f * u))
    drawRect(Color(0xFF8A5B34), topLeft = androidx.compose.ui.geometry.Offset(2.4f * u, 6.8f * u), size = androidx.compose.ui.geometry.Size(5.2f * u, 0.7f * u))

    when {
        key.contains("carrot") -> {
            drawRect(GroveLeaf, topLeft = androidx.compose.ui.geometry.Offset(3.2f * u, 3.8f * u), size = androidx.compose.ui.geometry.Size(1.1f * u, 3.2f * u))
            drawRect(Color(0xFF4D8F42), topLeft = androidx.compose.ui.geometry.Offset(4.6f * u, 3.1f * u), size = androidx.compose.ui.geometry.Size(1.1f * u, 3.9f * u))
            drawRect(GroveMoss, topLeft = androidx.compose.ui.geometry.Offset(5.9f * u, 3.9f * u), size = androidx.compose.ui.geometry.Size(1.1f * u, 3.1f * u))
            if (growthReady) {
                drawRect(Color(0xFFE87935), topLeft = androidx.compose.ui.geometry.Offset(4.1f * u, 5.6f * u), size = androidx.compose.ui.geometry.Size(2.1f * u, 2.8f * u))
                drawRect(Color(0xFFFFA755), topLeft = androidx.compose.ui.geometry.Offset(4.55f * u, 5.9f * u), size = androidx.compose.ui.geometry.Size(0.55f * u, 1.8f * u))
            } else {
                drawRect(Color(0xFFE87935), topLeft = androidx.compose.ui.geometry.Offset(4.6f * u, 6.6f * u), size = androidx.compose.ui.geometry.Size(0.9f * u, 0.8f * u))
            }
        }
        key.contains("strawberry") -> {
            drawRect(GroveLeaf, topLeft = androidx.compose.ui.geometry.Offset(2.8f * u, 5.4f * u), size = androidx.compose.ui.geometry.Size(4.6f * u, 1.2f * u))
            drawRect(Color(0xFF4F9849), topLeft = androidx.compose.ui.geometry.Offset(3.4f * u, 4.4f * u), size = androidx.compose.ui.geometry.Size(1.2f * u, 2.2f * u))
            drawRect(Color(0xFF4F9849), topLeft = androidx.compose.ui.geometry.Offset(5.6f * u, 4.5f * u), size = androidx.compose.ui.geometry.Size(1.1f * u, 2.1f * u))
            drawRect(GroveBerry, topLeft = androidx.compose.ui.geometry.Offset(3.4f * u, if (growthReady) 3.2f * u else 5.3f * u), size = androidx.compose.ui.geometry.Size(1.4f * u, 1.4f * u))
            drawRect(GroveBerry, topLeft = androidx.compose.ui.geometry.Offset(5.5f * u, if (growthReady) 4.1f * u else 5.8f * u), size = androidx.compose.ui.geometry.Size(1.2f * u, 1.2f * u))
            if (growthReady) {
                drawRect(Color(0xFFFFF3B4), topLeft = androidx.compose.ui.geometry.Offset(3.8f * u, 3.5f * u), size = androidx.compose.ui.geometry.Size(0.3f * u, 0.3f * u))
                drawRect(Color(0xFFFFF3B4), topLeft = androidx.compose.ui.geometry.Offset(5.9f * u, 4.4f * u), size = androidx.compose.ui.geometry.Size(0.3f * u, 0.3f * u))
            }
        }
        key.contains("blossom") -> {
            drawRect(Color(0xFF5E9F58), topLeft = androidx.compose.ui.geometry.Offset(4.6f * u, 3.5f * u), size = androidx.compose.ui.geometry.Size(0.9f * u, 3.7f * u))
            drawRect(GroveLeaf, topLeft = androidx.compose.ui.geometry.Offset(3.2f * u, 5.3f * u), size = androidx.compose.ui.geometry.Size(1.8f * u, 1.1f * u))
            drawRect(GroveLeaf, topLeft = androidx.compose.ui.geometry.Offset(5.2f * u, 4.8f * u), size = androidx.compose.ui.geometry.Size(1.8f * u, 1.1f * u))
            val blossom = Color(0xFF8E72D8)
            val bloomY = if (growthReady) 1.7f * u else 3.2f * u
            drawRect(blossom, topLeft = androidx.compose.ui.geometry.Offset(3.8f * u, bloomY), size = androidx.compose.ui.geometry.Size(2.4f * u, 2.4f * u))
            drawRect(Color(0xFFFFE08A), topLeft = androidx.compose.ui.geometry.Offset(4.6f * u, bloomY + 0.8f * u), size = androidx.compose.ui.geometry.Size(0.8f * u, 0.8f * u))
            if (growthReady) {
                drawRect(Color(0xFFCDBDFF), topLeft = androidx.compose.ui.geometry.Offset(3.1f * u, 2.4f * u), size = androidx.compose.ui.geometry.Size(3.8f * u, 0.75f * u))
            }
        }
        else -> {
            val accent = plantAccent(key)
            drawRect(GroveLeaf, topLeft = Offset(4.6f * u, 4.2f * u), size = Size(0.9f * u, 3f * u))
            drawRect(GroveMoss, topLeft = Offset(3.4f * u, 5.4f * u), size = Size(1.7f * u, 0.9f * u))
            drawRect(GroveMoss, topLeft = Offset(5.1f * u, 5f * u), size = Size(1.7f * u, 0.9f * u))
            when {
                key.hasAny("mushroom", "cap") -> {
                    val topY = if (growthReady) 2.4f * u else 4.2f * u
                    drawRect(Color(0xFFE9E0CE), topLeft = Offset(4.35f * u, topY + 1.9f * u), size = Size(1.25f * u, 2.1f * u))
                    drawRect(accent, topLeft = Offset(3f * u, topY), size = Size(4f * u, 2.2f * u))
                    drawRect(Color.White.copy(alpha = 0.65f), topLeft = Offset(3.7f * u, topY + 0.45f * u), size = Size(0.55f * u, 0.55f * u))
                    drawRect(Color.White.copy(alpha = 0.65f), topLeft = Offset(5.5f * u, topY + 0.9f * u), size = Size(0.55f * u, 0.55f * u))
                }
                key.hasAny("wheat", "grain", "corn", "cotton") -> {
                    repeat(3) { index ->
                        val x = (3.5f + index) * u
                        drawRect(Color(0xFFD7B14A), topLeft = Offset(x, 2.6f * u), size = Size(0.45f * u, 4.4f * u))
                        if (growthReady) {
                            drawRect(accent, topLeft = Offset(x - 0.35f * u, (2.3f + index * 0.35f) * u), size = Size(1.05f * u, 1.2f * u))
                        }
                    }
                    if (key.contains("cotton") && growthReady) {
                        drawRect(Color.White, topLeft = Offset(3.2f * u, 2.1f * u), size = Size(1.2f * u, 1.2f * u))
                        drawRect(Color.White, topLeft = Offset(5.5f * u, 2.3f * u), size = Size(1.1f * u, 1.1f * u))
                    }
                }
                key.hasAny("melon", "pumpkin", "gourd", "cabbage", "onion", "radish", "root") -> {
                    val y = if (growthReady) 4.4f * u else 5.8f * u
                    drawRect(accent.shaded(0.75f), topLeft = Offset(3.1f * u, y + 0.5f * u), size = Size(4f * u, 2.7f * u))
                    drawRect(accent, topLeft = Offset(2.8f * u, y), size = Size(4f * u, 2.7f * u))
                    drawRect(accent.lit(0.25f), topLeft = Offset(4f * u, y + 0.45f * u), size = Size(0.65f * u, 1.7f * u))
                }
                key.hasAny("berry", "grape", "pepper", "tomato", "fruit", "bean", "mango") -> {
                    repeat(3) { index ->
                        val x = (3.2f + index * 1.3f) * u
                        val y = if (growthReady) (3.1f + index % 2 * 0.9f) * u else 5.5f * u
                        drawRect(accent, topLeft = Offset(x, y), size = Size(1.1f * u, 1.1f * u))
                        drawRect(accent.lit(0.28f), topLeft = Offset(x + 0.2f * u, y + 0.2f * u), size = Size(0.35f * u, 0.35f * u))
                    }
                }
                key.hasAny("flower", "daisy", "tulip", "rose", "orchid", "lily", "iris", "poppy", "lavender", "marigold", "sunflower", "dahlia", "snowdrop") -> {
                    val bloomY = if (growthReady) 2.1f * u else 3.7f * u
                    drawRect(accent, topLeft = Offset(3.1f * u, bloomY), size = Size(3.8f * u, 3.1f * u))
                    drawRect(accent.lit(0.22f), topLeft = Offset(2.7f * u, bloomY + 0.9f * u), size = Size(4.6f * u, 1.2f * u))
                    drawRect(Color(0xFFFFE08A), topLeft = Offset(4.25f * u, bloomY + 0.95f * u), size = Size(1.5f * u, 1.3f * u))
                }
                else -> {
                    if (growthReady) {
                        drawRect(accent.lit(0.1f), topLeft = Offset(3.1f * u, 2.1f * u), size = Size(3.8f * u, 3.1f * u))
                        drawRect(accent, topLeft = Offset(4.25f * u, 3f * u), size = Size(1.5f * u, 1.4f * u))
                    } else {
                        drawRect(accent, topLeft = Offset(4.25f * u, 3.35f * u), size = Size(1.5f * u, 1.3f * u))
                    }
                }
            }
        }
    }
    drawCropIdentityDetail(key = key, itemId = itemId, growthReady = growthReady, u = u)
}

private fun DrawScope.drawCropIdentityDetail(key: String, itemId: String, growthReady: Boolean, u: Float) {
    val accent = plantAccent(key)
    val bloomY = if (growthReady) 2.0f * u else 3.55f * u
    when {
        key.contains("diamond_dahlia") -> {
            drawRect(Color(0xFFBDE7E7), topLeft = Offset(4.2f * u, bloomY + 0.25f * u), size = Size(1.6f * u, 1.9f * u))
            drawRect(Color.White.copy(alpha = 0.75f), topLeft = Offset(4.65f * u, bloomY + 0.55f * u), size = Size(0.55f * u, 0.55f * u))
        }
        key == "daisy" -> {
            repeat(4) { index ->
                drawRect(Color.White, topLeft = Offset((3.4f + index % 2 * 2.1f) * u, (bloomY + index / 2 * 1.45f * u)), size = Size(1.05f * u, 1.05f * u))
            }
            drawRect(Color(0xFFFFD96A), topLeft = Offset(4.55f * u, bloomY + 0.75f * u), size = Size(0.9f * u, 0.9f * u))
        }
        key.contains("lavender") -> repeat(4) { index ->
            drawRect(Color(0xFF9B7AE5).lit(index * 0.04f), topLeft = Offset((4.05f + index % 2 * 1.1f) * u, (bloomY - 0.45f + index * 0.55f) * u), size = Size(0.65f * u, 0.65f * u))
        }
        key.contains("aurora_orchid") -> {
            drawRect(Color(0xFF7ECFE0), topLeft = Offset(2.95f * u, bloomY + 0.7f * u), size = Size(1.5f * u, 1.3f * u))
            drawRect(Color(0xFFCDBDFF), topLeft = Offset(5.55f * u, bloomY + 0.7f * u), size = Size(1.5f * u, 1.3f * u))
            drawRect(Color(0xFFFFF4C0), topLeft = Offset(4.45f * u, bloomY + 0.95f * u), size = Size(1.1f * u, 0.8f * u))
        }
        key.contains("prism_poppy") -> repeat(3) { index ->
            drawRect(PixelItemPalette[(index + 4) % PixelItemPalette.size], topLeft = Offset((3.55f + index * 0.9f) * u, bloomY + 0.4f * u), size = Size(0.75f * u, 2.0f * u))
        }
        key.contains("royal_iris") -> {
            drawRect(Color(0xFF7051A4), topLeft = Offset(3.25f * u, bloomY + 0.3f * u), size = Size(1.2f * u, 2.1f * u))
            drawRect(Color(0xFF8E72D8), topLeft = Offset(4.35f * u, bloomY - 0.15f * u), size = Size(1.3f * u, 2.6f * u))
            drawRect(Color(0xFF7051A4), topLeft = Offset(5.55f * u, bloomY + 0.3f * u), size = Size(1.2f * u, 2.1f * u))
            drawRect(Color(0xFFFFD96A), topLeft = Offset(4.55f * u, bloomY - 0.55f * u), size = Size(1.0f * u, 0.55f * u))
        }
        key.contains("ruby_rose") -> {
            drawRect(Color(0xFFD95757), topLeft = Offset(3.4f * u, bloomY + 0.35f * u), size = Size(3.2f * u, 2.2f * u))
            drawRect(Color(0xFFFFA0A0), topLeft = Offset(4.1f * u, bloomY + 0.85f * u), size = Size(1.8f * u, 0.45f * u))
            drawRect(Color(0xFF8A2934), topLeft = Offset(4.7f * u, bloomY + 1.3f * u), size = Size(0.6f * u, 0.6f * u))
        }
        key.contains("ember_marigold") -> {
            drawRect(Color(0xFFFFA755), topLeft = Offset(3.2f * u, bloomY + 0.25f * u), size = Size(3.6f * u, 2.4f * u))
            drawRect(Color(0xFFE2564D), topLeft = Offset(3.7f * u, bloomY + 0.7f * u), size = Size(2.6f * u, 1.4f * u))
            drawRect(Color(0xFFFFD96A), topLeft = Offset(4.55f * u, bloomY + 1.05f * u), size = Size(0.8f * u, 0.7f * u))
        }
        key.contains("moon_daisy") -> {
            drawRect(Color(0xFFE9F4FF), topLeft = Offset(3.45f * u, bloomY + 0.25f * u), size = Size(2.4f * u, 2.4f * u))
            drawRect(Color(0xFF8E72D8), topLeft = Offset(4.4f * u, bloomY + 0.15f * u), size = Size(1.2f * u, 2.0f * u))
            drawRect(Color(0xFFFFF4C0), topLeft = Offset(5.9f * u, bloomY + 0.5f * u), size = Size(0.55f * u, 0.55f * u))
        }
        key.contains("snowdrop") -> {
            drawRect(Color.White, topLeft = Offset(3.85f * u, bloomY + 1.05f * u), size = Size(0.95f * u, 1.7f * u))
            drawRect(Color.White, topLeft = Offset(5.15f * u, bloomY + 0.75f * u), size = Size(0.95f * u, 1.7f * u))
            drawRect(GroveLeaf, topLeft = Offset(4.8f * u, bloomY + 0.25f * u), size = Size(0.45f * u, 1.0f * u))
        }
        key.contains("silver_sunflower") -> {
            repeat(6) { index ->
                drawRect(Color(0xFFE9F4FF), topLeft = Offset((3.2f + index % 3 * 1.2f) * u, (bloomY + index / 3 * 1.2f * u)), size = Size(0.9f * u, 0.9f * u))
            }
            drawRect(Color(0xFF9EA3A1), topLeft = Offset(4.35f * u, bloomY + 0.8f * u), size = Size(1.2f * u, 1.0f * u))
        }
        key.contains("sunbean") -> {
            drawRect(Color(0xFFFFD96A), topLeft = Offset(4.0f * u, bloomY + 0.3f * u), size = Size(1.7f * u, 2.9f * u))
            drawRect(Color(0xFFFFF4C0), topLeft = Offset(4.35f * u, bloomY + 0.65f * u), size = Size(0.45f * u, 1.8f * u))
        }
        key.contains("lantern_fruit") -> {
            drawRect(Color(0xFF6B513D), topLeft = Offset(4.8f * u, bloomY - 0.2f * u), size = Size(0.35f * u, 1.0f * u))
            drawRect(Color(0xFFFFA755), topLeft = Offset(3.85f * u, bloomY + 0.6f * u), size = Size(2.3f * u, 1.9f * u))
            drawRect(Color(0xFFFFF4C0), topLeft = Offset(4.45f * u, bloomY + 0.95f * u), size = Size(1.0f * u, 0.8f * u))
        }
        key.contains("brainberry") -> {
            drawRect(Color(0xFFD66CA8), topLeft = Offset(3.3f * u, bloomY + 0.65f * u), size = Size(1.2f * u, 1.2f * u))
            drawRect(Color(0xFFD66CA8).lit(0.12f), topLeft = Offset(4.3f * u, bloomY + 0.25f * u), size = Size(1.4f * u, 1.5f * u))
            drawRect(Color(0xFFD66CA8), topLeft = Offset(5.5f * u, bloomY + 0.75f * u), size = Size(1.1f * u, 1.1f * u))
        }
        key.contains("bookberry") -> {
            drawRect(Color(0xFFFFF2D1), topLeft = Offset(3.35f * u, bloomY + 0.9f * u), size = Size(3.5f * u, 1.3f * u))
            drawRect(Color(0xFF5F8FCB), topLeft = Offset(4.8f * u, bloomY + 0.9f * u), size = Size(0.35f * u, 1.3f * u))
            drawRect(GroveBerry, topLeft = Offset(3.65f * u, bloomY + 0.35f * u), size = Size(0.75f * u, 0.75f * u))
            drawRect(GroveBerry, topLeft = Offset(5.7f * u, bloomY + 0.45f * u), size = Size(0.75f * u, 0.75f * u))
        }
        key.contains("mastery_mango") -> {
            drawRect(Color(0xFFFFC94A), topLeft = Offset(4.05f * u, bloomY + 0.45f * u), size = Size(2.2f * u, 2.5f * u))
            drawRect(Color(0xFFFFF4C0), topLeft = Offset(4.45f * u, bloomY + 0.75f * u), size = Size(0.55f * u, 1.2f * u))
        }
        key.contains("tomato") -> {
            drawRect(Color(0xFFE2564D), topLeft = Offset(3.5f * u, bloomY + 0.65f * u), size = Size(1.5f * u, 1.5f * u))
            drawRect(Color(0xFFE2564D), topLeft = Offset(5.15f * u, bloomY + 0.95f * u), size = Size(1.35f * u, 1.35f * u))
            drawRect(GroveLeaf, topLeft = Offset(4.0f * u, bloomY + 0.3f * u), size = Size(0.55f * u, 0.45f * u))
        }
        key.contains("star_pepper") -> {
            drawRect(Color(0xFFFFD96A), topLeft = Offset(4.35f * u, bloomY + 0.45f * u), size = Size(1.2f * u, 2.1f * u))
            drawRect(Color(0xFFFFD96A), topLeft = Offset(3.8f * u, bloomY + 1.0f * u), size = Size(2.3f * u, 0.85f * u))
        }
        key.contains("phoenix_pepper") -> {
            drawRect(Color(0xFFE2564D), topLeft = Offset(4.15f * u, bloomY + 0.35f * u), size = Size(1.7f * u, 2.8f * u))
            drawRect(Color(0xFFFFA755), topLeft = Offset(4.65f * u, bloomY + 0.65f * u), size = Size(0.75f * u, 1.6f * u))
        }
        key.contains("honey_wheat") -> repeat(3) { index ->
            drawRect(Color(0xFFE9BD52), topLeft = Offset((3.55f + index * 1.0f) * u, bloomY + index * 0.25f * u), size = Size(0.75f * u, 1.5f * u))
        }
        key.contains("golden_corn") -> {
            drawRect(Color(0xFFFFD96A), topLeft = Offset(4.3f * u, bloomY + 0.2f * u), size = Size(1.55f * u, 3.1f * u))
            drawRect(GroveLeaf, topLeft = Offset(3.75f * u, bloomY + 1.2f * u), size = Size(0.7f * u, 1.9f * u))
            drawRect(GroveLeaf, topLeft = Offset(5.6f * u, bloomY + 1.0f * u), size = Size(0.7f * u, 2.0f * u))
        }
        key.contains("bluebell") -> repeat(3) { index ->
            drawRect(Color(0xFF5CA7DD), topLeft = Offset((3.5f + index * 1.1f) * u, (bloomY + 0.65f + index % 2 * 0.45f) * u), size = Size(0.85f * u, 1.2f * u))
        }
        key.contains("library_lotus") -> {
            drawRect(Color(0xFFFFF2D1), topLeft = Offset(3.15f * u, bloomY + 1.85f * u), size = Size(3.8f * u, 0.75f * u))
            drawRect(Color(0xFFD66CA8), topLeft = Offset(3.65f * u, bloomY + 0.55f * u), size = Size(2.9f * u, 1.5f * u))
            drawRect(Color(0xFFFFF4C0), topLeft = Offset(4.65f * u, bloomY + 0.9f * u), size = Size(0.8f * u, 0.7f * u))
        }
        key.contains("mint_sprout") || key.contains("tea_leaf") -> {
            drawRect(GroveLeaf.lit(0.18f), topLeft = Offset(3.45f * u, bloomY + 1.0f * u), size = Size(1.65f * u, 1.05f * u))
            drawRect(GroveLeaf, topLeft = Offset(5.0f * u, bloomY + 0.7f * u), size = Size(1.65f * u, 1.05f * u))
            if (key.contains("tea")) drawRect(Color(0xFFFFF2D1), topLeft = Offset(4.1f * u, bloomY + 2.1f * u), size = Size(1.8f * u, 0.55f * u))
        }
        key.contains("focus_fern") -> repeat(4) { index ->
            drawRect(GroveLeaf.lit(index * 0.04f), topLeft = Offset((3.3f + index * 0.75f) * u, (bloomY + 0.35f + index * 0.35f) * u), size = Size(1.35f * u, 0.45f * u))
        }
        key.contains("scholar_sage") -> {
            drawRect(Color(0xFF83B46A), topLeft = Offset(3.1f * u, bloomY + 0.75f * u), size = Size(3.9f * u, 1.9f * u))
            drawRect(Color(0xFFFFF2D1), topLeft = Offset(4.0f * u, bloomY + 1.15f * u), size = Size(2.0f * u, 0.55f * u))
        }
        key.contains("wisdom_willow") -> repeat(5) { index ->
            drawRect(GroveLeaf.shaded(0.74f), topLeft = Offset((3.05f + index * 0.85f) * u, bloomY + 0.1f * u), size = Size(0.38f * u, (1.8f + index % 2 * 0.6f) * u))
        }
        key.contains("melody_mint") -> {
            drawRect(GroveLeaf, topLeft = Offset(3.45f * u, bloomY + 1.0f * u), size = Size(2.0f * u, 0.9f * u))
            drawRect(GroveLeaf.lit(0.12f), topLeft = Offset(5.0f * u, bloomY + 0.55f * u), size = Size(1.5f * u, 0.9f * u))
            drawRect(Color(0xFFFFF4C0), topLeft = Offset(6.25f * u, bloomY + 0.25f * u), size = Size(0.45f * u, 1.1f * u))
        }
        key.contains("eternal_study") -> {
            drawRect(Color(0xFF6976A7), topLeft = Offset(3.35f * u, bloomY + 0.9f * u), size = Size(3.4f * u, 1.55f * u))
            drawRect(Color(0xFFBDE7E7), topLeft = Offset(4.75f * u, bloomY + 0.9f * u), size = Size(0.35f * u, 1.55f * u))
            drawRect(Color(0xFFFFF4C0), topLeft = Offset(5.75f * u, bloomY + 0.35f * u), size = Size(0.55f * u, 0.55f * u))
        }
        key.contains("notebook_clover") -> {
            drawRect(GroveLeaf, topLeft = Offset(3.55f * u, bloomY + 0.85f * u), size = Size(1.1f * u, 1.1f * u))
            drawRect(GroveLeaf, topLeft = Offset(5.25f * u, bloomY + 0.85f * u), size = Size(1.1f * u, 1.1f * u))
            drawRect(Color.White, topLeft = Offset(4.35f * u, bloomY + 1.65f * u), size = Size(1.3f * u, 0.8f * u))
        }
        key.contains("timekeeper_thyme") -> {
            drawRect(Color(0xFFFFF2D1), topLeft = Offset(4.0f * u, bloomY + 0.55f * u), size = Size(2.1f * u, 2.1f * u))
            drawRect(GroveInk, topLeft = Offset(4.9f * u, bloomY + 0.9f * u), size = Size(0.25f * u, 0.9f * u))
            drawRect(GroveInk, topLeft = Offset(4.9f * u, bloomY + 1.55f * u), size = Size(0.7f * u, 0.25f * u))
        }
        key.contains("inkcap_mushroom") -> {
            drawRect(Color(0xFF34324A), topLeft = Offset(3.25f * u, bloomY + 0.3f * u), size = Size(3.4f * u, 1.7f * u))
            drawRect(Color(0xFF34324A).shaded(0.75f), topLeft = Offset(3.8f * u, bloomY + 1.75f * u), size = Size(0.5f * u, 1.1f * u))
            drawRect(Color(0xFF34324A).shaded(0.75f), topLeft = Offset(5.45f * u, bloomY + 1.75f * u), size = Size(0.5f * u, 0.9f * u))
        }
        key.contains("glowcap") -> {
            drawRect(Color(0xFFBDE7E7), topLeft = Offset(3.2f * u, bloomY + 0.35f * u), size = Size(3.5f * u, 1.7f * u))
            drawRect(Color(0xFFFFF4C0), topLeft = Offset(3.75f * u, bloomY + 0.75f * u), size = Size(0.55f * u, 0.55f * u))
            drawRect(Color(0xFFFFF4C0), topLeft = Offset(5.6f * u, bloomY + 1.1f * u), size = Size(0.55f * u, 0.55f * u))
        }
        else -> {
            val variant = itemVariant(itemId, key, 3)
            drawRect(accent.lit(0.2f).copy(alpha = 0.78f), topLeft = Offset((3.7f + variant * 0.8f) * u, bloomY + 0.55f * u), size = Size(0.55f * u, 0.55f * u))
        }
    }
}

private fun DrawScope.drawShelfFrame(u: Float, wood: Color) {
    drawRect(wood.shaded(0.58f), topLeft = Offset(1.9f * u, 1.5f * u), size = Size(6.6f * u, 7.4f * u))
    drawRect(wood, topLeft = Offset(1.4f * u, 1f * u), size = Size(6.6f * u, 7.2f * u))
    repeat(3) { shelf ->
        val top = (2.2f + shelf * 1.9f) * u
        drawRect(wood.shaded(0.62f), topLeft = Offset(1.8f * u, top + 1.2f * u), size = Size(5.6f * u, 0.42f * u))
    }
}

private fun DrawScope.drawFurnitureSprite(key: String, itemId: String, u: Float) {
    drawRect(GroveInk.copy(alpha = 0.16f), topLeft = androidx.compose.ui.geometry.Offset(1.5f * u, 8.3f * u), size = androidx.compose.ui.geometry.Size(7.2f * u, 0.8f * u))
    when {
        key.contains("tea_shelf") -> {
            drawShelfFrame(u, Color(0xFF9B6942))
            drawRect(Color(0xFFFFF4C0), topLeft = Offset(2.4f * u, 2.7f * u), size = Size(1.1f * u, 0.8f * u))
            drawRect(Color(0xFFFFF4C0), topLeft = Offset(4.1f * u, 2.7f * u), size = Size(1.1f * u, 0.8f * u))
            drawRect(Color(0xFFE0A269), topLeft = Offset(5.8f * u, 2.5f * u), size = Size(1.1f * u, 1f * u))
            drawRect(GroveLeaf, topLeft = Offset(3.1f * u, 5.1f * u), size = Size(0.8f * u, 1.1f * u))
            drawRect(Color(0xFF7A4D31), topLeft = Offset(5.1f * u, 5.2f * u), size = Size(1.4f * u, 0.8f * u))
        }
        key.contains("plant_shelf") -> {
            drawShelfFrame(u, Color(0xFF8D5A35))
            repeat(3) { index ->
                drawRect(GroveClay, topLeft = Offset((2.1f + index * 1.7f) * u, 5.2f * u), size = Size(1f * u, 0.9f * u))
                drawRect(GroveLeaf.lit(index * 0.08f), topLeft = Offset((2.2f + index * 1.7f) * u, (3.3f + index % 2 * 0.4f) * u), size = Size(0.8f * u, 2f * u))
            }
        }
        key.contains("trophy_shelf") -> {
            drawShelfFrame(u, Color(0xFF8D5A35))
            drawRect(Color(0xFFFFD96A), topLeft = Offset(3.2f * u, 2.4f * u), size = Size(2.1f * u, 2.1f * u))
            drawRect(Color(0xFFFFD96A), topLeft = Offset(3.75f * u, 4.3f * u), size = Size(1f * u, 1f * u))
            drawRect(Color(0xFFDDAA39), topLeft = Offset(2.6f * u, 2.9f * u), size = Size(0.7f * u, 1f * u))
            drawRect(Color(0xFFDDAA39), topLeft = Offset(5.2f * u, 2.9f * u), size = Size(0.7f * u, 1f * u))
        }
        key.contains("nightstand") -> {
            drawRect(Color(0xFF5B3525), topLeft = Offset(3f * u, 3.4f * u), size = Size(4.6f * u, 4.8f * u))
            drawRect(Color(0xFF9B6942), topLeft = Offset(2.5f * u, 3f * u), size = Size(4.6f * u, 4.8f * u))
            drawRect(Color(0xFFB97948), topLeft = Offset(2.2f * u, 2.5f * u), size = Size(5f * u, 0.9f * u))
            drawRect(Color(0xFF6B513D), topLeft = Offset(3.1f * u, 4.2f * u), size = Size(2.8f * u, 0.45f * u))
            drawRect(Color(0xFFFFE08A), topLeft = Offset(5.6f * u, 1.4f * u), size = Size(1.1f * u, 1.3f * u))
        }
        key.hasAny("drawer", "cabinet", "archive") -> {
            val wood = if (key.contains("archive")) Color(0xFF6F5A45) else Color(0xFF9B6942)
            drawRect(wood.shaded(0.6f), topLeft = Offset(2.3f * u, 2.4f * u), size = Size(5.8f * u, 6.2f * u))
            drawRect(wood, topLeft = Offset(1.8f * u, 1.9f * u), size = Size(5.8f * u, 6.2f * u))
            repeat(3) { drawer ->
                val y = (2.5f + drawer * 1.7f) * u
                drawRect(wood.lit(0.12f), topLeft = Offset(2.3f * u, y), size = Size(4.8f * u, 1.1f * u))
                drawRect(Color(0xFFFFDFA9), topLeft = Offset(4.4f * u, y + 0.38f * u), size = Size(0.75f * u, 0.3f * u))
            }
        }
        key.contains("writing_desk") || key.contains("research_desk") || key.contains("golden_desk") || key.contains("eternal_scholar_desk") -> {
            val desk = when {
                key.contains("golden") -> Color(0xFFDDAA39)
                key.contains("eternal") -> Color(0xFF6976A7)
                key.contains("research") -> Color(0xFF7A4D31)
                else -> GroveClay
            }
            drawRect(desk.shaded(0.62f), topLeft = Offset(1.6f * u, 4.8f * u), size = Size(7.2f * u, 2.8f * u))
            drawRect(desk, topLeft = Offset(1.2f * u, 3.5f * u), size = Size(7.2f * u, 2.1f * u))
            drawRect(Color(0xFFFFF0C7), topLeft = Offset(2.4f * u, 2.6f * u), size = Size(2.1f * u, 0.8f * u))
            drawRect(Color(0xFF5B3525), topLeft = Offset(2.2f * u, 6.5f * u), size = Size(0.8f * u, 2f * u))
            drawRect(Color(0xFF5B3525), topLeft = Offset(6.9f * u, 6.5f * u), size = Size(0.8f * u, 2f * u))
            if (key.contains("research") || key.contains("eternal")) {
                drawRect(Color(0xFFBDE7E7), topLeft = Offset(5.7f * u, 2.3f * u), size = Size(1.2f * u, 1.2f * u))
            }
        }
        key.contains("rug") || key.contains("mat") -> {
            val rug = clothingColor(key).let { if (it == GroveMoss) keyedColor(key) else it }
            drawRect(rug.shaded(0.62f), topLeft = Offset(1.5f * u, 4.7f * u), size = Size(7.2f * u, 3.3f * u))
            drawRect(rug, topLeft = Offset(1.1f * u, 4.1f * u), size = Size(7.2f * u, 3.3f * u))
            drawRect(rug.lit(0.28f), topLeft = Offset(2.1f * u, 5f * u), size = Size(5.2f * u, 1.4f * u))
            drawRect(GroveCream.copy(alpha = 0.58f), topLeft = Offset(1.5f * u, 4.5f * u), size = Size(0.6f * u, 2.5f * u))
            drawRect(GroveCream.copy(alpha = 0.58f), topLeft = Offset(7.1f * u, 4.5f * u), size = Size(0.6f * u, 2.5f * u))
        }
        key.contains("fireplace") -> {
            drawRect(Color(0xFF6B513D), topLeft = Offset(2.1f * u, 3.3f * u), size = Size(6.3f * u, 4.7f * u))
            drawRect(Color(0xFF9EA3A1), topLeft = Offset(1.7f * u, 2.7f * u), size = Size(6.3f * u, 4.7f * u))
            drawRect(Color(0xFF4A3B30), topLeft = Offset(3f * u, 4.25f * u), size = Size(3.9f * u, 2.6f * u))
            drawRect(Color(0xFFE2564D), topLeft = Offset(3.8f * u, 5.1f * u), size = Size(1.9f * u, 1.5f * u))
            drawRect(Color(0xFFFFD96A), topLeft = Offset(4.35f * u, 4.65f * u), size = Size(0.9f * u, 1.7f * u))
            drawRect(Color(0xFFEAF2F0), topLeft = Offset(2.4f * u, 2.95f * u), size = Size(5.0f * u, 0.45f * u))
        }
        key.contains("music_box") -> {
            val box = Color(0xFF8D5A35)
            drawRect(box.shaded(0.58f), topLeft = Offset(2.8f * u, 4.5f * u), size = Size(5.1f * u, 3.2f * u))
            drawRect(box, topLeft = Offset(2.3f * u, 3.9f * u), size = Size(5.1f * u, 3.2f * u))
            drawRect(Color(0xFFFFD96A), topLeft = Offset(3.0f * u, 4.45f * u), size = Size(3.6f * u, 0.55f * u))
            drawRect(Color(0xFFFFF4C0), topLeft = Offset(4.2f * u, 2.6f * u), size = Size(0.45f * u, 1.7f * u))
            drawRect(Color(0xFFFFF4C0), topLeft = Offset(4.2f * u, 2.6f * u), size = Size(1.2f * u, 0.45f * u))
        }
        key.contains("tea_table_indoor") -> {
            val table = Color(0xFFB97948)
            drawRect(table.shaded(0.62f), topLeft = Offset(2.3f * u, 5.3f * u), size = Size(5.8f * u, 2.3f * u))
            drawRect(table, topLeft = Offset(1.8f * u, 4.6f * u), size = Size(5.8f * u, 1.5f * u))
            drawRect(Color(0xFFFFF2D1), topLeft = Offset(3.25f * u, 3.75f * u), size = Size(1.1f * u, 0.75f * u))
            drawRect(Color(0xFFFFF2D1), topLeft = Offset(5.05f * u, 3.65f * u), size = Size(1.1f * u, 0.8f * u))
            drawRect(GroveLeaf, topLeft = Offset(4.35f * u, 4.1f * u), size = Size(0.8f * u, 0.45f * u))
        }
        key.contains("drafting_table") -> {
            val desk = Color(0xFF9B6942)
            drawRect(desk.shaded(0.62f), topLeft = Offset(1.8f * u, 5.5f * u), size = Size(7f * u, 2.2f * u))
            drawRect(desk, topLeft = Offset(1.4f * u, 4.1f * u), size = Size(7f * u, 1.7f * u))
            drawRect(Color(0xFFFFF2D1), topLeft = Offset(2.4f * u, 3.0f * u), size = Size(4.3f * u, 1.1f * u))
            drawRect(Color(0xFF5F8FCB), topLeft = Offset(3.15f * u, 3.25f * u), size = Size(2.6f * u, 0.25f * u))
            drawRect(Color(0xFF5B3525), topLeft = Offset(2.4f * u, 6.6f * u), size = Size(0.8f * u, 2f * u))
            drawRect(Color(0xFF5B3525), topLeft = Offset(7.0f * u, 6.6f * u), size = Size(0.8f * u, 2f * u))
        }
        key.contains("lamp") || key.contains("candle") -> {
            val glow = if (key.contains("crystal")) Color(0xFFBDE7E7) else Color(0xFFFFE08A)
            drawRect(glow.copy(alpha = 0.34f), topLeft = Offset(2.6f * u, 1f * u), size = Size(5.2f * u, 5f * u))
            if (key.contains("candle")) {
                repeat(3) { index ->
                    drawRect(Color(0xFFFFF2D1), topLeft = Offset((3.1f + index * 1.2f) * u, (4.1f - index % 2 * 0.4f) * u), size = Size(0.65f * u, 2.4f * u))
                    drawRect(Color(0xFFFFA755), topLeft = Offset((3.15f + index * 1.2f) * u, (3.45f - index % 2 * 0.4f) * u), size = Size(0.55f * u, 0.65f * u))
                }
                drawRect(Color(0xFF6B513D), topLeft = Offset(2.7f * u, 6.6f * u), size = Size(4.3f * u, 0.8f * u))
            } else {
                drawRect(Color(0xFF5B4B45), topLeft = Offset(4.6f * u, 4.1f * u), size = Size(0.85f * u, 4.2f * u))
                drawRect(glow, topLeft = Offset(3.2f * u, 2.1f * u), size = Size(3.7f * u, 2.3f * u))
                drawRect(glow.lit(0.3f), topLeft = Offset(3.7f * u, 2.4f * u), size = Size(2.6f * u, 0.55f * u))
                drawRect(Color(0xFF6B513D), topLeft = Offset(3.5f * u, 7.6f * u), size = Size(3f * u, 0.8f * u))
            }
        }
        key.contains("plant") -> {
            drawRect(Color(0xFF8D5634), topLeft = Offset(3.2f * u, 5.8f * u), size = Size(3.7f * u, 2.4f * u))
            drawRect(GroveClay, topLeft = Offset(2.8f * u, 5.2f * u), size = Size(3.8f * u, 2.4f * u))
            repeat(4) { index ->
                drawRect(GroveLeaf.lit(index * 0.04f), topLeft = Offset((3f + index) * u, (2.1f + index % 2 * 0.7f) * u), size = Size(1.1f * u, 3.7f * u))
            }
        }
        key.contains("clock") || key.contains("mobile") -> {
            val body = if (key.contains("mobile")) Color(0xFF6976A7) else Color(0xFF8D5A35)
            drawRect(body.shaded(0.65f), topLeft = Offset(3.1f * u, 1.6f * u), size = Size(4.4f * u, 6.8f * u))
            drawRect(body, topLeft = Offset(2.7f * u, 1.2f * u), size = Size(4.4f * u, 6.8f * u))
            drawRect(Color(0xFFFFF4C0), topLeft = Offset(3.55f * u, 2.1f * u), size = Size(2.4f * u, 2.4f * u))
            drawRect(GroveInk, topLeft = Offset(4.65f * u, 2.55f * u), size = Size(0.25f * u, 1f * u))
            drawRect(GroveInk, topLeft = Offset(4.65f * u, 3.35f * u), size = Size(0.9f * u, 0.25f * u))
            if (key.contains("mobile")) {
                drawRect(Color(0xFFFFE08A), topLeft = Offset(3.2f * u, 5.2f * u), size = Size(0.85f * u, 0.85f * u))
                drawRect(Color(0xFFBDE7E7), topLeft = Offset(5.8f * u, 5.8f * u), size = Size(0.85f * u, 0.85f * u))
            }
        }
        key.hasAny("calendar", "board", "wall", "window", "map") -> {
            val frame = if (key.contains("window")) Color(0xFF8D5A35) else Color(0xFF6B513D)
            val fill = when {
                key.contains("chalk") -> Color(0xFF314E3A)
                key.contains("window") -> Color(0xFFA9D7D8)
                key.contains("map") -> Color(0xFFEADCB8)
                else -> GroveCream
            }
            drawRect(frame.shaded(0.68f), topLeft = Offset(2.4f * u, 1.7f * u), size = Size(5.8f * u, 6.2f * u))
            drawRect(frame, topLeft = Offset(1.9f * u, 1.2f * u), size = Size(5.8f * u, 6.2f * u))
            drawRect(fill, topLeft = Offset(2.4f * u, 1.8f * u), size = Size(4.7f * u, 5f * u))
            repeat(3) { index ->
                drawRect(frame.copy(alpha = 0.45f), topLeft = Offset(3f * u, (2.6f + index * 1.2f) * u), size = Size(3.5f * u, 0.28f * u))
            }
        }
        key.hasAny("piano", "telescope", "aquarium", "globe") -> {
            val base = if (key.contains("aquarium")) Color(0xFF5F9CC7) else keyedColor(key)
            drawRect(base.shaded(0.58f), topLeft = Offset(2.2f * u, 4.1f * u), size = Size(6.2f * u, 3.8f * u))
            drawRect(base, topLeft = Offset(1.8f * u, 3.5f * u), size = Size(6.2f * u, 3.8f * u))
            drawRect(base.lit(0.3f), topLeft = Offset(2.4f * u, 4.1f * u), size = Size(5f * u, 0.8f * u))
            if (key.contains("piano")) {
                repeat(5) { index -> drawRect(Color.White, topLeft = Offset((2.4f + index * 0.75f) * u, 5.8f * u), size = Size(0.45f * u, 1.1f * u)) }
            } else if (key.contains("telescope")) {
                drawRect(Color(0xFF4F5C74), topLeft = Offset(5.7f * u, 2.1f * u), size = Size(2.8f * u, 0.9f * u))
                drawRect(Color(0xFFFFE08A), topLeft = Offset(7.8f * u, 2.1f * u), size = Size(0.65f * u, 0.9f * u))
            } else if (key.contains("aquarium")) {
                drawRect(Color(0xFFFF8B5C), topLeft = Offset(3.5f * u, 5.2f * u), size = Size(1f * u, 0.55f * u))
                drawRect(Color(0xFFFFE08A), topLeft = Offset(6.2f * u, 5.8f * u), size = Size(0.8f * u, 0.5f * u))
            }
        }
        key.contains("beanbag") -> {
            val seat = itemAccent(itemId, key)
            drawRect(seat.shaded(0.6f), topLeft = Offset(2.3f * u, 4.2f * u), size = Size(6.0f * u, 3.5f * u))
            drawRect(seat, topLeft = Offset(1.8f * u, 3.5f * u), size = Size(6.1f * u, 3.7f * u))
            drawRect(seat.lit(0.2f), topLeft = Offset(3.0f * u, 4.1f * u), size = Size(3.6f * u, 0.8f * u))
            drawRect(seat.shaded(0.72f), topLeft = Offset(5.9f * u, 6.5f * u), size = Size(1.1f * u, 0.55f * u))
        }
        key.contains("armchair") -> {
            val seat = itemAccent(itemId, key)
            drawRect(seat.shaded(0.55f), topLeft = Offset(2.3f * u, 2.7f * u), size = Size(6.1f * u, 5.1f * u))
            drawRect(seat, topLeft = Offset(2.0f * u, 3.4f * u), size = Size(5.7f * u, 3.8f * u))
            drawRect(seat.lit(0.23f), topLeft = Offset(3.0f * u, 3.7f * u), size = Size(3.5f * u, 0.8f * u))
            drawRect(seat.shaded(0.48f), topLeft = Offset(1.45f * u, 4.5f * u), size = Size(1.2f * u, 2.4f * u))
            drawRect(seat.shaded(0.48f), topLeft = Offset(7.0f * u, 4.5f * u), size = Size(1.2f * u, 2.4f * u))
        }
        key.hasAny("sofa", "seat", "pillow", "cushion", "stool", "throne") -> {
            val seat = if (key.contains("throne")) Color(0xFFDDAA39) else keyedColor(key)
            drawRect(seat.shaded(0.62f), topLeft = Offset(2.5f * u, 3f * u), size = Size(5.9f * u, 4.5f * u))
            drawRect(seat, topLeft = Offset(1.8f * u, 4.2f * u), size = Size(6.4f * u, 2.9f * u))
            drawRect(seat.lit(0.22f), topLeft = Offset(2.5f * u, 4.6f * u), size = Size(5f * u, 0.8f * u))
            drawRect(seat.shaded(0.55f), topLeft = Offset(2.6f * u, 6.8f * u), size = Size(0.8f * u, 1.5f * u))
            drawRect(seat.shaded(0.55f), topLeft = Offset(6.7f * u, 6.8f * u), size = Size(0.8f * u, 1.5f * u))
        }
        key.contains("chair") -> {
            drawRect(Color(0xFF753D55), topLeft = androidx.compose.ui.geometry.Offset(3.1f * u, 2.2f * u), size = androidx.compose.ui.geometry.Size(4.6f * u, 4.5f * u))
            drawRect(GroveBerry, topLeft = androidx.compose.ui.geometry.Offset(2.4f * u, 4.3f * u), size = androidx.compose.ui.geometry.Size(5.3f * u, 2.7f * u))
            drawRect(Color(0xFFE889A0), topLeft = androidx.compose.ui.geometry.Offset(3.1f * u, 4.7f * u), size = androidx.compose.ui.geometry.Size(3.8f * u, 0.8f * u))
            drawRect(Color(0xFF5B3340), topLeft = androidx.compose.ui.geometry.Offset(3f * u, 7f * u), size = androidx.compose.ui.geometry.Size(0.8f * u, 1.7f * u))
            drawRect(Color(0xFF5B3340), topLeft = androidx.compose.ui.geometry.Offset(6.1f * u, 7f * u), size = androidx.compose.ui.geometry.Size(0.8f * u, 1.7f * u))
        }
        key.contains("bed") -> {
            val blanket = when {
                key.contains("dream") -> Color(0xFF8E72D8)
                key.contains("scholar") -> Color(0xFF5F8FCB)
                else -> Color(0xFF7096C8)
            }
            drawRect(Color(0xFF7A4D31), topLeft = androidx.compose.ui.geometry.Offset(1.3f * u, 3.7f * u), size = androidx.compose.ui.geometry.Size(7.7f * u, 4.5f * u))
            drawRect(blanket, topLeft = androidx.compose.ui.geometry.Offset(1.8f * u, 3f * u), size = androidx.compose.ui.geometry.Size(6.6f * u, 4.2f * u))
            drawRect(blanket.shaded(0.7f), topLeft = androidx.compose.ui.geometry.Offset(1.8f * u, 6.3f * u), size = androidx.compose.ui.geometry.Size(6.6f * u, 0.9f * u))
            drawRect(Color(0xFFFFE7CF), topLeft = androidx.compose.ui.geometry.Offset(2.4f * u, 3.4f * u), size = androidx.compose.ui.geometry.Size(2.2f * u, 1.4f * u))
            drawRect(Color(0xFFE9F1FF), topLeft = androidx.compose.ui.geometry.Offset(4.9f * u, 3.5f * u), size = androidx.compose.ui.geometry.Size(2.3f * u, 1.1f * u))
            if (key.contains("scholar")) {
                drawRect(Color(0xFFFFF2D1), topLeft = Offset(3.2f * u, 5.2f * u), size = Size(3.6f * u, 0.55f * u))
                drawRect(Color(0xFFFFD96A), topLeft = Offset(6.85f * u, 3.65f * u), size = Size(0.55f * u, 0.55f * u))
            } else if (key.contains("dream")) {
                drawRect(Color(0xFFCDBDFF), topLeft = Offset(3.25f * u, 5.15f * u), size = Size(3.4f * u, 0.6f * u))
                drawRect(Color(0xFFFFF4C0), topLeft = Offset(6.45f * u, 4.9f * u), size = Size(0.55f * u, 0.55f * u))
            }
        }
        key.contains("library_ladder") -> {
            drawRect(Color(0xFF8D5A35), topLeft = Offset(3.0f * u, 1.2f * u), size = Size(0.8f * u, 7.1f * u))
            drawRect(Color(0xFF8D5A35), topLeft = Offset(6.4f * u, 1.2f * u), size = Size(0.8f * u, 7.1f * u))
            repeat(5) { rung ->
                drawRect(Color(0xFFB97948), topLeft = Offset((3.1f + rung * 0.1f) * u, (2.0f + rung * 1.1f) * u), size = Size(3.7f * u, 0.42f * u))
            }
            drawRect(Color(0xFF5B3525), topLeft = Offset(2.7f * u, 8.1f * u), size = Size(5f * u, 0.55f * u))
        }
        key.contains("shelf") || key.contains("library") -> {
            drawRect(Color(0xFF5B3525), topLeft = androidx.compose.ui.geometry.Offset(1.9f * u, 1.5f * u), size = androidx.compose.ui.geometry.Size(6.6f * u, 7.4f * u))
            drawRect(Color(0xFF8D5A35), topLeft = androidx.compose.ui.geometry.Offset(1.4f * u, 1f * u), size = androidx.compose.ui.geometry.Size(6.6f * u, 7.2f * u))
            drawRect(Color(0xFFB97948), topLeft = androidx.compose.ui.geometry.Offset(1.8f * u, 1.4f * u), size = androidx.compose.ui.geometry.Size(5.6f * u, 0.7f * u))
            repeat(3) { shelf ->
                val top = (2.5f + shelf * 1.8f) * u
                drawRect(Color(0xFF5B3525), topLeft = androidx.compose.ui.geometry.Offset(1.8f * u, top + 1.1f * u), size = androidx.compose.ui.geometry.Size(5.6f * u, 0.4f * u))
                drawRect(Color(0xFF5F8FB1), topLeft = androidx.compose.ui.geometry.Offset(2.1f * u, top), size = androidx.compose.ui.geometry.Size(0.7f * u, 1.1f * u))
                drawRect(GroveBerry, topLeft = androidx.compose.ui.geometry.Offset(3.1f * u, top + 0.1f * u), size = androidx.compose.ui.geometry.Size(0.75f * u, u))
                drawRect(GroveLeaf, topLeft = androidx.compose.ui.geometry.Offset(4.2f * u, top), size = androidx.compose.ui.geometry.Size(0.65f * u, 1.1f * u))
                drawRect(Color(0xFFFFC94A), topLeft = androidx.compose.ui.geometry.Offset(5.2f * u, top + 0.15f * u), size = androidx.compose.ui.geometry.Size(0.7f * u, 0.95f * u))
            }
        }
        else -> {
            drawRect(Color(0xFF7A4D31), topLeft = androidx.compose.ui.geometry.Offset(2f * u, 4.8f * u), size = androidx.compose.ui.geometry.Size(6.5f * u, 2.6f * u))
            drawRect(GroveClay, topLeft = androidx.compose.ui.geometry.Offset(1.5f * u, 3.4f * u), size = androidx.compose.ui.geometry.Size(6.6f * u, 2.1f * u))
            drawRect(Color(0xFFD39B63), topLeft = androidx.compose.ui.geometry.Offset(2f * u, 3.7f * u), size = androidx.compose.ui.geometry.Size(5.4f * u, 0.45f * u))
            drawRect(Color(0xFF5B3525), topLeft = androidx.compose.ui.geometry.Offset(2.3f * u, 6.6f * u), size = androidx.compose.ui.geometry.Size(0.8f * u, 2.1f * u))
            drawRect(Color(0xFF5B3525), topLeft = androidx.compose.ui.geometry.Offset(6.7f * u, 6.6f * u), size = androidx.compose.ui.geometry.Size(0.8f * u, 2.1f * u))
            drawRect(Color(0xFFFFF0C7), topLeft = androidx.compose.ui.geometry.Offset(3.2f * u, 2.6f * u), size = androidx.compose.ui.geometry.Size(2.6f * u, 0.8f * u))
        }
    }
    drawFurnitureIdentityDetail(key = key, itemId = itemId, u = u)
}

private fun DrawScope.drawFurnitureIdentityDetail(key: String, itemId: String, u: Float) {
    when {
        key == "bookshelf" -> {
            drawRect(Color(0xFFFFF2D1), topLeft = Offset(2.25f * u, 2.45f * u), size = Size(1.0f * u, 1.0f * u))
            drawRect(Color(0xFF5F8FCB), topLeft = Offset(6.15f * u, 5.95f * u), size = Size(0.8f * u, 1.0f * u))
        }
        key.contains("tall_bookshelf") -> {
            drawRect(Color(0xFF5B3525), topLeft = Offset(1.35f * u, 0.7f * u), size = Size(6.8f * u, 0.55f * u))
            drawRect(Color(0xFFFFD96A), topLeft = Offset(6.25f * u, 2.3f * u), size = Size(0.75f * u, 0.75f * u))
        }
        key.contains("grand_bookshelf") -> {
            drawRect(Color(0xFFFFD96A), topLeft = Offset(2.0f * u, 1.0f * u), size = Size(5.6f * u, 0.5f * u))
            drawRect(Color(0xFFBDE7E7), topLeft = Offset(4.45f * u, 0.45f * u), size = Size(0.85f * u, 0.85f * u))
            drawRect(Color(0xFF5B3525), topLeft = Offset(4.55f * u, 1.6f * u), size = Size(0.45f * u, 6.3f * u))
        }
        key.contains("library_wall") -> {
            val trim = if (key.contains("master")) Color(0xFFFFD96A) else Color(0xFF5F8FCB)
            repeat(4) { index ->
                drawRect(trim, topLeft = Offset((2.5f + index * 1.1f) * u, 2.3f * u), size = Size(0.65f * u, 3.8f * u))
            }
            if (key.contains("master")) drawRect(Color(0xFFFFF4C0), topLeft = Offset(4.45f * u, 1.1f * u), size = Size(0.9f * u, 0.9f * u))
        }
        key.contains("wall_calendar") -> {
            drawRect(Color(0xFFD95757), topLeft = Offset(2.4f * u, 1.8f * u), size = Size(4.7f * u, 0.75f * u))
            repeat(3) { row ->
                repeat(3) { col ->
                    drawRect(Color(0xFF6B513D).copy(alpha = 0.55f), topLeft = Offset((2.8f + col * 1.1f) * u, (3.0f + row * 0.8f) * u), size = Size(0.5f * u, 0.35f * u))
                }
            }
        }
        key.contains("cork_board") -> {
            drawRect(Color(0xFFFFE08A), topLeft = Offset(3.0f * u, 2.4f * u), size = Size(1.0f * u, 0.9f * u))
            drawRect(Color(0xFF5F8FCB), topLeft = Offset(5.4f * u, 3.8f * u), size = Size(1.0f * u, 0.9f * u))
            drawRect(GroveBerry, topLeft = Offset(4.55f * u, 2.75f * u), size = Size(0.4f * u, 0.4f * u))
        }
        key.contains("window_curtains") -> {
            drawRect(GroveBerry, topLeft = Offset(2.2f * u, 1.55f * u), size = Size(0.8f * u, 5.8f * u))
            drawRect(GroveBerry, topLeft = Offset(6.55f * u, 1.55f * u), size = Size(0.8f * u, 5.8f * u))
            drawRect(Color(0xFFFFD96A), topLeft = Offset(2.8f * u, 3.9f * u), size = Size(3.9f * u, 0.35f * u))
        }
        key.contains("window_seat") -> {
            drawRect(itemAccent(itemId, key), topLeft = Offset(2.15f * u, 6.0f * u), size = Size(5.6f * u, 1.2f * u))
            drawRect(Color(0xFFFFF2D1), topLeft = Offset(3.1f * u, 5.3f * u), size = Size(1.2f * u, 0.8f * u))
            drawRect(Color(0xFFFFF2D1), topLeft = Offset(5.4f * u, 5.3f * u), size = Size(1.2f * u, 0.8f * u))
        }
        key.contains("observatory_window") -> {
            drawRect(Color(0xFF34324A), topLeft = Offset(2.6f * u, 2.0f * u), size = Size(4.3f * u, 4.5f * u))
            drawRect(Color(0xFFFFF4C0), topLeft = Offset(3.35f * u, 2.65f * u), size = Size(0.55f * u, 0.55f * u))
            drawRect(Color(0xFFFFF4C0), topLeft = Offset(5.75f * u, 4.1f * u), size = Size(0.45f * u, 0.45f * u))
            drawRect(Color(0xFFBDE7E7), topLeft = Offset(4.55f * u, 3.35f * u), size = Size(0.75f * u, 0.75f * u))
        }
        key.contains("focus_clock") -> {
            drawRect(Color(0xFFD95757), topLeft = Offset(4.65f * u, 2.55f * u), size = Size(0.25f * u, 1.05f * u))
            drawRect(Color(0xFFD95757), topLeft = Offset(4.65f * u, 3.35f * u), size = Size(0.95f * u, 0.25f * u))
        }
        key.contains("grandfather_clock") -> {
            drawRect(Color(0xFFFFD96A), topLeft = Offset(4.35f * u, 5.25f * u), size = Size(1.0f * u, 1.55f * u))
            drawRect(Color(0xFF5B3525), topLeft = Offset(3.25f * u, 7.25f * u), size = Size(3.3f * u, 0.55f * u))
        }
        key == "chair" -> {
            drawRect(Color(0xFFFFF2D1), topLeft = Offset(3.45f * u, 4.25f * u), size = Size(2.3f * u, 1.1f * u))
        }
        key == "desk" -> {
            drawRect(Color(0xFFFFF2D1), topLeft = Offset(3.25f * u, 3.0f * u), size = Size(1.9f * u, 0.65f * u))
            drawRect(Color(0xFF5F8FCB), topLeft = Offset(5.2f * u, 3.05f * u), size = Size(0.65f * u, 0.55f * u))
        }
        key.contains("golden_desk") -> drawRect(Color(0xFFFFF4C0), topLeft = Offset(5.8f * u, 2.55f * u), size = Size(0.8f * u, 0.8f * u))
        key.contains("nightstand") -> drawRect(Color(0xFF5F8FCB), topLeft = Offset(3.1f * u, 5.35f * u), size = Size(2.8f * u, 0.45f * u))
        key.contains("drawer") || key.contains("archive") -> {
            drawRect(Color(0xFF5F8FCB), topLeft = Offset(2.7f * u, 2.85f * u), size = Size(0.8f * u, 0.35f * u))
            drawRect(Color(0xFFD95757), topLeft = Offset(5.8f * u, 6.2f * u), size = Size(0.8f * u, 0.35f * u))
        }
        else -> {
            if (itemVariant(itemId, key, 5) == 0) {
                drawRect(itemAccent(itemId, key, 3).copy(alpha = 0.7f), topLeft = Offset(7.15f * u, 2.3f * u), size = Size(0.55f * u, 0.55f * u))
            }
        }
    }
}

private fun DrawScope.drawDecorationSprite(key: String, itemId: String, u: Float) {
    drawRect(GroveInk.copy(alpha = 0.16f), topLeft = androidx.compose.ui.geometry.Offset(1.7f * u, 8.2f * u), size = androidx.compose.ui.geometry.Size(6.6f * u, 0.75f * u))
    when {
        key.hasAny("bath", "fountain", "waterfall") -> {
            drawWaterSprite(key, itemId, u)
        }
        key.hasAny("sign", "mailbox", "chalkboard", "kiosk", "cart") -> {
            val post = Color(0xFF8D5A35)
            drawRect(post.shaded(0.72f), topLeft = Offset(4.7f * u, 4.5f * u), size = Size(0.9f * u, 3.8f * u))
            drawRect(post, topLeft = Offset(4.25f * u, 4.2f * u), size = Size(0.9f * u, 3.8f * u))
            val panel = when {
                key.contains("mailbox") -> Color(0xFF5F8FB1)
                key.contains("chalkboard") -> Color(0xFF314E3A)
                key.contains("kiosk") || key.contains("cart") -> Color(0xFFD48952)
                else -> GroveCream
            }
            drawRect(panel.shaded(0.7f), topLeft = Offset(2.6f * u, 2.2f * u), size = Size(5.2f * u, 2.8f * u))
            drawRect(panel, topLeft = Offset(2.1f * u, 1.8f * u), size = Size(5.2f * u, 2.8f * u))
            drawRect(panel.lit(0.22f), topLeft = Offset(2.7f * u, 2.4f * u), size = Size(3.7f * u, 0.55f * u))
            if (key.contains("mailbox")) {
                drawRect(Color(0xFFFFE08A), topLeft = Offset(6.4f * u, 2.4f * u), size = Size(0.7f * u, 0.7f * u))
            }
        }
        key.hasAny("bridge", "arch", "gate", "moon_gate") -> {
            val wood = woodLikeColor(key)
            drawRect(wood.shaded(0.65f), topLeft = Offset(1.2f * u, 5.8f * u), size = Size(7.5f * u, 2.1f * u))
            drawRect(wood, topLeft = Offset(0.9f * u, 5f * u), size = Size(7.5f * u, 1.7f * u))
            drawRect(wood.lit(0.14f), topLeft = Offset(1.4f * u, 5.3f * u), size = Size(6.4f * u, 0.35f * u))
            if (key.contains("arch") || key.contains("gate")) {
                drawRect(wood.shaded(0.7f), topLeft = Offset(2.1f * u, 2.1f * u), size = Size(0.9f * u, 4.5f * u))
                drawRect(wood.shaded(0.7f), topLeft = Offset(7f * u, 2.1f * u), size = Size(0.9f * u, 4.5f * u))
                drawRect(wood, topLeft = Offset(2.1f * u, 1.5f * u), size = Size(5.8f * u, 0.9f * u))
            }
        }
        key.hasAny("statue", "obelisk", "monument", "shrine", "portal", "sundial") -> {
            val stone = when {
                key.hasAny("gold", "master") -> Color(0xFFDDAA39)
                key.hasAny("celestial", "dream", "aurora") -> Color(0xFF8B72D6)
                else -> Color(0xFF9EA3A1)
            }
            drawRect(stone.shaded(0.62f), topLeft = Offset(2.7f * u, 6.8f * u), size = Size(4.9f * u, 1.4f * u))
            drawRect(stone, topLeft = Offset(2.2f * u, 6.2f * u), size = Size(4.9f * u, 1.4f * u))
            drawRect(stone.shaded(0.78f), topLeft = Offset(3.4f * u, 2f * u), size = Size(2.9f * u, 4.4f * u))
            drawRect(stone.lit(0.18f), topLeft = Offset(3.8f * u, 2.4f * u), size = Size(1.2f * u, 2.9f * u))
            if (key.contains("portal")) {
                drawRect(Color(0xFFBDE7E7), topLeft = Offset(3.2f * u, 2.1f * u), size = Size(3.3f * u, 3.5f * u))
                drawRect(Color(0xFF6976A7), topLeft = Offset(4f * u, 2.9f * u), size = Size(1.7f * u, 2f * u))
            }
        }
        key.hasAny("beehive", "butterfly", "bird", "owl", "frog") -> {
            val accent = when {
                key.contains("beehive") -> Color(0xFFDDAA39)
                key.contains("butterfly") -> Color(0xFFD66CA8)
                key.contains("frog") -> Color(0xFF70B86B)
                else -> Color(0xFF8D5A35)
            }
            drawRect(Color(0xFF7B4E2D), topLeft = Offset(4.4f * u, 5.1f * u), size = Size(1f * u, 3f * u))
            drawRect(accent.shaded(0.68f), topLeft = Offset(3f * u, 2.5f * u), size = Size(4.2f * u, 3.8f * u))
            drawRect(accent, topLeft = Offset(2.6f * u, 2.1f * u), size = Size(4.2f * u, 3.8f * u))
            drawRect(accent.lit(0.26f), topLeft = Offset(3.3f * u, 2.7f * u), size = Size(2.7f * u, 0.65f * u))
            if (key.contains("butterfly")) {
                drawRect(Color(0xFF8B72D6), topLeft = Offset(2.2f * u, 3.3f * u), size = Size(1.4f * u, 1.2f * u))
                drawRect(Color(0xFF8B72D6), topLeft = Offset(6.1f * u, 3.3f * u), size = Size(1.4f * u, 1.2f * u))
            }
        }
        key.hasAny("blanket", "hammock", "tea_table", "compost", "windmill", "clock", "jar", "chime", "cluster", "lantern") -> {
            val accent = keyedColor(key)
            if (key.contains("blanket")) {
                drawRect(accent.shaded(0.65f), topLeft = Offset(1.6f * u, 4.6f * u), size = Size(7f * u, 3.1f * u))
                drawRect(accent, topLeft = Offset(1.2f * u, 4f * u), size = Size(7f * u, 3.1f * u))
                drawRect(Color.White.copy(alpha = 0.45f), topLeft = Offset(2f * u, 4f * u), size = Size(0.55f * u, 3.1f * u))
                drawRect(Color.White.copy(alpha = 0.45f), topLeft = Offset(4.9f * u, 4f * u), size = Size(0.55f * u, 3.1f * u))
            } else if (key.contains("windmill")) {
                drawRect(Color(0xFF8D5A35), topLeft = Offset(4.5f * u, 4.1f * u), size = Size(1f * u, 4f * u))
                drawRect(GroveCream, topLeft = Offset(3.3f * u, 3.1f * u), size = Size(3.5f * u, 3.1f * u))
                drawRect(accent, topLeft = Offset(4.6f * u, 1.5f * u), size = Size(0.7f * u, 5f * u))
                drawRect(accent, topLeft = Offset(2.4f * u, 3.5f * u), size = Size(5f * u, 0.7f * u))
            } else {
                drawRect(accent.shaded(0.62f), topLeft = Offset(3f * u, 4.3f * u), size = Size(4.3f * u, 3.5f * u))
                drawRect(accent, topLeft = Offset(2.5f * u, 3.7f * u), size = Size(4.3f * u, 3.5f * u))
                drawRect(accent.lit(0.25f), topLeft = Offset(3.2f * u, 4.2f * u), size = Size(2.8f * u, 0.65f * u))
            }
        }
        key.contains("lamp") -> {
            drawRect(Color(0xFFFFE08A).copy(alpha = 0.35f), topLeft = androidx.compose.ui.geometry.Offset(2.2f * u, 0.8f * u), size = androidx.compose.ui.geometry.Size(5.6f * u, 4.4f * u))
            drawRect(Color(0xFF4A3B30), topLeft = androidx.compose.ui.geometry.Offset(4.55f * u, 3.4f * u), size = androidx.compose.ui.geometry.Size(0.95f * u, 4.6f * u))
            drawRect(Color(0xFF6B513D), topLeft = androidx.compose.ui.geometry.Offset(4.15f * u, 3.2f * u), size = androidx.compose.ui.geometry.Size(0.95f * u, 4.6f * u))
            drawRect(Color(0xFFFFE46B), topLeft = androidx.compose.ui.geometry.Offset(3 * u, 1.1f * u), size = androidx.compose.ui.geometry.Size(4 * u, 2.6f * u))
            drawRect(Color(0xFFFFF4C0), topLeft = androidx.compose.ui.geometry.Offset(3.4f * u, 1.4f * u), size = androidx.compose.ui.geometry.Size(3.2f * u, 0.7f * u))
            drawRect(Color(0xFF7A4D31), topLeft = androidx.compose.ui.geometry.Offset(3.5f * u, 7.5f * u), size = androidx.compose.ui.geometry.Size(2.3f * u, 0.7f * u))
        }
        key.contains("bench") -> {
            drawRect(Color(0xFF5B3525), topLeft = androidx.compose.ui.geometry.Offset(2.3f * u, 3.2f * u), size = androidx.compose.ui.geometry.Size(6.2f * u, 1.1f * u))
            drawRect(Color(0xFF8D5A35), topLeft = androidx.compose.ui.geometry.Offset(1.7f * u, 2.8f * u), size = androidx.compose.ui.geometry.Size(6.3f * u, 1.1f * u))
            drawRect(Color(0xFFB97948), topLeft = androidx.compose.ui.geometry.Offset(1.4f * u, 4.8f * u), size = androidx.compose.ui.geometry.Size(6.8f * u, 1.5f * u))
            drawRect(Color(0xFF6B513D), topLeft = androidx.compose.ui.geometry.Offset(1.4f * u, 6.1f * u), size = androidx.compose.ui.geometry.Size(6.8f * u, 0.55f * u))
            drawRect(Color(0xFF5B3525), topLeft = androidx.compose.ui.geometry.Offset(2.5f * u, 6.4f * u), size = androidx.compose.ui.geometry.Size(0.8f * u, 2f * u))
            drawRect(Color(0xFF5B3525), topLeft = androidx.compose.ui.geometry.Offset(6.2f * u, 6.4f * u), size = androidx.compose.ui.geometry.Size(0.8f * u, 2f * u))
        }
        key.contains("greenhouse") -> {
            drawRect(Color(0xFF6C8F89), topLeft = androidx.compose.ui.geometry.Offset(1.6f * u, 3.3f * u), size = androidx.compose.ui.geometry.Size(7.1f * u, 4.7f * u))
            drawRect(Color(0xFFBDE7E7), topLeft = androidx.compose.ui.geometry.Offset(1.2f * u, 3f * u), size = androidx.compose.ui.geometry.Size(7.1f * u, 4.7f * u))
            drawRect(Color(0xFFE7FFFF).copy(alpha = 0.8f), topLeft = androidx.compose.ui.geometry.Offset(2.1f * u, 3.5f * u), size = androidx.compose.ui.geometry.Size(1.1f * u, 3.3f * u))
            drawRect(Color(0xFF6C8F89), topLeft = androidx.compose.ui.geometry.Offset(4.6f * u, 3f * u), size = androidx.compose.ui.geometry.Size(0.45f * u, 4.7f * u))
            drawRect(GroveMoss, topLeft = androidx.compose.ui.geometry.Offset(3.8f * u, 5.5f * u), size = androidx.compose.ui.geometry.Size(2.5f * u, 2.2f * u))
            drawRect(Color(0xFF8ED5D6), topLeft = androidx.compose.ui.geometry.Offset(0.8f * u, 2.6f * u), size = androidx.compose.ui.geometry.Size(7.9f * u, 0.7f * u))
        }
        key.contains("observatory") -> {
            drawRect(Color(0xFF68758D), topLeft = androidx.compose.ui.geometry.Offset(2f * u, 4.4f * u), size = androidx.compose.ui.geometry.Size(6.2f * u, 3.8f * u))
            drawRect(Color(0xFFA9D7D8), topLeft = androidx.compose.ui.geometry.Offset(2.2f * u, 2.3f * u), size = androidx.compose.ui.geometry.Size(5.4f * u, 3.2f * u))
            drawRect(Color(0xFF4F5C74), topLeft = androidx.compose.ui.geometry.Offset(2.1f * u, 5.2f * u), size = androidx.compose.ui.geometry.Size(5.7f * u, 0.7f * u))
            drawRect(Color(0xFFFFE08A), topLeft = androidx.compose.ui.geometry.Offset(4.6f * u, 3.5f * u), size = androidx.compose.ui.geometry.Size(0.7f * u, 0.7f * u))
            drawRect(Color(0xFFFFF4C0), topLeft = androidx.compose.ui.geometry.Offset(5.8f * u, 2.8f * u), size = androidx.compose.ui.geometry.Size(0.45f * u, 0.45f * u))
        }
        key.contains("garden_expand") -> {
            val tier = when {
                itemId.contains("_30") -> 7
                itemId.contains("_24") -> 6
                itemId.contains("_20") -> 5
                itemId.contains("_18") -> 4
                itemId.contains("_15") -> 3
                itemId.contains("_12") -> 2
                else -> 1
            }
            drawRect(GroveLeaf, topLeft = androidx.compose.ui.geometry.Offset(2f * u, 2f * u), size = androidx.compose.ui.geometry.Size(5.2f * u, 5.2f * u))
            drawRect(GroveMoss, topLeft = androidx.compose.ui.geometry.Offset(2.7f * u, 2.7f * u), size = androidx.compose.ui.geometry.Size(3.8f * u, 3.8f * u))
            drawRect(Color(0xFFFFE08A), topLeft = androidx.compose.ui.geometry.Offset(6.4f * u, 5.5f * u), size = androidx.compose.ui.geometry.Size(1.8f * u, 0.7f * u))
            drawRect(Color(0xFFFFE08A), topLeft = androidx.compose.ui.geometry.Offset(6.95f * u, 4.95f * u), size = androidx.compose.ui.geometry.Size(0.7f * u, 1.8f * u))
            repeat(tier.coerceAtMost(7)) { index ->
                drawRect(Color(0xFFFFF4C0), topLeft = Offset((2.25f + index % 4 * 0.9f) * u, (2.25f + index / 4 * 0.9f) * u), size = Size(0.45f * u, 0.45f * u))
            }
        }
        key.contains("house_upgrade") -> {
            val tier = when {
                itemId.contains("_7") -> 6
                itemId.contains("_6") -> 5
                itemId.contains("_5") -> 4
                itemId.contains("_4") -> 3
                itemId.contains("_3") -> 2
                else -> 1
            }
            drawRect(Color(0xFF8D4C3B), topLeft = androidx.compose.ui.geometry.Offset(1.5f * u, 3f * u), size = androidx.compose.ui.geometry.Size(7f * u, 1.2f * u))
            drawRect(GroveClay, topLeft = androidx.compose.ui.geometry.Offset(2.2f * u, 4.1f * u), size = androidx.compose.ui.geometry.Size(5.6f * u, 4.2f * u))
            drawRect(Color(0xFFFFE08A), topLeft = androidx.compose.ui.geometry.Offset(6.1f * u, 5.1f * u), size = androidx.compose.ui.geometry.Size(1.1f * u, 1.1f * u))
            drawRect(GroveInk.copy(alpha = 0.3f), topLeft = androidx.compose.ui.geometry.Offset(3.2f * u, 5.6f * u), size = androidx.compose.ui.geometry.Size(1.3f * u, 2.7f * u))
            if (tier >= 3) drawRect(Color(0xFF8D4C3B), topLeft = Offset(5.2f * u, (3.0f - (tier - 2) * 0.25f) * u), size = Size(1.0f * u, (1.0f + tier * 0.22f) * u))
            repeat(tier.coerceAtMost(6)) { index ->
                drawRect(Color(0xFFFFF4C0), topLeft = Offset((2.6f + index * 0.75f) * u, 4.45f * u), size = Size(0.42f * u, 0.42f * u))
            }
        }
        else -> {
            val base = keyedColor(key)
            drawRect(base.shaded(0.58f), topLeft = Offset(3.1f * u, 5.1f * u), size = Size(4.1f * u, 2.4f * u))
            drawRect(base, topLeft = Offset(2.8f * u, 4.6f * u), size = Size(4.1f * u, 2.4f * u))
            repeat(3) { index ->
                val flower = keyedColor(key, index + 2)
                drawRect(flower, topLeft = Offset((3.1f + index * 1.3f) * u, (2.3f + index % 2 * 0.55f) * u), size = Size(1.2f * u, 1.2f * u))
            }
            drawRect(GroveLeaf, topLeft = Offset(4.4f * u, 3.7f * u), size = Size(0.9f * u, 1.2f * u))
        }
    }
    drawDecorationIdentityDetail(key = key, itemId = itemId, u = u)
}

private fun DrawScope.drawDecorationIdentityDetail(key: String, itemId: String, u: Float) {
    when {
        key.contains("golden_beehive") -> {
            drawRect(Color(0xFFFFD96A), topLeft = Offset(2.7f * u, 2.35f * u), size = Size(4.2f * u, 3.5f * u))
            repeat(3) { index -> drawRect(Color(0xFF8D5A35), topLeft = Offset(3.0f * u, (3.0f + index * 0.85f) * u), size = Size(3.3f * u, 0.32f * u)) }
            drawRect(Color.White.copy(alpha = 0.7f), topLeft = Offset(5.8f * u, 2.7f * u), size = Size(0.65f * u, 0.65f * u))
        }
        key.contains("beehive") -> {
            repeat(3) { index -> drawRect(Color(0xFF7A4D31), topLeft = Offset(3.0f * u, (3.15f + index * 0.85f) * u), size = Size(3.0f * u, 0.3f * u)) }
            drawRect(Color(0xFF4A3B30), topLeft = Offset(4.35f * u, 4.4f * u), size = Size(0.9f * u, 0.7f * u))
        }
        key.contains("moon_gate") -> {
            drawRect(Color(0xFF9EA3A1), topLeft = Offset(2.3f * u, 1.55f * u), size = Size(4.9f * u, 5.1f * u))
            drawRect(GrovePanel, topLeft = Offset(3.05f * u, 2.15f * u), size = Size(3.4f * u, 4.1f * u))
            drawRect(Color(0xFFFFF4C0), topLeft = Offset(5.15f * u, 2.3f * u), size = Size(0.7f * u, 0.7f * u))
        }
        key.contains("garden_arch") -> {
            repeat(4) { index -> drawRect(GroveLeaf, topLeft = Offset((2.25f + index * 1.2f) * u, (1.25f + index % 2 * 0.35f) * u), size = Size(0.85f * u, 0.85f * u)) }
            drawRect(GroveBerry, topLeft = Offset(6.2f * u, 2.2f * u), size = Size(0.6f * u, 0.6f * u))
        }
        key.contains("garden_search") -> {
            drawRect(Color(0xFFEAF2F0), topLeft = Offset(3.0f * u, 2.5f * u), size = Size(2.6f * u, 2.6f * u))
            drawRect(Color(0xFF5F8FCB), topLeft = Offset(3.35f * u, 2.85f * u), size = Size(1.9f * u, 1.9f * u))
            drawRect(Color(0xFF6B513D), topLeft = Offset(5.25f * u, 4.85f * u), size = Size(1.7f * u, 0.5f * u))
        }
        key.contains("telescope_platform") -> {
            drawRect(Color(0xFF7A4D31), topLeft = Offset(2.0f * u, 6.3f * u), size = Size(6.3f * u, 1.0f * u))
            drawRect(Color(0xFF4F5C74), topLeft = Offset(3.7f * u, 3.2f * u), size = Size(3.4f * u, 0.8f * u))
            drawRect(Color(0xFFFFD96A), topLeft = Offset(6.7f * u, 3.2f * u), size = Size(0.6f * u, 0.8f * u))
            drawRect(Color(0xFF6B513D), topLeft = Offset(4.75f * u, 4.0f * u), size = Size(0.45f * u, 2.5f * u))
        }
        key.contains("planetarium_dome") -> {
            drawRect(Color(0xFF34324A), topLeft = Offset(2.1f * u, 3.0f * u), size = Size(6.2f * u, 4.2f * u))
            drawRect(Color(0xFF8E72D8), topLeft = Offset(2.6f * u, 2.3f * u), size = Size(5.2f * u, 2.4f * u))
            drawRect(Color(0xFFFFF4C0), topLeft = Offset(4.1f * u, 3.05f * u), size = Size(0.5f * u, 0.5f * u))
            drawRect(Color(0xFFBDE7E7), topLeft = Offset(6.25f * u, 3.45f * u), size = Size(0.45f * u, 0.45f * u))
        }
        key.contains("observatory") || key.contains("celestial_observatory") -> {
            if (key.contains("celestial")) {
                drawRect(Color(0xFFFFF4C0), topLeft = Offset(3.1f * u, 1.5f * u), size = Size(0.55f * u, 0.55f * u))
                drawRect(Color(0xFFBDE7E7), topLeft = Offset(6.5f * u, 2.0f * u), size = Size(0.55f * u, 0.55f * u))
                drawRect(Color(0xFFCDBDFF), topLeft = Offset(4.3f * u, 4.6f * u), size = Size(1.5f * u, 0.45f * u))
            }
        }
        key.contains("starflower_bed") -> {
            drawRect(GroveLeaf, topLeft = Offset(2.0f * u, 5.1f * u), size = Size(5.9f * u, 1.5f * u))
            repeat(4) { index ->
                drawRect(Color(0xFFFFF4C0), topLeft = Offset((2.4f + index * 1.3f) * u, (3.7f + index % 2 * 0.55f) * u), size = Size(0.75f * u, 0.75f * u))
            }
        }
        key.contains("flower_pot") -> {
            drawRect(GroveClay, topLeft = Offset(3.15f * u, 5.4f * u), size = Size(3.3f * u, 1.9f * u))
            drawRect(Color(0xFFE2564D), topLeft = Offset(3.5f * u, 3.1f * u), size = Size(1.0f * u, 1.0f * u))
            drawRect(Color(0xFFFFD96A), topLeft = Offset(4.65f * u, 2.75f * u), size = Size(1.1f * u, 1.1f * u))
            drawRect(Color(0xFF8E72D8), topLeft = Offset(5.75f * u, 3.2f * u), size = Size(0.95f * u, 0.95f * u))
        }
        key.contains("mushroom_log") -> {
            drawRect(Color(0xFF7A4D31), topLeft = Offset(2.0f * u, 5.55f * u), size = Size(6.2f * u, 1.6f * u))
            drawRect(Color(0xFFE9E0CE), topLeft = Offset(3.15f * u, 4.35f * u), size = Size(0.8f * u, 1.3f * u))
            drawRect(GroveBerry, topLeft = Offset(2.75f * u, 3.75f * u), size = Size(1.6f * u, 0.8f * u))
            drawRect(Color(0xFFE9E0CE), topLeft = Offset(5.55f * u, 4.7f * u), size = Size(0.7f * u, 1.0f * u))
            drawRect(Color(0xFFBDE7E7), topLeft = Offset(5.25f * u, 4.15f * u), size = Size(1.3f * u, 0.7f * u))
        }
        key.contains("reading_gazebo") -> {
            drawRect(Color(0xFF8D4C3B), topLeft = Offset(1.5f * u, 2.7f * u), size = Size(7.0f * u, 1.0f * u))
            drawRect(Color(0xFFB97948), topLeft = Offset(2.2f * u, 3.7f * u), size = Size(0.55f * u, 3.9f * u))
            drawRect(Color(0xFFB97948), topLeft = Offset(7.2f * u, 3.7f * u), size = Size(0.55f * u, 3.9f * u))
            drawRect(Color(0xFFFFF2D1), topLeft = Offset(3.3f * u, 4.8f * u), size = Size(2.8f * u, 0.8f * u))
        }
        key.contains("tool_shed") -> {
            drawRect(Color(0xFF8D4C3B), topLeft = Offset(2.0f * u, 3.0f * u), size = Size(6.4f * u, 1.0f * u))
            drawRect(Color(0xFF9B6942), topLeft = Offset(2.6f * u, 4.0f * u), size = Size(5.2f * u, 3.6f * u))
            drawRect(Color(0xFF5F8FCB), topLeft = Offset(5.9f * u, 5.0f * u), size = Size(0.9f * u, 0.9f * u))
            drawRect(Color(0xFF6B513D), topLeft = Offset(3.35f * u, 5.15f * u), size = Size(1.1f * u, 2.4f * u))
        }
        key.contains("stone_lantern") -> {
            drawRect(Color(0xFF9EA3A1), topLeft = Offset(3.65f * u, 4.1f * u), size = Size(2.1f * u, 3.4f * u))
            drawRect(Color(0xFFFFE08A), topLeft = Offset(4.05f * u, 4.55f * u), size = Size(1.25f * u, 0.95f * u))
            drawRect(Color(0xFF6D7776), topLeft = Offset(3.2f * u, 3.45f * u), size = Size(3.0f * u, 0.7f * u))
        }
        key.contains("floating_lantern") -> repeat(3) { index ->
            drawRect(Color(0xFFFFE08A), topLeft = Offset((2.5f + index * 1.7f) * u, (3.0f + index % 2 * 0.6f) * u), size = Size(1.0f * u, 1.2f * u))
            drawRect(Color(0xFFFFE08A).copy(alpha = 0.35f), topLeft = Offset((2.25f + index * 1.7f) * u, (2.75f + index % 2 * 0.6f) * u), size = Size(1.5f * u, 1.7f * u))
        }
        key.contains("crystal_cluster") -> {
            repeat(3) { index ->
                drawRect(Color(0xFFBDE7E7).lit(index * 0.06f), topLeft = Offset((3.0f + index * 1.15f) * u, (3.0f - index % 2 * 0.45f) * u), size = Size(1.0f * u, (3.0f + index * 0.5f) * u))
            }
            drawRect(Color.White.copy(alpha = 0.75f), topLeft = Offset(4.35f * u, 3.1f * u), size = Size(0.35f * u, 1.4f * u))
        }
        key.contains("firefly_jar") -> {
            drawRect(Color(0xFFE7FFFF), topLeft = Offset(3.35f * u, 3.2f * u), size = Size(3.0f * u, 3.4f * u))
            drawRect(Color(0xFF6B513D), topLeft = Offset(3.15f * u, 2.75f * u), size = Size(3.4f * u, 0.55f * u))
            repeat(3) { index -> drawRect(Color(0xFFFFF4C0), topLeft = Offset((3.8f + index * 0.75f) * u, (4.0f + index % 2 * 0.75f) * u), size = Size(0.45f * u, 0.45f * u)) }
        }
        key.contains("book_cart") -> {
            drawRect(Color(0xFF8D5A35), topLeft = Offset(2.4f * u, 4.4f * u), size = Size(5.4f * u, 2.0f * u))
            repeat(4) { index -> drawRect(PixelItemPalette[(index + 2) % PixelItemPalette.size], topLeft = Offset((2.8f + index * 1.0f) * u, 3.3f * u), size = Size(0.65f * u, 1.25f * u)) }
            drawRect(Color(0xFF4A3B30), topLeft = Offset(3.0f * u, 6.5f * u), size = Size(0.75f * u, 0.75f * u))
            drawRect(Color(0xFF4A3B30), topLeft = Offset(6.2f * u, 6.5f * u), size = Size(0.75f * u, 0.75f * u))
        }
        key.contains("library_kiosk") -> {
            drawRect(Color(0xFF8D4C3B), topLeft = Offset(2.0f * u, 2.0f * u), size = Size(6.3f * u, 0.9f * u))
            drawRect(Color(0xFF9B6942), topLeft = Offset(2.4f * u, 3.0f * u), size = Size(5.5f * u, 4.2f * u))
            repeat(3) { index -> drawRect(PixelItemPalette[(index + 3) % PixelItemPalette.size], topLeft = Offset((3.0f + index * 1.1f) * u, 3.7f * u), size = Size(0.65f * u, 2.2f * u)) }
        }
        key.contains("sundial") -> {
            drawRect(Color(0xFF9EA3A1), topLeft = Offset(3.0f * u, 6.1f * u), size = Size(4.0f * u, 0.9f * u))
            drawRect(Color(0xFFEAF2F0), topLeft = Offset(3.3f * u, 4.5f * u), size = Size(3.4f * u, 1.4f * u))
            drawRect(Color(0xFF6D7776), topLeft = Offset(4.75f * u, 3.45f * u), size = Size(0.45f * u, 1.5f * u))
        }
        key.contains("obelisk") -> {
            drawRect(Color(0xFF6D7776), topLeft = Offset(3.6f * u, 1.35f * u), size = Size(2.2f * u, 5.3f * u))
            drawRect(Color(0xFFBDE7E7), topLeft = Offset(4.3f * u, 2.7f * u), size = Size(0.75f * u, 0.75f * u))
        }
        key.contains("monument") -> {
            drawRect(Color(0xFFDDAA39), topLeft = Offset(3.5f * u, 1.75f * u), size = Size(2.6f * u, 4.2f * u))
            drawRect(Color(0xFFFFF4C0), topLeft = Offset(4.35f * u, 2.35f * u), size = Size(0.9f * u, 1.2f * u))
        }
        key.contains("statue_plaza") -> {
            drawRect(Color(0xFF9EA3A1), topLeft = Offset(2.3f * u, 6.9f * u), size = Size(5.2f * u, 0.7f * u))
            drawRect(Color(0xFFFFD96A), topLeft = Offset(4.4f * u, 2.2f * u), size = Size(0.85f * u, 0.85f * u))
        }
        key.contains("scholar_statue") || key.contains("master_study_statue") -> {
            drawRect(Color(0xFFFFF2D1), topLeft = Offset(4.1f * u, 3.8f * u), size = Size(1.7f * u, 0.85f * u))
            drawRect(Color(0xFF5F8FCB), topLeft = Offset(4.85f * u, 3.8f * u), size = Size(0.25f * u, 0.85f * u))
            if (key.contains("master")) drawRect(Color(0xFFFFD96A), topLeft = Offset(4.45f * u, 1.45f * u), size = Size(0.85f * u, 0.85f * u))
        }
        key.contains("streak_shrine") || key.contains("study_shrine") -> {
            val glow = if (key.contains("streak")) Color(0xFFFFD96A) else Color(0xFFBDE7E7)
            drawRect(glow, topLeft = Offset(4.2f * u, 3.0f * u), size = Size(1.2f * u, 1.8f * u))
            drawRect(Color(0xFF8D4C3B), topLeft = Offset(3.2f * u, 5.7f * u), size = Size(3.1f * u, 0.7f * u))
        }
        key.contains("quest_board") -> {
            val trim = if (key.contains("weekly")) Color(0xFF8E72D8) else if (key.contains("advanced")) Color(0xFFFFD96A) else GroveBerry
            drawRect(trim, topLeft = Offset(2.35f * u, 1.9f * u), size = Size(5.5f * u, 0.65f * u))
            repeat(if (key.contains("weekly")) 3 else 2) { index ->
                drawRect(Color(0xFFFFF2D1), topLeft = Offset(3.0f * u, (3.0f + index * 0.9f) * u), size = Size(3.8f * u, 0.35f * u))
            }
        }
        key.contains("study_journal") || key.contains("crop_journal") || key.contains("library_catalog") || key.contains("master_crop_license") -> {
            val cover = when {
                key.contains("crop") -> GroveLeaf
                key.contains("license") -> Color(0xFFFFD96A)
                key.contains("catalog") -> Color(0xFF5F8FCB)
                else -> GroveBerry
            }
            drawRect(cover, topLeft = Offset(3.1f * u, 3.0f * u), size = Size(3.8f * u, 3.3f * u))
            drawRect(Color(0xFFFFF2D1), topLeft = Offset(4.65f * u, 3.0f * u), size = Size(0.35f * u, 3.3f * u))
            drawRect(Color.White.copy(alpha = 0.42f), topLeft = Offset(3.65f * u, 3.6f * u), size = Size(1.0f * u, 0.35f * u))
        }
        key.contains("seed_pouch") || key.contains("inventory_bag") -> {
            val bag = if (key.contains("seed")) Color(0xFFD4B95F) else Color(0xFF8D5A35)
            drawRect(bag.shaded(0.7f), topLeft = Offset(3.0f * u, 3.35f * u), size = Size(3.9f * u, 3.8f * u))
            drawRect(bag, topLeft = Offset(2.65f * u, 3.05f * u), size = Size(3.9f * u, 3.8f * u))
            drawRect(Color(0xFFFFF2D1), topLeft = Offset(4.0f * u, 4.15f * u), size = Size(1.15f * u, 0.75f * u))
            if (key.contains("_2")) drawRect(Color(0xFFFFD96A), topLeft = Offset(5.5f * u, 3.35f * u), size = Size(0.55f * u, 0.55f * u))
        }
        key.contains("auto_sort") -> {
            drawRect(Color(0xFF5F8FCB), topLeft = Offset(3.1f * u, 3.0f * u), size = Size(3.1f * u, 0.55f * u))
            drawRect(Color(0xFF5F8FCB), topLeft = Offset(5.5f * u, 2.65f * u), size = Size(0.8f * u, 1.2f * u))
            drawRect(Color(0xFF70B86B), topLeft = Offset(3.8f * u, 5.1f * u), size = Size(3.1f * u, 0.55f * u))
            drawRect(Color(0xFF70B86B), topLeft = Offset(3.55f * u, 4.75f * u), size = Size(0.8f * u, 1.2f * u))
        }
        key.contains("garden_presets") -> repeat(4) { index ->
            drawRect(if (index % 2 == 0) GroveLeaf else Color(0xFF5F8FCB), topLeft = Offset((3.0f + index % 2 * 1.8f) * u, (3.1f + index / 2 * 1.5f) * u), size = Size(1.25f * u, 1.0f * u))
        }
        key.contains("focus_bell") -> {
            drawRect(Color(0xFFFFD96A), topLeft = Offset(3.45f * u, 3.2f * u), size = Size(2.8f * u, 2.7f * u))
            drawRect(Color(0xFF8D5A35), topLeft = Offset(4.55f * u, 5.8f * u), size = Size(0.75f * u, 0.65f * u))
            drawRect(Color(0xFFFFF4C0), topLeft = Offset(4.2f * u, 3.55f * u), size = Size(1.1f * u, 0.45f * u))
        }
        key.contains("capacity") || key.contains("crop_markers") -> {
            repeat(3) { index ->
                drawRect(GroveLeaf, topLeft = Offset((3.0f + index * 1.25f) * u, 4.6f * u), size = Size(0.85f * u, 0.85f * u))
                drawRect(Color(0xFFFFF2D1), topLeft = Offset((3.15f + index * 1.25f) * u, 3.45f * u), size = Size(0.45f * u, 1.1f * u))
            }
        }
        key.contains("theme") || key.contains("weather") || key.contains("seasonal") || key.contains("lighting") -> {
            when {
                key.contains("morning") -> drawRect(Color(0xFFFFD96A), topLeft = Offset(4.0f * u, 2.5f * u), size = Size(2.0f * u, 2.0f * u))
                key.contains("night") -> {
                    drawRect(Color(0xFFE9F4FF), topLeft = Offset(4.0f * u, 2.7f * u), size = Size(2.0f * u, 2.0f * u))
                    drawRect(Color(0xFF34324A), topLeft = Offset(4.8f * u, 2.55f * u), size = Size(1.2f * u, 1.7f * u))
                }
                key.contains("weather") -> {
                    drawRect(Color.White, topLeft = Offset(3.2f * u, 2.9f * u), size = Size(3.2f * u, 1.3f * u))
                    drawRect(Color(0xFF5F8FCB), topLeft = Offset(4.4f * u, 4.7f * u), size = Size(0.45f * u, 1.0f * u))
                }
                else -> repeat(3) { index -> drawRect(PixelItemPalette[(index + 2) % PixelItemPalette.size], topLeft = Offset((3.2f + index * 1.1f) * u, (3.2f + index % 2 * 0.6f) * u), size = Size(0.8f * u, 0.8f * u)) }
            }
        }
        key.contains("greenhouse_shelf") || key.contains("golden_greenhouse_frame") -> {
            val frame = if (key.contains("golden")) Color(0xFFFFD96A) else Color(0xFF6C8F89)
            drawRect(Color(0xFFBDE7E7), topLeft = Offset(2.1f * u, 3.0f * u), size = Size(5.8f * u, 4.0f * u))
            repeat(if (key.contains("_3")) 3 else if (key.contains("_2")) 2 else 1) { index ->
                drawRect(frame, topLeft = Offset(2.4f * u, (4.0f + index * 0.9f) * u), size = Size(5.1f * u, 0.35f * u))
            }
            if (key.contains("golden")) drawRect(Color.White.copy(alpha = 0.7f), topLeft = Offset(6.8f * u, 3.25f * u), size = Size(0.45f * u, 0.45f * u))
        }
        key.contains("house_room") || key.contains("dream_library_wing") -> {
            when {
                key.contains("house_room_2") -> {
                    drawRect(Color(0xFF8D5A35), topLeft = Offset(2.45f * u, 2.8f * u), size = Size(5.3f * u, 4.1f * u))
                    repeat(4) { index ->
                        drawRect(PixelItemPalette[(index + 2) % PixelItemPalette.size], topLeft = Offset((3.0f + index * 0.95f) * u, 3.35f * u), size = Size(0.55f * u, 2.2f * u))
                    }
                    drawRect(Color(0xFFFFF2D1), topLeft = Offset(4.1f * u, 6.15f * u), size = Size(1.9f * u, 0.35f * u))
                }
                key.contains("house_room_3") -> {
                    drawRect(Color(0xFF7A4D31), topLeft = Offset(2.2f * u, 3.2f * u), size = Size(5.9f * u, 3.8f * u))
                    drawRect(Color(0xFF4A3B30), topLeft = Offset(2.8f * u, 2.15f * u), size = Size(4.7f * u, 1.1f * u))
                    repeat(4) { index ->
                        drawRect(Color(0xFFE8B27A).lit(index * 0.03f), topLeft = Offset((3.0f + index * 1.0f) * u, (4.0f + index * 0.55f) * u), size = Size((3.9f - index * 0.55f) * u, 0.35f * u))
                    }
                }
                key.contains("house_room_4") -> {
                    drawRect(Color(0xFFBDE7E7), topLeft = Offset(2.1f * u, 3.0f * u), size = Size(5.7f * u, 3.6f * u))
                    drawRect(Color(0xFF6C8F89), topLeft = Offset(2.0f * u, 6.2f * u), size = Size(6.0f * u, 0.55f * u))
                    repeat(4) { index ->
                        drawRect(Color(0xFF6C8F89), topLeft = Offset((2.55f + index * 1.35f) * u, 4.6f * u), size = Size(0.35f * u, 1.8f * u))
                    }
                    drawRect(Color(0xFFFFD96A), topLeft = Offset(5.8f * u, 2.3f * u), size = Size(1.0f * u, 1.0f * u))
                }
                else -> {
                    repeat(4) { index ->
                        val color = if (index % 2 == 0) GroveCream else Color(0xFFFFE08A)
                        drawRect(color, topLeft = Offset((2.8f + index % 2 * 1.8f) * u, (3.0f + index / 2 * 1.4f) * u), size = Size(1.4f * u, 1.0f * u))
                    }
                    repeat(3) { index ->
                        drawRect(PixelItemPalette[(index + 4) % PixelItemPalette.size], topLeft = Offset((3.1f + index * 1.0f) * u, 5.8f * u), size = Size(0.55f * u, 0.9f * u))
                    }
                }
            }
        }
        key.contains("study_desk_bonus") -> {
            drawRect(Color(0xFFB97948), topLeft = Offset(2.6f * u, 4.5f * u), size = Size(4.8f * u, 1.4f * u))
            drawRect(Color(0xFFFFF2D1), topLeft = Offset(3.3f * u, 3.7f * u), size = Size(2.2f * u, 0.6f * u))
        }
        key.contains("study_music_box") -> {
            drawRect(Color(0xFF8D5A35), topLeft = Offset(3.0f * u, 4.4f * u), size = Size(3.9f * u, 2.2f * u))
            drawRect(Color(0xFFFFF4C0), topLeft = Offset(4.2f * u, 2.6f * u), size = Size(0.45f * u, 1.5f * u))
            drawRect(Color(0xFFFFF4C0), topLeft = Offset(4.2f * u, 2.6f * u), size = Size(1.0f * u, 0.45f * u))
        }
        key.contains("harvest_bonus") -> {
            drawRect(Color(0xFFB97948), topLeft = Offset(3.1f * u, 4.6f * u), size = Size(3.8f * u, 2.0f * u))
            drawRect(GroveLeaf, topLeft = Offset(3.5f * u, 3.6f * u), size = Size(0.9f * u, 1.1f * u))
            drawRect(Color(0xFFFFA755), topLeft = Offset(5.4f * u, 3.8f * u), size = Size(0.9f * u, 0.9f * u))
        }
        key.contains("achievement_hall") -> {
            drawRect(Color(0xFFFFD96A), topLeft = Offset(3.8f * u, 2.9f * u), size = Size(2.0f * u, 2.0f * u))
            drawRect(Color(0xFFFFD96A), topLeft = Offset(4.35f * u, 4.7f * u), size = Size(0.9f * u, 1.0f * u))
            drawRect(Color(0xFF8D5A35), topLeft = Offset(3.65f * u, 5.65f * u), size = Size(2.3f * u, 0.55f * u))
        }
        key.contains("focus_streak_bonus") -> {
            drawRect(Color(0xFFFFD96A), topLeft = Offset(3.2f * u, 5.0f * u), size = Size(3.5f * u, 0.7f * u))
            repeat(3) { index -> drawRect(Color(0xFFFFF4C0), topLeft = Offset((3.6f + index * 0.9f) * u, (3.2f + index % 2 * 0.35f) * u), size = Size(0.55f * u, 1.5f * u)) }
        }
        key.contains("eternal_study_bonus") || key.contains("study_grove_legacy") || key.contains("grand_scholar_estate") -> {
            val glow = if (key.contains("legacy")) Color(0xFFFFD96A) else Color(0xFFBDE7E7)
            drawRect(glow.copy(alpha = 0.45f), topLeft = Offset(2.7f * u, 2.2f * u), size = Size(4.7f * u, 4.7f * u))
            drawRect(glow, topLeft = Offset(3.65f * u, 3.6f * u), size = Size(1.1f * u, 1.1f * u))
            drawRect(glow, topLeft = Offset(5.15f * u, 3.6f * u), size = Size(1.1f * u, 1.1f * u))
            drawRect(glow, topLeft = Offset(4.4f * u, 4.45f * u), size = Size(1.1f * u, 0.55f * u))
        }
        else -> {
            val variant = itemVariant(itemId, key, 4)
            drawRect(itemAccent(itemId, key, 4).copy(alpha = 0.72f), topLeft = Offset((3.1f + variant * 0.9f) * u, 2.45f * u), size = Size(0.55f * u, 0.55f * u))
        }
    }
}

fun formatTimer(millis: Long): String {
    val totalSeconds = (millis / 1_000L).coerceAtLeast(0L)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}
