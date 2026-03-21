package com.palabradeldia.data.prefs

import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Persists the user-chosen backup folder URI across sessions using DataStore.
 * Kept separate so SettingsViewModel does not depend on DataStore directly,
 * which would require an extra Hilt binding.
 */
@Singleton
class BackupPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val KEY = stringPreferencesKey("backup_folder_uri")

    suspend fun getFolder(): Uri? =
        dataStore.data.first()[KEY]?.let { Uri.parse(it) }

    suspend fun saveFolder(uri: Uri) {
        dataStore.edit { it[KEY] = uri.toString() }
    }
}
