package com.studygrove.app.data.model

object ShopCatalog {
    val items: List<ShopItem> = listOf(
        ShopItem(
            id = "seed_daisy",
            name = "Daisy Seed",
            description = "A sunny flower that blooms after one completed session.",
            category = ShopCategory.SEEDS,
            price = 10,
            unlockLevel = 1,
            placementTarget = PlacementTarget.GARDEN,
            gardenObjectType = GardenObjectType.CROP,
            growthSessions = 1,
            harvestCoins = 4,
            visualKey = "daisy"
        ),
        ShopItem(
            id = "seed_carrot",
            name = "Carrot Seed",
            description = "A sturdy study snack crop. Grows after two sessions.",
            category = ShopCategory.SEEDS,
            price = 20,
            unlockLevel = 2,
            placementTarget = PlacementTarget.GARDEN,
            gardenObjectType = GardenObjectType.CROP,
            growthSessions = 2,
            harvestCoins = 10,
            visualKey = "carrot"
        ),
        ShopItem(
            id = "seed_strawberry",
            name = "Strawberry Seed",
            description = "A sweet patch that ripens after three sessions.",
            category = ShopCategory.SEEDS,
            price = 35,
            unlockLevel = 3,
            placementTarget = PlacementTarget.GARDEN,
            gardenObjectType = GardenObjectType.CROP,
            growthSessions = 3,
            harvestCoins = 18,
            visualKey = "strawberry"
        ),
        ShopItem(
            id = "seed_study_blossom",
            name = "Study Blossom Seed",
            description = "A rare blossom that grows with five focused sessions.",
            category = ShopCategory.SEEDS,
            price = 100,
            unlockLevel = 8,
            placementTarget = PlacementTarget.GARDEN,
            gardenObjectType = GardenObjectType.CROP,
            growthSessions = 5,
            harvestCoins = 55,
            visualKey = "study_blossom"
        ),
        ShopItem(
            id = "path_wood",
            name = "Wooden Path",
            description = "Warm planks for a tiny garden trail.",
            category = ShopCategory.PATHS_FENCES,
            price = 5,
            unlockLevel = 1,
            placementTarget = PlacementTarget.GARDEN,
            gardenObjectType = GardenObjectType.PATH,
            visualKey = "wood_path"
        ),
        ShopItem(
            id = "fence_small",
            name = "Small Fence",
            description = "A friendly border for your plants.",
            category = ShopCategory.PATHS_FENCES,
            price = 15,
            unlockLevel = 2,
            placementTarget = PlacementTarget.GARDEN,
            gardenObjectType = GardenObjectType.FENCE,
            visualKey = "small_fence"
        ),
        ShopItem(
            id = "decor_flower_pot",
            name = "Flower Pot",
            description = "A bright pot for corners that need cheer.",
            category = ShopCategory.GARDEN_DECOR,
            price = 25,
            unlockLevel = 2,
            placementTarget = PlacementTarget.GARDEN,
            gardenObjectType = GardenObjectType.DECORATION,
            visualKey = "flower_pot"
        ),
        ShopItem(
            id = "tree_apple",
            name = "Apple Tree",
            description = "A small orchard friend for level three gardeners.",
            category = ShopCategory.GARDEN_DECOR,
            price = 70,
            unlockLevel = 3,
            placementTarget = PlacementTarget.GARDEN,
            gardenObjectType = GardenObjectType.TREE,
            visualKey = "apple_tree"
        ),
        ShopItem(
            id = "path_stone",
            name = "Stone Path",
            description = "Cool stepping stones for quiet mornings.",
            category = ShopCategory.PATHS_FENCES,
            price = 30,
            unlockLevel = 4,
            placementTarget = PlacementTarget.GARDEN,
            gardenObjectType = GardenObjectType.PATH,
            visualKey = "stone_path"
        ),
        ShopItem(
            id = "decor_lamp",
            name = "Lamp Post",
            description = "A soft glow for evening study walks.",
            category = ShopCategory.GARDEN_DECOR,
            price = 50,
            unlockLevel = 4,
            placementTarget = PlacementTarget.GARDEN,
            gardenObjectType = GardenObjectType.DECORATION,
            visualKey = "lamp"
        ),
        ShopItem(
            id = "water_tiny_pond",
            name = "Tiny Pond",
            description = "A calm square of water for deep breaths.",
            category = ShopCategory.GARDEN_DECOR,
            price = 120,
            unlockLevel = 5,
            placementTarget = PlacementTarget.GARDEN,
            gardenObjectType = GardenObjectType.WATER,
            visualKey = "pond"
        ),
        ShopItem(
            id = "decor_reading_bench",
            name = "Reading Bench",
            description = "A cozy garden seat for review notes.",
            category = ShopCategory.GARDEN_DECOR,
            price = 150,
            unlockLevel = 6,
            placementTarget = PlacementTarget.GARDEN,
            gardenObjectType = GardenObjectType.DECORATION,
            visualKey = "bench"
        ),
        ShopItem(
            id = "decor_greenhouse",
            name = "Greenhouse",
            description = "A glassy study nook for level seven growers.",
            category = ShopCategory.UPGRADES,
            price = 2_000,
            unlockLevel = 7,
            placementTarget = PlacementTarget.GARDEN,
            gardenObjectType = GardenObjectType.STRUCTURE,
            upgradeKind = UpgradeKind.GREENHOUSE,
            visualKey = "greenhouse",
            maxPurchases = 1
        ),
        ShopItem(
            id = "decor_observatory",
            name = "Observatory Decoration",
            description = "A stargazing dome for long-term dreamers.",
            category = ShopCategory.GARDEN_DECOR,
            price = 600,
            unlockLevel = 15,
            placementTarget = PlacementTarget.GARDEN,
            gardenObjectType = GardenObjectType.STRUCTURE,
            visualKey = "observatory"
        ),
        ShopItem(
            id = "tree_golden_study",
            name = "Golden Study Tree",
            description = "A glowing tree for masters of the grove.",
            category = ShopCategory.GARDEN_DECOR,
            price = 1_000,
            unlockLevel = 20,
            placementTarget = PlacementTarget.GARDEN,
            gardenObjectType = GardenObjectType.TREE,
            visualKey = "golden_tree"
        ),
        ShopItem(
            id = "furniture_small_desk",
            name = "Small Desk",
            description = "A practical desk for your cottage notes.",
            category = ShopCategory.FURNITURE,
            price = 50,
            unlockLevel = 2,
            placementTarget = PlacementTarget.HOUSE,
            houseObjectType = HouseObjectType.DESK,
            visualKey = "desk"
        ),
        ShopItem(
            id = "furniture_cozy_chair",
            name = "Cozy Chair",
            description = "A rounded chair for calm reading breaks.",
            category = ShopCategory.FURNITURE,
            price = 75,
            unlockLevel = 3,
            placementTarget = PlacementTarget.HOUSE,
            houseObjectType = HouseObjectType.CHAIR,
            visualKey = "chair"
        ),
        ShopItem(
            id = "furniture_bookshelf",
            name = "Bookshelf",
            description = "A tidy shelf that makes the room feel wiser.",
            category = ShopCategory.FURNITURE,
            price = 120,
            unlockLevel = 4,
            placementTarget = PlacementTarget.HOUSE,
            houseObjectType = HouseObjectType.SHELF,
            visualKey = "bookshelf"
        ),
        ShopItem(
            id = "furniture_bed",
            name = "Bed",
            description = "A cozy rest spot for healthy study rhythms.",
            category = ShopCategory.FURNITURE,
            price = 150,
            unlockLevel = 5,
            placementTarget = PlacementTarget.HOUSE,
            houseObjectType = HouseObjectType.BED,
            visualKey = "bed"
        ),
        ShopItem(
            id = "furniture_library_wall",
            name = "Library Wall",
            description = "A wall of books for the level ten library room.",
            category = ShopCategory.FURNITURE,
            price = 300,
            unlockLevel = 10,
            placementTarget = PlacementTarget.HOUSE,
            houseObjectType = HouseObjectType.WALL,
            visualKey = "library_wall"
        ),
        ShopItem(
            id = "cosmetic_blue_shirt",
            name = "Blue Shirt",
            description = "A clear-sky shirt for focused mornings.",
            category = ShopCategory.COSMETICS,
            price = 40,
            unlockLevel = 3,
            placementTarget = PlacementTarget.CHARACTER,
            cosmeticSlot = CosmeticSlot.SHIRT,
            visualKey = "blue_shirt",
            maxPurchases = 1
        ),
        ShopItem(
            id = "cosmetic_green_hat",
            name = "Green Hat",
            description = "A soft green cap for garden work.",
            category = ShopCategory.COSMETICS,
            price = 90,
            unlockLevel = 5,
            placementTarget = PlacementTarget.CHARACTER,
            cosmeticSlot = CosmeticSlot.HAT,
            visualKey = "green_hat",
            maxPurchases = 1
        ),
        ShopItem(
            id = "cosmetic_canvas_pack",
            name = "Canvas Backpack",
            description = "A small pack for notes, snacks, and ambition.",
            category = ShopCategory.COSMETICS,
            price = 140,
            unlockLevel = 6,
            placementTarget = PlacementTarget.CHARACTER,
            cosmeticSlot = CosmeticSlot.BACKPACK,
            visualKey = "backpack",
            maxPurchases = 1
        ),
        ShopItem(
            id = "upgrade_garden_10",
            name = "Garden Expansion 1",
            description = "Expands the garden from 8x8 to 10x10.",
            category = ShopCategory.UPGRADES,
            price = 300,
            unlockLevel = 3,
            placementTarget = PlacementTarget.UPGRADE,
            upgradeKind = UpgradeKind.GARDEN_EXPANSION_10,
            visualKey = "garden_expand",
            maxPurchases = 1
        ),
        ShopItem(
            id = "upgrade_garden_12",
            name = "Garden Expansion 2",
            description = "Expands the garden from 10x10 to 12x12.",
            category = ShopCategory.UPGRADES,
            price = 700,
            unlockLevel = 6,
            placementTarget = PlacementTarget.UPGRADE,
            upgradeKind = UpgradeKind.GARDEN_EXPANSION_12,
            visualKey = "garden_expand",
            maxPurchases = 1
        ),
        ShopItem(
            id = "upgrade_garden_15",
            name = "Garden Expansion 3",
            description = "Expands the garden into a roomy 15x15 grove.",
            category = ShopCategory.UPGRADES,
            price = 1_500,
            unlockLevel = 12,
            placementTarget = PlacementTarget.UPGRADE,
            upgradeKind = UpgradeKind.GARDEN_EXPANSION_15,
            visualKey = "garden_expand",
            maxPurchases = 1
        ),
        ShopItem(
            id = "upgrade_house_2",
            name = "House Upgrade 1",
            description = "Upgrades the tiny cottage into a cozy cottage.",
            category = ShopCategory.UPGRADES,
            price = 500,
            unlockLevel = 4,
            placementTarget = PlacementTarget.UPGRADE,
            upgradeKind = UpgradeKind.HOUSE_LEVEL_2,
            visualKey = "house_upgrade",
            maxPurchases = 1
        ),
        ShopItem(
            id = "upgrade_house_3",
            name = "House Upgrade 2",
            description = "Builds a study cabin with more room for furniture.",
            category = ShopCategory.UPGRADES,
            price = 1_200,
            unlockLevel = 8,
            placementTarget = PlacementTarget.UPGRADE,
            upgradeKind = UpgradeKind.HOUSE_LEVEL_3,
            visualKey = "house_upgrade",
            maxPurchases = 1
        ),
        ShopItem(
            id = "upgrade_house_4",
            name = "House Upgrade 3",
            description = "Turns your cabin into a scholar's home.",
            category = ShopCategory.UPGRADES,
            price = 2_400,
            unlockLevel = 12,
            placementTarget = PlacementTarget.UPGRADE,
            upgradeKind = UpgradeKind.HOUSE_LEVEL_4,
            visualKey = "house_upgrade",
            maxPurchases = 1
        ),
        ShopItem(
            id = "upgrade_house_5",
            name = "House Upgrade 4",
            description = "Creates the dream library house.",
            category = ShopCategory.UPGRADES,
            price = 4_000,
            unlockLevel = 20,
            placementTarget = PlacementTarget.UPGRADE,
            upgradeKind = UpgradeKind.HOUSE_LEVEL_5,
            visualKey = "house_upgrade",
            maxPurchases = 1
        )
    )

