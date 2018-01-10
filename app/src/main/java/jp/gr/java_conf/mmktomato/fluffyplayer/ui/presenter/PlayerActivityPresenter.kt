package jp.gr.java_conf.mmktomato.fluffyplayer.ui.presenter

import android.app.NotificationManager
import android.content.Intent
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.media.MediaMetadataRetriever
import android.os.IBinder
import android.widget.Button
import jp.gr.java_conf.mmktomato.fluffyplayer.R
import jp.gr.java_conf.mmktomato.fluffyplayer.dropbox.DbxNodeMetadata
import jp.gr.java_conf.mmktomato.fluffyplayer.dropbox.DbxProxy
import jp.gr.java_conf.mmktomato.fluffyplayer.entity.MusicMetadata
import jp.gr.java_conf.mmktomato.fluffyplayer.player.PlayerService
import jp.gr.java_conf.mmktomato.fluffyplayer.player.PlayerServiceBinder
import jp.gr.java_conf.mmktomato.fluffyplayer.player.PlayerServiceState
import jp.gr.java_conf.mmktomato.fluffyplayer.prefs.SharedPrefsHelper
import jp.gr.java_conf.mmktomato.fluffyplayer.ui.viewmodel.PlayerActivityViewModel
import jp.gr.java_conf.mmktomato.fluffyplayer.usecase.NotificationUseCase
import kotlinx.coroutines.experimental.*
import java.io.ByteArrayInputStream

/**
 * A presenter of PlayerActivity.
 */
internal interface PlayerActivityPresenter {
    /**
     * the NotificationUseCase.
     */
    val notificationUseCase: NotificationUseCase

    /**
     * the view model of this activity.
     */
    val viewModel: PlayerActivityViewModel

    /**
     * the player service state.
     */
    var svcState: PlayerServiceState

    /**
     * a Dropbox's node metadata.
     */
    val dbxMetadata: DbxNodeMetadata

    /**
     * indicates whether the PlayerService is initialized.
     */
    var isPlayerServiceInitialized: Boolean

    /**
     * a callback to bind a service.
     */
    val unbindService: () -> Unit

    /**
     * a callback to get string resources.
     */
    val getString: (Int) -> String

    fun onCreate()

    fun onDestroy() {
        if (!isPlayerServiceInitialized) {
            return
        }

        if (svcState.isBound) {
            unbindService()
        }
        svcState.unbind()
    }

    /**
     * A callback of playButton's onClick.
     */
    fun onPlayButtonClick() {
        if (isPlayerServiceInitialized) {
            svcState.binder.togglePlaying()
        }
    }

    /**
     * Initializes the PlayerServiceState.
     */
    fun initializePlayerServiceState(binder: IBinder?) = launch(CommonPool) {
        val onPlayerStateChanged = { viewModel.isPlaying.set(svcState.binder.isPlaying) }
        val onMusicChanged = { resetUI() }
        svcState =  PlayerServiceState(
                binder = binder as PlayerServiceBinder,
                isBound = true,
                onPlayerStateChangedListener = onPlayerStateChanged,
                onMusicChanged = onMusicChanged)

        onPlayerStateChanged()

        val musicUri = getMusicUri().await()
        val musicMetadataDeferred = async {
            /**
             * TODO: if the currently played music is not changed, to retrieve metadata is not needed.
             * but to set metadata to UI is needed. so the metadata must be stored somewhere.
             */
            getMusicMetadata(musicUri).await()
        }
        val preparePlayerJob = launch {
            if (!svcState.binder.isPlaying) {
                svcState.binder.prepare(musicUri)  // suspend function.
            }
        }

        val musicMetadata = musicMetadataDeferred.await()
        preparePlayerJob.join()

        setMusicMetadata(musicMetadata)
        updateNotification(musicMetadata)
        if (!svcState.binder.isPlaying) {
            svcState.binder.start()
        }

        isPlayerServiceInitialized = true
    }

    /**
     * Unbinds the PlayerServiceState.
     */
    fun unbindPlayerServiceState() {
        svcState.unbind()
    }

