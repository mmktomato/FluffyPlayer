package jp.gr.java_conf.mmktomato.fluffyplayer.ui.presenter

import android.widget.Button
import jp.gr.java_conf.mmktomato.fluffyplayer.dropbox.DbxProxy
import jp.gr.java_conf.mmktomato.fluffyplayer.prefs.SharedPrefsHelper
import jp.gr.java_conf.mmktomato.fluffyplayer.ui.viewmodel.SettingsActivityViewModel
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import javax.inject.Inject

/**
 * A presenter of SettingsActivity.
 */
interface SettingsActivityPresenter {

    fun onCreate()

    fun onResume()

    /**
     * Refreshes UI Component.
     */
    fun refreshUi(): Job

    /**
     * Called when the ConnectDropboxButton is clicked.
     */
    fun onConnectDropboxButtonClick()
}

/**
 * An implementation of SettingActivityPresenter.
 *
 * @param viewModel the view model of this activity.
 * @param connectDropboxButton the instance of connectDropboxButton.
 */
class SettingsActivityPresenterImpl(
        private val viewModel: SettingsActivityViewModel,
        private val connectDropboxButton: Button) : SettingsActivityPresenter {

    /**
     * the SharedPreferences.
     */
    @Inject
    lateinit var sharedPrefs: SharedPrefsHelper

    /**
     * the DbxProxy.
     */
    @Inject
    lateinit var dbxProxy: DbxProxy

    /**
     * indicates whether the OAuth2 is processing.
     */
    private var isOAuth2Processing = false

    /**
     * add listeners to UI Components.
     */
    private fun setUiComponentListeners() {
        connectDropboxButton.setOnClickListener({ v -> onConnectDropboxButtonClick() })
    }

    override fun onCreate() {
        refreshUi()
        setUiComponentListeners()
    }

    override fun onResume() {
        if (isOAuth2Processing) {
            dbxProxy.saveAccessToken()
        }
        isOAuth2Processing = false

        refreshUi()
    }

    /**
     * Refreshes UI Component.
     */
    override fun refreshUi(): Job {
        viewModel.connectDropboxButtonText.set(
                if (dbxProxy.isAuthenticated) "disconnect" else "connect")

        return launch(UI) {
            viewModel.dropboxAuthStatusText.set(
                    if (dbxProxy.isAuthenticated) dbxProxy.getDisplayName().await() else "(not connected)")
        }
    }

    /**
     * Called when the ConnectDropboxButton is clicked.
     */
    override fun onConnectDropboxButtonClick() {
        if (dbxProxy.isAuthenticated) {
            sharedPrefs.removeDbxAccessToken()
            refreshUi()
        }
        else {
            isOAuth2Processing = true
            dbxProxy.auth()
        }
    }
}