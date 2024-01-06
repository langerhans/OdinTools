package de.langerhans.odintools.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [AppOverrideEntity::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appOverrideDao(): AppOverrideDao
}