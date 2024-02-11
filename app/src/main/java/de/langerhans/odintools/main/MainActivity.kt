package de.langerhans.odintools.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import de.langerhans.odintools.R
import de.langerhans.odintools.appsettings.AppOverrideListScreen
import de.langerhans.odintools.appsettings.AppOverridesScreen
import de.langerhans.odintools.tools.DeviceType.ODIN2
import de.langerhans.odintools.ui.composables.*
import de.langerhans.odintools.ui.theme.OdinToolsTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OdinToolsTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "settings") {
                        composable("settings") {
                            SettingsScreen { navController.navigate("override/list") }
                        }
                        composable("override/list") {
                            AppOverrideListScreen { navController.navigate("override/$it") }
                        }
                        composable("override/{packageName}") {
                            AppOverridesScreen {
                                navController.popBackStack()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(
    viewModel: MainViewModel = hiltViewModel(),
    navigateToOverrideList: () -> Unit
) {
    val uiState: MainUiModel by viewModel.uiState.collectAsState()

    if (uiState.showPServerNotAvailableDialog) {
        PServerNotAvailableDialog()
    } else if (uiState.showNotAnOdinDialog) {
        NotAnOdinDialog { viewModel.incompatibleDeviceDialogDismissed() }
    }

    if (uiState.showControllerStyleDialog) {
        CheckBoxDialogPreference(items = viewModel.controllerStyleOptions, onCancel = {
            viewModel.hideControllerStylePreference()
        }) {
            viewModel.updateControllerStyles(it)
            viewModel.hideControllerStylePreference()
        }
    }

    if (uiState.showL2r2StyleDialog) {
        CheckBoxDialogPreference(items = viewModel.l2r2StyleOptions, onCancel = {
            viewModel.hideL2r2StylePreference()
        }) {
            viewModel.updateL2r2Styles(it)
            viewModel.hideL2r2StylePreference()
        }
    }

    if (uiState.showSaturationDialog) {
        SaturationPreferenceDialog(
            initialValue = uiState.currentSaturation,
            onCancel = { viewModel.saturationDialogDismissed() },
            onSave = { viewModel.saveSaturation(it) }
        )
    }

    if (uiState.showVibrationDialog) {
        VibrationPreferenceDialog(
            initialValue = uiState.currentVibration,
            onCancel = { viewModel.vibrationDialogDismissed() },
            onSave = { viewModel.saveVibration(it) }
        )
    }

    if (uiState.showRemapButtonDialog) {
        RemapButtonDialog(
            initialValue = uiState.currentButtonKeyCode,
            onCancel = { viewModel.remapButtonDialogDismissed() },
            onReset = { viewModel.resetButtonKeyCode(uiState.currentButtonSetting) },
            onSave = { viewModel.saveButtonKeyCode(uiState.currentButtonSetting, it) }
        )
    }

    Scaffold(topBar = {OdinTopAppBar(deviceVersion = uiState.deviceVersion)}) { contentPadding ->
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .padding(end = 8.dp) // Extra padding cause of GameAssist bar overlay
                .verticalScroll(rememberScrollState())
        ) {
            SettingsHeader(R.string.appOverrides)
            SwitchableTriggerPreference(
                icon = R.drawable.ic_app_settings,
                title = R.string.appOverrides,
                description = R.string.appOverridesDescription,
                state = uiState.appOverridesEnabled,
                onClick = navigateToOverrideList
            ) { newValue ->
                viewModel.appOverridesEnabled(newValue)
            }
            SwitchPreference(
                icon = R.drawable.ic_more_time,
                title = R.string.overrideDelay,
                description = R.string.overrideDelayDesc,
                state = uiState.overrideDelayEnabled
            ) {
                viewModel.overrideDelayEnabled(it)
            }
            SettingsHeader(R.string.quicksettings)
            TriggerPreference(
                icon = R.drawable.ic_controllerstyle,
                title = R.string.controllerStyle,
                description = R.string.controllerStyleDesc
            ) {
                viewModel.showControllerStylePreference()
            }
            TriggerPreference(
                icon = R.drawable.ic_sliders,
                title = R.string.l2r2mode,
                description = R.string.l2r2modeDesc
            ) {
                viewModel.showL2r2StylePreference()
            }
            SettingsHeader(R.string.buttons)
            SwitchPreference(
                icon = R.drawable.ic_home,
                title = R.string.doubleHomeTitle,
                description = R.string.doubleHomeDescription,
                state = uiState.singleHomeEnabled
            ) {
                viewModel.updateSingleHomePreference(it)
            }
            TriggerPreference(
                icon = R.drawable.ic_gamepad,
                title = R.string.m1Button,
                description = R.string.remapButtonDescription
            ) {
                viewModel.remapButtonClicked("remap_custom_to_m1_value")
            }
            TriggerPreference(
                icon = R.drawable.ic_gamepad,
                title = R.string.m2Button,
                description = R.string.remapButtonDescription
            ) {
                viewModel.remapButtonClicked("remap_custom_to_m2_value")
            }
            SettingsHeader(name = R.string.display)
            TriggerPreference(
                icon = R.drawable.ic_palette,
                title = R.string.saturation,
                description = R.string.saturationDescription
            ) {
                viewModel.saturationClicked()
            }
            if (uiState.deviceType == ODIN2) {
                SettingsHeader(name = R.string.haptics)
                SwitchableTriggerPreference(
                    icon = R.drawable.ic_vibration,
                    title = R.string.vibrationStrength,
                    description = R.string.vibrationStrengthDescription,
                    state = uiState.vibrationEnabled,
                    onClick = { viewModel.vibrationClicked() }
                ) {
                    viewModel.updateVibrationPreference(it)
                }
            }
        }
    }
}
