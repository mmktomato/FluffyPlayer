package jp.gr.java_conf.mmktomato.fluffyplayer.ui.presenter

import android.content.Context
import android.widget.Button
import jp.gr.java_conf.mmktomato.fluffyplayer.DUMMY_DBX_USER_NAME
import jp.gr.java_conf.mmktomato.fluffyplayer.di.component.DaggerSettingsActivityPresenterTestComponent
import jp.gr.java_conf.mmktomato.fluffyplayer.di.module.AppModuleMock
import jp.gr.java_conf.mmktomato.fluffyplayer.di.module.DbxModuleMock
import jp.gr.java_conf.mmktomato.fluffyplayer.di.module.SharedPrefsModuleMock
import jp.gr.java_conf.mmktomato.fluffyplayer.dropbox.DbxProxy
import jp.gr.java_conf.mmktomato.fluffyplayer.prefs.SharedPrefsHelper
import jp.gr.java_conf.mmktomato.fluffyplayer.ui.viewmodel.SettingsActivityViewModel
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
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

    private lateinit var viewModel: SettingsActivityViewModel
    private lateinit var presenter: SettingsActivityPresenter

    @Before
    fun setUp() {
        DaggerSettingsActivityPresenterTestComponent.builder()
                .appModuleMock(AppModuleMock())
                .sharedPrefsModuleMock(SharedPrefsModuleMock())
                .dbxModuleMock(DbxModuleMock(isAuthenticated = true))
                .build()
                .inject(this)

        viewModel = SettingsActivityViewModel()
        presenter = SettingsActivityPresenterImpl(
                sharedPrefs = sharedPrefs,
                dbxProxy = dbxProxy,
                viewModel = viewModel,
                connectDropboxButton = Button(ctx))
    }

    /**
     * Asserts values of `viewModel`.
     */
    private fun assertViewModel(dropboxAuthStatusText: String, connectDropboxButtonText: String) {
        assertEquals(dropboxAuthStatusText, viewModel.dropboxAuthStatusText.get())
        assertEquals(connectDropboxButtonText, viewModel.connectDropboxButtonText.get())
    }

    /**
     * Asserts values of 'viewModel` when connected to the Dropbox.
     */
    private fun assertViewModelForConnected() {
        assertViewModel(
                dropboxAuthStatusText = DUMMY_DBX_USER_NAME,
                connectDropboxButtonText = "disconnect")
    }

    /**
     * Asserts values of `viewModel` when not connected to the Dropbox.
     */
    private fun assertViewModelForNotConnected() {
        assertViewModel(
                dropboxAuthStatusText = "(not connected)",
                connectDropboxButtonText = "connect")
    }

    @Test
    fun onCreate_WhenDropboxConnected() {
        presenter.onCreate()

        assertViewModelForConnected()
    }

    @Test
    fun onCreate_WhenDropboxNotConnected() {
        `when`(dbxProxy.isAuthenticated).thenReturn(false)

        presenter.onCreate()

        assertViewModelForNotConnected()
    }

    @Test
    fun refreshUi_WhenDropboxConnected() {
        runBlocking {
            presenter.refreshUi().join()

            assertViewModelForConnected()
        }
    }

    @Test
    fun refreshUi_WhenDropboxNotConnected() {
        `when`(dbxProxy.isAuthenticated).thenReturn(false)

        runBlocking {
            presenter.refreshUi().join()

            assertViewModelForNotConnected()
        }
    }

    @Test
    fun onConnectDropboxButtonClick_WhenDropboxConnected() {
        presenter.onConnectDropboxButtonClick()

        verify(sharedPrefs, times(1)).removeDbxAccessToken()
    }

    @Test
    fun onConnectDropboxButtonClick_WhenDropboxNotConnected() {
        `when`(dbxProxy.isAuthenticated).thenReturn(false)

        presenter.onConnectDropboxButtonClick()

        verify(dbxProxy, times(1)).auth()
    }

    @Test
    fun onConnectDropboxButtonClick_AuthAndResume() {
        `when`(dbxProxy.isAuthenticated).thenReturn(false)

        presenter.onConnectDropboxButtonClick()
        presenter.onResume()

        verify(dbxProxy, times(1)).saveAccessToken()
    }
}