package jp.gr.java_conf.mmktomato.fluffyplayer.di.module

import android.app.NotificationManager
import android.content.Context
import dagger.Module
import dagger.Provides
import org.mockito.Mockito

@Module
class NotificationModuleMock {
    @Provides
    fun provideNotificationManagerMock(ctx: Context): NotificationManager {
        return Mockito.mock(NotificationManager::class.java)
    }
}