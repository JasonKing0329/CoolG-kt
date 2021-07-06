package com.king.app.coolg_kt.page.record.phone

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.conf.AppConstants
import com.king.app.coolg_kt.model.http.observer.SimpleObserver
import com.king.app.coolg_kt.model.repository.TagRepository
import com.king.app.coolg_kt.model.setting.SettingProperty
import com.king.app.coolg_kt.page.record.RecordTag
import com.king.app.gdb.data.DataConstants
import com.king.app.gdb.data.bean.RecordScene
import com.king.app.gdb.data.entity.TagClass
import io.reactivex.rxjava3.core.Observable

/**
 * @description:
 * @author：Jing
 * @date: 2021/2/5 16:23
 */
class PhoneRecordListViewModel(application: Application): BaseViewModel(application) {

    var tagsObserver: MutableLiveData<List<RecordTag>> = MutableLiveData()
    var tagClassesObserver: MutableLiveData<List<TagClass>> = MutableLiveData()
    var focusTagPosition: MutableLiveData<Int> = MutableLiveData()

    val TAG_CLASS_ALL_ID: Long = 0
    var mCurTagClassId = TAG_CLASS_ALL_ID

    private var tagRepository = TagRepository()

    private var tagClassList: List<TagClass> = mutableListOf()

    private var dataTagList: List<RecordTag> = mutableListOf()

    private var tagAll = RecordTag(1, AppConstants.KEY_SCENE_ALL, 0L, 0)
    private var mTagBean = tagAll
    private var mTagType = SettingProperty.getRecordListTagType()
    private var mTagSortType = SettingProperty.getTagSortType()

    fun loadStudioTitle(studioId: Long): String {
        var studio = getDatabase().getFavorDao().getFavorRecordOrderBy(studioId)
        var title: String? = null
        studio?.let {
            title = it.name
        }
        return title?:"Records"
    }

    fun isHeadTag(): Boolean {
        var type = SettingProperty.getRecordListTagType()
        return type != 1
    }

    fun isHeadScene(): Boolean {
        var type = SettingProperty.getRecordListTagType()
        return type == 1
    }

    fun loadHead() {
        if (isHeadScene()) {
            loadScenes()
        }
        else {
            getTagClasses()
            mCurTagClassId = TAG_CLASS_ALL_ID
            tagClassesObserver.value = tagClassList
            loadTags()
        }
    }

    private fun getTagClasses() {
        val list = getDatabase().getTagDao().getAllTagClassesBasic(DataConstants.TAG_TYPE_RECORD)
        val result = list.toMutableList();
        result.add(0, TagClass(TAG_CLASS_ALL_ID, DataConstants.TAG_TYPE_RECORD, "All", "all"))
        tagClassList = result
    }

    var isFirstTimeLoadTag = true

    fun loadTags() {
        convertTags()
            .flatMap {
                dataTagList = it
                sortTags(mTagSortType, dataTagList)
            }
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<List<RecordTag>>(getComposite()) {
                override fun onNext(t: List<RecordTag>) {
                    val allList: List<RecordTag> = addTagAll(t)
                    tagsObserver.value = allList
                    // 只有第一次才定位到第一个item，其他情况下，因为不重新加载tag下的items，所以让focus position消失
                    focusTagPosition.value = if (isFirstTimeLoadTag) {
                        isFirstTimeLoadTag = false
                        0
                    } else {
                        -1
                    }
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                    messageObserver.value = e?.message
                }

            })
    }

    private fun loadScenes() {
        dataTagList = convertScenes(getDatabase().getRecordDao().getAllScenes())
        sortTags(mTagSortType, dataTagList)
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<List<RecordTag>>(getComposite()) {
                override fun onNext(t: List<RecordTag>) {
                    val allList: List<RecordTag> = addTagAll(t)
                    tagsObserver.value = allList
                    focusTagPosition.value = 0
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                    messageObserver.value = e?.message
                }

            })
    }

    private fun convertScenes(list: List<RecordScene>): List<RecordTag> {
        var result = mutableListOf<RecordTag>()
        list.forEach {
            it.name?.let { name -> result.add(RecordTag(1, name, 0L, it.number)) }
        }
        return result
    }

    private fun convertTags(): Observable<List<RecordTag>> {
        return Observable.create {
            var list = if (mCurTagClassId == TAG_CLASS_ALL_ID) {
                tagRepository.loadTags(DataConstants.TAG_TYPE_RECORD)
            }
            else {
                tagRepository.loadTagClassItems(mCurTagClassId)
            }
            var result = mutableListOf<RecordTag>()
            list.forEach { tag ->
                var recordTag = RecordTag(0, tag.name?:"", tag.id!!)
                recordTag.number = getDatabase().getTagDao().countRecordTagItems(tag.id!!)
                result.add(recordTag)
            }
            it.onNext(result)
            it.onComplete()
        }
    }

    private fun sortTags(sortType: Int, list: List<RecordTag>): Observable<List<RecordTag>> {
        return Observable.create {
            var result = when(sortType) {
                AppConstants.TAG_SORT_NAME -> list.sortedBy { tag -> tag.name }
                AppConstants.TAG_SORT_RANDOM -> list.shuffled()
                AppConstants.TAG_SORT_NUMBER -> list.sortedByDescending { tag -> tag.number }
                else -> list
            }
            it.onNext(result)
            it.onComplete()
        }
    }

    fun startSortTag() {
        sortTags(mTagSortType, dataTagList)
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<List<RecordTag>>(getComposite()) {
                override fun onNext(t: List<RecordTag>) {
                    val allList: List<RecordTag> = addTagAll(t)
                    tagsObserver.value = allList
                    focusToCurrentTag(allList)
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                    messageObserver.value = e?.message
                }

            })
    }

    private fun addTagAll(tagList: List<RecordTag>): List<RecordTag> {
        return if (isHeadTag() && mCurTagClassId != TAG_CLASS_ALL_ID) {
            tagList
        }
        else {
            val list =  tagList.toMutableList();
            list.add(0, tagAll)
            list
        }
    }

    private fun focusToCurrentTag(allList: List<RecordTag>) {
        for (i in allList.indices) {
            if (mTagBean.name == allList[i].name) {
                focusTagPosition.value = i
                break
            }
        }
    }

    fun onTagTypeChanged() {
        mTagType = SettingProperty.getRecordListTagType()
    }

    fun onTagSortChanged() {
        mTagSortType = SettingProperty.getTagSortType()
    }

}