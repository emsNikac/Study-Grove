package com.studygrove.app.ui.house

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.studygrove.app.data.model.GameData
import com.studygrove.app.data.model.PlacementTarget
import com.studygrove.app.data.model.ShopCatalog
import com.studygrove.app.data.model.ShopItem
import com.studygrove.app.ui.components.EmptyState
import com.studygrove.app.ui.components.HouseObjectSprite
import com.studygrove.app.ui.components.InventoryItemSprite
import com.studygrove.app.ui.components.PixelButton
import com.studygrove.app.ui.components.PixelPanel
import com.studygrove.app.ui.components.SectionTitle
import com.studygrove.app.ui.theme.GroveBerry
import com.studygrove.app.ui.theme.GroveClay
import com.studygrove.app.ui.theme.GroveCream
import com.studygrove.app.ui.theme.GroveInk
import com.studygrove.app.ui.theme.GroveMoss

@Composable
fun HouseScreen(
    gameData: GameData,
    selectedItemId: String?,
    onTileTapped: (Int, Int) -> Unit,
    onSelectFurniture: (String) -> Unit,
    onClearSelection: () -> Unit
) {
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionTitle(gameData.house.title)
        selectedItemId?.let { itemId ->
            PixelPanel(color = Color(0xFFFFE9BD)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Placing ${ShopCatalog.byId(itemId)?.name.orEmpty()}", fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                    PixelButton("Cancel", color = GroveBerry, onClick = onClearSelection)
                }
            }
        }
        HouseGrid(gameData = gameData, onTileTapped = onTileTapped)
        SectionTitle("Furniture bag")
        val furniture = gameData.furnitureItems()
        if (furniture.isEmpty()) {
            EmptyState("Buy furniture in the shop to turn this cottage into a real study nest.")
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                furniture.forEach { furnitureItem ->
                    val item = furnitureItem.item
                    PixelPanel(color = GroveCream) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            InventoryItemSprite(item, Modifier.size(54.dp))
                            Column(Modifier.weight(1f)) {
                                Text(item.name, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                                Text(furnitureItem.detail, color = GroveInk.copy(alpha = 0.68f))
                            }
                            PixelButton("Place", color = GroveClay, onClick = { onSelectFurniture(furnitureItem.item.id) })
                        }
                    }
                }
            }
        }
    }
}

private data class FurnitureDisplayItem(
    val item: ShopItem,
    val detail: String
)

private fun GameData.furnitureItems(): List<FurnitureDisplayItem> =
    if (settings.testModeEnabled) {
        ShopCatalog.items
            .filter { it.placementTarget == PlacementTarget.HOUSE }
            .map { FurnitureDisplayItem(item = it, detail = "Test mode: unlimited") }
    } else {
        inventory.mapNotNull { inventoryItem ->
            val item = ShopCatalog.byId(inventoryItem.shopItemId)
            if (item?.placementTarget == PlacementTarget.HOUSE && inventoryItem.quantity > 0) {
                FurnitureDisplayItem(item = item, detail = "Quantity: ${inventoryItem.quantity}")
            } else {
                null
            }
        }
    }

@Composable
private fun HouseGrid(gameData: GameData, onTileTapped: (Int, Int) -> Unit) {
    val haptic = LocalHapticFeedback.current
    val objectsByTile = remember(gameData.house.objects) { gameData.house.objects.associateBy { it.x to it.y } }
    PixelPanel(color = Color(0xFFDCA875)) {
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val tileSize = maxWidth / gameData.house.width
            Column(Modifier.fillMaxWidth()) {
                repeat(gameData.house.height) { y ->
                    Row {
                        repeat(gameData.house.width) { x ->
                            val obj = objectsByTile[x to y]
                            Box(
                                modifier = Modifier
                                    .size(tileSize)
                                    .background(if ((x + y) % 2 == 0) Color(0xFFECC18D) else Color(0xFFE4B47D))
                                    .border(0.5.dp, GroveInk.copy(alpha = 0.28f))
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onTileTapped(x, y)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                obj?.let { HouseObjectSprite(it, Modifier.padding(2.dp).size(36.dp)) }
                            }
                        }
                    }
                }
            }
        }
    }
}
