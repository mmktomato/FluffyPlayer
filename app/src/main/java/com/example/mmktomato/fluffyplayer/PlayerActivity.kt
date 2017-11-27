package com.example.mmktomato.fluffyplayer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.widget.TextView
import com.example.mmktomato.fluffyplayer.dropbox.DbxProxy
import com.example.mmktomato.fluffyplayer.dropbox.MetadataDTO
import com.example.mmktomato.fluffyplayer.player.PlayerService
import kotlinx.coroutines.experimental.launch

class PlayerActivity : AppCompatActivity() {
    /**
     * the player service.
     */
    private lateinit var service: PlayerService

    /**
     * indicates whether this activity is bound to the service.
     */
    private var isBound = false

    /**
     * the connection to the service.
     */
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            service = (binder as PlayerService.LocalBinder).service
            isBound = true
        }

        // Called when the connection with the service disconnects unexpectedly.
        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        val dbxProxy = DbxProxy.create(this)

        val metadata = intent.getSerializableExtra("metadata") as MetadataDTO
        val textView = findViewById<TextView>(R.id.textView)
        textView.text = metadata.toString()

        val serviceIntent = Intent(this, PlayerService::class.java)
        launch {
            val temporaryLink = dbxProxy.getTemporaryLink(metadata.path).await()
            serviceIntent.putExtra("uri", temporaryLink)
            bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        if (isBound) {
            unbindService(connection)
            isBound = false
        }
    }
}
