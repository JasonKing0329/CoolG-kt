package com.king.app.coolg_kt.page.record

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.model.http.observer.SimpleObserver
import com.king.app.coolg_kt.model.image.ImageProvider
import com.king.app.coolg_kt.model.repository.RecordRepository
import com.king.app.coolg_kt.model.repository.TagRepository
import com.king.app.coolg_kt.model.setting.SettingProperty
import com.king.app.coolg_kt.page.record.popup.RecommendBean
import com.king.app.gdb.data.DataConstants
import com.king.app.gdb.data.RecordCursor
import com.king.app.gdb.data.entity.Tag
import com.king.app.gdb.data.relation.RecordWrap
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableSource

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2020/12/15 9:44
 */
class RecordListViewModel(application: Application): BaseViewModel(application) {

    val DEFAULT_LOAD_MORE = 50

    var tagsObserver: MutableLiveData<List<Tag>> = MutableLiveData()
    var recordsObserver: MutableLiveData<List<RecordWrap>> = MutableLiveData()
    var moreObserver: MutableLiveData<Int> = MutableLiveData()
    var scrollPositionObserver: MutableLiveData<Int> = MutableLiveData()
    var focusTagPosition: MutableLiveData<Int> = MutableLiveData()

    private var mTagSortType = SettingProperty.getTagSortType()

    var dataTagList: List<Tag> = mutableListOf()

    private var mRecordList = mutableListOf<RecordWrap>()

    private var tagRepository = TagRepository()
    private var recordRepository = RecordRepository()

    private var mSortMode = 0
    private var mSortDesc = false
    private val mKeyScene: String? = null
    private val mKeyword: String? = null
    private val mStarId: Long = 0
    private val mOrderId: Long = 0
    private val mRecordType: Int = 0

    private var mTagId: Long = 0

    var mRecommendBean: RecommendBean? = null

    private var moreCursor = RecordCursor()

    init {
        onSortTypeChanged()
    }

    fun getNotNullRecommendBean(): RecommendBean {
        return if (mRecommendBean == null) RecommendBean()
        else mRecommendBean!!
    }

    fun onSortTypeChanged() {
        mSortMode = SettingProperty.getRecordSortType()
        mSortDesc = SettingProperty.isRecordSortDesc()
    }

    fun loadTags() {
        dataTagList = tagRepository.loadTags(DataConstants.TAG_TYPE_RECORD)
        startSortTag(true)
    }

    fun startSortTag(loadAll: Boolean) {
        tagRepository.sortTags(mTagSortType, dataTagList)
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<List<Tag>>(getComposite()) {
                override fun onNext(t: List<Tag>) {
                    val allList: List<Tag> = addTagAll(t)
                    tagsObserver.setValue(allList)
                    if (loadAll) {
                        loadTagRecords(allList[0].id!!)
                    } else {
                        focusToCurrentTag(allList)
                    }
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                    messageObserver.value = e?.message
                }

            })
    }

    private fun addTagAll(tagList: List<Tag>): List<Tag> {
        val tags = mutableListOf<Tag>()
        tags.add(Tag(0L, "All"))
        if (tagList.isNotEmpty()) {
            tags.addAll(tagList)
        }
        return tags
    }

    private fun focusToCurrentTag(allList: List<Tag>) {
        for (i in allList.indices) {
            if (mTagId == allList[i].id) {
                focusTagPosition.value = i
                break
            }
        }
    }

    fun onTagSortChanged() {
        mTagSortType = SettingProperty.getTagSortType()
    }

    fun newRecordCursor() {
        moreCursor = RecordCursor()
        moreCursor.number = DEFAULT_LOAD_MORE
    }

    fun loadTagRecords() {
        loadTagRecords(mTagId)
    }

    fun loadTagRecords(tagId: Long) {
        mRecordList.clear()
        mTagId = tagId
        // 偏移量从0开始
        newRecordCursor()
        queryRecords(tagId)
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<List<RecordWrap>>(getComposite()) {

                override fun onNext(list: List<RecordWrap>) {
                    mRecordList.addAll(list)
                    recordsObserver.value = mRecordList
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                    messageObserver.value = "Load records error: " + e?.message
                }
            })
    }

    fun loadMoreRecords() {
        loadMoreRecords(null)
    }

    fun loadMoreRecords(scrollPosition: Int?) {
        val originSize = mRecordList.size
        queryRecords(mTagId)
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<List<RecordWrap>>(getComposite()) {

                override fun onNext(list: List<RecordWrap>) {
                    moreCursor.number = DEFAULT_LOAD_MORE
                    mRecordList.addAll(list)
                    moreObserver.value = originSize + 1
                    if (scrollPosition != null) {
                        scrollPositionObserver.value = scrollPosition
                    }
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                    messageObserver.value = "Load records error: " + e?.message
                }
            })
    }

    private fun queryRecords(tagId: Long): Observable<List<RecordWrap>> {
        return recordRepository.getRecordFilter(mSortMode, mSortDesc, mRecordType, mStarId, mOrderId, tagId, moreCursor, mRecommendBean, mKeyword, mKeyScene)
            .flatMap { filter -> recordRepository.getRecords(filter) }
            .flatMap { list ->  toViewItems(list)}
            .compose(applySchedulers());
    }

    private fun toViewItems(list: List<RecordWrap>): ObservableSource<List<RecordWrap>> {
        moreCursor.offset += list.size
        return ObservableSource {
            list.forEach { record ->
                var name = record.bean.name?:""
                record.imageUrl = ImageProvider.getRecordRandomPath(name, null)
            }
            it.onNext(list)
            it.onComplete()
        }
    }

    fun getOffset(): Int {
        return if (moreCursor != null) {
            moreCursor.offset
        } else 0
    }

    fun setOffset(offset: Int) {
        moreCursor.number = offset - moreCursor.offset + DEFAULT_LOAD_MORE
        loadMoreRecords(offset)
    }

}