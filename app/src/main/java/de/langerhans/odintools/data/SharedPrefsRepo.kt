package de.langerhans.odintools.data

import android.content.Context
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SharedPrefsRepo @Inject constructor(
    @ApplicationContext context: Context
) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var disabledControllerStyle
        get() = prefs.getString(KEY_DISABLED_CONTROLLER_STYLE, null)
        set(value) = prefs.edit().putString(KEY_DISABLED_CONTROLLER_STYLE, value).apply()

    var disabledL2r2Style
        get() = prefs.getString(KEY_DISABLED_L2R2_STYLE, null)
        set(value) = prefs.edit().putString(KEY_DISABLED_L2R2_STYLE, value).apply()

    var disabledPerfModes
        get() = prefs.getString(KEY_DISABLED_PERF_MODES, null)
        set(value) = prefs.edit().putString(KEY_DISABLED_PERF_MODES, value).apply()
    var disabledFanModes
        get() = prefs.getString(KEY_DISABLED_FAN_MODES, null)
        set(value) = prefs.edit().putString(KEY_DISABLED_FAN_MODES, value).apply()

    var saturationOverride
        get() = prefs.getFloat(KEY_SATURATION_OVERRIDE, 1.0f)
        set(value) = prefs.edit().putFloat(KEY_SATURATION_OVERRIDE, value).apply()

    var appOverridesEnabled
        get() = prefs.getBoolean(KEY_APP_OVERRIDE_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_APP_OVERRIDE_ENABLED, value).apply()

    private var appOverrideEnabledListener: OnSharedPreferenceChangeListener? = null

    fun observeAppOverrideEnabledState(onChange: (newState: Boolean) -> Unit) {
        appOverrideEnabledListener = OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_APP_OVERRIDE_ENABLED) {
                onChange(appOverridesEnabled)
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(appOverrideEnabledListener)
    }

    fun removeAppOverrideEnabledObserver() {
        prefs.unregisterOnSharedPreferenceChangeListener(appOverrideEnabledListener)
        appOverrideEnabledListener = null
    }

    companion object {
        private const val PREFS_NAME = "odintools"

        private const val KEY_DISABLED_CONTROLLER_STYLE = "disabled_controller_style"
        private const val KEY_DISABLED_L2R2_STYLE = "disabled_l2r2_style"
        private const val KEY_DISABLED_PERF_MODES = "disabled_perf_modes"
        private const val KEY_DISABLED_FAN_MODES = "disabled_fan_modes"
        private const val KEY_SATURATION_OVERRIDE = "saturation_override"
        private const val KEY_APP_OVERRIDE_ENABLED = "app_override_enabled"
    }
}