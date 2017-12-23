package jp.gr.java_conf.mmktomato.fluffyplayer.usecase

import android.app.Notification
import jp.gr.java_conf.mmktomato.fluffyplayer.dropbox.DbxNodeMetadata
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

/**
 * Tests for NotificationUseCase.
 */
@RunWith(RobolectricTestRunner::class)
class NotificationUseCaseTest {
    @Test
    fun createNowPlayingNotification_ReturnsCorrectOne() {
        val DUMMY_MUSIC_TITLE = "dummyTitle"
        val DUMMY_FILE_NAME = "dummyFileName.txt"
        val fileMetadata = DbxNodeMetadata(
                isFile = true,
                name = DUMMY_FILE_NAME,
                path = "/path/to/" + DUMMY_FILE_NAME)
        val useCase = NotificationUseCase()
        val notification = useCase.createNowPlayingNotification(
                ctx = RuntimeEnvironment.application,
                dbxMetadata = fileMetadata,
                musicTitle = DUMMY_MUSIC_TITLE)

        assertEquals("Now Playing", notification.extras["android.title"])
        assertEquals(DUMMY_MUSIC_TITLE, notification.extras["android.text"])
        assertEquals(Notification.FLAG_NO_CLEAR, notification.flags)
    }
}