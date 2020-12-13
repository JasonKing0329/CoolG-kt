package com.king.app.coolg_kt.model.http.observer

import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable

/**
 * @description:
 * @author：Jing
 * @date: 2020/12/13 11:34
 */
abstract class SimpleObserver<T>: Observer<T> {

    var composite: CompositeDisposable? = null

    constructor(composite: CompositeDisposable) {
        this.composite = composite
    }

    override fun onComplete() {
    }

    override fun onSubscribe(d: Disposable?) {
        d?.let {
            composite?.add(d)
        }
    }
}