package de.langerhans.odintools.tools

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import de.langerhans.odintools.data.SharedPrefsRepo
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var sharedPrefsRepo: SharedPrefsRepo

    @Inject
    lateinit var executor: ShellExecutor

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED && intent.action != ACTION_WATCHDOG_TRIGGER) {
            return
        }

        enableWatchdog(context)
        executor.enableA11yService()

        val saturation = sharedPrefsRepo.saturationOverride
        if (saturation != 1.0f) {
            executor.setSfSaturation(saturation)
        }
    }

    private fun enableWatchdog(context: Context) {
        val wdPath = "${context.filesDir.absolutePath}${File.separator}watchdog.sh"

        File(wdPath).delete()
        File(wdPath).outputStream().use { os ->
            context.assets.open("watchdog.sh").use { ins ->
                ins.copyTo(os)
            }
        }
        executor.executeAsRoot("chmod 777 $wdPath")
        executor.executeAsRoot("pkill -f de.langerhans.odintools/files/watchdog.sh")
        executor.executeAsRoot("sh -T- $wdPath")
    }

    companion object {
        const val ACTION_WATCHDOG_TRIGGER = "de.langerhans.odintools.WATCHDOG_TRIGGER"
    }
}