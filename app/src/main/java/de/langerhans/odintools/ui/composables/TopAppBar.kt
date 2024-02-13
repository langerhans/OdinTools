package de.langerhans.odintools.ui.composables

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.langerhans.odintools.BuildConfig
import de.langerhans.odintools.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OdinTopAppBar(deviceVersion: String) = TopAppBar(title = {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.appName),
            style = MaterialTheme.typography.headlineMedium,
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = stringResource(id = R.string.topBarVersions),
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.End,
            modifier = Modifier.padding(end = 4.dp),
        )
        Text(
            text = "$deviceVersion\n${BuildConfig.VERSION_NAME}",
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.End,
            modifier = Modifier.padding(end = 16.dp),
        )
    }
})
