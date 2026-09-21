package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.MusicDao
import com.example.data.local.entity.FavoriteEntity
import com.example.data.local.entity.PlaylistEntity
import com.example.data.local.entity.PlaylistSongEntity

@Database(
    entities = [
        FavoriteEntity::class,
        PlaylistEntity::class,
        PlaylistSongEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class MusicDatabase : RoomDatabase() {
    abstract fun musicDao(): MusicDao

    companion object {
        @Volatile
        private var INSTANCE: MusicDatabase? = null

        fun getDatabase(context: Context): MusicDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MusicDatabase::class.java,
                    "music_player_database"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
