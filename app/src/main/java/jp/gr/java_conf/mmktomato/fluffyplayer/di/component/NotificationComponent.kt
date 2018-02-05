package jp.gr.java_conf.mmktomato.fluffyplayer.di.component

import dagger.Component
import jp.gr.java_conf.mmktomato.fluffyplayer.di.module.AppModule
import jp.gr.java_conf.mmktomato.fluffyplayer.di.module.NotificationModule
import jp.gr.java_conf.mmktomato.fluffyplayer.usecase.NotificationUseCase

@Component(modules = [AppModule::class, NotificationModule::class])
interface NotificationComponent {
    fun createNotificationUseCase(): NotificationUseCase
}