package de.langerhans.odintools.models

import androidx.annotation.StringRes
import de.langerhans.odintools.R
import de.langerhans.odintools.tiles.L2R2TileService
import de.langerhans.odintools.tools.ShellExecutor

sealed class L2R2Style(
    val id: String,
    val settingsValue: Int,
    @StringRes val textRes: Int
) {
    data object Analog : L2R2Style("analog", 0, R.string.analog)
    data object Digital : L2R2Style("digital", 1, R.string.digitial)
    data object Both : L2R2Style("both", 2, R.string.both)
    data object Unknown : L2R2Style("unknown", -1, R.string.unknown)

    fun enable(executor: ShellExecutor) {
        if (this != Unknown) {
            executor.setSystemSetting(TRIGGER_INPUT_MODE, settingsValue)
        }
    }

    companion object {
        private const val TRIGGER_INPUT_MODE = "trigger_input_mode"

        fun getStyle(executor: ShellExecutor) =
            when (executor.getSystemSetting(TRIGGER_INPUT_MODE, Analog.settingsValue)) {
                Analog.settingsValue -> Analog
                Digital.settingsValue -> Digital
                Both.settingsValue -> Both
                else -> Unknown
            }

        fun getById(id: String?) = when(id) {
            Analog.id -> Analog
            Digital.id -> Digital
            Both.id -> Both
            else -> Unknown
        }
    }
}