package de.langerhans.odintools.data

import android.content.Context
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SharedPrefsRepo @Inject constructor(
    @ApplicationContext context: Context,
) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var disabledControllerStyle
        get() = prefs.getString(KEY_DISABLED_CONTROLLER_STYLE, null)
        set(value) = prefs.edit().putString(KEY_DISABLED_CONTROLLER_STYLE, value).apply()

    var disabledL2r2Style
        get() = prefs.getString(KEY_DISABLED_L2R2_STYLE, null)
        set(value) = prefs.edit().putString(KEY_DISABLED_L2R2_STYLE, value).apply()

    var saturationOverride
        get() = prefs.getFloat(KEY_SATURATION_OVERRIDE, 1.0f)
        set(value) = prefs.edit().putFloat(KEY_SATURATION_OVERRIDE, value).apply()

    var vibrationStrength
        get() = prefs.getInt(KEY_VIBRATION_STRENGTH, 0)
        set(value) = prefs.edit().putInt(KEY_VIBRATION_STRENGTH, value).apply()

    var appOverridesEnabled
        get() = prefs.getBoolean(KEY_APP_OVERRIDE_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_APP_OVERRIDE_ENABLED, value).apply()

    var overrideDelay
        get() = prefs.getBoolean(KEY_OVERRIDE_DELAY, false)
        set(value) = prefs.edit().putBoolean(KEY_OVERRIDE_DELAY, value).apply()

    var chargeLimitEnabled
        get() = prefs.getBoolean(KEY_CHARGE_LIMIT_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_CHARGE_LIMIT_ENABLED, value).apply()

    var minBatteryLevel
        get() = prefs.getInt(KEY_MIN_BATTERY_LEVEL, 20)
        set(value) = prefs.edit().putInt(KEY_MIN_BATTERY_LEVEL, value).apply()

    var maxBatteryLevel
        get() = prefs.getInt(KEY_MAX_BATTERY_LEVEL, 80)
        set(value) = prefs.edit().putInt(KEY_MAX_BATTERY_LEVEL, value).apply()

    private var appOverrideEnabledListener: OnSharedPreferenceChangeListener? = null

    fun observeAppOverrideEnabledState(overridesEnabled: (newState: Boolean) -> Unit, overrideDelayEnabled: (newState: Boolean) -> Unit) {
        appOverrideEnabledListener = OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_APP_OVERRIDE_ENABLED) {
                overridesEnabled(appOverridesEnabled)
            } else if (key == KEY_OVERRIDE_DELAY) {
                overrideDelayEnabled(overrideDelay)
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
        private const val KEY_SATURATION_OVERRIDE = "saturation_override"
        private const val KEY_VIBRATION_STRENGTH = "vibration_strength"
        private const val KEY_APP_OVERRIDE_ENABLED = "app_override_enabled"
        private const val KEY_OVERRIDE_DELAY = "override_delay"
        private const val KEY_CHARGE_LIMIT_ENABLED = "charge_limit_enabled"
        private const val KEY_MIN_BATTERY_LEVEL = "min_battery_level"
        private const val KEY_MAX_BATTERY_LEVEL = "max-battery_level"
    }
}
