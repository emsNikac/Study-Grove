package com.studygrove.app.ui.house

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.studygrove.app.data.model.GameData
import com.studygrove.app.data.model.HouseObject
import com.studygrove.app.data.model.HouseObjectType
import com.studygrove.app.data.model.HouseLayoutCell
import com.studygrove.app.data.model.HouseLayoutCellKind
import com.studygrove.app.data.model.HousePortalKind
import com.studygrove.app.data.model.HouseRoom
import com.studygrove.app.data.model.HouseRooms
import com.studygrove.app.data.model.HouseTransition
import com.studygrove.app.data.model.ShopCatalog
import com.studygrove.app.ui.components.CharacterSprite
import com.studygrove.app.ui.components.HouseObjectSprite
import com.studygrove.app.ui.components.PixelButton
import com.studygrove.app.ui.components.PixelPanel
import com.studygrove.app.ui.components.SectionTitle
import com.studygrove.app.ui.components.ZoomableMapViewport
import com.studygrove.app.ui.inventory.HouseInventoryPanel
import com.studygrove.app.ui.theme.GroveBerry
import com.studygrove.app.ui.theme.GroveInk
import com.studygrove.app.ui.theme.GroveMoss
import kotlin.math.roundToInt
import kotlin.math.sqrt

@Composable
fun HouseScreen(
    gameData: GameData,
    selectedItemId: String?,
    onTileTapped: (Int, Int) -> Unit,
    onSelectFurniture: (String) -> Unit,
    onClearSelection: () -> Unit
) {
    var showHouseInventory by remember { mutableStateOf(false) }
    var explorerPosition by remember {
        mutableStateOf(entryPositionForRoom(gameData.house.currentRoom, null))
    }
    var joystickVector by remember { mutableStateOf(Offset.Zero) }
    val latestGameData by rememberUpdatedState(gameData)
    val latestExplorerPosition by rememberUpdatedState(explorerPosition)
    val latestJoystickVector by rememberUpdatedState(joystickVector)
    val latestSelectedItemId by rememberUpdatedState(selectedItemId)
    val latestOnTileTapped by rememberUpdatedState(onTileTapped)
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(gameData.house.currentRoom) {
        val currentRoom = gameData.house.currentRoom
        val spec = HouseRooms.spec(currentRoom)
        if (explorerPosition.room != currentRoom) {
            explorerPosition = entryPositionForRoom(currentRoom, explorerPosition.room)
        } else if (explorerPosition.x !in 0f..(spec.width - 1).toFloat() || explorerPosition.y !in 0f..(spec.height - 1).toFloat()) {
            explorerPosition = entryPositionForRoom(currentRoom, null)
        }
    }

    LaunchedEffect(Unit) {
        var lastFrameNanos = withFrameNanos { it }
        while (true) {
            val frameNanos = withFrameNanos { it }
            val deltaSeconds = ((frameNanos - lastFrameNanos) / 1_000_000_000f).coerceAtMost(0.05f)
            lastFrameNanos = frameNanos

            val vector = latestJoystickVector
            if (vector == Offset.Zero) continue

            val move = resolveExplorerMove(
                gameData = latestGameData,
                current = latestExplorerPosition,
                dx = vector.x * HouseExplorerSpeedTilesPerSecond * deltaSeconds,
                dy = vector.y * HouseExplorerSpeedTilesPerSecond * deltaSeconds
            )

            if (move != null && !(move.portalTile != null && latestSelectedItemId != null)) {
                explorerPosition = move.position
                move.portalTile?.let { (x, y) ->
                    joystickVector = Offset.Zero
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    latestOnTileTapped(x, y)
                }
            }
        }
    }

    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                SectionTitle(gameData.house.currentRoom.displayName)
                Text(
                    gameData.house.title,
                    color = GroveInk.copy(alpha = 0.78f),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
            PixelButton("Furniture", color = GroveMoss, onClick = { showHouseInventory = true })
        }
        RoomStatusRow(gameData = gameData)
        selectedItemId?.let { itemId ->
            PixelPanel(color = Color(0xFFFFE9BD)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Placing ${ShopCatalog.byId(itemId)?.name.orEmpty()}", fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                    PixelButton("Cancel", color = GroveBerry, onClick = onClearSelection)
                }
            }
        }
        HouseGrid(
            gameData = gameData,
            explorerPosition = explorerPosition,
            onTileTapped = onTileTapped
        )
        HouseExplorerControls(
            canChangeRooms = selectedItemId == null,
            vector = joystickVector,
            onVectorChange = { joystickVector = it }
        )
    }
    if (showHouseInventory) {
        AlertDialog(
            onDismissRequest = { showHouseInventory = false },
            title = {
                Text("House inventory", fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
            },
            text = {
                HouseInventoryPanel(gameData = gameData) { itemId ->
                    showHouseInventory = false
                    onSelectFurniture(itemId)
                }
            },
            confirmButton = {
                PixelButton("Close", color = GroveBerry) { showHouseInventory = false }
            }
        )
    }
}

