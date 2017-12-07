package jp.gr.java_conf.mmktomato.fluffyplayer.ui

import android.widget.AbsListView
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.launch
import kotlin.coroutines.experimental.CoroutineContext

/**
 * A listView onScroll listener.
 *
 * @param coroutineCtx the CoroutinContext of `onScrollBotton` callback.
 * @param onScrollBottom a callback function called when listView is scrolled to bottom. Returns whether there are more items.
 */
internal class ListViewOnScrollListener(
        private val coroutineCtx: CoroutineContext,
        private val onScrollBottom: () -> Deferred<Boolean>) : AbsListView.OnScrollListener {
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

