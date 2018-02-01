package jp.gr.java_conf.mmktomato.fluffyplayer.di.component

import dagger.Component
import jp.gr.java_conf.mmktomato.fluffyplayer.ActivityBase
import jp.gr.java_conf.mmktomato.fluffyplayer.di.module.AppModuleMock
import jp.gr.java_conf.mmktomato.fluffyplayer.di.module.DbxModuleMock
import jp.gr.java_conf.mmktomato.fluffyplayer.di.module.SharedPrefsModuleMock

@Component(modules = [AppModuleMock::class, SharedPrefsModuleMock::class, DbxModuleMock::class])
interface ActivityBaseComponentMock {
    fun inject(activity: ActivityBase)
}
