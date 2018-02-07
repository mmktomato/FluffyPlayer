package jp.gr.java_conf.mmktomato.fluffyplayer.di.component

import android.content.Context
import jp.gr.java_conf.mmktomato.fluffyplayer.ActivityBase
import jp.gr.java_conf.mmktomato.fluffyplayer.db.AppDatabase
import jp.gr.java_conf.mmktomato.fluffyplayer.di.module.*
import jp.gr.java_conf.mmktomato.fluffyplayer.ui.presenter.FileBrowseActivityPresenterImpl
import jp.gr.java_conf.mmktomato.fluffyplayer.ui.presenter.PlayerActivityPresenterImpl
import jp.gr.java_conf.mmktomato.fluffyplayer.ui.presenter.SettingsActivityPresenterImpl
import jp.gr.java_conf.mmktomato.fluffyplayer.usecase.NotificationUseCase
import jp.gr.java_conf.mmktomato.fluffyplayer.usecase.ScrobbleUseCase

/**
 * Injects depedencies.
 */
abstract class ComponentInjector {
    /**
     * the DatabaseComponent.
     */
    protected lateinit var dbComponent: DatabaseComponent

    /**
     * the ScrobbleComponent.
     */
    protected lateinit var scrobbleComponent: ScrobbleComponent

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

        presenter.db = createAppDatabase(ctx)
        presenter.notificationUseCase = createNotificationUseCase(ctx)
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
     * Returns an instance of AppDatabase.
     *
     * @param ctx android's Context.
     */
    open fun createAppDatabase(ctx: Context): AppDatabase {
        if (!::dbComponent.isInitialized) {
            dbComponent = DaggerDatabaseComponent.builder()
                    .appModule(AppModule(ctx))
                    .databaseModule(DatabaseModule())
                    .build()
        }
        return dbComponent.createAppDatabase()
    }

    /**
     * Returns an instance of NotificationUseCase.
     *
     * @param ctx android's Context.
     */
    open fun createNotificationUseCase(ctx: Context): NotificationUseCase {
        return DaggerNotificationComponent.builder()
                .appModule(AppModule(ctx))
                .notificationModule(NotificationModule())
                .build()
                .createNotificationUseCase()
    }

    /**
     * Returns an instance of ScrobbleComponent.
     *
     * @param ctx android's Context.
     */
    open fun createScrobbleUseCase(ctx: Context): ScrobbleUseCase {
        if (!::scrobbleComponent.isInitialized) {
            scrobbleComponent = DaggerScrobbleComponent.builder()
                    .appModule(AppModule(ctx))
                    .sharedPrefsModule(SharedPrefsModule())
                    .scrobbleModule(ScrobbleModule())
                    .build()
        }
        return scrobbleComponent.createScrobbleUseCase()

        // TODO: override this method in sub class.
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
