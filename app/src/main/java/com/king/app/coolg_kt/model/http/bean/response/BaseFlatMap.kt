package com.king.app.coolg_kt.model.http.bean.response

import io.reactivex.rxjava3.core.ObservableSource
import io.reactivex.rxjava3.core.Observer

/**
 * @desc
 * @auth 景阳
 * @time 2018/5/12 0012 14:00
 */
object BaseFlatMap {
    fun <T> result(response: BaseResponse<T>): ObservableSource<T> {
        return ObservableSource { observer ->
            if (response.result == 1) {
                observer.onNext(response.data)
            } else {
                observer.onError(Throwable(response.message))
            }
        }
    }

    fun <T> resultIncludeNull(response: BaseResponse<T?>): ObservableSource<Any> {
        return ObservableSource<Any> { observer ->
            if (response.result == 1) {
                if (response.data == null) {
                    observer.onNext(Any())
                } else {
                    observer.onNext(response.data)
                }
            } else {
                observer.onError(Throwable(response.message))
            }
        }
    }
}