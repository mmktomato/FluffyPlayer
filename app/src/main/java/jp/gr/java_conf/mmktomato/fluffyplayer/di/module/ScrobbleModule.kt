package jp.gr.java_conf.mmktomato.fluffyplayer.di.module

import android.content.Context
import dagger.Module
import dagger.Provides
import de.umass.lastfm.Authenticator
import de.umass.lastfm.Caller
import de.umass.lastfm.Session
import de.umass.lastfm.cache.FileSystemCache
import jp.gr.java_conf.mmktomato.fluffyplayer.BuildConfig
import jp.gr.java_conf.mmktomato.fluffyplayer.prefs.SharedPrefsHelper
import java.io.File
import javax.inject.Singleton

@Module
class ScrobbleModule {
    @Singleton
    @Provides
    fun providesSession(ctx: Context, sharedPrefs: SharedPrefsHelper): Session {
        Caller.getInstance().cache = FileSystemCache(File(ctx.cacheDir, "lastFmCache"))

        return Authenticator.getMobileSession(
                sharedPrefs.lastFmUserName,
                sharedPrefs.lastFmPasswordDigest,
                BuildConfig.FLUFFY_PLAYER_LAST_FM_APP_KEY,
                BuildConfig.FLUFFY_PLAYER_LAST_FM_SECRET)
    }
}