package com.studygrove.app.data.model

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

private const val StartingCoins = 35
private const val FormerTestingCoinFloor = 1_000_000

fun GameData.normalized(
    nowMillis: Long = System.currentTimeMillis(),
    zoneId: ZoneId = ZoneId.systemDefault()
): GameData {
    val today = Instant.ofEpochMilli(nowMillis).atZone(zoneId).toLocalDate().toString()
    val normalizedGarden = garden.normalized()
    val normalizedAchievements = achievements.normalizedAchievements()
    val normalizedQuests = if (dailyQuests.localDate == today && dailyQuests.quests.isNotEmpty()) {
        dailyQuests
    } else {
        DailyQuestCatalog.questsFor(today)
    }
    val normalizedCoins = when {
        !settings.testModeEnabled && player.coins >= FormerTestingCoinFloor -> StartingCoins
        else -> player.coins.coerceAtLeast(0)
    }
    val normalizedPlayer = player.copy(
        coins = normalizedCoins,
        xp = player.xp.coerceAtLeast(0),
        level = player.level.coerceAtLeast(1),
        totalStudyMinutes = player.totalStudyMinutes.coerceAtLeast(0),
        completedSessions = player.completedSessions.coerceAtLeast(0),
        currentStreak = player.currentStreak.coerceAtLeast(0),
        longestStreak = player.longestStreak.coerceAtLeast(player.currentStreak.coerceAtLeast(0))
    )
    val savedUpgradeIds = upgrades.ownedUpgradeIds.distinct()
    val knownUpgradeIds = ShopCatalog.items
        .filter { it.category == ShopCategory.UPGRADES || it.upgradeKind != null }
        .map { it.id }
        .toSet()
    val normalizedUpgrades = upgrades.copy(
        ownedUpgradeIds = savedUpgradeIds.filter { it in knownUpgradeIds }.distinct(),
        focusCoinBonusPercent = upgrades.focusCoinBonusPercent.coerceIn(0, 60),
        harvestBonusPercent = upgrades.harvestBonusPercent.coerceIn(0, 75),
        dailyQuestBonusCoins = upgrades.dailyQuestBonusCoins.coerceIn(0, 80),
        cropGrowthSessionReduction = upgrades.cropGrowthSessionReduction.coerceIn(0, 2),
        greenhouseTier = upgrades.greenhouseTier.coerceIn(0, 6)
    )
    val normalizedInventory = inventory.filter { it.quantity > 0 && ShopCatalog.byId(it.shopItemId) != null }
    val migratedHouse = house
        .withOwnedHouseUpgradeEffects(savedUpgradeIds)
        .migrateToFixedRoomSizes(normalizedInventory)
    return copy(
        player = normalizedPlayer,
        inventory = migratedHouse.inventory,
        garden = normalizedGarden,
        house = migratedHouse.house,
        upgrades = normalizedUpgrades,
        achievements = normalizedAchievements,
        dailyQuests = normalizedQuests,
        settings = settings.normalized(),
        sessions = sessions.sortedByDescending { it.completedAtEpochMillis }.take(365)
    )
}

private fun SettingsState.normalized(): SettingsState =
    copy(
        defaultTimerMinutes = defaultTimerMinutes.coerceIn(5, 180),
        themeSong = if (themeSong in setOf("theme_1", "theme_2", "theme_3")) themeSong else "theme_1"
    )

