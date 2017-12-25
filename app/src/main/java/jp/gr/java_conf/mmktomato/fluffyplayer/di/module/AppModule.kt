package jp.gr.java_conf.mmktomato.fluffyplayer.di.module

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides

@Module
class AppModule(private val application: Application) {
    @Provides
    fun provideContext(): Context {
        return application.applicationContext
    }
}
