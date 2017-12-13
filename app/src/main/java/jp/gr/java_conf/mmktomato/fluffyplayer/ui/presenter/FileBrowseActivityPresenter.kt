package jp.gr.java_conf.mmktomato.fluffyplayer.ui.presenter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.widget.ListView
import com.dropbox.core.v2.files.ListFolderResult
import jp.gr.java_conf.mmktomato.fluffyplayer.FileBrowseActivity
import jp.gr.java_conf.mmktomato.fluffyplayer.PlayerActivity
import jp.gr.java_conf.mmktomato.fluffyplayer.R
import jp.gr.java_conf.mmktomato.fluffyplayer.dropbox.DbxProxy
import jp.gr.java_conf.mmktomato.fluffyplayer.dropbox.MetadataDTO
import jp.gr.java_conf.mmktomato.fluffyplayer.prefs.SharedPrefsHelper
import jp.gr.java_conf.mmktomato.fluffyplayer.ui.DbxFileAdapter
import jp.gr.java_conf.mmktomato.fluffyplayer.ui.DbxFileAdapterImpl
import jp.gr.java_conf.mmktomato.fluffyplayer.ui.ListViewOnScrollListener
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async

/**
 * A presenter of FileBrowseActivity
 */
internal interface FileBrowseActivityPresenter {
    /**
     * A DbxProxy.
     */
    val dbxProxy: DbxProxy

    /**
     * A callback that returns a Dropbox folder path.
     */
    val getDbxPath: () -> String?

    /**
     * A previous result of DbxProxy.listFolder.
     */
    var lastResult: ListFolderResult?

    /**
     * A filesListView's adapter.
     */
    var listViewAdapter: DbxFileAdapter

    fun onCreate()

    fun onResume()

    /**
     * A callback method of filesListView's OnScroll.
     */
    fun onFilesListViewScroll(): Deferred<Boolean> = async(UI) {
        // fetch
        val path = getDbxPath() ?: ""
        val res = dbxProxy.listFolder(path, lastResult).await()
        lastResult = res

        // add
        listViewAdapter.addItems(res.entries
                .map { MetadataDTO.createFrom(it) }
                .filter { !it.isFile || (it.isFile && isMusicFile(it.name)) })

        if (!res.hasMore) {
            removeProgressBar()
        }
        return@async res.hasMore
    }

    /**
     * Removes a progress bar.
     */
    fun removeProgressBar()

    /**
     * Returns whether `fileName` is a music file.
     *
     * @param fileName a file name.
     */
    private fun isMusicFile(fileName: String): Boolean {
        val extensions = listOf(".wav", ".m4a", ".mp3", ".flac", ".ogg")
        return extensions.any { fileName.toLowerCase().endsWith(it) }
    }
}

/**
 * An implementation of FileBrowseActivityPresenter
 *
 * @param sharedPrefs a SharedPrefsHelper.
 * @param dbxProxy a DbxProxy.
 * @param inflater a LayoutInflater.
 * @param filesListView a ListView to list files.
 * @param getDbxPath a callback that returns a Dropbox folder path.
 * @param startActivity a callback to start an activity.
 */
internal class FileBrowseActivityPresenterImpl(
        private val sharedPrefs: SharedPrefsHelper,
        override val dbxProxy: DbxProxy,
        private val inflater: LayoutInflater,
        private val filesListView: ListView,
        override val getDbxPath: () -> String?,
        private val startActivity: (Intent) -> Unit) : FileBrowseActivityPresenter {
    /**
     * A filesListView's adapter.
     */
    override lateinit var listViewAdapter: DbxFileAdapter

    /**
     * A filesListView's progress bar.
     */
    private lateinit var progressBar: View

    /**
     * A previous result of DbxProxy.listFolder.
     */
    override var lastResult: ListFolderResult? = null

    override fun onCreate() {
        initialize()
    }

    override fun onResume() {
        // do nothing.
    }

    /**
     * initializes views.
     */
    private fun initialize() {
        // listViewAdapter
        listViewAdapter = DbxFileAdapterImpl(inflater)
        filesListView.adapter = listViewAdapter as DbxFileAdapterImpl

        // progressBar
        progressBar = inflater.inflate(R.layout.listview_progress, filesListView, false)
        filesListView.addFooterView(progressBar)

        // on scroll
        filesListView.setOnScrollListener(ListViewOnScrollListener(UI, ::onFilesListViewScroll))

        // on item click
        filesListView.setOnItemClickListener { parent, view, position, id ->
            if (view.id != R.id.dbxFileListItem) {
                return@setOnItemClickListener
            }

            val metadata = listViewAdapter.getItem(position)

            if (metadata is MetadataDTO) {
                onFilesListViewItemClick(metadata)
            }
        }
    }

    /**
     * Removes a progress bar.
     */
    override fun removeProgressBar() {
        filesListView.removeFooterView(progressBar)
    }

    /**
     * Called when an item of filesListView is tapped.
     *
     * @param metadata the tapped metadata.
     */
    private fun onFilesListViewItemClick(metadata: MetadataDTO) {
        if (metadata.isFile) {
            val intent = Intent(sharedPrefs.context, PlayerActivity::class.java)
            intent.putExtra("metadata", metadata)
            startActivity(intent)
        }
        else {
            val intent = Intent(sharedPrefs.context, FileBrowseActivity::class.java)
            intent.putExtra("path", metadata.path)
            startActivity(intent)
        }
    }
}
