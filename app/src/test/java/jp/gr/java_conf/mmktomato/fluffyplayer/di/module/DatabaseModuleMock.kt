package jp.gr.java_conf.mmktomato.fluffyplayer.di.module

import android.arch.persistence.room.Room
import android.content.Context
import dagger.Module
import dagger.Provides
import jp.gr.java_conf.mmktomato.fluffyplayer.db.AppDatabase

@Module
class DatabaseModuleMock {
    @Provides
    fun provideAppDatabase(ctx: Context): AppDatabase {
        return Room.databaseBuilder(ctx, AppDatabase::class.java, "fluffy.db")
                .allowMainThreadQueries()  // for unit test
                .build()
    }
}