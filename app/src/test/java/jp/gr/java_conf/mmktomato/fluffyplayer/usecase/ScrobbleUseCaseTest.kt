package jp.gr.java_conf.mmktomato.fluffyplayer.usecase

import android.content.Context
import de.umass.lastfm.Session
import jp.gr.java_conf.mmktomato.fluffyplayer.*
import jp.gr.java_conf.mmktomato.fluffyplayer.di.component.MockComponentInjector
import jp.gr.java_conf.mmktomato.fluffyplayer.entity.MusicMetadata
import jp.gr.java_conf.mmktomato.fluffyplayer.proxy.LastFmProxy
import org.junit.Assert.*
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

/**
 * Tests for ScrobbleUseCase.
 */
@RunWith(RobolectricTestRunner::class)
class ScrobbleUseCaseTest {
    private lateinit var ctx: Context
    private lateinit var lastFmProxy: LastFmProxy
    private lateinit var useCase: ScrobbleUseCase

    companion object {
        @BeforeClass
        @JvmStatic
        fun setUpClass() {
            MockComponentInjector.setTestMode()
        }
    }

    @Before
    fun setUp() {
        ctx = RuntimeEnvironment.application
        lastFmProxy = mock(LastFmProxy::class.java)

        val session = mock(Session::class.java)
        useCase = ScrobbleUseCaseImpl(session, lastFmProxy)
    }

    /**
     * Returns a dummy music metadata.
     */
    private fun createDummyMusicMetadata(): MusicMetadata {
        // TODO: this is duplicated with PlayerActivityPresenterTest#createMusicMetadata.

        val artwork = ctx.resources.getDrawable(R.drawable.ic_no_image, null)
        return MusicMetadata(
                title = DUMMY_MUSIC_TITLE,
                artist = DUMMY_MUSIC_ARTIST,
                duration = DUMMY_MUSIC_DURATION,
                trackNumber = DUMMY_MUSIC_TRACK_NUMBER,
                artwork = artwork,
                albumTitle = DUMMY_ALBUM_TITLE,
                albumArtist = DUMMY_ALBUM_ARTIST)
    }

    @Test
    fun createScrobbleData() {
        val metadata = createDummyMusicMetadata()
        val scrobbleData = useCase.createScrobbleData(metadata)

        assertEquals(metadata.title, scrobbleData.track)
        assertEquals(metadata.artist, scrobbleData.artist)
        //assertEquals(  timestamp
        assertEquals(metadata.duration, scrobbleData.duration)
        assertEquals(metadata.albumTitle, scrobbleData.album)
        assertEquals(metadata.albumArtist, scrobbleData.albumArtist)
        assertNull(scrobbleData.musicBrainzId)
        assertEquals(metadata.trackNumber, scrobbleData.trackNumber)
        assertNull(scrobbleData.streamId)
        assertTrue(scrobbleData.isChosenByUser)
    }

    @Test
    fun updateNowPlaying() {
        val metadata = createDummyMusicMetadata()
        val result = useCase.updateNowPlaying(metadata)

        verify(lastFmProxy, times(1)).updateNowPlaying(MockitoWorkaround.any(), MockitoWorkaround.any())
    }

    @Test
    fun scrobble() {
        val metadata = createDummyMusicMetadata()
        val result = useCase.scrobble(metadata)

        verify(lastFmProxy, times(1)).scrobble(MockitoWorkaround.any(), MockitoWorkaround.any())
    }
}