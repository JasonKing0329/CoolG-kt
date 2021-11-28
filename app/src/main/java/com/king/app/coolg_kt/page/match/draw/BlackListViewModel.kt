package com.king.app.coolg_kt.page.match.draw

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.model.http.observer.SimpleObserver
import com.king.app.gdb.data.entity.match.MatchBlackList
import com.king.app.gdb.data.relation.RecordWrap
import io.reactivex.rxjava3.core.Observable

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/11/28 16:53
 */
class BlackListViewModel(application: Application): BaseViewModel(application) {

    val blackList = MutableLiveData<List<RecordWrap>>()

    fun loadBlackList() {
        getBlackList()
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<List<RecordWrap>>(getComposite()) {
                override fun onNext(t: List<RecordWrap>) {
                    blackList.value = t
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                }
            })
    }

    private fun getBlackList(): Observable<List<RecordWrap>> {
        return Observable.create {
            val list = mutableListOf<RecordWrap?>()
            getDatabase().getMatchDao().queryBlackList().mapTo(list) { mb ->
                val item = getDatabase().getRecordDao().getRecord(mb.recordId)
                item?.let { bean -> bean.canSelect = true }
                item
            }
            val result = list.filterNotNull()
            it.onNext(result)
            it.onComplete()
        }
    }

    fun addToBlackList(recordId: Long) {
        var bean = MatchBlackList(recordId)
        getDatabase().getMatchDao().insertBlackList(listOf(bean))
        loadBlackList()
    }

    fun deleteFromBlackList(items: List<RecordWrap>) {
        val list = mutableListOf<MatchBlackList>()
        items.mapTo(list) {
            MatchBlackList(it.bean.id!!)
        }
        getDatabase().getMatchDao().deleteBlackList(list)
        loadBlackList()
    }

}