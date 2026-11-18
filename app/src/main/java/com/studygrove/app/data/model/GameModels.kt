package com.studygrove.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class PlayerState(
    val coins: Int = 35,
    val xp: Int = 0,
    val level: Int = 1,
    val totalStudyMinutes: Int = 0,
    val completedSessions: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastStudyDate: String? = null,
    val purchasedCounts: Map<String, Int> = emptyMap(),
    val gardenItemsBought: Int = 0,
    val furnitureItemsBought: Int = 0
)

@Serializable
data class StudySession(
    val id: String,
    val preset: TimerPreset,
    val plannedMinutes: Int,
    val completedMinutes: Int,
    val completed: Boolean,
    val startedAtEpochMillis: Long,
    val completedAtEpochMillis: Long,
    val coinsEarned: Int,
    val xpEarned: Int,
    val streakAfterCompletion: Int,
    val localDate: String
)

@Serializable
data class ShopItem(
    val id: String,
    val name: String,
    val description: String,
    val category: ShopCategory,
    val price: Int,
    val unlockLevel: Int,
    val placementTarget: PlacementTarget,
    val gardenObjectType: GardenObjectType? = null,
    val houseObjectType: HouseObjectType? = null,
    val growthSessions: Int = 0,
    val harvestCoins: Int = 0,
    val upgradeKind: UpgradeKind? = null,
    val cosmeticSlot: CosmeticSlot? = null,
    val visualKey: String = id,
    val maxPurchases: Int? = null
)

@Serializable
data class InventoryItem(
    val shopItemId: String,
    val quantity: Int = 0,
    val firstPurchasedAtEpochMillis: Long = 0L
)

@Serializable
data class GardenTile(
    val x: Int,
    val y: Int,
    val terrain: String = "grass"
)

@Serializable
data class GardenObject(
    val id: String,
    val shopItemId: String,
    val x: Int,
    val y: Int,
    val type: GardenObjectType,
    val growthStage: Int = 0,
    val sessionsRemaining: Int = 0,
    val placedAtEpochMillis: Long = 0L
)

@Serializable
data class GardenState(
    val width: Int = 8,
    val height: Int = 8,
    val tiles: List<GardenTile> = emptyList(),
    val objects: List<GardenObject> = emptyList()
)

data class GardenObjectFootprint(
    val width: Int,
    val height: Int
)

object GardenObjectFootprints {
    private val SingleTile = GardenObjectFootprint(width = 1, height = 1)
    private val LargeFeature = GardenObjectFootprint(width = 2, height = 2)

    fun sizeFor(item: ShopItem?): GardenObjectFootprint {
        val key = item?.visualKey.orEmpty()
        val id = item?.id.orEmpty()
        return when {
            key.contains("pond") -> LargeFeature
            key.contains("fountain") -> LargeFeature
            key.contains("garden_arch") || id.contains("garden_arch") -> LargeFeature
            else -> SingleTile
        }
    }

    fun isLarge(item: ShopItem?): Boolean = sizeFor(item) != SingleTile

    fun occupiedTiles(anchorX: Int, anchorY: Int, footprint: GardenObjectFootprint): List<Pair<Int, Int>> =
        buildList {
            repeat(footprint.height) { yOffset ->
                repeat(footprint.width) { xOffset ->
                    add((anchorX + xOffset) to (anchorY + yOffset))
                }
            }
        }

    fun occupiedTiles(gardenObject: GardenObject, item: ShopItem?): List<Pair<Int, Int>> =
        occupiedTiles(gardenObject.x, gardenObject.y, sizeFor(item))

    fun containsTile(gardenObject: GardenObject, item: ShopItem?, x: Int, y: Int): Boolean =
        occupiedTiles(gardenObject, item).any { it.first == x && it.second == y }
}

@Serializable
data class HouseObject(
    val id: String,
    val shopItemId: String,
    val x: Int,
    val y: Int,
    val type: HouseObjectType,
    val room: HouseRoom = HouseRoom.MAIN,
    val placedAtEpochMillis: Long = 0L,
    val rotationDegrees: Int = 0
)

sealed interface HouseItemEdit {
    data class Rotate(val degrees: Int) : HouseItemEdit
    data object Delete : HouseItemEdit
}

