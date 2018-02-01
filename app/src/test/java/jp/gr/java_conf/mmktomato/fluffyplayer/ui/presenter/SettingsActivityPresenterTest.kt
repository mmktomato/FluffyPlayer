package jp.gr.java_conf.mmktomato.fluffyplayer.ui.presenter

import android.widget.Button
import jp.gr.java_conf.mmktomato.fluffyplayer.DUMMY_DBX_USER_NAME
import jp.gr.java_conf.mmktomato.fluffyplayer.di.component.DependencyInjector
import jp.gr.java_conf.mmktomato.fluffyplayer.di.component.MockComponentInjector
import jp.gr.java_conf.mmktomato.fluffyplayer.dropbox.DbxProxy
import jp.gr.java_conf.mmktomato.fluffyplayer.prefs.SharedPrefsHelper
import jp.gr.java_conf.mmktomato.fluffyplayer.ui.viewmodel.SettingsActivityViewModel
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

/**
 * Tests for SettingsActivityPresenter.
 */
@RunWith(RobolectricTestRunner::class)
class SettingsActivityPresenterTest {
    private lateinit var sharedPrefs: SharedPrefsHelper
    private lateinit var dbxProxy: DbxProxy
    private lateinit var viewModel: SettingsActivityViewModel
    private lateinit var presenter: SettingsActivityPresenter

    companion object {
        @BeforeClass
        @JvmStatic
        fun setUpClass() {
            MockComponentInjector.setTestMode()
        }
    }

    @Before
    fun setUp() {
        val ctx = RuntimeEnvironment.application

        viewModel = SettingsActivityViewModel()
        presenter = SettingsActivityPresenterImpl(
                viewModel = viewModel,
                connectDropboxButton = Button(ctx))

        DependencyInjector.injector.inject(presenter as SettingsActivityPresenterImpl, ctx)

        sharedPrefs = (presenter as SettingsActivityPresenterImpl).sharedPrefs
        dbxProxy = (presenter as SettingsActivityPresenterImpl).dbxProxy
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