@Composable
private fun HouseGrid(
    gameData: GameData,
    explorerPosition: HouseExplorerPosition,
    onTileTapped: (Int, Int) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val currentRoom = gameData.house.currentRoom
    val roomSpec = remember(currentRoom) { HouseRooms.spec(currentRoom) }
    val objectsByTile = remember(gameData.house.objects, currentRoom) {
        gameData.house.objects
            .filter { it.room == currentRoom }
            .associateBy { it.x to it.y }
    }
    val transitionsByTile = remember(gameData.house.currentRoom, gameData.house.level, gameData.house.width, gameData.house.height) {
        HouseRooms.transitions(gameData.house).associateBy { it.x to it.y }
    }
    val layoutByTile = remember(gameData.house.currentRoom, gameData.house.level, gameData.house.width, gameData.house.height) {
        HouseRooms.layoutCells(gameData.house).associateBy { it.x to it.y }
    }
    ZoomableMapViewport(
        gridWidth = roomSpec.width,
        gridHeight = roomSpec.height,
        panelColor = Color(0xFFC88A58),
        maxScale = 3.5f
    ) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val tileSize = maxWidth / roomSpec.width
            Column(Modifier.fillMaxWidth()) {
                repeat(roomSpec.height) { y ->
                    Row {
                        repeat(roomSpec.width) { x ->
                            val obj = objectsByTile[x to y]
                            val transition = transitionsByTile[x to y]?.takeIf { obj == null }
                            val layoutCell = layoutByTile[x to y]?.takeIf { obj == null && transition == null }
                            Box(
                                modifier = Modifier
                                    .size(tileSize)
                                    .background(houseFloorColor(currentRoom, x, y))
                                    .border(0.5.dp, GroveInk.copy(alpha = 0.28f))
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onTileTapped(x, y)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                when {
                                    transition != null -> HousePortalTile(
                                        transition = transition,
                                        unlocked = transition.target in gameData.house.unlockedRooms,
                                        modifier = Modifier.matchParentSize().padding(2.dp)
                                    )
                                    layoutCell != null -> HouseLayoutTile(
                                        cell = layoutCell,
                                        modifier = Modifier.matchParentSize().padding(1.dp)
                                    )
                                    obj != null -> HouseObjectSprite(obj, Modifier.matchParentSize().padding(2.dp))
                                }
                            }
                        }
                    }
                }
            }
            if (explorerPosition.room == currentRoom) {
                HouseExplorerSprite(
                    gameData.character,
                    modifier = Modifier
                        .size(tileSize)
                        .offset {
                            IntOffset(
                                x = (explorerPosition.x * tileSize.toPx()).roundToInt(),
                                y = (explorerPosition.y * tileSize.toPx()).roundToInt()
                            )
                        }
                        .padding(1.dp)
                )
            }
        }
    }
}

@Composable
private fun HouseExplorerSprite(
    character: com.studygrove.app.data.model.CharacterCustomization,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Canvas(Modifier.fillMaxSize()) {
            val u = size.minDimension / 10f
            drawOval(
                Color(0xFF2D241F).copy(alpha = 0.24f),
                topLeft = Offset(2.0f * u, 7.7f * u),
                size = Size(6.0f * u, 1.4f * u)
            )
            drawRect(
                Color(0xFF2D241F).copy(alpha = 0.16f),
                topLeft = Offset(2.7f * u, 6.2f * u),
                size = Size(4.6f * u, 2.1f * u)
            )
        }
        CharacterSprite(character = character, modifier = Modifier.fillMaxSize().padding(1.dp))
    }
}

