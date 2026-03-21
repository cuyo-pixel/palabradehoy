package com.palabradeldia.presentation.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.palabradeldia.data.prefs.BackupPreferences
import com.palabradeldia.domain.usecase.ExportFavouritesUseCase
import com.palabradeldia.domain.usecase.ImportFavouritesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

sealed interface ExportUiState {
    data object Idle    : ExportUiState
    data object Loading : ExportUiState
    data class  NeedFolder(val json: String, val fileName: String) : ExportUiState
    data object Done    : ExportUiState
    data object Error   : ExportUiState
}

sealed interface ImportUiState {
    data object Idle    : ImportUiState
    data object Loading : ImportUiState
    data class  Success(val count: Int) : ImportUiState
    data object Error   : ImportUiState
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val exportFavourites: ExportFavouritesUseCase,
    private val importFavourites: ImportFavouritesUseCase,
    private val backupPreferences: BackupPreferences
) : ViewModel() {

    private val _exportState = MutableStateFlow<ExportUiState>(ExportUiState.Idle)
    val exportState: StateFlow<ExportUiState> = _exportState

    private val _importState = MutableStateFlow<ImportUiState>(ImportUiState.Idle)
    val importState: StateFlow<ImportUiState> = _importState

    private val _backupFolderUri = MutableStateFlow<Uri?>(null)
    val backupFolderUri: StateFlow<Uri?> = _backupFolderUri

    init {
        viewModelScope.launch {
            _backupFolderUri.value = backupPreferences.getFolder()
        }
    }

    fun startExport(context: Context) {
        viewModelScope.launch {
            _exportState.value = ExportUiState.Loading
            val json     = exportFavourites()
            val fileName = buildFileName()
            val folder   = _backupFolderUri.value
            if (folder == null) {
                _exportState.value = ExportUiState.NeedFolder(json, fileName)
            } else {
                writeFile(context, folder, json, fileName)
            }
        }
    }

    fun onFolderPicked(context: Context, treeUri: Uri) {
        context.contentResolver.takePersistableUriPermission(
            treeUri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )
        _backupFolderUri.value = treeUri
        viewModelScope.launch { backupPreferences.saveFolder(treeUri) }

        val pending = _exportState.value
        if (pending is ExportUiState.NeedFolder) {
            writeFile(context, treeUri, pending.json, pending.fileName)
        }
    }

    private fun writeFile(context: Context, folderUri: Uri, json: String, fileName: String) {
        viewModelScope.launch {
            try {
                val docFile = androidx.documentfile.provider.DocumentFile
                    .fromTreeUri(context, folderUri)
                    ?.createFile("application/json", fileName)
                if (docFile == null) { _exportState.value = ExportUiState.Error; return@launch }
                context.contentResolver.openOutputStream(docFile.uri)?.use { out ->
                    out.write(json.toByteArray(Charsets.UTF_8))
                }
                _exportState.value = ExportUiState.Done
            } catch (e: Exception) {
                _exportState.value = ExportUiState.Error
            }
        }
    }

    fun importFavourites(context: Context, uri: Uri) {
        viewModelScope.launch {
            _importState.value = ImportUiState.Loading
            try {
                val json = context.contentResolver
                    .openInputStream(uri)?.bufferedReader()?.readText()
                    ?: run { _importState.value = ImportUiState.Error; return@launch }
                val count = importFavourites(json)
                _importState.value = if (count >= 0) ImportUiState.Success(count)
                                     else ImportUiState.Error
            } catch (e: Exception) {
                _importState.value = ImportUiState.Error
            }
        }
    }

    fun clearExportState() { _exportState.value = ExportUiState.Idle }
    fun clearImportState() { _importState.value = ImportUiState.Idle }

    private fun buildFileName(): String =
        "PalabraDeHoy-${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"))}.json"
}
