package jp.gr.java_conf.mmktomato.fluffyplayer.di.component

import dagger.Component
import jp.gr.java_conf.mmktomato.fluffyplayer.di.module.AppModuleMock
import jp.gr.java_conf.mmktomato.fluffyplayer.usecase.NotificationUseCaseTest

@Component(modules = [AppModuleMock::class])
interface NotificationUseCaseTestComponent {
    fun inject(testInstance: NotificationUseCaseTest)
}