private fun HouseState.withOwnedHouseUpgradeEffects(ownedUpgradeIds: List<String>): HouseState {
    val owned = ownedUpgradeIds.toSet()
    val specialFromUpgrades = mutableListOf<String>()
    val themesFromUpgrades = mutableListOf<String>()
    if ("upgrade_dream_library_wing" in owned) {
        specialFromUpgrades += HouseSpecialUnlocks.UnlockAllRooms
        specialFromUpgrades += HouseSpecialUnlocks.DreamLibraryTheme
        themesFromUpgrades += HouseSpecialUnlocks.DreamLibraryTheme
    }
    if ("upgrade_celestial_observatory" in owned) {
        specialFromUpgrades += HouseSpecialUnlocks.UnlockAllRooms
        specialFromUpgrades += HouseSpecialUnlocks.CelestialTheme
        themesFromUpgrades += HouseSpecialUnlocks.CelestialTheme
    }
    if ("upgrade_grand_scholar_estate" in owned) {
        specialFromUpgrades += HouseSpecialUnlocks.UnlockAllRooms
        specialFromUpgrades += HouseSpecialUnlocks.GrandEstateTheme
        themesFromUpgrades += HouseSpecialUnlocks.GrandEstateTheme
    }

    val legacyRoomLevel = when {
        "upgrade_house_room_4" in owned || HouseRoom.BALCONY in unlockedRooms -> 7
        "upgrade_house_room_3" in owned || HouseRoom.ATTIC in unlockedRooms -> 6
        "upgrade_house_room_2" in owned || HouseRoom.LIBRARY in unlockedRooms -> 5
        else -> level
    }
    val levelFromUpgrades = when {
        "upgrade_grand_scholar_estate" in owned ||
            "upgrade_celestial_observatory" in owned ||
            "upgrade_dream_library_wing" in owned -> 8
        "upgrade_house_7" in owned -> 7
        "upgrade_house_6" in owned -> 6
        "upgrade_house_5" in owned -> 5
        "upgrade_house_4" in owned -> 4
        "upgrade_house_3" in owned -> 3
        "upgrade_house_2" in owned -> 2
        else -> legacyRoomLevel
    }

    return copy(
        level = maxOf(level, levelFromUpgrades),
        specialUnlocks = (specialUnlocks + specialFromUpgrades).distinct(),
        unlockedThemes = (unlockedThemes + themesFromUpgrades).distinct()
    )
}

fun GardenState.normalized(): GardenState {
    val safeWidth = width.coerceIn(8, 30)
    val safeHeight = height.coerceIn(8, 30)
    val knownItems = ShopCatalog.placeableGardenItems()
    val occupiedTiles = mutableSetOf<Pair<Int, Int>>()
    val safeObjects = objects.mapNotNull { gardenObject ->
        val item = ShopCatalog.byId(gardenObject.shopItemId)
        val objectTiles = GardenObjectFootprints.occupiedTiles(gardenObject, item)
        val isValid = gardenObject.shopItemId in knownItems &&
            objectTiles.all { (x, y) -> x in 0 until safeWidth && y in 0 until safeHeight } &&
            objectTiles.none { it in occupiedTiles }
        if (isValid) {
            occupiedTiles += objectTiles
            gardenObject
        } else {
            null
        }
    }
    return copy(
        width = safeWidth,
        height = safeHeight,
        tiles = buildTiles(safeWidth, safeHeight),
        objects = safeObjects
    )
}

fun HouseState.normalized(): HouseState {
    return migrateHouseLayoutsToFixedRoomSizes(this)
}

fun migrateHouseLayoutsToFixedRoomSizes(houseState: HouseState): HouseState =
    houseState.migrateToFixedRoomSizes(emptyList()).house

private data class HouseMigrationResult(
    val house: HouseState,
    val inventory: List<InventoryItem>
)

