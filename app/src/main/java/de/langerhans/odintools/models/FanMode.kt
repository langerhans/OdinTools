package de.langerhans.odintools.models

import androidx.annotation.StringRes
import de.langerhans.odintools.R
import de.langerhans.odintools.tools.ShellExecutor

sealed class FanMode(
    val id: String,
    val settingsValue: Int,
    @StringRes val textRes: Int
) {
    data object Off : FanMode("fanOff", 0, R.string.fanOff)
    data object Quiet : FanMode("quiet", 1, R.string.quiet)
    data object Smart : FanMode("smart", 4, R.string.smart)
    data object Sport : FanMode("sport", 5, R.string.sport)
    data object Unknown : FanMode("unknown", -1, R.string.unknown)

    fun enable(executor: ShellExecutor) {
        if (this != Unknown) {
            executor.setSystemSetting(FAN_MODE, settingsValue)
        }
    }

    companion object {
        private const val FAN_MODE = "fan_mode"

        fun getMode(executor: ShellExecutor) =
            when (executor.getSystemSetting(FAN_MODE, Quiet.settingsValue)) {
                Quiet.settingsValue -> Quiet
                Smart.settingsValue -> Smart
                Sport.settingsValue -> Sport
                Off.settingsValue -> Off
                else -> Unknown
            }

        fun getById(id: String?) = when(id) {
            Quiet.id -> Quiet
            Smart.id -> Smart
            Sport.id -> Sport
            Off.id -> Off
            else -> Unknown
        }

        fun getDisabledFanModes(perfModeKey: String?): List<String> {
            return when(perfModeKey) {
                PerfMode.Standard.id -> listOf(NoChange.KEY, Unknown.id)
                PerfMode.Performance.id -> listOf(NoChange.KEY, Unknown.id, Off.id)
                PerfMode.HighPerformance.id -> listOf(NoChange.KEY, Unknown.id, Off.id, Quiet.id)
                else -> emptyList()
            }
        }
    }
}