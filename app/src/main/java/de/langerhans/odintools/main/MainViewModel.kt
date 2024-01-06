package de.langerhans.odintools.main

import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import de.langerhans.odintools.R
import de.langerhans.odintools.data.SharedPrefsRepo
import de.langerhans.odintools.models.ControllerStyle.*
import de.langerhans.odintools.models.L2R2Style.*
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
    private val prefs: SharedPrefsRepo
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
        executor.enableA11yService()
        executor.grantAllAppsPermission()

        val isOdin2 = deviceUtils.isOdin2()
        val preventHomePressSetting = executor.getBooleanSystemSetting("prevent_press_home_accidentally", true)

        _uiState.update { _ ->
            MainUiModel(
                deviceVersion = deviceUtils.getDeviceVersion(),
                showNotAnOdinDialog = !isOdin2,
                singleHomeEnabled = !preventHomePressSetting,
                showPServerNotAvailableDialog = !deviceUtils.isPServerAvailable()
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
            CheckboxPreferenceUiModel(Disconnect.id, R.string.disconnect, disabled != Disconnect.id)
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
            CheckboxPreferenceUiModel(Digital.id, R.string.digitial, disabled != Digital.id),
            CheckboxPreferenceUiModel(Both.id, R.string.both, disabled != Both.id)
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

    fun appOverridesEnabled(newValue: Boolean) {
        prefs.appOverridesEnabled = newValue
        _uiState.update {
            it.copy(appOverridesEnabled = newValue)
        }
    }
}