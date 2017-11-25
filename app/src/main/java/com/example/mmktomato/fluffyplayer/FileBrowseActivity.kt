package com.example.mmktomato.fluffyplayer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.dropbox.core.v2.files.Metadata
import com.dropbox.core.v2.files.FileMetadata
import com.dropbox.core.v2.files.FolderMetadata
import com.dropbox.core.v2.files.ListFolderResult
import com.example.mmktomato.fluffyplayer.dropbox.DbxProxy
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import kotlin.coroutines.experimental.CoroutineContext

class FileBrowseActivity : AppCompatActivity() {
    /**
     * A listView onScroll listener.
     *
     * @param coroutineCtx the CoroutinContext of `onScrollBotton` callback.
     * @param onScrollBottom a callback function called when listView is scrolled to bottom. Returns whether there are more items.
     */
    private class OnScrollListener(private val coroutineCtx: CoroutineContext, private val onScrollBottom: () -> Deferred<Boolean>) : AbsListView.OnScrollListener {
        /**
         * The flag of preventing OnScroll callback.
         */
        private var preventOnScroll = false

        /**
         * The flag of whether all items are loaded.
         */
        private var isAllItemsLoaded = false

        override fun onScroll(view: AbsListView?, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
            if (preventOnScroll || isAllItemsLoaded) {
                return
            }
            preventOnScroll = true

            // If listView is scrolled to bottom, fetch next.
            if (totalItemCount == firstVisibleItem + visibleItemCount) {
                launch(coroutineCtx) {
                    try {
                        isAllItemsLoaded = !onScrollBottom().await()
                        preventOnScroll = false
                    } finally {
                        preventOnScroll = false
                    }
                }
            }
            else {
                preventOnScroll = false
            }
        }

        override fun onScrollStateChanged(p0: AbsListView?, p1: Int) {
        }
    }

    /**
     * A custom adapter of ListView to show Dropbox files.
     *
     * @param ctx android context.
     */
    private class DbxFileAdapter(private val ctx: Context) : BaseAdapter() {
        private val items = mutableListOf<Metadata>()
        private val inflater = LayoutInflater.from(ctx)

        override fun getCount(): Int = items.size

        override fun getItem(position: Int): Any = items.get(position)

        override fun getItemId(position: Int): Long = items.get(position).hashCode().toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val item = items.get(position)
            //val inflater = LayoutInflater.from(ctx)

            val ret = if (convertView != null && convertView.id == R.id.dbxFileListItem) {
                convertView
            }
            else {
                inflater.inflate(R.layout.listitem_dbx_file, parent, false)
            }

            val fileNameTextView = ret.findViewById<TextView>(R.id.fileNameTextView)
            fileNameTextView.text = item.name

            return ret
        }

        /**
         * Adds items to this adapter.
         *
         * @param list items to add.
         */
        internal fun addItems(list: List<Metadata>) {
            items.addAll(list)
            this.notifyDataSetChanged()
        }
    }

    /**
     * Whether this activity is initialized.
     */
    private var isInitialized = false

    /**
     * A DbxProxy.
     */
    private lateinit var dbxProxy: DbxProxy

    /**
     * Previous result of DbxProxy.listFolderAsync.
     */
    private var lastResult: ListFolderResult? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_browse)

        if (DbxProxy.isAuthenticated(this)) {
            initialize()
        }
        DbxProxy.auth(this)
    }

    override fun onResume() {
        super.onResume()

        if (DbxProxy.isAuthenticated(this) && !isInitialized) {
            initialize()
        }
    }

    private fun initialize() {
        dbxProxy = DbxProxy.create(this)
        val filesListView = findViewById<ListView>(R.id.filesListView)

        // listViewAdapter
        val listViewAdapter = DbxFileAdapter(this)
        filesListView.adapter = listViewAdapter

        // progressBar
        val inflater = LayoutInflater.from(this)
        val progressBar = inflater.inflate(R.layout.listview_progress, filesListView, false)
        filesListView.addFooterView(progressBar)


        // on scroll
        val path = intent.getStringExtra("path") ?: ""
        val coroutinCtx = UI
        val onScrollListener = OnScrollListener(coroutinCtx) {
            async(coroutinCtx) {
                // fetch
                val res = dbxProxy.listFolder(path, lastResult).await()
                lastResult = res

                // add
                listViewAdapter.addItems(res.entries)

                if (!res.hasMore) {
                    filesListView.removeFooterView(progressBar)
                }
                return@async res.hasMore
            }
        }
        filesListView.setOnScrollListener(onScrollListener)

        // on item click
        filesListView.setOnItemClickListener { parent, view, position, id ->
            val metadata = lastResult?.entries?.get(position)

            if (metadata != null) {
                this.onListViewItemClick(metadata)
            }
        }

        isInitialized = true
    }

    /**
     * Called when an item of filesListView is tapped.
     *
     * @param metadata the tapped metadata.
     */
    private fun onListViewItemClick(metadata: Metadata) {
        when (metadata) {
            is FileMetadata -> {
                val intent = Intent(this, PlayerActivity::class.java)
                intent.putExtra("fileName", metadata.name)
                this.startActivity(intent)
            }
            is FolderMetadata -> {
                val intent = Intent(this, FileBrowseActivity::class.java)
                intent.putExtra("path", metadata.pathLower)
                this.startActivity(intent)
            }
        }
    }
}
