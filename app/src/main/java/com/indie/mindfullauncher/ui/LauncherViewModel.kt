package com.indie.mindfullauncher.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.indie.mindfullauncher.data.AppRepository
import com.indie.mindfullauncher.data.IconPackHelper
import com.indie.mindfullauncher.data.NotificationRepository
import com.indie.mindfullauncher.data.PreferencesRepository
import com.indie.mindfullauncher.domain.CommandBarEngine
import com.indie.mindfullauncher.domain.CommandResult
import com.indie.mindfullauncher.model.AccentPreset
import com.indie.mindfullauncher.model.AppInfo
import com.indie.mindfullauncher.model.AppShortcutItem
import com.indie.mindfullauncher.model.CalendarEventPreview
import com.indie.mindfullauncher.model.ClockStyle
import com.indie.mindfullauncher.model.ContactMatch
import com.indie.mindfullauncher.model.ContextProfile
import com.indie.mindfullauncher.model.FontPreset
import com.indie.mindfullauncher.model.IconPackInfo
import com.indie.mindfullauncher.model.IconShapePreset
import com.indie.mindfullauncher.model.IconSizePreset
import com.indie.mindfullauncher.model.LauncherFolder
import com.indie.mindfullauncher.model.MediaPlaybackState
import com.indie.mindfullauncher.model.NotificationPreview
import com.indie.mindfullauncher.model.PostSessionNudge
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
    val opensToday: Int = 0,
    val resistedToday: Int = 0,
    val hasUsagePermission: Boolean,
    val secondsRemaining: Int = 5,
    val selectedIntention: String? = null
)

