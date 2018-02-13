package jp.gr.java_conf.mmktomato.fluffyplayer.player

import android.media.MediaPlayer
import jp.gr.java_conf.mmktomato.fluffyplayer.DUMMY_MUSIC_URI
import jp.gr.java_conf.mmktomato.fluffyplayer.di.component.MockComponentInjector
import kotlinx.coroutines.experimental.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.ParameterizedRobolectricTestRunner

/**
 * Tests for PlayerServiceBinder.
 *
 * @param isPlaying whether the player is playing music.
 */
@RunWith(ParameterizedRobolectricTestRunner::class)
class PlayerServiceBinderTest(private val isPlaying: Boolean) {

    private interface CallbackHolder {
        fun onPlayerStateChagned()
        fun onMusicFinished()
    }

    lateinit var player: MediaPlayer

    private lateinit var binder: PlayerServiceBinder

    private lateinit var listenerIndices: MutableList<Int>

    private lateinit var callbacks: CallbackHolder

    companion object {
        @BeforeClass
        @JvmStatic
        fun setUpClass() {
            MockComponentInjector.setTestMode()
        }

        @ParameterizedRobolectricTestRunner.Parameters(name = "player#isPlaying = {0}")
        @JvmStatic
        fun isPlayingParams(): List<Array<out Boolean>> {
            return listOf(arrayOf(true), arrayOf(false))
        }
    }

    @Before
    fun setUp() {
        player = mock(MediaPlayer::class.java)
        `when`(player.isPlaying).thenReturn(isPlaying)

        binder = PlayerServiceBinder(player)
        listenerIndices = mutableListOf()
        callbacks = mock(CallbackHolder::class.java)
        listenerIndices.add(binder.addOnPlayerStateChangedListener(callbacks::onPlayerStateChagned))
        listenerIndices.add(binder.addOnMusicFinishedListener(callbacks::onMusicFinished))
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
    fun togglePlaying() {
        binder.togglePlaying()

        if (isPlaying) {
            verify(player, times(1)).pause()
        }
        else {
            verify(player, times(1)).start()
        }
    }
}