package jp.gr.java_conf.mmktomato.fluffyplayer

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import jp.gr.java_conf.mmktomato.fluffyplayer.ui.presenter.FileBrowseActivityPresenter
import jp.gr.java_conf.mmktomato.fluffyplayer.ui.presenter.FileBrowseActivityPresenterImpl

class FileBrowseActivity : ActivityBase() {
    private lateinit var presenter: FileBrowseActivityPresenter

    /**
     * Initializes the `presenter`.
     */
    private fun initializePresenter() {
        presenter = FileBrowseActivityPresenterImpl(
                sharedPrefs = sharedPrefs,
                dbxProxy = dbxProxy,
                filesListView = findViewById<ListView>(R.id.filesListView),
                inflater = LayoutInflater.from(this),
                getDbxPath = { intent.getStringExtra("path") },
                startActivity = ::startActivity
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_browse)

        initializePresenter()

        presenter.onCreate()
    }

    override fun onResume() {
        super.onResume()

        presenter.onResume()
    }
}
