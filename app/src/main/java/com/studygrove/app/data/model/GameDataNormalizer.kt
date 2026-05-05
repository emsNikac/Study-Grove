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
    val normalizedHouse = house.normalized()
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
    val knownUpgradeIds = ShopCatalog.items
        .filter { it.category == ShopCategory.UPGRADES || it.upgradeKind != null }
        .map { it.id }
        .toSet()
    val normalizedUpgrades = upgrades.copy(
        ownedUpgradeIds = upgrades.ownedUpgradeIds.filter { it in knownUpgradeIds }.distinct(),
        focusCoinBonusPercent = upgrades.focusCoinBonusPercent.coerceIn(0, 60),
        harvestBonusPercent = upgrades.harvestBonusPercent.coerceIn(0, 75),
        dailyQuestBonusCoins = upgrades.dailyQuestBonusCoins.coerceIn(0, 80),
        cropGrowthSessionReduction = upgrades.cropGrowthSessionReduction.coerceIn(0, 2),
        greenhouseTier = upgrades.greenhouseTier.coerceIn(0, 6)
    )
    return copy(
        player = normalizedPlayer,
        inventory = inventory.filter { it.quantity > 0 && ShopCatalog.byId(it.shopItemId) != null },
        garden = normalizedGarden,
        house = normalizedHouse,
        upgrades = normalizedUpgrades,
        achievements = normalizedAchievements,
        dailyQuests = normalizedQuests,
        sessions = sessions.sortedByDescending { it.completedAtEpochMillis }.take(365)
    )
}

fun GardenState.normalized(): GardenState {
    val safeWidth = width.coerceIn(8, 30)
    val safeHeight = height.coerceIn(8, 30)
    val knownItems = ShopCatalog.placeableGardenItems()
    val safeObjects = objects
        .filter { it.x in 0 until safeWidth && it.y in 0 until safeHeight && it.shopItemId in knownItems }
        .distinctBy { it.x to it.y }
    return copy(
        width = safeWidth,
        height = safeHeight,
        tiles = buildTiles(safeWidth, safeHeight),
        objects = safeObjects
    )
}

fun HouseState.normalized(): HouseState {
    val safeLevel = level.coerceIn(1, 8)
    val safeWidth = (5 + safeLevel).coerceIn(6, 13)
    val safeHeight = (4 + (safeLevel / 2)).coerceIn(5, 8)
    val knownItems = ShopCatalog.placeableHouseItems()
    val safeObjects = objects
        .filter { it.x in 0 until safeWidth && it.y in 0 until safeHeight && it.shopItemId in knownItems }
        .distinctBy { it.x to it.y }
    return copy(level = safeLevel, width = safeWidth, height = safeHeight, objects = safeObjects)
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
