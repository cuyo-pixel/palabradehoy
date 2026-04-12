package com.palabradeldia.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.palabradeldia.data.prefs.NotificationPreferences
import com.palabradeldia.notification.NotificationScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val prefs: NotificationPreferences,
    private val scheduler: NotificationScheduler
) : ViewModel() {

    // true = show the first-launch dialog asking for permission
    private val _showPrompt = MutableStateFlow(false)
    val showPrompt: StateFlow<Boolean> = _showPrompt.asStateFlow()

    val notificationsEnabled: StateFlow<Boolean> = prefs.enabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun checkFirstLaunch() {
        viewModelScope.launch {
            if (!prefs.hasBeenPrompted()) {
                _showPrompt.value = true
            }
        }
    }

    fun onPromptAccepted(context: Context) {
        viewModelScope.launch {
            prefs.setEnabled(true)
            scheduler.schedule(context)
            _showPrompt.value = false
        }
    }

    fun onPromptDeclined() {
        viewModelScope.launch {
            prefs.setEnabled(false)
            _showPrompt.value = false
        }
    }

    fun setEnabled(context: Context, enabled: Boolean) {
        viewModelScope.launch {
            prefs.setEnabled(enabled)
            if (enabled) scheduler.schedule(context) else scheduler.cancel(context)
        }
    }
}