    /**
     * Returns the music metadata.
     *
     * @param uri the music uri.
     */
    fun getMusicMetadata(uri: String): Deferred<MusicMetadata>

    /**
     * Sets the music metadata.
     *
     * @param metadata the music metadata.
     */
    fun setMusicMetadata(metadata: MusicMetadata) {
        viewModel.title.set(metadata.title)
        viewModel.artwork.set(metadata.artwork)
    }

    /**
     * Returns the music uri.
     */
    fun getMusicUri(): Deferred<String>

    /**
     * Resets UI Components.
     */
    fun resetUI() {
        setMusicMetadata(MusicMetadata(
                title = getString(R.string.now_loading_text),
                artwork = noArtworkImage))

        viewModel.isPlaying.set(false)
    }

    /**
     * Updates now playing notification.
     *
     * @param metadata a music metadata.
     */
    fun updateNotification(metadata: MusicMetadata)

    /**
     * Returns the empty album artwork.
     */
    val noArtworkImage: Drawable
}

/**
 * An implementation of PlayerActivityPresenter.
 *
 * @param sharedPrefs the SharedPrefsHelper.
 * @param dbxProxy the Dropbox API Proxy.
 * @param viewModel the view model.
 * @param dbxMetadata the Dropbox's node metadata.
 * @param playerServiceIntent the player service intent.
 * @param startService the callback to start a service.
 * @param bindService the callback to bind a service.
 * @param unbindService the callback to unbind a service.
 * @param getString the callback to get string resources.
 * @param notificationManager the NotificationManager.
 * @param playButton the play button.
 * @param resources android's resource.
 */
internal class PlayerActivityPresenterImpl(
        private val sharedPrefs: SharedPrefsHelper,
        private val dbxProxy: DbxProxy,
        override val viewModel: PlayerActivityViewModel,
        override val dbxMetadata: DbxNodeMetadata,
        private val playerServiceIntent: Intent,
        private val startService: (Intent) -> Unit,
        private val bindService: (Intent) -> Unit,
        override val unbindService: () -> Unit,
        override val getString: (Int) -> String,
        private val notificationManager: NotificationManager,
        private val playButton: Button,
        private val resources: Resources) : PlayerActivityPresenter {

    /**
     * the NotificationUseCase.
     */
    override val notificationUseCase = NotificationUseCase()

    /**
     * the player service state.
     */
    override lateinit var svcState: PlayerServiceState

    /**
     * indicates whether the PlayerService is initialized.
     */
    override var isPlayerServiceInitialized: Boolean = false

    override fun onCreate() {
        resetUI()

        playButton.setOnClickListener { v -> onPlayButtonClick() }

        playerServiceIntent.putExtra("dbxMetadata", dbxMetadata)
        startService(playerServiceIntent)
        bindService(playerServiceIntent)
    }

    /**
     * Returns the music metadata.
     */
    override fun getMusicMetadata(uri: String): Deferred<MusicMetadata> = async {
        val mmr = MediaMetadataRetriever()
        mmr.setDataSource(uri, mapOf<String, String>())

        // title
        val title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)

        // artwork
        val artworkBytes: ByteArray? = mmr.embeddedPicture
        val artworkDrawable = if (artworkBytes == null) {
            noArtworkImage
        } else {
            ByteArrayInputStream(artworkBytes).use {
                Drawable.createFromStream(it, null)
            }
        }

        return@async MusicMetadata(title, artworkDrawable)
    }

    /**
     * Returns a music uri.
     */
    override fun getMusicUri(): Deferred<String> {
        return dbxProxy.getTemporaryLink(dbxMetadata.path)
    }

    /**
     * Updates now playing notification.
     *
     * @param metadata a music metadata.
     */
    override fun updateNotification(metadata: MusicMetadata) {
        val notification = notificationUseCase.createNowPlayingNotification(
                sharedPrefs.context, dbxMetadata, metadata.title ?: getString(R.string.unknown_music_title))

        notificationManager.notify(PlayerService.NOTIFICATION_ID, notification)
    }

    /**
     * Returns the empty album artwork.
     */
    override val noArtworkImage: Drawable
        get() = resources.getDrawable(R.drawable.ic_no_image, null)
}