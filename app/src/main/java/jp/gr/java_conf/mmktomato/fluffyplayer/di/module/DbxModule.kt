package jp.gr.java_conf.mmktomato.fluffyplayer.di.module

import android.content.Context
import dagger.Module
import dagger.Provides
import jp.gr.java_conf.mmktomato.fluffyplayer.dropbox.DbxProxy
import jp.gr.java_conf.mmktomato.fluffyplayer.dropbox.DbxProxyImpl
import jp.gr.java_conf.mmktomato.fluffyplayer.prefs.SharedPrefsHelper

@Module
class DbxModule {
    @Provides
    fun provideDbxProxy(ctx: Context, sharedPrefs: SharedPrefsHelper): DbxProxy {
        return DbxProxyImpl(ctx, sharedPrefs)
    }
}
