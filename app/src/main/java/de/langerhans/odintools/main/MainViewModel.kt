package de.langerhans.odintools.main

import android.view.KeyEvent
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import de.langerhans.odintools.R
import de.langerhans.odintools.data.SharedPrefsRepo
import de.langerhans.odintools.models.ControllerStyle.*
import de.langerhans.odintools.models.L2R2Style.*
import de.langerhans.odintools.tools.DeviceType.ODIN2
import de.langerhans.odintools.tools.DeviceUtils
import de.langerhans.odintools.tools.ShellExecutor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    deviceUtils: DeviceUtils,
    private val executor: ShellExecutor,
    private val prefs: SharedPrefsRepo,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiModel())
    val uiState: StateFlow<MainUiModel> = _uiState.asStateFlow()

    private var _controllerStyleOptions = getCurrentControllerStyles().toMutableStateList()
    val controllerStyleOptions: List<CheckboxPreferenceUiModel>
        get() = _controllerStyleOptions

    private var _l2r2StyleOptions = getCurrentL2r2Styles().toMutableStateList()
    val l2r2StyleOptions: List<CheckboxPreferenceUiModel>
        get() = _l2r2StyleOptions

    init {
        executor.applyRequiredSettings()

        val deviceType = deviceUtils.getDeviceType()
        val preventHomePressSetting = executor.getBooleanSystemSetting("prevent_press_home_accidentally", true)
        val vibrationOnSetting = executor.getBooleanSystemSetting("vibrate_on", true)
        val vibrationStrength = executor.getVibrationStrength()

        _uiState.update { _ ->
            MainUiModel(
                deviceType = deviceType,
                deviceVersion = deviceUtils.getDeviceVersion(),
                showNotAnOdinDialog = deviceType != ODIN2,
                singleHomeEnabled = !preventHomePressSetting,
                showPServerNotAvailableDialog = !executor.pServerAvailable,
                overrideDelayEnabled = prefs.overrideDelay,
                vibrationEnabled = vibrationOnSetting,
                currentVibration = vibrationStrength,
            )
        }
    }

    fun incompatibleDeviceDialogDismissed() {
        _uiState.update { current ->
            current.copy(showNotAnOdinDialog = false)
        }
    }

    fun updateSingleHomePreference(newValue: Boolean) {
        // Invert here as prevent == double press
        executor.setBooleanSystemSetting("prevent_press_home_accidentally", !newValue)

        _uiState.update { current ->
            current.copy(singleHomeEnabled = newValue)
        }
    }

    fun showControllerStylePreference() {
        _controllerStyleOptions = getCurrentControllerStyles().toMutableStateList()
        _uiState.update { current ->
            current.copy(showControllerStyleDialog = true)
        }
    }

    fun hideControllerStylePreference() {
        _uiState.update { current ->
            current.copy(showControllerStyleDialog = false)
        }
    }

    private fun getCurrentControllerStyles(): List<CheckboxPreferenceUiModel> {
        val disabled = prefs.disabledControllerStyle
        return listOf(
            CheckboxPreferenceUiModel(Xbox.id, R.string.xbox, disabled != Xbox.id),
            CheckboxPreferenceUiModel(Odin.id, R.string.odin, disabled != Odin.id),
            CheckboxPreferenceUiModel(Disconnect.id, R.string.disconnect, disabled != Disconnect.id),
        )
    }

    fun updateControllerStyles(models: List<CheckboxPreferenceUiModel>) {
        prefs.disabledControllerStyle = models.find { it.checked.not() }?.key
    }

    fun showL2r2StylePreference() {
        _l2r2StyleOptions = getCurrentL2r2Styles().toMutableStateList()
        _uiState.update { current ->
            current.copy(showL2r2StyleDialog = true)
        }
    }

    fun hideL2r2StylePreference() {
        _uiState.update { current ->
            current.copy(showL2r2StyleDialog = false)
        }
    }

    private fun getCurrentL2r2Styles(): List<CheckboxPreferenceUiModel> {
        val disabled = prefs.disabledL2r2Style
        return listOf(
            CheckboxPreferenceUiModel(Analog.id, R.string.analog, disabled != Analog.id),
            CheckboxPreferenceUiModel(Digital.id, R.string.digital, disabled != Digital.id),
            CheckboxPreferenceUiModel(Both.id, R.string.both, disabled != Both.id),
        )
    }

    fun updateL2r2Styles(models: List<CheckboxPreferenceUiModel>) {
        prefs.disabledL2r2Style = models.find { it.checked.not() }?.key
    }

    fun saturationClicked() {
        _uiState.update {
            it.copy(showSaturationDialog = true, currentSaturation = prefs.saturationOverride)
        }
    }

    fun saturationDialogDismissed() {
        _uiState.update {
            it.copy(showSaturationDialog = false)
        }
    }

    fun saveSaturation(newValue: Float) {
        prefs.saturationOverride = newValue
        executor.setSfSaturation(newValue)
        _uiState.update {
            it.copy(showSaturationDialog = false)
        }
    }

    fun updateVibrationPreference(newValue: Boolean) {
        executor.setBooleanSystemSetting("vibrate_on", newValue)

        _uiState.update { current ->
            current.copy(vibrationEnabled = newValue)
        }
    }

    fun vibrationClicked() {
        _uiState.update {
            it.copy(showVibrationDialog = true, currentVibration = executor.getVibrationStrength())
        }
    }

    fun vibrationDialogDismissed() {
        _uiState.update {
            it.copy(showVibrationDialog = false)
        }
    }

    fun saveVibration(newValue: Int) {
        prefs.vibrationStrength = newValue
        executor.setVibrationStrength(newValue)
        _uiState.update {
            it.copy(showVibrationDialog = false, currentVibration = newValue)
        }
    }

    fun remapButtonClicked(setting: String) {
        _uiState.update {
            it.copy(
                showRemapButtonDialog = true,
                currentButtonSetting = setting,
                currentButtonKeyCode = executor.getIntSystemSetting(setting, 0),
            )
        }
    }

    fun remapButtonDialogDismissed() {
        _uiState.update {
            it.copy(showRemapButtonDialog = false)
        }
    }

    private fun getDefaultKeyCode(setting: String): Int {
        if (setting == "remap_custom_to_m1_value") {
            return KeyEvent.KEYCODE_BUTTON_C
        }
        if (setting == "remap_custom_to_m2_value") {
            return KeyEvent.KEYCODE_BUTTON_Z
        }
        return KeyEvent.KEYCODE_UNKNOWN
    }

    fun resetButtonKeyCode(setting: String) {
        val newValue: Int = getDefaultKeyCode(setting)
        executor.setIntSystemSetting(setting, newValue)
        _uiState.update {
            it.copy(showRemapButtonDialog = false)
        }
    }

    fun saveButtonKeyCode(setting: String, newValue: Int) {
        executor.setIntSystemSetting(setting, newValue)
        _uiState.update {
            it.copy(showRemapButtonDialog = false)
        }
    }

    fun appOverridesEnabled(newValue: Boolean) {
        prefs.appOverridesEnabled = newValue
        _uiState.update {
            it.copy(appOverridesEnabled = newValue)
        }
    }

    fun overrideDelayEnabled(newValue: Boolean) {
        prefs.overrideDelay = newValue
        _uiState.update {
            it.copy(overrideDelayEnabled = newValue)
        }
    }
}
