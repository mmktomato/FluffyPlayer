package jp.gr.java_conf.mmktomato.fluffyplayer.di.component

import dagger.Component
import jp.gr.java_conf.mmktomato.fluffyplayer.db.AppDatabase
import jp.gr.java_conf.mmktomato.fluffyplayer.di.module.AppModule
import jp.gr.java_conf.mmktomato.fluffyplayer.di.module.DatabaseModule
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class, DatabaseModule::class])
interface DatabaseComponent {
    fun createAppDatabase(): AppDatabase
}