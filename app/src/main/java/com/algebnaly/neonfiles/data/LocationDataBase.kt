package com.algebnaly.neonfiles.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.algebnaly.neonfiles.filesystem.utils.FsConfigConverter
import com.algebnaly.neonfiles.filesystem.utils.FsTypeConverter


@Database(entities = [LocationItem::class], version = 1, exportSchema = false)
@TypeConverters(FsTypeConverter::class, FsConfigConverter::class)
abstract class LocationDataBase: RoomDatabase() {
    abstract fun locationItemDao(): LocationItemDao

    companion object {
        @Volatile
        private var Instance: LocationDataBase? = null

        fun getDatabase(context: Context): LocationDataBase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, LocationDataBase::class.java, "location_database")
                    .fallbackToDestructiveMigration(false)
                    .build()
                    .also {
                        Instance = it
                    }
            }
        }
    }
}