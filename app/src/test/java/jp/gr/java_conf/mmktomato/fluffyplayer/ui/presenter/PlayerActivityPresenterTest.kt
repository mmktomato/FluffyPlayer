package jp.gr.java_conf.mmktomato.fluffyplayer.ui.presenter

import android.app.Notification
import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.widget.Button
import de.umass.lastfm.scrobble.ScrobbleResult
import jp.gr.java_conf.mmktomato.fluffyplayer.*
import jp.gr.java_conf.mmktomato.fluffyplayer.db.model.PlaylistItem
import jp.gr.java_conf.mmktomato.fluffyplayer.di.component.DaggerPlayerActivityPresenterTestComponent
import jp.gr.java_conf.mmktomato.fluffyplayer.di.component.DependencyInjector
import jp.gr.java_conf.mmktomato.fluffyplayer.di.component.MockComponentInjector
import jp.gr.java_conf.mmktomato.fluffyplayer.di.module.AppModuleMock
import jp.gr.java_conf.mmktomato.fluffyplayer.di.module.DbxModuleMock
import jp.gr.java_conf.mmktomato.fluffyplayer.di.module.PlayerModuleMock
import jp.gr.java_conf.mmktomato.fluffyplayer.di.module.SharedPrefsModuleMock
import jp.gr.java_conf.mmktomato.fluffyplayer.dropbox.DbxNodeMetadata
import jp.gr.java_conf.mmktomato.fluffyplayer.dropbox.DbxProxy
import jp.gr.java_conf.mmktomato.fluffyplayer.entity.MusicMetadata
import jp.gr.java_conf.mmktomato.fluffyplayer.player.PlayerServiceBinder
import jp.gr.java_conf.mmktomato.fluffyplayer.player.PlayerServiceState
import jp.gr.java_conf.mmktomato.fluffyplayer.prefs.AppPrefs
import jp.gr.java_conf.mmktomato.fluffyplayer.prefs.SharedPrefsHelper
import jp.gr.java_conf.mmktomato.fluffyplayer.ui.viewmodel.PlayerActivityViewModel
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowToast
import javax.inject.Inject
import javax.inject.Named

/**
 * Tests for PlayerActivityPresenter.
 */
@RunWith(RobolectricTestRunner::class)
class PlayerActivityPresenterTest {
    private interface CallbackHolder {
        fun startService(playerServiceIntent: Intent)
        fun bindService(playerServiceIntent: Intent)
        fun unbindService()
    }

    @Inject
    lateinit var ctx: Context

    @Inject
    lateinit var sharedPrefs: SharedPrefsHelper

    @Inject
    lateinit var dbxProxy: DbxProxy

    @Inject
    @field:Named("FileArray")
    lateinit var dbxFileMetadataArray: Array<DbxNodeMetadata>

    @Inject
    lateinit var player: MediaPlayer

    @Inject
    lateinit var mediaMetadataRetriever: MediaMetadataRetriever

//    @Inject
//    @field:Named("Directory")
//    lateinit var dbxDirectoryMetadata: DbxNodeMetadata

    private lateinit var viewModel: PlayerActivityViewModel
    private lateinit var callbacks: CallbackHolder
    private lateinit var playerServiceIntent: Intent
    private lateinit var presenter: PlayerActivityPresenter

    companion object {
        @BeforeClass
        @JvmStatic
        fun setUpClass() {
            MockComponentInjector.setTestMode()
        }
    }

    @Before
    fun setUp() {
        DaggerPlayerActivityPresenterTestComponent.builder()
                .appModuleMock(AppModuleMock())
                .sharedPrefsModuleMock(SharedPrefsModuleMock())
                .dbxModuleMock(DbxModuleMock(true))
                .playerModuleMock(PlayerModuleMock(false))
                .build()
                .inject(this)

        viewModel = PlayerActivityViewModel()
        callbacks = mock(CallbackHolder::class.java)
        playerServiceIntent = Intent()
        presenter = PlayerActivityPresenterImpl(
                viewModel = viewModel,
                dbxMetadataArray = dbxFileMetadataArray,
                nowPlayingItem = null,
                playerServiceIntent = playerServiceIntent,
                startService = callbacks::startService,
                bindService = callbacks::bindService,
                unbindService = callbacks::unbindService,
                getString = ctx::getString,
                playButton = Button(ctx),
                resources = ctx.resources,
                mediaMetadataRetriever = mediaMetadataRetriever)

        runBlocking {
            DependencyInjector.injector.inject(presenter as PlayerActivityPresenterImpl, ctx)

            presenter.onCreate().join()
        }
    }

