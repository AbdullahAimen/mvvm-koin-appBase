package com.core.domain.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.core.model.RecordInfo

/**
 * @author Abdullah Ayman on 23/06/2020
 */
@Database(
    entities = [
        RecordInfo::class],
    version = 1,
    exportSchema = false
)
abstract class AppDB : RoomDatabase() {
    abstract fun RecordsDao(): RecordDAO
}
