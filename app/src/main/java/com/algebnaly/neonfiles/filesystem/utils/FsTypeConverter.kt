package com.algebnaly.neonfiles.filesystem.utils

import androidx.room.TypeConverter
import com.algebnaly.neonfiles.filesystem.FsType

class FsTypeConverter {
    @TypeConverter
    fun toInt(fsType: FsType): Int = fsType.code.toInt()

    @TypeConverter
    fun fromInt(value: Int): FsType = FsType.fromU16(value.toUShort())
}