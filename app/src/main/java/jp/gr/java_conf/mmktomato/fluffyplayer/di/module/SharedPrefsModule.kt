package jp.gr.java_conf.mmktomato.fluffyplayer.di.module

import android.content.Context
import dagger.Module
import dagger.Provides
import jp.gr.java_conf.mmktomato.fluffyplayer.prefs.SharedPrefsHelper
import jp.gr.java_conf.mmktomato.fluffyplayer.prefs.SharedPrefsHelperImpl

@Module
class SharedPrefsModule {
    @Provides
    fun provideSharedPrefsHelper(ctx: Context): SharedPrefsHelper {
        return SharedPrefsHelperImpl(ctx)
    }
}
