package jp.gr.java_conf.mmktomato.fluffyplayer.player

/**
 * Holds the PlayerService's State.
 *
 * @param binder the player service binder.
 * @param isBound indicates whether this activity is bound to the service.
 * @param onPlayerStateChangedListener the listener of `svcBinder#onPlayerStateChanged`.
 */
internal class PlayerServiceState(
        val binder: PlayerService.LocalBinder,
        var isBound: Boolean,
        private val onPlayerStateChangedListener: () -> Unit) {
    /**
     * the list of listener indices.
     */
    private val listenerIndices = mutableListOf<Int>()

    init {
        val index = binder.addOnPlayerStateChangedListener(onPlayerStateChangedListener)
        listenerIndices.add(index)
    }

    /**
     * Unbinds from service.
     */
    fun unbind() {
        isBound = false
        listenerIndices.forEach { index -> binder.removeOnPlayerStateChangedListener(index) }
    }
}