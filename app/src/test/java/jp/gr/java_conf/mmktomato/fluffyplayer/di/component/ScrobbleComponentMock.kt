package jp.gr.java_conf.mmktomato.fluffyplayer.di.component

import dagger.Component
import jp.gr.java_conf.mmktomato.fluffyplayer.di.module.ScrobbleModuleMock
import jp.gr.java_conf.mmktomato.fluffyplayer.usecase.ScrobbleUseCase

@Component(modules = [ScrobbleModuleMock::class])
interface ScrobbleComponentMock {
    fun createScrobbleUseCaseMock(): ScrobbleUseCase
}