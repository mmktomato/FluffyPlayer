package jp.gr.java_conf.mmktomato.fluffyplayer

import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.databinding.DataBindingUtil
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.os.IBinder
import android.widget.Button
import jp.gr.java_conf.mmktomato.fluffyplayer.databinding.ActivityPlayerBinding
import jp.gr.java_conf.mmktomato.fluffyplayer.dropbox.DbxNodeMetadata
import jp.gr.java_conf.mmktomato.fluffyplayer.player.PlayerService
import jp.gr.java_conf.mmktomato.fluffyplayer.ui.presenter.PlayerActivityPresenter
import jp.gr.java_conf.mmktomato.fluffyplayer.ui.presenter.PlayerActivityPresenterImpl
import jp.gr.java_conf.mmktomato.fluffyplayer.ui.viewmodel.PlayerActivityViewModel

class PlayerActivity : ActivityBase() {
    private lateinit var presenter: PlayerActivityPresenter

    /**
     * the connection to the service.
     */
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            presenter.initializePlayerServiceState(binder)
        }

        // Called when the connection with the service disconnects unexpectedly.
        override fun onServiceDisconnected(name: ComponentName?) {
            presenter.unbindPlayerServiceState()
        }
    }

    /**
     * Initializes the `presenter`.
     */
    private fun initializePresenter() {
        val viewModel = PlayerActivityViewModel()
        val binding = DataBindingUtil.setContentView<ActivityPlayerBinding>(this, R.layout.activity_player)
        binding.viewModel = viewModel

        val playerServiceIntent = Intent(this, PlayerService::class.java)

        presenter = PlayerActivityPresenterImpl(
                sharedPrefs = sharedPrefs,
                dbxProxy = dbxProxy,
                viewModel = viewModel,
                dbxMetadata = intent.getSerializableExtra("dbxMetadata") as DbxNodeMetadata,
                playerServiceIntent = playerServiceIntent,
                startService = { intent -> startService(intent) },
                bindService = { intent -> bindService(intent, connection, Context.BIND_AUTO_CREATE) },
                unbindService = { unbindService(connection) },
                getString = ::getString,
                notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager,
                playButton = findViewById<Button>(R.id.playButton),
                resources = resources,
                mediaMetadataRetriever = MediaMetadataRetriever())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        initializePresenter()
        presenter.onCreate()
    }

    override fun onDestroy() {
        super.onDestroy()

        presenter.onDestroy()
    }
}