@Serializable
data class HouseState(
    val level: Int = 1,
    val width: Int = 12,
    val height: Int = 7,
    val currentRoom: HouseRoom = HouseRoom.MAIN,
    val unlockedRooms: List<HouseRoom> = HouseRooms.defaultUnlockedRooms,
    val specialUnlocks: List<String> = emptyList(),
    val unlockedThemes: List<String> = emptyList(),
    val disabledThemes: List<String> = emptyList(),
    val objects: List<HouseObject> = emptyList()
) {
    val title: String
        get() = when (level) {
            1 -> "Tiny cottage"
            2 -> "Cozy cottage"
            3 -> "Study cabin"
            4 -> "Scholar's home"
            5 -> "Dream library house"
            6 -> "Reading loft"
            7 -> "Tower study"
            else -> "Grand scholar estate"
        }

    fun comfortScore(room: HouseRoom): Int =
        objects.count { it.room == room } * 5 + if (room in unlockedRooms) 10 else 0
}

data class RoomSize(
    val columns: Int,
    val rows: Int
)

data class HouseRoomSpec(
    val room: HouseRoom,
    val width: Int,
    val height: Int
)

data class HouseTransition(
    val x: Int,
    val y: Int,
    val target: HouseRoom,
    val label: String,
    val kind: HousePortalKind
)

data class HouseLayoutCell(
    val x: Int,
    val y: Int,
    val kind: HouseLayoutCellKind,
    val label: String
)

object HouseRooms {
    val defaultUnlockedRooms = listOf(HouseRoom.MAIN)
    val lateGameRooms = listOf(HouseRoom.LIBRARY, HouseRoom.ATTIC, HouseRoom.BALCONY)

    fun spec(room: HouseRoom): HouseRoomSpec {
        val size = HouseRoomSizes.getSize(room)
        return HouseRoomSpec(room = room, width = size.columns, height = size.rows)
    }

    fun spec(room: HouseRoom, level: Int): HouseRoomSpec {
        return spec(room)
    }

    fun spec(house: HouseState): HouseRoomSpec = spec(house.currentRoom)

    fun transitions(house: HouseState): List<HouseTransition> =
        transitions(house.level, house.currentRoom)

    fun transitions(level: Int, room: HouseRoom): List<HouseTransition> =
        when (room) {
            HouseRoom.MAIN -> listOf(
                HouseTransition(3, 6, HouseRoom.MAIN, "Outside Door", HousePortalKind.DOOR),
                HouseTransition(0, 3, HouseRoom.BEDROOM, "Bedroom", HousePortalKind.DOOR),
                HouseTransition(11, 5, HouseRoom.KITCHEN, "Kitchen", HousePortalKind.DOOR),
                HouseTransition(11, 1, HouseRoom.STUDY, "Study Room", HousePortalKind.DOOR),
                HouseTransition(2, 0, HouseRoom.LIBRARY, "Library", HousePortalKind.STAIRS_UP)
            )
            HouseRoom.BEDROOM -> listOf(
                HouseTransition(4, 5, HouseRoom.MAIN, "Main Room", HousePortalKind.DOOR)
            )
            HouseRoom.STUDY -> listOf(
                HouseTransition(4, 5, HouseRoom.MAIN, "Main Room", HousePortalKind.DOOR)
            )
            HouseRoom.KITCHEN -> listOf(
                HouseTransition(4, 5, HouseRoom.MAIN, "Main Room", HousePortalKind.DOOR)
            )
            HouseRoom.LIBRARY -> listOf(
                HouseTransition(5, 6, HouseRoom.MAIN, "Main Room", HousePortalKind.STAIRS_DOWN),
                HouseTransition(8, 0, HouseRoom.ATTIC, "Attic", HousePortalKind.STAIRS_UP)
            )
            HouseRoom.ATTIC -> listOf(
                HouseTransition(1, 4, HouseRoom.LIBRARY, "Library", HousePortalKind.STAIRS_DOWN),
                HouseTransition(8, 1, HouseRoom.BALCONY, "Balcony", HousePortalKind.BALCONY_DOOR),
                HouseTransition(8, 2, HouseRoom.BALCONY, "Balcony", HousePortalKind.BALCONY_DOOR)
            )
            HouseRoom.BALCONY -> listOf(
                HouseTransition(0, 1, HouseRoom.ATTIC, "Attic", HousePortalKind.BALCONY_DOOR),
                HouseTransition(0, 2, HouseRoom.ATTIC, "Attic", HousePortalKind.BALCONY_DOOR)
            )
        }

