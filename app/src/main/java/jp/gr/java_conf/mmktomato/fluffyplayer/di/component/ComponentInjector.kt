package jp.gr.java_conf.mmktomato.fluffyplayer.di.component

import android.content.Context
import jp.gr.java_conf.mmktomato.fluffyplayer.ActivityBase
import jp.gr.java_conf.mmktomato.fluffyplayer.di.module.AppModule
import jp.gr.java_conf.mmktomato.fluffyplayer.di.module.DatabaseModule
import jp.gr.java_conf.mmktomato.fluffyplayer.di.module.DbxModule
import jp.gr.java_conf.mmktomato.fluffyplayer.di.module.SharedPrefsModule
import jp.gr.java_conf.mmktomato.fluffyplayer.ui.presenter.FileBrowseActivityPresenterImpl
import jp.gr.java_conf.mmktomato.fluffyplayer.ui.presenter.PlayerActivityPresenterImpl
import jp.gr.java_conf.mmktomato.fluffyplayer.ui.presenter.SettingsActivityPresenterImpl

/**
 * Injects depedencies.
 */
abstract class ComponentInjector {
    /**
     * the DatabaseComponent.
     */
    protected lateinit var dbComponent: DatabaseComponent

    /**
     * Inject to ActivityBase.
     *
     * @param activityBase the instance to inject dependencies.
     * @param ctx android's Context.
     */
    open fun inject(activityBase: ActivityBase, ctx: Context) {
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
    open fun inject(presenter: FileBrowseActivityPresenterImpl, ctx: Context) {
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
    open fun inject(presenter: PlayerActivityPresenterImpl, ctx: Context) {
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
    open fun inject(presenter: SettingsActivityPresenterImpl, ctx: Context) {
        DaggerActivityPresenterComponent.builder()
                .appModule(AppModule(ctx))
                .sharedPrefsModule(SharedPrefsModule())
                .dbxModule(DbxModule())
                .build()
                .inject(presenter)
    }

    /**
     * Inject AppDatabase to PlayerActivityPresenter.
     *
     * @param presenter the instance to inject dependencies.
     */
    open fun injectAppDatabase(presenter: PlayerActivityPresenterImpl) {
        if (!::dbComponent.isInitialized) {
            dbComponent = DaggerDatabaseComponent.builder()
                    .appModule(AppModule(presenter.ctx))
                    .databaseModule(DatabaseModule())
                    .build()
        }
        presenter.db = dbComponent.createAppDatabase()
    }
}

object DependencyInjector {
    private lateinit var mInjector: ComponentInjector

    var injector: ComponentInjector
            get() {
                if (!::mInjector.isInitialized) {
                    mInjector = object : ComponentInjector() { }
                }
                return mInjector
            }
            set(value) {
                mInjector = value
            }
}
