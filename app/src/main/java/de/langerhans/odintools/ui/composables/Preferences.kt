package de.langerhans.odintools.ui.composables

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.langerhans.odintools.R
import de.langerhans.odintools.main.CheckboxPreferenceUiModel

@Composable
fun SettingsHeader(@StringRes name: Int) {
    Text(
        text = stringResource(id = name),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, bottom = 2.dp, top = 8.dp)
    )
}

@Composable
fun PreferenceDescription(
    @DrawableRes icon: Int,
    @StringRes title: Int,
    @StringRes description: Int,
    modifier: Modifier = Modifier
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        Image(painter = painterResource(id = icon), contentDescription = null)
        Column(
            modifier = Modifier.padding(start = 16.dp)
        ) {
            Text(
                text = stringResource(id = title), modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = stringResource(id = description), style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun SwitchPreference(
    @DrawableRes icon: Int,
    @StringRes title: Int,
    @StringRes description: Int,
    state: Boolean,
    onChange: (newValue: Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onChange(state.not())
            }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        PreferenceDescription(
            icon = icon,
            title = title,
            description = description,
            modifier = Modifier
                .weight(1f)
                .padding(end = 16.dp)
        )
        Switch(
            checked = state,
            onCheckedChange = {
                onChange(it)
            },
        )
    }
}

@Composable
fun SwitchableTriggerPreference(
    @DrawableRes icon: Int,
    @StringRes title: Int,
    @StringRes description: Int,
    state: Boolean,
    onClick: () -> Unit,
    onChange: (newValue: Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        PreferenceDescription(
            icon = icon,
            title = title,
            description = description,
            modifier = Modifier
                .weight(1f)
                .padding(end = 16.dp)
        )
        Row(Modifier.height(IntrinsicSize.Min)) {
            Divider(
                modifier = Modifier
                    .width(17.dp)
                    .padding(end = 16.dp)
                    .fillMaxHeight()
            )
            Switch(
                checked = state,
                onCheckedChange = {
                    onChange(it)
                },
            )
        }
    }
}

@Composable
fun TriggerPreference(
    @DrawableRes icon: Int,
    @StringRes title: Int,
    @StringRes description: Int,
    onClick: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier
        .fillMaxWidth()
        .clickable {
            onClick()
        }
        .padding(horizontal = 16.dp, vertical = 8.dp)) {
            PreferenceDescription(icon = icon, title = title, description = description)
    }
}

@Composable
fun CheckBoxDialogPreference(
    items: List<CheckboxPreferenceUiModel>,
    minSelected: Int = 2,
    onCancel: () -> Unit,
    onSave: (items: List<CheckboxPreferenceUiModel>) -> Unit
) {
    AlertDialog(onDismissRequest = {}, confirmButton = {
        DialogButton(text = stringResource(id = R.string.save)) {
            onSave(items)
        }
    }, dismissButton = {
        DialogButton(text = stringResource(id = R.string.cancel), onCancel)
    }, title = {
        Text(text = stringResource(id = R.string.controllerStyle))
    }, text = {
        LazyColumn {
            items(items = items, key = { item -> item.key }) { item ->
                fun canChangeCheckbox(): Boolean {
                    return item.checked.not() || items.count { it.checked } == minSelected + 1
                }

                CheckboxDialogRow(
                    text = stringResource(id = item.text),
                    checked = item.checked,
                    enabled = canChangeCheckbox()
                ) {
                    item.checked = it
                }
            }
        }
    })
}

@Composable
fun CheckboxDialogRow(
    text: String,
    enabled: Boolean,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
            if (enabled) onCheckedChange.invoke(!checked)
        }
    ) {
        Text(text = text)
        Spacer(modifier = Modifier.weight(1f))
        Checkbox(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
    }
}

@Composable
fun SaturationPreferenceDialog(
    initialValue: Float,
    onCancel: () -> Unit,
    onSave: (newVal: Float) -> Unit
) {
    var userValue: Float by remember {
        mutableFloatStateOf(initialValue)
    }

    AlertDialog(onDismissRequest = {}, confirmButton = {
        DialogButton(text = stringResource(id = R.string.save)) {
            onSave(userValue)
        }
    }, dismissButton = {
        DialogButton(text = stringResource(id = R.string.cancel), onCancel)
    }, title = {
        Text(text = stringResource(id = R.string.saturation))
    }, text = {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Slider(
                value = userValue,
                valueRange = 0f..2f,
                steps = 19,
                onValueChange = {
                    userValue = it
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 4.dp)
            )
            Text(
                text = String.format("%.1f", userValue),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    })
}

@Composable
fun VibrationPreferenceDialog(
    initialValue: Int,
    onCancel: () -> Unit,
    onSave: (newValue: Int) -> Unit
) {
    var userValue: Int by remember {
        mutableIntStateOf(initialValue)
    }

    AlertDialog(onDismissRequest = {}, confirmButton = {
        DialogButton(text = stringResource(id = R.string.save)) {
            onSave(userValue)
        }
    }, dismissButton = {
        DialogButton(text = stringResource(id = R.string.cancel), onCancel)
    }, title = {
        Text(text = stringResource(id = R.string.vibrationStrength))
    }, text = {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Slider(
                value = userValue.toFloat(),
                valueRange = 1000f..5800f,
                steps = 23,
                onValueChange = {
                    userValue = it.toInt()
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 4.dp)
            )
            Text(
                text = String.format("$userValue"),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    })
}
