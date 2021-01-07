package com.king.app.coolg_kt.page.record

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.conf.AppConstants
import com.king.app.coolg_kt.model.http.observer.SimpleObserver
import com.king.app.coolg_kt.model.image.ImageProvider
import com.king.app.coolg_kt.model.repository.PlayRepository
import com.king.app.coolg_kt.model.repository.RecordRepository
import com.king.app.coolg_kt.model.repository.TagRepository
import com.king.app.coolg_kt.model.setting.SettingProperty
import com.king.app.coolg_kt.page.record.popup.RecommendBean
import com.king.app.gdb.data.DataConstants
import com.king.app.gdb.data.RecordCursor
import com.king.app.gdb.data.bean.RecordScene
import com.king.app.gdb.data.entity.Record
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

    var tagsObserver: MutableLiveData<List<RecordTag>> = MutableLiveData()
    var recordsObserver: MutableLiveData<List<RecordWrap>> = MutableLiveData()
    var moreObserver: MutableLiveData<Int> = MutableLiveData()
    var scrollPositionObserver: MutableLiveData<Int> = MutableLiveData()
    var focusTagPosition: MutableLiveData<Int> = MutableLiveData()

    private var mTagType = SettingProperty.getRecordListTagType()
    private var mTagSortType = SettingProperty.getTagSortType()

    private var dataTagList: List<RecordTag> = mutableListOf()

    private var mRecordList = mutableListOf<RecordWrap>()

    private var tagRepository = TagRepository()
    private var recordRepository = RecordRepository()

    private var mSortMode = 0
    private var mSortDesc = false
    private var mKeyScene: String? = null
    private val mKeyword: String? = null
    private val mStarId: Long = 0
    var mOrderId: Long = 0
    private val mRecordType: Int = 0

    private var tagAll = RecordTag(AppConstants.KEY_SCENE_ALL, 0L, 0)
    private var mTagBean = tagAll
    private var mTagId: Long = 0

    var mRecommendBean: RecommendBean? = null

    var mRecordToPlayOrder: Record? = null

    private var moreCursor = RecordCursor()

    private var playRepository = PlayRepository()

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

    fun loadHead() {
        // studio records page, hide tag bar and related menu
        if (mOrderId != 0L) {
            val allList: List<RecordTag> = addTagAll(dataTagList)
            tagsObserver.value = allList
            loadTagRecords(allList[0].id!!)
        }
        else {
            var type = SettingProperty.getRecordListTagType()
            if (type == 1) {
                loadScenes()
            }
            else {
                loadTags()
            }
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
                    loadTagRecords(allList[0].id!!)
                    focusTagPosition.value = 0
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                    messageObserver.value = e?.message
                }

            })
    }

    private fun convertTags(): Observable<List<RecordTag>> {
        return Observable.create {
            var list = tagRepository.loadTags(DataConstants.TAG_TYPE_RECORD)
            var result = mutableListOf<RecordTag>()
            list.forEach { tag ->
                var recordTag = RecordTag(tag.name?:"", tag.id!!)
                recordTag.number = getDatabase().getTagDao().countRecordTagItems(tag.id!!)
                result.add(recordTag)
            }
            it.onNext(result)
            it.onComplete()
        }
    }

    private fun loadScenes() {
        dataTagList = convertScenes(getDatabase().getRecordDao().getAllScenes())
        sortTags(mTagSortType, dataTagList)
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<List<RecordTag>>(getComposite()) {
                override fun onNext(t: List<RecordTag>) {
                    val allList: List<RecordTag> = addTagAll(t)
                    tagsObserver.value = allList
                    loadSceneRecords(allList[0].name)
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
            it.name?.let { name -> result.add(RecordTag(name, 0L, it.number)) }
        }
        return result
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

    fun newRecordCursor() {
        moreCursor = RecordCursor()
        moreCursor.number = DEFAULT_LOAD_MORE
    }

    fun loadRecordsByTag() {
        loadRecordsByTag(mTagBean)
    }

    fun loadRecordsByTag(bean: RecordTag) {
        mTagBean = bean
        if (mTagType == 1) {
            loadSceneRecords(bean.name)
        }
        else {
            loadTagRecords(bean.id)
        }
    }

    private fun loadTagRecords(tagId: Long) {
        mRecordList.clear()
        mTagId = tagId
        mKeyScene = null
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

    private fun loadSceneRecords(scene: String) {
        mRecordList.clear()
        mTagId = 0
        mKeyScene = if (scene == AppConstants.KEY_SCENE_ALL) null
            else scene
        // 偏移量从0开始
        newRecordCursor()
        queryRecords(mTagId)
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

    fun saveRecordToPlayOrder(record: Record) {
        mRecordToPlayOrder = record
    }

    fun addToPlay(list: ArrayList<CharSequence>?) {
        mRecordToPlayOrder?.let {
            playRepository.insertPlayItem(it.id!!, null, list)
                .compose(applySchedulers())
                .subscribe(object : SimpleObserver<Boolean>(getComposite()) {
                    override fun onNext(t: Boolean) {
                        messageObserver.value = "Add successfully"
                    }

                    override fun onError(e: Throwable?) {
                        e?.printStackTrace()
                        messageObserver.value = e?.message
                    }
                })
        }
    }

    fun loadStudioTitle(studioId: Long): String {
        var studio = getDatabase().getFavorDao().getFavorRecordOrderBy(studioId)
        var title: String? = null
        studio?.let {
            title = it.name
        }
        return title?:"Records"
    }
}