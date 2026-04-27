package com.cuyo_pixel.palabra_de_hoy.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cuyo_pixel.palabra_de_hoy.R
import com.cuyo_pixel.palabra_de_hoy.presentation.ui.navigation.PalabraNavHost
import com.cuyo_pixel.palabra_de_hoy.presentation.ui.theme.PalabraTheme
import com.cuyo_pixel.palabra_de_hoy.presentation.viewmodel.NotificationViewModel
import com.cuyo_pixel.palabra_de_hoy.presentation.viewmodel.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeViewModel: ThemeViewModel        = viewModel()
            val notifViewModel: NotificationViewModel = viewModel()
            val themeMode  by themeViewModel.themeMode.collectAsState()
            val showPrompt by notifViewModel.showPrompt.collectAsState()
            val context    = LocalContext.current

            val permissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { granted ->
                if (granted) notifViewModel.onPromptAccepted(context)
                else         notifViewModel.onPromptDeclined()
            }

            LaunchedEffect(Unit) { notifViewModel.checkFirstLaunch() }

            PalabraTheme(themeMode = themeMode) {
                PalabraNavHost()

                if (showPrompt) {
                    AlertDialog(
                        onDismissRequest = { notifViewModel.onPromptDeclined() },
                        title   = { Text(stringResource(R.string.notif_prompt_title)) },
                        text    = { Text(stringResource(R.string.notif_prompt_body)) },
                        confirmButton = {
                            TextButton(onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    val perm = Manifest.permission.POST_NOTIFICATIONS
                                    if (ContextCompat.checkSelfPermission(context, perm)
                                        == PackageManager.PERMISSION_GRANTED) {
                                        notifViewModel.onPromptAccepted(context)
                                    } else {
                                        permissionLauncher.launch(perm)
                                    }
                                } else {
                                    notifViewModel.onPromptAccepted(context)
                                }
                            }) { Text(stringResource(R.string.notif_prompt_accept)) }
                        },
                        dismissButton = {
                            TextButton(onClick = { notifViewModel.onPromptDeclined() }) {
                                Text(stringResource(R.string.notif_prompt_decline))
                            }
                        }
                    )
                }
            }
        }
    }
}
