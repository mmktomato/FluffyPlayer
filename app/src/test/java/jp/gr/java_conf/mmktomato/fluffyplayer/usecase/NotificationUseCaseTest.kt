package jp.gr.java_conf.mmktomato.fluffyplayer.usecase

import android.app.Notification
import android.content.Context
import jp.gr.java_conf.mmktomato.fluffyplayer.DUMMY_MUSIC_TITLE
import jp.gr.java_conf.mmktomato.fluffyplayer.db.model.PlaylistItem
import jp.gr.java_conf.mmktomato.fluffyplayer.di.component.DaggerNotificationUseCaseTestComponent
import jp.gr.java_conf.mmktomato.fluffyplayer.di.component.MockComponentInjector
import jp.gr.java_conf.mmktomato.fluffyplayer.di.module.AppModuleMock
import jp.gr.java_conf.mmktomato.fluffyplayer.di.module.PlaylistModuleMock
import org.junit.Assert.*
import org.junit.Before
import org.junit.BeforeClass
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

    @Inject
    lateinit var nowPlayingItem: PlaylistItem

    private lateinit var useCase: NotificationUseCase

    companion object {
        @BeforeClass
        @JvmStatic
        fun setUpClass() {
            MockComponentInjector.setTestMode()
        }
    }

    @Before
    fun setUp() {
        DaggerNotificationUseCaseTestComponent.builder()
                .appModuleMock(AppModuleMock())
                .playlistModuleMock(PlaylistModuleMock(PlaylistItem.Status.PLAYING))
                .build()
                .inject(this)

        useCase = NotificationUseCase()
    }

    @Test
    fun createNowPlayingNotification_ReturnsCorrectOne() {
        val notification = useCase.createNowPlayingNotification(
                ctx = ctx,
                nowPlayingItem = nowPlayingItem,
                musicTitle = DUMMY_MUSIC_TITLE)

        assertEquals("Now playing", notification.extras["android.title"])
        assertEquals(DUMMY_MUSIC_TITLE, notification.extras["android.text"])
        assertEquals(Notification.FLAG_NO_CLEAR, notification.flags)
    }
}