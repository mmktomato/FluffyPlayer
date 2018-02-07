package jp.gr.java_conf.mmktomato.fluffyplayer.ui.presenter

import android.widget.Button
import jp.gr.java_conf.mmktomato.fluffyplayer.dropbox.DbxProxy
import jp.gr.java_conf.mmktomato.fluffyplayer.prefs.AppPrefs
import jp.gr.java_conf.mmktomato.fluffyplayer.prefs.SharedPrefsHelper
import jp.gr.java_conf.mmktomato.fluffyplayer.ui.viewmodel.SettingsActivityViewModel
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import java.security.MessageDigest
import javax.inject.Inject

/**
 * A presenter of SettingsActivity.
 */
interface SettingsActivityPresenter {

    fun onCreate()

    fun onResume()

    fun onDestroy()

    /**
     * Refreshes UI Component.
     */
    fun refreshUi(): Job

    /**
     * Called when the connectDropboxButton is clicked.
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

    override fun onDestroy() {
        // Last.fm user name
        sharedPrefs.lastFmUserName = viewModel.lastFmUserNameText.get()

        // Last.fm password
        val plainPassword = viewModel.lastFmPasswordText.get()
        when (plainPassword) {
            null, "" -> sharedPrefs.lastFmPasswordDigest = ""
            AppPrefs.LAST_FM_PASSWORD_MARKER -> { /* do nothing. */ }
            else -> {
                val md5 = MessageDigest.getInstance("MD5").digest(plainPassword.toByteArray(Charsets.UTF_8))
                sharedPrefs.lastFmPasswordDigest = com.dropbox.core.util.StringUtil.binaryToHex(md5)
            }
        }
    }

    /**
     * Refreshes UI Component.
     */
    override fun refreshUi(): Job {
        return launch(UI) {
            viewModel.connectDropboxButtonText.set(
                    if (dbxProxy.isAuthenticated) "disconnect" else "connect")

            // This needs a coroutine context.
            viewModel.dropboxAuthStatusText.set(
                    if (dbxProxy.isAuthenticated) dbxProxy.getDisplayName().await() else "(not connected)")

            viewModel.lastFmUserNameText.set(sharedPrefs.lastFmUserName)

            viewModel.lastFmPasswordText.set(
                    if (sharedPrefs.lastFmPasswordDigest.isNullOrEmpty()) "" else AppPrefs.LAST_FM_PASSWORD_MARKER)
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