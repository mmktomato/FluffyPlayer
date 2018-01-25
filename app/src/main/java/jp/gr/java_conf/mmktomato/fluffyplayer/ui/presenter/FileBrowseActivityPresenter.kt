package jp.gr.java_conf.mmktomato.fluffyplayer.ui.presenter

import android.content.Intent
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.widget.ListView
import com.dropbox.core.v2.files.ListFolderResult
import com.dropbox.core.v2.files.Metadata
import jp.gr.java_conf.mmktomato.fluffyplayer.FileBrowseActivity
import jp.gr.java_conf.mmktomato.fluffyplayer.PlayerActivity
import jp.gr.java_conf.mmktomato.fluffyplayer.R
import jp.gr.java_conf.mmktomato.fluffyplayer.dropbox.DbxNodeMetadata
import jp.gr.java_conf.mmktomato.fluffyplayer.dropbox.DbxProxy
import jp.gr.java_conf.mmktomato.fluffyplayer.prefs.SharedPrefsHelper
import jp.gr.java_conf.mmktomato.fluffyplayer.ui.DbxFileAdapter
import jp.gr.java_conf.mmktomato.fluffyplayer.ui.DbxFileAdapterImpl
import jp.gr.java_conf.mmktomato.fluffyplayer.ui.ListViewOnScrollListener
import kotlinx.coroutines.experimental.CommonPool
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
     * A Dropbox folder path.
     */
    val dbxPath: String

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

        val res = dbxProxy.listFolder(dbxPath, lastResult).await()
        lastResult = res

        // add
        listViewAdapter.addItems(toFolderOrMusicFile(res.entries))

        if (!res.hasMore) {
            removeProgressBar()
        }
        return@async res.hasMore
    }

    /**
     * Returns all contents of `dbxPath`.
     */
    fun listFolderAll(): Deferred<List<DbxNodeMetadata>> = async(CommonPool) {
        var res: ListFolderResult? = null
        val ret = mutableListOf<DbxNodeMetadata>()

        do {
            res = dbxProxy.listFolder(dbxPath, res).await()
            ret.addAll(toFolderOrMusicFile(res.entries))
        } while (res!!.hasMore)

        return@async ret
    }

    /**
     * Converts `List<Metadata>` to `List<DbxNodeMetadata>`.
     * Removes files which are not music files.
     *
     * @param metadataList the list of Dropbox's Metadata.
     * @return the list of DbxNodeMetadata.
     */
    fun toFolderOrMusicFile(metadataList: List<Metadata>): List<DbxNodeMetadata> {
        return metadataList
                .map { DbxNodeMetadata.createFrom(it) }
                .filter { !it.isFile || (it.isFile && isMusicFile(it.name)) }
                .sortedBy { it.name }
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

    /**
     * Handles `onOptionsItemSelected`.
     *
     * @param menuItemId the menu item's id.
     * @return Returns true if `menuItemId` is handled.
     */
    fun onOptionsItemSelected(menuItemId: Int): Deferred<Boolean>
}

/**
 * An implementation of FileBrowseActivityPresenter
 *
 * @param sharedPrefs a SharedPrefsHelper.
 * @param dbxProxy a DbxProxy.
 * @param inflater a LayoutInflater.
 * @param filesListView a ListView to list files.
 * @param toolBar a ToolBar.
 * @param dbxPath a Dropbox folder path.
 * @param startActivity a callback to start an activity.
 * @param setSupportActionBar a callback to set action bar.
 */
internal class FileBrowseActivityPresenterImpl(
        private val sharedPrefs: SharedPrefsHelper,
        override val dbxProxy: DbxProxy,
        private val inflater: LayoutInflater,
        private val filesListView: ListView,
        private val toolBar: Toolbar,
        override val dbxPath: String,
        private val startActivity: (Intent) -> Unit,
        private val setSupportActionBar: (Toolbar) -> Unit) : FileBrowseActivityPresenter {
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
            // TODO: extract to method to make the test easy.

            if (view.id != R.id.dbxFileListItem) {
                return@setOnItemClickListener
            }

            val dbxMetadata = listViewAdapter.getItem(position)

            if (dbxMetadata is DbxNodeMetadata) {
                when (dbxMetadata.isFile) {
                    true -> playback(listOf(dbxMetadata))
                    false -> browseFolder(dbxMetadata)
                }
            }
        }

        // toolBar
        toolBar.setTitle(R.string.folder)
        setSupportActionBar(toolBar)
    }

    /**
     * Removes a progress bar.
     */
    override fun removeProgressBar() {
        filesListView.removeFooterView(progressBar)
    }

    /**
     * Starts `PlayerActivity` to playback musics.
     *
     * @param dbxFileMetadataList the list of file metadata to playback.
     */
    private fun playback(dbxFileMetadataList: List<DbxNodeMetadata>) {
        val intent = Intent(sharedPrefs.context, PlayerActivity::class.java)
        intent.putExtra("dbxMetadataArray", dbxFileMetadataList.toTypedArray())
        startActivity(intent)
    }

    /**
     * Starts this activity to browse folder.
     *
     * @param dbxFolderMetadata the folder metadata to browse.
     */
    private fun browseFolder(dbxFolderMetadata: DbxNodeMetadata) {
        if (dbxFolderMetadata.isFile) {
            return
        }

        val intent = Intent(sharedPrefs.context, FileBrowseActivity::class.java)
        intent.putExtra("path", dbxFolderMetadata.path)
        startActivity(intent)
    }

    /**
     * Handles `onOptionsItemSelected`.
     *
     * @param menuItemId the menu item's id.
     * @return Returns true if `menuItemId` is handled.
     */
    override fun onOptionsItemSelected(menuItemId: Int): Deferred<Boolean> = async(CommonPool) {
        if (menuItemId == R.id.playback_folder_item) {
            val allContents = listFolderAll().await().filter { it.isFile }

            if (0 < allContents.count()) {
                playback(allContents)
            }
            return@async true
        }
        return@async false
    }
}
