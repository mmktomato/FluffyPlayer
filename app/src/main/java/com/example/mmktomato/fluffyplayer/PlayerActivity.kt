package com.example.mmktomato.fluffyplayer

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class PlayerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        val path = intent.getStringExtra("path") ?: ""
        val textView = findViewById<TextView>(R.id.textView)
        textView.text = path
    }
}
