package jp.gr.java_conf.mmktomato.fluffyplayer

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val listContents = listOf("Browse Dropbox files", "Settings")
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, listContents)

        val listView = findViewById<ListView>(R.id.mainListView)
        listView.adapter = adapter
        listView.setOnItemClickListener { parent, view, position, id ->
            when (position) {
                0 -> {
                    val intent = Intent(this, FileBrowseActivity::class.java)
                    startActivity(intent)
                }
            }
        }
    }
}
