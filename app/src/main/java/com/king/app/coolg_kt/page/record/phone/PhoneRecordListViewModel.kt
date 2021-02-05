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
import io.reactivex.rxjava3.core.Observable

/**
 * @description:
 * @author：Jing
 * @date: 2021/2/5 16:23
 */
class PhoneRecordListViewModel(application: Application): BaseViewModel(application) {

    var tagsObserver: MutableLiveData<List<RecordTag>> = MutableLiveData()
    var focusTagPosition: MutableLiveData<Int> = MutableLiveData()

    private var tagRepository = TagRepository()

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

    fun loadHead() {
        var type = SettingProperty.getRecordListTagType()
        if (type == 1) {
            loadScenes()
        }
        else {
            loadTags()
        }
    }

    private fun loadTags() {
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
                    focusTagPosition.value = 0
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
            var list = tagRepository.loadTags(DataConstants.TAG_TYPE_RECORD)
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
        val tags = mutableListOf<RecordTag>()
        tags.add(tagAll)
        if (tagList.isNotEmpty()) {
            tags.addAll(tagList)
        }
        return tags
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