package com.core.domain.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.core.model.RecordInfo

/**
 * @author Abdullah Ayman on 23/06/2020
 */
@Dao
interface RecordDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRecord(records: List<RecordInfo>)

    @Delete
    fun delete(record: RecordInfo)

    @Query("SELECT * FROM RecordInfo order by recordCreationDate DESC")
    fun retrieveRecords(): LiveData<RecordInfo>

    @Query("DELETE FROM RecordInfo")
    fun deleteAllRecords()
}
