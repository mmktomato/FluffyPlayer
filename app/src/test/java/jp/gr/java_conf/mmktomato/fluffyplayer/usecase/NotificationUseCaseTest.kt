package jp.gr.java_conf.mmktomato.fluffyplayer.usecase

import android.app.Notification
import android.content.Context
import jp.gr.java_conf.mmktomato.fluffyplayer.DUMMY_DBX_FILE_NAME
import jp.gr.java_conf.mmktomato.fluffyplayer.DUMMY_MUSIC_TITLE
import jp.gr.java_conf.mmktomato.fluffyplayer.di.component.DaggerNotificationUseCaseTestComponent
import jp.gr.java_conf.mmktomato.fluffyplayer.di.module.AppModuleMock
import jp.gr.java_conf.mmktomato.fluffyplayer.dropbox.DbxNodeMetadata
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import javax.inject.Inject

/**
 * Tests for NotificationUseCase.
 */
@RunWith(RobolectricTestRunner::class)
class NotificationUseCaseTest {
    @Inject
    lateinit var ctx: Context

    @Test
    fun createNowPlayingNotification_ReturnsCorrectOne() {
        DaggerNotificationUseCaseTestComponent.builder()
                .appModuleMock(AppModuleMock())
                .build()
                .inject(this)

        val fileMetadata = DbxNodeMetadata(
                isFile = true,
                name = DUMMY_DBX_FILE_NAME,
                path = "/path/to/" + DUMMY_DBX_FILE_NAME)
        val useCase = NotificationUseCase()
        val notification = useCase.createNowPlayingNotification(
                ctx = ctx,
                dbxMetadata = fileMetadata,
                musicTitle = DUMMY_MUSIC_TITLE)

        assertEquals("Now playing", notification.extras["android.title"])
        assertEquals(DUMMY_MUSIC_TITLE, notification.extras["android.text"])
        assertEquals(Notification.FLAG_NO_CLEAR, notification.flags)
    }
}