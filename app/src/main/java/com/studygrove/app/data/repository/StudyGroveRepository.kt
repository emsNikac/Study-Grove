package com.studygrove.app.data.repository

import com.studygrove.app.data.local.StudyGroveLocalDataSource
import com.studygrove.app.data.model.ActiveTimerState
import com.studygrove.app.data.model.CancelTimerResult
import com.studygrove.app.data.model.CharacterCustomization
import com.studygrove.app.data.model.CosmeticSlot
import com.studygrove.app.data.model.GameData
import com.studygrove.app.data.model.GardenObject
import com.studygrove.app.data.model.GardenObjectType
import com.studygrove.app.data.model.HouseObject
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
                questBonus = questResult.bonusCoins,
                harvestBonus = 0,
                newLevel = newLevel,
                completedSessionsBefore = data.player.completedSessions
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
            val occupied = data.garden.objects.any { it.x == x && it.y == y }
            when {
                x !in 0 until data.garden.width || y !in 0 until data.garden.height -> {
                    result = PlacementResult(false, "That tile is outside your grove.")
                    data
                }
                occupied -> {
                    result = PlacementResult(false, "That tile already has something cozy on it.")
                    data
                }
                !hasItem -> {
                    result = PlacementResult(false, "You do not have ${item.name} in your inventory.")
                    data
                }
                else -> {
                    // Seeds become crop objects with a session counter; every completed study
                    // session advances those counters through GardenGrowthEngine.
                    val newObject = GardenObject(
                        id = "garden_${item.id}_$nowMillis",
                        shopItemId = item.id,
                        x = x,
                        y = y,
                        type = item.gardenObjectType,
                        growthStage = if (item.growthSessions > 0) 0 else 1,
                        sessionsRemaining = item.growthSessions,
                        placedAtEpochMillis = nowMillis
                    )
                    val today = todayString(nowMillis, zoneId)
                    val questResult = if (item.gardenObjectType == GardenObjectType.CROP) {
                        DailyQuestEngine.onCropPlanted(data.dailyQuests, today)
                    } else {
                        DailyQuestEngine.ensureToday(data.dailyQuests, today).let { DailyQuestEngine.onSessionCompleted(it, today, 0) }
                    }
                    val playerWithQuestCoins = data.player.copy(coins = data.player.coins + questResult.bonusCoins)
                    result = PlacementResult(
                        true,
                        if (questResult.bonusCoins > 0) {
                            "Placed ${item.name}. Daily quest complete: +${questResult.bonusCoins} coins."
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
            val gardenObject = data.garden.objects.firstOrNull { it.x == x && it.y == y } ?: return@update data
            val item = ShopCatalog.byId(gardenObject.shopItemId) ?: return@update data
            val remainingObjects = data.garden.objects.filterNot { it.id == gardenObject.id }
            if (gardenObject.type == GardenObjectType.CROP && gardenObject.sessionsRemaining == 0) {
                result = PlacementResult(true, "Harvested ${item.name.removeSuffix(" Seed")} for ${item.harvestCoins} coins.")
                data.copy(
                    player = data.player.copy(coins = data.player.coins + item.harvestCoins),
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
            val occupied = data.house.objects.any { it.x == x && it.y == y }
            when {
                x !in 0 until data.house.width || y !in 0 until data.house.height -> {
                    result = PlacementResult(false, "That spot is outside the room.")
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
            val houseObject = data.house.objects.firstOrNull { it.x == x && it.y == y } ?: return@update data
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

    private fun applyUpgrade(data: GameData, item: ShopItem): GameData = when (item.upgradeKind) {
        UpgradeKind.GARDEN_EXPANSION_10 -> data.copy(garden = data.garden.copy(width = 10, height = 10)).normalized()
        UpgradeKind.GARDEN_EXPANSION_12 -> data.copy(garden = data.garden.copy(width = 12, height = 12)).normalized()
        UpgradeKind.GARDEN_EXPANSION_15 -> data.copy(garden = data.garden.copy(width = 15, height = 15)).normalized()
        UpgradeKind.HOUSE_LEVEL_2 -> data.copy(house = data.house.copy(level = maxOf(data.house.level, 2))).normalized()
        UpgradeKind.HOUSE_LEVEL_3 -> data.copy(house = data.house.copy(level = maxOf(data.house.level, 3))).normalized()
        UpgradeKind.HOUSE_LEVEL_4 -> data.copy(house = data.house.copy(level = maxOf(data.house.level, 4))).normalized()
        UpgradeKind.HOUSE_LEVEL_5 -> data.copy(house = data.house.copy(level = maxOf(data.house.level, 5))).normalized()
        UpgradeKind.GREENHOUSE, null -> data
    }

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
