package jp.gr.java_conf.mmktomato.fluffyplayer.ui.presenter

import android.widget.Button
import jp.gr.java_conf.mmktomato.fluffyplayer.dropbox.DbxProxy
import jp.gr.java_conf.mmktomato.fluffyplayer.prefs.SharedPrefsHelper
import jp.gr.java_conf.mmktomato.fluffyplayer.ui.viewmodel.SettingsActivityViewModel
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch

/**
 * A presenter of SettingsActivity.
 */
internal interface SettingsActivityPresenter {

    fun onCreate()

    fun onResume()

    /**
     * Refreshes UI Component.
     */
    fun refreshUi()
}

/**
 * An implementation of SettingActivityPresenter.
 *
 * @param sharedPrefs the SharedPreferences.
 * @param dbxProxy the Dropbox API proxy.
 * @param viewModel the view model of this activity.
 * @param connectDropboxButton the instance of connectDropboxButton.
 */
internal class SettingsActivityPresenterImpl(
        private val sharedPrefs: SharedPrefsHelper,
        private val dbxProxy: DbxProxy,
        private val viewModel: SettingsActivityViewModel,
        private val connectDropboxButton: Button) : SettingsActivityPresenter {

    /**
     * indicates whether the OAuth2 is processing.
     */
    private var isOAuth2Processing = false

    /**
     * add listeners to UI Components.
     */
    private fun setUiComponentListeners() {
        connectDropboxButton.setOnClickListener { v ->
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
    override fun refreshUi() {
        viewModel.connectDropboxButtonText.set(
                if (dbxProxy.isAuthenticated) "disconnect" else "connect")

        launch(UI) {
            viewModel.dropboxAuthStatusText.set(
                    if (dbxProxy.isAuthenticated) dbxProxy.getDisplayName().await() else "(not connected)")
        }
    }
}