package jp.gr.java_conf.mmktomato.fluffyplayer.ui.presenter

import android.app.NotificationManager
import android.content.Intent
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.media.MediaMetadataRetriever
import android.os.IBinder
import android.widget.Button
import jp.gr.java_conf.mmktomato.fluffyplayer.R
import jp.gr.java_conf.mmktomato.fluffyplayer.db.AppDatabase
import jp.gr.java_conf.mmktomato.fluffyplayer.db.model.PlaylistItem
import jp.gr.java_conf.mmktomato.fluffyplayer.di.component.DaggerPlayerActivityPresenterComponent
import jp.gr.java_conf.mmktomato.fluffyplayer.di.module.AppModule
import jp.gr.java_conf.mmktomato.fluffyplayer.di.module.DatabaseModule
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
import java.util.*
import javax.inject.Inject

/**
 * A presenter of PlayerActivity.
 */
internal interface PlayerActivityPresenter {
    /**
     * the database.
     */
    val db: AppDatabase

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
     * the now playing item.
     */
    val nowPlayingItem: PlaylistItem?

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

    /**
     * a MediaMetadataRetriever.
     */
    val mediaMetadataRetriever: MediaMetadataRetriever

    fun onCreate()

    fun onDestroy() {
        mediaMetadataRetriever.release()

        if (!isPlayerServiceInitialized) {
            return
        }

        if (svcState.isBound) {
            unbindService()
        }
        unbindPlayerServiceState()
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

            nowPlayingItem!!.status = PlaylistItem.Status.PLAYING
            db.playlistDao.update(nowPlayingItem!!)
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
 * @param nowPlayingItem the now playing item.
 * @param playerServiceIntent the player service intent.
 * @param startService the callback to start a service.
 * @param bindService the callback to bind a service.
 * @param unbindService the callback to unbind a service.
 * @param getString the callback to get string resources.
 * @param notificationManager the NotificationManager.
 * @param playButton the play button.
 * @param resources android's resource.
 * @param mediaMetadataRetriever the mediaMetadataRetriever.
 */
class PlayerActivityPresenterImpl(
        private val sharedPrefs: SharedPrefsHelper,
        private val dbxProxy: DbxProxy,
        override val viewModel: PlayerActivityViewModel,
        private val dbxMetadata: DbxNodeMetadata?,
        override var nowPlayingItem: PlaylistItem?,
        private val playerServiceIntent: Intent,
        private val startService: (Intent) -> Unit,
        private val bindService: (Intent) -> Unit,
        override val unbindService: () -> Unit,
        override val getString: (Int) -> String,
        private val notificationManager: NotificationManager,
        private val playButton: Button,
        private val resources: Resources,
        override val mediaMetadataRetriever: MediaMetadataRetriever) : PlayerActivityPresenter {

    /**
     * the database.
     */
    @Inject
    override lateinit var db: AppDatabase

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

    init {
        DaggerPlayerActivityPresenterComponent.builder()
                .appModule(AppModule(sharedPrefs.context))
                .databaseModule(DatabaseModule())
                .build()
                .inject(this)
    }

    override fun onCreate() {
        resetUI()

        playButton.setOnClickListener { v -> onPlayButtonClick() }

        launch(CommonPool) {
            if (dbxMetadata != null) {
                val id = UUID.randomUUID().toString()
                nowPlayingItem = PlaylistItem(id, dbxMetadata.path, PlaylistItem.Status.WAIT)

                db.playlistDao.deleteAll()
                db.playlistDao.insert(nowPlayingItem!!)
            }
            else if (nowPlayingItem == null) {
                nowPlayingItem = db.playlistDao.getNowPlaying()
            }

            playerServiceIntent.putExtra("dbxMetadata", dbxMetadata)
            playerServiceIntent.putExtra("nowPlayingItem", nowPlayingItem)

            startService(playerServiceIntent)
            bindService(playerServiceIntent)
        }
    }

    /**
     * Returns the music metadata.
     */
    override fun getMusicMetadata(uri: String): Deferred<MusicMetadata> = async {
        mediaMetadataRetriever.setDataSource(uri, mapOf<String, String>())

        // title
        val title = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)

        // artwork
        val artworkBytes: ByteArray? = mediaMetadataRetriever.embeddedPicture
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
        return dbxProxy.getTemporaryLink(nowPlayingItem!!.path)
    }

    /**
     * Updates now playing notification.
     *
     * @param metadata a music metadata.
     */
    override fun updateNotification(metadata: MusicMetadata) {
        val notification = notificationUseCase.createNowPlayingNotification(
                sharedPrefs.context, nowPlayingItem!!, metadata.title ?: getString(R.string.unknown_music_title))

        notificationManager.notify(PlayerService.NOTIFICATION_ID, notification)
    }

    /**
     * Returns the empty album artwork.
     */
    override val noArtworkImage: Drawable
        get() = resources.getDrawable(R.drawable.ic_no_image, null)
}