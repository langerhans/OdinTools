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
        val result = reply.createByteArray()?.toString(Charset.defaultCharset())?.trim()?.let {
            if (it == "null") null else it
        }
        data.recycle()
        reply.recycle()
        return Result.success(result)
    }

    private fun getProperty(property: String): Result<String?> {
        return executeAsRoot("getprop $property")
    }

    fun getIntProperty(property: String, defaultValue: Int): Int {
        return getProperty(property)
            .mapCatching { it?.toInt() ?: defaultValue }
            .getOrDefault(defaultValue)
    }

    fun getFloatProperty(property: String, defaultValue: Float): Float {
        return getProperty(property)
            .mapCatching { it?.toFloat() ?: defaultValue }
            .getOrDefault(defaultValue)
    }

    fun getBooleanProperty(property: String, defaultValue: Boolean): Boolean {
        return getProperty(property)
            .map { if (it == null) defaultValue else it == "1" }
            .getOrDefault(defaultValue)
    }

    fun getStringProperty(property: String, defaultValue: String): String {
        return getProperty(property)
            .map { it ?: defaultValue }
            .getOrDefault(defaultValue)
    }

    private fun getSystemSetting(setting: String): Result<String?> {
        return executeAsRoot("settings get system $setting")
    }

    fun getStringSystemSetting(setting: String, defaultValue: String): String {
        return getSystemSetting(setting)
            .map { it ?: defaultValue }
            .getOrDefault(defaultValue)
    }

    fun setStringSystemSetting(setting: String, value: String) {
        executeAsRoot("settings put system $setting $value")
    }

    fun getIntSystemSetting(setting: String, defaultValue: Int): Int {
        return getSystemSetting(setting)
            .mapCatching { it?.toInt() ?: defaultValue }
            .getOrDefault(defaultValue)
    }

    fun setIntSystemSetting(setting: String, value: Int) {
        executeAsRoot("settings put system $setting $value")
    }

    fun getBooleanSystemSetting(setting: String, defaultValue: Boolean): Boolean {
        return getSystemSetting(setting)
            .map { if (it == null) defaultValue else it == "1" }
            .getOrDefault(defaultValue)
    }

    fun setBooleanSystemSetting(setting: String, value: Boolean) {
        setIntSystemSetting(setting, if (value) 1 else 0)
    }

    private fun getValue(file: String): Result<String?> {
        return executeAsRoot("cat $file")
    }

    fun getStringValue(file: String, defaultValue: String): String {
        return getValue(file)
            .map { it ?: defaultValue }
            .getOrDefault(defaultValue)
    }

    fun setStringValue(file: String, value: String) {
        executeAsRoot("echo $value > $file")
    }

    fun getIntValue(file: String, defaultValue: Int): Int {
        return getValue(file)
            .mapCatching { it?.toInt() ?: defaultValue }
            .getOrDefault(defaultValue)
    }

    fun setIntValue(file: String, value: Int) {
        executeAsRoot("echo $value > $file")
    }

    fun getBooleanValue(file: String, defaultValue: Boolean): Boolean {
        return getValue(file)
            .map { if (it == null) defaultValue else it == "1" }
            .getOrDefault(defaultValue)
    }

    fun setBooleanValue(file: String, value: Boolean) {
        setIntValue(file, if (value) 1 else 0)
    }
}
