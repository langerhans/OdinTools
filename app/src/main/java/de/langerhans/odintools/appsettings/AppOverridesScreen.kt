package de.langerhans.odintools.appsettings

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import de.langerhans.odintools.R
import de.langerhans.odintools.models.ControllerStyle.Disconnect
import de.langerhans.odintools.models.ControllerStyle.Odin
import de.langerhans.odintools.models.ControllerStyle.Xbox
import de.langerhans.odintools.models.FanMode.Off
import de.langerhans.odintools.models.FanMode.Quiet
import de.langerhans.odintools.models.FanMode.Smart
import de.langerhans.odintools.models.FanMode.Sport
import de.langerhans.odintools.models.L2R2Style.Analog
import de.langerhans.odintools.models.L2R2Style.Both
import de.langerhans.odintools.models.L2R2Style.Digital
import de.langerhans.odintools.models.NoChange
import de.langerhans.odintools.models.PerfMode
import de.langerhans.odintools.models.PerfMode.HighPerformance
import de.langerhans.odintools.models.PerfMode.Performance
import de.langerhans.odintools.models.PerfMode.Standard
import de.langerhans.odintools.tools.DeviceType
import de.langerhans.odintools.tools.DeviceUtils
import de.langerhans.odintools.ui.composables.DeleteConfirmDialog
import de.langerhans.odintools.ui.composables.LargeDropdownMenu
import de.langerhans.odintools.ui.composables.OdinTopAppBar
import de.langerhans.odintools.ui.composables.VibrationPreferenceDialog
import de.langerhans.odintools.ui.theme.Typography

