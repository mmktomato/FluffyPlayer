package jp.gr.java_conf.mmktomato.fluffyplayer.di.module

import dagger.Module
import dagger.Provides
import jp.gr.java_conf.mmktomato.fluffyplayer.DUMMY_DBX_FILE_NAME
import jp.gr.java_conf.mmktomato.fluffyplayer.DUMMY_DBX_FILE_PATH
import jp.gr.java_conf.mmktomato.fluffyplayer.DUMMY_DBX_USER_NAME
import jp.gr.java_conf.mmktomato.fluffyplayer.DUMMY_MUSIC_URI
import jp.gr.java_conf.mmktomato.fluffyplayer.dropbox.DbxNodeMetadata
import jp.gr.java_conf.mmktomato.fluffyplayer.dropbox.DbxProxy
import jp.gr.java_conf.mmktomato.fluffyplayer.prefs.SharedPrefsHelper
import kotlinx.coroutines.experimental.async
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.*
import javax.inject.Named

@Module
class DbxModuleMock(private val isAuthenticated: Boolean) {
    @Provides
    fun provideDbxProxy(sharedPrefs: SharedPrefsHelper): DbxProxy {
        val dbxProxy = mock(DbxProxy::class.java)

        `when`(dbxProxy.isAuthenticated).thenReturn(isAuthenticated)
        `when`(dbxProxy.getDisplayName()).thenReturn(async { DUMMY_DBX_USER_NAME })
        `when`(dbxProxy.listFolder(anyString(), any())).thenThrow(NotImplementedError())
        `when`(dbxProxy.getTemporaryLink(anyString())).thenReturn(async { DUMMY_MUSIC_URI })

        return dbxProxy
    }

    @Provides
    @Named("File")
    fun provideDbxFileMetadata(): DbxNodeMetadata = createDbxNodeMetadataMock(true)

    @Provides
    @Named("FileArray")
    fun provideDbxFileMetadataArray(): Array<DbxNodeMetadata> {
        return arrayOf(
                createDbxNodeMetadataMock(true),
                createDbxNodeMetadataMock(true))
    }

    @Provides
    @Named("Directory")
    fun provideDbxDirectoryMetadata(): DbxNodeMetadata = createDbxNodeMetadataMock(false)

    /**
     * Creates a mock of DbxNodeMetadata.
     */
    private fun createDbxNodeMetadataMock(isFile: Boolean): DbxNodeMetadata {
        val metadata = mock(DbxNodeMetadata::class.java)

        `when`(metadata.isFile).thenReturn(isFile)
        `when`(metadata.name).thenReturn(DUMMY_DBX_FILE_NAME)
        `when`(metadata.path).thenReturn(DUMMY_DBX_FILE_PATH)

        return metadata
    }
}
