package jp.gr.java_conf.mmktomato.fluffyplayer.usecase

import android.app.Notification
import android.content.Context
import jp.gr.java_conf.mmktomato.fluffyplayer.DUMMY_MUSIC_TITLE
import jp.gr.java_conf.mmktomato.fluffyplayer.db.model.PlaylistItem
import jp.gr.java_conf.mmktomato.fluffyplayer.di.component.DaggerNotificationUseCaseTestComponent
import jp.gr.java_conf.mmktomato.fluffyplayer.di.component.DependencyInjector
import jp.gr.java_conf.mmktomato.fluffyplayer.di.component.MockComponentInjector
import jp.gr.java_conf.mmktomato.fluffyplayer.di.module.AppModuleMock
import jp.gr.java_conf.mmktomato.fluffyplayer.di.module.PlaylistModuleMock
import jp.gr.java_conf.mmktomato.fluffyplayer.entity.MusicMetadata
import jp.gr.java_conf.mmktomato.fluffyplayer.player.PlayerService
import jp.gr.java_conf.mmktomato.fluffyplayer.prefs.AppPrefs
import org.junit.Assert.*
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
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

        useCase = DependencyInjector.injector.createNotificationUseCase(ctx)
    }

    @Test
    fun createNowPlayingNotification() {
        val notification = useCase.createNowPlayingNotification(nowPlayingItem, DUMMY_MUSIC_TITLE)

        assertEquals("Now playing", notification.extras["android.title"])
        assertEquals(DUMMY_MUSIC_TITLE, notification.extras["android.text"])
        assertEquals(Notification.FLAG_NO_CLEAR, notification.flags)
    }

    @Test
    fun updateNotification() {
        useCase.updateNowPlayingNotification(nowPlayingItem, DUMMY_MUSIC_TITLE)

        verify(useCase.notificationManager, times(1))
                .notify(eq(AppPrefs.NOW_PLAYING_NOTIFICATION_ID), any(Notification::class.java))
    }
}