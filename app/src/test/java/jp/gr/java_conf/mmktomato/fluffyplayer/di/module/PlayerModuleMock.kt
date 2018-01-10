package jp.gr.java_conf.mmktomato.fluffyplayer.di.module

import android.media.MediaPlayer
import dagger.Module
import dagger.Provides
import org.mockito.Mockito.*

@Module
class PlayerModuleMock(private val isPlaying: Boolean) {
    @Provides
    fun provideMusicPlayer(): MediaPlayer {
        val player = mock(MediaPlayer::class.java)

        `when`(player.isPlaying).thenReturn(isPlaying)

        return player
    }
}