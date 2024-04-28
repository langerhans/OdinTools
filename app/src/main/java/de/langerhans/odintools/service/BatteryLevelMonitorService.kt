package de.langerhans.odintools.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import de.langerhans.odintools.R
import de.langerhans.odintools.tools.BatteryLevelReceiver
import javax.inject.Inject

@AndroidEntryPoint
class BatteryLevelMonitorService @Inject constructor() : Service() {

    lateinit var batteryLevelReceiver: BatteryLevelReceiver

    private var registered: Boolean = false

    fun createNotificationChannel(context: Context) {
        val name = context.getString(R.string.chargeLimitChannelName)
        val description = context.getString(R.string.chargeLimitChannelDescription)
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            name,
            importance,
        )
        channel.description = description
        val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun register() {
        if (registered) {
            return
        }
        registered = true
        val intentFilter = IntentFilter().apply {
            BatteryLevelReceiver.ALLOWED_INTENTS.forEach {
                addAction(it)
            }
        }
        registerReceiver(batteryLevelReceiver, intentFilter)
    }

    private fun unregister() {
        if (!registered) {
            return
        }
        registered = false
        unregisterReceiver(batteryLevelReceiver)
    }

    private fun start() {
        register()
        val notification = NotificationCompat
            .Builder(this, NOTIFICATION_CHANNEL_ID)
            .build()
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun stop() {
        unregister()
        stopSelf()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        batteryLevelReceiver = BatteryLevelReceiver()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ServiceHelper.Action.START.toString() -> start()
            ServiceHelper.Action.STOP.toString() -> stop()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stop()
    }

    companion object {
        const val NOTIFICATION_ID = 1
        const val NOTIFICATION_CHANNEL_ID = "BatteryLevelMonitorService"
    }
}
