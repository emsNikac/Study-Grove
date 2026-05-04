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
import com.studygrove.app.data.model.CharacterCustomization
import com.studygrove.app.data.model.DailyQuest
import com.studygrove.app.data.model.GardenObject
import com.studygrove.app.data.model.GardenObjectType
import com.studygrove.app.data.model.HouseObject
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
            PixelPanel(color = if (quest.completed) Color(0xFFDDF1BD) else GroveCream.copy(alpha = 0.82f)) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(quest.title, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                        Text("+${quest.rewardCoins}", fontWeight = FontWeight.Black, color = GroveClay)
                    }
                    Text(quest.description, fontSize = 12.sp, color = GroveInk.copy(alpha = 0.72f))
                    PixelProgressBar(
                        progress = quest.progress.toFloat() / quest.target.toFloat(),
                        fill = if (quest.completed) GroveLeaf else GroveBerry
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
    Canvas(modifier.size(24.dp)) {
        val ink = if (selected) GroveMoss else GroveInk.copy(alpha = 0.58f)
        val u = size.minDimension / 8f
        when (screen) {
            StudyGroveScreen.GARDEN -> {
                drawRect(GroveLeaf, topLeft = androidx.compose.ui.geometry.Offset(2 * u, 4 * u), size = androidx.compose.ui.geometry.Size(4 * u, 3 * u))
                drawRect(ink, topLeft = androidx.compose.ui.geometry.Offset(3 * u, 1 * u), size = androidx.compose.ui.geometry.Size(2 * u, 4 * u))
            }
            StudyGroveScreen.TIMER -> {
                drawRect(ink, topLeft = androidx.compose.ui.geometry.Offset(2 * u, u), size = androidx.compose.ui.geometry.Size(4 * u, 6 * u))
                drawRect(GroveCream, topLeft = androidx.compose.ui.geometry.Offset(3 * u, 2 * u), size = androidx.compose.ui.geometry.Size(2 * u, 3 * u))
            }
            StudyGroveScreen.SHOP -> {
                drawRect(GroveClay, topLeft = androidx.compose.ui.geometry.Offset(2 * u, 3 * u), size = androidx.compose.ui.geometry.Size(4 * u, 4 * u))
                drawRect(ink, topLeft = androidx.compose.ui.geometry.Offset(3 * u, u), size = androidx.compose.ui.geometry.Size(2 * u, 3 * u))
            }
            StudyGroveScreen.INVENTORY -> {
                drawRect(ink, topLeft = androidx.compose.ui.geometry.Offset(2 * u, 2 * u), size = androidx.compose.ui.geometry.Size(4 * u, 5 * u))
                drawRect(GroveBerry, topLeft = androidx.compose.ui.geometry.Offset(3 * u, u), size = androidx.compose.ui.geometry.Size(2 * u, 2 * u))
            }
            StudyGroveScreen.HOUSE -> {
                drawRect(GroveClay, topLeft = androidx.compose.ui.geometry.Offset(2 * u, 3 * u), size = androidx.compose.ui.geometry.Size(4 * u, 4 * u))
                drawRect(ink, topLeft = androidx.compose.ui.geometry.Offset(u, 2 * u), size = androidx.compose.ui.geometry.Size(6 * u, u))
            }
            StudyGroveScreen.PROFILE -> {
                drawRect(ink, topLeft = androidx.compose.ui.geometry.Offset(3 * u, u), size = androidx.compose.ui.geometry.Size(2 * u, 2 * u))
                drawRect(GroveLeaf, topLeft = androidx.compose.ui.geometry.Offset(2 * u, 4 * u), size = androidx.compose.ui.geometry.Size(4 * u, 3 * u))
            }
            StudyGroveScreen.SETTINGS -> {
                drawRect(ink, topLeft = androidx.compose.ui.geometry.Offset(2 * u, 2 * u), size = androidx.compose.ui.geometry.Size(4 * u, 4 * u))
                drawRect(GroveCream, topLeft = androidx.compose.ui.geometry.Offset(3 * u, 3 * u), size = androidx.compose.ui.geometry.Size(2 * u, 2 * u))
            }
        }
    }
}

