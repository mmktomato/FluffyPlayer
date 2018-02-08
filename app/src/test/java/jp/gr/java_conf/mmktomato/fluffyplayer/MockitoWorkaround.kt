package jp.gr.java_conf.mmktomato.fluffyplayer

import org.mockito.Mockito

/**
 * Workaround functions about Mockito.
 *
 * @see 'https://stackoverflow.com/questions/30305217/is-it-possible-to-use-mockito-in-kotlin'
 * @see 'https://github.com/googlesamples/android-architecture/blob/dev-todo-mvp-kotlin/todoapp/app/src/test/java/com/example/android/architecture/blueprints/todoapp/MockitoKotlinHelpers.kt'
 */
object MockitoWorkaround {
    fun <T> eq(obj: T): T = Mockito.eq<T>(obj)

    fun <T> any(): T = Mockito.any<T>()
}