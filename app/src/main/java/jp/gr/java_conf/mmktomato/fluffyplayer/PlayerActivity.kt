package jp.gr.java_conf.mmktomato.fluffyplayer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.databinding.BaseObservable
import android.databinding.DataBindingUtil
import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.graphics.drawable.Drawable
import android.media.MediaMetadataRetriever
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.widget.Button
import jp.gr.java_conf.mmktomato.fluffyplayer.databinding.ActivityPlayerBinding
import jp.gr.java_conf.mmktomato.fluffyplayer.dropbox.DbxProxy
import jp.gr.java_conf.mmktomato.fluffyplayer.dropbox.MetadataDTO
import jp.gr.java_conf.mmktomato.fluffyplayer.player.PlayerService
import jp.gr.java_conf.mmktomato.fluffyplayer.prefs.SharedPrefsHelperImpl
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import java.io.ByteArrayInputStream

class PlayerActivity : AppCompatActivity() {
    /**
     * An view model class.
     *
     * @param isPlaying indicates whether the music is playing.
     * @param title the music title.
     * @param artwork the album artwork.
     */
    class ViewModel(val isPlaying: ObservableBoolean,
                    val title: ObservableField<String> = ObservableField("(title)"),
                    val artwork: ObservableField<Drawable?> = ObservableField(null)) : BaseObservable()

    /**
     * Holds a service binder state.
     *
     * @param svcBinder the player service binder.
     * @param isBound indicates whether this activity is bound to the service.
     * @param onPlayerStateChangedListener
     */
    private class BinderState(
            val svcBinder: PlayerService.LocalBinder,
            var isBound: Boolean,
            val onPlayerStateChangedListener: (Boolean) -> Unit) {

        /**
         * the list of listener indices.
         */
        private val listenerIndices = mutableListOf<Int>()

        init {
            val index = svcBinder.addOnPlayerStateChangedListener(onPlayerStateChangedListener)
            listenerIndices.add(index)
        }

        /**
         * Unbinds from service.
         */
        fun unbind() {
            isBound = false
            listenerIndices.forEach { index -> svcBinder.removeOnPlayerStateChangedListener(index) }
        }
    }

    /**
     * the view model.
     */
    private var viewModel = ViewModel(ObservableBoolean(false))

    /**
     * Holds the service binder state.
     */
    private lateinit var binderState: BinderState

    /**
     * the connection to the service.
     */
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            binderState = BinderState(binder as PlayerService.LocalBinder, true) { isPlaying ->
                viewModel.isPlaying.set(isPlaying)
            }
        }

        // Called when the connection with the service disconnects unexpectedly.
        override fun onServiceDisconnected(name: ComponentName?) {
            binderState.unbind()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        // TODO: Refactoring

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        val binding = DataBindingUtil.setContentView<ActivityPlayerBinding>(this, R.layout.activity_player)
        binding.viewModel = viewModel

        val playButton = findViewById<Button>(R.id.playButton)
        playButton.setOnClickListener { v ->
            binderState.svcBinder.togglePlaying()
        }

        val dbxProxy = DbxProxy(SharedPrefsHelperImpl(this))
        val metadata = intent.getSerializableExtra("metadata") as MetadataDTO
        val serviceIntent = Intent(this, PlayerService::class.java)

        launch(CommonPool) {
            val temporaryLink = dbxProxy.getTemporaryLink(metadata.path).await()
            serviceIntent.putExtra("uri", temporaryLink)
            bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE)

            val mmr = MediaMetadataRetriever()
            mmr.setDataSource(temporaryLink, mapOf<String, String>())
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
    }

    override fun onDestroy() {
        super.onDestroy()

        if (binderState.isBound) {
            unbindService(connection)
        }
        binderState.unbind()
    }

    /**
     * Returns the empty album artwork.
     */
    private val noArtworkImage: Drawable
        get() = resources.getDrawable(R.drawable.ic_no_image, null)
}
