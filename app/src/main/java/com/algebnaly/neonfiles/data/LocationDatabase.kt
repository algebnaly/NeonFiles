package com.algebnaly.neonfiles.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.algebnaly.neonfiles.filesystem.utils.StorageConfigConverter
import com.algebnaly.neonfiles.filesystem.utils.FsTypeConverter


@Database(entities = [LocationEntity::class], version = 2, exportSchema = false)
@TypeConverters(FsTypeConverter::class, StorageConfigConverter::class)
abstract class LocationDatabase: RoomDatabase() {
    abstract fun locationEntityDao(): LocationEntityDao

    companion object {
        @Volatile
        private var Instance: LocationDatabase? = null

        fun getDatabase(context: Context): LocationDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, LocationDatabase::class.java, "location_database")
                    .fallbackToDestructiveMigration(false)
                    .build()
                    .also {
                        Instance = it
                    }
            }
        }
    }
}