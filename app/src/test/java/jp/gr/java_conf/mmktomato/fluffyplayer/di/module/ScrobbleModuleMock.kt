package jp.gr.java_conf.mmktomato.fluffyplayer.di.module

import dagger.Module
import dagger.Provides
import jp.gr.java_conf.mmktomato.fluffyplayer.usecase.ScrobbleUseCase
import org.mockito.Mockito.*

@Module
class ScrobbleModuleMock {
    @Provides
    fun provideScrobbleUseCaseMock(): ScrobbleUseCase {
        return mock(ScrobbleUseCase::class.java)
    }
}