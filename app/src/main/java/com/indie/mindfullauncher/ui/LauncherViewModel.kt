package com.indie.mindfullauncher.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.indie.mindfullauncher.data.AppRepository
import com.indie.mindfullauncher.data.PreferencesRepository
import com.indie.mindfullauncher.domain.CommandBarEngine
import com.indie.mindfullauncher.domain.CommandResult
import com.indie.mindfullauncher.model.AppInfo
import com.indie.mindfullauncher.model.ContextProfile
import com.indie.mindfullauncher.model.QuickNote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class PendingFriction(
    val app: AppInfo,
    val minutesToday: Int,
    val hasUsagePermission: Boolean,
    val secondsRemaining: Int = 5
)

data class LauncherUiState(
    val activeProfile: ContextProfile = ContextProfile.WORK,
    val autoContextEnabled: Boolean = true,
    val installedApps: List<AppInfo> = emptyList(),
    val pinnedByProfile: Map<ContextProfile, Set<String>> = emptyMap(),
    val frictionPackages: Set<String> = emptySet(),
    val notes: List<QuickNote> = emptyList(),
    val commandQuery: String = "",
    val commandResult: CommandResult = CommandResult.Empty,
    val pendingFriction: PendingFriction? = null,
    val hasUsagePermission: Boolean = false,
    val isDefaultLauncher: Boolean = false,
    val isSetupBannerDismissed: Boolean = false
) {
    val visibleContextApps: List<AppInfo>
        get() {
            if (activeProfile == ContextProfile.ALL) return installedApps.take(12)
            val pinned = pinnedByProfile[activeProfile].orEmpty()
            return installedApps.filter { it.packageName in pinned }
        }
}

class LauncherViewModel(application: Application) : AndroidViewModel(application) {

    private val appRepo = AppRepository(application)
    private val prefsRepo = PreferencesRepository(application)

    private val _uiState = MutableStateFlow(LauncherUiState())
    val uiState: StateFlow<LauncherUiState> = _uiState.asStateFlow()

    private var frictionTimerJob: Job? = null

    init {
        refreshAll()
    }

    fun refreshAll() {
        viewModelScope.launch {
            val apps = withContext(Dispatchers.IO) { appRepo.loadInstalledApps() }
            val autoEnabled = prefsRepo.isAutoContextEnabled()
            val profile = if (autoEnabled) prefsRepo.getCurrentTimeProfile() else _uiState.value.activeProfile
            val pinnedMap = ContextProfile.entries
                .filter { it != ContextProfile.ALL }
                .associateWith { prefsRepo.getPinnedPackages(it, apps) }
            val friction = prefsRepo.getFrictionPackages(apps)
            val notes = prefsRepo.getNotes()
            val hasUsage = appRepo.hasUsageStatsPermission()
            val isDefault = appRepo.isDefaultLauncher()
            val bannerDismissed = prefsRepo.isSetupBannerDismissed()

            _uiState.update { state ->
                state.copy(
                    activeProfile = profile,
                    autoContextEnabled = autoEnabled,
                    installedApps = apps,
                    pinnedByProfile = pinnedMap,
                    frictionPackages = friction,
                    notes = notes,
                    hasUsagePermission = hasUsage,
                    isDefaultLauncher = isDefault,
                    isSetupBannerDismissed = bannerDismissed,
                    commandResult = CommandBarEngine.evaluate(state.commandQuery, apps)
                )
            }
        }
    }

    fun refreshTimeContextIfAuto() {
        val state = _uiState.value
        val hasUsage = appRepo.hasUsageStatsPermission()
        val isDefault = appRepo.isDefaultLauncher()
        val nextProfile = if (state.autoContextEnabled) prefsRepo.getCurrentTimeProfile() else state.activeProfile
        _uiState.update {
            it.copy(
                activeProfile = nextProfile,
                hasUsagePermission = hasUsage,
                isDefaultLauncher = isDefault
            )
        }
    }

    fun dismissSetupBanner() {
        prefsRepo.dismissSetupBanner()
        _uiState.update { it.copy(isSetupBannerDismissed = true) }
    }

