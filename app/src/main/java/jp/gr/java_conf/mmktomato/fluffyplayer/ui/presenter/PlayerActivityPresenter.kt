package jp.gr.java_conf.mmktomato.fluffyplayer.ui.presenter

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.media.MediaMetadataRetriever
import android.os.IBinder
import android.widget.Button
import android.widget.Toast
import jp.gr.java_conf.mmktomato.fluffyplayer.R
import jp.gr.java_conf.mmktomato.fluffyplayer.db.AppDatabase
import jp.gr.java_conf.mmktomato.fluffyplayer.db.model.PlaylistItem
import jp.gr.java_conf.mmktomato.fluffyplayer.dropbox.DbxNodeMetadata
import jp.gr.java_conf.mmktomato.fluffyplayer.dropbox.DbxProxy
import jp.gr.java_conf.mmktomato.fluffyplayer.entity.MusicMetadata
import jp.gr.java_conf.mmktomato.fluffyplayer.player.PlayerServiceBinder
import jp.gr.java_conf.mmktomato.fluffyplayer.player.PlayerServiceState
import jp.gr.java_conf.mmktomato.fluffyplayer.ui.viewmodel.PlayerActivityViewModel
import jp.gr.java_conf.mmktomato.fluffyplayer.usecase.NotificationUseCase
import jp.gr.java_conf.mmktomato.fluffyplayer.usecase.ScrobbleUseCase
import kotlinx.coroutines.experimental.*
import java.io.ByteArrayInputStream
import java.util.*
import javax.inject.Inject

/**
 * A presenter of PlayerActivity.
 */
internal interface PlayerActivityPresenter {
    /**
     * An android's Context.
     */
    val ctx: Context

    /**
     * the database.
     */
    var db: AppDatabase

    /**
     * the NotificationUseCase.
     */
    var notificationUseCase: NotificationUseCase

    /**
     * the ScrobbleUseCase.
     */
    var scrobbleUseCase: ScrobbleUseCase

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

    fun onCreate(): Job

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
        svcState =  PlayerServiceState(
                binder = binder as PlayerServiceBinder,
                isBound = true,
                onPlayerStateChangedListener = onPlayerStateChanged,
                onMusicFinished = { onMusicFinished() })

        onPlayerStateChanged()

        val musicUri = getMusicUri().await()

        when (svcState.binder.isPlaying) {
            true ->  startRefreshUI(musicUri)
            false -> startMusicWithRefreshUi(musicUri)
        }.join()

        isPlayerServiceInitialized = true
    }

    /**
     * The callback of `onMusicFinished`.
     */
    fun onMusicFinished(): Job

    /**
     * Fetches music metadata and refreshes UI.
     *
     * @param uri the music uri.
     */
    fun startRefreshUI(uri: String) = async {
        val musicMetadata = getMusicMetadata(uri).await()
        setMusicMetadata(musicMetadata)

        return@async musicMetadata
    }

    /**
     * Starts the music with refreshing UI.
     *
     * @param uri the music uri.
     */
    fun startMusicWithRefreshUi(uri: String) = launch {
        val refreshUiDeferred = startRefreshUI(uri)
        val startMusicJob =  launch {
            svcState.binder.prepare(uri)  // suspend function.

            nowPlayingItem!!.status = PlaylistItem.Status.PLAYING
            db.playlistDao.update(nowPlayingItem!!)

            svcState.binder.start()
        }

        val musicMetadata = refreshUiDeferred.await()
        startMusicJob.join()

        notificationUseCase.updateNowPlayingNotification(
                nowPlayingItem!!,
                musicMetadata.title ?: getString(R.string.unknown_music_title))

        val res = scrobbleUseCase.updateNowPlaying(musicMetadata)
        if (res != null && (!res.isSuccessful || res.isIgnored)) {
            Toast.makeText(ctx, "Last.fm status is not updated.", Toast.LENGTH_SHORT).show()
        }
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
                artist = "",
                duration = -1,
                trackNumber = -1,
                artwork = noArtworkImage,
                albumTitle = "",
                albumArtist = ""))

        viewModel.isPlaying.set(false)
    }

    /**
     * Returns the empty album artwork.
     */
    val noArtworkImage: Drawable
}

/**
 * An implementation of PlayerActivityPresenter.
 *
 * @param viewModel the view model.
 * @param dbxMetadataArray the array of Dropbox's node metadata.
 * @param nowPlayingItem the now playing item.
 * @param playerServiceIntent the player service intent.
 * @param startService the callback to start a service.
 * @param bindService the callback to bind a service.
 * @param unbindService the callback to unbind a service.
 * @param getString the callback to get string resources.
 * @param playButton the play button.
 * @param resources android's resource.
 * @param mediaMetadataRetriever the mediaMetadataRetriever.
 */
