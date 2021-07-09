package com.king.app.coolg_kt.model.module

import com.king.app.coolg_kt.model.http.observer.SimpleObserver
import com.king.app.coolg_kt.page.match.TimeWasteRange
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

/**
 * Desc: 封装加载包含复杂内容的List数据
 * List本体先加载通知界面显示基本信息
 * 再异加载耗时内容并在指定的条数内通知更新
 * 使用示例模板：
 * BasicAndTimeWaste<Any>()
        .basic(basicMatches())
        .timeWaste(timeWaste(), 10)
        .composite(getComposite())
        .subscribe(
            object : SimpleObserver<List<Any>>(getComposite()) {},
            object : SimpleObserver<TimeWasteRange>(getComposite()) {}
    )
 * @author：Jing Yang
 * @date: 2021/6/9 14:05
 */
class BasicAndTimeWaste<T> {

    private lateinit var basicObservable: Observable<List<T>>

    private lateinit var timeWasteTask: TimeWasteTask<T>

    private var notifyCount: Int = 0

    private lateinit var composite: CompositeDisposable

    /**
     * list基础数据
     */
    fun basic(observable: Observable<List<T>>): BasicAndTimeWaste<T> {
        this.basicObservable = observable
        return this;
    }

    /**
     * 每个item要做的耗时操作，notifyCount指定每完成多少个通知observer onNext
     */
    fun timeWaste(timeWasteTask: TimeWasteTask<T>, notifyCount: Int): BasicAndTimeWaste<T> {
        this.timeWasteTask = timeWasteTask
        this.notifyCount = notifyCount
        return this;
    }

    fun composite(composite: CompositeDisposable): BasicAndTimeWaste<T> {
        this.composite = composite
        return this;
    }

    fun subscribe(basicObserver: Observer<List<T>>, timeWasteObserver: Observer<TimeWasteRange>) {
        basicObservable
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe(object : SimpleObserver<List<T>>(composite) {
                override fun onNext(t: List<T>) {
                    basicObserver.onNext(t)
                    startTimeWaste(t, timeWasteObserver)
                }

                override fun onError(e: Throwable?) {
                    basicObserver.onError(e)
                }
            })
    }

    private fun startTimeWaste(t: List<T>, timeWasteObserver: Observer<TimeWasteRange>) {
        waste(t)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe(timeWasteObserver)
    }

    private fun waste(list: List<T>): Observable<TimeWasteRange> {
        return Observable.create {

            var count = 0
            var totalNotified = 0
            list.forEach { item ->

                timeWasteTask.handle(count, item)
                // 每30条通知一次
                count ++
                if (count % notifyCount == 0) {
                    it.onNext(TimeWasteRange(count - notifyCount, count))
                    totalNotified = count
                }
            }
            if (totalNotified != list.size) {
                it.onNext(TimeWasteRange(totalNotified, list.size - totalNotified))
            }
            it.onComplete()
        }
    }
}

interface TimeWasteTask<T> {
    fun handle(index: Int, data: T)
}