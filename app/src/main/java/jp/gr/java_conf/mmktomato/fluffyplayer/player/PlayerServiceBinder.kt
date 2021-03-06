package jp.gr.java_conf.mmktomato.fluffyplayer.player

import android.media.MediaPlayer
import android.os.Binder

/**
 * A binder to communicate PlayerService.
 *
 * @param player a media player.
 */
class PlayerServiceBinder(private val player: MediaPlayer) : Binder() {
    /**
     * the counter of listener
     */
    private var listenerCounter = 0

    /**
     * the map of listeners for onPlayerStateChanged.
     */
    private val onPlayerStateChangedListeners = mutableMapOf<Int, () -> Unit>()

    /**
     * the map of listeners for onMusicFinished.
     */
    private val onMusicFinishedListeners = mutableMapOf<Int, () -> Unit>()

    /**
     * Prepares dataSource.
     *
     * @param uri the music uri.
     */
    suspend fun prepare(uri: String) {
        player.setDataSource(uri)
        player.prepare()
    }

    /**
     * Starts playing the music.
     */
    fun start() {
        // ALAC is not supported ...
        player.start()
        notifyOnPlayerStateChanged()
    }

    /**
     * Pauses the music.
     */
    fun pause() {
        player.pause()
        notifyOnPlayerStateChanged()
    }

    /**
     * Resets the player.
     */
    fun reset() {
        player.stop()
        notifyOnPlayerStateChanged()

        player.reset()
        notifyOnMusicFinished()
    }

    /**
     * Toggles playing state.
     */
    fun togglePlaying() {
        if (isPlaying) {
            pause()
        } else {
            start()
        }
    }

    /**
     * Returns whether the player is playing a music.
     */
    val isPlaying
        get() = player.isPlaying

    /**
     * Adds listener for onPlayerStateChanged.
     *
     * @param listener the listener to add.
     */
    fun addOnPlayerStateChangedListener(listener: () -> Unit): Int {
        onPlayerStateChangedListeners.put(++listenerCounter, listener)
        return listenerCounter
    }

    /**
     * Adds listener for onMusicFinished.
     *
     * @param listener the listener to add.
     */
    fun addOnMusicFinishedListener(listener: () -> Unit): Int {
        onMusicFinishedListeners.put(++listenerCounter, listener)
        return listenerCounter
    }

    /**
     * Removes listener for onPlayerStateChanged.
     *
     * @param index the listener index.
     */
    fun removeOnPlayerStateChangedListener(index: Int) {
        onPlayerStateChangedListeners.remove(index)
    }

    /**
     * Removes listener for onMusicFinished.
     *
     * @param index the listener index.
     */
    fun removeOnMusicFinishedListener(index: Int) {
        onMusicFinishedListeners.remove(index)
    }

    /**
     * Notifies onPlayerStateChanged.
     */
    private fun notifyOnPlayerStateChanged() {
        onPlayerStateChangedListeners.values.forEach { it.invoke() }
    }

    /**
     * Notifies onMusicFinished.
     */
    private fun notifyOnMusicFinished() {
        onMusicFinishedListeners.values.forEach { it.invoke() }
    }
}

