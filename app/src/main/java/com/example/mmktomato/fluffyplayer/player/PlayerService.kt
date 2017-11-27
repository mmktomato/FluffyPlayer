package com.example.mmktomato.fluffyplayer.player

import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.example.mmktomato.fluffyplayer.prefs.AppPrefs

/**
 * A music player service.
 */
internal class PlayerService : Service() {
    /**
     * A binder to communicate this service.
     */
    internal class LocalBinder(internal val service: PlayerService) : Binder()

    /**
     * the media player.
     */
    private val player = MediaPlayer()

    override fun onCreate() {
        super.onCreate()
    }

    override fun onBind(intent: Intent): IBinder? {
        val uri = intent.getStringExtra("uri")
        val audioAttr = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()

        player.setAudioAttributes(audioAttr)
        player.setDataSource(uri)
        player.setOnPreparedListener { mp ->
            // ALAC is not supported ...
            player.start()
        }
        player.setOnErrorListener { mp, what, extra ->
            Log.e(AppPrefs.logTag, "what:$what, extra:$extra")
            return@setOnErrorListener true
        }
        player.setOnCompletionListener {
            // TODO: fix this.
            Log.d(AppPrefs.logTag, "done.")
        }
        player.prepareAsync()

        return LocalBinder(this)
    }

    override fun onDestroy() {
        super.onDestroy()

        player.release()
    }
}
