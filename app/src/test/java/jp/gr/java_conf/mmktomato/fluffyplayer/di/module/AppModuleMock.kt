package jp.gr.java_conf.mmktomato.fluffyplayer.di.module

import android.content.Context
import dagger.Module
import dagger.Provides
import org.robolectric.RuntimeEnvironment

@Module
class AppModuleMock() {
    @Provides
    fun provideContext(): Context {
        return RuntimeEnvironment.application
    }
}