package jp.gr.java_conf.mmktomato.fluffyplayer

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast

class MainActivity : ActivityBase() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // TODO: !!!!! refactor this !!!!!

        val listContents = listOf("Browse Dropbox files", "Settings")
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, listContents)

        val listView = findViewById<ListView>(R.id.mainListView)
        listView.adapter = adapter
        listView.setOnItemClickListener { parent, view, position, id ->
            when (position) {
                // Browse Dropbox files
                0 -> {
                    if (dbxProxy.isAuthenticated) {
                        val intent = Intent(this, FileBrowseActivity::class.java)
                        startActivity(intent)
                    }
                    else {
                        Toast.makeText(this, "Please connect to Dropbox. See 'Settings > Dropbox'", Toast.LENGTH_LONG).show()
                    }
                }
                // Settings
                1 -> {
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                }
            }
        }
    }
}
