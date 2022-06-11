package com.king.app.coolg_kt.model.extension

import android.util.Log
import android.widget.EditText
import com.king.app.coolg_kt.model.http.bean.response.BaseResponse
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

fun<T> T.log(tag: String) {
    Log.e(tag, toString())
}

@Throws
inline fun<T> BaseResponse<T>.flat(): T {
    if (result == 1) {
        return data!!
    } else {
        throw Throwable(message)
    }
}

@Throws
inline fun<T> BaseResponse<T>.flatNullable(): Boolean {
    if (result == 1) {
        return true
    } else {
        throw Throwable(message)
    }
}