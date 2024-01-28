package de.langerhans.odintools.appsettings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dagger.Module
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.lifecycle.HiltViewModel
import de.langerhans.odintools.data.AppOverrideDao
import de.langerhans.odintools.data.AppOverrideEntity
import de.langerhans.odintools.models.ControllerStyle
import de.langerhans.odintools.models.L2R2Style
import de.langerhans.odintools.models.PerfModes
import de.langerhans.odintools.models.FanModes
import de.langerhans.odintools.models.NoChange
import de.langerhans.odintools.tools.DeviceUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AppOverridesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val appOverrideDao: AppOverrideDao,
    private val appOverrideMapper: AppOverrideMapper,
    private val deviceUtils: DeviceUtils
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppOverridesUiModel())
    val uiState: StateFlow<AppOverridesUiModel> = _uiState.asStateFlow()

    private val packageName = checkNotNull(savedStateHandle.get<String>("packageName"))
    private var initialControllerStyle = NoChange.KEY
    private var initialL2R2Style = NoChange.KEY
    private var initialPerfModes = NoChange.KEY
    private var initialfanModes = NoChange.KEY


    init {
        viewModelScope.launch {
            val app = withContext(Dispatchers.IO) {
                appOverrideDao.getForPackage(packageName)
            }

            val uiModel = if (app == null) {
                appOverrideMapper.mapEmptyOverride(packageName)
            } else {
                initialControllerStyle = app.controllerStyle ?: NoChange.KEY
                initialL2R2Style = app.l2R2Style ?: NoChange.KEY
                initialfanModes = app.fanModes ?: NoChange.KEY


                appOverrideMapper.mapAppOverride(app)
            }

            _uiState.update {
                it.copy(app = uiModel, isNewApp = app == null, deviceVersion = deviceUtils.getDeviceVersion())
            }
        }
    }

    fun saveClicked() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                appOverrideDao.save(AppOverrideEntity(
                    packageName = packageName,
                    controllerStyle = _uiState.value.app?.controllerStyle?.id,
                    l2R2Style = _uiState.value.app?.l2r2Style?.id,
                    perfModes = _uiState.value.app?.perfModes?.id,
                    fanModes = _uiState.value.app?.fanModes?.id
                ))
            }
        }
        _uiState.update {
            it.copy(navigateBack = true)
        }
    }

    fun deleteClicked() {
        _uiState.update {
            it.copy(showDeleteConfirmDialog = true)
        }
    }

    fun deleteDismissed() {
        _uiState.update {
            it.copy(showDeleteConfirmDialog = false)
        }
    }

    fun deleteConfirmed() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                appOverrideDao.deleteByPackageName(packageName)
            }
        }
        _uiState.update {
            it.copy(showDeleteConfirmDialog = false, navigateBack = true)
        }
    }

    fun controllerStyleSelected(key: String) {
        _uiState.update {
            it.copy(
                app = it.app?.copy(controllerStyle = ControllerStyle.getById(key)),
                hasUnsavedChanges = hasUnsavedChanges(controllerStyle = key)
            )
        }
    }

    fun l2R2StyleSelected(key: String) {
        _uiState.update {
            it.copy(
                app = it.app?.copy(l2r2Style = L2R2Style.getById(key)),
                hasUnsavedChanges = hasUnsavedChanges(l2R2Style = key)
            )
        }
    }

    fun perfModesSelected(key: String) {
        _uiState.update {
            it.copy(
                app = it.app?.copy(perfModes = PerfModes.getById(key)),
                hasUnsavedChanges = hasUnsavedChanges(perfModes = key)
            )
        }
    }
    fun fanModesSelected(key: String) {
        _uiState.update {
            it.copy(
                app = it.app?.copy(fanModes = FanModes.getById(key)),
                hasUnsavedChanges = hasUnsavedChanges(fanModes = key)
            )
        }
    }

    private fun hasUnsavedChanges(
        controllerStyle: String? = null,
        l2R2Style: String? = null,
        perfModes: String? = null,
        fanModes: String? = null
    ): Boolean {
        // Cascade through all possibly changed options. One should hit. If none hit the dev was an idiot.
        controllerStyle?.let {
            return it != initialControllerStyle
        }
        l2R2Style?.let {
            return it != initialL2R2Style
        }
        perfModes?.let {
            return it != initialPerfModes
        }
        fanModes?.let {
            return it != initialfanModes
        }

        return false
    }
}