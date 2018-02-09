package jp.gr.java_conf.mmktomato.fluffyplayer.usecase

import de.umass.lastfm.Session
import de.umass.lastfm.Track
import de.umass.lastfm.scrobble.ScrobbleData
import de.umass.lastfm.scrobble.ScrobbleResult
import jp.gr.java_conf.mmktomato.fluffyplayer.entity.MusicMetadata
import javax.inject.Inject

/**
 * Business logics for scrobbling.
 */
interface ScrobbleUseCase {
    /**
     * Whether this use case is valid.
     */
    val isValid: Boolean

    /**
     * Returns an instance of ScrobbleData.
     *
     * @param metadata the music metadata.
     */
    fun createScrobbleData(metadata: MusicMetadata): ScrobbleData {
        val timeStamp = System.currentTimeMillis() / 1000   // UTC

        return ScrobbleData(
                metadata.artist,
                metadata.title,
                timeStamp.toInt(),  // TODO: year 2038 problem ?
                metadata.duration,
                metadata.albumTitle,
                metadata.albumArtist,
                null,  // musicBrainzId
                metadata.trackNumber,
                null,  // streamId
                true)  // chosenByUser
    }

    /**
     * Updates NowPlaying status of Last.fm.
     *
     * @param metadata the music metadata.
     * @return the scrobble result.
     */
    fun updateNowPlaying(metadata: MusicMetadata): ScrobbleResult?

    /**
     * Scrobbles music to Last.fm.
     *
     * @param metadata the music metadata.
     * @return the scrobble result.
     */
    fun scrobble(metadata: MusicMetadata): ScrobbleResult?
}

/**
 * Implementation of ScrobbleUseCase.
 *
 * @param session a Last.fm's session.
 */
class ScrobbleUseCaseImpl(private val session: Session) : ScrobbleUseCase {
    /**
     * Override super.isValid.
     */
    override val isValid: Boolean = true

    /**
     * Override super.updateNowPlaying.
     */
    override fun updateNowPlaying(metadata: MusicMetadata): ScrobbleResult? {
        val scrobbleData = createScrobbleData(metadata)

        return Track.updateNowPlaying(scrobbleData, session)
    }

    /**
     * Override super.scrobble.
     */
    override fun scrobble(metadata: MusicMetadata): ScrobbleResult? {
        val scrobbleData = createScrobbleData(metadata)

        return Track.scrobble(scrobbleData, session)
    }
}

/**
 * Dummy implementation of ScrobbleUseCase.
 * This class is used when Last.fm scrobbling is not enabled.
 */
class InvalidScrobbleUseCaseImpl() : ScrobbleUseCase {
    override val isValid: Boolean = false

    override fun updateNowPlaying(metadata: MusicMetadata): ScrobbleResult? {
        return null
    }

    override fun scrobble(metadata: MusicMetadata): ScrobbleResult? {
        return null
    }
}