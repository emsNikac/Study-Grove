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
        val berry = GroveBerry.copy(alpha = accentAlpha)
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
            StudyGroveScreen.INVENTORY -> {
                drawRect(ink, topLeft = androidx.compose.ui.geometry.Offset(2 * u, 2 * u), size = androidx.compose.ui.geometry.Size(4 * u, 5 * u))
                drawRect(berry, topLeft = androidx.compose.ui.geometry.Offset(3 * u, u), size = androidx.compose.ui.geometry.Size(2 * u, 2 * u))
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
                drawRect(ink, topLeft = androidx.compose.ui.geometry.Offset(2 * u, 2 * u), size = androidx.compose.ui.geometry.Size(4 * u, 4 * u))
                drawRect(moss, topLeft = androidx.compose.ui.geometry.Offset(3 * u, 3 * u), size = androidx.compose.ui.geometry.Size(2 * u, 2 * u))
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
            item != null && item.placementTarget == PlacementTarget.HOUSE -> drawFurnitureSprite(key, u)
            item != null && item.placementTarget == PlacementTarget.CHARACTER -> drawCosmeticSprite(item.cosmeticSlot, key, u)
            else -> drawDecorationSprite(key, u)
        }
    }
}

private fun DrawScope.drawCosmeticSprite(slot: CosmeticSlot?, key: String, u: Float) {
    drawRect(GroveInk.copy(alpha = 0.15f), topLeft = androidx.compose.ui.geometry.Offset(1.8f * u, 8.2f * u), size = androidx.compose.ui.geometry.Size(6.4f * u, 0.8f * u))
    when (slot) {
        CosmeticSlot.SHIRT -> {
            val shirt = if (key.contains("blue")) Color(0xFF4B8FC9) else GroveMoss
            drawRect(Color(0xFF2E5572), topLeft = androidx.compose.ui.geometry.Offset(2.6f * u, 3.1f * u), size = androidx.compose.ui.geometry.Size(4.9f * u, 4.8f * u))
            drawRect(shirt, topLeft = androidx.compose.ui.geometry.Offset(2.1f * u, 2.7f * u), size = androidx.compose.ui.geometry.Size(5.2f * u, 4.8f * u))
            drawRect(shirt.copy(alpha = 0.88f), topLeft = androidx.compose.ui.geometry.Offset(1.1f * u, 3.2f * u), size = androidx.compose.ui.geometry.Size(1.7f * u, 1.9f * u))
            drawRect(shirt.copy(alpha = 0.75f), topLeft = androidx.compose.ui.geometry.Offset(7.2f * u, 3.2f * u), size = androidx.compose.ui.geometry.Size(1.7f * u, 1.9f * u))
            drawRect(Color.White.copy(alpha = 0.24f), topLeft = androidx.compose.ui.geometry.Offset(3.1f * u, 3.2f * u), size = androidx.compose.ui.geometry.Size(2.8f * u, 0.6f * u))
            drawRect(GroveInk.copy(alpha = 0.32f), topLeft = androidx.compose.ui.geometry.Offset(4.2f * u, 2.7f * u), size = androidx.compose.ui.geometry.Size(1.2f * u, 0.8f * u))
        }
        CosmeticSlot.HAT -> {
            val hat = if (key.contains("green")) GroveLeaf else GroveClay
            drawRect(Color(0xFF2E5E38), topLeft = androidx.compose.ui.geometry.Offset(2f * u, 5.1f * u), size = androidx.compose.ui.geometry.Size(6.7f * u, 1.1f * u))
            drawRect(hat, topLeft = androidx.compose.ui.geometry.Offset(1.4f * u, 4.4f * u), size = androidx.compose.ui.geometry.Size(7f * u, 1.1f * u))
            drawRect(hat, topLeft = androidx.compose.ui.geometry.Offset(3f * u, 2.1f * u), size = androidx.compose.ui.geometry.Size(4.1f * u, 2.5f * u))
            drawRect(Color.White.copy(alpha = 0.18f), topLeft = androidx.compose.ui.geometry.Offset(3.5f * u, 2.6f * u), size = androidx.compose.ui.geometry.Size(2.8f * u, 0.55f * u))
            drawRect(GroveInk.copy(alpha = 0.18f), topLeft = androidx.compose.ui.geometry.Offset(3f * u, 4.1f * u), size = androidx.compose.ui.geometry.Size(4.1f * u, 0.5f * u))
        }
        CosmeticSlot.BACKPACK -> {
            drawRect(Color(0xFF6B4A2F), topLeft = androidx.compose.ui.geometry.Offset(3.1f * u, 2.2f * u), size = androidx.compose.ui.geometry.Size(4.4f * u, 6.2f * u))
            drawRect(GroveClay, topLeft = androidx.compose.ui.geometry.Offset(2.5f * u, 2f * u), size = androidx.compose.ui.geometry.Size(4.4f * u, 6.1f * u))
            drawRect(Color(0xFFE0A269), topLeft = androidx.compose.ui.geometry.Offset(3.1f * u, 2.6f * u), size = androidx.compose.ui.geometry.Size(3.1f * u, 0.7f * u))
            drawRect(Color(0xFF8D5634), topLeft = androidx.compose.ui.geometry.Offset(3.3f * u, 5.1f * u), size = androidx.compose.ui.geometry.Size(2.7f * u, 2.1f * u))
            drawRect(Color(0xFFFFDFA9), topLeft = androidx.compose.ui.geometry.Offset(4.2f * u, 5.7f * u), size = androidx.compose.ui.geometry.Size(0.9f * u, 0.5f * u))
            drawRect(Color(0xFF5B3525), topLeft = androidx.compose.ui.geometry.Offset(2f * u, 3f * u), size = androidx.compose.ui.geometry.Size(0.7f * u, 4.2f * u))
            drawRect(Color(0xFF5B3525), topLeft = androidx.compose.ui.geometry.Offset(6.8f * u, 3f * u), size = androidx.compose.ui.geometry.Size(0.7f * u, 4.2f * u))
        }
        else -> {
            drawRect(GroveCream, topLeft = androidx.compose.ui.geometry.Offset(3.1f * u, 2.2f * u), size = androidx.compose.ui.geometry.Size(3.8f * u, 5.9f * u))
            drawRect(GroveBerry, topLeft = androidx.compose.ui.geometry.Offset(3.6f * u, 3f * u), size = androidx.compose.ui.geometry.Size(2.8f * u, 3.5f * u))
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
            drawRect(GroveLeaf, topLeft = androidx.compose.ui.geometry.Offset(4.6f * u, 4.2f * u), size = androidx.compose.ui.geometry.Size(0.9f * u, 3f * u))
            drawRect(GroveMoss, topLeft = androidx.compose.ui.geometry.Offset(3.4f * u, 5.4f * u), size = androidx.compose.ui.geometry.Size(1.7f * u, 0.9f * u))
            drawRect(GroveMoss, topLeft = androidx.compose.ui.geometry.Offset(5.1f * u, 5f * u), size = androidx.compose.ui.geometry.Size(1.7f * u, 0.9f * u))
            if (growthReady) {
                drawRect(Color(0xFFFFF6CE), topLeft = androidx.compose.ui.geometry.Offset(3.1f * u, 2.1f * u), size = androidx.compose.ui.geometry.Size(3.8f * u, 3.1f * u))
                drawRect(Color(0xFFFFC94A), topLeft = androidx.compose.ui.geometry.Offset(4.25f * u, 3f * u), size = androidx.compose.ui.geometry.Size(1.5f * u, 1.4f * u))
            } else {
                drawRect(Color(0xFFFFC94A), topLeft = androidx.compose.ui.geometry.Offset(4.25f * u, 3.35f * u), size = androidx.compose.ui.geometry.Size(1.5f * u, 1.3f * u))
            }
        }
    }
}

