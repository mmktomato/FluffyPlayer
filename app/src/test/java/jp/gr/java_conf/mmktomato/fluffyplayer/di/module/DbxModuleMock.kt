package jp.gr.java_conf.mmktomato.fluffyplayer.di.module

import dagger.Module
import dagger.Provides
import jp.gr.java_conf.mmktomato.fluffyplayer.DUMMY_DBX_USER_NAME
import jp.gr.java_conf.mmktomato.fluffyplayer.dropbox.DbxProxy
import jp.gr.java_conf.mmktomato.fluffyplayer.prefs.SharedPrefsHelper
import kotlinx.coroutines.experimental.async
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.*

@Module
class DbxModuleMock(private val isAuthenticated: Boolean) {
    @Provides
    fun provideDbxProxy(sharedPrefs: SharedPrefsHelper): DbxProxy {
        val dbxProxy = mock(DbxProxy::class.java)

        `when`(dbxProxy.isAuthenticated).thenReturn(isAuthenticated)
        `when`(dbxProxy.getDisplayName()).thenReturn(async { DUMMY_DBX_USER_NAME })
        `when`(dbxProxy.listFolder(anyString(), any())).thenThrow(NotImplementedError())
        `when`(dbxProxy.getTemporaryLink(anyString())).thenThrow(NotImplementedError())

        return dbxProxy
    }
}