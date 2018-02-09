package jp.gr.java_conf.mmktomato.fluffyplayer

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import jp.gr.java_conf.mmktomato.fluffyplayer.di.component.DependencyInjector
import jp.gr.java_conf.mmktomato.fluffyplayer.proxy.DbxProxy
import javax.inject.Inject

/**
 * A base class of Activity
 */
abstract class ActivityBase : AppCompatActivity() {
    @Inject
    lateinit var dbxProxy: DbxProxy

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DependencyInjector.injector.inject(this, application)
    }
}