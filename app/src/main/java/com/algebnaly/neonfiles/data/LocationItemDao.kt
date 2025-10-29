package com.algebnaly.neonfiles.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow


@Dao
interface LocationItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(locationItem: LocationItem)

    @Update
    suspend fun update(locationItem: LocationItem)

    @Delete
    suspend fun delete(locationItem: LocationItem)

    @Query("SELECT * from location_items WHERE id = :id")
    fun getItem(id: Int): Flow<LocationItem?>

    @Query("SELECT * from location_items ORDER BY id ASC")
    fun getAllItems(): Flow<List<LocationItem>>
}