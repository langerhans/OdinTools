package de.langerhans.odintools.appsettings

import android.graphics.drawable.Drawable
import de.langerhans.odintools.models.ControllerStyle
import de.langerhans.odintools.models.FanMode
import de.langerhans.odintools.models.L2R2Style
import de.langerhans.odintools.models.PerfMode
import de.langerhans.odintools.models.VibrationStrength

data class AppOverrideListUiModel(
    val deviceVersion: String = "",
    val showAppSelectDialog: Boolean = false,
    val overrideList: List<AppUiModel> = emptyList(),
    val overrideCandidates: List<AppUiModel> = emptyList(),
)

data class AppOverridesUiModel(
    val deviceVersion: String = "",
    val app: AppUiModel? = null,

    val hasUnsavedChanges: Boolean = false,
    val showDeleteConfirmDialog: Boolean = false,
    val navigateBack: Boolean = false,
    val isNewApp: Boolean = false,
    val disabledFanModeKeys: List<String> = emptyList(),
)

data class AppUiModel(
    val packageName: String,
    val appName: String,
    val appIcon: Drawable,
    val subtitle: String? = null,
    val controllerStyle: ControllerStyle? = null,
    val l2r2Style: L2R2Style? = null,
    val fanMode: FanMode? = null,
    val perfMode: PerfMode? = null,
    val vibrationStrength: VibrationStrength? = null,
)