    fun selectProfile(profile: ContextProfile) {
        _uiState.update { it.copy(activeProfile = profile) }
    }

    fun toggleAutoContext(enabled: Boolean) {
        prefsRepo.setAutoContextEnabled(enabled)
        val nextProfile = if (enabled) prefsRepo.getCurrentTimeProfile() else _uiState.value.activeProfile
        _uiState.update { it.copy(autoContextEnabled = enabled, activeProfile = nextProfile) }
    }

    fun onCommandQueryChanged(query: String) {
        val apps = _uiState.value.installedApps
        _uiState.update {
            it.copy(
                commandQuery = query,
                commandResult = CommandBarEngine.evaluate(query, apps)
            )
        }
    }

    fun submitCommand() {
        when (val res = _uiState.value.commandResult) {
            is CommandResult.NoteAction -> {
                if (res.noteText.isNotEmpty()) {
                    val updated = prefsRepo.addNote(res.noteText)
                    _uiState.update {
                        it.copy(
                            notes = updated,
                            commandQuery = "",
                            commandResult = CommandResult.Empty
                        )
                    }
                }
            }
            is CommandResult.AppMatches -> {
                res.apps.firstOrNull()?.let { onAppClicked(it) }
            }
            else -> Unit
        }
    }

    fun toggleNoteDone(id: String) {
        _uiState.update { it.copy(notes = prefsRepo.toggleNoteDone(id)) }
    }

    fun deleteNote(id: String) {
        _uiState.update { it.copy(notes = prefsRepo.deleteNote(id)) }
    }

    fun onAppClicked(app: AppInfo) {
        if (app.packageName in _uiState.value.frictionPackages) {
            startFrictionFlow(app)
        } else {
            appRepo.launchApp(app.packageName)
            clearCommandIfActive()
        }
    }

    private fun startFrictionFlow(app: AppInfo) {
        frictionTimerJob?.cancel()
        val usage = appRepo.getTodayUsageSummary(app.packageName)
        _uiState.update {
            it.copy(
                pendingFriction = PendingFriction(
                    app = app,
                    minutesToday = usage.minutesToday,
                    hasUsagePermission = usage.hasPermission,
                    secondsRemaining = 5
                )
            )
        }
        frictionTimerJob = viewModelScope.launch {
            for (sec in 4 downTo 0) {
                delay(1000L)
                _uiState.update { state ->
                    val current = state.pendingFriction ?: return@update state
                    state.copy(pendingFriction = current.copy(secondsRemaining = sec))
                }
            }
        }
    }

    fun confirmFrictionLaunch() {
        val pending = _uiState.value.pendingFriction ?: return
        if (pending.secondsRemaining > 0) return
        frictionTimerJob?.cancel()
        _uiState.update { it.copy(pendingFriction = null) }
        appRepo.launchApp(pending.app.packageName)
        clearCommandIfActive()
    }

    fun dismissFriction() {
        frictionTimerJob?.cancel()
        _uiState.update { it.copy(pendingFriction = null) }
    }

    fun togglePin(profile: ContextProfile, packageName: String) {
        if (profile == ContextProfile.ALL) return
        val apps = _uiState.value.installedApps
        val updatedSet = prefsRepo.togglePinnedPackage(profile, packageName, apps)
        _uiState.update { state ->
            state.copy(pinnedByProfile = state.pinnedByProfile + (profile to updatedSet))
        }
    }

    fun toggleFrictionApp(packageName: String) {
        val apps = _uiState.value.installedApps
        val updated = prefsRepo.toggleFrictionPackage(packageName, apps)
        _uiState.update { it.copy(frictionPackages = updated) }
    }

    fun openUsageSettings() = appRepo.openUsageStatsSettings()

    fun openDefaultLauncherSettings() = appRepo.openDefaultLauncherSettings()

    private fun clearCommandIfActive() {
        if (_uiState.value.commandQuery.isNotEmpty()) {
            _uiState.update { it.copy(commandQuery = "", commandResult = CommandResult.Empty) }
        }
    }
}
