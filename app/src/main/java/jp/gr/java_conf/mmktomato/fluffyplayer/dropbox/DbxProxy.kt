package jp.gr.java_conf.mmktomato.fluffyplayer.dropbox

import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.android.Auth
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.ListFolderResult
import jp.gr.java_conf.mmktomato.fluffyplayer.BuildConfig
import jp.gr.java_conf.mmktomato.fluffyplayer.prefs.AppPrefs
import jp.gr.java_conf.mmktomato.fluffyplayer.prefs.SharedPrefsHelper
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async

/**
 * A proxy of Dropbox API.
 *
 * @param sharedPrefs a SharedPrefsHelper.
 */
internal class DbxProxy(private val sharedPrefs: SharedPrefsHelper) {
    /**
     * An Dropbox API client.
     */
    private var client: DbxClientV2

    init {
        val config = DbxRequestConfig(AppPrefs.appName)
        client = DbxClientV2(config, sharedPrefs.dbxAccessToken)
    }

    /**
     * Checks that access token is exists and start authentication if needed.
     */
    internal fun auth() {
        if (sharedPrefs.dbxAccessToken.isNullOrEmpty()) {
            Auth.startOAuth2Authentication(sharedPrefs.context, BuildConfig.FLUFFY_PLAYER_DBX_APP_KEY)
        }
    }

    /**
     * Returns whether the authentication is finished.
     */
    internal fun isAuthenticated(): Boolean = !getAccessToken().isNullOrEmpty()

    /**
     * Returns the access token of Dropbox API.
     *
     * If the access token is exists in SharedPreferences, returns it.
     * If not, checks Auth.getOAuth2Token (result of OAuth2 authentication).
     *
     * @return the access token of Dropbox API.
     */
    private fun getAccessToken(): String {
        var ret = sharedPrefs.dbxAccessToken
        if (ret.isNullOrEmpty()) {
            val accessToken = Auth.getOAuth2Token()
            if (accessToken.isNullOrEmpty()) {
                ret = ""
            }
            else {
                ret = accessToken
                sharedPrefs.dbxAccessToken = ret
            }
        }
        return ret
    }

    /**
     * Returns the user display name.
     *
     * @return the user display name.
     */
    fun getDisplayName() = async(CommonPool) {
        return@async client.users().currentAccount.name.displayName
    }

    /**
     * Returns files and folder information.
     *
     * @param path the folder path in DropBox.
     * @param prevRes the previous result of this method.
     * @return the instance of ListFolderResult class.
     */
    fun listFolder(path: String, prevRes: ListFolderResult?) = async(CommonPool) {
        if (prevRes == null) {
            return@async client.files().listFolderBuilder(path)
                    .withLimit(50)
                    .start()
        }
        else {
            return@async client.files().listFolderContinue(prevRes.cursor)
        }
    }

    /**
     * Returns the temporary link of file.
     *
     * @param path the file path.
     * @return the temporary link.
     */
    fun getTemporaryLink(path: String) = async(CommonPool) {
        return@async client.files().getTemporaryLink(path).link
    }
}