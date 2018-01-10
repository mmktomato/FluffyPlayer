package jp.gr.java_conf.mmktomato.fluffyplayer.player

/**
 * Holds the PlayerService's State.
 *
 * @param binder the player service binder.
 * @param isBound indicates whether this activity is bound to the service.
 * @param onPlayerStateChangedListener the listener of `svcBinder#onPlayerStateChanged`.
 * @param onMusicChanged the listener of `svcBinder#onMusicChanged`.
 */
internal class PlayerServiceState(
        val binder: PlayerServiceBinder,
        var isBound: Boolean,
        private val onPlayerStateChangedListener: () -> Unit,
        private val onMusicChanged:() -> Unit) {
    /**
     * the list of listener indices.
     */
    private val listenerIndices = mutableListOf<Int>()

    init {
        listenerIndices.add(binder.addOnPlayerStateChangedListener(onPlayerStateChangedListener))
        listenerIndices.add(binder.addOnMusicChangedListener(onMusicChanged))
    }

    /**
     * Unbinds from service.
     */
    fun unbind() {
        isBound = false
        listenerIndices.forEach { index ->
            binder.removeOnPlayerStateChangedListener(index)
            binder.removeOnMusicChangedListener(index)
        }
    }
}