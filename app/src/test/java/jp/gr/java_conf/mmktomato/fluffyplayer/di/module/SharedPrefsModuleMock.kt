package jp.gr.java_conf.mmktomato.fluffyplayer.di.module

import dagger.Module
import dagger.Provides
import jp.gr.java_conf.mmktomato.fluffyplayer.DUMMY_DBX_ACCESS_TOKEN
import jp.gr.java_conf.mmktomato.fluffyplayer.prefs.SharedPrefsHelper
import org.mockito.Mockito.*

@Module
class SharedPrefsModuleMock {
    @Provides
    fun provideSharedPrefsHelper(): SharedPrefsHelper {
        val sharedPrefs = mock(SharedPrefsHelper::class.java)

        `when`(sharedPrefs.dbxAccessToken).thenReturn(DUMMY_DBX_ACCESS_TOKEN)

        return sharedPrefs
    }
}