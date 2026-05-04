package com.studygrove.app.ui.inventory

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
import com.studygrove.app.data.model.PlacementTarget
import com.studygrove.app.data.model.ShopCatalog
import com.studygrove.app.ui.components.EmptyState
import com.studygrove.app.ui.components.InventoryItemSprite
import com.studygrove.app.ui.components.PixelButton
import com.studygrove.app.ui.components.PixelPanel
import com.studygrove.app.ui.components.SectionTitle
import com.studygrove.app.ui.theme.GroveBerry
import com.studygrove.app.ui.theme.GroveClay
import com.studygrove.app.ui.theme.GroveCream
import com.studygrove.app.ui.theme.GroveInk
import com.studygrove.app.ui.theme.GroveMoss

private enum class InventoryTab(val label: String) {
    GARDEN("Garden"),
    HOUSE("House"),
    COSMETICS("Cosmetics")
}

private data class InventoryDisplayItem(
    val shopItemId: String,
    val detail: String
)

@Composable
fun InventoryScreen(
    gameData: GameData,
    onSelectGardenItem: (String) -> Unit,
    onSelectHouseItem: (String) -> Unit,
    onEquipCosmetic: (String) -> Unit
) {
    var tab by remember { mutableStateOf(InventoryTab.GARDEN) }
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle("Inventory")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            InventoryTab.entries.forEach { entry ->
                PixelButton(
                    entry.label,
                    modifier = Modifier.weight(1f),
                    color = if (tab == entry) GroveMoss else GroveClay,
                    onClick = { tab = entry }
                )
            }
        }
        when (tab) {
            InventoryTab.GARDEN -> InventoryList(
                items = gameData.displayItemsFor(PlacementTarget.GARDEN),
                emptyText = "Your garden bag is empty. Study, earn coins, and visit the shop for seeds or decorations.",
                actionText = "Place",
                onAction = onSelectGardenItem
            )
            InventoryTab.HOUSE -> InventoryList(
                items = gameData.displayItemsFor(PlacementTarget.HOUSE),
                emptyText = "No furniture yet. A small desk is waiting in the shop once you reach level 2.",
                actionText = "Place",
                onAction = onSelectHouseItem
            )
            InventoryTab.COSMETICS -> CosmeticList(gameData = gameData, onEquipCosmetic = onEquipCosmetic)
        }
    }
}

@Composable
private fun InventoryList(
    items: List<InventoryDisplayItem>,
    emptyText: String,
    actionText: String,
    onAction: (String) -> Unit
) {
    if (items.isEmpty()) {
        EmptyState(emptyText)
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(items, key = { it.shopItemId }) { inventoryItem ->
                val shopItem = ShopCatalog.byId(inventoryItem.shopItemId)
                PixelPanel(color = Color(0xFFFFF0CE)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        InventoryItemSprite(shopItem, Modifier.size(54.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                shopItem?.name.orEmpty(),
                                color = GroveInk,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 15.sp
                            )
                            Text(inventoryItem.detail, color = GroveInk.copy(alpha = 0.78f), fontWeight = FontWeight.Bold)
                        }
                        PixelButton(actionText, color = GroveMoss, onClick = { onAction(inventoryItem.shopItemId) })
                    }
                }
            }
        }
    }
}

@Composable
private fun CosmeticList(gameData: GameData, onEquipCosmetic: (String) -> Unit) {
    val cosmetics = if (gameData.settings.testModeEnabled) {
        ShopCatalog.items.filter { it.placementTarget == PlacementTarget.CHARACTER }
    } else {
        gameData.character.ownedCosmetics.mapNotNull { ShopCatalog.byId(it) }
    }
    if (cosmetics.isEmpty()) {
        EmptyState("No cosmetics owned yet. Unlock shirts, hats, and backpacks through study progress.")
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(cosmetics, key = { it.id }) { item ->
                PixelPanel(color = Color(0xFFFFF0CE)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        InventoryItemSprite(item, Modifier.size(54.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                item.name,
                                color = GroveInk,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 15.sp
                            )
                            Text(
                                if (gameData.settings.testModeEnabled) "Test mode: unlocked" else item.description,
                                color = GroveInk.copy(alpha = 0.78f)
                            )
                        }
                        PixelButton("Equip", color = GroveBerry, onClick = { onEquipCosmetic(item.id) })
                    }
                }
            }
        }
    }
}

private fun GameData.displayItemsFor(target: PlacementTarget): List<InventoryDisplayItem> =
    if (settings.testModeEnabled) {
        ShopCatalog.items
            .filter { it.placementTarget == target }
            .map { InventoryDisplayItem(shopItemId = it.id, detail = "Test mode: unlimited") }
    } else {
        inventory
            .filter { inventoryItem ->
                ShopCatalog.byId(inventoryItem.shopItemId)?.placementTarget == target && inventoryItem.quantity > 0
            }
            .map { InventoryDisplayItem(shopItemId = it.shopItemId, detail = "Quantity: ${it.quantity}") }
    }
