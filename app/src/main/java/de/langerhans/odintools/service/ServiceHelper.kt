package de.langerhans.odintools.service

import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import de.langerhans.odintools.data.SharedPrefsRepo
import javax.inject.Inject

class ServiceHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val prefs: SharedPrefsRepo,
) {
    private var batteryLevelMonitorService: BatteryLevelMonitorService = BatteryLevelMonitorService()

    init {
        batteryLevelMonitorService.createNotificationChannel(context)
    }

    fun applyRequiredServices() {
        val intent = Intent(context, batteryLevelMonitorService::class.java)
        if (prefs.chargeLimitEnabled) {
            intent.action = Action.START.toString()
            context.startForegroundService(intent)
        } else {
            intent.action = Action.STOP.toString()
            context.startService(intent)
        }
    }

    enum class Action {
        START,
        STOP,
    }
}
