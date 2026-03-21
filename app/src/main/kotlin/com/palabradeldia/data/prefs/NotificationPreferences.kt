package com.palabradeldia.data.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Persists two notification-related flags:
 *  - whether the user has already answered the first-launch prompt
 *  - whether daily notifications are enabled
 */
@Singleton
class NotificationPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val KEY_PROMPTED = booleanPreferencesKey("notif_prompted")
    private val KEY_ENABLED  = booleanPreferencesKey("notif_enabled")

    val enabled: Flow<Boolean> = dataStore.data.map { it[KEY_ENABLED] ?: false }

    suspend fun hasBeenPrompted(): Boolean =
        dataStore.data.first()[KEY_PROMPTED] ?: false

    suspend fun setEnabled(value: Boolean) {
        dataStore.edit {
            it[KEY_ENABLED]  = value
            it[KEY_PROMPTED] = true
        }
    }

}