data class LauncherUiState(
    val activeProfile: ContextProfile = ContextProfile.WORK,
    val autoContextEnabled: Boolean = true,
    val installedApps: List<AppInfo> = emptyList(),
    val pinnedByProfile: Map<ContextProfile, Set<String>> = emptyMap(),
    val frictionPackages: Set<String> = emptySet(),
    val hiddenPackages: Set<String> = emptySet(),
    val iconSizePreset: IconSizePreset = IconSizePreset.MEDIUM,
    val iconShapePreset: IconShapePreset = IconShapePreset.DEFAULT,
    val hideStatusBar: Boolean = false,
    val favoriteOrder: List<String> = emptyList(),
    val pinnedWidgetIds: List<Int> = emptyList(),
    val clockStyle: ClockStyle = ClockStyle.CLASSIC,
    val accentPreset: AccentPreset = AccentPreset.NIAGARA_BLUE,
    val hideFavoriteLabels: Boolean = false,
    val showBatteryOnHome: Boolean = true,
    val showAlphabetWave: Boolean = true,
    val showWallpaperBackground: Boolean = true,
    val showCalendarEvents: Boolean = true,
    val showNotificationPreviews: Boolean = true,
    val showMediaPlayer: Boolean = true,
    val smartSortEnabled: Boolean = false,
    val showMostUsedApps: Boolean = true,
    val monochromeIcons: Boolean = false,
    val fontPreset: FontPreset = FontPreset.NIAGARA,
    val showContactSearch: Boolean = true,
    val showMusicOnHeadphones: Boolean = true,
    val twoColumnLargeScreen: Boolean = true,
    val isHeadphonesConnected: Boolean = false,
    val customAppNames: Map<String, String> = emptyMap(),
    val appPopupWidgets: Map<String, Int> = emptyMap(),
    val contactMatches: List<ContactMatch> = emptyList(),
    val upcomingCalendarEvents: List<CalendarEventPreview> = emptyList(),
    val hasContactsPermission: Boolean = false,
    val selectedIconPack: String? = null,
    val availableIconPacks: List<IconPackInfo> = emptyList(),
    val niagaraButtonPackage: String? = null,
    val pinnedWidgetId: Int = -1,
    val summaryPackages: Set<String> = emptySet(),
    val weeklyUsageMillis: Map<String, Long> = emptyMap(),
    val folders: List<LauncherFolder> = emptyList(),
    val nextCalendarEvent: CalendarEventPreview? = null,
    val notificationPreviews: Map<String, NotificationPreview> = emptyMap(),
    val mediaPlayback: MediaPlaybackState? = null,
    val hasCalendarPermission: Boolean = false,
    val hasNotificationPermission: Boolean = false,
    val notes: List<QuickNote> = emptyList(),
    val commandQuery: String = "",
    val commandResult: CommandResult = CommandResult.Empty,
    val pendingFriction: PendingFriction? = null,
    val postSessionNudge: PostSessionNudge? = null,
    val frictionResistedToday: Int = 0,
    val hasUsagePermission: Boolean = false,
    val isDefaultLauncher: Boolean = false,
    val isSetupBannerDismissed: Boolean = false,
    val isTipsBannerDismissed: Boolean = false
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
    private var lastFrictionLaunchPkg: String? = null
    private var lastFrictionLaunchAtMillis: Long = 0L

    init {
        refreshAll()
        viewModelScope.launch {
            NotificationRepository.previews.collect { previews ->
                _uiState.update { it.copy(notificationPreviews = previews) }
            }
        }
        viewModelScope.launch {
            NotificationRepository.mediaState.collect { media ->
                _uiState.update { it.copy(mediaPlayback = media) }
            }
        }
    }

    fun refreshAll() {
        viewModelScope.launch {
            val rawApps = withContext(Dispatchers.IO) { appRepo.loadInstalledApps() }
            val customNames = prefsRepo.getCustomAppNames()
            val apps = rawApps.map { app ->
                val custom = customNames[app.packageName]
                if (custom != null) app.copy(label = custom) else app
            }.sortedBy { it.label.lowercase() }
            val autoEnabled = prefsRepo.isAutoContextEnabled()
            val profile = if (autoEnabled) prefsRepo.getCurrentTimeProfile() else _uiState.value.activeProfile
            val pinnedMap = ContextProfile.entries
                .filter { it != ContextProfile.ALL }
                .associateWith { prefsRepo.getPinnedPackages(it, apps) }
            val friction = prefsRepo.getFrictionPackages(apps)
            val hidden = prefsRepo.getHiddenPackages()
            val iconSize = prefsRepo.getIconSizePreset()
            val iconShape = prefsRepo.getIconShapePreset()
            val hideStatusBar = prefsRepo.isHideStatusBar()
            val favoriteOrder = prefsRepo.getFavoriteOrder(pinnedMap[ContextProfile.WORK].orEmpty())
            val widgetIds = prefsRepo.getPinnedWidgetIds()
            val clockStyle = prefsRepo.getClockStyle()
            val accentPreset = prefsRepo.getAccentPreset()
            val hideFavLabels = prefsRepo.isHideFavoriteLabels()
            val showBattery = prefsRepo.isShowBatteryOnHome()
            val showAlphabet = prefsRepo.isShowAlphabetWave()
            val showWallpaper = prefsRepo.isShowWallpaperBackground()
            val showCalendar = prefsRepo.isShowCalendarEvents()
            val showNotif = prefsRepo.isShowNotificationPreviews()
            val showMedia = prefsRepo.isShowMediaPlayer()
            val smartSort = prefsRepo.isSmartSortEnabled()
            val showMostUsed = prefsRepo.isShowMostUsedApps()
            val monochrome = prefsRepo.isMonochromeIcons()
            val fontPreset = prefsRepo.getFontPreset()
            val showContacts = prefsRepo.isShowContactSearch()
            val showMusicHeadphones = prefsRepo.isShowMusicOnHeadphones()
            val twoColumn = prefsRepo.isTwoColumnLargeScreen()
            val headphonesConnected = appRepo.isExternalAudioConnected()
            val popupWidgets = prefsRepo.getAppPopupWidgets()
            val iconPack = prefsRepo.getIconPackPackage()
            val iconPacks = withContext(Dispatchers.IO) { IconPackHelper.listInstalledIconPacks(getApplication()) }
            val fabPkg = prefsRepo.getNiagaraButtonPackage()
            val widgetId = prefsRepo.getPinnedWidgetId()
            val summaryPkgs = prefsRepo.getSummaryPackages()
            val folders = prefsRepo.getFolders()
            val notes = prefsRepo.getNotes()
            val hasUsage = appRepo.hasUsageStatsPermission()
            val usageMap = if (hasUsage) withContext(Dispatchers.IO) { appRepo.loadWeeklyUsageMillis() } else emptyMap()
            val hasCalendar = appRepo.hasCalendarPermission()
            val hasContacts = appRepo.hasContactsPermission()
            val hasNotif = appRepo.hasNotificationListenerPermission()
            val upcomingEvents = if (showCalendar && hasCalendar) {
                withContext(Dispatchers.IO) { appRepo.loadUpcomingCalendarEvents() }
            } else {
                emptyList()
            }
            val nextEvent = upcomingEvents.firstOrNull()
            val isDefault = appRepo.isDefaultLauncher()
            val bannerDismissed = prefsRepo.isSetupBannerDismissed()
            val tipsDismissed = prefsRepo.isTipsBannerDismissed()
            val resistedToday = prefsRepo.getFrictionResistedToday()

            _uiState.update { state ->
                state.copy(
                    activeProfile = profile,
                    autoContextEnabled = autoEnabled,
                    installedApps = apps,
                    pinnedByProfile = pinnedMap,
                    frictionPackages = friction,
                    hiddenPackages = hidden,
                    iconSizePreset = iconSize,
                    iconShapePreset = iconShape,
                    hideStatusBar = hideStatusBar,
                    favoriteOrder = favoriteOrder,
                    pinnedWidgetIds = widgetIds,
                    clockStyle = clockStyle,
                    accentPreset = accentPreset,
                    hideFavoriteLabels = hideFavLabels,
                    showBatteryOnHome = showBattery,
                    showAlphabetWave = showAlphabet,
                    showWallpaperBackground = showWallpaper,
                    showCalendarEvents = showCalendar,
                    showNotificationPreviews = showNotif,
                    showMediaPlayer = showMedia,
                    smartSortEnabled = smartSort,
                    showMostUsedApps = showMostUsed,
                    monochromeIcons = monochrome,
                    fontPreset = fontPreset,
                    showContactSearch = showContacts,
                    showMusicOnHeadphones = showMusicHeadphones,
                    twoColumnLargeScreen = twoColumn,
                    isHeadphonesConnected = headphonesConnected,
                    customAppNames = customNames,
                    appPopupWidgets = popupWidgets,
                    upcomingCalendarEvents = upcomingEvents,
                    hasContactsPermission = hasContacts,
                    selectedIconPack = iconPack,
                    availableIconPacks = iconPacks,
                    niagaraButtonPackage = fabPkg,
                    pinnedWidgetId = widgetId,
                    summaryPackages = summaryPkgs,
                    weeklyUsageMillis = usageMap,
                    folders = folders,
                    nextCalendarEvent = nextEvent,
                    hasCalendarPermission = hasCalendar,
                    hasNotificationPermission = hasNotif,
                    notes = notes,
                    frictionResistedToday = resistedToday,
                    hasUsagePermission = hasUsage,
                    isDefaultLauncher = isDefault,
                    isSetupBannerDismissed = bannerDismissed,
                    isTipsBannerDismissed = tipsDismissed,
                    commandResult = CommandBarEngine.evaluate(state.commandQuery, apps)
                )
            }
        }
    }

    fun refreshTimeContextIfAuto() {
        val state = _uiState.value
        val hasUsage = appRepo.hasUsageStatsPermission()
        val hasCalendar = appRepo.hasCalendarPermission()
        val hasContacts = appRepo.hasContactsPermission()
        val hasNotif = appRepo.hasNotificationListenerPermission()
        val headphonesConnected = appRepo.isExternalAudioConnected()
        val isDefault = appRepo.isDefaultLauncher()
        val nextProfile = if (state.autoContextEnabled) prefsRepo.getCurrentTimeProfile() else state.activeProfile
        _uiState.update {
            it.copy(
                activeProfile = nextProfile,
                hasUsagePermission = hasUsage,
                hasCalendarPermission = hasCalendar,
                hasContactsPermission = hasContacts,
                hasNotificationPermission = hasNotif,
                isHeadphonesConnected = headphonesConnected,
                isDefaultLauncher = isDefault
            )
        }
        if (state.showCalendarEvents && hasCalendar) {
            viewModelScope.launch {
                val events = withContext(Dispatchers.IO) { appRepo.loadUpcomingCalendarEvents() }
                _uiState.update {
                    it.copy(
                        upcomingCalendarEvents = events,
                        nextCalendarEvent = events.firstOrNull()
                    )
                }
            }
        }
    }

    fun dismissSetupBanner() {
        prefsRepo.dismissSetupBanner()
        _uiState.update { it.copy(isSetupBannerDismissed = true) }
    }

    fun dismissTipsBanner() {
        prefsRepo.dismissTipsBanner()
        _uiState.update { it.copy(isTipsBannerDismissed = true) }
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
        val state = _uiState.value
        val apps = state.installedApps
        val result = CommandBarEngine.evaluate(query, apps)
        _uiState.update {
            it.copy(
                commandQuery = query,
                commandResult = result,
                contactMatches = if (query.isBlank()) emptyList() else it.contactMatches
            )
        }
        if (state.showContactSearch && state.hasContactsPermission && result is CommandResult.AppMatches && query.trim().length >= 2) {
            viewModelScope.launch {
                val contacts = withContext(Dispatchers.IO) { appRepo.searchContacts(query) }
                if (_uiState.value.commandQuery == query) {
                    _uiState.update { it.copy(contactMatches = contacts) }
                }
            }
        } else {
            _uiState.update { it.copy(contactMatches = emptyList()) }
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
            launchAppDirect(app)
        }
    }

    fun launchAppDirect(app: AppInfo) {
        appRepo.launchApp(app.packageName)
        clearCommandIfActive()
    }

    private fun startFrictionFlow(app: AppInfo) {
        frictionTimerJob?.cancel()
        val usage = appRepo.getTodayUsageSummary(app.packageName)
        val opensToday = if (usage.hasPermission && usage.opensToday > 0) {
            usage.opensToday
        } else {
            prefsRepo.getLocalAppOpensToday(app.packageName) + 1
        }
        val resistedToday = prefsRepo.getFrictionResistedToday()
        _uiState.update {
            it.copy(
                pendingFriction = PendingFriction(
                    app = app,
                    minutesToday = usage.minutesToday,
                    opensToday = opensToday,
                    resistedToday = resistedToday,
                    hasUsagePermission = usage.hasPermission,
                    secondsRemaining = 5
                ),
                frictionResistedToday = resistedToday
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

    fun selectFrictionIntention(intention: String?) {
        _uiState.update { state ->
            val current = state.pendingFriction ?: return@update state
            val next = if (current.selectedIntention == intention) null else intention
            state.copy(pendingFriction = current.copy(selectedIntention = next))
        }
    }

    fun confirmFrictionLaunch() {
        val pending = _uiState.value.pendingFriction ?: return
        if (pending.secondsRemaining > 0) return
        frictionTimerJob?.cancel()
        lastFrictionLaunchPkg = pending.app.packageName
        lastFrictionLaunchAtMillis = System.currentTimeMillis()
        prefsRepo.incrementLocalAppOpensToday(pending.app.packageName)
        _uiState.update { it.copy(pendingFriction = null) }
        appRepo.launchApp(pending.app.packageName)
        clearCommandIfActive()
    }

    fun dismissFriction() {
        frictionTimerJob?.cancel()
        val resisted = prefsRepo.incrementFrictionResistedToday()
        _uiState.update {
            it.copy(
                pendingFriction = null,
                frictionResistedToday = resisted
            )
        }
    }

    fun onLauncherResumed() {
        refreshTimeContextIfAuto()
        val pkg = lastFrictionLaunchPkg ?: return
        val launchedAt = lastFrictionLaunchAtMillis
        lastFrictionLaunchPkg = null
        val elapsedMinutes = ((System.currentTimeMillis() - launchedAt) / 60_000L).toInt()
        if (elapsedMinutes < 10) return
        val app = _uiState.value.installedApps.firstOrNull { it.packageName == pkg } ?: return
        val usage = appRepo.getTodayUsageSummary(pkg)
        val opensToday = if (usage.hasPermission && usage.opensToday > 0) {
            usage.opensToday
        } else {
            prefsRepo.getLocalAppOpensToday(pkg)
        }
        _uiState.update {
            it.copy(
                postSessionNudge = PostSessionNudge(
                    app = app,
                    sessionMinutes = elapsedMinutes,
                    opensToday = opensToday
                )
            )
        }
    }

    fun dismissPostSessionNudge() {
        _uiState.update { it.copy(postSessionNudge = null) }
    }

    fun togglePin(profile: ContextProfile, packageName: String) {
        if (profile == ContextProfile.ALL) return
        val apps = _uiState.value.installedApps
        val updatedSet = prefsRepo.togglePinnedPackage(profile, packageName, apps)
        val updatedOrder = if (profile == ContextProfile.WORK) {
            val order = prefsRepo.getFavoriteOrder(updatedSet)
            prefsRepo.saveFavoriteOrder(order)
            order
        } else {
            _uiState.value.favoriteOrder
        }
        _uiState.update { state ->
            state.copy(
                pinnedByProfile = state.pinnedByProfile + (profile to updatedSet),
                favoriteOrder = updatedOrder
            )
        }
    }

    fun moveFavorite(packageName: String, delta: Int) {
        val currentFavs = _uiState.value.pinnedByProfile[ContextProfile.WORK].orEmpty()
        val updatedOrder = prefsRepo.moveFavorite(packageName, delta, currentFavs)
        _uiState.update { it.copy(favoriteOrder = updatedOrder) }
    }

    fun setIconShapePreset(preset: IconShapePreset) {
        prefsRepo.setIconShapePreset(preset)
        _uiState.update { it.copy(iconShapePreset = preset) }
    }

    fun setHideStatusBar(hide: Boolean) {
        prefsRepo.setHideStatusBar(hide)
        _uiState.update { it.copy(hideStatusBar = hide) }
    }

    fun renameFolder(folderId: String, newName: String) {
        val updated = prefsRepo.renameFolder(folderId, newName)
        _uiState.update { it.copy(folders = updated) }
    }

    fun rerollCommandUtility() {
        val state = _uiState.value
        val result = CommandBarEngine.evaluate(state.commandQuery, state.installedApps)
        _uiState.update { it.copy(commandResult = result) }
    }

    fun toggleFrictionApp(packageName: String) {
        val apps = _uiState.value.installedApps
        val updated = prefsRepo.toggleFrictionPackage(packageName, apps)
        _uiState.update { it.copy(frictionPackages = updated) }
    }

    fun toggleHiddenApp(packageName: String) {
        val updated = prefsRepo.toggleHiddenPackage(packageName)
        _uiState.update { it.copy(hiddenPackages = updated) }
    }

    fun setIconSizePreset(preset: IconSizePreset) {
        prefsRepo.setIconSizePreset(preset)
        _uiState.update { it.copy(iconSizePreset = preset) }
    }

    fun setClockStyle(style: ClockStyle) {
        prefsRepo.setClockStyle(style)
        _uiState.update { it.copy(clockStyle = style) }
    }

    fun setAccentPreset(preset: AccentPreset) {
        prefsRepo.setAccentPreset(preset)
        _uiState.update { it.copy(accentPreset = preset) }
    }

    fun setHideFavoriteLabels(hide: Boolean) {
        prefsRepo.setHideFavoriteLabels(hide)
        _uiState.update { it.copy(hideFavoriteLabels = hide) }
    }

    fun setShowBatteryOnHome(show: Boolean) {
        prefsRepo.setShowBatteryOnHome(show)
        _uiState.update { it.copy(showBatteryOnHome = show) }
    }

    fun setShowAlphabetWave(show: Boolean) {
        prefsRepo.setShowAlphabetWave(show)
        _uiState.update { it.copy(showAlphabetWave = show) }
    }

    fun setShowWallpaperBackground(show: Boolean) {
        prefsRepo.setShowWallpaperBackground(show)
        _uiState.update { it.copy(showWallpaperBackground = show) }
    }

    fun setShowCalendarEvents(show: Boolean) {
        prefsRepo.setShowCalendarEvents(show)
        _uiState.update { it.copy(showCalendarEvents = show) }
        refreshTimeContextIfAuto()
    }

    fun setShowNotificationPreviews(show: Boolean) {
        prefsRepo.setShowNotificationPreviews(show)
        _uiState.update { it.copy(showNotificationPreviews = show) }
    }

    fun createFolder(name: String, initialPackage: String? = null) {
        val updated = prefsRepo.createFolder(name, initialPackage)
        _uiState.update { it.copy(folders = updated) }
    }

    fun deleteFolder(folderId: String) {
        val updated = prefsRepo.deleteFolder(folderId)
        _uiState.update { it.copy(folders = updated) }
    }

    fun toggleAppInFolder(folderId: String, packageName: String) {
        val updated = prefsRepo.toggleAppInFolder(folderId, packageName)
        _uiState.update { it.copy(folders = updated) }
    }

    fun exportBackupJson(): String = prefsRepo.exportBackupJson(_uiState.value.installedApps)

    fun importBackupJson(rawJson: String): Boolean {
        val ok = prefsRepo.importBackupJson(rawJson)
        if (ok) refreshAll()
        return ok
    }

    fun openNotificationListenerSettings() = appRepo.openNotificationListenerSettings()

    fun setShowMediaPlayer(show: Boolean) {
        prefsRepo.setShowMediaPlayer(show)
        _uiState.update { it.copy(showMediaPlayer = show) }
    }

    fun setSmartSortEnabled(enabled: Boolean) {
        prefsRepo.setSmartSortEnabled(enabled)
        _uiState.update { it.copy(smartSortEnabled = enabled) }
    }

    fun setShowMostUsedApps(show: Boolean) {
        prefsRepo.setShowMostUsedApps(show)
        _uiState.update { it.copy(showMostUsedApps = show) }
    }

    fun setMonochromeIcons(enabled: Boolean) {
        prefsRepo.setMonochromeIcons(enabled)
        _uiState.update { it.copy(monochromeIcons = enabled) }
    }

    fun setSelectedIconPack(packageName: String?) {
        prefsRepo.setIconPackPackage(packageName)
        _uiState.update { it.copy(selectedIconPack = packageName) }
    }

    fun setNiagaraButtonPackage(packageName: String?) {
        prefsRepo.setNiagaraButtonPackage(packageName)
        _uiState.update { it.copy(niagaraButtonPackage = packageName) }
    }

    fun setPinnedWidgetId(widgetId: Int) {
        prefsRepo.setPinnedWidgetId(widgetId)
        _uiState.update { it.copy(pinnedWidgetId = widgetId) }
    }

    fun addPinnedWidgetId(widgetId: Int) {
        val updated = prefsRepo.addPinnedWidgetId(widgetId)
        _uiState.update {
            it.copy(
                pinnedWidgetIds = updated,
                pinnedWidgetId = updated.firstOrNull() ?: -1
            )
        }
    }

    fun removePinnedWidgetId(widgetId: Int) {
        if (widgetId > 0) WidgetHostHelper.deleteWidgetId(getApplication(), widgetId)
        val updated = prefsRepo.removePinnedWidgetId(widgetId)
        _uiState.update {
            it.copy(
                pinnedWidgetIds = updated,
                pinnedWidgetId = updated.firstOrNull() ?: -1
            )
        }
    }

    fun clearPinnedWidget() {
        _uiState.value.pinnedWidgetIds.forEach { id ->
            if (id > 0) WidgetHostHelper.deleteWidgetId(getApplication(), id)
        }
        val oldId = _uiState.value.pinnedWidgetId
        if (oldId > 0 && oldId !in _uiState.value.pinnedWidgetIds) {
            WidgetHostHelper.deleteWidgetId(getApplication(), oldId)
        }
        _uiState.value.pinnedWidgetIds.forEach { prefsRepo.removePinnedWidgetId(it) }
        prefsRepo.setPinnedWidgetId(-1)
        _uiState.update { it.copy(pinnedWidgetIds = emptyList(), pinnedWidgetId = -1) }
    }

    fun toggleSummaryPackage(packageName: String) {
        val updated = prefsRepo.toggleSummaryPackage(packageName)
        _uiState.update { it.copy(summaryPackages = updated) }
    }

    fun loadShortcutsForApp(packageName: String): List<AppShortcutItem> =
        appRepo.loadAppShortcuts(packageName)

    fun launchShortcut(packageName: String, shortcutId: String) {
        appRepo.launchAppShortcut(packageName, shortcutId)
    }

    fun toggleMediaPlayPause() = NotificationRepository.toggleMediaPlayPause(getApplication())

    fun skipMediaNext() = NotificationRepository.skipMediaNext(getApplication())

    fun skipMediaPrevious() = NotificationRepository.skipMediaPrevious(getApplication())

    fun dismissNotification(key: String) = NotificationRepository.dismissNotification(key)

    fun sendNotificationReply(preview: NotificationPreview, replyText: String): Boolean =
        NotificationRepository.sendQuickReply(getApplication(), preview, replyText)

    fun dialContact(phoneNumber: String) {
        appRepo.dialContact(phoneNumber)
        clearCommandIfActive()
    }

    fun setFontPreset(preset: FontPreset) {
        prefsRepo.setFontPreset(preset)
        _uiState.update { it.copy(fontPreset = preset) }
    }

    fun setShowContactSearch(show: Boolean) {
        prefsRepo.setShowContactSearch(show)
        _uiState.update { it.copy(showContactSearch = show) }
    }

    fun setShowMusicOnHeadphones(show: Boolean) {
        prefsRepo.setShowMusicOnHeadphones(show)
        _uiState.update { it.copy(showMusicOnHeadphones = show) }
    }

    fun setTwoColumnLargeScreen(enabled: Boolean) {
        prefsRepo.setTwoColumnLargeScreen(enabled)
        _uiState.update { it.copy(twoColumnLargeScreen = enabled) }
    }

    fun setCustomAppName(packageName: String, newName: String?) {
        prefsRepo.setCustomAppName(packageName, newName)
        refreshAll()
    }

    fun setAppPopupWidget(packageName: String, widgetId: Int) {
        val updated = prefsRepo.setAppPopupWidget(packageName, widgetId)
        _uiState.update { it.copy(appPopupWidgets = updated) }
    }

    fun clearAppPopupWidget(packageName: String) {
        val oldId = _uiState.value.appPopupWidgets[packageName] ?: -1
        if (oldId > 0) WidgetHostHelper.deleteWidgetId(getApplication(), oldId)
        val updated = prefsRepo.setAppPopupWidget(packageName, -1)
        _uiState.update { it.copy(appPopupWidgets = updated) }
    }

    fun openUsageSettings() = appRepo.openUsageStatsSettings()

    fun openDefaultLauncherSettings() = appRepo.openDefaultLauncherSettings()

    fun openAppInfo(packageName: String) = appRepo.openAppInfo(packageName)

    fun uninstallApp(packageName: String) = appRepo.uninstallApp(packageName)

    private fun clearCommandIfActive() {
        if (_uiState.value.commandQuery.isNotEmpty()) {
            _uiState.update { it.copy(commandQuery = "", commandResult = CommandResult.Empty) }
        }
    }
}
