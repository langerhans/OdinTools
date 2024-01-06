package de.langerhans.odintools.ui.composables

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import de.langerhans.odintools.R
import kotlin.system.exitProcess

@Composable
fun NotAnOdinDialog(
    onAccept: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {},
        confirmButton = {
            DialogButton(text = stringResource(id = R.string.accept)) {
                onAccept()
            }
        },
        dismissButton = {
            DialogButton(text = stringResource(id = R.string.close)) {
                exitProcess(0)
            }
        },
        title = {
            Text(text = stringResource(id = R.string.warning))
        },
        text = {
            Text(text = stringResource(id = R.string.incompatibleDevice))
        }
    )
}

@Composable
fun PServerNotAvailableDialog() {
    AlertDialog(
        onDismissRequest = {},
        confirmButton = {
            DialogButton(text = stringResource(id = R.string.closeButSad)) {
                exitProcess(0)
            }
        },
        title = {
            Text(text = stringResource(id = R.string.warning))
        },
        text = {
            Text(text = stringResource(id = R.string.pServerNotAvailable))
        }
    )
}

@Composable
fun DialogButton(
    text: String,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick
    ) {
        Text(text = text)
    }
}

@Composable
fun DeleteConfirmDialog(
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {},
        confirmButton = {
            DialogButton(text = stringResource(id = R.string.delete), onClick = onDelete)
        },
        dismissButton = {
            DialogButton(text = stringResource(id = R.string.cancel), onClick = onDismiss)
        },
        text = {
            Text(text = stringResource(id = R.string.deleteConfirm))
        }
    )
}