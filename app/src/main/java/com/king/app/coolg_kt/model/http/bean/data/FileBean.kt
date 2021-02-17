package com.king.app.coolg_kt.model.http.bean.data

class FileBean {
    var name: String? = null
    var extra: String? = null
    var path: String? = null
    var isFolder = false
    var lastModifyTime: Long = 0
    var size: Long = 0
    var sourceUrl: String? = null

    /**
     * server端不赋值，client端记录parent
     */
    var parentBean: FileBean? = null
    var namePinyin: String? = null
}