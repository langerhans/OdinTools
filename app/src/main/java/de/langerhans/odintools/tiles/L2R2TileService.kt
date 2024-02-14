package de.langerhans.odintools.tiles

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import dagger.hilt.android.AndroidEntryPoint
import de.langerhans.odintools.R
import de.langerhans.odintools.data.SharedPrefsRepo
import de.langerhans.odintools.models.L2R2Style
import de.langerhans.odintools.models.L2R2Style.*
import de.langerhans.odintools.tools.DeviceUtils
import de.langerhans.odintools.tools.ShellExecutor
import javax.inject.Inject

@AndroidEntryPoint
class L2R2TileService : TileService() {

    @Inject
    lateinit var executor: ShellExecutor

    @Inject
    lateinit var prefs: SharedPrefsRepo

    private var disabledStyle: L2R2Style? = null

    override fun onStartListening() {
        super.onStartListening()
        if (!executor.pServerAvailable) {
            qsTile.state = Tile.STATE_UNAVAILABLE
            qsTile.subtitle = getString(R.string.unknown)
            qsTile.updateTile()
            return
        }

        val currentStyle = L2R2Style.getStyle(executor)
        disabledStyle = prefs.disabledL2r2Style?.let {
            L2R2Style.getById(it)
        }

        qsTile.state = Tile.STATE_ACTIVE
        qsTile.subtitle = getString(currentStyle.textRes)
        qsTile.updateTile()
    }

    override fun onClick() {
        super.onClick()

        val current = L2R2Style.getStyle(executor)
        val newStyle = when (current) {
            is Analog -> if (disabledStyle == Digital) Both else Digital
            is Digital -> if (disabledStyle == Both) Analog else Both
            is Both, is Unknown -> if (disabledStyle == Analog) Digital else Analog
        }
        newStyle.enable(executor)

        qsTile.state = Tile.STATE_ACTIVE
        qsTile.subtitle = getString(newStyle.textRes)
        qsTile.updateTile()
    }
}
