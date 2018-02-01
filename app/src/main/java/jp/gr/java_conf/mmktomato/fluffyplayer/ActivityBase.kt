package jp.gr.java_conf.mmktomato.fluffyplayer

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import jp.gr.java_conf.mmktomato.fluffyplayer.di.component.DaggerActivityBaseComponent
import jp.gr.java_conf.mmktomato.fluffyplayer.di.component.createComponentInjector
import jp.gr.java_conf.mmktomato.fluffyplayer.di.module.AppModule
import jp.gr.java_conf.mmktomato.fluffyplayer.di.module.DbxModule
import jp.gr.java_conf.mmktomato.fluffyplayer.di.module.SharedPrefsModule
import jp.gr.java_conf.mmktomato.fluffyplayer.dropbox.DbxProxy
import jp.gr.java_conf.mmktomato.fluffyplayer.prefs.SharedPrefsHelper
import javax.inject.Inject

/**
 * A base class of Activity
 */
abstract class ActivityBase : AppCompatActivity() {
    @Inject
    lateinit var dbxProxy: DbxProxy

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val injector = createComponentInjector()
        injector.inject(this, application)
    }
}