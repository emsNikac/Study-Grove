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
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.studygrove.app.data.model.CharacterCustomization
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
import com.studygrove.app.ui.components.AvatarFacing
import com.studygrove.app.ui.components.connectionMask
import com.studygrove.app.ui.components.CharacterSprite
import com.studygrove.app.ui.components.MapTileGrid
import com.studygrove.app.ui.components.PixelSheet
import com.studygrove.app.ui.components.PixelTile
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
    var facing by remember { mutableStateOf(AvatarFacing.Front) }
    var walkFrame by remember { mutableIntStateOf(0) }
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

    val walking = joystickVector != Offset.Zero
    LaunchedEffect(gameData.house.currentRoom, walking) {
        walkFrame = 0
        if (!walking) return@LaunchedEffect
        var stride = 0f
        var lastFrameNanos = withFrameNanos { it }
        while (true) {
            val frameNanos = withFrameNanos { it }
            val deltaSeconds = ((frameNanos - lastFrameNanos) / 1_000_000_000f).coerceAtMost(0.05f)
            lastFrameNanos = frameNanos

            val vector = latestJoystickVector
            val current = explorerPosition

            val move = resolveExplorerMove(
                gameData = latestGameData,
                current = current,
                dx = vector.x * HouseExplorerSpeedTilesPerSecond * deltaSeconds,
                dy = vector.y * HouseExplorerSpeedTilesPerSecond * deltaSeconds
            )

            if (move == null || (move.portalTile != null && latestSelectedItemId != null)) {
                facing = facing.toward(vector.x, vector.y)
                stride = 0f
                walkFrame = 0
                continue
            }
            explorerPosition = move.position
            if (move.portalTile != null) {
                walkFrame = 0
                joystickVector = Offset.Zero
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                latestOnTileTapped(move.portalTile.first, move.portalTile.second)
                break
            }
            if (move.position.room == current.room) {
                val dx = move.position.x - current.x
                val dy = move.position.y - current.y
                facing = facing.toward(dx, dy)
                stride = (stride + sqrt(dx * dx + dy * dy) / HouseExplorerStrideTiles) % 1f
                walkFrame = 1 + (stride * 8).toInt()
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
            facing = facing,
            walkFrame = walkFrame,
            placing = selectedItemId != null,
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
    facing: AvatarFacing,
    walkFrame: Int,
    placing: Boolean,
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
    val partitionMasks = remember(layoutByTile, objectsByTile) {
        val walls = layoutByTile.filter { (tile, cell) ->
            cell.kind == HouseLayoutCellKind.WALL && tile !in objectsByTile
        }.keys
        walls.associateWith { (x, y) ->
            connectionMask(x, y) { nx, ny -> (nx to ny) in walls }
        }
    }
    ZoomableMapViewport(
        gridWidth = roomSpec.width,
        gridHeight = roomSpec.height,
        panelColor = Color(0xFFC88A58),
        maxScale = 3.5f
    ) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val tiles = MapTileGrid(
                constraints.maxWidth, constraints.maxHeight, roomSpec.width, roomSpec.height, LocalDensity.current
            )
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
                                    .size(tiles.size(x, y))
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onTileTapped(x, y)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                val variant = (x * 3 + y + x * y) % 4
                                PixelTile(PixelSheet.House, currentRoom.materialRow * 4 + variant, Modifier.matchParentSize())
                                val hasWindow = y < 2 && x % 4 == 2 &&
                                    (x to 0) !in layoutByTile && (x to 0) !in transitionsByTile
                                if (hasWindow && currentRoom != HouseRoom.LIBRARY && currentRoom != HouseRoom.BALCONY) {
                                    PixelTile(PixelSheet.House, 45 + y, Modifier.matchParentSize())
                                }
                                if (y == 0) {
                                    val wall = currentRoom.materialRow * 2 + if (hasWindow) 1 else 0
                                    PixelTile(PixelSheet.BackWalls, wall, Modifier.matchParentSize())
                                }
                                when {
                                    transition != null -> HousePortalTile(
                                        transition = transition,
                                        unlocked = transition.target in gameData.house.unlockedRooms,
                                        modifier = Modifier.matchParentSize().padding(2.dp)
                                    )
                                    layoutCell != null -> HouseLayoutTile(
                                        cell = layoutCell,
                                        wallMask = partitionMasks[x to y] ?: 0,
                                        modifier = Modifier.matchParentSize()
                                    )
                                    obj != null -> HouseObjectSprite(obj, Modifier.matchParentSize())
                                }
                                if (placing) {
                                    Box(Modifier.matchParentSize().border(0.5.dp, GroveInk.copy(alpha = 0.28f)))
                                }
                            }
                        }
                    }
                }
            }
            if (explorerPosition.room == currentRoom) {
                HouseExplorerSprite(
                    gameData.character,
                    facing,
                    walkFrame,
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
    character: CharacterCustomization,
    facing: AvatarFacing,
    walkFrame: Int,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Canvas(Modifier.fillMaxSize()) {
            val u = size.minDimension / 10f
            drawOval(
                Color(0xFF2D241F).copy(alpha = 0.24f),
                topLeft = Offset(2.5f * u, 8.7f * u),
                size = Size(5f * u, 0.9f * u)
            )

        }
        CharacterSprite(character, Modifier.fillMaxSize(), facing, walkFrame)
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

private const val HouseExplorerStrideTiles = 1.65f
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
    wallMask: Int,
    modifier: Modifier = Modifier
) {
    val frame = when (cell.kind) {
        HouseLayoutCellKind.WALL -> {
            PixelTile(PixelSheet.Partitions, wallMask, modifier)
            return
        }
        HouseLayoutCellKind.COUNTER -> if (cell.label.contains("desk", ignoreCase = true)) 31 else 30
        HouseLayoutCellKind.WINDOW -> 32
        HouseLayoutCellKind.WARDROBE -> if (cell.label.contains("storage", ignoreCase = true)) 33 else 37
        HouseLayoutCellKind.RAILING -> 34
        HouseLayoutCellKind.SHELF -> 35
        HouseLayoutCellKind.HEARTH -> 36
        HouseLayoutCellKind.ROOF_BEAM -> 38
        HouseLayoutCellKind.PLANTER -> 39
    }
    PixelTile(PixelSheet.House, frame, modifier)
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
    val frame = when (transition.kind) {
        HousePortalKind.DOOR -> 40
        HousePortalKind.BALCONY_DOOR -> 41
        HousePortalKind.STAIRS_UP -> 42
        HousePortalKind.STAIRS_DOWN -> 43
    }
    Box(modifier) {
        PixelTile(PixelSheet.House, frame, Modifier.matchParentSize())
        if (!unlocked) {
            Box(Modifier.matchParentSize().background(Color(0x5547393C)))
            PixelTile(PixelSheet.House, 44, Modifier.matchParentSize())
        }
    }
}

private val HouseRoom.materialRow: Int
    get() = when (this) {
        HouseRoom.MAIN -> 0
        HouseRoom.BEDROOM -> 1
        HouseRoom.STUDY -> 2
        HouseRoom.KITCHEN -> 3
        HouseRoom.LIBRARY -> 4
        HouseRoom.ATTIC -> 5
        HouseRoom.BALCONY -> 6
    }
