package jp.gr.java_conf.mmktomato.fluffyplayer.ui.viewmodel

import android.databinding.BaseObservable
import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.graphics.drawable.Drawable

/**
 * A view model of PlayerActivity.
 */
class PlayerActivityViewModel  : BaseObservable() {
    /**
     * indicates whether the music is playing
     */
    val isPlaying: ObservableBoolean = ObservableBoolean(false)

    /**
     * the music title.
     */
    val title: ObservableField<String> = ObservableField("(title)")

    /**
     * the album artwork.
     */
    val artwork: ObservableField<Drawable?> = ObservableField(null)
}