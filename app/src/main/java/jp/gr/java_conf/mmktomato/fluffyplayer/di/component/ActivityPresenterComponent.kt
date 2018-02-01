package jp.gr.java_conf.mmktomato.fluffyplayer.di.component

import dagger.Component
import jp.gr.java_conf.mmktomato.fluffyplayer.di.module.AppModule
import jp.gr.java_conf.mmktomato.fluffyplayer.di.module.DbxModule
import jp.gr.java_conf.mmktomato.fluffyplayer.di.module.SharedPrefsModule
import jp.gr.java_conf.mmktomato.fluffyplayer.ui.presenter.FileBrowseActivityPresenterImpl
import jp.gr.java_conf.mmktomato.fluffyplayer.ui.presenter.PlayerActivityPresenterImpl
import jp.gr.java_conf.mmktomato.fluffyplayer.ui.presenter.SettingsActivityPresenterImpl

@Component(modules = [AppModule::class, SharedPrefsModule::class, DbxModule::class])
interface ActivityPresenterComponent {
    fun inject(presenter: FileBrowseActivityPresenterImpl)
    fun inject(presenter: PlayerActivityPresenterImpl)
    fun inject(presenter: SettingsActivityPresenterImpl)
}