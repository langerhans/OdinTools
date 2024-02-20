package de.langerhans.odintools.tools

import de.langerhans.odintools.BuildConfig
import javax.inject.Inject

class SettingsRepo @Inject constructor(
    private val executor: ShellExecutor,
) {

    fun applyRequiredSettings() {
        enableA11yService()
        grantAllAppsPermission()
        // Don't add to whitelist on debug builds, otherwise even Android Studio can't kill the app
        if (!BuildConfig.DEBUG) {
            addOdinToolsToWhitelist()
        }
    }

    private fun enableA11yService() {
        val currentServices =
            executor.executeAsRoot("settings get secure $KEY_ACCESSIBILITY_SERVICES")
                .map { it ?: "" }
                .getOrDefault("")

        if (currentServices.contains(PACKAGE)) return

        executor.executeAsRoot(
            "settings put secure $KEY_ACCESSIBILITY_SERVICES $PACKAGE/$PACKAGE.service.ForegroundAppWatcherService:$currentServices"
                .trimEnd(':'),
        )
    }

    private fun grantAllAppsPermission() {
        executor.executeAsRoot("pm grant $PACKAGE android.permission.QUERY_ALL_PACKAGES")
    }

    private fun addOdinToolsToWhitelist() {
        val currentWhitelist = whitelist
        if (currentWhitelist.contains(PACKAGE)) return
        val newWhitelist = "$PACKAGE,$currentWhitelist".trimEnd(',')
        whitelist = newWhitelist
    }

    fun setSfSaturation(value: Float) {
        executor.executeAsRoot("service call SurfaceFlinger 1022 f ${String.format("%.1f", value)}")
    }

    private var whitelist: String
        get() = executor.getStringSystemSetting(KEY_APP_WHITELIST, "")
        set(value) = executor.setStringSystemSetting(KEY_APP_WHITELIST, value)

    var preventPressHome: Boolean
        get() = executor.getBooleanSystemSetting(KEY_PREVENT_PRESS_HOME, true)
        set(value) = executor.setBooleanSystemSetting(KEY_PREVENT_PRESS_HOME, value)

    var vibrationEnabled: Boolean
        get() = executor.getBooleanSystemSetting(KEY_VIBRATE_ON, false)
        set(value) = executor.setBooleanSystemSetting(KEY_VIBRATE_ON, value)

    var vibrationStrength: Int
        get() = executor.getIntValue(KEY_VIBRATION_STRENGTH, 0)
        set(value) = executor.setIntValue(KEY_VIBRATION_STRENGTH, value)

    var chargingSeparationEnabled: Boolean
        get() = executor.getBooleanSystemSetting(KEY_CHARGING_SEPARATION, false)
        set(value) = executor.setBooleanSystemSetting(KEY_CHARGING_SEPARATION, value)

    var restrictCharge: Boolean
        get() = executor.getBooleanSystemSetting(KEY_RESTRICT_CHARGE, false)
        set(value) = executor.setBooleanSystemSetting(KEY_RESTRICT_CHARGE, value)

    var restrictCurrent: Int
        get() = executor.getIntValue(KEY_RESTRICT_CURRENT, 0)
        set(value) = executor.setIntValue(KEY_RESTRICT_CURRENT, value)

    var chargingLimit80Enabled: Boolean
        get() = executor.getBooleanSystemSetting(KEY_CHARGING_LIMIT_80, false)
        set(value) = executor.setBooleanSystemSetting(KEY_CHARGING_LIMIT_80, value)

    var chargingLimit10Enabled: Boolean
        get() = executor.getBooleanSystemSetting(KEY_CHARGING_LIMIT_10, false)
        set(value) = executor.setBooleanSystemSetting(KEY_CHARGING_LIMIT_10, value)

    companion object {
        private const val PACKAGE = BuildConfig.APPLICATION_ID
        const val KEY_VENDOR_NAME = "ro.vendor.retro.name"
        const val KEY_BUILD_VERSION = "ro.build.odin2.ota.version"
        const val KEY_SATURATION = "persist.sys.sf.color_saturation"
        const val KEY_ACCESSIBILITY_SERVICES = "enabled_accessibility_services"
        const val KEY_APP_WHITELIST = "app_whiteList"
        const val KEY_PREVENT_PRESS_HOME = "prevent_press_home_accidentally"
        const val KEY_VIBRATE_ON = "vibrate_on"
        const val KEY_CUSTOM_M1_VALUE = "remap_custom_to_m1_value"
        const val KEY_CUSTOM_M2_VALUE = "remap_custom_to_m2_value"
        const val KEY_VIBRATION_STRENGTH = "/d/haptics/user_vmax_mv"
        const val KEY_CHARGING_SEPARATION = "is_charging_separation"
        const val KEY_CHARGING_LIMIT_80 = "charging_limit_greater_than_80"
        const val KEY_CHARGING_LIMIT_10 = "charging_limit_less_than_10"
        const val KEY_RESTRICT_CHARGE = "/sys/class/qcom-battery/restrict_chg"
        const val KEY_RESTRICT_CURRENT = "/sys/class/qcom-battery/restrict_cur"
    }
}
