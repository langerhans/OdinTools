package de.langerhans.odintools.tools

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import dagger.hilt.android.AndroidEntryPoint
import de.langerhans.odintools.data.SharedPrefsRepo
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
class BatteryLevelReceiver : BroadcastReceiver() {

    @Inject
    lateinit var prefs: SharedPrefsRepo

    @Inject
    lateinit var settings: SettingsRepo

    override fun onReceive(context: Context, intent: Intent) {
        if (!prefs.chargeLimitEnabled || intent.action !in ALLOWED_INTENTS) {
            return
        }

        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val batteryLevel = (level * 100 / scale.toFloat()).roundToInt()

        if (batteryLevel >= prefs.maxBatteryLevel) {
            settings.enableChargingSeparation()
        } else if (batteryLevel <= prefs.minBatteryLevel) {
            settings.disableChargingSeparation()
        }
    }

    companion object {
        val ALLOWED_INTENTS = listOf(
            Intent.ACTION_BATTERY_CHANGED,
            Intent.ACTION_POWER_CONNECTED,
            Intent.ACTION_POWER_DISCONNECTED,
        )
    }
}
