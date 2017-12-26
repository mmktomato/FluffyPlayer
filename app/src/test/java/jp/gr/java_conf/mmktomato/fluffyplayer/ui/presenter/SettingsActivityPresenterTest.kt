package jp.gr.java_conf.mmktomato.fluffyplayer.ui.presenter

import android.content.Context
import jp.gr.java_conf.mmktomato.fluffyplayer.DUMMY_DBX_USER_NAME
import jp.gr.java_conf.mmktomato.fluffyplayer.R
import jp.gr.java_conf.mmktomato.fluffyplayer.SettingsActivity
import jp.gr.java_conf.mmktomato.fluffyplayer.di.component.DaggerSettingsActivityPresenterTestComponent
import jp.gr.java_conf.mmktomato.fluffyplayer.di.module.AppModuleMock
import jp.gr.java_conf.mmktomato.fluffyplayer.di.module.DbxModuleMock
import jp.gr.java_conf.mmktomato.fluffyplayer.di.module.SharedPrefsModuleMock
import jp.gr.java_conf.mmktomato.fluffyplayer.dropbox.DbxProxy
import jp.gr.java_conf.mmktomato.fluffyplayer.prefs.SharedPrefsHelper
import jp.gr.java_conf.mmktomato.fluffyplayer.ui.viewmodel.SettingsActivityViewModel
import kotlinx.android.synthetic.main.activity_settings.view.*
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import javax.inject.Inject

/**
 * Tests for SettingsActivityPresenter.
 */
@RunWith(RobolectricTestRunner::class)
class SettingsActivityPresenterTest {
    @Inject
    lateinit var ctx: Context

    @Inject
    lateinit var sharedPrefs: SharedPrefsHelper

    @Inject
    lateinit var dbxProxy: DbxProxy

    private val activity = Robolectric.setupActivity(SettingsActivity::class.java)
    private val viewModel = SettingsActivityViewModel()
    private lateinit var presenter: SettingsActivityPresenter

    @Before
    fun setUp() {
        DaggerSettingsActivityPresenterTestComponent.builder()
                .appModuleMock(AppModuleMock())
                .sharedPrefsModuleMock(SharedPrefsModuleMock())
                .dbxModuleMock(DbxModuleMock(isAuthenticated = true))
                .build()
                .inject(this)

        presenter = SettingsActivityPresenterImpl(
                sharedPrefs = sharedPrefs,
                dbxProxy = dbxProxy,
                viewModel = viewModel,
                connectDropboxButton = activity.findViewById(R.id.connectDropboxButton))

    }

    @Test
    fun refreshUi_WhenDropboxConnected() {
        runBlocking {
            presenter.refreshUi().join()

            assertEquals(DUMMY_DBX_USER_NAME, viewModel.dropboxAuthStatusText.get())
            assertEquals("disconnect", viewModel.connectDropboxButtonText.get())
        }
    }

    @Test
    fun refreshUi_WhenDropboxNotConnected() {
        Mockito.`when`(dbxProxy.isAuthenticated).thenReturn(false)

        runBlocking {
            presenter.refreshUi().join()

            assertEquals("(not connected)", viewModel.dropboxAuthStatusText.get())
            assertEquals("connect", viewModel.connectDropboxButtonText.get())
        }
    }
}