package jp.gr.java_conf.mmktomato.fluffyplayer.di.component

import dagger.Component
import jp.gr.java_conf.mmktomato.fluffyplayer.di.module.PlayerModuleMock
import jp.gr.java_conf.mmktomato.fluffyplayer.player.PlayerServiceBinderTest

@Component(modules = [PlayerModuleMock::class])
interface PlayerServiceBinderTestComponent {
    fun inject(testInstance: PlayerServiceBinderTest)
}