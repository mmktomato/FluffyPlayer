package jp.gr.java_conf.mmktomato.fluffyplayer.dropbox

import android.content.Context
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.android.Auth
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.ListFolderResult
import jp.gr.java_conf.mmktomato.fluffyplayer.BuildConfig
import jp.gr.java_conf.mmktomato.fluffyplayer.prefs.AppPrefs
import jp.gr.java_conf.mmktomato.fluffyplayer.prefs.SharedPrefsHelper
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async

/**
 * A proxy of Dropbox API.
 */
interface DbxProxy {
    /**
     * Checks that access token is exists and start authentication if needed.
     */
    fun auth()

    /**
     * Saves the access token from `Auth.getOAuth2AccessToken`.
     */
    fun saveAccessToken()

    /**
     * Returns whether the authentication is finished.
     */
    val isAuthenticated: Boolean

    /**
     * Returns the user display name.
     */
    fun getDisplayName(): Deferred<String>

    /**
     * Returns files and folder information.
     *
     * @param path the folder path in DropBox.
     * @param prevRes the previous result of this method.
     * @return the instance of ListFolderResult class.
     */
    fun listFolder(path: String, prevRes: ListFolderResult?): Deferred<ListFolderResult>

    /**
     * Returns the temporary link of file.
     *
     * @param path the file path.
     * @return the temporary link.
     */
    fun getTemporaryLink(path: String): Deferred<String>
}

/**
 * An implementation of DbxProxy.
 *
 * @param ctx an android context.
 * @param sharedPrefs a SharedPrefsHelper.
 */
class DbxProxyImpl constructor(private val ctx: Context, private val sharedPrefs: SharedPrefsHelper) : DbxProxy {
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
    override fun auth() {
        if (sharedPrefs.dbxAccessToken.isNullOrEmpty()) {
            Auth.startOAuth2Authentication(ctx, BuildConfig.FLUFFY_PLAYER_DBX_APP_KEY)
        }
    }

    /**
     * Saves the access token from `Auth.getOAuth2AccessToken`.
     */
    override fun saveAccessToken() {
        val accessToken = Auth.getOAuth2Token()
        if (!accessToken.isNullOrEmpty()) {
            sharedPrefs.dbxAccessToken = accessToken
        }
    }

    /**
     * Returns whether the authentication is finished.
     */
    override val isAuthenticated: Boolean
            get() = !sharedPrefs.dbxAccessToken.isNullOrEmpty()

    /**
     * Returns the user display name.
     *
     * @return the user display name.
     */
    override fun getDisplayName() = async(CommonPool) {
        return@async client.users().currentAccount.name.displayName
    }

    /**
     * Returns files and folder information.
     *
     * @param path the folder path in DropBox.
     * @param prevRes the previous result of this method.
     * @return the instance of ListFolderResult class.
     */
    override fun listFolder(path: String, prevRes: ListFolderResult?) = async(CommonPool) {
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
    override fun getTemporaryLink(path: String) = async(CommonPool) {
        return@async client.files().getTemporaryLink(path).link
    }
}