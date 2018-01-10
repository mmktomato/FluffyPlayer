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
    private data class ListenerCallState(
            var isOnPlayerStateChagnedCalled: Boolean,
            var isOnMusicChangedCallded: Boolean)

    @Inject
    lateinit var player: MediaPlayer

    private lateinit var binder: PlayerServiceBinder

    private lateinit var listenerIndices: MutableList<Int>

    private lateinit var listenerCallState: ListenerCallState

    @Before
    fun setUp() {
        DaggerPlayerServiceBinderTestComponent.builder()
                .playerModuleMock(PlayerModuleMock(false))
                .build()
                .inject(this)

        binder = PlayerServiceBinder(player)
        listenerIndices = mutableListOf()
        listenerCallState = ListenerCallState(false, false)

        binder.addOnPlayerStateChangedListener {
            listenerCallState.isOnPlayerStateChagnedCalled = true
        }
        binder.addOnMusicChangedListener {
            listenerCallState.isOnMusicChangedCallded = true
        }
    }

    @After
    fun tearDown() {
        listenerIndices.forEach { index ->
            binder.removeOnPlayerStateChangedListener(index)
            binder.removeOnMusicChangedListener(index)
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
        assertTrue(listenerCallState.isOnPlayerStateChagnedCalled)
    }

    @Test
    fun pause() {
        binder.pause()

        verify(player, times(1)).pause()
        assertTrue(listenerCallState.isOnPlayerStateChagnedCalled)
    }

    @Test
    fun reset() {
        binder.reset()

        verify(player, times(1)).reset()
        assertTrue(listenerCallState.isOnPlayerStateChagnedCalled)
        assertTrue(listenerCallState.isOnMusicChangedCallded)
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