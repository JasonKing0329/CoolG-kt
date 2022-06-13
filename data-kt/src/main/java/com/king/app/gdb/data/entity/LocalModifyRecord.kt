package com.king.app.gdb.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2022/6/13 10:13
 */
@Entity(tableName = "local_modify_record")
data class LocalModifyRecord(
    @PrimaryKey
    var recordId: Long,
    var itemJson: String
)