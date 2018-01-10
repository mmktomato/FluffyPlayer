package jp.gr.java_conf.mmktomato.fluffyplayer.player

import android.app.*
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
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
     * the Dropbox's metadata.
     */
    private lateinit var dbxMetadata: DbxNodeMetadata

    /**
     * the NotificationUseCase.
     */
    private val notificationUseCase = NotificationUseCase()

    init {
        binder = PlayerServiceBinder(player)
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
