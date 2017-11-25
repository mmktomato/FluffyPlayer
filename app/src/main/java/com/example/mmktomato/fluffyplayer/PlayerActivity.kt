package com.example.mmktomato.fluffyplayer

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.example.mmktomato.fluffyplayer.dropbox.MetadataDTO

class PlayerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        val metadata = intent.getSerializableExtra("metadata") as MetadataDTO
        val textView = findViewById<TextView>(R.id.textView)
        textView.text = metadata.toString()
    }
}
