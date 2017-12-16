package jp.gr.java_conf.mmktomato.fluffyplayer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.databinding.DataBindingUtil
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.widget.Button
import jp.gr.java_conf.mmktomato.fluffyplayer.databinding.ActivityPlayerBinding
import jp.gr.java_conf.mmktomato.fluffyplayer.dropbox.DbxNodeMetadata
import jp.gr.java_conf.mmktomato.fluffyplayer.dropbox.DbxProxy
import jp.gr.java_conf.mmktomato.fluffyplayer.player.PlayerService
import jp.gr.java_conf.mmktomato.fluffyplayer.prefs.SharedPrefsHelperImpl
import jp.gr.java_conf.mmktomato.fluffyplayer.ui.presenter.PlayerActivityPresenter
import jp.gr.java_conf.mmktomato.fluffyplayer.ui.presenter.PlayerActivityPresenterImpl
import jp.gr.java_conf.mmktomato.fluffyplayer.ui.viewmodel.PlayerActivityViewModel

class PlayerActivity : AppCompatActivity() {
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
        val sharedPrefs = SharedPrefsHelperImpl(this)
        val dbxProxy = DbxProxy(sharedPrefs)

        val viewModel = PlayerActivityViewModel()
        val binding = DataBindingUtil.setContentView<ActivityPlayerBinding>(this, R.layout.activity_player)
        binding.viewModel = viewModel

        presenter = PlayerActivityPresenterImpl(
                dbxProxy = dbxProxy,
                viewModel = viewModel,
                dbxMetadata = intent.getSerializableExtra("dbxMetadata") as DbxNodeMetadata,
                playerServiceIntent =  Intent(this, PlayerService::class.java),
                bindService = { intent -> bindService(intent, connection, Context.BIND_AUTO_CREATE) },
                unbindService = { unbindService(connection) },
                playButton = findViewById<Button>(R.id.playButton),
                resources = resources
        )
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
