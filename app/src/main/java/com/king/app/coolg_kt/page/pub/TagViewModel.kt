package com.king.app.coolg_kt.page.pub

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.model.http.observer.SimpleObserver
import com.king.app.coolg_kt.utils.PinyinUtil
import com.king.app.gdb.data.entity.TagClass
import io.reactivex.rxjava3.core.Observable

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/7/5 16:02
 */
class TagViewModel(application: Application): BaseViewModel(application) {

    var tagList = MutableLiveData<List<Any>>()

    fun loadTags() {
        getTags()
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<List<Any>>(getComposite()) {
                override fun onNext(t: List<Any>?) {
                    tagList.value = t
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                    messageObserver.value = e?.message?:""
                }
            })
    }

    private fun getTags(): Observable<List<Any>> {
        return Observable.create {
            val list = mutableListOf<Any>()
            val classes = getDatabase().getTagDao().getAllTagClasses()
            classes.forEach { cl ->
                list.add(cl)
                cl.itemList.sortedBy { item -> item.nameForSort }.forEach { item -> list.add(item) }
            }
            it.onNext(list)
            it.onComplete()
        }
    }

    fun newTagClass(it: String) {
        if (getDatabase().getTagDao().countTagClass(it) > 0) {
            messageObserver.value = "Target tag is already existed"
            return
        }
        var newTag = TagClass(0, it, PinyinUtil.toPinyinConcat(it))
        getDatabase().getTagDao().insertTagClass(newTag)
    }

    fun newTagClassItem(classId: Long, itemId: Long) {
//        if (getDatabase().getTagDao().countTagClass(it) > 0) {
//            messageObserver.value = "Target tag is already existed"
//            return
//        }
//        var newTag = TagClass(0, it, PinyinUtil.toPinyinConcat(it))
//        getDatabase().getTagDao().insertTagClass(newTag)
    }
}