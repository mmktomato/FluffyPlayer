package jp.gr.java_conf.mmktomato.fluffyplayer.di.module

import android.content.Context
import dagger.Module
import dagger.Provides
import de.umass.lastfm.Authenticator
import de.umass.lastfm.Caller
import de.umass.lastfm.cache.FileSystemCache
import jp.gr.java_conf.mmktomato.fluffyplayer.BuildConfig
import jp.gr.java_conf.mmktomato.fluffyplayer.prefs.SharedPrefsHelper
import jp.gr.java_conf.mmktomato.fluffyplayer.proxy.LastFmProxy
import jp.gr.java_conf.mmktomato.fluffyplayer.usecase.InvalidScrobbleUseCaseImpl
import jp.gr.java_conf.mmktomato.fluffyplayer.usecase.ScrobbleUseCase
import jp.gr.java_conf.mmktomato.fluffyplayer.usecase.ScrobbleUseCaseImpl
import java.io.File
import javax.inject.Singleton

@Module
class ScrobbleModule {
    @Singleton
    @Provides
    fun provideScrobbleUseCase(ctx: Context, sharedPrefs: SharedPrefsHelper): ScrobbleUseCase {
        return if (sharedPrefs.lastFmUserName.isNullOrEmpty() || sharedPrefs.lastFmPasswordDigest.isNullOrEmpty()) {
            InvalidScrobbleUseCaseImpl()
        }
        else {
            val proxy = LastFmProxy()

            proxy.initializeGlobalCache(ctx)

            val session = proxy.getMobileSession(sharedPrefs.lastFmUserName, sharedPrefs.lastFmPasswordDigest)

            ScrobbleUseCaseImpl(session!!, proxy)  // TODO: session is null when credential is not valid.
        }
    }
}