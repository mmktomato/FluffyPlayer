package jp.gr.java_conf.mmktomato.fluffyplayer.entity

import android.graphics.drawable.Drawable

/**
 * a data class of music metadata.
 *
 * @param title a music title.
 * @param artist a music artist.
 * @param duration a music duration.
 * @param trackNumber a track number.
 * @param artwork an album artwork.
 * @param albumTitle an album title.
 * @param albumArtist an album artist.
 */
data class MusicMetadata(val title: String,
                         val artist: String,
                         val duration: Int,
                         val trackNumber: Int,
                         val artwork: Drawable,
                         val albumTitle: String,
                         val albumArtist: String)
