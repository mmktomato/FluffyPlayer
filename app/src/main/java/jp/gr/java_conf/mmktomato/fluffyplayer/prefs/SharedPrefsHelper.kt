package jp.gr.java_conf.mmktomato.fluffyplayer.prefs

import android.content.Context
import android.content.SharedPreferences

/**
 * An helper of SharedPreferences.
 */
internal object SharedPrefsHelper {
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

    /**
     * Returns a SharedPreferences.
     *
     * @param ctx An android context.
     */
    private fun getSharedPrefs(ctx: Context): SharedPreferences {
        return ctx.getSharedPreferences(PREF_NAME, PREF_MODE)
    }

    /**
     * Returns or sets a string value.
     *
     * @param ctx An android context.
     * @param key The key of SharedPreferences.
     * @param defaultValue The default value of SharedPreferences.
     * @param value A value to set to SharedPreferences.
     */
    private fun getOrPutString(ctx: Context, key: String, defaultValue: String, value: String?): String {
        val prefs = getSharedPrefs(ctx)

        if (value != null) {
            prefs.edit().putString(key, value).apply()
        }

        return getSharedPrefs(ctx).getString(key, defaultValue)
    }

    /**
     * Returns or sets Dropbox access token.
     *
     * @param ctx An android context.
     * @param value A value to set to SharedPreferences.
     */
    internal fun dbxAccessToken(ctx: Context, value: String? = null): String {
        return getOrPutString(ctx, KEY_DBX_ACCESS_TOKEN, "", value)
    }

    /**
     * Removes Dropbox access token.
     *
     * @param ctx An android context.
     */
    internal fun removeDbxAccessToken(ctx: Context) {
        getSharedPrefs(ctx).edit().remove(KEY_DBX_ACCESS_TOKEN).apply()
    }
}