package jp.gr.java_conf.mmktomato.fluffyplayer.di.module

import android.arch.persistence.room.Room
import android.content.Context
import dagger.Module
import dagger.Provides
import jp.gr.java_conf.mmktomato.fluffyplayer.db.AppDatabase
import jp.gr.java_conf.mmktomato.fluffyplayer.di.scope.AppScope

@Module
class DatabaseModule {
    @AppScope
    @Provides
    fun provideDatabase(ctx: Context): AppDatabase {
        return Room.databaseBuilder(ctx, AppDatabase::class.java, "fluffy.db").build()
    }
}