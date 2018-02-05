package jp.gr.java_conf.mmktomato.fluffyplayer.di.component

import dagger.Component
import jp.gr.java_conf.mmktomato.fluffyplayer.di.module.AppModuleMock
import jp.gr.java_conf.mmktomato.fluffyplayer.di.module.NotificationModuleMock
import jp.gr.java_conf.mmktomato.fluffyplayer.usecase.NotificationUseCase

@Component(modules = [AppModuleMock::class, NotificationModuleMock::class])
interface NotificationComponentMock {
    fun createNotificationUseCaseMock(): NotificationUseCase
}