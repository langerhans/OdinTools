package de.langerhans.odintools.appsettings

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import de.langerhans.odintools.R
import de.langerhans.odintools.ui.composables.DialogButton
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding),
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
    AlertDialog(onDismissRequest = {}, confirmButton = { }, dismissButton = {
        DialogButton(text = stringResource(id = R.string.cancel), onDismiss)
    }, title = {}, text = {
        if (apps.isEmpty()) {
            Text(text = stringResource(id = R.string.noOverrideCandidates))
            return@AlertDialog
        }
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
        ) {
            items(items = apps, itemContent = {
                AppItem(
                    it.packageName,
                    it.appName,
                    it.appIcon,
                    36.dp,
                    "",
                    onAppSelected,
                )
            })
        }
    })
}
