package de.langerhans.odintools.tiles

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import dagger.hilt.android.AndroidEntryPoint
import de.langerhans.odintools.R
import de.langerhans.odintools.models.FanModes
import de.langerhans.odintools.models.FanModes.*
import de.langerhans.odintools.tools.DeviceUtils
import de.langerhans.odintools.data.SharedPrefsRepo
import de.langerhans.odintools.tools.ShellExecutor
import javax.inject.Inject

@AndroidEntryPoint
class FanModesService : TileService() {

    @Inject
    lateinit var executor: ShellExecutor

    @Inject
    lateinit var deviceUtils: DeviceUtils

    @Inject
    lateinit var prefs: SharedPrefsRepo

    private var disabledStyle: FanModes? = null

    override fun onStartListening() {
        super.onStartListening()
        if (!deviceUtils.isPServerAvailable()) {
            qsTile.state = Tile.STATE_UNAVAILABLE
            qsTile.subtitle = getString(R.string.unknown)
            qsTile.updateTile()
            return
        }

        val currentStyle = FanModes.getStyle(executor)
        disabledStyle = prefs.disabledFanModes?.let {
            FanModes.getById(it)
        }

        qsTile.state = Tile.STATE_ACTIVE
        qsTile.subtitle = getString(currentStyle.textRes)
        qsTile.updateTile()
    }

    override fun onClick() {
        super.onClick()

        val current = FanModes.getStyle(executor)
        val newStyle = when (current) {
            is Quiet -> if (disabledStyle == Smart) Sport else Smart
            is Smart -> if (disabledStyle == Sport) Quiet else Sport
            is Sport, is Unknown -> if (disabledStyle == Quiet) Smart else Quiet
            else -> Off
        }
        newStyle.enable(executor)

        qsTile.state = Tile.STATE_ACTIVE
        qsTile.subtitle = getString(newStyle.textRes)
        qsTile.updateTile()
    }
}