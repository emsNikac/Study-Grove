package com.studygrove.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.studygrove.app.data.model.GameData
import com.studygrove.app.data.model.normalized
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.IOException

private val Context.studyGroveDataStore by preferencesDataStore(name = "study_grove_state")

class StudyGroveLocalDataSource(private val context: Context) {
    private val gameDataKey = stringPreferencesKey("game_data_json")
    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    // The MVP persists the full offline save file as one version-tolerant JSON blob.
    // Decoding always falls back to a normalized default so a corrupted save never blocks launch.
    val gameData: Flow<GameData> = context.studyGroveDataStore.data
        .catch { throwable ->
            if (throwable is IOException || throwable is SerializationException) {
                emit(emptyPreferences())
            } else {
                emit(emptyPreferences())
            }
        }
        .map { preferences -> decode(preferences[gameDataKey]).normalized() }

    suspend fun current(): GameData = gameData.first()

    suspend fun update(transform: (GameData) -> GameData): GameData {
        var saved = GameData.default().normalized()
        context.studyGroveDataStore.edit { preferences ->
            val current = decode(preferences[gameDataKey]).normalized()
            saved = transform(current).normalized()
            preferences[gameDataKey] = json.encodeToString(saved)
        }
        return saved
    }

    suspend fun replace(gameData: GameData): GameData {
        val normalized = gameData.normalized()
        context.studyGroveDataStore.edit { preferences ->
            preferences[gameDataKey] = json.encodeToString(normalized)
        }
        return normalized
    }

    suspend fun reset(): GameData = replace(GameData.default())

    private fun decode(rawJson: String?): GameData {
        if (rawJson.isNullOrBlank()) return GameData.default()
        return runCatching {
            json.decodeFromString<GameData>(rawJson)
        }.getOrElse {
            GameData.default()
        }
    }
}
