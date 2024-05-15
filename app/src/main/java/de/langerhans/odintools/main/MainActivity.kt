package de.langerhans.odintools.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
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
import de.langerhans.odintools.BuildConfig
import de.langerhans.odintools.R
import de.langerhans.odintools.appsettings.AppOverrideListScreen
import de.langerhans.odintools.appsettings.AppOverridesScreen
import de.langerhans.odintools.tools.DeviceType.ODIN2
import de.langerhans.odintools.tools.SettingsRepo
import de.langerhans.odintools.ui.composables.ChargeLimitPreferenceDialog
import de.langerhans.odintools.ui.composables.CheckBoxDialogPreference
import de.langerhans.odintools.ui.composables.NotAnOdinDialog
import de.langerhans.odintools.ui.composables.OdinTopAppBar
import de.langerhans.odintools.ui.composables.PServerNotAvailableDialog
import de.langerhans.odintools.ui.composables.RemapButtonDialog
import de.langerhans.odintools.ui.composables.SaturationPreferenceDialog
import de.langerhans.odintools.ui.composables.SettingsHeader
import de.langerhans.odintools.ui.composables.SwitchPreference
import de.langerhans.odintools.ui.composables.SwitchableTriggerPreference
import de.langerhans.odintools.ui.composables.TriggerPreference
import de.langerhans.odintools.ui.composables.VibrationPreferenceDialog
import de.langerhans.odintools.ui.theme.OdinToolsTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
fun SettingsScreen(viewModel: MainViewModel = hiltViewModel(), navigateToOverrideList: () -> Unit) {
    val uiState: MainUiModel by viewModel.uiState.collectAsState()

    if (uiState.showPServerNotAvailableDialog) {
        PServerNotAvailableDialog()
    } else if (uiState.showIncompatibleDeviceDialog) {
        NotAnOdinDialog { viewModel.incompatibleDeviceDialogDismissed() }
    }

    if (uiState.showControllerStyleDialog) {
        CheckBoxDialogPreference(
            items = viewModel.controllerStyleOptions,
            title = R.string.controllerStyle,
            onCancel = {
                viewModel.hideControllerStylePreference()
            },
        ) {
            viewModel.updateControllerStyles(it)
            viewModel.hideControllerStylePreference()
        }
    }

    if (uiState.showL2r2StyleDialog) {
        CheckBoxDialogPreference(
            items = viewModel.l2r2StyleOptions,
            title = R.string.l2r2mode,
            onCancel = {
                viewModel.hideL2r2StylePreference()
            },
        ) {
            viewModel.updateL2r2Styles(it)
            viewModel.hideL2r2StylePreference()
        }
    }

    if (uiState.showSaturationDialog) {
        SaturationPreferenceDialog(
            initialValue = uiState.currentSaturation,
            onCancel = { viewModel.saturationDialogDismissed() },
            onSave = { viewModel.saveSaturation(it) },
        )
    }

    if (uiState.showVibrationDialog) {
        VibrationPreferenceDialog(
            initialValue = uiState.currentVibration,
            onCancel = { viewModel.vibrationDialogDismissed() },
            onSave = { viewModel.saveVibration(it) },
        )
    }

    if (uiState.showRemapButtonDialog) {
        RemapButtonDialog(
            initialValue = uiState.currentButtonKeyCode,
            onCancel = { viewModel.remapButtonDialogDismissed() },
            onReset = { viewModel.resetButtonKeyCode(uiState.currentButtonSetting) },
            onSave = { viewModel.saveButtonKeyCode(uiState.currentButtonSetting, it) },
        )
    }

    if (uiState.showChargeLimitDialog) {
        ChargeLimitPreferenceDialog(
            initialValue = uiState.currentChargeLimit,
            onCancel = { viewModel.chargeLimitDialogDismissed() },
            onSave = { viewModel.saveChargeLimit(it) },
        )
    }

    Scaffold(topBar = { OdinTopAppBar(deviceVersion = uiState.deviceVersion) }) { contentPadding ->
        Column(
            modifier = Modifier
                .padding(top = contentPadding.calculateTopPadding()) // Top app bar padding
                .padding(end = 8.dp) // Extra padding cause of GameAssist bar overlay
                .verticalScroll(rememberScrollState()),
        ) {
            SettingsHeader(R.string.appOverrides)
            SwitchableTriggerPreference(
                icon = R.drawable.ic_app_settings,
                title = R.string.appOverrides,
                description = R.string.appOverridesDescription,
                state = uiState.appOverridesEnabled,
                onClick = navigateToOverrideList,
            ) { newValue ->
                viewModel.appOverridesEnabled(newValue)
            }
            SwitchPreference(
                icon = R.drawable.ic_more_time,
                title = R.string.overrideDelay,
                description = R.string.overrideDelayDescription,
                state = uiState.overrideDelayEnabled,
            ) {
                viewModel.overrideDelayEnabled(it)
            }
            SettingsHeader(R.string.quickSettings)
            TriggerPreference(
                icon = R.drawable.ic_face_buttons,
                title = R.string.controllerStyle,
                description = R.string.controllerStyleDesc,
            ) {
                viewModel.showControllerStylePreference()
            }
            TriggerPreference(
                icon = R.drawable.ic_sliders,
                title = R.string.l2r2mode,
                description = R.string.l2r2modeDesc,
            ) {
                viewModel.showL2r2StylePreference()
            }
            SettingsHeader(R.string.buttons)
            SwitchPreference(
                icon = R.drawable.ic_home,
                title = R.string.singlePressHome,
                description = R.string.singlePressHomeDescription,
                state = uiState.singlePressHomeEnabled,
            ) {
                viewModel.updateSinglePressHomePreference(it)
            }
            if (uiState.deviceType == ODIN2) {
                TriggerPreference(
                    icon = R.drawable.ic_gamepad,
                    title = R.string.m1Button,
                    description = R.string.remapButtonDescription,
                ) {
                    viewModel.remapButtonClicked(SettingsRepo.KEY_CUSTOM_M1_VALUE)
                }
                TriggerPreference(
                    icon = R.drawable.ic_gamepad,
                    title = R.string.m2Button,
                    description = R.string.remapButtonDescription,
                ) {
                    viewModel.remapButtonClicked(SettingsRepo.KEY_CUSTOM_M2_VALUE)
                }
            }
            SettingsHeader(name = R.string.display)
            TriggerPreference(
                icon = R.drawable.ic_palette,
                title = R.string.saturation,
                description = R.string.saturationDescription,
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
                    onClick = { viewModel.vibrationClicked() },
                ) {
                    viewModel.updateVibrationPreference(it)
                }
                SettingsHeader(R.string.battery)
                SwitchableTriggerPreference(
                    icon = R.drawable.ic_electrical_services,
                    title = R.string.chargeLimit,
                    description = R.string.chargeLimitDescription,
                    state = uiState.chargeLimitEnabled,
                    onClick = { viewModel.chargeLimitClicked() },
                ) {
                    viewModel.updateChargeLimitPreference(it)
                }
            }
            TriggerPreference(
                icon = R.drawable.ic_file_save,
                title = R.string.dumpLogToFile,
                description = R.string.dumpLogToFileDescription,
            ) {
                viewModel.dumpLogToFile()
            }

            // Navigation bar padding
            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.systemBars))
        }
    }
}
