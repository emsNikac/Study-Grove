package com.studygrove.app.ui.shop

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studygrove.app.data.model.GameData
import com.studygrove.app.data.model.ShopCatalog
import com.studygrove.app.data.model.ShopCategory
import com.studygrove.app.data.model.ShopItem
import com.studygrove.app.ui.components.InventoryItemSprite
import com.studygrove.app.ui.components.PixelButton
import com.studygrove.app.ui.components.PixelPanel
import com.studygrove.app.ui.components.SectionTitle
import com.studygrove.app.ui.theme.GroveBerry
import com.studygrove.app.ui.theme.GroveClay
import com.studygrove.app.ui.theme.GroveCream
import com.studygrove.app.ui.theme.GroveInk
import com.studygrove.app.ui.theme.GroveLeaf
import com.studygrove.app.ui.theme.GroveMoss

@Composable
fun ShopScreen(gameData: GameData, onBuyItem: (String) -> Unit) {
    var selectedCategory by remember { mutableStateOf(ShopCategory.SEEDS) }
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle("Shop")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            PixelPanel(modifier = Modifier.weight(1f), color = GroveCream) {
                Text("Coins: ${gameData.player.coins}", color = GroveInk, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
            }
            PixelPanel(modifier = Modifier.weight(1f), color = GroveCream) {
                Text("Level: ${gameData.player.level}", color = GroveInk, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
            }
        }
        ShopCategoryGrid(selectedCategory = selectedCategory, onSelected = { selectedCategory = it })
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(ShopCatalog.items.filter { it.category == selectedCategory }, key = { it.id }) { item ->
                ShopItemRow(gameData = gameData, item = item, onBuyItem = onBuyItem)
            }
        }
    }
}

@Composable
private fun ShopCategoryGrid(
    selectedCategory: ShopCategory,
    onSelected: (ShopCategory) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        ShopCategory.entries.chunked(2).forEach { rowCategories ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                rowCategories.forEach { category ->
                    PixelButton(
                        text = category.displayName,
                        modifier = Modifier.weight(1f),
                        color = if (selectedCategory == category) GroveMoss else GroveClay,
                        onClick = { onSelected(category) }
                    )
                }
                if (rowCategories.size == 1) {
                    PixelPanel(modifier = Modifier.weight(1f), color = Color.Transparent) {}
                }
            }
        }
    }
}

@Composable
private fun ShopItemRow(gameData: GameData, item: ShopItem, onBuyItem: (String) -> Unit) {
    val testMode = gameData.settings.testModeEnabled
    val ownedCount = gameData.player.purchasedCounts[item.id] ?: 0
    val ownedMax = item.maxPurchases != null && ownedCount >= item.maxPurchases
    val unlocked = testMode || gameData.player.level >= item.unlockLevel
    val affordable = testMode || gameData.player.coins >= item.price
    val enabled = testMode || (unlocked && affordable && !ownedMax)
    PixelPanel(color = if (unlocked) Color(0xFFFFF0CE) else Color(0xFFE7DCCB)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            InventoryItemSprite(item = item, modifier = Modifier.size(54.dp).padding(2.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(item.name, color = GroveInk, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace, fontSize = 15.sp)
                Text(item.description, fontSize = 12.sp, color = GroveInk.copy(alpha = 0.78f))
                Text(
                    if (testMode) {
                        "Test mode: free and unlocked"
                    } else {
                        "Cost ${item.price} coins | Unlock level ${item.unlockLevel}" + if (ownedMax) " | Owned" else ""
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (enabled) Color(0xFF2F6845) else GroveBerry
                )
            }
            PixelButton(
                text = if (testMode) "Test" else if (ownedMax) "Owned" else "Buy",
                enabled = enabled,
                color = if (enabled) GroveLeaf else GroveBerry,
                onClick = { onBuyItem(item.id) }
            )
        }
    }
}
