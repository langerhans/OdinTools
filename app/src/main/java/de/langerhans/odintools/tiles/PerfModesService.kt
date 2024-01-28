package de.langerhans.odintools.tiles

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import dagger.hilt.android.AndroidEntryPoint
import de.langerhans.odintools.R
import de.langerhans.odintools.models.PerfModes
import de.langerhans.odintools.models.PerfModes.*
import de.langerhans.odintools.tools.DeviceUtils
import de.langerhans.odintools.data.SharedPrefsRepo
import de.langerhans.odintools.tools.ShellExecutor
import javax.inject.Inject

@AndroidEntryPoint
class PerfModesService : TileService() {

    @Inject
    lateinit var executor: ShellExecutor

    @Inject
    lateinit var deviceUtils: DeviceUtils

    @Inject
    lateinit var prefs: SharedPrefsRepo

    private var disabledStyle: PerfModes? = null

    override fun onStartListening() {
        super.onStartListening()
        if (!deviceUtils.isPServerAvailable()) {
            qsTile.state = Tile.STATE_UNAVAILABLE
            qsTile.subtitle = getString(R.string.unknown)
            qsTile.updateTile()
            return
        }

        val currentStyle = PerfModes.getStyle(executor)
        disabledStyle = prefs.disabledPerfModes?.let {
            PerfModes.getById(it)
        }

        qsTile.state = Tile.STATE_ACTIVE
        qsTile.subtitle = getString(currentStyle.textRes)
        qsTile.updateTile()
    }

    override fun onClick() {
        super.onClick()

        val current = PerfModes.getStyle(executor)
        val newStyle = when (current) {
            is Standard -> if (disabledStyle == Performance) HighPerformance else Performance
            is Performance -> if (disabledStyle == HighPerformance) Standard else HighPerformance
            is HighPerformance, is Unknown -> if (disabledStyle == Standard) Performance else Standard
        }
        newStyle.enable(executor)

        qsTile.state = Tile.STATE_ACTIVE
        qsTile.subtitle = getString(newStyle.textRes)
        qsTile.updateTile()
    }
}