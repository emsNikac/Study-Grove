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

@Serializable
data class HouseObject(
    val id: String,
    val shopItemId: String,
    val x: Int,
    val y: Int,
    val type: HouseObjectType,
    val placedAtEpochMillis: Long = 0L
)

@Serializable
data class HouseState(
    val level: Int = 1,
    val width: Int = 6,
    val height: Int = 5,
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
    val notificationsEnabled: Boolean = false,
    val defaultTimerMinutes: Int = 25,
    val gentleRemindersEnabled: Boolean = false,
    val testModeEnabled: Boolean = false
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
