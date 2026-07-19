package com.algebnaly.neonfiles.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow


@Dao
interface LocationEntityDao {
    @Upsert
    suspend fun upsert(entity: LocationEntity)

    @Delete
    suspend fun delete(locationItem: LocationEntity)

    @Query("SELECT * from location_items WHERE id = :id")
    fun observe(id: Int): Flow<LocationEntity?>

    @Query("SELECT * from location_items ORDER BY id ASC")
    fun observeAll(): Flow<List<LocationEntity>>
}