package de.langerhans.odintools.tools

import android.annotation.SuppressLint
import android.os.IBinder
import android.os.Parcel
import java.nio.charset.Charset
import javax.inject.Inject

@SuppressLint("DiscouragedPrivateApi", "PrivateApi") // :kekw:
class ShellExecutor @Inject constructor() {

    private val binder: IBinder?
    var pServerAvailable: Boolean = false
        private set

    init {
        binder = runCatching {
            val serviceManager = Class.forName("android.os.ServiceManager")
            val getService = serviceManager.getDeclaredMethod("getService", String::class.java)
            val binder = getService.invoke(serviceManager, "PServerBinder") as IBinder
            pServerAvailable = true
            binder
        }.getOrDefault(null)
    }

    fun executeAsRoot(cmd: String): Result<String?> {
        if (binder == null) return Result.failure(IllegalStateException("PServer not available!"))

        val data = Parcel.obtain()
        val reply = Parcel.obtain()
        data.writeStringArray(arrayOf(cmd, "1"))
        runCatching { binder!!.transact(0, data, reply, 0) }
            .getOrElse {
                return Result.failure(it)
            }
        val result = reply.createByteArray()?.toString(Charset.defaultCharset())?.trim()
        data.recycle()
        reply.recycle()
        return Result.success(result)
    }

    fun getSystemSetting(setting: String, defaultValue: Int): Int {
        return executeAsRoot("settings get system $setting")
            .map { if (it == null || it == "null") defaultValue else it.toInt() }
            .getOrDefault(defaultValue)
    }

    fun setSystemSetting(setting: String, value: Int) {
        executeAsRoot("settings put system $setting $value")
    }

    fun setBooleanSystemSetting(setting: String, value: Boolean) {
        setSystemSetting(setting, if (value) 1 else 0)
    }

    fun getBooleanSystemSetting(setting: String, defaultValue: Boolean): Boolean {
        return executeAsRoot("settings get system $setting")
            .map { if (it == null) defaultValue else it == "1" }
            .getOrDefault(defaultValue)
    }

    fun enableA11yService() {
        val currentServices =
            executeAsRoot("settings get secure enabled_accessibility_services")
                .map {
                    if (it == null || it == "null") "" else it
                }
                .getOrDefault("")

        if (currentServices.contains("de.langerhans.odintools")) return

        executeAsRoot(
            "settings put secure enabled_accessibility_services $PACKAGE/$PACKAGE.service.ForegroundAppWatcherService:$currentServices"
                .trimEnd(':')
        )
    }

    fun setSfSaturation(saturation: Float) {
        executeAsRoot("service call SurfaceFlinger 1022 f ${String.format("%.1f", saturation)}")
    }

    fun setVibrationStrength(newValue: Int) {
        executeAsRoot("echo $newValue > /d/haptics/user_vmax_mv")
    }

    fun getVibrationStrength(): Int {
        val defaultValue = 0

        return executeAsRoot("cat /d/haptics/user_vmax_mv")
            .mapCatching { it?.toInt() ?: defaultValue } // This throws on RP4 because it has no rumble
            .getOrDefault(defaultValue)
    }

    fun grantAllAppsPermission() {
        executeAsRoot("pm grant $PACKAGE android.permission.QUERY_ALL_PACKAGES")
    }

    companion object {
        private const val PACKAGE = "de.langerhans.odintools"
    }
}
