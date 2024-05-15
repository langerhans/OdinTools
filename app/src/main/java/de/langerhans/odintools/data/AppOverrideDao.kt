package de.langerhans.odintools.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AppOverrideDao {

    @Query("SELECT * FROM appoverride")
    fun getAll(): Flow<List<AppOverrideEntity>>

    @Query("SELECT * FROM appoverride WHERE packageName = :packageName")
    fun getForPackage(packageName: String): AppOverrideEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(override: AppOverrideEntity)

    @Query("DELETE FROM appoverride WHERE packageName = :packageName")
    fun deleteByPackageName(packageName: String)
}
