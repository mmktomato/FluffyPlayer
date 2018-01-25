package jp.gr.java_conf.mmktomato.fluffyplayer.player

import android.media.MediaPlayer
import jp.gr.java_conf.mmktomato.fluffyplayer.DUMMY_MUSIC_URI
import jp.gr.java_conf.mmktomato.fluffyplayer.di.component.DaggerPlayerServiceBinderTestComponent
import jp.gr.java_conf.mmktomato.fluffyplayer.di.module.PlayerModuleMock
import kotlinx.coroutines.experimental.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import javax.inject.Inject

/**
 * Tests for PlayerServiceBinder.
 */
//@RunWith(RobolectricTestRunner::class)
class PlayerServiceBinderTest {
    private interface CallbackHolder {
        fun onPlayerStateChagned()
        fun onMusicFinished()
    }

    @Inject
    lateinit var player: MediaPlayer

    private lateinit var binder: PlayerServiceBinder

    private lateinit var listenerIndices: MutableList<Int>

    private lateinit var callbacks: CallbackHolder

    @Before
    fun setUp() {
        DaggerPlayerServiceBinderTestComponent.builder()
                .playerModuleMock(PlayerModuleMock(false))
                .build()
                .inject(this)

        binder = PlayerServiceBinder(player)
        listenerIndices = mutableListOf()
        callbacks = mock(CallbackHolder::class.java)
        binder.addOnPlayerStateChangedListener(callbacks::onPlayerStateChagned)
        binder.addOnMusicFinishedListener(callbacks::onMusicFinished)
    }

    @After
    fun tearDown() {
        listenerIndices.forEach { index ->
            binder.removeOnPlayerStateChangedListener(index)
            binder.removeOnMusicFinishedListener(index)
        }
        player.release()
    }

    @Test
    fun prepare() {
        runBlocking {
            binder.prepare(DUMMY_MUSIC_URI)
        }

        verify(player, times(1)).setDataSource(DUMMY_MUSIC_URI)
        verify(player, times(1)).prepare()
    }

    @Test
    fun start() {
        binder.start()

        verify(player, times(1)).start()
        verify(callbacks, times(1)).onPlayerStateChagned()
    }

    @Test
    fun pause() {
        binder.pause()

        verify(player, times(1)).pause()
        verify(callbacks, times(1)).onPlayerStateChagned()
    }

    @Test
    fun reset() {
        binder.reset()

        verify(player, times(1)).reset()
        verify(callbacks, times(1)).onPlayerStateChagned()
        verify(callbacks, times(1)).onMusicFinished()
    }

    @Test
    fun togglePlaying_WhenNotPlaying() {
        binder.togglePlaying()

        verify(player, times(1)).start()
    }

    @Test
    fun togglePlaying_WhenPlaying() {
        `when`(player.isPlaying).thenReturn(true)

        binder.togglePlaying()

        verify(player, times(1)).pause()
    }
}