private fun DrawScope.drawFurnitureSprite(key: String, u: Float) {
    drawRect(GroveInk.copy(alpha = 0.16f), topLeft = androidx.compose.ui.geometry.Offset(1.5f * u, 8.3f * u), size = androidx.compose.ui.geometry.Size(7.2f * u, 0.8f * u))
    when {
        key.contains("chair") -> {
            drawRect(Color(0xFF753D55), topLeft = androidx.compose.ui.geometry.Offset(3.1f * u, 2.2f * u), size = androidx.compose.ui.geometry.Size(4.6f * u, 4.5f * u))
            drawRect(GroveBerry, topLeft = androidx.compose.ui.geometry.Offset(2.4f * u, 4.3f * u), size = androidx.compose.ui.geometry.Size(5.3f * u, 2.7f * u))
            drawRect(Color(0xFFE889A0), topLeft = androidx.compose.ui.geometry.Offset(3.1f * u, 4.7f * u), size = androidx.compose.ui.geometry.Size(3.8f * u, 0.8f * u))
            drawRect(Color(0xFF5B3340), topLeft = androidx.compose.ui.geometry.Offset(3f * u, 7f * u), size = androidx.compose.ui.geometry.Size(0.8f * u, 1.7f * u))
            drawRect(Color(0xFF5B3340), topLeft = androidx.compose.ui.geometry.Offset(6.1f * u, 7f * u), size = androidx.compose.ui.geometry.Size(0.8f * u, 1.7f * u))
        }
        key.contains("bed") -> {
            drawRect(Color(0xFF7A4D31), topLeft = androidx.compose.ui.geometry.Offset(1.3f * u, 3.7f * u), size = androidx.compose.ui.geometry.Size(7.7f * u, 4.5f * u))
            drawRect(Color(0xFF7096C8), topLeft = androidx.compose.ui.geometry.Offset(1.8f * u, 3f * u), size = androidx.compose.ui.geometry.Size(6.6f * u, 4.2f * u))
            drawRect(Color(0xFF4F6F9E), topLeft = androidx.compose.ui.geometry.Offset(1.8f * u, 6.3f * u), size = androidx.compose.ui.geometry.Size(6.6f * u, 0.9f * u))
            drawRect(Color(0xFFFFE7CF), topLeft = androidx.compose.ui.geometry.Offset(2.4f * u, 3.4f * u), size = androidx.compose.ui.geometry.Size(2.2f * u, 1.4f * u))
            drawRect(Color(0xFFE9F1FF), topLeft = androidx.compose.ui.geometry.Offset(4.9f * u, 3.5f * u), size = androidx.compose.ui.geometry.Size(2.3f * u, 1.1f * u))
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
}

private fun DrawScope.drawDecorationSprite(key: String, u: Float) {
    drawRect(GroveInk.copy(alpha = 0.16f), topLeft = androidx.compose.ui.geometry.Offset(1.7f * u, 8.2f * u), size = androidx.compose.ui.geometry.Size(6.6f * u, 0.75f * u))
    when {
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
            drawRect(GroveLeaf, topLeft = androidx.compose.ui.geometry.Offset(2f * u, 2f * u), size = androidx.compose.ui.geometry.Size(5.2f * u, 5.2f * u))
            drawRect(GroveMoss, topLeft = androidx.compose.ui.geometry.Offset(2.7f * u, 2.7f * u), size = androidx.compose.ui.geometry.Size(3.8f * u, 3.8f * u))
            drawRect(Color(0xFFFFE08A), topLeft = androidx.compose.ui.geometry.Offset(6.4f * u, 5.5f * u), size = androidx.compose.ui.geometry.Size(1.8f * u, 0.7f * u))
            drawRect(Color(0xFFFFE08A), topLeft = androidx.compose.ui.geometry.Offset(6.95f * u, 4.95f * u), size = androidx.compose.ui.geometry.Size(0.7f * u, 1.8f * u))
        }
        key.contains("house_upgrade") -> {
            drawRect(Color(0xFF8D4C3B), topLeft = androidx.compose.ui.geometry.Offset(1.5f * u, 3f * u), size = androidx.compose.ui.geometry.Size(7f * u, 1.2f * u))
            drawRect(GroveClay, topLeft = androidx.compose.ui.geometry.Offset(2.2f * u, 4.1f * u), size = androidx.compose.ui.geometry.Size(5.6f * u, 4.2f * u))
            drawRect(Color(0xFFFFE08A), topLeft = androidx.compose.ui.geometry.Offset(6.1f * u, 5.1f * u), size = androidx.compose.ui.geometry.Size(1.1f * u, 1.1f * u))
            drawRect(GroveInk.copy(alpha = 0.3f), topLeft = androidx.compose.ui.geometry.Offset(3.2f * u, 5.6f * u), size = androidx.compose.ui.geometry.Size(1.3f * u, 2.7f * u))
        }
        else -> {
            drawRect(Color(0xFF8D5634), topLeft = androidx.compose.ui.geometry.Offset(3.1f * u, 5.1f * u), size = androidx.compose.ui.geometry.Size(4.1f * u, 2.4f * u))
            drawRect(GroveClay, topLeft = androidx.compose.ui.geometry.Offset(2.8f * u, 4.6f * u), size = androidx.compose.ui.geometry.Size(4.1f * u, 2.4f * u))
            drawRect(Color(0xFFFFE08A), topLeft = androidx.compose.ui.geometry.Offset(3.2f * u, 2.8f * u), size = androidx.compose.ui.geometry.Size(1.3f * u, 1.3f * u))
            drawRect(GroveBerry, topLeft = androidx.compose.ui.geometry.Offset(4.6f * u, 2.2f * u), size = androidx.compose.ui.geometry.Size(1.5f * u, 1.5f * u))
            drawRect(Color(0xFFFFF4C0), topLeft = androidx.compose.ui.geometry.Offset(5.8f * u, 3.1f * u), size = androidx.compose.ui.geometry.Size(1.1f * u, 1.1f * u))
            drawRect(GroveLeaf, topLeft = androidx.compose.ui.geometry.Offset(4.4f * u, 3.7f * u), size = androidx.compose.ui.geometry.Size(0.9f * u, 1.2f * u))
        }
    }
}

fun formatTimer(millis: Long): String {
    val totalSeconds = (millis / 1_000L).coerceAtLeast(0L)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}
