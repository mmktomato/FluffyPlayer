package jp.gr.java_conf.mmktomato.fluffyplayer.di.component

import dagger.Component
import jp.gr.java_conf.mmktomato.fluffyplayer.di.module.AppModule
import jp.gr.java_conf.mmktomato.fluffyplayer.di.module.ScrobbleModule
import jp.gr.java_conf.mmktomato.fluffyplayer.di.module.SharedPrefsModule
import jp.gr.java_conf.mmktomato.fluffyplayer.usecase.ScrobbleUseCase
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class, SharedPrefsModule::class, ScrobbleModule::class])
interface ScrobbleComponent {
    fun createScrobbleUseCase(): ScrobbleUseCase
}