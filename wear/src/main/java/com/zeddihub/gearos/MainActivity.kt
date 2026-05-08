package com.zeddihub.gearos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Wearable
import com.zeddihub.gearos.ui.GearOsRoot
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_GearOS)
        setContent { GearOsRoot() }

        // Advertise install + version přes Wearable.CapabilityClient.
        // Mobile companion (zeddihub_tools_mobile) si dotazuje
        // `gearos_installed` capability na connected nodes a podle toho
        // ví, že GearOS je nainstalovaný na spárovaných hodinkách.
        // Verzi exposeujeme jako capability suffix `gearos_v_<versionCode>`,
        // takže companion může spočítat, jestli je třeba update.
        lifecycleScope.launch {
            runCatching {
                val caps = Wearable.getCapabilityClient(this@MainActivity)
                caps.addLocalCapability(CAP_INSTALLED).await()
                caps.addLocalCapability(CAP_VERSION_PREFIX + BuildConfig.VERSION_CODE).await()
            }.onFailure { Timber.w(it, "addLocalCapability failed") }
        }
    }

    companion object {
        /** Mobile companion checks for this capability on paired nodes. */
        const val CAP_INSTALLED = "gearos_installed"
        /** Suffix is versionCode (int). Mobile compares with embedded versionCode. */
        const val CAP_VERSION_PREFIX = "gearos_v_"
    }
}
