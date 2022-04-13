package com.king.app.coolg_kt.model.extension

import android.widget.EditText

inline fun EditText.toIntOrZero(): Int {
    kotlin.runCatching {
        return text.toString().toInt()
    }
    return 0
}