class PlayerActivityPresenterImpl(
        override val viewModel: PlayerActivityViewModel,
        private val dbxMetadataArray: Array<DbxNodeMetadata>?,
        override var nowPlayingItem: PlaylistItem?,
        private val playerServiceIntent: Intent,
        private val startService: (Intent) -> Unit,
        private val bindService: (Intent) -> Unit,
        override val unbindService: () -> Unit,
        override val getString: (Int) -> String,
        private val playButton: Button,
        private val resources: Resources,
        override val mediaMetadataRetriever: MediaMetadataRetriever) : PlayerActivityPresenter {

    /**
     * An android's Context.
     */
    @Inject
    override lateinit var ctx: Context

    /**
     * A DbxProxy.
     */
    @Inject
    lateinit var dbxProxy: DbxProxy

    /**
     * the database.
     */
    override lateinit var db: AppDatabase

    /**
     * the NotificationUseCase.
     */
    override lateinit var notificationUseCase: NotificationUseCase

    /**
     * the ScrobbleUseCase.
     */
    override lateinit var scrobbleUseCase: ScrobbleUseCase

    /**
     * the player service state.
     */
    override lateinit var svcState: PlayerServiceState

    /**
     * indicates whether the PlayerService is initialized.
     */
    override var isPlayerServiceInitialized: Boolean = false

    override fun onCreate(): Job {
        resetUI()

        playButton.setOnClickListener { v -> onPlayButtonClick() }

        return launch {
            // Start playing new music when launched by FileBrowseActivity.
            if (dbxMetadataArray != null) {
                db.playlistDao.deleteAll()
                dbxMetadataArray.forEachIndexed { index, dbxMetadata ->
                    val id = UUID.randomUUID().toString()
                    val order = index + 1
                    val playlistItem = PlaylistItem(id, order, dbxMetadata.path, PlaylistItem.Status.WAIT)
                    db.playlistDao.insert(playlistItem)

                    if (order == 1) {
                        nowPlayingItem = playlistItem
                    }
                }
            }
            else if (nowPlayingItem == null) {
                nowPlayingItem = db.playlistDao.getNowPlaying()
            }

            playerServiceIntent.putExtra("dbxMetadataArray", dbxMetadataArray)
            playerServiceIntent.putExtra("nowPlayingItem", nowPlayingItem)

            startService(playerServiceIntent)
            bindService(playerServiceIntent)
        }
    }

    /**
     * The callback of `onMusicFinished`.
     */
    override fun onMusicFinished() = launch {
        resetUI()

        nowPlayingItem!!.status = PlaylistItem.Status.PLAYED
        db.playlistDao.update(nowPlayingItem!!)

        val nextItem = db.playlistDao.getNext()
        if (nextItem != null) {
            nextItem.status = PlaylistItem.Status.PLAYING

            nowPlayingItem = nextItem
            db.playlistDao.update(nowPlayingItem!!)

            val musicUri = getMusicUri().await()
            startMusicWithRefreshUi(musicUri).join()
        }
    }

    /**
     * Returns the music metadata.
     */
    override fun getMusicMetadata(uri: String): Deferred<MusicMetadata> = async {
        mediaMetadataRetriever.setDataSource(uri, mapOf<String, String>())

        // title
        val title = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) ?: ""

        // artist
        val artist = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: ""

        // duration
        val duration = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION).toIntOrNull() ?: -1

        // track number
        val trackNumber = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER).toIntOrNull() ?: -1

        // artwork
        val artworkBytes: ByteArray? = mediaMetadataRetriever.embeddedPicture
        val artworkDrawable = if (artworkBytes == null) {
            noArtworkImage
        } else {
            ByteArrayInputStream(artworkBytes).use {
                Drawable.createFromStream(it, null)
            }
        }

        // album title
        val albumTitle = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM) ?: ""

        // album artist
        val albumArtist = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST) ?: ""

        return@async MusicMetadata(
                title = title,
                artist = artist,
                duration = duration,
                trackNumber = trackNumber,
                artwork = artworkDrawable,
                albumTitle = albumTitle,
                albumArtist = albumArtist)
    }

    /**
     * Returns a music uri.
     */
    override fun getMusicUri(): Deferred<String> {
        return dbxProxy.getTemporaryLink(nowPlayingItem!!.path)
    }

    /**
     * Returns the empty album artwork.
     */
    override val noArtworkImage: Drawable
        get() = resources.getDrawable(R.drawable.ic_no_image, null)
}