@Composable
fun GardenObjectSprite(gardenObject: GardenObject, modifier: Modifier = Modifier) {
    val item = ShopCatalog.byId(gardenObject.shopItemId)
    PixelItemSprite(item = item, type = gardenObject.type, growthReady = gardenObject.sessionsRemaining == 0, modifier = modifier)
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

@Composable
fun CharacterSprite(character: CharacterCustomization, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val u = size.minDimension / 10f
        val skin = when (character.skinTone) {
            "deep" -> Color(0xFF8F5A3D)
            "light" -> Color(0xFFE7B98A)
            else -> Color(0xFFC98B5A)
        }
        val shirt = when (character.shirt) {
            "blue_shirt" -> Color(0xFF4B8FC9)
            else -> GroveMoss
        }
        drawRect(Color(0xFF4A3328), topLeft = androidx.compose.ui.geometry.Offset(3 * u, u), size = androidx.compose.ui.geometry.Size(4 * u, 2 * u))
        drawRect(skin, topLeft = androidx.compose.ui.geometry.Offset(3 * u, 2 * u), size = androidx.compose.ui.geometry.Size(4 * u, 3 * u))
        drawRect(shirt, topLeft = androidx.compose.ui.geometry.Offset(2 * u, 5 * u), size = androidx.compose.ui.geometry.Size(6 * u, 3 * u))
        drawRect(Color(0xFF2D5C6F), topLeft = androidx.compose.ui.geometry.Offset(3 * u, 8 * u), size = androidx.compose.ui.geometry.Size(1.5f * u, 2 * u))
        drawRect(Color(0xFF2D5C6F), topLeft = androidx.compose.ui.geometry.Offset(5.5f * u, 8 * u), size = androidx.compose.ui.geometry.Size(1.5f * u, 2 * u))
        if (character.hat == "green_hat") {
            drawRect(GroveLeaf, topLeft = androidx.compose.ui.geometry.Offset(2 * u, 0.2f * u), size = androidx.compose.ui.geometry.Size(6 * u, u))
            drawRect(GroveLeaf, topLeft = androidx.compose.ui.geometry.Offset(3 * u, -0.5f * u), size = androidx.compose.ui.geometry.Size(4 * u, u))
        }
        if (character.backpack == "backpack") {
            drawRect(GroveClay, topLeft = androidx.compose.ui.geometry.Offset(1.3f * u, 5.3f * u), size = androidx.compose.ui.geometry.Size(1.3f * u, 2.8f * u))
        }
    }
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
    modifier: Modifier = Modifier
) {
    Canvas(modifier) {
        val u = min(size.width, size.height) / 10f
        val key = item?.visualKey.orEmpty()
        when {
            type == GardenObjectType.PATH || key.contains("path") -> drawPathSprite(key, u)
            type == GardenObjectType.FENCE -> drawFenceSprite(u)
            type == GardenObjectType.WATER || key == "pond" -> drawPondSprite(u)
            type == GardenObjectType.TREE || key.contains("tree") -> drawTreeSprite(key, u)
            type == GardenObjectType.CROP -> drawCropSprite(key, growthReady, u)
            item?.placementTarget?.name == "HOUSE" -> drawFurnitureSprite(key, u)
            else -> drawDecorationSprite(key, u)
        }
    }
}

private fun DrawScope.drawPathSprite(key: String, u: Float) {
    val color = if (key.contains("stone")) Color(0xFF9EA3A1) else Color(0xFFB97948)
    drawRect(color, topLeft = androidx.compose.ui.geometry.Offset(u, 4 * u), size = androidx.compose.ui.geometry.Size(8 * u, 2 * u))
    drawRect(color.copy(alpha = 0.72f), topLeft = androidx.compose.ui.geometry.Offset(2 * u, 6 * u), size = androidx.compose.ui.geometry.Size(6 * u, u))
}

private fun DrawScope.drawFenceSprite(u: Float) {
    drawRect(Color(0xFF8D5A35), topLeft = androidx.compose.ui.geometry.Offset(2 * u, 2 * u), size = androidx.compose.ui.geometry.Size(u, 6 * u))
    drawRect(Color(0xFF8D5A35), topLeft = androidx.compose.ui.geometry.Offset(7 * u, 2 * u), size = androidx.compose.ui.geometry.Size(u, 6 * u))
    drawRect(Color(0xFFB97948), topLeft = androidx.compose.ui.geometry.Offset(u, 4 * u), size = androidx.compose.ui.geometry.Size(8 * u, u))
}

private fun DrawScope.drawPondSprite(u: Float) {
    drawRect(Color(0xFF5DB7C8), topLeft = androidx.compose.ui.geometry.Offset(2 * u, 3 * u), size = androidx.compose.ui.geometry.Size(6 * u, 4 * u))
    drawRect(Color(0xFFBDE7E7), topLeft = androidx.compose.ui.geometry.Offset(4 * u, 4 * u), size = androidx.compose.ui.geometry.Size(2 * u, u))
}

