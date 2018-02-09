package jp.gr.java_conf.mmktomato.fluffyplayer.ui.presenter

import android.widget.Button
import jp.gr.java_conf.mmktomato.fluffyplayer.DUMMY_DBX_USER_NAME
import jp.gr.java_conf.mmktomato.fluffyplayer.DUMMY_LAST_FM_USER_NAME
import jp.gr.java_conf.mmktomato.fluffyplayer.di.component.DependencyInjector
import jp.gr.java_conf.mmktomato.fluffyplayer.di.component.MockComponentInjector
import jp.gr.java_conf.mmktomato.fluffyplayer.proxy.DbxProxy
import jp.gr.java_conf.mmktomato.fluffyplayer.prefs.AppPrefs
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
        presenter.onCreate()

        sharedPrefs = (presenter as SettingsActivityPresenterImpl).sharedPrefs
        dbxProxy = (presenter as SettingsActivityPresenterImpl).dbxProxy
    }

    /**
     * Asserts values of `viewModel`.
     */
    private fun assertViewModel(
            dropboxAuthStatusText: String = DUMMY_DBX_USER_NAME,
            connectDropboxButtonText: String = "disconnect",
            lastFmUserNameText: String = DUMMY_LAST_FM_USER_NAME,
            lastFmPasswordText: String = AppPrefs.LAST_FM_PASSWORD_MARKER) {

        assertEquals(dropboxAuthStatusText, viewModel.dropboxAuthStatusText.get())
        assertEquals(connectDropboxButtonText, viewModel.connectDropboxButtonText.get())
        assertEquals(lastFmUserNameText, viewModel.lastFmUserNameText.get())
        assertEquals(lastFmPasswordText, viewModel.lastFmPasswordText.get())
    }

    /**
     * Asserts values of 'viewModel` when connected to the Dropbox.
     */
    private fun assertViewModelForDbxConnected() {
        assertViewModel(
                dropboxAuthStatusText = DUMMY_DBX_USER_NAME,
                connectDropboxButtonText = "disconnect")
    }

    /**
     * Asserts values of `viewModel` when not connected to the Dropbox.
     */
    private fun assertViewModelForDbxNotConnected() {
        assertViewModel(
                dropboxAuthStatusText = "(not connected)",
                connectDropboxButtonText = "connect")
    }

    /**
     * Asserts values of `viewModel` when Last.fm is enabled.
     */
    private fun assertViewModelForLastFmEnabled() {
        assertViewModel(
                lastFmUserNameText = DUMMY_LAST_FM_USER_NAME,
                lastFmPasswordText = AppPrefs.LAST_FM_PASSWORD_MARKER)
    }

    /**
     * Asserts values of `viewModel` when Last.fm is not enabled.
     */
    private fun assertViewModelForLastFmNotEnabled() {
        assertViewModel(
                lastFmUserNameText = "",
                lastFmPasswordText = "")
    }

    @Test
    fun onCreate_WhenDropboxConnected() {
        presenter.onCreate()

        assertViewModelForDbxConnected()
    }

    @Test
    fun onCreate_WhenDropboxNotConnected() {
        `when`(dbxProxy.isAuthenticated).thenReturn(false)

        presenter.onCreate()

        assertViewModelForDbxNotConnected()
    }

    @Test
    fun refreshUi_WhenDropboxConnected() {
        runBlocking {
            presenter.refreshUi().join()

            assertViewModelForDbxConnected()
        }
    }

    @Test
    fun refreshUi_WhenDropboxNotConnected() {
        `when`(dbxProxy.isAuthenticated).thenReturn(false)

        runBlocking {
            presenter.refreshUi().join()

            assertViewModelForDbxNotConnected()
        }
    }

    @Test
    fun refreshUi_WhenLastFmEnabled() {
        runBlocking {
            presenter.refreshUi().join()

            assertViewModelForLastFmEnabled()
        }
    }

    @Test
    fun refreshUi_WhenLastFmNotEnabled() {
        `when`(sharedPrefs.lastFmUserName).thenReturn("")
        `when`(sharedPrefs.lastFmPasswordDigest).thenReturn("")

        runBlocking {
            presenter.refreshUi().join()

            assertViewModelForLastFmNotEnabled()
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

    @Test
    fun onDestroy() {
        presenter.onDestroy()

        verify(sharedPrefs, times(1)).lastFmUserName = anyString()

        // assertions about Last.fm password is below.
    }

    @Test
    fun onDestroy_WhenLastFmPasswordIsEmpty() {
        viewModel.lastFmPasswordText.set("")

        presenter.onDestroy()

        verify(sharedPrefs, times(1)).lastFmPasswordDigest = ""
    }

    @Test
    fun onDestroy_WhenLastFmPasswordIsNotChanged() {
        presenter.onDestroy()

        verify(sharedPrefs, times(0)).lastFmPasswordDigest = anyString()
    }

    @Test
    fun onDestroy_WhenLastFmPasswordChanged() {
        viewModel.lastFmPasswordText.set("test")

        presenter.onDestroy()

        verify(sharedPrefs, times(1)).lastFmPasswordDigest = anyString()
    }
}