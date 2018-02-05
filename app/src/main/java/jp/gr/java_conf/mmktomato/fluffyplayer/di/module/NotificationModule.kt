package jp.gr.java_conf.mmktomato.fluffyplayer.di.module

import android.app.NotificationManager
import android.content.Context
import dagger.Module
import dagger.Provides

@Module
class NotificationModule {
    @Provides
    fun provideNotificationManager(ctx: Context): NotificationManager {
        return ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
}