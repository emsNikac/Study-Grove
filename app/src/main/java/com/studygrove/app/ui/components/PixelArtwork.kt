package com.studygrove.app.ui.components

import android.content.res.Resources
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.studygrove.app.R
import kotlin.math.roundToInt

/** All sheets use density-independent source pixels and row-major frame order. */
internal enum class PixelSheet(val resource: Int, val width: Int = 48, val height: Int = 48) {
    Items(R.drawable.garden_items),
    Seedlings(R.drawable.garden_seedlings),
    Fences(R.drawable.garden_fences),
    Motion(R.drawable.garden_motion),
    Grass(R.drawable.garden_ground),
    Furniture(R.drawable.house_furniture),
    House(R.drawable.house_materials),
    Partitions(R.drawable.house_partitions),
    Avatar(R.drawable.avatar_layers, 32, 40),
    BackWalls(R.drawable.house_back_walls),
    Scenery(R.drawable.grove_landscapes, 256, 100),
    Cottages(R.drawable.grove_cottages, 112, 80),
    NightCottages(R.drawable.grove_cottages_night, 112, 80);

    // A garden can show hundreds of cells sharing the same unqualified nodpi resource.
    private var bitmap: ImageBitmap? = null

    @Synchronized
    fun load(resources: Resources): ImageBitmap =
        bitmap ?: ImageBitmap.imageResource(resources, resource).also { bitmap = it }
}

@Composable
internal fun rememberSheet(sheet: PixelSheet): ImageBitmap {
    val resources = LocalContext.current.resources
    return remember(sheet, resources) { sheet.load(resources) }
}

internal fun DrawScope.drawPixelFrame(
    bitmap: ImageBitmap,
    sheet: PixelSheet,
    index: Int,
    offset: IntOffset = IntOffset.Zero,
    destination: IntSize = IntSize(size.width.roundToInt(), size.height.roundToInt())
) {
    val columns = bitmap.width / sheet.width
    drawImage(
        image = bitmap,
        srcOffset = IntOffset(index % columns * sheet.width, index / columns * sheet.height),
        srcSize = IntSize(sheet.width, sheet.height),
        dstOffset = offset,
        dstSize = destination,
        filterQuality = FilterQuality.None
    )
}

@Composable
internal fun PixelTile(
    sheet: PixelSheet,
    index: Int,
    modifier: Modifier,
    animationFrame: (() -> Int)? = null
) {
    val bitmap = rememberSheet(sheet)
    Canvas(modifier) {
        drawPixelFrame(bitmap, sheet, index + (animationFrame?.invoke() ?: 0))
    }
}

/** Frame bits shared by the garden fence and house partition sheets. */
internal inline fun connectionMask(x: Int, y: Int, connected: (Int, Int) -> Boolean): Int {
    var mask = 0
    if (connected(x - 1, y)) mask = mask or 1
    if (connected(x + 1, y)) mask = mask or 2
    if (connected(x, y - 1)) mask = mask or 4
    if (connected(x, y + 1)) mask = mask or 8
    return mask
}

private fun frameIndices(keys: String) =
    keys.trimIndent().trim().split(Regex("\\s+")).withIndex().associate { (index, key) -> key to index }

// Keep the sheet coordinates independent of shop ordering and saved inventory IDs.
internal object GardenArt {
    const val cropCount = 54

    val items = frameIndices("""
        daisy carrot strawberry study_blossom sunbean mint_sprout notebook_clover bluebell radish
        moon_daisy tomato lavender pumpkin focus_fern peachroot crystal_tulip honey_wheat pageleaf
        star_pepper memory_mushroom inkcap_mushroom scholar_sage golden_corn ruby_rose cocoa_bean
        snowdrop clockvine aurora_orchid brainberry tea_leaf dream_melon stardust_lily study_spruce
        ember_marigold pearl_onion bookberry_bush glowcap silver_sunflower ancient_grain prism_poppy
        cloud_cotton lantern_fruit sapphire_grape royal_iris wisdom_willow comet_cabbage melody_mint
        phoenix_pepper library_lotus galaxy_gourd timekeeper_thyme diamond_dahlia mastery_mango
        eternal_study apple_tree golden_tree berry_bush cherry_blossom willow_tree cloud_tree
        rainbow_tree aurora_tree pond study_fountain frog_pond mini_waterfall koi_pond dream_pond
        knowledge_fountain flower_pot lamp bench bird_bath watering_can_statue study_signpost
        mushroom_log picnic_blanket mailbox wind_chime butterfly_house compost_bin tea_table
        scarecrow outdoor_chalkboard beehive stone_lantern hammock book_cart crystal_cluster
        garden_clock firefly_jar owl_perch starflower_bed sundial scholar_statue golden_beehive
        floating_lanterns master_study_statue greenhouse observatory tiny_bridge garden_arch
        study_shrine reading_gazebo library_kiosk moon_gate tiny_windmill telescope_platform
        ancient_obelisk planetarium_dome celestial_portal eternal_grove_monument wood_path stone_path
        dirt_path pebble_path grass_trim_path light_wood_path dark_wood_path mossy_stone_path
        brick_path sand_path clay_tile_path blue_tile_path checker_path flower_edge_path leafy_path
        study_rune_path cherry_wood_path marble_path lantern_path slate_path autumn_leaf_path
        snow_path star_tile_path moonstone_path rainbow_brick_path waterline_path observatory_path
        cloud_path library_carpet_path ancient_ruin_path firefly_path study_streak_path aurora_path
        planet_path diamond_path mastery_path eternal_grove_path
    """)
    val fences = frameIndices("""
        small_fence rope_fence white_picket_fence bamboo_fence hedge_fence iron_fence garden_gate
        ivy_fence crystal_fence scholar_fence golden_rail_fence bonsai_fence royal_garden_fence
        dream_fence clockwork_fence celestial_fence
    """)
    val animated = frameIndices("tiny_windmill lamp mini_waterfall knowledge_fountain celestial_portal")
}

internal val furnitureFrames = frameIndices("""
    desk chair bookshelf bed library_wall round_rug wooden_stool wall_calendar study_lamp
    small_plant blue_rug cork_board tea_shelf reading_pillow nightstand window_curtains
    drawer_cabinet focus_clock beanbag_chair floor_cushions writing_desk globe fireplace
    tall_bookshelf plant_shelf cozy_sofa study_mat music_box tea_table_indoor map_wall
    drafting_table comfy_armchair candle_set window_seat scholar_bed trophy_shelf
    chalkboard_wall aquarium library_ladder grand_bookshelf starry_rug telescope piano
    research_desk archive_cabinet crystal_lamp observatory_window dream_bed golden_desk
    scholar_sofa_set planet_mobile grandfather_clock master_library_wall
    celestial_reading_throne eternal_scholar_desk
""")