@Composable
private fun HouseExplorerControls(
    canChangeRooms: Boolean,
    vector: Offset,
    onVectorChange: (Offset) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Walk",
            color = GroveInk,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Black
        )
        Box(
            modifier = Modifier
                .size(148.dp)
                .background(Color(0xFFFFE0A3), CircleShape)
                .border(3.dp, GroveInk.copy(alpha = 0.5f), CircleShape)
                .pointerInput(Unit) {
                    fun updateVector(position: Offset) {
                        val radius = minOf(size.width, size.height) / 2f
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val raw = position - center
                        val length = sqrt(raw.x * raw.x + raw.y * raw.y)
                        if (length < radius * 0.12f) {
                            onVectorChange(Offset.Zero)
                            return
                        }

                        val scale = if (length > radius) radius / length else 1f
                        val clamped = Offset(raw.x * scale, raw.y * scale)
                        onVectorChange(Offset(clamped.x / radius, clamped.y / radius))
                    }

                    detectDragGestures(
                        onDragStart = { position -> updateVector(position) },
                        onDragCancel = { onVectorChange(Offset.Zero) },
                        onDragEnd = { onVectorChange(Offset.Zero) },
                        onDrag = { change, _ ->
                            updateVector(change.position)
                            change.consume()
                        }
                    )
                }
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(Modifier.fillMaxSize()) {
                val u = size.minDimension / 10f
                drawCircle(GroveInk.copy(alpha = 0.08f), radius = 4.1f * u, center = center)
                drawCircle(GroveInk.copy(alpha = 0.16f), radius = 2.35f * u, center = center)
                drawRect(Color(0xFFFFF2D1), topLeft = Offset(center.x - 0.25f * u, 1.0f * u), size = Size(0.5f * u, 1.1f * u))
                drawRect(Color(0xFFFFF2D1), topLeft = Offset(center.x - 0.25f * u, 7.9f * u), size = Size(0.5f * u, 1.1f * u))
                drawRect(Color(0xFFFFF2D1), topLeft = Offset(1.0f * u, center.y - 0.25f * u), size = Size(1.1f * u, 0.5f * u))
                drawRect(Color(0xFFFFF2D1), topLeft = Offset(7.9f * u, center.y - 0.25f * u), size = Size(1.1f * u, 0.5f * u))
            }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .offset(x = (vector.x * 42).dp, y = (vector.y * 42).dp)
                    .background(
                        if (canChangeRooms) GroveMoss else Color(0xFF9F978B),
                        CircleShape
                    )
                    .border(2.dp, GroveInk.copy(alpha = 0.38f), CircleShape)
            )
        }
    }
}

private data class HouseExplorerPosition(
    val room: HouseRoom,
    val x: Float,
    val y: Float
)

private data class HouseExplorerMove(
    val position: HouseExplorerPosition,
    val portalTile: Pair<Int, Int>? = null
)

private const val HouseExplorerSpeedTilesPerSecond = 3.4f

private fun resolveExplorerMove(
    gameData: GameData,
    current: HouseExplorerPosition,
    dx: Float,
    dy: Float
): HouseExplorerMove? {
    val directMove = resolveExplorerMoveCandidate(gameData, current, dx, dy)
    if (directMove != null) return directMove

    val horizontalMove = resolveExplorerMoveCandidate(gameData, current, dx, 0f)
    if (horizontalMove != null) return horizontalMove

    return resolveExplorerMoveCandidate(gameData, current, 0f, dy)
}

