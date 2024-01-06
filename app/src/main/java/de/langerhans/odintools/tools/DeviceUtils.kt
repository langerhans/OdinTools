package de.langerhans.odintools.tools

import javax.inject.Inject

class DeviceUtils @Inject constructor(
    private val shellExecutor: ShellExecutor
) {
    fun isOdin2() = shellExecutor.executeAsRoot("getprop ro.vendor.retro.name").map { it == "Q9" }
        .getOrDefault(false)

    fun isPServerAvailable() = shellExecutor.pServerAvailable

    fun getDeviceVersion() = shellExecutor.executeAsRoot("getprop ro.build.odin2.ota.version").map { it ?: "" }
        .getOrDefault("")
}