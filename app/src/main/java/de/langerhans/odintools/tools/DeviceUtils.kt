package de.langerhans.odintools.tools

import de.langerhans.odintools.tools.DeviceType.*
import javax.inject.Inject

class DeviceUtils @Inject constructor(
    private val shellExecutor: ShellExecutor,
) {

    fun getDeviceType(): DeviceType {
        val deviceName = shellExecutor.executeAsRoot("getprop ro.vendor.retro.name").getOrNull()
        return when (deviceName) {
            "Q9" -> ODIN2
            "4.0", "4.0P" -> RP4
            else -> OTHER
        }
    }

    fun getDeviceVersion() = shellExecutor.executeAsRoot("getprop ro.build.odin2.ota.version").map { it ?: "" }
        .getOrDefault("")
}

enum class DeviceType {
    ODIN2,
    RP4,
    OTHER,
}
