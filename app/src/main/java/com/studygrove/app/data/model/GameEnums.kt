package com.studygrove.app.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class ShopCategory(val displayName: String) {
    SEEDS("Seeds"),
    GARDEN_DECOR("Garden decorations"),
    FURNITURE("House furniture"),
    COSMETICS("Character cosmetics"),
    PATHS_FENCES("Paths and fences"),
    UPGRADES("Upgrades")
}

@Serializable
enum class PlacementTarget {
    GARDEN,
    HOUSE,
    CHARACTER,
    UPGRADE
}

@Serializable
enum class GardenObjectType {
    CROP,
    TREE,
    DECORATION,
    PATH,
    FENCE,
    WATER,
    STRUCTURE
}

@Serializable
enum class HouseObjectType {
    DESK,
    CHAIR,
    SHELF,
    BED,
    WALL,
    DECOR
}

@Serializable
enum class HouseRoom(val displayName: String) {
    MAIN("Main Room"),
    BEDROOM("Bedroom"),
    STUDY("Study Room"),
    KITCHEN("Kitchen"),
    LIBRARY("Library"),
    ATTIC("Attic"),
    BALCONY("Balcony")
}

enum class HousePortalKind {
    DOOR,
    STAIRS_UP,
    STAIRS_DOWN,
    BALCONY_DOOR
}

enum class HouseLayoutCellKind {
    WALL,
    WINDOW,
    SHELF,
    COUNTER,
    HEARTH,
    WARDROBE,
    ROOF_BEAM,
    RAILING,
    PLANTER
}

@Serializable
enum class TimerPreset(val label: String, val minutes: Int?) {
    QUICK_STUDY("Quick Study", 15),
    FOCUS_SESSION("Focus Session", 25),
    DEEP_WORK("Deep Work", 50),
    CUSTOM("Custom Timer", null)
}

@Serializable
enum class UpgradeKind {
    GARDEN_EXPANSION_10,
    GARDEN_EXPANSION_12,
    GARDEN_EXPANSION_15,
    HOUSE_LEVEL_2,
    HOUSE_LEVEL_3,
    HOUSE_LEVEL_4,
    HOUSE_LEVEL_5,
    GREENHOUSE
}

@Serializable
enum class CosmeticSlot {
    HAIR,
    SHIRT,
    HAT,
    BACKPACK,
    SKIN_TONE,
    ACCESSORY
}
