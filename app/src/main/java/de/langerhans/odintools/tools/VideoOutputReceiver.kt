package de.langerhans.odintools.tools

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import dagger.hilt.android.AndroidEntryPoint
import de.langerhans.odintools.data.SharedPrefsRepo
import de.langerhans.odintools.models.ControllerStyle
import de.langerhans.odintools.models.L2R2Style
import de.langerhans.odintools.service.ForegroundAppWatcherService.Companion.OVERRIDE_DELAY
import javax.inject.Inject

@AndroidEntryPoint
class VideoOutputReceiver : BroadcastReceiver() {

    var overrideEnabled = false

    private var savedControllerStyle: ControllerStyle? = null
    private var savedL2R2Style: L2R2Style? = null

    @Inject
    lateinit var executor: ShellExecutor

    @Inject
    lateinit var prefs: SharedPrefsRepo

    private fun handleEvent(connected: Boolean?) {
        if (connected == true && !overrideEnabled) {
            // Save default styles
            savedControllerStyle = ControllerStyle.getStyle(executor)
            savedL2R2Style = L2R2Style.getStyle(executor)

            // Apply style profile
            ControllerStyle.getById(prefs.videoOutputControllerStyle).takeIf {
                it != ControllerStyle.Unknown
            }?.enable(executor)
            L2R2Style.getById(prefs.videoOutputL2R2Style).takeIf {
                it != L2R2Style.Unknown
            }?.enable(executor)

            overrideEnabled = true
        } else if (connected == false && overrideEnabled) {
            // Reset to defaults
            savedControllerStyle?.enable(executor)
            savedL2R2Style?.enable(executor)

            overrideEnabled = false
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action !in ALLOWED_INTENTS) {
            return
        }

        val connected = intent.extras?.getBoolean("is_connected")

        if (prefs.overrideDelay) {
            Handler(Looper.getMainLooper()).postDelayed({ handleEvent(connected) }, OVERRIDE_DELAY)
        } else {
            handleEvent(connected)
        }
    }

    companion object {
        private const val ACTION_DP_STATUS_CHANGED = "com.retrostation.action.DP_STATUS_CHANGED"
        private const val ACTION_HDMI_STATUS_CHANGED = "com.retrostation.action.HDMI_STATUS_CHANGED"
        private const val ACTION_HDMI_OR_DP_STATUS_CHANGED = "com.retrostation.action.HDMI_OR_DP_STATUS_CHANGED"
        val ALLOWED_INTENTS = listOf(
            ACTION_DP_STATUS_CHANGED,
            ACTION_HDMI_STATUS_CHANGED,
            ACTION_HDMI_OR_DP_STATUS_CHANGED,
        )
    }
}
