package de.langerhans.odintools.tools

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import de.langerhans.odintools.data.SharedPrefsRepo
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var settings: SettingsRepo

    @Inject
    lateinit var prefs: SharedPrefsRepo

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) {
            return
        }
        val saturation = prefs.saturationOverride
        if (saturation != 1.0f) {
            settings.setSfSaturation(saturation)
        }
        val vibrationStrength = prefs.vibrationStrength
        if (vibrationStrength != 0) {
            settings.vibrationStrength = vibrationStrength
        }
    }
}
