package jp.gr.java_conf.mmktomato.fluffyplayer.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import jp.gr.java_conf.mmktomato.fluffyplayer.R
import jp.gr.java_conf.mmktomato.fluffyplayer.dropbox.MetadataDTO

/**
 * A custom adapter of ListView to show Dropbox files.
 */
internal interface DbxFileAdapter {
    /**
     * listView items.
     */
    val items: MutableList<MetadataDTO>

    /**
     * Adds items to this adapter.
     *
     * @param list items to add.
     */
    fun addItems(list: List<MetadataDTO>) {
        items.addAll(list)
        notifyItemsChanged()
    }

    /**
     * Returns the item of the `position`.
     */
    fun getItem(position: Int): Any

    /**
     * Notifies that the items are changed.
     */
    fun notifyItemsChanged()
}

/**
 * An implementation of DbxFileAdapter.
 *
 * @param inflater a LayoutInflater.
 */
internal class DbxFileAdapterImpl(private val inflater: LayoutInflater) : BaseAdapter(), DbxFileAdapter {
    /**
     * listView items.
     */
    override val items = mutableListOf<MetadataDTO>()

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

        val folderImageView = ret.findViewById<ImageView>(R.id.folderImageView)
        folderImageView.visibility = if (item.isFile) View.INVISIBLE else View.VISIBLE

        return ret
    }

    /**
     * Notifies thar the items are changed.
     */
    override fun notifyItemsChanged() {
        notifyDataSetChanged()
    }
}

