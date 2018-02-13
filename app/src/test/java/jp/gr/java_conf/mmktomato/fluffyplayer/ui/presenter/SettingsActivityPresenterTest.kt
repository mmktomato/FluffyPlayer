package jp.gr.java_conf.mmktomato.fluffyplayer.ui.presenter

import android.widget.Button
import jp.gr.java_conf.mmktomato.fluffyplayer.DUMMY_DBX_USER_NAME
import jp.gr.java_conf.mmktomato.fluffyplayer.DUMMY_LAST_FM_PASSWORD_DIGEST
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
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.RuntimeEnvironment

/**
 * Tests for SettingsActivityPresenter.
 *
 * @param dbxConnected whether is Dropbox connected.
 * @param lastFmEnabled whether is Last.fm enabled.
 */
@RunWith(ParameterizedRobolectricTestRunner::class)
class SettingsActivityPresenterTest(
        private val dbxConnected: Boolean,
        private val lastFmEnabled: Boolean) {

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

        @ParameterizedRobolectricTestRunner.Parameters(name = "dbxConnected = {0}, lastFmEnabled = {1}")
        @JvmStatic
        fun testParams(): List<Array<out Boolean>> {
            return listOf(
                    arrayOf(true, true),
                    arrayOf(true, false),
                    arrayOf(false, true),
                    arrayOf(false, false))
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

        `when`(dbxProxy.isAuthenticated).thenReturn(dbxConnected)
        `when`(sharedPrefs.lastFmUserName).thenReturn(if (lastFmEnabled) { DUMMY_LAST_FM_USER_NAME } else { "" })
        `when`(sharedPrefs.lastFmPasswordDigest).thenReturn(if (lastFmEnabled) { DUMMY_LAST_FM_PASSWORD_DIGEST } else { "" })

        presenter.onCreate()
    }

    /**
     * Asserts values of 'viewModel` when connected to the Dropbox.
     */
    private fun assertViewModelForDbxConnected() {
        assertEquals(DUMMY_DBX_USER_NAME, viewModel.dropboxAuthStatusText.get())
        assertEquals("disconnect", viewModel.connectDropboxButtonText.get())
    }

    /**
     * Asserts values of `viewModel` when not connected to the Dropbox.
     */
    private fun assertViewModelForDbxNotConnected() {
        assertEquals("(not connected)", viewModel.dropboxAuthStatusText.get())
        assertEquals("connect", viewModel.connectDropboxButtonText.get())
    }

    /**
     * Asserts values of `viewModel` when Last.fm is enabled.
     */
    private fun assertViewModelForLastFmEnabled() {
        assertEquals(DUMMY_LAST_FM_USER_NAME, viewModel.lastFmUserNameText.get())
        assertEquals(AppPrefs.LAST_FM_PASSWORD_MARKER, viewModel.lastFmPasswordText.get())
    }

    /**
     * Asserts values of `viewModel` when Last.fm is not enabled.
     */
    private fun assertViewModelForLastFmNotEnabled() {
        assertEquals("", viewModel.lastFmUserNameText.get())
        assertEquals("", viewModel.lastFmPasswordText.get())
    }

    @Test
    fun onCreate() {
        presenter.onCreate()

        if (dbxConnected) {
            assertViewModelForDbxConnected()
        }
        else {
            assertViewModelForDbxNotConnected()
        }
    }

    @Test
    fun refreshUi() {
        runBlocking {
            presenter.refreshUi().join()

            if (dbxConnected) {
                assertViewModelForDbxConnected()
            }
            else {
                assertViewModelForDbxNotConnected()
            }

            if (lastFmEnabled) {
                assertViewModelForLastFmEnabled()
            }
            else {
                assertViewModelForLastFmNotEnabled()
            }
        }
    }

    @Test
    fun onConnectDropboxButtonClick() {
        presenter.onConnectDropboxButtonClick()

        if (dbxConnected) {
            verify(sharedPrefs, times(1)).removeDbxAccessToken()
        }
        else {
            verify(dbxProxy, times(1)).auth()

            // resume
            presenter.onResume()

            verify(dbxProxy, times(1)).saveAccessToken()
        }
    }

    @Test
    fun onDestroy() {
        presenter.onDestroy()

        verify(sharedPrefs, times(1)).lastFmUserName = anyString()

        // assertions about Last.fm password is below.
    }

    @Test
    fun onDestroy_WhenLastFmPasswordIsChangedToEmpty() {
        viewModel.lastFmPasswordText.set("")

        presenter.onDestroy()

        verify(sharedPrefs, times(1)).lastFmPasswordDigest = ""
    }

    @Test
    fun onDestroy_WhenLastFmPasswordIsNotChanged() {
        presenter.onDestroy()

        if (lastFmEnabled) {
            verify(sharedPrefs, times(0)).lastFmPasswordDigest = anyString()
        }
        else {
            verify(sharedPrefs, times(1)).lastFmPasswordDigest = ""
        }
    }

    @Test
    fun onDestroy_WhenLastFmPasswordIsChangedToAnyString() {
        viewModel.lastFmPasswordText.set("test")

        presenter.onDestroy()

        verify(sharedPrefs, times(1)).lastFmPasswordDigest = "098f6bcd4621d373cade4e832627b4f6" // $(md5 -s test)
    }
}