package jp.gr.java_conf.mmktomato.fluffyplayer.di.component

import dagger.Component
import jp.gr.java_conf.mmktomato.fluffyplayer.db.AppDatabase
import jp.gr.java_conf.mmktomato.fluffyplayer.di.module.AppModuleMock
import jp.gr.java_conf.mmktomato.fluffyplayer.di.module.DatabaseModuleMock

@Component(modules = [AppModuleMock::class, DatabaseModuleMock::class])
interface DatabaseComponentMock {
    fun createAppDatabaseMock(): AppDatabase
}