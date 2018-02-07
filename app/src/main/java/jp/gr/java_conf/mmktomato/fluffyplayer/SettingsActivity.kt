package jp.gr.java_conf.mmktomato.fluffyplayer

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.widget.Button
import jp.gr.java_conf.mmktomato.fluffyplayer.databinding.ActivitySettingsBinding
import jp.gr.java_conf.mmktomato.fluffyplayer.di.component.DependencyInjector
import jp.gr.java_conf.mmktomato.fluffyplayer.ui.presenter.SettingsActivityPresenter
import jp.gr.java_conf.mmktomato.fluffyplayer.ui.presenter.SettingsActivityPresenterImpl
import jp.gr.java_conf.mmktomato.fluffyplayer.ui.viewmodel.SettingsActivityViewModel

class SettingsActivity : ActivityBase() {
    private lateinit var presenter: SettingsActivityPresenter

    /**
     * Initializes the `presenter`.
     */
    private fun initializePresenter() {
        val viewModel = SettingsActivityViewModel()
        val binding = DataBindingUtil.setContentView<ActivitySettingsBinding>(this, R.layout.activity_settings)
        binding.viewModel = viewModel

        presenter = SettingsActivityPresenterImpl(
                viewModel = viewModel,
                connectDropboxButton = findViewById(R.id.connectDropboxButton))

        DependencyInjector.injector.inject(presenter as SettingsActivityPresenterImpl, this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        initializePresenter()

        presenter.onCreate()
    }

    override fun onResume() {
        super.onResume()

        presenter.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()

        presenter.onDestroy()
    }
}
