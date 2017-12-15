package jp.gr.java_conf.mmktomato.fluffyplayer.player

import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.util.Log
import jp.gr.java_conf.mmktomato.fluffyplayer.prefs.AppPrefs

/**
 * A music player service.
 */
internal class PlayerService : Service() {
    /**
     * A binder to communicate this service.
     *
     * @param player a media player.
     */
    internal class LocalBinder(private val player: MediaPlayer) : Binder() {
        /**
         * the counter of listener
         */
        private var listenerCounter = 0

        /**
         * the map of listeners for onPlayerStateChanged.
         */
        private val onPlayerStateChangedListeners = mutableMapOf<Int, (Boolean) -> Unit>()

        /**
         * Prepares datasource.
         */
        fun prepare() {
            player.prepare()
        }

        /**
         * Starts playing the music.
         */
        fun start() {
            // ALAC is not supported ...
            player.start()
            notifyOnPlayerStateChanged()
        }

        /**
         * Pauses the music.
         */
        fun pause() {
            player.pause()
            notifyOnPlayerStateChanged()
        }

        /**
         * Toggles playing state.
         */
        fun togglePlaying() {
            if (player.isPlaying) {
                pause()
            } else {
                start()
            }
        }

        /**
         * Adds listener for onPlayerStateChanged.
         *
         * @param listener the listener to add.
         */
        fun addOnPlayerStateChangedListener(listener: (Boolean) -> Unit): Int {
            val index = ++listenerCounter
            onPlayerStateChangedListeners.put(index, listener)
            return index
        }

        /**
         * Removes listener for onPlayerStateChanged.
         *
         * @param index the listener index.
         */
        fun removeOnPlayerStateChangedListener(index: Int) {
            onPlayerStateChangedListeners.remove(index)
        }

        /**
         * Notifies onPlayerStateChanged.
         */
        fun notifyOnPlayerStateChanged() {
            onPlayerStateChangedListeners.values.forEach { it.invoke(player.isPlaying) }
        }
    }

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
        val binder = LocalBinder(player)

        player.setAudioAttributes(audioAttr)
        player.setDataSource(uri)
        player.setOnErrorListener { mp, what, extra ->
            Log.e(AppPrefs.logTag, "what:$what, extra:$extra")
            return@setOnErrorListener true
        }
        player.setOnCompletionListener {
            binder.notifyOnPlayerStateChanged()
        }

        return binder
    }

    override fun onDestroy() {
        super.onDestroy()

        player.release()
    }
}
