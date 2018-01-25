package jp.gr.java_conf.mmktomato.fluffyplayer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import jp.gr.java_conf.mmktomato.fluffyplayer.ui.presenter.FileBrowseActivityPresenter
import jp.gr.java_conf.mmktomato.fluffyplayer.ui.presenter.FileBrowseActivityPresenterImpl
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking

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
                dbxPath = intent.getStringExtra("path") ?: "",
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
        return runBlocking(CommonPool) {
            return@runBlocking when (presenter.onOptionsItemSelected(item!!.itemId).await()) {
                true -> true
                false -> super.onOptionsItemSelected(item)
            }
        }
    }
}