    fun transitionAt(house: HouseState, x: Int, y: Int): HouseTransition? =
        transitions(house).firstOrNull { it.x == x && it.y == y }

    fun transitionAt(level: Int, room: HouseRoom, x: Int, y: Int): HouseTransition? =
        transitions(level, room).firstOrNull { it.x == x && it.y == y }

    fun layoutCells(house: HouseState): List<HouseLayoutCell> =
        layoutCells(house.level, house.currentRoom)

    fun layoutCells(level: Int, room: HouseRoom): List<HouseLayoutCell> {
        val spec = spec(room, level)
        val cells = mutableListOf<HouseLayoutCell>()

        fun add(x: Int, y: Int, kind: HouseLayoutCellKind, label: String) {
            val inBounds = x in 0 until spec.width && y in 0 until spec.height
            val isPortal = transitionAt(level, room, x, y) != null
            if (inBounds && !isPortal) {
                cells += HouseLayoutCell(x = x, y = y, kind = kind, label = label)
            }
        }

        fun addAll(
            coordinates: List<Pair<Int, Int>>,
            kind: HouseLayoutCellKind,
            label: String
        ) {
            coordinates.forEach { (x, y) -> add(x, y, kind, label) }
        }

        when (room) {
            HouseRoom.MAIN -> {
                addAll(
                    listOf(11 to 3, 10 to 3, 9 to 3, 8 to 3, 7 to 3, 6 to 3, 6 to 2, 6 to 4, 6 to 5),
                    HouseLayoutCellKind.WALL,
                    "white plaster wall"
                )
            }
            HouseRoom.BEDROOM -> {
                addAll(
                    listOf(3 to 0, 3 to 1, 3 to 2, 3 to 3),
                    HouseLayoutCellKind.WALL,
                    "white plaster wall"
                )
            }
            HouseRoom.STUDY -> {
                addAll(
                    listOf(1 to 1, 2 to 1),
                    HouseLayoutCellKind.COUNTER,
                    "desk counter"
                )
            }
            HouseRoom.KITCHEN -> {
                addAll(
                    listOf(0 to 0, 1 to 0, 2 to 0, 3 to 0, 3 to 1),
                    HouseLayoutCellKind.COUNTER,
                    "prep counter"
                )
            }
            HouseRoom.LIBRARY -> {
                add(5, 0, HouseLayoutCellKind.WINDOW, "window")
            }
            HouseRoom.ATTIC -> {
                addAll(
                    listOf(1 to 0, 2 to 0),
                    HouseLayoutCellKind.WARDROBE,
                    "storage trunk"
                )
                addAll(
                    listOf(8 to 4, 8 to 3, 7 to 4, 7 to 3),
                    HouseLayoutCellKind.WALL,
                    "white plaster wall"
                )
            }
            HouseRoom.BALCONY -> {
                addAll(
                    listOf(5 to 0, 5 to 1),
                    HouseLayoutCellKind.RAILING,
                    "inner balcony rail"
                )
            }
        }

        return cells.distinctBy { it.x to it.y }
    }

    fun layoutCellAt(house: HouseState, x: Int, y: Int): HouseLayoutCell? =
        layoutCells(house).firstOrNull { it.x == x && it.y == y }

    fun layoutCellAt(level: Int, room: HouseRoom, x: Int, y: Int): HouseLayoutCell? =
        layoutCells(level, room).firstOrNull { it.x == x && it.y == y }
}

object HouseRoomSizes {
    fun getSize(room: HouseRoom): RoomSize =
        when (room) {
            HouseRoom.MAIN -> RoomSize(columns = 12, rows = 7)
            HouseRoom.BEDROOM -> RoomSize(columns = 9, rows = 6)
            HouseRoom.STUDY -> RoomSize(columns = 9, rows = 6)
            HouseRoom.KITCHEN -> RoomSize(columns = 9, rows = 6)
            HouseRoom.LIBRARY -> RoomSize(columns = 11, rows = 7)
            HouseRoom.ATTIC -> RoomSize(columns = 9, rows = 5)
            HouseRoom.BALCONY -> RoomSize(columns = 9, rows = 4)
        }
}

object HouseSpecialUnlocks {
    const val UnlockAllRooms = "unlock_all_rooms"
    const val DreamLibraryTheme = "dream_library_theme"
    const val CelestialTheme = "celestial_theme"
    const val GrandEstateTheme = "grand_estate_theme"
}

