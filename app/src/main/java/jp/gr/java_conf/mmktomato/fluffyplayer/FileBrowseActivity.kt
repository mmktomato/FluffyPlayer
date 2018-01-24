package jp.gr.java_conf.mmktomato.fluffyplayer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
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
                filesListView = findViewById(R.id.filesListView),
                toolBar = findViewById(R.id.toolbar),
                inflater = LayoutInflater.from(this),
                getDbxPath = { intent.getStringExtra("path") },
                startActivity = ::startActivity,
                setSupportActionBar = ::setSupportActionBar)
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_folder_browse, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        // TODO: move to presenter.
        return super.onOptionsItemSelected(item)
    }
}
