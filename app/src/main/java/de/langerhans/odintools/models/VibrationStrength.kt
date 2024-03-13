package de.langerhans.odintools.models

import androidx.annotation.StringRes
import de.langerhans.odintools.R
import de.langerhans.odintools.tools.ShellExecutor

sealed class VibrationStrength(
    val id: String,
    val settingsValue: Int,
    @StringRes val textRes: Int,
) {
    data object VibrationOff : VibrationStrength("vibrationOff", 0, R.string.vibrationOff)
    data object VibrationLow : VibrationStrength("lowVibration", 1100, R.string.lowVibration)
    data object VibrationMedium : VibrationStrength("mediumVibration", 1600, R.string.mediumVibration)
    data object VibrationHigh : VibrationStrength("highVibration", 2800, R.string.highVibration)
    data object VibrationUnknown : VibrationStrength("unknown", -1, R.string.unknown)

    fun enable(executor: ShellExecutor) {
        if (this != VibrationUnknown) {
            executor.setVibrationStrength(settingsValue)
        }
    }

    companion object {
        private const val KEY_VIBRATION_STRENGTH = "vibration_strength"

        fun getMode(executor: ShellExecutor) = when (executor.getVibrationStrength()) {
            VibrationLow.settingsValue -> VibrationLow
            VibrationMedium.settingsValue -> VibrationMedium
            VibrationHigh.settingsValue -> VibrationHigh
            VibrationOff.settingsValue -> VibrationOff
            else -> VibrationUnknown
        }

        fun getById(id: String?) = when (id) {
            VibrationLow.id -> VibrationLow
            VibrationMedium.id -> VibrationMedium
            VibrationHigh.id -> VibrationHigh
            VibrationOff.id -> VibrationOff
            else -> VibrationUnknown
        }

    }
}
