package com.example.mmktomato.fluffyplayer.dropbox

import android.content.Context
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.android.Auth
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.ListFolderResult
import com.example.mmktomato.fluffyplayer.BuildConfig
import com.example.mmktomato.fluffyplayer.prefs.AppPrefs
import com.example.mmktomato.fluffyplayer.prefs.SharedPrefsHelper
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async

/**
 * A proxy of Dropbox API.
 *
 * @param accessToken An access token of Dropbox API.
 */
internal class DbxProxy(private val accessToken: String) {
    companion object {
        /**
         * Checks that access token is exists and start authentication if needed.
         */
        internal fun auth(ctx: Context) {
            if (SharedPrefsHelper.dbxAccessToken(ctx).isNullOrEmpty()) {
                Auth.startOAuth2Authentication(ctx, BuildConfig.FLUFFY_PLAYER_DBX_APP_KEY)
            }
        }

        /**
         * Returns whether the authentication is finished.
         */
        internal fun isAuthenticated(ctx: Context): Boolean = !getAccessToken(ctx).isNullOrEmpty()

        /**
         * Returns a DbxProxy instance.
         */
        internal fun create(ctx: Context): DbxProxy = DbxProxy(getAccessToken(ctx))

        /**
         * Returns the access token of Dropbox API.
         *
         * If the access token is exists in SharedPreferences, returns it.
         * If not, checks Auth.getOAuth2Token (result of OAuth2 authentication).
         *
         * @return the access token of Dropbox API.
         */
        private fun getAccessToken(ctx: Context): String {
            var ret = SharedPrefsHelper.dbxAccessToken(ctx)
            if (ret.isNullOrEmpty()) {
                val accessToken = Auth.getOAuth2Token()
                if (accessToken.isNullOrEmpty()) {
                    ret = ""
                }
                else {
                    ret = accessToken
                    SharedPrefsHelper.dbxAccessToken(ctx, ret)
                }
            }
            return ret
        }
    }

    /**
     * An Dropbox API client.
     */
    private var client: DbxClientV2

    init {
        val config = DbxRequestConfig(AppPrefs.appName)
        client = DbxClientV2(config, accessToken)
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
}