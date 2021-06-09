package com.king.app.gdb.data.bean

import androidx.room.Embedded
import com.king.app.gdb.data.entity.Record

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/6/9 13:58
 */
data class RecordCpt (
    
    @Embedded
    var record: Record,

    var num: Int = 0

)