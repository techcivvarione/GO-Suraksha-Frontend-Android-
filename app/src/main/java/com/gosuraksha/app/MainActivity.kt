package com.gosuraksha.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.gosuraksha.app.data.LanguageDataStore
import com.gosuraksha.app.network.ApiClient
import com.gosuraksha.app.ui.theme.GOSurakshaTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ApiClient.init(applicationContext)

        // 🌍 Apply saved language
        lifecycleScope.launch {
            LanguageDataStore.getSelectedLanguage(this@MainActivity)
                .collectLatest { code ->
                    code?.let {
                        val locale = Locale(it)
                        Locale.setDefault(locale)
                        val config = resources.configuration
                        config.setLocale(locale)
                        resources.updateConfiguration(config, resources.displayMetrics)
                    }
                }
        }

        setContent {
            GOSurakshaTheme {
                AppRoot()
            }
        }
    }
}
