package jp.gr.java_conf.mmktomato.fluffyplayer.ui.presenter

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.widget.Button
import jp.gr.java_conf.mmktomato.fluffyplayer.DUMMY_MUSIC_TITLE
import jp.gr.java_conf.mmktomato.fluffyplayer.DUMMY_MUSIC_URI
import jp.gr.java_conf.mmktomato.fluffyplayer.R
import jp.gr.java_conf.mmktomato.fluffyplayer.di.component.DaggerPlayerActivityPresenterTestComponent
import jp.gr.java_conf.mmktomato.fluffyplayer.di.module.AppModuleMock
import jp.gr.java_conf.mmktomato.fluffyplayer.di.module.DbxModuleMock
import jp.gr.java_conf.mmktomato.fluffyplayer.di.module.PlayerModuleMock
import jp.gr.java_conf.mmktomato.fluffyplayer.di.module.SharedPrefsModuleMock
import jp.gr.java_conf.mmktomato.fluffyplayer.dropbox.DbxNodeMetadata
import jp.gr.java_conf.mmktomato.fluffyplayer.dropbox.DbxProxy
import jp.gr.java_conf.mmktomato.fluffyplayer.entity.MusicMetadata
import jp.gr.java_conf.mmktomato.fluffyplayer.player.PlayerService
import jp.gr.java_conf.mmktomato.fluffyplayer.player.PlayerServiceBinder
import jp.gr.java_conf.mmktomato.fluffyplayer.player.PlayerServiceState
import jp.gr.java_conf.mmktomato.fluffyplayer.prefs.SharedPrefsHelper
import jp.gr.java_conf.mmktomato.fluffyplayer.ui.viewmodel.PlayerActivityViewModel
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner
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
    @field:Named("File")
    lateinit var dbxFileMetadata: DbxNodeMetadata

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
    private lateinit var notificationManager: NotificationManager
    private lateinit var presenter: PlayerActivityPresenter

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
        notificationManager = mock(NotificationManager::class.java)
        presenter = PlayerActivityPresenterImpl(
                sharedPrefs = sharedPrefs,
                dbxProxy = dbxProxy,
                viewModel = viewModel,
                dbxMetadata = dbxFileMetadata,
                playerServiceIntent = playerServiceIntent,
                startService = callbacks::startService,
                bindService = callbacks::bindService,
                unbindService = callbacks::unbindService,
                getString = ctx::getString,
                notificationManager = notificationManager,
                playButton = Button(ctx),
                resources = ctx.resources,
                mediaMetadataRetriever = mediaMetadataRetriever)
    }

    /**
     * Prepares `presenter.svcState`.
     *
     * @param isBound initial value of `isBound`.
     */
    private fun prepareDummyPlayerServiceState(isBound: Boolean) {
        presenter.isPlayerServiceInitialized = true
        presenter.svcState = PlayerServiceState(
                binder = PlayerServiceBinder(player),
                isBound = isBound,
                onMusicChanged = { },
                onPlayerStateChangedListener = { })
    }

    @Test
    fun onCreate() {
        presenter.onCreate()

        val intentMetadata = playerServiceIntent.getSerializableExtra("dbxMetadata")
        assertEquals(dbxFileMetadata, intentMetadata)
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

        assertEquals(DUMMY_MUSIC_TITLE, viewModel.title.get())
        assertTrue(viewModel.isPlaying.get())
        verify(notificationManager, times(1)).notify(eq(PlayerService.NOTIFICATION_ID), any(Notification::class.java))
        verify(player, times(0)).start()
        assertTrue(presenter.isPlayerServiceInitialized)
    }

    @Test
    fun initializePlayerServiceState_WhenNotPlaying() {
        runBlocking {
            presenter.initializePlayerServiceState(PlayerServiceBinder(player)).join()
        }

        assertEquals(DUMMY_MUSIC_TITLE, viewModel.title.get())
        assertFalse(viewModel.isPlaying.get())
        verify(notificationManager, times(1)).notify(eq(PlayerService.NOTIFICATION_ID), any(Notification::class.java))
        verify(player, times(1)).start()
        assertTrue(presenter.isPlayerServiceInitialized)
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

            assertEquals(DUMMY_MUSIC_TITLE, metadata.title)
        }
    }

    @Test
    fun setMusicMetadata() {
        val artwork = ctx.resources.getDrawable(R.drawable.ic_no_image, null)
        val metadata = MusicMetadata(DUMMY_MUSIC_TITLE, artwork)

        presenter.setMusicMetadata(metadata)

        assertEquals(DUMMY_MUSIC_TITLE, viewModel.title.get())
        assertEquals(artwork, viewModel.artwork.get())
    }

    @Test
    fun getMusicUri() {
        runBlocking {
            val uri = presenter.getMusicUri().await()

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
    fun updateNotification() {
        presenter.updateNotification(mock(MusicMetadata::class.java))

        verify(notificationManager, times(1)).notify(eq(PlayerService.NOTIFICATION_ID), any(Notification::class.java))
    }
}