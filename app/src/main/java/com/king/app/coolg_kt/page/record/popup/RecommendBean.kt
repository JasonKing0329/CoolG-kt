package com.king.app.coolg_kt.page.record.popup

class RecommendBean {
    var sql: String? = null
    var sql1v1: String? = null
    var sql3w: String? = null
    var number = 0
    var isTypeAll = true
    var isType1v1 = true
    var isType3w = true
    var isTypeMulti = true
    var isTypeTogether = true
    var isOnline = false

    val isOnlyType1v1: Boolean
        get() = isType1v1 && !isTypeAll && !isTypeMulti && !isType3w && !isTypeTogether

    val isOnlyType3w: Boolean
        get() = isType3w && !isTypeAll && !isTypeMulti && !isType1v1 && !isTypeTogether

}