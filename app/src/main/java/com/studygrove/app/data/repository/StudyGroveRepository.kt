package com.studygrove.app.data.repository

import com.studygrove.app.data.local.StudyGroveLocalDataSource
import com.studygrove.app.data.model.ActiveTimerState
import com.studygrove.app.data.model.CancelTimerResult
import com.studygrove.app.data.model.CharacterCustomization
import com.studygrove.app.data.model.CosmeticSlot
import com.studygrove.app.data.model.GameData
import com.studygrove.app.data.model.GardenObject
import com.studygrove.app.data.model.GardenObjectFootprints
import com.studygrove.app.data.model.GardenObjectType
import com.studygrove.app.data.model.HouseObject
import com.studygrove.app.data.model.HouseRooms
import com.studygrove.app.data.model.HouseSpecialUnlocks
import com.studygrove.app.data.model.InventoryItem
import com.studygrove.app.data.model.PlacementResult
import com.studygrove.app.data.model.PlacementTarget
import com.studygrove.app.data.model.PurchaseResult
import com.studygrove.app.data.model.ShopCatalog
import com.studygrove.app.data.model.ShopItem
import com.studygrove.app.data.model.StudyReward
import com.studygrove.app.data.model.StudySession
import com.studygrove.app.data.model.TimerPreset
import com.studygrove.app.data.model.UpgradeKind
import com.studygrove.app.data.model.normalized
import com.studygrove.app.data.model.todayString
import com.studygrove.app.data.model.yesterdayString
import com.studygrove.app.domain.achievements.AchievementEngine
import com.studygrove.app.domain.progression.GardenGrowthEngine
import com.studygrove.app.domain.progression.ProgressionRules
import com.studygrove.app.domain.quests.DailyQuestEngine
import com.studygrove.app.domain.rewards.RewardCalculator
import com.studygrove.app.domain.timer.FocusTimerEngine
import kotlinx.coroutines.flow.Flow
import java.time.ZoneId
import kotlin.math.roundToInt

