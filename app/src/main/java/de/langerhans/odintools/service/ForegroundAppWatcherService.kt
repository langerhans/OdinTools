package de.langerhans.odintools.service

import android.accessibilityservice.AccessibilityService
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent
import dagger.hilt.android.AndroidEntryPoint
import de.langerhans.odintools.data.AppOverrideDao
import de.langerhans.odintools.data.AppOverrideEntity
import de.langerhans.odintools.data.SharedPrefsRepo
import de.langerhans.odintools.models.ControllerStyle
import de.langerhans.odintools.models.ControllerStyle.Unknown
import de.langerhans.odintools.models.FanModes
import de.langerhans.odintools.models.PerfModes
import de.langerhans.odintools.models.L2R2Style
import de.langerhans.odintools.tools.ShellExecutor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ForegroundAppWatcherService @Inject constructor(): AccessibilityService() {

    @Inject
    lateinit var appOverrideDao: AppOverrideDao

    @Inject
    lateinit var shellExecutor: ShellExecutor

    @Inject
    lateinit var sharedPrefsRepo: SharedPrefsRepo

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    private var currentForegroundPackage: CharSequence = ""
    private lateinit var overrides: List<AppOverrideEntity>
    private var overridesEnabled = true

    private var hasSetOverride = false
    private var savedControllerStyle: ControllerStyle? = null
    private var savedL2R2Style: L2R2Style? = null
    private var savedPerfModes: PerfModes? = null
    private var savedFanModes: FanModes? = null


    private var currentIme = ""
    private val imeObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) {
            currentIme = shellExecutor
                .executeAsRoot("settings get secure default_input_method")
                .getOrDefault("") ?: ""
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (shouldIgnore(event)) return

        currentForegroundPackage = event.packageName

        overrides.find { it.packageName == currentForegroundPackage }?.let { override ->
            applyOverride(override)
        } ?: run {
            resetOverrides()
        }
    }

    private fun shouldIgnore(event: AccessibilityEvent): Boolean {
        if (overridesEnabled.not()) return true // User disabled overrides globally
        if (::overrides.isInitialized.not()) return true // Got an event before the DB was returning data
        if (ignoredPackages.contains(event.packageName)) return true // Ignore some system packages
        if (event.packageName == currentForegroundPackage) return true // No action on duplicate events
        if (currentIme.contains(event.packageName)) return true // Ignore keyboards popping up

        return false // All good, process event
    }

    private fun applyOverride(override: AppOverrideEntity) {
        // This check makes sure that we don't override the "defaults" when switching between apps with overrides
        if (!hasSetOverride) {
            savedControllerStyle = ControllerStyle.getStyle(shellExecutor)
            savedL2R2Style = L2R2Style.getStyle(shellExecutor)
            savedPerfModes = PerfModes.getStyle(shellExecutor)
            savedFanModes = FanModes.getStyle(shellExecutor)

        }

        ControllerStyle.getById(override.controllerStyle).takeIf {
            it != Unknown
        }?.enable(shellExecutor) ?: run {
            // Reset to default if we switch between override and NoChange app
            savedControllerStyle?.enable(shellExecutor)
        }

        L2R2Style.getById(override.l2R2Style).takeIf {
            it != L2R2Style.Unknown
        }?.enable(shellExecutor) ?: run {
            // Reset to default if we switch between override and NoChange app
            savedL2R2Style?.enable(shellExecutor)
        }

        PerfModes.getById(override.perfModes).takeIf {
            it != PerfModes.Unknown
        }?.enable(shellExecutor) ?: run {
            // Reset to default if we switch between override and NoChange app
            savedPerfModes?.enable(shellExecutor)
        }

        FanModes.getById(override.fanModes).takeIf {
            it != FanModes.Unknown
        }?.enable(shellExecutor) ?: run {
            // Reset to default if we switch between override and NoChange app
            savedFanModes?.enable(shellExecutor)
        }

        hasSetOverride = true
    }

    private fun resetOverrides() {
        if (!hasSetOverride) return

        savedControllerStyle?.enable(shellExecutor)
        savedControllerStyle = null

        savedL2R2Style?.enable(shellExecutor)
        savedL2R2Style = null

        savedPerfModes?.enable(shellExecutor)
        savedPerfModes = null

        savedFanModes?.enable(shellExecutor)
        savedFanModes = null

        hasSetOverride = false
    }

    override fun onInterrupt() {
        // Nothing here
    }

    override fun onServiceConnected() {
        super.onServiceConnected()

        currentIme = shellExecutor
            .executeAsRoot("settings get secure default_input_method")
            .getOrDefault("") ?: ""
        contentResolver.registerContentObserver(
            Settings.Secure.getUriFor(Settings.Secure.DEFAULT_INPUT_METHOD),
            false,
            imeObserver
        )

        overridesEnabled = sharedPrefsRepo.appOverridesEnabled
        sharedPrefsRepo.observeAppOverrideEnabledState { overridesEnabled = it }

        scope.launch {
            appOverrideDao.getAll()
                .flowOn(Dispatchers.IO)
                .collect { overrides ->
                    this@ForegroundAppWatcherService.overrides = overrides
                }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
        contentResolver.unregisterContentObserver(imeObserver)
        sharedPrefsRepo.removeAppOverrideEnabledObserver()
    }

    companion object {
        private val ignoredPackages = listOf(
            "com.android.launcher3",
            "com.odin2.gameassistant",
            "com.android.systemui",
            "android"
        )
    }
}