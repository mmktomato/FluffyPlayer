package jp.gr.java_conf.mmktomato.fluffyplayer.player

import android.app.*
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.util.Log
import jp.gr.java_conf.mmktomato.fluffyplayer.PlayerActivity
import jp.gr.java_conf.mmktomato.fluffyplayer.R
import jp.gr.java_conf.mmktomato.fluffyplayer.dropbox.DbxNodeMetadata
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
        private val onPlayerStateChangedListeners = mutableMapOf<Int, () -> Unit>()

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
            onPlayerStateChangedListeners.values.forEach { it.invoke() }
        }
    }

    /**
     * the media player.
     */
    private val player = MediaPlayer()

    /**
     * the Dropbox's metadata.
     */
    private lateinit var dbxMetadata: DbxNodeMetadata

    /**
     * Returns a new instance of `Notification`.
     */
    private fun createNotification(): Notification {

        // TODO: fix
        // PlayerActivity is stacked per launched from notification.
        // Maybe I had better to set SingleTop to PlayerActivity?

        val notificationIntent = Intent(this, PlayerActivity::class.java)
        notificationIntent.putExtra("dbxMetadata", dbxMetadata)
        val pendingIntent = PendingIntent.getActivities(
                this, 0, arrayOf(notificationIntent), PendingIntent.FLAG_UPDATE_CURRENT)

        return NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_no_image)
                .setContentTitle("test title")
                .setContentText("test content")
                .setContentIntent(pendingIntent)
                .build()
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onBind(intent: Intent): IBinder? {
        dbxMetadata = intent.getSerializableExtra("dbxMetadata") as DbxNodeMetadata

        val audioAttr = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        val binder = LocalBinder(player)

        player.setAudioAttributes(audioAttr)
        player.setOnErrorListener { mp, what, extra ->
            Log.e(AppPrefs.logTag, "what:$what, extra:$extra")
            return@setOnErrorListener true
        }
        player.setOnCompletionListener {
            binder.notifyOnPlayerStateChanged()
        }

        // start foreground.
        val notificationId = 1
        startForeground(notificationId, createNotification())

        return binder
    }

    override fun onDestroy() {
        super.onDestroy()

        player.release()
    }
}
