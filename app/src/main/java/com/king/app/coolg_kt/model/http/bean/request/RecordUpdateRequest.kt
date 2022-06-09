package com.king.app.coolg_kt.model.http.bean.request

import com.king.app.gdb.data.entity.Record
import com.king.app.gdb.data.entity.RecordType1v1
import com.king.app.gdb.data.entity.RecordType3w

class RecordUpdateRequest {
    var record: Record? = null
    var recordType1v1: RecordType1v1? = null
    var recordType3w: RecordType3w? = null
    var videoUrl: String? = null
    var stars: List<RecordUpdateStarItem>? = null
}

class RecordUpdateStarItem {
    var starId: Long = 0
    var starName: String? = null
    var type = 0
    var score = 0
    var scoreC = 0
    var imageUrl: String? = null
}