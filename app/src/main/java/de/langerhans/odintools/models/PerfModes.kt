package de.langerhans.odintools.models

import androidx.annotation.StringRes
import de.langerhans.odintools.R
import de.langerhans.odintools.tiles.PerfModesService
import de.langerhans.odintools.tools.ShellExecutor

sealed class PerfModes(
    val id: String,
    val settingsValue: Int,
    @StringRes val textRes: Int
) {
    data object Standard : PerfModes("standard", 0, R.string.standard)
    data object Performance : PerfModes("smart", 1, R.string.performance)
    data object HighPerformance : PerfModes("sport", 2, R.string.highperformance)
    data object Unknown : PerfModes("unknown", -1, R.string.unknown)

    fun enable(executor: ShellExecutor) {
        if (this != Unknown) {
            executor.setSystemSetting(PERFORMANCE_MODE, settingsValue)
        }
    }

    companion object {
        private const val PERFORMANCE_MODE = "performance_mode"

        fun getStyle(executor: ShellExecutor) =
            when (executor.getSystemSetting(PERFORMANCE_MODE, Standard.settingsValue)) {
                Standard.settingsValue -> Standard
                Performance.settingsValue -> Performance
                HighPerformance.settingsValue -> HighPerformance
                else -> Unknown
            }

        fun getById(id: String?) = when(id) {
            Standard.id -> Standard
            Performance.id -> Performance
            HighPerformance.id -> HighPerformance
            else -> Unknown
        }
    }
}