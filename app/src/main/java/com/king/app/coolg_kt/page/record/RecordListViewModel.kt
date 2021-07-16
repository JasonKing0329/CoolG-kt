package com.king.app.coolg_kt.page.record

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.model.http.observer.SimpleObserver
import com.king.app.coolg_kt.model.image.ImageProvider
import com.king.app.coolg_kt.model.module.BasicAndTimeWaste
import com.king.app.coolg_kt.model.module.TimeWasteTask
import com.king.app.coolg_kt.model.repository.PlayRepository
import com.king.app.coolg_kt.model.repository.RankRepository
import com.king.app.coolg_kt.model.repository.RecordRepository
import com.king.app.coolg_kt.model.setting.SettingProperty
import com.king.app.coolg_kt.page.match.TimeWasteRange
import com.king.app.coolg_kt.page.record.popup.RecommendBean
import com.king.app.gdb.data.RecordCursor
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

    var recordsObserver: MutableLiveData<List<RecordWrap>> = MutableLiveData()
    var moreObserver: MutableLiveData<Int> = MutableLiveData()
    var scrollPositionObserver: MutableLiveData<Int> = MutableLiveData()
    var rangeChangedObserver: MutableLiveData<TimeWasteRange> = MutableLiveData()

    private var mRecordList = mutableListOf<RecordWrap>()

    private var recordRepository = RecordRepository()

    private var mSortMode = 0
    private var mSortDesc = false

    var mRecommendBean: RecommendBean? = null

    var mRecordToPlayOrder: Record? = null

    private var moreCursor = RecordCursor()

    private var playRepository = PlayRepository()

    private var factor = RecordsFragment.Factor()

    var selectAsMatchItem = false

    init {
        onSortTypeChanged()
    }

    var samePeriodItems: List<Long>? = null

    fun getNotNullRecommendBean(): RecommendBean {
        return if (mRecommendBean == null) RecommendBean()
        else mRecommendBean!!
    }

    fun onSortTypeChanged() {
        mSortMode = SettingProperty.getRecordSortType()
        mSortDesc = SettingProperty.isRecordSortDesc()
    }

    fun newRecordCursor() {
        moreCursor = RecordCursor()
        moreCursor.number = DEFAULT_LOAD_MORE
    }

    fun reloadRecords() {
        mRecordList.clear()
        // 偏移量从0开始
        newRecordCursor()
        loadRecords()
    }

    private fun loadRecords() {
        if (factor.outOfRank) {
            loadRecordsOutOfRank()
        }
        else {
            loadRecordsByFilter()
        }
    }

    private fun loadRecordsOutOfRank() {
        BasicAndTimeWaste<RecordWrap>()
            .basic(recordRepository.getRecordsOutOfRank())
            .timeWaste(outOfRankWaste(), 20)
            .composite(getComposite())
            .subscribe(
                object : SimpleObserver<List<RecordWrap>>(getComposite()) {
                    override fun onNext(t: List<RecordWrap>) {
                        mRecordList.addAll(t)
                        recordsObserver.value = mRecordList
                    }

                    override fun onError(e: Throwable?) {
                        e?.printStackTrace()
                        messageObserver.value = e?.message?:""
                    }
                },
                object : SimpleObserver<TimeWasteRange>(getComposite()) {
                    override fun onNext(t: TimeWasteRange) {
                        rangeChangedObserver.value = t
                    }

                    override fun onError(e: Throwable?) {
                        e?.printStackTrace()
                        messageObserver.value = e?.message?:""
                    }
                }
            )
    }

    private fun loadRecordsByFilter() {
        queryRecords()
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

    private fun outOfRankWaste(): TimeWasteTask<RecordWrap> {
        return object : TimeWasteTask<RecordWrap> {
            override fun handle(index: Int, data: RecordWrap) {
                // match选择模式下，标记已在draw下的item
                // samePeriodItems只加载一次
                if (selectAsMatchItem && samePeriodItems == null) {
                    loadSamePeriodItems()
                }
                itemDetail(data)
            }
        }
    }

    fun loadMoreRecords() {
        loadMoreRecords(null)
    }

    private fun loadMoreRecords(scrollPosition: Int?) {
        val originSize = mRecordList.size
        queryRecords()
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

    private fun queryRecords(): Observable<List<RecordWrap>> {
        return recordRepository.getRecordFilter(mSortMode, mSortDesc, factor.recordType, factor.starId
            , factor.orderId, factor.tagId, moreCursor, mRecommendBean, factor.keyword, factor.scene, factor.outOfRank)
            .flatMap { filter -> recordRepository.getRecords(filter) }
            .flatMap { list ->  toFilterViewItems(list)};
    }

    /**
     * item details, 耗时操作
     */
    private fun itemDetail(record: RecordWrap) {
        var name = record.bean.name?:""
        record.imageUrl = ImageProvider.getRecordRandomPath(name, null)
        // 默认都可选
        record.canSelect = true
        // match选择模式下，标记已在draw下的item
        samePeriodItems?.let {
            record.canSelect = !it.contains(record.bean.id)
        }
    }

    /**
     * match选择模式下，标记已在draw下的item
     */
    private fun loadSamePeriodItems() {
        var rankPack = RankRepository().getRankPeriodPack()
        rankPack.matchPeriod?.let { matchPeriod ->
            samePeriodItems = getDatabase().getMatchDao().getSamePeriodRecordIds(matchPeriod.period, matchPeriod.orderInPeriod)
        }
        // 保证只被加载一次
        if (samePeriodItems == null) {
            samePeriodItems = mutableListOf()
        }
    }

    /**
     * 通过filter模式一次只加载N个items，所以，耗时操作可以直接一次flatmap
     */
    private fun toFilterViewItems(list: List<RecordWrap>): ObservableSource<List<RecordWrap>> {
        moreCursor.offset += list.size
        return ObservableSource {
            // match选择模式下，标记已在draw下的item
            if (selectAsMatchItem) {
                loadSamePeriodItems()
            }

            list.forEach { record ->
                itemDetail(record)
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

    fun updateFactors(factor: RecordsFragment.Factor) {
        this.factor = factor
    }
}