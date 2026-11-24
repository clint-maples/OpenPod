package com.openpod.app

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.openpod.app.ui.AppViewModel
import com.openpod.app.ui.OpenPodRoot
import com.openpod.app.ui.OpenPodTheme

class MainActivity : ComponentActivity() {
    private val notifyPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 33) {
            notifyPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        enableEdgeToEdge()
        setContent {
            val vm: AppViewModel = viewModel()
            val settings by vm.settings.collectAsState()
            OpenPodTheme(
                themeMode = settings.themeMode,
                darkBackground = settings.darkBackground,
                accentId = settings.accent
            ) {
                OpenPodRoot(vm)
            }
        }
    }
}
