package jp.gr.java_conf.mmktomato.fluffyplayer.player

import android.app.*
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.IBinder
import android.util.Log
import jp.gr.java_conf.mmktomato.fluffyplayer.R
import jp.gr.java_conf.mmktomato.fluffyplayer.db.model.PlaylistItem
import jp.gr.java_conf.mmktomato.fluffyplayer.dropbox.DbxNodeMetadata
import jp.gr.java_conf.mmktomato.fluffyplayer.prefs.AppPrefs
import jp.gr.java_conf.mmktomato.fluffyplayer.usecase.NotificationUseCase

/**
 * A music player service.
 */
internal class PlayerService : Service() {
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
    private val binder: PlayerServiceBinder

    /**
     * the NotificationUseCase.
     */
    private val notificationUseCase = NotificationUseCase()

    /**
     * whether this service is started.
     */
    private var isServiceStarted = false

    init {
        binder = PlayerServiceBinder(player)
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val dbxMetadata = intent.getSerializableExtra("dbxMetadata") as DbxNodeMetadata?
        val nowPlayingItem = intent.getSerializableExtra("nowPlayingItem") as PlaylistItem

        if (dbxMetadata != null) {
            binder.reset()
        }

        if (!isServiceStarted) {
            // start foreground.
            val notification = notificationUseCase.createNowPlayingNotification(
                    this, nowPlayingItem, getString(R.string.now_loading_text))
            startForeground(NOTIFICATION_ID, notification)
        }

        isServiceStarted = true
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
            //binder.notifyOnPlayerStateChanged()
            binder.reset()

            stopSelf()
        }

        return binder
    }

    override fun onDestroy() {
        super.onDestroy()

        player.release()
    }
}
