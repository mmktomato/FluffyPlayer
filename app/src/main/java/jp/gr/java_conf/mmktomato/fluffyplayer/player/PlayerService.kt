package jp.gr.java_conf.mmktomato.fluffyplayer.player

import android.app.*
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.util.Log
import jp.gr.java_conf.mmktomato.fluffyplayer.R
import jp.gr.java_conf.mmktomato.fluffyplayer.dropbox.DbxNodeMetadata
import jp.gr.java_conf.mmktomato.fluffyplayer.prefs.AppPrefs
import jp.gr.java_conf.mmktomato.fluffyplayer.usecase.NotificationUseCase

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

        // TODO: Refactoring.

        /**
         * the counter of listener
         */
        private var listenerCounter = 0

        /**
         * the map of listeners for onPlayerStateChanged.
         */
        private val onPlayerStateChangedListeners = mutableMapOf<Int, () -> Unit>()

        /**
         * the map of listeners for onMusicChanged.
         */
        private val onMusicChangedListeners = mutableMapOf<Int, () -> Unit>()

        /**
         * Prepares dataSource.
         *
         * @param uri the music uri.
         */
        suspend fun prepare(uri: String) {
            player.setDataSource(uri)
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
         * Resets the player.
         */
        fun reset() {
            player.stop()
            notifyOnPlayerStateChanged()

            player.reset()
            notifyOnMusicChanged()
        }

        /**
         * Toggles playing state.
         */
        fun togglePlaying() {
            if (isPlaying) {
                pause()
            } else {
                start()
            }
        }

        /**
         * Returns whether the player is playing a music.
         */
        val isPlaying
            get() = player.isPlaying

        /**
         * Adds listener for onPlayerStateChanged.
         *
         * @param listener the listener to add.
         */
        fun addOnPlayerStateChangedListener(listener: () -> Unit): Int {
            onPlayerStateChangedListeners.put(++listenerCounter, listener)
            return listenerCounter
        }

        /**
         * Adds listener for onMusicChanged.
         *
         * @param listener the listener to add.
         */
        fun addOnMusicChangedListener(listener: () -> Unit): Int {
            onMusicChangedListeners.put(++listenerCounter, listener)
            return listenerCounter
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
         * Removes listener for onMusicChanged.
         *
         * @param index the listener index.
         */
        fun removeOnMusicChangedListener(index: Int) {
            onMusicChangedListeners.remove(index)
        }

        /**
         * Notifies onPlayerStateChanged.
         */
        fun notifyOnPlayerStateChanged() {
            onPlayerStateChangedListeners.values.forEach { it.invoke() }
        }

        /**
         * Notifies onMusicChanged.
         */
        fun notifyOnMusicChanged() {
            onMusicChangedListeners.values.forEach { it.invoke() }
        }
    }

    companion object {
        /**
         * the notification id.
         */
        val NOTIFICATION_ID = 1
    }

    /**
     * the media player.
     */
    private val player = MediaPlayer()

    /**
     * the binder instance to communicate with activity.
     */
    private val binder: LocalBinder

    /**
     * the Dropbox's metadata.
     */
    private lateinit var dbxMetadata: DbxNodeMetadata

    /**
     * the NotificationUseCase.
     */
    private val notificationUseCase = NotificationUseCase()

    init {
        binder = LocalBinder(player)
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val intentMetadata = intent.getSerializableExtra("dbxMetadata") as DbxNodeMetadata
        var isServiceStarted = false

        if (this::dbxMetadata.isInitialized) {
            isServiceStarted = true

            if (dbxMetadata.path != intentMetadata.path) {
                binder.reset()
            }
        }
        dbxMetadata = intentMetadata

        if (!isServiceStarted) {
            // start foreground.
            val notification = notificationUseCase.createNowPlayingNotification(
                    this, dbxMetadata, getString(R.string.now_loading_text))
            startForeground(NOTIFICATION_ID, notification)
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        val audioAttr = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()

        player.setAudioAttributes(audioAttr)
        player.setOnErrorListener { mp, what, extra ->
            Log.e(AppPrefs.logTag, "what:$what, extra:$extra")
            return@setOnErrorListener true
        }
        player.setOnCompletionListener {
            binder.notifyOnPlayerStateChanged()

            stopSelf()
        }

        return binder
    }

    override fun onDestroy() {
        super.onDestroy()

        player.release()
    }
}
