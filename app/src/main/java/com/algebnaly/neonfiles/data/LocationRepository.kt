package com.algebnaly.neonfiles.data

import kotlinx.coroutines.flow.Flow

class LocationRepository(private val locationItemDao: LocationItemDao) {
    fun getAllLocationStream(): Flow<List<LocationItem>> = locationItemDao.getAllItems()

    fun getLocationStream(id: Int): Flow<LocationItem?> = locationItemDao.getItem(id)

    suspend fun insertLocation(locationItem: LocationItem) = locationItemDao.insert(locationItem)

    suspend fun deleteLocation(locationItem: LocationItem) = locationItemDao.delete(locationItem)

    suspend fun updateLocation(locationItem: LocationItem) = locationItemDao.update(locationItem)
}