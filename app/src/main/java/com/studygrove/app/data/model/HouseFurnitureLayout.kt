package com.studygrove.app.data.model

internal fun furnitureAngle(degrees: Int): Int = (degrees.mod(360) + 22) / 45 * 45 % 360

// Local left and right exchange places after a half turn.
private val axes = listOf(1 to 0, 1 to 1, 0 to 1, -1 to 1)
internal fun furnitureAxis(degrees: Int): Pair<Int, Int> = axes[furnitureAngle(degrees) / 45 % axes.size]

internal data class FurnitureJoin(val first: HouseObject, val second: HouseObject, val dx: Int, val dy: Int)

private fun cornerIsClear(
    x: Int, y: Int, dx: Int, dy: Int,
    objects: Map<Pair<Int, Int>, HouseObject>,
    reserved: Set<Pair<Int, Int>>
): Boolean {
    if (dx == 0 || dy == 0) return true
    val beside = (x + dx) to y
    val above = x to (y + dy)
    return beside !in objects && above !in objects && beside !in reserved && above !in reserved
}

internal fun furnitureJoins(
    objects: Map<Pair<Int, Int>, HouseObject>,
    reserved: Set<Pair<Int, Int>>
): List<FurnitureJoin> = buildList {
    for ((tile, first) in objects) {
        val axis = furnitureAxis(first.rotationDegrees)
        val (dx, dy) = axis
        val (x, y) = tile
        val second = objects[(x + dx) to (y + dy)] ?: continue
        if (first.room == second.room && furnitureAxis(second.rotationDegrees) == axis &&
            cornerIsClear(x, y, dx, dy, objects, reserved)) {
            add(FurnitureJoin(first, second, dx, dy))
        }
    }
}

/** Continue the run with the most open ends pointing at this tile. Ties follow the axis order above. */
internal fun placementAngle(
    x: Int, y: Int,
    objects: Map<Pair<Int, Int>, HouseObject>,
    reserved: Set<Pair<Int, Int>>
): Int {
    var angle = 0
    var bestCount = 0
    for (axis in axes) {
        val (dx, dy) = axis
        var neighborAngle = 0
        var count = 0
        for (sign in -1..1 step 2) {
            val nx = dx * sign
            val ny = dy * sign
            val piece = objects[(x + nx) to (y + ny)] ?: continue
            if (furnitureAxis(piece.rotationDegrees) == axis && cornerIsClear(x, y, nx, ny, objects, reserved)) {
                if (count == 0) neighborAngle = piece.rotationDegrees
                count++
            }
        }
        if (count > bestCount) {
            angle = neighborAngle
            bestCount = count
        }
    }
    return angle
}
