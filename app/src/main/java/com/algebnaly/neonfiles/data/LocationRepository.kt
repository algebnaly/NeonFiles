package com.algebnaly.neonfiles.data

import com.algebnaly.neonfiles.core.model.StorageLocation
import com.algebnaly.neonfiles.data.location.mapper.toDomain
import com.algebnaly.neonfiles.data.location.mapper.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface LocationRepository{
    fun observeAll(): Flow<List<StorageLocation>>
    fun observe(id: Int): Flow<StorageLocation?>
    suspend fun save(location: StorageLocation)
    suspend fun delete(location: StorageLocation)
}

class OfflineLocationRepository(
    private val dao: LocationEntityDao,
) : LocationRepository {

    override fun observeAll(): Flow<List<StorageLocation>> =
        dao.observeAll().map { entities -> entities.map(LocationEntity::toDomain) }

    override fun observe(id: Int): Flow<StorageLocation?> =
        dao.observe(id).map { it?.toDomain() }

    override suspend fun save(location: StorageLocation) {
        dao.upsert(location.toEntity())
    }

    override suspend fun delete(location: StorageLocation) {
        dao.delete(location.toEntity())
    }
}