    /**
     * Prepares dummy `presenter.svcState`.
     *
     * @param isBound initial value of `isBound`.
     */
    private fun prepareDummyPlayerServiceState(isBound: Boolean) {
        presenter.isPlayerServiceInitialized = true
        presenter.svcState = PlayerServiceState(
                binder = PlayerServiceBinder(player),
                isBound = isBound,
                onMusicFinished = { },
                onPlayerStateChangedListener = { })
    }

    /**
     * Returns a dummy music metadata.
     */
    private fun createDummyMusicMetadata(): MusicMetadata {
        // TODO: this is duplicated with ScrobbleUseCateTest#createMusicMetadata.

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
    fun onCreate() {
        // TODO: add tests for launched by FileBrowseActivity, launched by Notification, etc...

        //presenter.onCreate().join()

        val intentMetadataArray = playerServiceIntent.getSerializableExtra("dbxMetadataArray") as Array<DbxNodeMetadata>
        assertArrayEquals(dbxFileMetadataArray, intentMetadataArray)

        val intentPlaylistItem = playerServiceIntent.getSerializableExtra("nowPlayingItem")
        assertEquals(presenter.nowPlayingItem!!, intentPlaylistItem)
        assertEquals(DUMMY_DBX_FILE_PATH, presenter.nowPlayingItem!!.path)
        assertEquals(PlaylistItem.Status.WAIT, presenter.nowPlayingItem!!.status)

        verify(callbacks, times(1)).startService(playerServiceIntent)
        verify(callbacks, times(1)).bindService(playerServiceIntent)
    }

    @Test
    fun onDestroy_WhenBindedToService() {
        prepareDummyPlayerServiceState(true)

        presenter.onDestroy()

        verify(callbacks, times(1)).unbindService()
        verify(mediaMetadataRetriever, times(1)).release()
        assertFalse(presenter.svcState.isBound)
    }

    @Test
    fun onDestroy_WhenNotBindedToService() {
        prepareDummyPlayerServiceState(false)

        presenter.onDestroy()

        verify(callbacks, times(0)).unbindService()
        verify(mediaMetadataRetriever, times(1)).release()
        assertFalse(presenter.svcState.isBound)
    }

    @Test
    fun onPlayButtonClick_WhenPlaying() {
        `when`(player.isPlaying).thenReturn(true)
        prepareDummyPlayerServiceState(true)

        presenter.onPlayButtonClick()

        verify(player, times(1)).pause()
        assertTrue(presenter.svcState.isBound)
    }

    @Test
    fun onPlayButtonClick_WhenNotPlaying() {
        prepareDummyPlayerServiceState(true)

        presenter.onPlayButtonClick()

        verify(player, times(1)).start()
        assertTrue(presenter.svcState.isBound)
    }

    @Test
    fun initializePlayerServiceState_WhenPlaying() {
        `when`(player.isPlaying).thenReturn(true)

        runBlocking {
            presenter.initializePlayerServiceState(PlayerServiceBinder(player)).join()
        }

        verify(player, times(0)).start()
        assertTrue(presenter.isPlayerServiceInitialized)
    }

    @Test
    fun initializePlayerServiceState_WhenNotPlaying() {
        runBlocking {
            presenter.initializePlayerServiceState(PlayerServiceBinder(player)).join()
        }

        verify(player, times(1)).start()
        assertTrue(presenter.isPlayerServiceInitialized)
    }

    @Test
    fun onMusicFinished() {
        prepareDummyPlayerServiceState(true)

        runBlocking {
            presenter.onMusicFinished().join()
        }

        assertEquals(2, presenter.nowPlayingItem!!.order)
        assertEquals(PlaylistItem.Status.PLAYING.value, presenter.nowPlayingItem!!.status.value)

        verify(player, times(1)).start()

        val expectedMetadata = createDummyMusicMetadata()
        verify(presenter.scrobbleUseCase, times(1)).scrobble(MockitoWorkaround.eq(createDummyMusicMetadata()))
    }

    @Test
    fun startRefreshUI() {
        runBlocking {
            presenter.startRefreshUI(DUMMY_MUSIC_URI).await()
        }

        assertEquals(DUMMY_MUSIC_TITLE, viewModel.title.get())
    }

    @Test
    fun startMusicWithRefreshUI() {
        prepareDummyPlayerServiceState(true)

        runBlocking {
            presenter.startMusicWithRefreshUi(DUMMY_MUSIC_URI).join()
        }

        //assertTrue(viewModel.isPlaying.get())
        assertEquals(DUMMY_MUSIC_TITLE, viewModel.title.get())
        verify(presenter.notificationUseCase.notificationManager, times(1)).notify(eq(AppPrefs.NOW_PLAYING_NOTIFICATION_ID), any(Notification::class.java))
        verify(player, times(1)).start()
        assertEquals(PlaylistItem.Status.PLAYING, presenter.nowPlayingItem!!.status)

        val expectedMetadata = createDummyMusicMetadata()
        verify(presenter.scrobbleUseCase, times(1)).updateNowPlaying(MockitoWorkaround.eq(expectedMetadata))
    }

    @Test
    fun unbindPlayerServiceState() {
        prepareDummyPlayerServiceState(true)

        presenter.unbindPlayerServiceState()

        assertFalse(presenter.svcState.isBound)
    }

    @Test
    fun getMusicMetadata() {
        runBlocking {
            val metadata = presenter.getMusicMetadata(DUMMY_MUSIC_URI).await()
            val expected = createDummyMusicMetadata()

            assertEquals(expected, metadata)
        }
    }

    @Test
    fun setMusicMetadata() {
        val metadata = createDummyMusicMetadata()

        presenter.setMusicMetadata(metadata)

        assertEquals(DUMMY_MUSIC_TITLE, viewModel.title.get())
        assertEquals(metadata.artwork, viewModel.artwork.get())
    }

    @Test
    fun getMusicUri() {
        runBlocking {
            val uri = presenter.getMusicUri(DUMMY_DBX_FILE_PATH).await()

            assertEquals(DUMMY_MUSIC_URI, uri)
        }
    }

    @Test
    fun resetUI() {
        presenter.resetUI()

        assertEquals(ctx.getString(R.string.now_loading_text), viewModel.title.get())
        assertEquals(ctx.resources.getDrawable(R.drawable.ic_no_image, null), viewModel.artwork.get())
        assertFalse(viewModel.isPlaying.get())
    }

    @Test
    fun handleScrobbleResult_Null() {
        presenter.handleScrobbleResult(null)

        assertNull(ShadowToast.getLatestToast())
    }

    @Test
    fun handleScrobbleResult_Successful() {
        val result = mock(ScrobbleResult::class.java)
        `when`(result.isSuccessful).thenReturn(true)
        `when`(result.isIgnored).thenReturn(false)

        presenter.handleScrobbleResult(result)

        assertNull(ShadowToast.getLatestToast())
    }

    @Test
    fun handleScrobbleResult_NowSuccessful() {
        val result = mock(ScrobbleResult::class.java)
        `when`(result.isSuccessful).thenReturn(false)
        `when`(result.isIgnored).thenReturn(false)

        presenter.handleScrobbleResult(result)

        assertEquals(ctx.getString(R.string.last_fm_failure), ShadowToast.getTextOfLatestToast())
    }

    @Test
    fun handleScrobbleResult_Ignored() {
        val result = mock(ScrobbleResult::class.java)
        `when`(result.isSuccessful).thenReturn(true)
        `when`(result.isIgnored).thenReturn(true)

        presenter.handleScrobbleResult(result)

        assertEquals(ctx.getString(R.string.last_fm_failure), ShadowToast.getTextOfLatestToast())
    }
}