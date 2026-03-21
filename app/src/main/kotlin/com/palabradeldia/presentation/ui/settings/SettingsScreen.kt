package com.palabradeldia.presentation.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.palabradeldia.R
import com.palabradeldia.domain.model.ThemeMode
import com.palabradeldia.presentation.viewmodel.ExportUiState
import com.palabradeldia.presentation.viewmodel.ImportUiState
import com.palabradeldia.presentation.viewmodel.NotificationViewModel
import com.palabradeldia.presentation.viewmodel.SettingsViewModel
import com.palabradeldia.presentation.viewmodel.ThemeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    themeViewModel: ThemeViewModel           = hiltViewModel(),
    settingsViewModel: SettingsViewModel     = hiltViewModel(),
    notificationViewModel: NotificationViewModel = hiltViewModel()
) {
    val currentTheme   by themeViewModel.themeMode.collectAsState()
    val exportState    by settingsViewModel.exportState.collectAsState()
    val importState    by settingsViewModel.importState.collectAsState()
    val backupFolder   by settingsViewModel.backupFolderUri.collectAsState()
    val notifsEnabled  by notificationViewModel.notificationsEnabled.collectAsState()
    val context        = LocalContext.current
    val snackbar       = remember { SnackbarHostState() }

    val folderLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        uri?.let { settingsViewModel.onFolderPicked(context, it) }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { settingsViewModel.importFavourites(context, it) }
    }

    val exportSuccessMsg = stringResource(R.string.export_success)
    val exportErrorMsg   = stringResource(R.string.export_error)
    val importErrorMsg   = stringResource(R.string.import_error)
    val importSuccessFmt = stringResource(R.string.import_success)

    LaunchedEffect(exportState) {
        when (exportState) {
            is ExportUiState.NeedFolder -> folderLauncher.launch(null)
            is ExportUiState.Done  -> { snackbar.showSnackbar(exportSuccessMsg); settingsViewModel.clearExportState() }
            is ExportUiState.Error -> { snackbar.showSnackbar(exportErrorMsg);   settingsViewModel.clearExportState() }
            else -> Unit
        }
    }

    LaunchedEffect(importState) {
        when (val s = importState) {
            is ImportUiState.Success -> {
                snackbar.showSnackbar(importSuccessFmt.format(s.count))
                settingsViewModel.clearImportState()
            }
            is ImportUiState.Error -> { snackbar.showSnackbar(importErrorMsg); settingsViewModel.clearImportState() }
            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.nav_back))
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Appearance
            SectionLabel(stringResource(R.string.settings_appearance))
            ThemeSelector(selected = currentTheme, onSelect = themeViewModel::onThemeSelected)

            HorizontalDivider(Modifier.padding(vertical = 4.dp))

            // Notifications
            SectionLabel(stringResource(R.string.settings_notifications))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.settings_notif_daily),
                        style = MaterialTheme.typography.bodyMedium)
                    Text(stringResource(R.string.settings_notif_time),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = notifsEnabled,
                    onCheckedChange = { notificationViewModel.setEnabled(context, it) })
            }

            HorizontalDivider(Modifier.padding(vertical = 4.dp))

            // Backup
            SectionLabel(stringResource(R.string.settings_backup))
            if (backupFolder != null) {
                val label = backupFolder!!.lastPathSegment?.substringAfterLast(':') ?: ""
                InfoRow(stringResource(R.string.settings_folder_current), label)
            }
            Button(onClick = { settingsViewModel.startExport(context) },
                modifier = Modifier.fillMaxWidth(),
                enabled = exportState !is ExportUiState.Loading) {
                Text(stringResource(R.string.settings_export))
            }
            OutlinedButton(onClick = { importLauncher.launch(arrayOf("application/json", "text/plain")) },
                modifier = Modifier.fillMaxWidth(),
                enabled = importState !is ImportUiState.Loading) {
                Text(stringResource(R.string.settings_import))
            }
            TextButton(onClick = { folderLauncher.launch(backupFolder) },
                modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(
                    if (backupFolder == null) R.string.settings_folder_choose
                    else R.string.settings_folder_change))
            }

            HorizontalDivider(Modifier.padding(vertical = 4.dp))

            // Info
            SectionLabel(stringResource(R.string.settings_info))
            InfoRow(stringResource(R.string.settings_version), "1.0.0")
            InfoRow(stringResource(R.string.settings_dictionary),
                stringResource(R.string.settings_dictionary_value))
            InfoRow(stringResource(R.string.settings_offline),
                stringResource(R.string.settings_offline_value),
                MaterialTheme.colorScheme.primary)

            HorizontalDivider(Modifier.padding(vertical = 4.dp))

            AppFooter()
        }
    }
}

@Composable
private fun AppFooter() {
    val context = LocalContext.current
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(stringResource(R.string.app_by) + " Cuyo Pixel",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)

        Text(
            text = "github.com/cuyo-pixel",
            style = MaterialTheme.typography.bodySmall.copy(textDecoration = TextDecoration.Underline),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable {
                context.startActivity(Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://github.com/cuyo-pixel")))
            }
        )

        Spacer(Modifier.height(4.dp))
        OutlinedButton(onClick = {
            context.startActivity(Intent(Intent.ACTION_VIEW,
                Uri.parse("https://ko-fi.com/cuyopixel")))
        }) {
            Text(stringResource(R.string.kofi_button))
        }
        Spacer(Modifier.height(2.dp))
        Text(stringResource(R.string.license),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Row(modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = valueColor)
    }
}

// Theme selector

private data class ThemeOption(val mode: ThemeMode, val labelRes: Int, val descRes: Int)

private val themeOptions = listOf(
    ThemeOption(ThemeMode.SYSTEM, R.string.theme_system, R.string.theme_system_desc),
    ThemeOption(ThemeMode.LIGHT,  R.string.theme_light,  R.string.theme_light_desc),
    ThemeOption(ThemeMode.DARK,   R.string.theme_dark,   R.string.theme_dark_desc),
    ThemeOption(ThemeMode.OLED,   R.string.theme_oled,   R.string.theme_oled_desc)
)

@Composable
private fun ThemeSelector(selected: ThemeMode, onSelect: (ThemeMode) -> Unit) {
    Card(shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(0.dp)) {
        Column(Modifier.padding(4.dp)) {
            themeOptions.forEachIndexed { idx, option ->
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(option.labelRes), style = MaterialTheme.typography.bodyLarge)
                        Text(stringResource(option.descRes), style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    RadioButton(selected == option.mode, onClick = { onSelect(option.mode) })
                }
                if (idx < themeOptions.lastIndex)
                    HorizontalDivider(Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    }
}

