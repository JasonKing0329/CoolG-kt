package com.king.app.coolg_kt.page.pub

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.model.bean.TagGroupItem
import com.king.app.coolg_kt.model.http.observer.SimpleObserver
import com.king.app.coolg_kt.model.repository.TagRepository
import com.king.app.coolg_kt.utils.PinyinUtil
import com.king.app.gdb.data.entity.Tag
import com.king.app.gdb.data.entity.TagClass
import com.king.app.gdb.data.entity.TagClassItem
import io.reactivex.rxjava3.core.Observable

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/7/5 16:02
 */
class TagViewModel(application: Application): BaseViewModel(application) {

    var tagList = MutableLiveData<List<Any>>()
    var repository = TagRepository()

    var tagType = 0

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
            val classes = getDatabase().getTagDao().getAllTagClasses(tagType)
            classes.forEach { cl ->
                list.add(cl.bean)
                cl.itemList.sortedBy { item -> item.nameForSort.toLowerCase() }.forEach { item ->
                    list.add(TagGroupItem(item, cl.bean, false))
                }
            }
            it.onNext(list)
            it.onComplete()
        }
    }

    fun newTagClass(it: String) {
        if (getDatabase().getTagDao().countTagClass(it) > 0) {
            messageObserver.value = "Target tag class is already existed"
            return
        }
        var newTag = TagClass(0, tagType, it, PinyinUtil.toPinyinConcat(it))
        getDatabase().getTagDao().insertTagClass(newTag)
        messageObserver.value = "success"
    }

    fun editTagClass(tagClass: TagClass, newName: String) {
        if (getDatabase().getTagDao().countTagClass(newName) > 0) {
            messageObserver.value = "Target tag class is already existed"
            return
        }
        tagClass.name = newName
        tagClass.nameForSort = PinyinUtil.toPinyinConcat(newName)
        getDatabase().getTagDao().updateTagClass(tagClass)
        messageObserver.value = "success"
    }

    fun newTagClassItem(classId: Long, tag: Tag) {
        if (getDatabase().getTagDao().countTagClassItem(classId, tag.id!!) > 0) {
            messageObserver.value = "Target tag is already existed"
            return
        }
        // 老数据没有nameForSort，这这里设置上。新数据在保存tag时就有了
        if (tag.nameForSort.isEmpty()) {
            tag.nameForSort = PinyinUtil.toPinyinConcat(tag.name)
            getDatabase().getTagDao().updateTag(tag)
        }
        var newTag = TagClassItem(0, classId, tag.id!!)
        getDatabase().getTagDao().insertTagClassItem(newTag)
        messageObserver.value = "success"
    }

    fun editTagItem(tag: Tag, newName: String) {
        if (getDatabase().getTagDao().getTagCountBy(newName, tag.type) > 0) {
            messageObserver.value = "Target name is already existed"
            return
        }
        tag.name = newName
        tag.nameForSort = PinyinUtil.toPinyinConcat(newName)
        getDatabase().getTagDao().updateTag(tag)
        messageObserver.value = "success"
    }

    fun deleteTagItem(item: TagGroupItem) {
        getDatabase().getTagDao().deleteTagClassItem(item.parent.id, item.item.id!!)
        messageObserver.value = "success"
    }

    fun deleteTagClass(bean: TagClass) {
        getDatabase().runInTransaction {
            getDatabase().getTagDao().deleteTagClass(bean.id)
            getDatabase().getTagDao().deleteTagClassItems(bean.id)
            messageObserver.value = "success"
        }
    }
}