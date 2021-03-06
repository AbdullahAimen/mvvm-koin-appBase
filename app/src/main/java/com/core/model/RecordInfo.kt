package com.core.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

/**
 * @author Abdullah Ayman on 23/06/2020
 */
@Parcelize
@Entity(tableName = "RecordInfo")
data class RecordInfo(
    @PrimaryKey(autoGenerate = true)
    var id: Long = -1,
    var recordUri: String,
    var recordName: String,
    var recordDuration: String,
    var recordSize: String,
    var recordCreationDate: String,
    var selectionMode: Boolean = false,
    var fileSelected: Boolean = false,
    var isPlaying: Boolean = false
) : Parcelable {

    companion object {
        fun emptyRecord(): RecordInfo = RecordInfo(
            recordUri = "",
            recordName = "",
            recordCreationDate = "",
            recordSize = "",
            recordDuration = ""
        )
    }
}
