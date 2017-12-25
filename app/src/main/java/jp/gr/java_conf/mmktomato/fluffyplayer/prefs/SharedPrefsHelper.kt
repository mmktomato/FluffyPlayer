package jp.gr.java_conf.mmktomato.fluffyplayer.prefs

import android.content.Context
import android.content.SharedPreferences

/**
 * An helper of SharedPreferences.
 */
interface SharedPrefsHelper {
    /**
     * an android context.
     */
    val context: Context

    /**
     * a Dropbox access token.
     */
    var dbxAccessToken: String

    /**
     * Removes Dropbox access token.
     */
    fun removeDbxAccessToken()
}

/**
 * An implementation of SharedPrefsHelper.
 *
 * @param context An android context.
 */
class SharedPrefsHelperImpl(override val context: Context) : SharedPrefsHelper {
    companion object {
        /**
         * The preference name.
         */
        private val PREF_NAME = "mmktomato-fluffy-player"

        /**
         * The preference mode.
         */
        private val PREF_MODE = Context.MODE_PRIVATE

        /**
         * The key for Dropbox access token.
         */
        private val KEY_DBX_ACCESS_TOKEN = "dbx-access-token"
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
}