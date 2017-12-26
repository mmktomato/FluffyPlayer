package jp.gr.java_conf.mmktomato.fluffyplayer.di.module

import android.content.Context
import dagger.Module
import dagger.Provides
import jp.gr.java_conf.mmktomato.fluffyplayer.DUMMY_DBX_ACCESS_TOKEN
import jp.gr.java_conf.mmktomato.fluffyplayer.prefs.SharedPrefsHelper
import org.mockito.Mockito.*

@Module
class SharedPrefsModuleMock {
    @Provides
    fun provideSharedPrefsHelper(ctx: Context): SharedPrefsHelper {
        val sharedPrefs = mock(SharedPrefsHelper::class.java)

        `when`(sharedPrefs.context).thenReturn(ctx)
        `when`(sharedPrefs.dbxAccessToken).thenReturn(DUMMY_DBX_ACCESS_TOKEN)

        return sharedPrefs
    }
}