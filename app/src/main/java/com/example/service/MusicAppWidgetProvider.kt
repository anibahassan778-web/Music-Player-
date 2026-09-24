package com.example.service

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.domain.model.Song

class MusicAppWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, null, false)
        }
    }

    companion object {
        fun updateAllWidgets(
            context: Context,
            currentSong: Song?,
            isPlaying: Boolean
        ) {
            try {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val thisWidget = ComponentName(context, MusicAppWidgetProvider::class.java)
                val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
                for (widgetId in allWidgetIds) {
                    updateAppWidget(context, appWidgetManager, widgetId, currentSong, isPlaying)
                }
            } catch (e: Exception) {
                // Ignore
            }
        }

        private fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            currentSong: Song?,
            isPlaying: Boolean
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_music_player)

            // Open MainActivity on root click
            val mainIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val mainPendingIntent = PendingIntent.getActivity(
                context,
                0,
                mainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, mainPendingIntent)

            // Track info
            if (currentSong != null) {
                views.setTextViewText(R.id.widget_song_title, currentSong.title)
                views.setTextViewText(R.id.widget_song_artist, currentSong.artist)

                if (!currentSong.albumArtUri.isNullOrBlank()) {
                    try {
                        val uri = Uri.parse(currentSong.albumArtUri)
                        context.contentResolver.openInputStream(uri)?.use { stream ->
                            val bitmap = BitmapFactory.decodeStream(stream)
                            if (bitmap != null) {
                                views.setImageViewBitmap(R.id.widget_album_art, bitmap)
                            } else {
                                views.setImageViewResource(R.id.widget_album_art, R.drawable.ic_widget_music_note)
                            }
                        }
                    } catch (e: Exception) {
                        views.setImageViewResource(R.id.widget_album_art, R.drawable.ic_widget_music_note)
                    }
                } else {
                    views.setImageViewResource(R.id.widget_album_art, R.drawable.ic_widget_music_note)
                }
            } else {
                views.setTextViewText(R.id.widget_song_title, context.getString(R.string.no_song_playing))
                views.setTextViewText(R.id.widget_song_artist, context.getString(R.string.app_name))
                views.setImageViewResource(R.id.widget_album_art, R.drawable.ic_widget_music_note)
            }

            // Play/Pause icon
            val playPauseIcon = if (isPlaying) R.drawable.ic_widget_pause else R.drawable.ic_widget_play
            views.setImageViewResource(R.id.widget_btn_play_pause, playPauseIcon)

            // Pending Intents for Controls
            views.setOnClickPendingIntent(
                R.id.widget_btn_play_pause,
                getServicePendingIntent(context, MusicService.ACTION_PLAY_PAUSE, 101)
            )
            views.setOnClickPendingIntent(
                R.id.widget_btn_prev,
                getServicePendingIntent(context, MusicService.ACTION_PREVIOUS, 102)
            )
            views.setOnClickPendingIntent(
                R.id.widget_btn_next,
                getServicePendingIntent(context, MusicService.ACTION_NEXT, 103)
            )

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun getServicePendingIntent(
            context: Context,
            action: String,
            requestCode: Int
        ): PendingIntent {
            val intent = Intent(context, MusicService::class.java).apply {
                this.action = action
            }
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                PendingIntent.getForegroundService(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            } else {
                PendingIntent.getService(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            }
        }
    }
}
