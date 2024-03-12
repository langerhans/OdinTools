package de.langerhans.odintools.models

import androidx.annotation.StringRes
import de.langerhans.odintools.R
import de.langerhans.odintools.tools.ShellExecutor

sealed class VibrationStrength(
    val id: String,
    val settingsValue: Int,
    @StringRes val textRes: Int,
) {
    data object Off : VibrationStrength("vibrationOff", 0, R.string.vibrationOff)
    data object Low : VibrationStrength("lowVibration", 1100, R.string.lowVibration)
    data object Medium : VibrationStrength("mediumVibration", 2100, R.string.mediumVibration)
    data object High : VibrationStrength("highVibration", 3100, R.string.highVibration)
    data object Unknown : VibrationStrength("unknown", -1, R.string.unknown)

    fun enable(executor: ShellExecutor) {
        if (this != Unknown) {
            executor.setIntSystemSetting(KEY_VIBRATION_STRENGTH, settingsValue)
        }
    }

    companion object {
        private const val KEY_VIBRATION_STRENGTH = "vibration_strength"

        fun getMode(executor: ShellExecutor) = when (executor.getIntSystemSetting(KEY_VIBRATION_STRENGTH, Low.settingsValue)) {
            Low.settingsValue -> Low
            Medium.settingsValue -> Medium
            High.settingsValue -> High
            Off.settingsValue -> Off
            else -> Unknown
        }

        fun getById(id: String?) = when (id) {
            Low.id -> Low
            Medium.id -> Medium
            High.id -> High
            Off.id -> Off
            else -> Unknown
        }

        fun getDisabledVibrationStrengths(perfModeKey: String?): List<String> {
            return when (perfModeKey) {
                PerfMode.Standard.id -> listOf(NoChange.KEY, Unknown.id)
                PerfMode.Performance.id -> listOf(NoChange.KEY, Unknown.id, Off.id)
                PerfMode.HighPerformance.id -> listOf(NoChange.KEY, Unknown.id, Off.id, Low.id)
                else -> emptyList()
            }
        }
    }
}
