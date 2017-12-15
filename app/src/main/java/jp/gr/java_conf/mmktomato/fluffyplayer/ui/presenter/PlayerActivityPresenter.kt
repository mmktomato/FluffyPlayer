package jp.gr.java_conf.mmktomato.fluffyplayer.ui.presenter

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
import jp.gr.java_conf.mmktomato.fluffyplayer.player.PlayerServiceState
import jp.gr.java_conf.mmktomato.fluffyplayer.ui.viewmodel.PlayerActivityViewModel
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import java.io.ByteArrayInputStream

/**
 * A presenter of PlayerActivity.
 */
internal interface PlayerActivityPresenter {
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
    val metadata: DbxNodeMetadata

    /**
     * a musing streaming url.
     */
    var streamingUrl: String

    /**
     * indicates whether the PlayerService is initialized.
     */
    var isPlayerServiceInitialized: Boolean

    /**
     * a callback to bind a service.
     */
    val unbindService: () -> Unit

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
     * Binds the player service.
     *
     * @param uri the music uri.
     */
    fun bindPlayerService(uri: String)

    /**
     * Initializes the PlayerServiceState.
     */
    fun initializePlayerServiceState(binder: IBinder?) {
        svcState =  PlayerServiceState(binder as PlayerService.LocalBinder, true) { isPlaying ->
            viewModel.isPlaying.set(isPlaying)
        }

        val musicMetadataDeferred = async { getMusicMetadata(streamingUrl) }
        val preparePlayerJob = launch { svcState.binder.prepare() }

        launch {
            val musicMetadata = musicMetadataDeferred.await()
            preparePlayerJob.join()

            setMusicMetadata(musicMetadata)
            svcState.binder.start()

            isPlayerServiceInitialized = true
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
    fun getMusicMetadata(uri: String): MusicMetadata

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
     * Returns the empty album artwork.
     */
    val noArtworkImage: Drawable
}

/**
 * An implementation of PlayerActivityPresenter.
 *
 * @param dbxProxy the Dropbox API Proxy.
 * @param viewModel the view model.
 * @param metadata the Dropbox's node metadata.
 * @param playerServiceIntent the player service intent.
 * @param bindService the callback to bind a service.
 * @param unbindService the callback to unbind a service.
 * @param playButton the play button.
 * @param resources android's resource.
 */
internal class PlayerActivityPresenterImpl(
        private val dbxProxy: DbxProxy,
        override val viewModel: PlayerActivityViewModel,
        override val metadata: DbxNodeMetadata,
        private val playerServiceIntent: Intent,
        private val bindService: (Intent) -> Unit,
        override val unbindService: () -> Unit,
        private val playButton: Button,
        private val resources: Resources) : PlayerActivityPresenter {

    /**
     * the player service state.
     */
    override lateinit var svcState: PlayerServiceState

    /**
     * a music streaming Url.
     */
    override lateinit var streamingUrl: String

    /**
     * indicates whether the PlayerService is initialized.
     */
    override var isPlayerServiceInitialized: Boolean = false

    override fun onCreate() {
        viewModel.title.set("(Loading...)")

        playButton.setOnClickListener { v -> onPlayButtonClick() }

        launch(CommonPool) {
            streamingUrl = dbxProxy.getTemporaryLink(metadata.path).await()

            bindPlayerService(streamingUrl)
        }
    }

    /**
     * Binds the player service.
     *
     * @param uri the music uri.
     */
    override fun bindPlayerService(uri: String) {
        playerServiceIntent.putExtra("uri", uri)
        bindService(playerServiceIntent)
    }

    /**
     * Returns the music metadata.
     */
    override fun getMusicMetadata(uri: String): MusicMetadata {
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

        return MusicMetadata(title, artworkDrawable)
    }

    /**
     * Returns the empty album artwork.
     */
    override val noArtworkImage: Drawable
        get() = resources.getDrawable(R.drawable.ic_no_image, null)
}