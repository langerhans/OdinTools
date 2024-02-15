package de.langerhans.odintools.models

import androidx.annotation.StringRes
import de.langerhans.odintools.R
import de.langerhans.odintools.tools.ShellExecutor

sealed class PerfMode(
    val id: String,
    val settingsValue: Int,
    @StringRes val textRes: Int,
) {
    data object Standard : PerfMode("standard", 0, R.string.standard)
    data object Performance : PerfMode("performance", 1, R.string.performance)
    data object HighPerformance : PerfMode("highPerformance", 2, R.string.highPerformance)
    data object Unknown : PerfMode("unknown", -1, R.string.unknown)

    fun enable(executor: ShellExecutor) {
        if (this != Unknown) {
            executor.setIntSystemSetting(PERFORMANCE_MODE, settingsValue)
        }
    }

    companion object {
        private const val PERFORMANCE_MODE = "performance_mode"

        fun getMode(executor: ShellExecutor) = when (executor.getIntSystemSetting(PERFORMANCE_MODE, Standard.settingsValue)) {
            Standard.settingsValue -> Standard
            Performance.settingsValue -> Performance
            HighPerformance.settingsValue -> HighPerformance
            else -> Unknown
        }

        fun getById(id: String?) = when (id) {
            Standard.id -> Standard
            Performance.id -> Performance
            HighPerformance.id -> HighPerformance
            else -> Unknown
        }
    }
}
