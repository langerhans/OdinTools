package de.langerhans.odintools.tools

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.BATTERY_SERVICE
import android.content.Intent
import android.os.BatteryManager
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import de.langerhans.odintools.data.SharedPrefsRepo
import javax.inject.Inject

@AndroidEntryPoint
class BatteryLevelReceiver : BroadcastReceiver() {

    @ApplicationContext
    lateinit var context: Context

    @Inject
    lateinit var settings: SettingsRepo

    @Inject
    lateinit var prefs: SharedPrefsRepo

    override fun onReceive(context: Context, intent: Intent) {
        if (!prefs.chargeLimitEnabled || intent.action !in ALLOWED_INTENTS) {
            return
        }

        val batteryManager = context.getSystemService(BATTERY_SERVICE) as BatteryManager
        val batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)

        if (batteryLevel >= prefs.maxBatteryLevel && !settings.chargingSeparationEnabled()) {
            settings.enableChargingSeparation()
        } else if (batteryLevel <= prefs.minBatteryLevel && settings.chargingSeparationEnabled()) {
            settings.disableChargingSeparation()
        }
    }

    companion object {
        val ALLOWED_INTENTS = listOf(
            Intent.ACTION_BATTERY_CHANGED,
            Intent.ACTION_POWER_CONNECTED,
            Intent.ACTION_POWER_DISCONNECTED,
            Intent.ACTION_SCREEN_OFF,
            Intent.ACTION_SCREEN_ON,
        )
    }
}
