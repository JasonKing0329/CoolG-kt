package com.king.app.coolg_kt.model.log

import com.king.app.coolg_kt.utils.DebugLog

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/3/5 15:46
 */
object CoolLogger {

    fun logTv(message: String) {
        LogWriter.getInstance().log("[Tv]$message")
        DebugLog.e("[Tv]$message")
    }
}