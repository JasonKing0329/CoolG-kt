package com.king.app.coolg_kt.model.extension

import android.util.Log
import kotlin.system.measureTimeMillis

public inline fun printCostTime(tag: String? = null, block: () -> Unit) {
    measureTimeMillis {
        block()
    }.let {
        Log.e(tag?:"NOTAG", "cost $it")
    }
}