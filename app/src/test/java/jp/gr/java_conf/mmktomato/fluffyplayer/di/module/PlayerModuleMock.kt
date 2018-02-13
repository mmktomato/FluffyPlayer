package jp.gr.java_conf.mmktomato.fluffyplayer.di.module

import android.media.MediaMetadataRetriever
import dagger.Module
import dagger.Provides
import jp.gr.java_conf.mmktomato.fluffyplayer.*
import org.mockito.Mockito.*

@Module
class PlayerModuleMock {
    @Provides
    fun provideMediaMetadataRetriever(): MediaMetadataRetriever {
        val mmr = mock(MediaMetadataRetriever::class.java)

        `when`(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)).thenReturn(DUMMY_MUSIC_TITLE)
        `when`(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)).thenReturn(DUMMY_MUSIC_ARTIST)
        `when`(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)).thenReturn(DUMMY_MUSIC_DURATION.toString())
        `when`(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER)).thenReturn(DUMMY_MUSIC_TRACK_NUMBER.toString())
        `when`(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)).thenReturn(DUMMY_ALBUM_TITLE)
        `when`(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST)).thenReturn(DUMMY_ALBUM_ARTIST)

        //`when`(mmr.embeddedPicture).thenReturn(

        return mmr
    }
}