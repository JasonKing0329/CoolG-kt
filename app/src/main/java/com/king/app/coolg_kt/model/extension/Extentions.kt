package com.king.app.coolg_kt.model.extension

import android.widget.EditText
import io.reactivex.rxjava3.core.Observable

inline fun EditText.toIntOrZero(): Int {
    kotlin.runCatching {
        return text.toString().toInt()
    }
    return 0
}

fun<T> wrapToRx(data: T): Observable<T> {
    return Observable.create {
        it.onNext(data)
        it.onComplete()
    }
}