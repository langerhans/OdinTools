package de.langerhans.odintools.data

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [AppOverrideEntity::class],
    version = 2,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
    ],
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appOverrideDao(): AppOverrideDao
}