class StudyGroveRepository(
    private val localDataSource: StudyGroveLocalDataSource,
    private val zoneId: ZoneId = ZoneId.systemDefault()
) {
    val gameData: Flow<GameData> = localDataSource.gameData

    suspend fun current(): GameData = localDataSource.current()

    suspend fun startTimer(
        preset: TimerPreset,
        customMinutes: Int?,
        nowMillis: Long = System.currentTimeMillis()
    ): ActiveTimerState? {
        val minutes = (preset.minutes ?: customMinutes ?: 25).coerceIn(5, 180)
        var timer: ActiveTimerState? = null
        localDataSource.update { data ->
            if (data.activeTimer != null) {
                timer = data.activeTimer
                data
            } else {
                val activeTimer = ActiveTimerState(
                    preset = preset,
                    durationMinutes = minutes,
                    startedAtEpochMillis = nowMillis
                )
                timer = activeTimer
                data.copy(activeTimer = activeTimer)
            }
        }
        return timer
    }

    suspend fun togglePause(nowMillis: Long = System.currentTimeMillis()): PlacementResult {
        var result = PlacementResult(false, "No active focus timer.")
        localDataSource.update { data ->
            val timer = data.activeTimer ?: return@update data
            if (timer.isPaused) {
                val pauseStartedAt = timer.pauseStartedAtEpochMillis ?: nowMillis
                val resumed = timer.copy(
                    accumulatedPausedMillis = timer.accumulatedPausedMillis + (nowMillis - pauseStartedAt).coerceAtLeast(0),
                    pauseStartedAtEpochMillis = null
                )
                result = PlacementResult(true, "Timer resumed. Welcome back.")
                data.copy(activeTimer = resumed)
            } else if (timer.pauseCount >= FocusTimerEngine.MaxPauses) {
                result = PlacementResult(false, "This session has used all 3 pauses. Keep it gentle and steady.")
                data
            } else {
                val paused = timer.copy(
                    pauseStartedAtEpochMillis = nowMillis,
                    pauseCount = timer.pauseCount + 1
                )
                result = PlacementResult(true, "Paused. You have ${FocusTimerEngine.MaxPauses - paused.pauseCount} pauses left.")
                data.copy(activeTimer = paused)
            }
        }
        return result
    }

    suspend fun cancelTimer(nowMillis: Long = System.currentTimeMillis()): CancelTimerResult {
        var result = CancelTimerResult(0, "No active timer to cancel.")
        localDataSource.update { data ->
            val timer = data.activeTimer ?: return@update data
            val elapsed = FocusTimerEngine.elapsedWholeMinutes(timer, nowMillis)
            result = CancelTimerResult(
                elapsedMinutes = elapsed,
                message = if (elapsed >= 5) {
                    "No coins this time, but those $elapsed minutes still count as showing up. Try a shorter session next."
                } else {
                    "No coins this time. A tiny restart is still a real restart."
                }
            )
            data.copy(activeTimer = null)
        }
        return result
    }

    suspend fun discardFinishedTimer(): PlacementResult {
        var result = PlacementResult(false, "No finished timer to review.")
        localDataSource.update { data ->
            val timer = data.activeTimer ?: return@update data
            if (!FocusTimerEngine.isComplete(timer, System.currentTimeMillis())) {
                result = PlacementResult(false, "This session is still running.")
                data
            } else {
                result = PlacementResult(true, "No reward counted this time. Thanks for being honest with yourself.")
                data.copy(activeTimer = null)
            }
        }
        return result
    }

    suspend fun completeActiveTimer(nowMillis: Long = System.currentTimeMillis()): StudyReward? {
        var reward: StudyReward? = null
        localDataSource.update { data ->
            val timer = data.activeTimer ?: return@update data
            // Completed timers are the only full reward path. This keeps the loop motivating
            // without punishing cancelled sessions or requiring online checks.
            val today = todayString(nowMillis, zoneId)
            val yesterday = yesterdayString(nowMillis, zoneId)
            val newStreak = RewardCalculator.nextStreak(
                lastStudyDate = data.player.lastStudyDate,
                currentStreak = data.player.currentStreak,
                today = today,
                yesterday = yesterday
            )
            val questResult = DailyQuestEngine.onSessionCompleted(
                state = data.dailyQuests,
                today = today,
                completedMinutes = timer.durationMinutes
            )
            val newXp = data.player.xp + timer.durationMinutes
            val newLevel = ProgressionRules.levelForXp(newXp)
            val calculatedReward = RewardCalculator.calculate(
                minutes = timer.durationMinutes,
                newStreak = newStreak,
                questBonus = questResult.bonusCoins + if (questResult.bonusCoins > 0) data.upgrades.dailyQuestBonusCoins else 0,
                harvestBonus = 0,
                newLevel = newLevel,
                completedSessionsBefore = data.player.completedSessions,
                focusCoinBonusPercent = data.upgrades.focusCoinBonusPercent
            )
            val session = StudySession(
                id = "session_$nowMillis",
                preset = timer.preset,
                plannedMinutes = timer.durationMinutes,
                completedMinutes = timer.durationMinutes,
                completed = true,
                startedAtEpochMillis = timer.startedAtEpochMillis,
                completedAtEpochMillis = nowMillis,
                coinsEarned = calculatedReward.totalCoins,
                xpEarned = calculatedReward.xpEarned,
                streakAfterCompletion = newStreak,
                localDate = today
            )
            val updatedPlayer = data.player.copy(
                coins = data.player.coins + calculatedReward.totalCoins,
                xp = newXp,
                level = newLevel,
                totalStudyMinutes = data.player.totalStudyMinutes + timer.durationMinutes,
                completedSessions = data.player.completedSessions + 1,
                currentStreak = newStreak,
                longestStreak = maxOf(data.player.longestStreak, newStreak),
                lastStudyDate = today
            )
            val updatedData = data.copy(
                player = updatedPlayer,
                garden = GardenGrowthEngine.advanceCrops(data.garden),
                dailyQuests = questResult.state,
                sessions = listOf(session) + data.sessions,
                activeTimer = null
            )
            val withAchievements = updatedData.copy(
                achievements = AchievementEngine.evaluate(updatedData, nowMillis)
            )
            reward = calculatedReward
            withAchievements
        }
        return reward
    }

    suspend fun buyItem(itemId: String, nowMillis: Long = System.currentTimeMillis()): PurchaseResult {
        val item = ShopCatalog.byId(itemId) ?: return PurchaseResult(false, "That shop item is no longer available.")
        var result = PurchaseResult(false, "Could not buy ${item.name}.", item)
        localDataSource.update { data ->
            val ownedCount = data.player.purchasedCounts[item.id] ?: 0
            when {
                data.settings.testModeEnabled -> {
                    val afterPurchase = applyTestPurchase(data, item, nowMillis)
                    result = PurchaseResult(true, "Test mode unlocked ${item.name}.", item)
                    afterPurchase.copy(
                        achievements = AchievementEngine.evaluate(afterPurchase, nowMillis)
                    )
                }
                data.player.level < item.unlockLevel -> {
                    result = PurchaseResult(false, "${item.name} unlocks at level ${item.unlockLevel}.", item)
                    data
                }
                item.maxPurchases != null && ownedCount >= item.maxPurchases -> {
                    result = PurchaseResult(false, "${item.name} is already owned.", item)
                    data
                }
                data.player.coins < item.price -> {
                    result = PurchaseResult(false, "You need ${item.price - data.player.coins} more coins for ${item.name}.", item)
                    data
                }
                else -> {
                    val afterPurchase = applyPurchase(data, item, nowMillis)
                    result = PurchaseResult(true, "Bought ${item.name}.", item)
                    afterPurchase.copy(
                        achievements = AchievementEngine.evaluate(afterPurchase, nowMillis)
                    )
                }
            }
        }
        return result
    }

    suspend fun placeGardenItem(
        itemId: String,
        x: Int,
        y: Int,
        nowMillis: Long = System.currentTimeMillis()
    ): PlacementResult {
        val item = ShopCatalog.byId(itemId) ?: return PlacementResult(false, "Unknown item.")
        if (item.placementTarget != PlacementTarget.GARDEN || item.gardenObjectType == null) {
            return PlacementResult(false, "${item.name} cannot be placed in the garden.")
        }
        var result = PlacementResult(false, "Could not place ${item.name}.")
        localDataSource.update { data ->
            val hasItem = data.settings.testModeEnabled || data.inventory.any { it.shopItemId == item.id && it.quantity > 0 }
            val footprint = GardenObjectFootprints.sizeFor(item)
            val targetTiles = GardenObjectFootprints.occupiedTiles(x, y, footprint)
            val outOfBounds = targetTiles.any { (tileX, tileY) ->
                tileX !in 0 until data.garden.width || tileY !in 0 until data.garden.height
            }
            val occupied = data.garden.objects.any { existing ->
                val existingItem = ShopCatalog.byId(existing.shopItemId)
                GardenObjectFootprints.occupiedTiles(existing, existingItem).any { it in targetTiles }
            }
            when {
                outOfBounds -> {
                    result = PlacementResult(
                        false,
                        if (footprint.width > 1 || footprint.height > 1) {
                            "${item.name} needs a clear ${footprint.width}x${footprint.height} space inside your grove."
                        } else {
                            "That tile is outside your grove."
                        }
                    )
                    data
                }
                occupied -> {
                    result = PlacementResult(false, "That space already has something cozy on it.")
                    data
                }
                !hasItem -> {
                    result = PlacementResult(false, "You do not have ${item.name} in your inventory.")
                    data
                }
                else -> {
                    val growthSessions = if (item.growthSessions > 0) {
                        (item.growthSessions - data.upgrades.cropGrowthSessionReduction).coerceAtLeast(1)
                    } else {
                        0
                    }
                    // Seeds become crop objects with a session counter; every completed study
                    // session advances those counters through GardenGrowthEngine.
                    val newObject = GardenObject(
                        id = "garden_${item.id}_$nowMillis",
                        shopItemId = item.id,
                        x = x,
                        y = y,
                        type = item.gardenObjectType,
                        growthStage = if (growthSessions > 0) 0 else 1,
                        sessionsRemaining = growthSessions,
                        placedAtEpochMillis = nowMillis
                    )
                    val today = todayString(nowMillis, zoneId)
                    val questResult = if (item.gardenObjectType == GardenObjectType.CROP) {
                        DailyQuestEngine.onCropPlanted(data.dailyQuests, today)
                    } else {
                        DailyQuestEngine.ensureToday(data.dailyQuests, today).let { DailyQuestEngine.onSessionCompleted(it, today, 0) }
                    }
                    val questCoins = questResult.bonusCoins + if (questResult.bonusCoins > 0) data.upgrades.dailyQuestBonusCoins else 0
                    val playerWithQuestCoins = data.player.copy(coins = data.player.coins + questCoins)
                    result = PlacementResult(
                        true,
                        if (questCoins > 0) {
                            "Placed ${item.name}. Daily quest complete: +$questCoins coins."
                        } else {
                            "Placed ${item.name}."
                        }
                    )
                    data.copy(
                        player = playerWithQuestCoins,
                        inventory = if (data.settings.testModeEnabled) data.inventory else data.inventory.decrement(item.id),
                        garden = data.garden.copy(objects = data.garden.objects + newObject),
                        dailyQuests = questResult.state
                    )
                }
            }
        }
        return result
    }

    suspend fun interactGardenTile(
        x: Int,
        y: Int,
        nowMillis: Long = System.currentTimeMillis()
    ): PlacementResult {
        var result = PlacementResult(false, "Nothing to pick up here.")
        localDataSource.update { data ->
            val gardenObject = data.garden.objects.firstOrNull { existing ->
                GardenObjectFootprints.containsTile(existing, ShopCatalog.byId(existing.shopItemId), x, y)
            } ?: return@update data
            val item = ShopCatalog.byId(gardenObject.shopItemId) ?: return@update data
            val remainingObjects = data.garden.objects.filterNot { it.id == gardenObject.id }
            if (gardenObject.type == GardenObjectType.CROP && gardenObject.sessionsRemaining == 0) {
                val upgradeBonus = (item.harvestCoins * (data.upgrades.harvestBonusPercent / 100.0)).roundToInt()
                val harvestCoins = item.harvestCoins + upgradeBonus
                result = PlacementResult(true, "Harvested ${item.name.removeSuffix(" Seed")} for $harvestCoins coins.")
                data.copy(
                    player = data.player.copy(coins = data.player.coins + harvestCoins),
                    garden = data.garden.copy(objects = remainingObjects)
                )
            } else {
                result = PlacementResult(
                    true,
                    if (data.settings.testModeEnabled) "Removed ${item.name}." else "Moved ${item.name} back to inventory."
                )
                data.copy(
                    inventory = if (data.settings.testModeEnabled) data.inventory else data.inventory.add(item.id, nowMillis),
                    garden = data.garden.copy(objects = remainingObjects)
                )
            }
        }
        return result
    }

    suspend fun placeHouseItem(
        itemId: String,
        x: Int,
        y: Int,
        nowMillis: Long = System.currentTimeMillis()
    ): PlacementResult {
        val item = ShopCatalog.byId(itemId) ?: return PlacementResult(false, "Unknown item.")
        if (item.placementTarget != PlacementTarget.HOUSE || item.houseObjectType == null) {
            return PlacementResult(false, "${item.name} belongs somewhere else.")
        }
        var result = PlacementResult(false, "Could not place ${item.name}.")
        localDataSource.update { data ->
            val hasItem = data.settings.testModeEnabled || data.inventory.any { it.shopItemId == item.id && it.quantity > 0 }
            val currentRoom = data.house.currentRoom
            val roomSpec = HouseRooms.spec(currentRoom)
            val portal = HouseRooms.transitionAt(data.house, x, y)
            val layoutCell = HouseRooms.layoutCellAt(data.house, x, y)
            val occupied = data.house.objects.any { it.room == currentRoom && it.x == x && it.y == y }
            when {
                x !in 0 until roomSpec.width || y !in 0 until roomSpec.height -> {
                    result = PlacementResult(false, "That spot is outside the room.")
                    data
                }
                portal != null -> {
                    result = PlacementResult(false, "That ${portal.label.lowercase()} path needs to stay open.")
                    data
                }
                layoutCell != null -> {
                    result = PlacementResult(false, "The built-in ${layoutCell.label} helps shape this room.")
                    data
                }
                occupied -> {
                    result = PlacementResult(false, "That room tile is already used.")
                    data
                }
                !hasItem -> {
                    result = PlacementResult(false, "You do not have ${item.name} in your inventory.")
                    data
                }
                else -> {
                    val houseObject = HouseObject(
                        id = "house_${item.id}_$nowMillis",
                        shopItemId = item.id,
                        x = x,
                        y = y,
                        type = item.houseObjectType,
                        room = currentRoom,
                        placedAtEpochMillis = nowMillis
                    )
                    result = PlacementResult(true, "Placed ${item.name}.")
                    data.copy(
                        inventory = if (data.settings.testModeEnabled) data.inventory else data.inventory.decrement(item.id),
                        house = data.house.copy(objects = data.house.objects + houseObject)
                    )
                }
            }
        }
        return result
    }

    suspend fun interactHouseTile(x: Int, y: Int, nowMillis: Long = System.currentTimeMillis()): PlacementResult {
        var result = PlacementResult(false, "That room tile is empty.")
        localDataSource.update { data ->
            val currentRoom = data.house.currentRoom
            val houseObject = data.house.objects.firstOrNull { it.room == currentRoom && it.x == x && it.y == y }
            if (houseObject == null) {
                val portal = HouseRooms.transitionAt(data.house, x, y)
                when {
                    portal == null -> {
                        val layoutCell = HouseRooms.layoutCellAt(data.house, x, y) ?: return@update data
                        result = PlacementResult(true, "That built-in ${layoutCell.label} stays in place.")
                        data
                    }
                    portal.target == currentRoom -> {
                        result = PlacementResult(true, "The ${portal.label.lowercase()} stays open.")
                        data
                    }
                    portal.target !in data.house.unlockedRooms -> {
                        result = PlacementResult(false, "${portal.target.displayName} is locked. Raise your house level to unlock it.")
                        data
                    }
                    else -> {
                        val targetSpec = HouseRooms.spec(portal.target)
                        result = PlacementResult(true, "Entered ${portal.target.displayName}.")
                        data.copy(
                            house = data.house.copy(
                                currentRoom = portal.target,
                                width = targetSpec.width,
                                height = targetSpec.height
                            )
                        )
                    }
                }
            } else {
                val item = ShopCatalog.byId(houseObject.shopItemId) ?: return@update data
                result = PlacementResult(
                    true,
                    if (data.settings.testModeEnabled) "Removed ${item.name}." else "Moved ${item.name} back to inventory."
                )
                data.copy(
                    inventory = if (data.settings.testModeEnabled) data.inventory else data.inventory.add(item.id, nowMillis),
                    house = data.house.copy(objects = data.house.objects.filterNot { it.id == houseObject.id })
                )
            }
        }
        return result
    }

    suspend fun equipCosmetic(itemId: String): PlacementResult {
        val item = ShopCatalog.byId(itemId) ?: return PlacementResult(false, "Unknown cosmetic.")
        if (item.cosmeticSlot == null) return PlacementResult(false, "${item.name} is not wearable.")
        var result = PlacementResult(false, "Buy ${item.name} first.")
        localDataSource.update { data ->
            if (!data.settings.testModeEnabled && item.id !in data.character.ownedCosmetics) {
                data
            } else {
                result = PlacementResult(true, "Equipped ${item.name}.")
                val ownedCosmetics = if (data.settings.testModeEnabled) {
                    (data.character.ownedCosmetics + item.id).distinct()
                } else {
                    data.character.ownedCosmetics
                }
                data.copy(character = data.character.copy(ownedCosmetics = ownedCosmetics).equip(item))
            }
        }
        return result
    }

    suspend fun setHouseThemeEnabled(themeId: String, enabled: Boolean): PlacementResult {
        var result = PlacementResult(false, "That house theme is not unlocked yet.")
        localDataSource.update { data ->
            if (themeId !in data.house.unlockedThemes) {
                data
            } else {
                val disabledThemes = if (enabled) {
                    data.house.disabledThemes - themeId
                } else {
                    (data.house.disabledThemes + themeId).distinct()
                }
                result = PlacementResult(true, if (enabled) "House theme enabled." else "House theme disabled.")
                data.copy(house = data.house.copy(disabledThemes = disabledThemes))
            }
        }
        return result
    }

    suspend fun updateSettings(transform: (GameData) -> GameData): GameData =
        localDataSource.update(transform)

    suspend fun resetProgress(): GameData = localDataSource.reset()

    private fun applyPurchase(data: GameData, item: ShopItem, nowMillis: Long): GameData {
        val count = (data.player.purchasedCounts[item.id] ?: 0) + 1
        val paidPlayer = data.player.copy(
            coins = data.player.coins - item.price,
            purchasedCounts = data.player.purchasedCounts + (item.id to count),
            gardenItemsBought = data.player.gardenItemsBought + if (item.placementTarget == PlacementTarget.GARDEN) 1 else 0,
            furnitureItemsBought = data.player.furnitureItemsBought + if (item.placementTarget == PlacementTarget.HOUSE) 1 else 0
        )
        return when (item.placementTarget) {
            PlacementTarget.GARDEN, PlacementTarget.HOUSE -> data.copy(
                player = paidPlayer,
                inventory = data.inventory.add(item.id, nowMillis)
            )
            PlacementTarget.CHARACTER -> data.copy(
                player = paidPlayer,
                character = data.character.copy(
                    ownedCosmetics = (data.character.ownedCosmetics + item.id).distinct()
                ).equip(item)
            )
            PlacementTarget.UPGRADE -> applyUpgrade(data.copy(player = paidPlayer), item)
        }
    }

    private fun applyTestPurchase(data: GameData, item: ShopItem, nowMillis: Long): GameData =
        when (item.placementTarget) {
            PlacementTarget.GARDEN, PlacementTarget.HOUSE -> data.copy(
                inventory = data.inventory.add(item.id, nowMillis)
            )
            PlacementTarget.CHARACTER -> data.copy(
                character = data.character.copy(
                    ownedCosmetics = (data.character.ownedCosmetics + item.id).distinct()
                ).equip(item)
            )
            PlacementTarget.UPGRADE -> applyUpgrade(data, item)
        }

    private fun applyUpgrade(data: GameData, item: ShopItem): GameData {
        val alreadyOwned = item.id in data.upgrades.ownedUpgradeIds
        val marked = data.copy(
            upgrades = data.upgrades.copy(
                ownedUpgradeIds = (data.upgrades.ownedUpgradeIds + item.id).distinct()
            )
        )
        if (alreadyOwned) return marked
        return applyCatalogUpgradeEffect(marked, item).normalized()
    }

    private fun applyCatalogUpgradeEffect(data: GameData, item: ShopItem): GameData = when {
        item.upgradeKind == UpgradeKind.GARDEN_EXPANSION_10 -> data.withGardenSize(10)
        item.upgradeKind == UpgradeKind.GARDEN_EXPANSION_12 -> data.withGardenSize(12)
        item.upgradeKind == UpgradeKind.GARDEN_EXPANSION_15 -> data.withGardenSize(15)
        item.id == "upgrade_garden_18" -> data.withGardenSize(18)
        item.id == "upgrade_garden_20" -> data.withGardenSize(20)
        item.id == "upgrade_garden_24" -> data.withGardenSize(24)
        item.id == "upgrade_garden_30" -> data.withGardenSize(30)

        item.upgradeKind == UpgradeKind.HOUSE_LEVEL_2 -> data.withHouseLevel(2)
        item.upgradeKind == UpgradeKind.HOUSE_LEVEL_3 -> data.withHouseLevel(3)
        item.upgradeKind == UpgradeKind.HOUSE_LEVEL_4 -> data.withHouseLevel(4)
        item.upgradeKind == UpgradeKind.HOUSE_LEVEL_5 -> data.withHouseLevel(5)
        item.id == "upgrade_house_6" -> data.withHouseLevel(6)
        item.id == "upgrade_house_7" -> data.withHouseLevel(7)
        item.id == "upgrade_dream_library_wing" -> data
            .withHouseLevel(8)
            .withHouseSpecialUnlocks(HouseSpecialUnlocks.UnlockAllRooms, HouseSpecialUnlocks.DreamLibraryTheme)
            .withHouseThemes(HouseSpecialUnlocks.DreamLibraryTheme)
        item.id == "upgrade_grand_scholar_estate" -> data
            .withHouseLevel(8)
            .withHouseSpecialUnlocks(HouseSpecialUnlocks.UnlockAllRooms, HouseSpecialUnlocks.GrandEstateTheme)
            .withHouseThemes(HouseSpecialUnlocks.GrandEstateTheme)

        item.id == "upgrade_crop_watering_bonus" -> data.withUpgradeBonuses(growthReduction = 1, harvestPercent = 2)
        item.id == "upgrade_crop_harvest_bonus_1" -> data.withUpgradeBonuses(harvestPercent = 5)
        item.id == "upgrade_crop_harvest_bonus_2" -> data.withUpgradeBonuses(harvestPercent = 10)
        item.id == "upgrade_master_crop_license" -> data.withUpgradeBonuses(harvestPercent = 5, growthReduction = 1)
        item.id == "upgrade_crop_markers" || item.id == "upgrade_crop_journal" -> data.withUpgradeBonuses(harvestPercent = 2)

        item.id == "upgrade_daily_quest_board" -> data.withUpgradeBonuses(questCoins = 4)
        item.id == "upgrade_advanced_daily_quests" -> data.withUpgradeBonuses(questCoins = 8)
        item.id == "upgrade_weekly_quest_board" -> data.withUpgradeBonuses(questCoins = 12)

        item.id == "upgrade_focus_bell" -> data.withUpgradeBonuses(focusPercent = 2)
        item.id == "upgrade_study_desk_bonus" -> data.withUpgradeBonuses(focusPercent = 4)
        item.id == "upgrade_focus_streak_bonus_2" -> data.withUpgradeBonuses(focusPercent = 5)
        item.id == "upgrade_eternal_study_bonus" -> data.withUpgradeBonuses(focusPercent = 8)
        item.id == "upgrade_study_grove_legacy" -> data.withUpgradeBonuses(focusPercent = 10, harvestPercent = 5, questCoins = 5)

        item.id == "upgrade_greenhouse_shelf_1" -> data.withUpgradeBonuses(harvestPercent = 3, greenhouseTier = 1)
        item.id == "upgrade_greenhouse_shelf_2" -> data.withUpgradeBonuses(harvestPercent = 4, greenhouseTier = 1)
        item.id == "upgrade_greenhouse_shelf_3" -> data.withUpgradeBonuses(harvestPercent = 5, greenhouseTier = 1)
        item.id == "upgrade_golden_greenhouse_frame" -> data.withUpgradeBonuses(harvestPercent = 6, greenhouseTier = 1, visualTheme = "golden_greenhouse")
        item.upgradeKind == UpgradeKind.GREENHOUSE -> data.withUpgradeBonuses(harvestPercent = 2, greenhouseTier = 1)

        item.id == "upgrade_morning_theme" -> data.withUpgradeBonuses(focusPercent = 1, visualTheme = "morning")
        item.id == "upgrade_night_theme" -> data.withUpgradeBonuses(focusPercent = 1, visualTheme = "night")
        item.id == "upgrade_weather_visual_pack" -> data.withUpgradeBonuses(focusPercent = 1, visualTheme = "weather")
        item.id == "upgrade_seasonal_garden_pack" -> data.withUpgradeBonuses(harvestPercent = 2, visualTheme = "seasonal")
        item.id == "upgrade_cozy_lighting_pack" -> data.withUpgradeBonuses(focusPercent = 1, visualTheme = "cozy_lights")

        item.id == "upgrade_tool_shed" ||
            item.id == "upgrade_seed_pouch_1" ||
            item.id == "upgrade_inventory_bag_1" ||
            item.id == "upgrade_inventory_bag_2" ||
            item.id == "upgrade_auto_sort_inventory" ||
            item.id == "upgrade_garden_presets" ||
            item.id == "upgrade_garden_search_mode" ||
            item.id == "upgrade_library_catalog" ||
            item.id == "upgrade_achievement_hall" ||
            item.id == "upgrade_study_journal" ||
            item.id == "upgrade_study_music_box" ||
            item.id == "upgrade_furniture_capacity_1" ||
            item.id == "upgrade_garden_capacity_1" -> data.withUpgradeBonuses(focusPercent = 1)

        item.id == "upgrade_scholar_statue_plaza" -> data.withUpgradeBonuses(questCoins = 5, visualTheme = "plaza")
        item.id == "upgrade_celestial_observatory" -> data
            .withHouseLevel(8)
            .withHouseSpecialUnlocks(HouseSpecialUnlocks.UnlockAllRooms, HouseSpecialUnlocks.CelestialTheme)
            .withHouseThemes(HouseSpecialUnlocks.CelestialTheme)
            .withUpgradeBonuses(focusPercent = 4, visualTheme = "observatory")
        else -> data.withUpgradeBonuses(focusPercent = 1)
    }

    private fun GameData.withGardenSize(size: Int): GameData =
        copy(garden = garden.copy(width = maxOf(garden.width, size), height = maxOf(garden.height, size)))

    private fun GameData.withHouseLevel(level: Int): GameData =
        copy(house = house.copy(level = maxOf(house.level, level)))

    private fun GameData.withHouseSpecialUnlocks(vararg unlocks: String): GameData =
        copy(house = house.copy(specialUnlocks = (house.specialUnlocks + unlocks).distinct()))

    private fun GameData.withHouseThemes(vararg themes: String): GameData =
        copy(
            house = house.copy(
                unlockedThemes = (house.unlockedThemes + themes).distinct(),
                disabledThemes = house.disabledThemes - themes.toSet()
            )
        )

    private fun GameData.withUpgradeBonuses(
        focusPercent: Int = 0,
        harvestPercent: Int = 0,
        questCoins: Int = 0,
        growthReduction: Int = 0,
        greenhouseTier: Int = 0,
        visualTheme: String? = null
    ): GameData = copy(
        upgrades = upgrades.copy(
            focusCoinBonusPercent = upgrades.focusCoinBonusPercent + focusPercent,
            harvestBonusPercent = upgrades.harvestBonusPercent + harvestPercent,
            dailyQuestBonusCoins = upgrades.dailyQuestBonusCoins + questCoins,
            cropGrowthSessionReduction = upgrades.cropGrowthSessionReduction + growthReduction,
            greenhouseTier = upgrades.greenhouseTier + greenhouseTier,
            visualTheme = visualTheme ?: upgrades.visualTheme
        )
    )

    private fun List<InventoryItem>.add(itemId: String, nowMillis: Long): List<InventoryItem> {
        val existing = firstOrNull { it.shopItemId == itemId }
        return if (existing == null) {
            this + InventoryItem(shopItemId = itemId, quantity = 1, firstPurchasedAtEpochMillis = nowMillis)
        } else {
            map { if (it.shopItemId == itemId) it.copy(quantity = it.quantity + 1) else it }
        }
    }

    private fun List<InventoryItem>.decrement(itemId: String): List<InventoryItem> =
        mapNotNull { item ->
            if (item.shopItemId == itemId) {
                val nextQuantity = item.quantity - 1
                if (nextQuantity > 0) item.copy(quantity = nextQuantity) else null
            } else {
                item
            }
        }

    private fun CharacterCustomization.equip(item: ShopItem): CharacterCustomization = when (item.cosmeticSlot) {
        CosmeticSlot.SHIRT -> copy(shirt = item.visualKey)
        CosmeticSlot.HAT -> copy(hat = item.visualKey)
        CosmeticSlot.BACKPACK -> copy(backpack = item.visualKey)
        CosmeticSlot.HAIR -> copy(hairStyle = item.visualKey)
        CosmeticSlot.SKIN_TONE -> copy(skinTone = item.visualKey)
        CosmeticSlot.ACCESSORY -> copy(accessory = item.visualKey)
        null -> this
    }
}
