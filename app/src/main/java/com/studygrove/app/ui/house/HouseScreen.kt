package com.studygrove.app.ui.house

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.studygrove.app.data.model.GameData
import com.studygrove.app.data.model.HouseObjectType
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
    PixelPanel(color = Color(0xFFC88A58)) {
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val tileSize = maxWidth / gameData.house.width
            Column(Modifier.fillMaxWidth()) {
                repeat(gameData.house.height) { y ->
                    Row {
                        repeat(gameData.house.width) { x ->
                            val obj = objectsByTile[x to y]
                            val bedConnections = BedConnections(
                                left = objectsByTile[(x - 1) to y]?.type == HouseObjectType.BED,
                                right = objectsByTile[(x + 1) to y]?.type == HouseObjectType.BED,
                                up = objectsByTile[x to (y - 1)]?.type == HouseObjectType.BED,
                                down = objectsByTile[x to (y + 1)]?.type == HouseObjectType.BED
                            )
                            Box(
                                modifier = Modifier
                                    .size(tileSize)
                                    .background(if ((x + y) % 2 == 0) Color(0xFFE8B27A) else Color(0xFFD79A62))
                                    .border(0.5.dp, GroveInk.copy(alpha = 0.28f))
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onTileTapped(x, y)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                obj?.let {
                                    if (it.type == HouseObjectType.BED) {
                                        Canvas(Modifier.matchParentSize().padding(1.dp)) {
                                            drawConnectedBedTile(bedConnections)
                                        }
                                    } else {
                                        HouseObjectSprite(it, Modifier.matchParentSize().padding(2.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class BedConnections(
    val left: Boolean = false,
    val right: Boolean = false,
    val up: Boolean = false,
    val down: Boolean = false
)

private fun DrawScope.drawConnectedBedTile(connections: BedConnections) {
    val u = size.minDimension / 10f
    val horizontal = connections.left || connections.right || (!connections.up && !connections.down)
    val vertical = connections.up || connections.down
    val wood = Color(0xFF7A4D31)
    val woodDark = Color(0xFF4F3223)
    val blanket = Color(0xFF7096C8)
    val blanketDark = Color(0xFF4F6F9E)
    val pillow = Color(0xFFFFE7CF)

    drawRect(GroveInk.copy(alpha = 0.16f), topLeft = Offset(0.8f * u, 8.4f * u), size = Size(8.5f * u, 0.85f * u))

    if (horizontal) {
        val start = if (connections.left) 0f else 0.9f * u
        val end = if (connections.right) size.width else size.width - 0.9f * u
        val width = end - start
        drawRect(woodDark, topLeft = Offset(start + 0.25f * u, 3.2f * u), size = Size(width, 5.2f * u))
        drawRect(wood, topLeft = Offset(start, 2.7f * u), size = Size(width, 5.2f * u))
        drawRect(blanket, topLeft = Offset(start + 0.45f * u, 3.3f * u), size = Size(width - 0.9f * u, 3.5f * u))
        drawRect(blanketDark, topLeft = Offset(start + 0.45f * u, 6.1f * u), size = Size(width - 0.9f * u, 0.75f * u))
        if (!connections.left) {
            drawRect(pillow, topLeft = Offset(start + 0.55f * u, 3.6f * u), size = Size(2.1f * u, 1.4f * u))
            drawRect(Color.White.copy(alpha = 0.35f), topLeft = Offset(start + 0.9f * u, 3.9f * u), size = Size(1.35f * u, 0.45f * u))
        }
        if (!connections.right) {
            drawRect(woodDark, topLeft = Offset(end - 0.75f * u, 2.7f * u), size = Size(0.75f * u, 5.2f * u))
        }
    } else if (vertical) {
        val start = if (connections.up) 0f else 0.65f * u
        val end = if (connections.down) size.height else size.height - 0.65f * u
        val height = end - start
        drawRect(woodDark, topLeft = Offset(2.15f * u, start + 0.25f * u), size = Size(5.8f * u, height))
        drawRect(wood, topLeft = Offset(1.65f * u, start), size = Size(5.8f * u, height))
        drawRect(blanket, topLeft = Offset(2.2f * u, start + 0.45f * u), size = Size(4.5f * u, height - 0.9f * u))
        drawRect(blanketDark, topLeft = Offset(5.85f * u, start + 0.45f * u), size = Size(0.85f * u, height - 0.9f * u))
        if (!connections.up) {
            drawRect(pillow, topLeft = Offset(2.65f * u, start + 0.55f * u), size = Size(3.4f * u, 1.35f * u))
            drawRect(Color.White.copy(alpha = 0.35f), topLeft = Offset(3.2f * u, start + 0.9f * u), size = Size(2.35f * u, 0.38f * u))
        }
        if (!connections.down) {
            drawRect(woodDark, topLeft = Offset(1.65f * u, end - 0.75f * u), size = Size(5.8f * u, 0.75f * u))
        }
    }
}
