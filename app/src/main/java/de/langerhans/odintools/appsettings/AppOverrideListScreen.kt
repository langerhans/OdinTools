package de.langerhans.odintools.appsettings

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import de.langerhans.odintools.R
import de.langerhans.odintools.ui.composables.OdinTopAppBar

@Composable
fun AppOverrideListScreen(viewModel: AppOverrideListViewModel = hiltViewModel(), navigateToOverrides: (packageName: String) -> Unit) {
    val uiState: AppOverrideListUiModel by viewModel.uiState.collectAsState()
    Scaffold(topBar = { OdinTopAppBar(deviceVersion = uiState.deviceVersion) }) { contentPadding ->

        if (uiState.showAppSelectDialog) {
            AppPickerDialog(
                uiState.overrideCandidates,
                {
                    viewModel.dismissAppSelectDialog()
                    navigateToOverrides(it)
                },
                {
                    viewModel.dismissAppSelectDialog()
                },
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(
                top = contentPadding.calculateTopPadding(),
                bottom = contentPadding.calculateBottomPadding(),
            ),
        ) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.addClicked() }
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_add),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(LocalContentColor.current),
                        modifier = Modifier.size(48.dp),
                    )
                    Text(
                        text = stringResource(id = R.string.addOverride),
                        modifier = Modifier.padding(start = 16.dp),
                    )
                }
            }
            items(items = uiState.overrideList, itemContent = {
                AppItem(
                    it.packageName,
                    it.appName,
                    it.appIcon,
                    48.dp,
                    it.subtitle,
                ) { packageName ->
                    navigateToOverrides(packageName)
                }
            })
        }
    }
}

@Composable
fun AppItem(packageName: String, label: String, icon: Drawable, iconSize: Dp = 48.dp, subLabel: String? = "", onClick: (String) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(packageName) }
            .padding(vertical = 12.dp, horizontal = 16.dp),
    ) {
        Image(painter = rememberDrawablePainter(drawable = icon), contentDescription = null, Modifier.size(iconSize))
        Column(modifier = Modifier.padding(start = 16.dp)) {
            Text(text = label, modifier = Modifier.padding(bottom = 4.dp))
            if (subLabel?.isNotEmpty() == true) {
                Text(
                    text = subLabel,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
fun AppPickerDialog(apps: List<AppUiModel>, onAppSelected: (String) -> Unit, onDismiss: () -> Unit) {
    var searchText by rememberSaveable { mutableStateOf("") }
    val filteredApps = apps.filter {
        it.appName.contains(searchText, ignoreCase = true)
    }.ifEmpty {
        apps.filter { it.packageName.contains(searchText, ignoreCase = true) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {},
        text = {
            val focusManager = LocalFocusManager.current
            if (apps.isEmpty()) {
                Text(text = stringResource(id = R.string.noOverrideCandidates))
                return@AlertDialog
            }
            Column {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = {
                        searchText = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(text = stringResource(id = R.string.searchApp))
                    },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Search,
                        keyboardType = KeyboardType.Text,
                    ),
                    keyboardActions = KeyboardActions(
                        onAny = {
                            searchText = searchText.trim()
                            focusManager.clearFocus()
                        },
                    ),
                    singleLine = true,
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (filteredApps.isEmpty()) {
                    Text(
                        text = stringResource(id = R.string.noMatches),
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        items(
                            items = filteredApps,
                            itemContent = {
                                AppItem(
                                    it.packageName,
                                    it.appName,
                                    it.appIcon,
                                    36.dp,
                                    "",
                                    onAppSelected,
                                )
                            },
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {},
    )
}
