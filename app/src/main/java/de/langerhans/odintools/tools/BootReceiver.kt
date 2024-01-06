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
    lateinit var sharedPrefsRepo: SharedPrefsRepo

    @Inject
    lateinit var executor: ShellExecutor

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) {
            return
        }

        val saturation = sharedPrefsRepo.saturationOverride
        if (saturation != 1.0f) {
            executor.setSfSaturation(saturation)
        }
    }
}