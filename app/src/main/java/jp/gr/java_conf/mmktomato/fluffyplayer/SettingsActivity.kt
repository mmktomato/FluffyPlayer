package jp.gr.java_conf.mmktomato.fluffyplayer

import android.databinding.DataBindingUtil
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import jp.gr.java_conf.mmktomato.fluffyplayer.databinding.ActivitySettingsBinding
import jp.gr.java_conf.mmktomato.fluffyplayer.dropbox.DbxProxy
import jp.gr.java_conf.mmktomato.fluffyplayer.prefs.SharedPrefsHelperImpl
import jp.gr.java_conf.mmktomato.fluffyplayer.ui.presenter.SettingsActivityPresenter
import jp.gr.java_conf.mmktomato.fluffyplayer.ui.presenter.SettingsActivityPresenterImpl
import jp.gr.java_conf.mmktomato.fluffyplayer.ui.viewmodel.SettingsActivityViewModel

class SettingsActivity : AppCompatActivity() {
    private lateinit var presenter: SettingsActivityPresenter

    /**
     * Initializes the `presenter`.
     */
    private fun initializePresenter() {
        val sharedPrefs = SharedPrefsHelperImpl(this)
        val dbxProxy = DbxProxy(sharedPrefs)

        val viewModel = SettingsActivityViewModel()
        val binding = DataBindingUtil.setContentView<ActivitySettingsBinding>(this, R.layout.activity_settings)
        binding.viewModel = viewModel

        presenter = SettingsActivityPresenterImpl(
                sharedPrefs = sharedPrefs,
                dbxProxy = dbxProxy,
                viewModel = viewModel,
                connectDropboxButton = findViewById<Button>(R.id.connectDropboxButton)
        )
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
}