package jp.gr.java_conf.mmktomato.fluffyplayer.di.module

import dagger.Module
import dagger.Provides
import jp.gr.java_conf.mmktomato.fluffyplayer.DUMMY_DBX_FILE_PATH
import jp.gr.java_conf.mmktomato.fluffyplayer.db.model.PlaylistItem
import java.util.*

@Module
class PlaylistModuleMock(private val playlistItemStatus: PlaylistItem.Status) {
    @Provides
    fun providePlaylistItem(): PlaylistItem {
        val id = UUID.randomUUID().toString()
        return PlaylistItem(id, 1, DUMMY_DBX_FILE_PATH, playlistItemStatus)
    }
}