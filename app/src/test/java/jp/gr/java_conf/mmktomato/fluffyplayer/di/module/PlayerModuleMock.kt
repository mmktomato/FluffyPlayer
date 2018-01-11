package jp.gr.java_conf.mmktomato.fluffyplayer.di.module

import android.content.Context
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import dagger.Module
import dagger.Provides
import jp.gr.java_conf.mmktomato.fluffyplayer.DUMMY_MUSIC_TITLE
import org.mockito.Mockito.*

@Module
class PlayerModuleMock(private val isPlaying: Boolean) {
    @Provides
    fun provideMusicPlayer(): MediaPlayer {
        val player = mock(MediaPlayer::class.java)

        `when`(player.isPlaying).thenReturn(isPlaying)

        return player
    }

    @Provides
    fun provideMediaMetadataRetriever(ctx: Context): MediaMetadataRetriever {
        val mmr = mock(MediaMetadataRetriever::class.java)

        `when`(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)).thenReturn(DUMMY_MUSIC_TITLE)
        //`when`(mmr.embeddedPicture).thenReturn(

        return mmr
    }
}