@Composable
fun AppOverridesScreen(viewModel: AppOverridesViewModel = hiltViewModel(), navigateBack: () -> Unit) {
    val uiState: AppOverridesUiModel by viewModel.uiState.collectAsState()
    val app = uiState.app ?: return run {
        // Shouldn't happen
    }

    Scaffold(topBar = { OdinTopAppBar(deviceVersion = uiState.deviceVersion) }) { contentPadding ->
        LaunchedEffect(uiState.navigateBack) {
            if (uiState.navigateBack) navigateBack()
        }

        if (uiState.showDeleteConfirmDialog) {
            DeleteConfirmDialog(
                onDelete = { viewModel.deleteConfirmed() },
                onDismiss = { viewModel.deleteDismissed() },
            )
        }

        Row(
            modifier = Modifier
                .fillMaxHeight()
                .padding(contentPadding)
                .padding(16.dp),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(0.3f)
                    .align(Alignment.CenterVertically),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f),
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Image(
                            painter = rememberDrawablePainter(drawable = app.appIcon),
                            contentDescription = null,
                            modifier = Modifier
                                .size(72.dp)
                                .padding(bottom = 8.dp),
                        )
                        Text(
                            text = app.appName,
                            modifier = Modifier.padding(bottom = 8.dp),
                        )
                    }
                }
                Button(
                    onClick = { viewModel.saveClicked() },
                    enabled = uiState.hasUnsavedChanges,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                ) {
                    Text(text = stringResource(id = R.string.save))
                }
                FilledTonalButton(
                    onClick = navigateBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = if (uiState.isNewApp) 0.dp else 8.dp),
                ) {
                    Text(text = stringResource(id = R.string.cancel))
                }
                if (uiState.isNewApp.not()) {
                    OutlinedButton(
                        onClick = { viewModel.deleteClicked() },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            color = Color.Red,
                            text = stringResource(id = R.string.deleteOverride),
                        )
                    }
                }
            }
            VerticalDivider(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxHeight()
                    .width(1.dp),
            )
            Column(
                modifier = Modifier
                    .weight(0.7f)
                    .verticalScroll(rememberScrollState()),
            ) {
                OverrideSpinnerRow(
                    label = R.string.controllerStyle,
                    spinnerItems = listOf(
                        NoChange.KEY to stringResource(id = NoChange.textRes),
                        Odin.id to stringResource(id = Odin.textRes),
                        Xbox.id to stringResource(id = Xbox.textRes),
                        Disconnect.id to stringResource(id = Disconnect.textRes),
                    ),
                    initialSelection = uiState.app?.controllerStyle?.id ?: NoChange.KEY,
                    onSelectionChanged = { viewModel.controllerStyleSelected(it) },
                    modifier = Modifier.padding(bottom = 16.dp),
                )
                OverrideSpinnerRow(
                    label = R.string.l2r2mode,
                    spinnerItems = listOf(
                        NoChange.KEY to stringResource(id = NoChange.textRes),
                        Analog.id to stringResource(id = Analog.textRes),
                        Digital.id to stringResource(id = Digital.textRes),
                        Both.id to stringResource(id = Both.textRes),
                    ),
                    initialSelection = uiState.app?.l2r2Style?.id ?: NoChange.KEY,
                    onSelectionChanged = { viewModel.l2R2StyleSelected(it) },
                    modifier = Modifier.padding(bottom = 16.dp),
                )
                OverrideSpinnerRow(
                    label = R.string.perfMode,
                    spinnerItems = listOf(
                        NoChange.KEY to stringResource(id = NoChange.textRes),
                        Standard.id to stringResource(id = Standard.textRes),
                        Performance.id to stringResource(id = Performance.textRes),
                        HighPerformance.id to stringResource(id = HighPerformance.textRes),
                    ),
                    initialSelection = uiState.app?.perfMode?.id ?: NoChange.KEY,
                    onSelectionChanged = { viewModel.perfModeSelected(it) },
                    modifier = Modifier.padding(bottom = 16.dp),
                )
//                if (viewModel.getDeviceType() == DeviceType.ODIN2) {
                    Column {
                        VibrationPreferenceRow(
                            label = stringResource(id = R.string.vibrationStrength),
                            initialValue = uiState.app?.vibrationStrength?: 0,
                            onSave = { newValue ->
                                viewModel.vibrationStrengthSelected(newValue)
                            }
                        )
                    }
//                }
                if (uiState.app?.perfMode != null && uiState.app?.perfMode != PerfMode.Unknown) {
                    OverrideSpinnerRow(
                        label = R.string.fanMode,
                        spinnerItems = listOf(
                            Off.id to stringResource(id = Off.textRes),
                            Quiet.id to stringResource(id = Quiet.textRes),
                            Smart.id to stringResource(id = Smart.textRes),
                            Sport.id to stringResource(id = Sport.textRes),
                        ).filterNot { it.first in uiState.disabledFanModeKeys },
                        initialSelection = uiState.app?.fanMode?.id ?: Smart.id,
                        onSelectionChanged = { viewModel.fanModeSelected(it) },
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                    Row(
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(),
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_info),
                            contentDescription = null,
                            modifier = Modifier
                                .size(18.dp)
                                .padding(end = 4.dp),
                        )
                        Text(
                            style = Typography.labelSmall,
                            text = stringResource(id = R.string.fanModeRequired),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Spinner(options: List<Pair<String, String>>, initialSelection: String, onSelectionChanged: (key: String) -> Unit) {
    val initial = options.indexOfFirst { it.first == initialSelection }.coerceAtLeast(0)
    var selectedIndex by remember { mutableIntStateOf(initial) }

    var optionsCount by remember { mutableIntStateOf(options.size) }
    if (optionsCount != options.size) {
        // Reset if our options have changed
        optionsCount = options.size
        selectedIndex = options.indexOfFirst { it.first == initialSelection }.coerceAtLeast(0)
    }

    LargeDropdownMenu(
        items = options,
        onItemSelected = { index: Int, item: Pair<String, String> ->
            selectedIndex = index
            onSelectionChanged(item.first)
        },
        selectedIndex = selectedIndex,
        selectedItemToString = { it.second },
    )
}

@Composable
fun OverrideSpinnerRow(
    @StringRes label: Int,
    spinnerItems: List<Pair<String, String>>,
    initialSelection: String,
    onSelectionChanged: (key: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        Text(
            text = stringResource(id = label),
            modifier = Modifier
                .weight(1f)
                .padding(end = 16.dp),
        )
        Spinner(
            options = spinnerItems,
            initialSelection = initialSelection,
            onSelectionChanged = onSelectionChanged,
        )
    }
}

@Composable
fun VibrationPreferenceRow(
    label: String,
    initialValue: Int,
    onSave: (newValue: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDialog by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .padding(8.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier
                .weight(1f)
                .padding(end = 16.dp),
        )
        OutlinedButton(
            onClick = { showDialog = true }
        ) {
            Text(text = "$initialValue")
        }
    }

    if (showDialog) {
        VibrationPreferenceDialog(
            initialValue = initialValue,
            onCancel = { showDialog = false },
            onSave = { newValue ->
                onSave(newValue)
                showDialog = false
            }
        )
    }
}
