package de.langerhans.odintools.appsettings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.langerhans.odintools.data.AppOverrideDao
import de.langerhans.odintools.tools.DeviceUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppOverrideListViewModel @Inject constructor(
    private val appOverrideDao: AppOverrideDao,
    private val appOverrideMapper: AppOverrideMapper,
    private val deviceUtils: DeviceUtils,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppOverrideListUiModel())
    val uiState: StateFlow<AppOverrideListUiModel> = _uiState.asStateFlow()

    private var existingOverrides = emptyList<String>()

    init {
        viewModelScope.launch {
            appOverrideDao.getAll()
                .flowOn(Dispatchers.IO)
                .collect { overrides ->
                    existingOverrides = overrides.map { it.packageName }
                    _uiState.update {
                        it.copy(
                            overrideList = appOverrideMapper.mapAppOverrides(overrides),
                            overrideCandidates = appOverrideMapper.mapOverrideCandidates(overrides),
                            deviceVersion = deviceUtils.getDeviceVersion(),
                        )
                    }
                }
        }
    }

    fun addClicked() {
        _uiState.update {
            it.copy(showAppSelectDialog = true)
        }
    }

    fun dismissAppSelectDialog() {
        _uiState.update {
            it.copy(showAppSelectDialog = false)
        }
    }

    /**
     * System settings:
     * performance_mode: standard 0, performance 1, high performance 2
     * fan_mode: disabled 0, quiet 1, (balance 2), (performance 3), smart 4, sport 5, custom 6
     */
}
