package de.langerhans.odintools.tiles

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import dagger.hilt.android.AndroidEntryPoint
import de.langerhans.odintools.R
import de.langerhans.odintools.data.SharedPrefsRepo
import de.langerhans.odintools.models.ControllerStyle
import de.langerhans.odintools.models.ControllerStyle.*
import de.langerhans.odintools.tools.ShellExecutor
import javax.inject.Inject

@AndroidEntryPoint
class ControllerTileService : TileService() {

    @Inject
    lateinit var executor: ShellExecutor

    @Inject
    lateinit var prefs: SharedPrefsRepo

    private var disabledStyle: ControllerStyle? = null

    override fun onStartListening() {
        super.onStartListening()
        if (!executor.pServerAvailable) {
            qsTile.state = Tile.STATE_UNAVAILABLE
            qsTile.subtitle = getString(R.string.unknown)
            qsTile.updateTile()
            return
        }

        val currentStyle = ControllerStyle.getStyle(executor)
        disabledStyle = prefs.disabledControllerStyle?.let {
            ControllerStyle.getById(it)
        }

        qsTile.state = Tile.STATE_ACTIVE
        qsTile.subtitle = getString(currentStyle.textRes)
        qsTile.updateTile()
    }

    override fun onClick() {
        super.onClick()

        val current = ControllerStyle.getStyle(executor)
        val newStyle = when (current) {
            Xbox -> if (disabledStyle == Odin) Disconnect else Odin
            Odin -> if (disabledStyle == Disconnect) Xbox else Disconnect
            Disconnect, Unknown -> if (disabledStyle == Xbox) Odin else Xbox
        }
        newStyle.enable(executor)

        qsTile.state = Tile.STATE_ACTIVE
        qsTile.subtitle = getString(newStyle.textRes)
        qsTile.updateTile()
    }
}
