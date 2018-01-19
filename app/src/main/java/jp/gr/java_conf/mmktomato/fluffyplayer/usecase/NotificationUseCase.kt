package jp.gr.java_conf.mmktomato.fluffyplayer.usecase

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import jp.gr.java_conf.mmktomato.fluffyplayer.PlayerActivity
import jp.gr.java_conf.mmktomato.fluffyplayer.R
import jp.gr.java_conf.mmktomato.fluffyplayer.db.model.PlaylistItem
import jp.gr.java_conf.mmktomato.fluffyplayer.dropbox.DbxNodeMetadata

/**
 * Business logics for Notification.
 */
class NotificationUseCase {
    /**
     * Returns a now playing notification.
     *
     * @param ctx an android context.
     * @param nowPlayingItem a PlaylistItem to pass the PlayerActivity via Intent.
     * @param musicTitle a music title to be shown on notification.
     */
    internal fun createNowPlayingNotification(ctx: Context, nowPlayingItem: PlaylistItem, musicTitle: String): Notification {
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
}