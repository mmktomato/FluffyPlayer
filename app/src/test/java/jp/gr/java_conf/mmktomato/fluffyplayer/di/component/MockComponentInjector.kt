package jp.gr.java_conf.mmktomato.fluffyplayer.di.component

import android.content.Context
import jp.gr.java_conf.mmktomato.fluffyplayer.ActivityBase
import jp.gr.java_conf.mmktomato.fluffyplayer.db.AppDatabase
import jp.gr.java_conf.mmktomato.fluffyplayer.di.module.*
import jp.gr.java_conf.mmktomato.fluffyplayer.player.PlayerService
import jp.gr.java_conf.mmktomato.fluffyplayer.ui.presenter.FileBrowseActivityPresenterImpl
import jp.gr.java_conf.mmktomato.fluffyplayer.ui.presenter.PlayerActivityPresenterImpl
import jp.gr.java_conf.mmktomato.fluffyplayer.ui.presenter.SettingsActivityPresenterImpl
import jp.gr.java_conf.mmktomato.fluffyplayer.usecase.NotificationUseCase
import kotlinx.coroutines.experimental.async

/**
 * Inject mocks.
 */
class MockComponentInjector : ComponentInjector() {
    companion object {
        /**
         * Sets test mode.
         */
        fun setTestMode() {
            DependencyInjector.injector = MockComponentInjector()
        }
    }

    /**
     * Inject to ActivityBase.
     *
     * @param activityBase the instance to inject dependencies.
     * @param ctx this argument is ignored.
     */
    override fun inject(activityBase: ActivityBase, ctx: Context) {
        DaggerActivityBaseComponentMock.builder()
                .appModuleMock(AppModuleMock())
                .sharedPrefsModuleMock(SharedPrefsModuleMock())
                .dbxModuleMock(DbxModuleMock(true))
                .build()
                .inject(activityBase)
    }

    /**
     * Inject to FileBrowseActivityPresenter.
     *
     * @param presenter the instance to inject dependencies.
     * @param ctx this argument is ignored.
     */
    override fun inject(presenter: FileBrowseActivityPresenterImpl, ctx: Context) {
        // TODO: implement this when tests of `FileBrowseActivityPresenter` is implemented.
        //DaggerActivityPresenterComponentMock.builder()
        //        .appModuleMock(AppModuleMock())
        //        .sharedPrefsModuleMock(SharedPrefsModuleMock())
        //        .dbxModuleMock(DbxModuleMock(true))
        //        .build()
        //        .inject(presenter)

        super.inject(presenter, ctx)
    }

    /**
     * Inject to PlayerActivityPresenter.
     *
     * @param presenter the instance to inject dependencies.
     * @param ctx this argument is ignored.
     */
    override suspend fun inject(presenter: PlayerActivityPresenterImpl, ctx: Context) {
        DaggerActivityPresenterComponentMock.builder()
                .appModuleMock(AppModuleMock())
                .sharedPrefsModuleMock(SharedPrefsModuleMock())
                .dbxModuleMock(DbxModuleMock(true))
                .build()
                .inject(presenter)

        presenter.db = createAppDatabase(ctx)
        presenter.notificationUseCase = createNotificationUseCase(ctx)
        presenter.scrobbleUseCase = createScrobbleUseCase(ctx).await()
    }

    /**
     * Inject to SettingsActivityPresenter.
     *
     * @param presenter the instance to inject dependencies.
     * @param ctx this argument is ignored.
     */
    override fun inject(presenter: SettingsActivityPresenterImpl, ctx: Context) {
        DaggerActivityPresenterComponentMock.builder()
                .appModuleMock(AppModuleMock())
                .sharedPrefsModuleMock(SharedPrefsModuleMock())
                .dbxModuleMock(DbxModuleMock(true))
                .build()
                .inject(presenter)
    }

    /**
     * Returns an instance of AppDatabase.
     *
     * @param ctx this argument is ignored.
     */
    override fun createAppDatabase(ctx: Context): AppDatabase {
        return DaggerDatabaseComponentMock.builder()
                .appModuleMock(AppModuleMock())
                .databaseModuleMock(DatabaseModuleMock())
                .build()
                .createAppDatabaseMock()
    }

    /**
     * Returns an instance of NotificationUseCase.
     *
     * @param ctx this argument is ignored.
     */
    override fun createNotificationUseCase(ctx: Context): NotificationUseCase {
        return DaggerNotificationComponentMock.builder()
                .appModuleMock(AppModuleMock())
                .notificationModuleMock(NotificationModuleMock())
                .build()
                .createNotificationUseCaseMock()
    }

    /**
     * Returns an instance of ScrobbleComponent.
     *
     * @param ctx this argument is ignored.
     */
    override fun createScrobbleUseCase(ctx: Context) = async {
        return@async DaggerScrobbleComponentMock.builder()
                .scrobbleModuleMock(ScrobbleModuleMock())
                .build()
                .createScrobbleUseCaseMock()
    }
}