private fun HouseState.migrateToFixedRoomSizes(existingInventory: List<InventoryItem>): HouseMigrationResult {
    val safeLevel = level.coerceIn(1, 8)
    val safeSpecialUnlocks = specialUnlocks.filter { it.isNotBlank() }.distinct()
    val safeUnlockedThemes = unlockedThemes.filter { it.isNotBlank() }.distinct()
    val safeDisabledThemes = disabledThemes.filter { it in safeUnlockedThemes }.distinct()
    val safeUnlockedRooms = HouseRoomUnlocks.getUnlockedRooms(safeLevel, safeSpecialUnlocks.toSet())
    val safeCurrentRoom = if (currentRoom in safeUnlockedRooms) currentRoom else HouseRoom.MAIN
    val currentSpec = HouseRooms.spec(safeCurrentRoom)
    val knownHouseItems = ShopCatalog.placeableHouseItems()
    val occupiedTiles = mutableSetOf<Triple<HouseRoom, Int, Int>>()
    val keptObjects = mutableListOf<HouseObject>()
    val returnedObjects = mutableListOf<HouseObject>()

    objects.forEach { houseObject ->
        val spec = HouseRooms.spec(houseObject.room)
        val tile = Triple(houseObject.room, houseObject.x, houseObject.y)
        val isKnownItem = houseObject.shopItemId in knownHouseItems
        val isValidPlacement = isKnownItem &&
            houseObject.room in safeUnlockedRooms &&
            houseObject.x in 0 until spec.width &&
            houseObject.y in 0 until spec.height &&
            HouseRooms.transitionAt(safeLevel, houseObject.room, houseObject.x, houseObject.y) == null &&
            HouseRooms.layoutCellAt(safeLevel, houseObject.room, houseObject.x, houseObject.y) == null &&
            tile !in occupiedTiles

        if (isValidPlacement) {
            occupiedTiles += tile
            val angle = furnitureAngle(houseObject.rotationDegrees)
            keptObjects += if (angle == houseObject.rotationDegrees) houseObject else houseObject.copy(rotationDegrees = angle)
        } else if (isKnownItem) {
            returnedObjects += houseObject
        }
    }

    val migratedHouse = copy(
        level = safeLevel,
        width = currentSpec.width,
        height = currentSpec.height,
        currentRoom = safeCurrentRoom,
        unlockedRooms = safeUnlockedRooms,
        specialUnlocks = safeSpecialUnlocks,
        unlockedThemes = safeUnlockedThemes,
        disabledThemes = safeDisabledThemes,
        objects = keptObjects
    )
    return HouseMigrationResult(
        house = migratedHouse,
        inventory = existingInventory.addReturnedHouseObjects(returnedObjects)
    )
}

private fun List<InventoryItem>.addReturnedHouseObjects(returnedObjects: List<HouseObject>): List<InventoryItem> {
    if (returnedObjects.isEmpty()) return this
    val returnedCounts = returnedObjects
        .groupBy { it.shopItemId }
        .mapValues { entry -> entry.value.size }
    val firstReturnedAt = returnedObjects
        .groupBy { it.shopItemId }
        .mapValues { entry -> entry.value.minOf { it.placedAtEpochMillis } }
    val existingIds = map { it.shopItemId }.toSet()
    val updatedExisting = map { item ->
        val returnedQuantity = returnedCounts[item.shopItemId] ?: 0
        item.copy(quantity = item.quantity + returnedQuantity)
    }
    val newReturnedItems = returnedCounts
        .filterKeys { it !in existingIds }
        .map { (itemId, quantity) ->
            InventoryItem(
                shopItemId = itemId,
                quantity = quantity,
                firstPurchasedAtEpochMillis = firstReturnedAt[itemId] ?: 0L
            )
        }
    return (updatedExisting + newReturnedItems).filter { it.quantity > 0 }
}

fun buildTiles(width: Int, height: Int): List<GardenTile> =
    (0 until height).flatMap { y ->
        (0 until width).map { x ->
            val terrain = if ((x + y) % 5 == 0) "clover" else "grass"
            GardenTile(x = x, y = y, terrain = terrain)
        }
    }

private fun List<Achievement>.normalizedAchievements(): List<Achievement> {
    val savedById = associateBy { it.id }
    return AchievementCatalog.achievements.map { definition ->
        val saved = savedById[definition.id]
        if (saved == null) {
            definition
        } else {
            definition.copy(
                unlocked = saved.unlocked,
                unlockedAtEpochMillis = saved.unlockedAtEpochMillis
            )
        }
    }
}

fun todayString(
    nowMillis: Long = System.currentTimeMillis(),
    zoneId: ZoneId = ZoneId.systemDefault()
): String = Instant.ofEpochMilli(nowMillis).atZone(zoneId).toLocalDate().toString()

fun yesterdayString(
    nowMillis: Long = System.currentTimeMillis(),
    zoneId: ZoneId = ZoneId.systemDefault()
): String = LocalDate.parse(todayString(nowMillis, zoneId)).minusDays(1).toString()
