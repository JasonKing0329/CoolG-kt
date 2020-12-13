package com.king.app.coolg_kt.model.http.bean.response

class AppCheckBean {
    var isAppUpdate = false
    var appVersion: String? = null
    var appName: String? = null
    var appSize: Long = 0
    var isGdbDatabaseUpdate = false
    var gdbDabaseVersion: String? = null
    var gdbDabaseName: String? = null
    var gdbDabaseSize: Long = 0

}