private fun DrawScope.drawTreeSprite(key: String, u: Float) {
    val leaves = if (key.contains("golden")) Color(0xFFFFC94A) else GroveLeaf
    drawRect(Color(0xFF7B4E2D), topLeft = androidx.compose.ui.geometry.Offset(4 * u, 5 * u), size = androidx.compose.ui.geometry.Size(2 * u, 4 * u))
    drawRect(leaves, topLeft = androidx.compose.ui.geometry.Offset(2 * u, u), size = androidx.compose.ui.geometry.Size(6 * u, 5 * u))
    if (key == "apple_tree") {
        drawRect(GroveBerry, topLeft = androidx.compose.ui.geometry.Offset(3 * u, 3 * u), size = androidx.compose.ui.geometry.Size(u, u))
        drawRect(GroveBerry, topLeft = androidx.compose.ui.geometry.Offset(6 * u, 2 * u), size = androidx.compose.ui.geometry.Size(u, u))
    }
}

private fun DrawScope.drawCropSprite(key: String, growthReady: Boolean, u: Float) {
    drawRect(Color(0xFF6E4C2B), topLeft = androidx.compose.ui.geometry.Offset(2 * u, 7 * u), size = androidx.compose.ui.geometry.Size(6 * u, u))
    val flower = when {
        key.contains("carrot") -> Color(0xFFE87935)
        key.contains("strawberry") -> GroveBerry
        key.contains("blossom") -> Color(0xFF8E72D8)
        else -> Color(0xFFFFE46B)
    }
    drawRect(GroveLeaf, topLeft = androidx.compose.ui.geometry.Offset(4 * u, 4 * u), size = androidx.compose.ui.geometry.Size(2 * u, 3 * u))
    if (growthReady) {
        drawRect(flower, topLeft = androidx.compose.ui.geometry.Offset(3 * u, 2 * u), size = androidx.compose.ui.geometry.Size(4 * u, 3 * u))
    } else {
        drawRect(GroveLeaf, topLeft = androidx.compose.ui.geometry.Offset(4 * u, 5 * u), size = androidx.compose.ui.geometry.Size(2 * u, 2 * u))
    }
}

private fun DrawScope.drawFurnitureSprite(key: String, u: Float) {
    val color = when {
        key.contains("chair") -> GroveBerry
        key.contains("bed") -> Color(0xFF7096C8)
        key.contains("shelf") || key.contains("library") -> Color(0xFF8D5A35)
        else -> GroveClay
    }
    drawRect(color, topLeft = androidx.compose.ui.geometry.Offset(2 * u, 3 * u), size = androidx.compose.ui.geometry.Size(6 * u, 4 * u))
    drawRect(GroveInk.copy(alpha = 0.22f), topLeft = androidx.compose.ui.geometry.Offset(3 * u, 4 * u), size = androidx.compose.ui.geometry.Size(4 * u, u))
}

private fun DrawScope.drawDecorationSprite(key: String, u: Float) {
    when {
        key.contains("lamp") -> {
            drawRect(Color(0xFF6B513D), topLeft = androidx.compose.ui.geometry.Offset(4.5f * u, 3 * u), size = androidx.compose.ui.geometry.Size(u, 5 * u))
            drawRect(Color(0xFFFFE46B), topLeft = androidx.compose.ui.geometry.Offset(3 * u, u), size = androidx.compose.ui.geometry.Size(4 * u, 3 * u))
        }
        key.contains("bench") -> {
            drawRect(Color(0xFF8D5A35), topLeft = androidx.compose.ui.geometry.Offset(2 * u, 4 * u), size = androidx.compose.ui.geometry.Size(6 * u, 2 * u))
            drawRect(Color(0xFF6B513D), topLeft = androidx.compose.ui.geometry.Offset(3 * u, 6 * u), size = androidx.compose.ui.geometry.Size(u, 2 * u))
            drawRect(Color(0xFF6B513D), topLeft = androidx.compose.ui.geometry.Offset(6 * u, 6 * u), size = androidx.compose.ui.geometry.Size(u, 2 * u))
        }
        key.contains("greenhouse") -> {
            drawRect(Color(0xFFBDE7E7), topLeft = androidx.compose.ui.geometry.Offset(2 * u, 3 * u), size = androidx.compose.ui.geometry.Size(6 * u, 4 * u))
            drawRect(GroveMoss, topLeft = androidx.compose.ui.geometry.Offset(4 * u, 5 * u), size = androidx.compose.ui.geometry.Size(2 * u, 2 * u))
        }
        else -> {
            drawRect(GroveClay, topLeft = androidx.compose.ui.geometry.Offset(3 * u, 4 * u), size = androidx.compose.ui.geometry.Size(4 * u, 3 * u))
            drawRect(GroveBerry, topLeft = androidx.compose.ui.geometry.Offset(4 * u, 2 * u), size = androidx.compose.ui.geometry.Size(2 * u, 2 * u))
        }
    }
}

fun formatTimer(millis: Long): String {
    val totalSeconds = (millis / 1_000L).coerceAtLeast(0L)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}
