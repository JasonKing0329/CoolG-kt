package com.king.app.coolg_kt.page.record

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.model.http.observer.SimpleObserver
import com.king.app.coolg_kt.model.image.ImageProvider
import com.king.app.coolg_kt.model.repository.RecordRepository
import com.king.app.gdb.data.relation.RecordWrap
import io.reactivex.rxjava3.core.ObservableSource

/**
 * @description:
 * @author：Jing
 * @date: 2021/1/23 20:47
 */
class NoStudioViewModel(application: Application): BaseViewModel(application) {

    var listObserver = MutableLiveData<List<RecordWrap>>()

    val repository = RecordRepository()

    fun loadData() {
        loadingObserver.value = true
        repository.getRecordsWithoutStudio()
            .flatMap { toViewList(it) }
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<List<RecordWrap>>(getComposite()){
                override fun onNext(t: List<RecordWrap>?) {
                    loadingObserver.value = false
                    listObserver.value = t
                }

                override fun onError(e: Throwable?) {
                    loadingObserver.value = false
                    e?.printStackTrace()
                    messageObserver.value = e?.message
                }
            })
    }

    private fun toViewList(list: List<RecordWrap>): ObservableSource<List<RecordWrap>> {
        return ObservableSource {
            list.forEach { wrap ->
                wrap.imageUrl = ImageProvider.getRecordRandomPath(wrap.bean.name, null)
            }
            it.onNext(list)
            it.onComplete()
        }
    }
}