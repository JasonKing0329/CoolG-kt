package com.king.app.coolg_kt.utils

/**
 * @description:
 * @author：Jing
 * @date: 2021/1/26 14:30
 */
class TimeCostUtil {

    companion object {

        var time: Long = 0

        fun start() {
            time = System.currentTimeMillis()
        }

        fun end(tag: String) {
            val t = System.currentTimeMillis()
            DebugLog.e("$tag cost ${t - time}")
        }
    }
}