package de.langerhans.odintools.main

import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import de.langerhans.odintools.models.ControllerStyle
import de.langerhans.odintools.models.L2R2Style
import de.langerhans.odintools.tools.DeviceType
import de.langerhans.odintools.tools.DeviceType.ODIN2

data class MainUiModel(
    val deviceType: DeviceType = ODIN2,
    val deviceVersion: String = "",
    val showIncompatibleDeviceDialog: Boolean = false,
    val showPServerNotAvailableDialog: Boolean = false,

    val singlePressHomeEnabled: Boolean = false,
    val appOverridesEnabled: Boolean = true,
    val overrideDelayEnabled: Boolean = false,

    val showControllerStyleDialog: Boolean = false,
    val showL2r2StyleDialog: Boolean = false,

    val showSaturationDialog: Boolean = false,
    val currentSaturation: Float = 1.0f,

    val showVideoOutputOverrideDialog: Boolean = false,
    val videoOutputOverrideEnabled: Boolean = false,
    val videoOutputControllerStyle: ControllerStyle = ControllerStyle.Unknown,
    val videoOutputL2R2Style: L2R2Style = L2R2Style.Unknown,

    val showVibrationDialog: Boolean = false,
    val vibrationEnabled: Boolean = false,
    val currentVibration: Int = 0,

    val showRemapButtonDialog: Boolean = false,
    val currentButtonSetting: String = "",
    val currentButtonKeyCode: Int = 0,

    val showChargeLimitDialog: Boolean = false,
    val chargeLimitEnabled: Boolean = false,
    val currentChargeLimit: ClosedRange<Int> = 20..80,
)

class CheckboxPreferenceUiModel(
    val key: String,
    @StringRes val text: Int,
    initialChecked: Boolean = false,
) {
    var checked by mutableStateOf(initialChecked)
}
