package com.example.ui

import android.app.Application
import android.content.Context
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.ClipboardItemEntity
import com.example.data.PreferencesManager
import com.example.data.ShortcutEntity
import com.example.engine.SoundManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    val prefs = PreferencesManager(application)
    private val database = AppDatabase.getInstance(application)
    private val shortcutDao = database.shortcutDao()
    private val clipboardDao = database.clipboardDao()
    private val soundManager = SoundManager(application)

    val shortcuts: StateFlow<List<ShortcutEntity>> = shortcutDao.getAllFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val clipboardItems: StateFlow<List<ClipboardItemEntity>> = clipboardDao.getRecentItemsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isKeyboardEnabled = MutableStateFlow(false)
    val isKeyboardEnabled: StateFlow<Boolean> = _isKeyboardEnabled.asStateFlow()

    private val _isKeyboardSelected = MutableStateFlow(false)
    val isKeyboardSelected: StateFlow<Boolean> = _isKeyboardSelected.asStateFlow()

    init {
        checkKeyboardStatus()
        seedDefaultShortcuts()
    }

    fun checkKeyboardStatus() {
        val context = getApplication<Application>()
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        val packageName = context.packageName

        val enabledList = imm?.enabledInputMethodList ?: emptyList()
        val isEnabled = enabledList.any { it.packageName == packageName }
        _isKeyboardEnabled.value = isEnabled

        val defaultIme = Settings.Secure.getString(context.contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD) ?: ""
        _isKeyboardSelected.value = defaultIme.contains(packageName)
    }

    private fun seedDefaultShortcuts() {
        viewModelScope.launch {
            val list = shortcutDao.getAllList()
            if (list.isEmpty()) {
                shortcutDao.insert(
                    ShortcutEntity(
                        triggerText = "سلام",
                        expandedText = "السلام عليكم ورحمة الله وبركاته"
                    )
                )
                shortcutDao.insert(
                    ShortcutEntity(
                        triggerText = "ايميلي",
                        expandedText = "example@gmail.com"
                    )
                )
                shortcutDao.insert(
                    ShortcutEntity(
                        triggerText = "شكرا",
                        expandedText = "شكراً جزيلاً لك وجزاك الله خيراً!"
                    )
                )
                prefs.notifyShortcutsChanged()
            }
        }
    }

    fun addShortcut(trigger: String, expanded: String) {
        if (trigger.isBlank() || expanded.isBlank()) return
        viewModelScope.launch {
            shortcutDao.insert(
                ShortcutEntity(
                    triggerText = trigger.trim(),
                    expandedText = expanded.trim()
                )
            )
            com.example.engine.ShortcutsManager.getInstance(getApplication()).saveShortcut(trigger, expanded)
            prefs.notifyShortcutsChanged()
        }
    }

    fun updateShortcut(shortcut: ShortcutEntity, newTrigger: String, newExpanded: String) {
        if (newTrigger.isBlank() || newExpanded.isBlank()) return
        viewModelScope.launch {
            shortcutDao.update(
                shortcut.copy(
                    triggerText = newTrigger.trim(),
                    expandedText = newExpanded.trim()
                )
            )
            val sm = com.example.engine.ShortcutsManager.getInstance(getApplication())
            if (shortcut.triggerText != newTrigger.trim()) {
                sm.deleteShortcut(shortcut.triggerText)
            }
            sm.saveShortcut(newTrigger, newExpanded)
            prefs.notifyShortcutsChanged()
        }
    }

    fun deleteShortcut(shortcut: ShortcutEntity) {
        viewModelScope.launch {
            shortcutDao.delete(shortcut)
            com.example.engine.ShortcutsManager.getInstance(getApplication()).deleteShortcut(shortcut.triggerText)
            prefs.notifyShortcutsChanged()
        }
    }

    fun clearClipboard() {
        viewModelScope.launch {
            clipboardDao.clearAll()
        }
    }

    fun previewKeySound(profile: String, event: SoundManager.SoundEvent, volume: Int, customUri: String? = null) {
        soundManager.previewSound(profile, event, volume, customUri)
    }

    override fun onCleared() {
        soundManager.release()
        super.onCleared()
    }
}
