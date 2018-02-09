package jp.gr.java_conf.mmktomato.fluffyplayer.prefs

import android.content.Context
import android.content.SharedPreferences

/**
 * An helper of SharedPreferences.
 */
interface SharedPrefsHelper {
    /**
     * a Dropbox access token.
     */
    var dbxAccessToken: String

    /**
     * Removes Dropbox access token.
     */
    fun removeDbxAccessToken()

    /**
     * a Last.fm user name.
     */
    var lastFmUserName: String

    /**
     * a Last.fm password digest.
     */
    var lastFmPasswordDigest: String
}

/**
 * An implementation of SharedPrefsHelper.
 *
 * @param context An android context.
 */
class SharedPrefsHelperImpl(private val context: Context) : SharedPrefsHelper {
    companion object {
        /**
         * The preference name.
         */
        private const val PREF_NAME = "mmktomato-fluffy-player"

        /**
         * The preference mode.
         */
        private const val PREF_MODE = Context.MODE_PRIVATE

        /**
         * The key for Dropbox access token.
         */
        private const val KEY_DBX_ACCESS_TOKEN = "dbx-access-token"

        /**
         * The key for Last.fm user name.
         */
        private const val KEY_LAST_FM_USER_NAME = "last-fm-user-name"

        /**
         * The key for Last.fm password digest.
         */
        private const val KEY_LAST_FM_PASSWORD_DIGEST = "last-fm-password-digest"
    }

    /**
     * Returns a SharedPreferences.
     */
    private val sharedPrefs: SharedPreferences
        get() = context.getSharedPreferences(PREF_NAME, PREF_MODE)

    /**
     * Returns or sets Dropbox access token.
     */
    override var dbxAccessToken: String
        get() = sharedPrefs.getString(KEY_DBX_ACCESS_TOKEN, "")
        set(value) = sharedPrefs.edit().putString(KEY_DBX_ACCESS_TOKEN, value).apply()

    /**
     * Removes Dropbox access token.
     */
    override fun removeDbxAccessToken() {
        sharedPrefs.edit().remove(KEY_DBX_ACCESS_TOKEN).apply()
    }

    /**
     * a Last.fm user name.
     */
    override var lastFmUserName: String
        get() = sharedPrefs.getString(KEY_LAST_FM_USER_NAME, "")
        set(value) = sharedPrefs.edit().putString(KEY_LAST_FM_USER_NAME, value).apply()

    /**
     * a Last.fm password digest.
     */
    override var lastFmPasswordDigest: String
        get() = sharedPrefs.getString(KEY_LAST_FM_PASSWORD_DIGEST, "")
        set(value) = sharedPrefs.edit().putString(KEY_LAST_FM_PASSWORD_DIGEST, value).apply()
}