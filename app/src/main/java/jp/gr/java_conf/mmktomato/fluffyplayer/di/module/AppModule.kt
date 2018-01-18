package jp.gr.java_conf.mmktomato.fluffyplayer.di.module

import android.content.Context
import dagger.Module
import dagger.Provides

@Module
class AppModule(private val ctx: Context) {
    @Provides
    fun provideContext(): Context {
        return ctx
    }
}
