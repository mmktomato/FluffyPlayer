package jp.gr.java_conf.mmktomato.fluffyplayer.ui.presenter

import android.content.Intent
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.media.MediaMetadataRetriever
import android.os.IBinder
import android.widget.Button
import jp.gr.java_conf.mmktomato.fluffyplayer.R
import jp.gr.java_conf.mmktomato.fluffyplayer.dropbox.DbxProxy
import jp.gr.java_conf.mmktomato.fluffyplayer.dropbox.MetadataDTO
import jp.gr.java_conf.mmktomato.fluffyplayer.player.PlayerService
import jp.gr.java_conf.mmktomato.fluffyplayer.player.PlayerServiceState
import jp.gr.java_conf.mmktomato.fluffyplayer.ui.viewmodel.PlayerActivityViewModel
import kotlinx.coroutines.experimental.CommonPool
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
     * a Dropbox's file or folder metadata.
     */
    val metadata: MetadataDTO

    /**
     * a callback to bind a service.
     */
    val unbindService: () -> Unit

    /**
     * A callback of the activity's `onCreate`.
     */
    fun onCreate()

    /**
     * A callback of the activity's `onDestroy`.
     */
    fun onDestroy() {
        if (svcState.isBound) {
            unbindService()
        }
        svcState.unbind()
    }

    /**
     * A callback of playButton's onClick.
     */
    fun onPlayButtonClick() {
        svcState.binder.togglePlaying()
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
    }

    /**
     * Unbinds the PlayerServiceState.
     */
    fun unbindPlayerServiceState() {
        svcState.unbind()
    }

    /**
     * Sets the music metadata.
     *
     * @param uri the music uri.
     */
    fun setMetadata(uri: String)

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
 * @param metadata the file or folder metadata.
 * @param playerServiceIntent the player service intent.
 * @param bindService the callback to bind a service.
 * @param unbindService the callback to unbind a service.
 * @param playButton the play button.
 * @param resources android's resource.
 */
internal class PlayerActivityPresenterImpl(
        private val dbxProxy: DbxProxy,
        override val viewModel: PlayerActivityViewModel,
        override val metadata: MetadataDTO,
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
     * A callback of the activity's `onCreate`.
     */
    override fun onCreate() {
        playButton.setOnClickListener { v -> onPlayButtonClick() }

        launch(CommonPool) {
            val temporaryLink = dbxProxy.getTemporaryLink(metadata.path).await()

            bindPlayerService(temporaryLink)
            setMetadata(temporaryLink)
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
     * Sets the music metadata.
     *
     * @param uri the music uri.
     */
    override fun setMetadata(uri: String) {
        val mmr = MediaMetadataRetriever()
        mmr.setDataSource(uri, mapOf<String, String>())
        val title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
        viewModel.title.set(title)

        val artworkBytes: ByteArray? = mmr.embeddedPicture
        val artworkDrawable = if (artworkBytes == null) {
            noArtworkImage
        } else {
            ByteArrayInputStream(artworkBytes).use {
                Drawable.createFromStream(it, null)
            }
        }
        viewModel.artwork.set(artworkDrawable)
    }

    /**
     * Returns the empty album artwork.
     */
    override val noArtworkImage: Drawable
        get() = resources.getDrawable(R.drawable.ic_no_image, null)
}