private fun resolveExplorerMoveCandidate(
    gameData: GameData,
    current: HouseExplorerPosition,
    dx: Float,
    dy: Float
): HouseExplorerMove? {
    if (dx == 0f && dy == 0f) return null

    val room = gameData.house.currentRoom
    val start = if (current.room == room) current else entryPositionForRoom(room, current.room)
    val nextX = start.x + dx
    val nextY = start.y + dy
    val roomSpec = HouseRooms.spec(room)

    if (nextX !in 0f..(roomSpec.width - 1).toFloat() || nextY !in 0f..(roomSpec.height - 1).toFloat()) {
        return null
    }

    val tileX = nextX.roundToInt().coerceIn(0, roomSpec.width - 1)
    val tileY = nextY.roundToInt().coerceIn(0, roomSpec.height - 1)

    val portal = HouseRooms.transitionAt(gameData.house.level, room, tileX, tileY)
    if (portal != null) {
        if (portal.target == room) {
            return HouseExplorerMove(start.copy(x = nextX, y = nextY))
        }
        if (portal.target !in gameData.house.unlockedRooms) return null
        return HouseExplorerMove(
            position = entryPositionForRoom(portal.target, room),
            portalTile = tileX to tileY
        )
    }

    if (HouseRooms.layoutCellAt(gameData.house.level, room, tileX, tileY) != null) return null

    val objectAtTile = gameData.house.objects.firstOrNull {
        it.room == room && it.x == tileX && it.y == tileY
    }
    if (objectAtTile?.blocksExplorer() == true) return null

    return HouseExplorerMove(start.copy(x = nextX, y = nextY))
}

private fun entryPositionForRoom(room: HouseRoom, fromRoom: HouseRoom?): HouseExplorerPosition {
    val coordinate = when (room) {
        HouseRoom.MAIN -> when (fromRoom) {
            HouseRoom.BEDROOM -> 1 to 3
            HouseRoom.KITCHEN -> 10 to 5
            HouseRoom.STUDY -> 10 to 1
            HouseRoom.LIBRARY -> 2 to 1
            else -> 3 to 5
        }
        HouseRoom.BEDROOM -> 4 to 4
        HouseRoom.STUDY -> 4 to 4
        HouseRoom.KITCHEN -> 4 to 4
        HouseRoom.LIBRARY -> if (fromRoom == HouseRoom.ATTIC) 8 to 1 else 5 to 5
        HouseRoom.ATTIC -> if (fromRoom == HouseRoom.BALCONY) 7 to 1 else 1 to 3
        HouseRoom.BALCONY -> 1 to 1
    }
    return HouseExplorerPosition(room = room, x = coordinate.first.toFloat(), y = coordinate.second.toFloat())
}

private fun HouseObject.blocksExplorer(): Boolean =
    when (type) {
        HouseObjectType.DESK,
        HouseObjectType.CHAIR,
        HouseObjectType.SHELF,
        HouseObjectType.BED,
        HouseObjectType.WALL -> true
        HouseObjectType.DECOR -> false
    }

@Composable
private fun HouseLayoutTile(
    cell: HouseLayoutCell,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        drawHouseLayoutCell(cell)
    }
}

