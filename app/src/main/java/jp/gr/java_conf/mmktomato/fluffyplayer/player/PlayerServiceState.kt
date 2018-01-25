package jp.gr.java_conf.mmktomato.fluffyplayer.player

/**
 * Holds the PlayerService's State.
 *
 * @param binder the player service binder.
 * @param isBound indicates whether this activity is bound to the service.
 * @param onPlayerStateChangedListener the listener of `svcBinder#onPlayerStateChanged`.
 * @param onMusicFinished the listener of `svcBinder#onMusicFinished`.
 */
class PlayerServiceState(
        val binder: PlayerServiceBinder,
        var isBound: Boolean,
        private val onPlayerStateChangedListener: () -> Unit,
        private val onMusicFinished:() -> Unit) {
    /**
     * the list of listener indices.
     */
    private val listenerIndices = mutableListOf<Int>()

    init {
        listenerIndices.add(binder.addOnPlayerStateChangedListener(onPlayerStateChangedListener))
        listenerIndices.add(binder.addOnMusicFinishedListener(onMusicFinished))
    }

    /**
     * Unbinds from service.
     */
    fun unbind() {
        isBound = false
        listenerIndices.forEach { index ->
            binder.removeOnPlayerStateChangedListener(index)
            binder.removeOnMusicFinishedListener(index)
        }
    }
}