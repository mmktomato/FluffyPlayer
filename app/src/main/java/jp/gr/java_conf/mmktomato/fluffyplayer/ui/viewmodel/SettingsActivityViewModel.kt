package jp.gr.java_conf.mmktomato.fluffyplayer.ui.viewmodel

import android.databinding.BaseObservable
import android.databinding.ObservableField

/**
 * A view model of SettingsActivity.
 */
class SettingsActivityViewModel : BaseObservable() {
    /**
     * The text of `connectDropboxButton`.
     */
    val connectDropboxButtonText = ObservableField<String>("connect")

    /**
     * the text of `dropboxAuthStatusTextView'.
     */
    val dropboxAuthStatusText = ObservableField<String>("(not connected)")

    /**
     * the text of `lastFmUserNameText`.
     */
    val lastFmUserNameText = ObservableField<String>("")

    /**
     * the text of `lastFmPasswordText`.
     */
    val lastFmPasswordText = ObservableField<String>("")
}