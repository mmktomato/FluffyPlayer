package jp.gr.java_conf.mmktomato.fluffyplayer.di.component

import android.content.Context
import jp.gr.java_conf.mmktomato.fluffyplayer.ActivityBase
import jp.gr.java_conf.mmktomato.fluffyplayer.di.module.AppModule
import jp.gr.java_conf.mmktomato.fluffyplayer.di.module.DbxModule
import jp.gr.java_conf.mmktomato.fluffyplayer.di.module.SharedPrefsModule
import jp.gr.java_conf.mmktomato.fluffyplayer.ui.presenter.FileBrowseActivityPresenterImpl
import jp.gr.java_conf.mmktomato.fluffyplayer.ui.presenter.PlayerActivityPresenterImpl
import jp.gr.java_conf.mmktomato.fluffyplayer.ui.presenter.SettingsActivityPresenterImpl

/**
 * Injects depedencies.
 */
interface ComponentInjector {
    /**
     * Inject to ActivityBase.
     *
     * @param activityBase the instance to inject dependencies.
     * @param ctx android's Context.
     */
    fun inject(activityBase: ActivityBase, ctx: Context) {
        DaggerActivityBaseComponent.builder()
                .appModule(AppModule(ctx))
                .sharedPrefsModule(SharedPrefsModule())
                .dbxModule(DbxModule())
                .build()
                .inject(activityBase)
    }

    /**
     * Inject to FileBrowseActivityPresenter.
     *
     * @param presenter the instance to inject dependencies.
     * @param ctx android's Context.
     */
    fun inject(presenter: FileBrowseActivityPresenterImpl, ctx: Context) {
        DaggerActivityPresenterComponent.builder()
                .appModule(AppModule(ctx))
                .sharedPrefsModule(SharedPrefsModule())
                .dbxModule(DbxModule())
                .build()
                .inject(presenter)
    }

    /**
     * Inject to PlayerActivityPresenter.
     *
     * @param presenter the instance to inject dependencies.
     * @param ctx android's Context.
     */
    fun inject(presenter: PlayerActivityPresenterImpl, ctx: Context) {
        DaggerActivityPresenterComponent.builder()
                .appModule(AppModule(ctx))
                .sharedPrefsModule(SharedPrefsModule())
                .dbxModule(DbxModule())
                .build()
                .inject(presenter)
    }

    /**
     * Inject to SettingsActivityPresenter.
     *
     * @param presenter the instance to inject dependencies.
     * @param ctx android's Context.
     */
    fun inject(presenter: SettingsActivityPresenterImpl, ctx: Context) {
        DaggerActivityPresenterComponent.builder()
                .appModule(AppModule(ctx))
                .sharedPrefsModule(SharedPrefsModule())
                .dbxModule(DbxModule())
                .build()
                .inject(presenter)
    }
}

var componentInjector: ComponentInjector? = null
fun createComponentInjector(): ComponentInjector = componentInjector ?: object : ComponentInjector { }
