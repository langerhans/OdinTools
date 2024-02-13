package de.langerhans.odintools.models

import androidx.annotation.StringRes
import de.langerhans.odintools.R
import de.langerhans.odintools.tools.ShellExecutor

sealed class ControllerStyle(
    val id: String,
    private val tempAbxyLayout: Int,
    private val noCreateGamepadLayout: Int,
    private val flipButtonLayout: Int,
    @StringRes val textRes: Int,
) {
    data object Xbox : ControllerStyle("xbox", 0, 0, 1, R.string.xbox)
    data object Odin : ControllerStyle("odin", 1, 0, 0, R.string.odin)
    data object Disconnect : ControllerStyle("disconnect", 2, 1, 0, R.string.disconnect)
    data object Unknown : ControllerStyle("unknown", -1, -1, -1, R.string.unknown)

    fun enable(executor: ShellExecutor) {
        if (this != Unknown) {
            executor.setIntSystemSetting(TEMP_ABXY_LAYOUT_MODE, tempAbxyLayout)
            executor.setIntSystemSetting(NO_CREATE_GAMEPAD_BUTTON_LAYOUT, noCreateGamepadLayout)
            executor.setIntSystemSetting(FLIP_BUTTON_LAYOUT, flipButtonLayout)
        }
    }

    companion object {
        private const val TEMP_ABXY_LAYOUT_MODE = "temp_abxy_layout_mode"
        private const val NO_CREATE_GAMEPAD_BUTTON_LAYOUT = "no_create_gamepad_button_layout"
        private const val FLIP_BUTTON_LAYOUT = "flip_button_layout"

        fun getStyle(executor: ShellExecutor): ControllerStyle {
            return if (executor.getIntSystemSetting(NO_CREATE_GAMEPAD_BUTTON_LAYOUT, 0) == 1) {
                Disconnect
            } else if (executor.getIntSystemSetting(FLIP_BUTTON_LAYOUT, 0) == 1) {
                Xbox
            } else {
                Odin
            }
        }

        fun getById(id: String?) = when (id) {
            Xbox.id -> Xbox
            Odin.id -> Odin
            Disconnect.id -> Disconnect
            else -> Unknown
        }
    }
}
