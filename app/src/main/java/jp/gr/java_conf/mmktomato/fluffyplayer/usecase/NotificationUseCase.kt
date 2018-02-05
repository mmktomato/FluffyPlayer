package jp.gr.java_conf.mmktomato.fluffyplayer.usecase

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import jp.gr.java_conf.mmktomato.fluffyplayer.PlayerActivity
import jp.gr.java_conf.mmktomato.fluffyplayer.R
import jp.gr.java_conf.mmktomato.fluffyplayer.db.model.PlaylistItem
import jp.gr.java_conf.mmktomato.fluffyplayer.dropbox.DbxNodeMetadata
import jp.gr.java_conf.mmktomato.fluffyplayer.entity.MusicMetadata
import jp.gr.java_conf.mmktomato.fluffyplayer.player.PlayerService
import jp.gr.java_conf.mmktomato.fluffyplayer.prefs.AppPrefs
import javax.inject.Inject

/**
 * Business logics for Notification.
 *
 * @param ctx: the android's Context.
 * @param mNotificationManager the notificationManager.
 */
class NotificationUseCase @Inject constructor(
        private val ctx: Context,
        val notificationManager: NotificationManager) {

    /**
     * Returns a now playing notification.
     *
     * @param nowPlayingItem a PlaylistItem to pass the PlayerActivity via Intent.
     * @param musicTitle a music title to be shown on notification.
     */
    fun createNowPlayingNotification(nowPlayingItem: PlaylistItem, musicTitle: String): Notification {
        val notificationIntent = Intent(ctx, PlayerActivity::class.java)
        notificationIntent.putExtra("nowPlayingItem", nowPlayingItem)
        val pendingIntent = PendingIntent.getActivities(
                ctx, 0, arrayOf(notificationIntent), PendingIntent.FLAG_UPDATE_CURRENT)

        val ret = NotificationCompat.Builder(ctx)
                .setSmallIcon(R.drawable.ic_no_image)  // TODO: app icon.
                .setContentTitle("Now playing")
                .setContentText(musicTitle)
                .setContentIntent(pendingIntent)
                .build()
        ret.flags = Notification.FLAG_NO_CLEAR

        return ret
    }

    /**
     * Updates now playing notification.
     *
     * @param nowPlayingItem a PlaylistItem to pass the PlayerActivity via Intent.
     * @param musicTitle a music title to be shown on notification.
     */
    fun updateNowPlayingNotification(nowPlayingItem: PlaylistItem, musicTitle: String) {
        val notification = createNowPlayingNotification(nowPlayingItem, musicTitle)

        notificationManager.notify(AppPrefs.NOW_PLAYING_NOTIFICATION_ID, notification)
    }
}