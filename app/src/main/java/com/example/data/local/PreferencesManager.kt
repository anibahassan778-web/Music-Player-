package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "player_preferences")

class PreferencesManager(private val context: Context) {

    companion object {
        val KEY_LAST_SONG_ID = longPreferencesKey("last_played_song_id")
        val KEY_LAST_POSITION = longPreferencesKey("last_played_position")
    }

    val lastPlayedSongId: Flow<Long?> = context.dataStore.data.map { preferences ->
        preferences[KEY_LAST_SONG_ID]
    }

    val lastPlayedPosition: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[KEY_LAST_POSITION] ?: 0L
    }

    suspend fun saveLastPlayed(songId: Long, position: Long) {
        context.dataStore.edit { preferences ->
            preferences[KEY_LAST_SONG_ID] = songId
            preferences[KEY_LAST_POSITION] = position
        }
    }
}
