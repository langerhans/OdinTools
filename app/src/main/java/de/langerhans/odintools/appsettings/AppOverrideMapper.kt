package de.langerhans.odintools.appsettings

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import de.langerhans.odintools.R
import de.langerhans.odintools.data.AppOverrideEntity
import de.langerhans.odintools.models.ControllerStyle
import de.langerhans.odintools.models.L2R2Style
import javax.inject.Inject

class AppOverrideMapper @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun mapOverrideCandidates(
        existingOverrides: List<AppOverrideEntity>
    ): List<AppUiModel> {
        return context.packageManager.getInstalledApplications(PackageManager.GET_META_DATA).filter {
            it.flags and ApplicationInfo.FLAG_SYSTEM == 0 && it.enabled
        }.filterNot { appInfo ->
            existingOverrides.any { appInfo.packageName == it.packageName }
        }.map {
            val icon = context.packageManager.getApplicationIcon(it.packageName)
            val label = context.packageManager.getApplicationLabel(it).toString()
            AppUiModel(it.packageName, label, icon)
        }.sortedBy {
            it.appName
        }
    }

    fun mapAppOverrides(overrides: List<AppOverrideEntity>): List<AppUiModel> {
        return overrides.mapNotNull(::mapAppOverride)
    }

    fun mapAppOverride(app: AppOverrideEntity): AppUiModel? {
        val appInfo = runCatching {
            context.packageManager.getApplicationInfo(app.packageName, PackageManager.GET_META_DATA)
        }.onFailure { return null }.getOrNull() ?: return null
        // TODO do DB cleanup on uninstalled packages?

        val controllerStyle = ControllerStyle.getById(app.controllerStyle)
        val l2R2Style = L2R2Style.getById(app.l2R2Style)
        return AppUiModel(
            packageName = app.packageName,
            appName = context.packageManager.getApplicationLabel(appInfo).toString(),
            appIcon = context.packageManager.getApplicationIcon(appInfo),
            subtitle = getSubtitle(controllerStyle, l2R2Style),
            controllerStyle = controllerStyle,
            l2r2Style = l2R2Style
        )
    }

    fun mapEmptyOverride(
        packageName: String
    ): AppUiModel {
        // If this crashes then something is fishy...
        val appInfo = context.packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)

        return AppUiModel(
            packageName = packageName,
            appName = context.packageManager.getApplicationLabel(appInfo).toString(),
            appIcon = context.packageManager.getApplicationIcon(appInfo),
        )
    }

    private fun getSubtitle(controllerStyle: ControllerStyle, l2R2Style: L2R2Style): String? {
        return buildString {
            if (controllerStyle != ControllerStyle.Unknown) {
                append(context.getString(R.string.controllerStyle))
                append(": ")
                append(context.getString(controllerStyle.textRes))
                append(" | ")
            }
            if (l2R2Style != L2R2Style.Unknown) {
                append(context.getString(R.string.l2r2mode))
                append(": ")
                append(context.getString(l2R2Style.textRes))
                append(" | ")
            }
        }.trimEnd(' ', '|').ifEmpty { null }
    }
}