package com.studygrove.app

import com.studygrove.app.data.model.GameData
import com.studygrove.app.data.model.HouseItemEdit
import com.studygrove.app.data.model.HouseObject
import com.studygrove.app.data.model.HouseObjectType
import com.studygrove.app.data.model.HouseRoom
import com.studygrove.app.data.model.HouseState
import com.studygrove.app.data.model.normalized
import com.studygrove.app.data.repository.withHouseItemEdit
import com.studygrove.app.ui.house.drawFurnitureSeam
import com.studygrove.app.data.model.furnitureJoins
import com.studygrove.app.data.model.placementAngle
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HouseFurnitureTest {
    private val desk = HouseObject("first", "furniture_small_desk", 2, 2, HouseObjectType.DESK)
    private val game = GameData(house = HouseState(objects = listOf(desk)))

    @Test
    fun legacyFurnitureStartsAtZeroAndEveryRotationSurvivesSaveAndNormalization() {
        val legacy = """{"id":"first","shopItemId":"furniture_small_desk","x":2,"y":2,"type":"DESK"}"""
        assertEquals(0, Json.decodeFromString<HouseObject>(legacy).rotationDegrees)
        for (angle in 0..360 step 45) {
            val (updated, result) = game.withHouseItemEdit(desk, HouseItemEdit.Rotate(angle))
            assertTrue(result.success)
            val saved = Json.decodeFromString<GameData>(Json.encodeToString(updated)).normalized()
            assertEquals(angle % 360, saved.house.objects.single().rotationDegrees)
            assertEquals(game.inventory, saved.inventory)
        }
    }

    @Test
    fun deletionReturnsOneItemAndStaleMenusCannotEditAReplacementOrAnotherRoom() {
        val (empty, removed) = game.withHouseItemEdit(desk, HouseItemEdit.Delete)
        assertTrue(removed.success)
        val (again, retry) = empty.withHouseItemEdit(desk, HouseItemEdit.Delete)
        assertFalse(retry.success)
        assertEquals(1, again.inventory.single { it.shopItemId == desk.shopItemId }.quantity)
        val replacement = game.copy(house = game.house.copy(objects = listOf(desk.copy(id = "new"))))
        assertEquals(replacement, replacement.withHouseItemEdit(desk, HouseItemEdit.Delete).first)
        val elsewhere = game.copy(house = game.house.copy(currentRoom = HouseRoom.BEDROOM))
        assertFalse(elsewhere.withHouseItemEdit(desk, HouseItemEdit.Rotate(90)).second.success)
        val testGame = game.copy(settings = game.settings.copy(testModeEnabled = true))
        assertEquals(testGame.inventory, testGame.withHouseItemEdit(desk, HouseItemEdit.Delete).first.inventory)
    }

    @Test
    fun allEightAnglesConnectOnceAndDiagonalJointsRespectOccupiedCorners() {
        val axes = listOf(1 to 0, 1 to 1, 0 to 1, -1 to 1)
        for (step in 0..7) {
            val (dx, dy) = axes[step % 4]
            val first = desk.copy(rotationDegrees = step * 45)
            val nextTile = first.x + dx to first.y + dy
            val next = first.copy(id = "next", x = nextTile.first, y = nextTile.second, rotationDegrees = (step * 45 + 180) % 360)
            val tiles = mapOf((first.x to first.y) to first, nextTile to next)
            val existing = mapOf((first.x to first.y) to first)
            assertEquals(step * 45, placementAngle(next.x, next.y, existing, emptySet()))
            val joint = furnitureJoins(tiles, emptySet()).single()
            val solid = IntArray(48 * 48) { if (it % 48 in 9..37 && it / 48 in 12..35) -1 else 0 }
            var painted = 0
            drawFurnitureSeam(joint, solid, solid) { x, y, _ ->
                assertTrue(x / 48 in minOf(first.x, next.x)..maxOf(first.x, next.x))
                assertTrue(y / 48 in minOf(first.y, next.y)..maxOf(first.y, next.y))
                painted++
            }
            assertTrue(painted > 0)
            assertTrue(furnitureJoins(tiles + (nextTile to next.copy(rotationDegrees = next.rotationDegrees + 45)), emptySet()).isEmpty())
            if (dx != 0 && dy != 0) {
                val corner = first.x + dx to first.y
                assertTrue(furnitureJoins(tiles, setOf(corner)).isEmpty())
                assertEquals(0, placementAngle(next.x, next.y, existing, setOf(corner)))
                val obstacle = desk.copy(id = "obstacle", x = corner.first, y = corner.second, rotationDegrees = 90)
                assertTrue(furnitureJoins(tiles + (corner to obstacle), emptySet()).isEmpty())
            }
        }
    }
}
