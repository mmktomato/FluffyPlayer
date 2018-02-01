package jp.gr.java_conf.mmktomato.fluffyplayer.di.component

import android.content.Context
import jp.gr.java_conf.mmktomato.fluffyplayer.ActivityBase
import jp.gr.java_conf.mmktomato.fluffyplayer.di.module.AppModuleMock
import jp.gr.java_conf.mmktomato.fluffyplayer.di.module.DatabaseModuleMock
import jp.gr.java_conf.mmktomato.fluffyplayer.di.module.DbxModuleMock
import jp.gr.java_conf.mmktomato.fluffyplayer.di.module.SharedPrefsModuleMock
import jp.gr.java_conf.mmktomato.fluffyplayer.ui.presenter.FileBrowseActivityPresenterImpl
import jp.gr.java_conf.mmktomato.fluffyplayer.ui.presenter.PlayerActivityPresenterImpl
import jp.gr.java_conf.mmktomato.fluffyplayer.ui.presenter.SettingsActivityPresenterImpl

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
    override fun inject(presenter: PlayerActivityPresenterImpl, ctx: Context) {
        DaggerActivityPresenterComponentMock.builder()
                .appModuleMock(AppModuleMock())
                .sharedPrefsModuleMock(SharedPrefsModuleMock())
                .dbxModuleMock(DbxModuleMock(true))
                .build()
                .inject(presenter)
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
     * Inject AppDatabase to PlayerActivityPresenter.
     *
     * @param presenter the instance to inject dependencies.
     */
    override fun injectAppDatabase(presenter: PlayerActivityPresenterImpl) {
        presenter.db = DaggerDatabaseComponentMock.builder()
                .appModuleMock(AppModuleMock())
                .databaseModuleMock(DatabaseModuleMock())
                .build()
                .createAppDatabaseMock()
    }
}