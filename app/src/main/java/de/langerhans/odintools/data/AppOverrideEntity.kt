package de.langerhans.odintools.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "appoverride")
data class AppOverrideEntity(
    @PrimaryKey
    val packageName: String,
    val controllerStyle: String?,
    val l2R2Style: String?,
    val perfMode: String?,
    val fanMode: String?,
    val vibrationStrength: Int?
)