object HouseRoomUnlocks {
    fun getUnlockedRooms(houseLevel: Int, specialUnlocks: Set<String> = emptySet()): List<HouseRoom> {
        if (HouseSpecialUnlocks.UnlockAllRooms in specialUnlocks) {
            return HouseRoom.entries.toList()
        }

        val safeLevel = houseLevel.coerceIn(1, 8)
        val rooms = mutableListOf(HouseRoom.MAIN)
        if (safeLevel >= 2) rooms += HouseRoom.BEDROOM
        if (safeLevel >= 3) rooms += HouseRoom.STUDY
        if (safeLevel >= 4) rooms += HouseRoom.KITCHEN
        if (safeLevel >= 5) rooms += HouseRoom.LIBRARY
        if (safeLevel >= 6) rooms += HouseRoom.ATTIC
        if (safeLevel >= 7) rooms += HouseRoom.BALCONY
        return rooms.distinct()
    }

    fun isRoomUnlocked(
        room: HouseRoom,
        houseLevel: Int,
        specialUnlocks: Set<String> = emptySet()
    ): Boolean = room in getUnlockedRooms(houseLevel, specialUnlocks)
}

@Serializable
data class CharacterCustomization(
    val skinTone: String = "warm",
    val hairStyle: String = "sprout",
    val shirt: String = "moss",
    val hat: String? = null,
    val backpack: String? = null,
    val accessory: String? = null,
    val ownedCosmetics: List<String> = emptyList()
)

@Serializable
data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val unlocked: Boolean = false,
    val unlockedAtEpochMillis: Long? = null
)

@Serializable
data class DailyQuest(
    val id: String,
    val title: String,
    val description: String,
    val target: Int,
    val progress: Int = 0,
    val rewardCoins: Int,
    val completed: Boolean = false,
    val rewarded: Boolean = false
)

@Serializable
data class DailyQuestState(
    val localDate: String = "",
    val quests: List<DailyQuest> = emptyList()
)

@Serializable
data class SettingsState(
    val soundEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val defaultTimerMinutes: Int = 25,
    val gentleRemindersEnabled: Boolean = false,
    val testModeEnabled: Boolean = false,
    val themeSong: String = "theme_1"
)

@Serializable
data class UpgradeState(
    val ownedUpgradeIds: List<String> = emptyList(),
    val focusCoinBonusPercent: Int = 0,
    val harvestBonusPercent: Int = 0,
    val dailyQuestBonusCoins: Int = 0,
    val cropGrowthSessionReduction: Int = 0,
    val greenhouseTier: Int = 0,
    val visualTheme: String? = null
)

@Serializable
data class ActiveTimerState(
    val preset: TimerPreset,
    val durationMinutes: Int,
    val startedAtEpochMillis: Long,
    val accumulatedPausedMillis: Long = 0L,
    val pauseStartedAtEpochMillis: Long? = null,
    val pauseCount: Int = 0
) {
    val isPaused: Boolean
        get() = pauseStartedAtEpochMillis != null
}

@Serializable
data class GameData(
    val player: PlayerState = PlayerState(),
    val inventory: List<InventoryItem> = emptyList(),
    val garden: GardenState = GardenState(),
    val house: HouseState = HouseState(),
    val character: CharacterCustomization = CharacterCustomization(),
    val upgrades: UpgradeState = UpgradeState(),
    val achievements: List<Achievement> = emptyList(),
    val sessions: List<StudySession> = emptyList(),
    val activeTimer: ActiveTimerState? = null,
    val settings: SettingsState = SettingsState(),
    val dailyQuests: DailyQuestState = DailyQuestState()
) {
    companion object {
        fun default() = GameData()
    }
}

data class StudyReward(
    val baseCoins: Int,
    val fullSessionBonus: Int,
    val streakBonus: Int,
    val questBonus: Int,
    val harvestBonus: Int,
    val upgradeBonus: Int,
    val totalCoins: Int,
    val xpEarned: Int,
    val newLevel: Int,
    val newStreak: Int,
    val message: String
)

data class PurchaseResult(
    val success: Boolean,
    val message: String,
    val purchasedItem: ShopItem? = null
)

data class PlacementResult(
    val success: Boolean,
    val message: String
)

data class CancelTimerResult(
    val elapsedMinutes: Int,
    val message: String
)
