package jp.gr.java_conf.mmktomato.fluffyplayer.di.component

import dagger.Component
import jp.gr.java_conf.mmktomato.fluffyplayer.di.module.AppModuleMock
import jp.gr.java_conf.mmktomato.fluffyplayer.di.module.PlaylistModuleMock
import jp.gr.java_conf.mmktomato.fluffyplayer.usecase.NotificationUseCaseTest

@Component(modules = [AppModuleMock::class, PlaylistModuleMock::class])
interface NotificationUseCaseTestComponent {
    fun inject(testInstance: NotificationUseCaseTest)
}