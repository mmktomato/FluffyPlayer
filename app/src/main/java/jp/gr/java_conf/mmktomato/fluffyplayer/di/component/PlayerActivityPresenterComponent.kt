package jp.gr.java_conf.mmktomato.fluffyplayer.di.component

import dagger.Component
import jp.gr.java_conf.mmktomato.fluffyplayer.di.module.AppModule
import jp.gr.java_conf.mmktomato.fluffyplayer.di.module.DatabaseModule
import jp.gr.java_conf.mmktomato.fluffyplayer.di.scope.AppScope
import jp.gr.java_conf.mmktomato.fluffyplayer.ui.presenter.PlayerActivityPresenterImpl

@AppScope
@Component(modules = [AppModule::class, DatabaseModule::class])
interface PlayerActivityPresenterComponent {
    fun inject(presenter: PlayerActivityPresenterImpl)
}