    fun byId(id: String): ShopItem? = items.firstOrNull { it.id == id }

    fun placeableGardenItems(): Set<String> =
        items.filter { it.placementTarget == PlacementTarget.GARDEN }.map { it.id }.toSet()

    fun placeableHouseItems(): Set<String> =
        items.filter { it.placementTarget == PlacementTarget.HOUSE }.map { it.id }.toSet()
}

object AchievementCatalog {
    val achievements = listOf(
        Achievement(
            id = "first_focus",
            title = "First Focus",
            description = "Complete 1 study session."
        ),
        Achievement(
            id = "tiny_scholar",
            title = "Tiny Scholar",
            description = "Study 60 total minutes."
        ),
        Achievement(
            id = "garden_starter",
            title = "Garden Starter",
            description = "Buy 5 garden items."
        ),
        Achievement(
            id = "cozy_home",
            title = "Cozy Home",
            description = "Buy 5 furniture items."
        ),
        Achievement(
            id = "one_week_strong",
            title = "One Week Strong",
            description = "Maintain a 7-day streak."
        ),
        Achievement(
            id = "deep_worker",
            title = "Deep Worker",
            description = "Complete a 50-minute session."
        ),
        Achievement(
            id = "study_grove_master",
            title = "Study Grove Master",
            description = "Study 1,000 total minutes."
        )
    )
}

object DailyQuestCatalog {
    fun questsFor(localDate: String) = DailyQuestState(
        localDate = localDate,
        quests = listOf(
            DailyQuest(
                id = "daily_focus_25",
                title = "Focus candle",
                description = "Complete one 25-minute session.",
                target = 1,
                rewardCoins = 12
            ),
            DailyQuest(
                id = "daily_60_minutes",
                title = "Grove tending",
                description = "Study for 60 total minutes today.",
                target = 60,
                rewardCoins = 18
            ),
            DailyQuest(
                id = "daily_plant_crop",
                title = "Plant hope",
                description = "Plant one crop.",
                target = 1,
                rewardCoins = 8
            )
        )
    )
}
