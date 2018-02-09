package jp.gr.java_conf.mmktomato.fluffyplayer.proxy

import android.content.Context
import de.umass.lastfm.Authenticator
import de.umass.lastfm.Caller
import de.umass.lastfm.Session
import de.umass.lastfm.Track
import de.umass.lastfm.cache.FileSystemCache
import de.umass.lastfm.scrobble.ScrobbleData
import de.umass.lastfm.scrobble.ScrobbleResult
import jp.gr.java_conf.mmktomato.fluffyplayer.BuildConfig
import java.io.File

/**
 * A proxy of Last.fm API.
 */
class LastFmProxy {
    /**
     * Initializes cache.
     *
     * @param ctx android's Context.
     */
    fun initializeGlobalCache(ctx: Context) {
        Caller.getInstance().cache = FileSystemCache(File(ctx.cacheDir, "lastFmCache"))
    }

    /**
     * Returns mobile session.
     *
     * @param username a Last.fm user name.
     * @param passwordDigest a MD5 digest of Last.fm password.
     * @return the authenticated session. Null if credential of Last.fm is invalid.
     */
    fun getMobileSession(username: String, passwordDigest: String): Session? {
        return Authenticator.getMobileSession(
                username,
                passwordDigest,
                BuildConfig.FLUFFY_PLAYER_LAST_FM_APP_KEY,
                BuildConfig.FLUFFY_PLAYER_LAST_FM_SECRET)
    }

    /**
     * Updates NowPlaying status of Last.fm.
     *
     * @param scrobbleData an instance of `ScrobbleData`.
     * @param session an authenticated session.
     * @return the scrobble result.
     */
    fun updateNowPlaying(scrobbleData: ScrobbleData, session: Session): ScrobbleResult {
        return Track.updateNowPlaying(scrobbleData, session)
    }

    /**
     * Scrobbles music to Last.fm.
     *
     * @param scrobbleData an instance of `ScrobbleData`.
     * @param session an authenticated session.
     * @return the scrobble result.
     */
    fun scrobble(scrobbleData: ScrobbleData, session: Session): ScrobbleResult {
        return Track.scrobble(scrobbleData, session)
    }
}
