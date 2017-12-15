package jp.gr.java_conf.mmktomato.fluffyplayer.entity

import android.graphics.drawable.Drawable

/**
 * a data class of music metadata.
 *
 * @param title a music title.
 * @param artwork an album artwork.
 */
data class MusicMetadata(val title: String?,
                         val artwork: Drawable)