@Composable
private fun RoomStatusRow(gameData: GameData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val visibleRooms = gameData.house.unlockedRooms.ifEmpty { listOf(HouseRoom.MAIN) }
        visibleRooms.forEach { room ->
            Box(
                modifier = Modifier
                    .background(
                        color = when {
                            room == gameData.house.currentRoom -> Color(0xFFFFE08A)
                            else -> Color(0xFFE7D3C2)
                        }
                    )
                    .border(1.dp, GroveInk.copy(alpha = 0.38f))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = room.displayName,
                    color = GroveInk,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
private fun HousePortalTile(
    transition: HouseTransition,
    unlocked: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        drawPortal(transition = transition, unlocked = unlocked)
    }
}

private fun houseFloorColor(room: HouseRoom, x: Int, y: Int): Color {
    val even = (x + y) % 2 == 0
    return when (room) {
        HouseRoom.MAIN -> if (even) Color(0xFFE8B27A) else Color(0xFFD79A62)
        HouseRoom.BEDROOM -> if (even) Color(0xFFE8A7A0) else Color(0xFFD98A84)
        HouseRoom.STUDY -> if (even) Color(0xFFD6C596) else Color(0xFFC2AE78)
        HouseRoom.KITCHEN -> if (even) Color(0xFFFFD58E) else Color(0xFFE7B86C)
        HouseRoom.LIBRARY -> if (even) Color(0xFFC78567) else Color(0xFFA96855)
        HouseRoom.ATTIC -> if (even) Color(0xFFB47A4D) else Color(0xFF95613E)
        HouseRoom.BALCONY -> if (even) Color(0xFFC4D7D9) else Color(0xFFA8C3C7)
    }
}

private fun DrawScope.drawHouseLayoutCell(cell: HouseLayoutCell) {
    val u = size.minDimension / 10f
    val dark = Color(0xFF3E2F2B)
    when (cell.kind) {
        HouseLayoutCellKind.WALL -> {
            val isGrayWall = cell.label.contains("gray", ignoreCase = true)
            val isWoodenWall = cell.label.contains("wooden", ignoreCase = true)
            val edge = when {
                isGrayWall -> Color(0xFF6F7477)
                isWoodenWall -> Color(0xFF6A432B)
                else -> Color(0xFF8D5A35)
            }
            val face = when {
                isGrayWall -> Color(0xFFA9AFB0)
                isWoodenWall -> Color(0xFF9B6942)
                else -> Color(0xFFB97948)
            }
            val groove = when {
                isGrayWall -> Color(0xFFE2E0D5).copy(alpha = 0.38f)
                isWoodenWall -> Color(0xFFFFD58E).copy(alpha = 0.28f)
                else -> Color(0xFFFFD58E).copy(alpha = 0.36f)
            }
            drawRect(edge, topLeft = Offset(0.8f * u, 1.0f * u), size = Size(8.4f * u, 7.8f * u))
            drawRect(face, topLeft = Offset(1.2f * u, 1.5f * u), size = Size(7.6f * u, 6.6f * u))
            repeat(3) { index ->
                drawRect(groove, topLeft = Offset((1.4f + index * 2.4f) * u, 2.1f * u), size = Size(0.4f * u, 5.1f * u))
            }
        }
        HouseLayoutCellKind.WINDOW -> {
            drawRect(dark.copy(alpha = 0.25f), topLeft = Offset(1.4f * u, 1.2f * u), size = Size(7.2f * u, 7.0f * u))
            drawRect(Color(0xFFBDE7E7), topLeft = Offset(1.9f * u, 1.7f * u), size = Size(6.2f * u, 5.8f * u))
            drawRect(Color(0xFFE7FFFF), topLeft = Offset(2.4f * u, 2.2f * u), size = Size(2.2f * u, 2.1f * u))
            drawRect(Color(0xFF6C8F89), topLeft = Offset(4.75f * u, 1.8f * u), size = Size(0.45f * u, 5.6f * u))
            drawRect(Color(0xFF6C8F89), topLeft = Offset(2.0f * u, 4.6f * u), size = Size(6.0f * u, 0.45f * u))
            drawRect(Color(0xFFFFF2B6), topLeft = Offset(6.1f * u, 2.4f * u), size = Size(0.8f * u, 0.8f * u))
        }
        HouseLayoutCellKind.SHELF -> {
            drawRect(Color(0xFF6B513D), topLeft = Offset(1.3f * u, 1.2f * u), size = Size(7.4f * u, 7.2f * u))
            drawRect(Color(0xFF8D5A35), topLeft = Offset(1.7f * u, 1.6f * u), size = Size(6.6f * u, 6.3f * u))
            repeat(3) { shelf ->
                drawRect(Color(0xFF4A3B30), topLeft = Offset(2.0f * u, (3.0f + shelf * 1.5f) * u), size = Size(6.0f * u, 0.32f * u))
            }
            repeat(5) { index ->
                val color = listOf(Color(0xFF5F8FCB), GroveBerry, Color(0xFFFFD96A), GroveMoss, Color(0xFF8E72D8))[index]
                drawRect(color, topLeft = Offset((2.15f + index * 1.05f) * u, (2.0f + index % 2 * 1.55f) * u), size = Size(0.52f * u, 1.1f * u))
            }
        }
        HouseLayoutCellKind.COUNTER -> {
            drawRect(Color(0xFF7A4D31), topLeft = Offset(1.2f * u, 4.0f * u), size = Size(7.7f * u, 3.9f * u))
            drawRect(Color(0xFFFFE0A3), topLeft = Offset(0.9f * u, 3.35f * u), size = Size(8.2f * u, 1.2f * u))
            drawRect(Color(0xFFBDE7E7), topLeft = Offset(5.9f * u, 2.25f * u), size = Size(1.4f * u, 1.0f * u))
            drawRect(Color(0xFF8D5A35), topLeft = Offset(2.1f * u, 5.1f * u), size = Size(1.4f * u, 1.5f * u))
            drawRect(Color(0xFF8D5A35), topLeft = Offset(4.1f * u, 5.1f * u), size = Size(1.4f * u, 1.5f * u))
        }
        HouseLayoutCellKind.HEARTH -> {
            drawRect(Color(0xFF7A4D31), topLeft = Offset(1.1f * u, 2.0f * u), size = Size(7.8f * u, 6.1f * u))
            drawRect(Color(0xFF40332F), topLeft = Offset(2.6f * u, 3.3f * u), size = Size(4.8f * u, 3.4f * u))
            drawRect(Color(0xFFFFA755), topLeft = Offset(4.0f * u, 4.2f * u), size = Size(1.6f * u, 2.0f * u))
            drawRect(Color(0xFFFFD96A), topLeft = Offset(4.5f * u, 3.6f * u), size = Size(0.9f * u, 1.0f * u))
            drawRect(Color(0xFFB97948), topLeft = Offset(1.8f * u, 1.45f * u), size = Size(6.5f * u, 0.65f * u))
        }
        HouseLayoutCellKind.WARDROBE -> {
            if (cell.label.contains("storage", ignoreCase = true)) {
                drawRect(Color(0xFF4A3328), topLeft = Offset(1.5f * u, 4.5f * u), size = Size(7.0f * u, 3.7f * u))
                drawRect(Color(0xFF8D5A35), topLeft = Offset(1.9f * u, 3.7f * u), size = Size(6.2f * u, 4.0f * u))
                drawRect(Color(0xFFB97948), topLeft = Offset(2.3f * u, 4.2f * u), size = Size(5.4f * u, 2.7f * u))
                drawRect(Color(0xFFFFD96A), topLeft = Offset(4.5f * u, 5.1f * u), size = Size(1.0f * u, 0.7f * u))
                drawRect(Color(0xFF6B513D), topLeft = Offset(1.8f * u, 3.45f * u), size = Size(6.4f * u, 0.55f * u))
            } else {
                drawRect(Color(0xFF6B513D), topLeft = Offset(1.8f * u, 1.0f * u), size = Size(6.4f * u, 7.9f * u))
                drawRect(Color(0xFF9B6942), topLeft = Offset(2.25f * u, 1.45f * u), size = Size(5.5f * u, 7.0f * u))
                drawRect(Color(0xFF7A4D31), topLeft = Offset(4.75f * u, 1.7f * u), size = Size(0.38f * u, 6.3f * u))
                drawRect(Color(0xFFFFD96A), topLeft = Offset(4.1f * u, 4.7f * u), size = Size(0.42f * u, 0.42f * u))
                drawRect(Color(0xFFFFD96A), topLeft = Offset(5.55f * u, 4.7f * u), size = Size(0.42f * u, 0.42f * u))
            }
        }
        HouseLayoutCellKind.ROOF_BEAM -> {
            drawRect(Color(0xFF4A3B30), topLeft = Offset(0.8f * u, 2.5f * u), size = Size(8.4f * u, 1.4f * u))
            drawRect(Color(0xFF7A4D31), topLeft = Offset(1.2f * u, 2.9f * u), size = Size(7.6f * u, 0.55f * u))
            drawRect(Color(0xFF95613E), topLeft = Offset(2.1f * u, 4.0f * u), size = Size(0.65f * u, 3.4f * u))
            drawRect(Color(0xFF95613E), topLeft = Offset(7.0f * u, 4.0f * u), size = Size(0.65f * u, 3.4f * u))
        }
        HouseLayoutCellKind.RAILING -> {
            drawRect(Color(0xFF6C8F89), topLeft = Offset(0.8f * u, 5.9f * u), size = Size(8.4f * u, 0.7f * u))
            drawRect(Color(0xFF6C8F89), topLeft = Offset(0.8f * u, 7.4f * u), size = Size(8.4f * u, 0.7f * u))
            repeat(4) { index ->
                drawRect(Color(0xFF557D83), topLeft = Offset((1.3f + index * 2.0f) * u, 4.3f * u), size = Size(0.45f * u, 3.7f * u))
            }
        }
        HouseLayoutCellKind.PLANTER -> {
            drawRect(Color(0xFF7A4D31), topLeft = Offset(1.3f * u, 5.8f * u), size = Size(7.3f * u, 2.0f * u))
            drawRect(Color(0xFF4A3B30), topLeft = Offset(1.8f * u, 5.3f * u), size = Size(6.3f * u, 0.7f * u))
            repeat(4) { index ->
                drawRect(GroveMoss.lit(index * 0.04f), topLeft = Offset((2.0f + index * 1.4f) * u, (3.6f + index % 2 * 0.35f) * u), size = Size(0.9f * u, 1.7f * u))
                drawRect(Color(0xFFFFD96A), topLeft = Offset((2.15f + index * 1.4f) * u, 3.15f * u), size = Size(0.55f * u, 0.55f * u))
            }
        }
    }
}

private fun DrawScope.drawPortal(transition: HouseTransition, unlocked: Boolean) {
    val u = size.minDimension / 10f
    val ink = Color(0xFF3E2F2B)
    val wood = if (unlocked) Color(0xFF8D5A35) else Color(0xFF8C7D71)
    val glow = if (unlocked) Color(0xFFFFF2B6) else Color(0xFFBBAFA6)

    when (transition.kind) {
        HousePortalKind.DOOR,
        HousePortalKind.BALCONY_DOOR -> {
            drawRect(ink.copy(alpha = 0.24f), topLeft = Offset(2.0f * u, 2.0f * u), size = Size(6.0f * u, 7.0f * u))
            drawRect(wood, topLeft = Offset(2.6f * u, 2.6f * u), size = Size(4.8f * u, 5.9f * u))
            drawRect(wood.lit(0.18f), topLeft = Offset(3.2f * u, 3.1f * u), size = Size(3.6f * u, 4.8f * u))
            drawRect(glow, topLeft = Offset(6.1f * u, 5.0f * u), size = Size(0.55f * u, 0.55f * u))
            if (transition.kind == HousePortalKind.BALCONY_DOOR) {
                drawRect(Color(0xFF7A9BA0), topLeft = Offset(2.4f * u, 7.6f * u), size = Size(5.2f * u, 0.45f * u))
                repeat(3) { index ->
                    drawRect(Color(0xFF7A9BA0), topLeft = Offset((3.0f + index * 1.5f) * u, 6.2f * u), size = Size(0.35f * u, 1.7f * u))
                }
            }
        }
        HousePortalKind.STAIRS_UP,
        HousePortalKind.STAIRS_DOWN -> {
            val stepColor = if (unlocked) Color(0xFF7A4D31) else Color(0xFF8C7D71)
            repeat(5) { index ->
                val width = (2.2f + index * 1.1f) * u
                val left = (5f * u) - width / 2f
                drawRect(
                    stepColor.lit(index * 0.05f),
                    topLeft = Offset(left, (2.1f + index * 1.05f) * u),
                    size = Size(width, 0.62f * u)
                )
            }
            val arrowTop = if (transition.kind == HousePortalKind.STAIRS_UP) 1.2f else 7.4f
            drawRect(glow, topLeft = Offset(4.4f * u, arrowTop * u), size = Size(1.2f * u, 0.7f * u))
            if (transition.kind == HousePortalKind.STAIRS_UP) {
                drawRect(glow, topLeft = Offset(4.75f * u, 0.8f * u), size = Size(0.5f * u, 1.3f * u))
            } else {
                drawRect(glow, topLeft = Offset(4.75f * u, 7.2f * u), size = Size(0.5f * u, 1.3f * u))
            }
        }
    }

    if (!unlocked) {
        drawRect(Color(0xFF40332F).copy(alpha = 0.42f), topLeft = Offset(1.6f * u, 1.5f * u), size = Size(6.8f * u, 7.2f * u))
        drawRect(Color(0xFFFFF2D1), topLeft = Offset(3.7f * u, 4.0f * u), size = Size(2.6f * u, 2.1f * u))
        drawRect(Color(0xFF40332F), topLeft = Offset(4.25f * u, 3.2f * u), size = Size(1.5f * u, 1.2f * u))
    }
}

private fun Color.lit(amount: Float): Color =
    Color(
        red = (red + amount).coerceIn(0f, 1f),
        green = (green + amount).coerceIn(0f, 1f),
        blue = (blue + amount).coerceIn(0f, 1f),
        alpha = alpha
    )
