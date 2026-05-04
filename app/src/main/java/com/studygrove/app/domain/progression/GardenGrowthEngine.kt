package com.studygrove.app.domain.progression

import com.studygrove.app.data.model.GardenObjectType
import com.studygrove.app.data.model.GardenState
import com.studygrove.app.data.model.ShopCatalog

object GardenGrowthEngine {
    fun advanceCrops(garden: GardenState): GardenState {
        val advanced = garden.objects.map { gardenObject ->
            if (gardenObject.type != GardenObjectType.CROP || gardenObject.sessionsRemaining <= 0) {
                gardenObject
            } else {
                val item = ShopCatalog.byId(gardenObject.shopItemId)
                val nextRemaining = (gardenObject.sessionsRemaining - 1).coerceAtLeast(0)
                val totalGrowth = item?.growthSessions?.coerceAtLeast(1) ?: 1
                gardenObject.copy(
                    sessionsRemaining = nextRemaining,
                    growthStage = (totalGrowth - nextRemaining).coerceIn(0, totalGrowth)
                )
            }
        }
        return garden.copy(objects = advanced)
    }
}
