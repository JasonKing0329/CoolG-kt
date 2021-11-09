package com.king.app.coolg_kt.page.match.rank

import android.app.Application
import android.view.View
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import androidx.lifecycle.MutableLiveData
import androidx.sqlite.db.SimpleSQLiteQuery
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.conf.AppConstants
import com.king.app.coolg_kt.conf.MatchConstants
import com.king.app.coolg_kt.model.http.observer.SimpleObserver
import com.king.app.coolg_kt.model.image.ImageProvider
import com.king.app.coolg_kt.model.repository.OrderRepository
import com.king.app.coolg_kt.model.repository.RankRepository
import com.king.app.coolg_kt.page.match.RankItem
import com.king.app.coolg_kt.page.match.ShowPeriod
import com.king.app.coolg_kt.page.match.TimeWasteRange
import com.king.app.coolg_kt.utils.DebugLog
import com.king.app.coolg_kt.utils.TimeCostUtil
import com.king.app.gdb.data.bean.RankLevelCount
import com.king.app.gdb.data.bean.ScoreCount
import com.king.app.gdb.data.entity.FavorRecordOrder
import com.king.app.gdb.data.entity.Record
import com.king.app.gdb.data.entity.Star
import com.king.app.gdb.data.entity.match.MatchRankDetail
import com.king.app.gdb.data.entity.match.MatchRankRecord
import com.king.app.gdb.data.entity.match.MatchRankStar
import com.king.app.gdb.data.relation.MatchRankStarWrap
import com.king.app.gdb.data.relation.RankItemWrap
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableSource
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/1/14 16:28
 */
class RankViewModel(application: Application): BaseViewModel(application) {

    var recordRankList = listOf<RankItem<Record?>>()
    var recordRanksObserver = MutableLiveData<List<RankItem<Record?>>>()
    var starRanksObserver = MutableLiveData<List<RankItem<Star?>>>()
    var imageChanged = MutableLiveData<TimeWasteRange>()
    var detailProgressing = MutableLiveData<Int>()
    var detailProgressError = MutableLiveData<Boolean>()

    var periodGroupVisibility = ObservableInt(View.GONE)
    var periodLastVisibility = ObservableInt(View.GONE)
    var periodNextVisibility = ObservableInt(View.GONE)
    var periodText = ObservableField<String>()

    private var rankRepository = RankRepository()
    private var orderRepository = OrderRepository()

    // 由record/star spinner来触发初始化，recordOrStar设为-1来标识变化
    var periodOrRtf = 0 //0 period, 1 RTF
    var recordOrStar = -1 // 0 record, 1 star
    lateinit var showPeriod : ShowPeriod
    lateinit var currentPeriod: ShowPeriod

    var isSelectMode = false
    var isSelectAllValid = false
    // select模式下显示match level对应的参加次数(current period)
    var mMatchSelectLevel = 0

    var studioList = listOf<FavorRecordOrder>()
    var studioTextList = mutableListOf<String>()
    var studiosObserver = MutableLiveData<List<String>>()

    var rtfNextVisibility = ObservableInt(View.VISIBLE)
    var isPeriodFinalRank = false

    var mOnlyStudioId: Long = 0

    fun initPeriod() {
        val curPack = rankRepository.getRankPeriodPack()
        currentPeriod = if (curPack.matchPeriod == null) {
            ShowPeriod(0, 0)
        } else {
            ShowPeriod(curPack.matchPeriod!!.period, curPack.matchPeriod!!.orderInPeriod)
        }
        val completedPack = rankRepository.getCompletedPeriodPack()
        val showPack = if (isSelectMode) {
            completedPack
        }
        else {
            curPack
        }
        showPeriod = if (showPack.matchPeriod == null) {
            ShowPeriod(0, 0)
        } else {
            ShowPeriod(showPack.matchPeriod!!.period, showPack.matchPeriod!!.orderInPeriod)
        }
    }

    fun loadStudios() {
        getStudios()
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<Boolean>(getComposite()) {
                override fun onNext(t: Boolean?) {
                    studiosObserver.value = studioTextList
                }

                override fun onError(e: Throwable?) {
                    messageObserver.value = e?.message?:""
                }

            })
    }

    fun onPeriodOrRtfChanged(periodOrRtf: Int) {
        if (this.periodOrRtf != periodOrRtf) {
            this.periodOrRtf = periodOrRtf
            if (periodOrRtf == 0) {
                periodGroupVisibility.set(View.VISIBLE)
            }
            else {
                periodGroupVisibility.set(View.GONE)
            }
            loadData()
        }
    }

    fun onRecordOrStarChanged(recordOrStar: Int) {
        if (this.recordOrStar != recordOrStar) {
            this.recordOrStar = recordOrStar
            loadData()
        }
    }

    private fun loadData() {
        if (recordOrStar == 0) {
            if (periodOrRtf == 0) {
                periodGroupVisibility.set(View.VISIBLE)
                loadRecordRankPeriod()
            }
            else {
                periodGroupVisibility.set(View.GONE)
                loadRecordRaceToFinal()
            }
        }
        else {
            if (periodOrRtf == 0) {
                periodGroupVisibility.set(View.VISIBLE)
                loadStarRankPeriod()
            }
            else {
                periodGroupVisibility.set(View.GONE)
                loadStarRaceToFinal()
            }
        }
    }

    fun isLastRecordRankCreated(): Boolean {
        return rankRepository.isRecordRankCreated()
    }

    fun isLastStarRankCreated(): Boolean {
        return rankRepository.isStarRankCreated()
    }

    private fun loadRecordRankPeriod() {
        loadingObserver.value = true
        recordRankPeriodRx()
            .flatMap { toRecordList(it) }
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<List<RankItem<Record?>>>(getComposite()) {
                override fun onNext(t: List<RankItem<Record?>>) {
                    checkRecordLastNext()
                    loadingObserver.value = false
                    recordRankList = t
                    recordRanksObserver.value = t
                    loadDetails()
                }

                override fun onError(e: Throwable?) {
                    loadingObserver.value = false
                    e?.printStackTrace()
                    messageObserver.value = e?.message
                }
            })
    }

    private fun loadDetails() {
        if (mOnlyStudioId > 0) {
            filterOnlyStudio(recordRanksObserver.value, mOnlyStudioId)
                .compose(applySchedulers())
                .subscribe(object : SimpleObserver<List<RankItem<Record?>>?>(getComposite()) {
                    override fun onNext(t: List<RankItem<Record?>>?) {
                        recordRanksObserver.value = t
                        loadBasicDetails()
                    }

                    override fun onError(e: Throwable?) {
                        e?.printStackTrace()
                        messageObserver.value = e?.message?:""
                    }
                })
        }
        else {
            loadBasicDetails()
        }
    }

    private fun filterOnlyStudio(items: List<RankItem<Record?>>?, mOnlyStudioId: Long): Observable<List<RankItem<Record?>>?> {
        return Observable.create {
            val result = items?.filter { item ->
                item.studioId == mOnlyStudioId
            }
            it.onNext(result)
            it.onComplete()
        }
    }

    private fun loadBasicDetails() {
        loadTimeWaste(recordRanksObserver.value)
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<TimeWasteRange>(getComposite()){
                override fun onNext(t: TimeWasteRange) {
                    imageChanged.value = t
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                }
            })

//        loadTimeWaste1(recordRanksObserver.value)
//            .compose(applySchedulers())
//            .subscribe(object : SimpleObserver<TimeWasteRange>(getComposite()){
//                override fun onNext(t: TimeWasteRange) {
//                    imageChanged.value = t
//                }
//
//                override fun onError(e: Throwable?) {
//                    e?.printStackTrace()
//                }
//            })
    }

    private fun recordRankPeriodRx(): Observable<List<RankItemWrap>> {
        periodText.set("P${showPeriod.period}-W${showPeriod.orderInPeriod}")
        // 只有当前week的排名要判断是否统计当前积分，其他情况都从rank表里取
        return if (currentPeriod.period == showPeriod.period && currentPeriod.orderInPeriod == showPeriod.orderInPeriod) {
            // 从match_rank_record表中查询
            if (rankRepository.isRecordRankCreated()) {
                DebugLog.e("record rank from table")
                rankRepository.getRankPeriodRecordRanks()
            }
            // 实时统计积分排名
            else {
                DebugLog.e("record rank from score")
                rankRepository.getRankPeriodRecordScores()
                    .flatMap { toMatchRankRecords(it, false) }
            }
        }
        else {
            // 从match_rank_record表中查询
            DebugLog.e("record rank from table")
            rankRepository.getSpecificPeriodRecordRanks(showPeriod.period, showPeriod.orderInPeriod)
        }

    }

    /**
     * race to final肯定实时统计，不做数据表存储
     */
    private fun loadRecordRaceToFinal() {
        loadingObserver.value = true
        rankRepository.getRTFRecordScores()
            .flatMap { toMatchRankRecords(it, isSelectMode) }
            .flatMap { toRecordList(it) }
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<List<RankItem<Record?>>>(getComposite()) {
                override fun onNext(t: List<RankItem<Record?>>) {
                    loadingObserver.value = false
                    recordRankList = t
                    recordRanksObserver.value = t
                    loadDetails()
                }

                override fun onError(e: Throwable?) {
                    loadingObserver.value = false
                    e?.printStackTrace()
                    messageObserver.value = e?.message
                }
            })
    }

    private fun loadStarRankPeriod() {
        loadingObserver.value = true
        starRankPeriodRx()
            .flatMap { toStarList(it) }
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<List<RankItem<Star?>>>(getComposite()) {
                override fun onNext(t: List<RankItem<Star?>>?) {
                    checkStarLastNext()
                    loadingObserver.value = false
                    starRanksObserver.value = t
                }

                override fun onError(e: Throwable?) {
                    loadingObserver.value = false
                    e?.printStackTrace()
                    messageObserver.value = e?.message
                }
            })
    }

    private fun starRankPeriodRx(): Observable<List<MatchRankStarWrap>> {
        periodText.set("P${showPeriod.period}-W${showPeriod.orderInPeriod}")
        // 只有当前week的排名要判断是否统计当前积分，其他情况都从rank表里取
        return if (currentPeriod.period == showPeriod.period && currentPeriod.orderInPeriod == showPeriod.orderInPeriod) {
            // 从match_rank_record表中查询
            return if (rankRepository.isStarRankCreated()) {
                DebugLog.e("star rank from table")
                rankRepository.getRankPeriodStarRanks()
            }
            // 实时统计积分排名
            else {
                DebugLog.e("star rank from score")
                rankRepository.getRankPeriodStarScores()
                    .flatMap { toMatchRankStars(it) }
            }
        }
        else {
            // 从match_rank_record表中查询
            DebugLog.e("star rank from table")
            rankRepository.getSpecificPeriodStarRanks(showPeriod.period, showPeriod.orderInPeriod)
        }
    }

    /**
     * race to final肯定实时统计，不做数据表存储
     */
    private fun loadStarRaceToFinal() {
        loadingObserver.value = true
        rankRepository.getRTFStarScores()
            .flatMap { toMatchRankStars(it) }
            .flatMap { toStarList(it) }
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<List<RankItem<Star?>>>(getComposite()) {
                override fun onNext(t: List<RankItem<Star?>>?) {
                    loadingObserver.value = false
                    starRanksObserver.value = t
                }

                override fun onError(e: Throwable?) {
                    loadingObserver.value = false
                    e?.printStackTrace()
                    messageObserver.value = e?.message
                }
            })
    }

    private fun toMatchRankRecords(list: List<ScoreCount>, loadDetail: Boolean): ObservableSource<List<RankItemWrap>> {
        return ObservableSource {
            TimeCostUtil.start()
            var result = mutableListOf<RankItemWrap>()
            list.forEachIndexed { index, scoreCount ->
                var record = getDatabase().getRecordDao().getRecordBasic(scoreCount.id)
                // 只有在select模式下需要使用
                var detail = if (loadDetail) {
                    getDatabase().getMatchDao().getMatchRankDetail(scoreCount.id)
                }
                else {
                    null
                }
                var wrap = RankItemWrap(
                    MatchRankRecord(0, 0, 0,
                    scoreCount.id, index + 1, scoreCount.score, scoreCount.matchCount)
                    , 0, "", record, detail
                )
                wrap.unAvailableScore = scoreCount.unavailableScore
                result.add(wrap)
            }
            TimeCostUtil.end("toMatchRankRecords")
            it.onNext(result)
            it.onComplete()
        }
    }

    private fun toRecordList(list: List<RankItemWrap>): ObservableSource<List<RankItem<Record?>>> {
        return ObservableSource {
            TimeCostUtil.start()
            var lastRanks = listOf<RankItemWrap>()
            // period加载变化
            if (periodOrRtf == 0) {
                // 当前period的上一站
                val lp = rankRepository.getLastPeriod(showPeriod)
                lastRanks = rankRepository.specificPeriodRecordRanks(lp.period, lp.orderInPeriod)
            }
            var result = mutableListOf<RankItem<Record?>>()
            list.forEach { bean ->
                // studioName, levelMatchCount从detail表里取（该表的数据在create rank时生成）
                val studioName = bean.studioName?:""
                val studioId = bean.studioId?:0
                // 只有select模式显示levelMatchCount
                val levelMatchCount = if (isSelectMode) {
                    bean.details?.let { detail ->
                        val count = when(mMatchSelectLevel) {
                            MatchConstants.MATCH_LEVEL_GS -> detail.gsCount
                            MatchConstants.MATCH_LEVEL_GM1000 -> detail.gm1000Count
                            MatchConstants.MATCH_LEVEL_GM500 -> detail.gm500Count
                            MatchConstants.MATCH_LEVEL_GM250 -> detail.gm250Count
                            MatchConstants.MATCH_LEVEL_LOW -> detail.lowCount
                            else -> 0
                        }
                        "${MatchConstants.MATCH_LEVEL[mMatchSelectLevel]} $count"
                    }
                }
                else ""
                // 加载图片路径属于耗时操作，不在这里进行，由后续异步加载
                var item = RankItem(bean.record, bean.bean.recordId, bean.bean.rank, "",
                    null, bean.record?.name, bean.bean.score, bean.bean.matchCount, bean.unAvailableScore,
                    studioId, studioName, true, levelMatchCount)
                result.add(item)

                // 上一站存在才加载变化
                if (lastRanks.isNotEmpty()) {
                    val lastRank = lastRanks.firstOrNull { it.bean.recordId == bean.bean.recordId }
                    item.change = if (lastRank == null) {
                        "New"
                    }
                    else {
                        when {
                            lastRank.bean.rank > bean.bean.rank -> "+${lastRank.bean.rank - bean.bean.rank}"
                            bean.bean.rank > lastRank.bean.rank -> "-${bean.bean.rank - lastRank.bean.rank}"
                            else -> "0"
                        }
                    }
                }
            }
            TimeCostUtil.end("toRecordList")
            it.onNext(result)
            it.onComplete()
        }
    }

    /**
     * 给1000+条加载图片路径属于耗时操作（经测试1200个record耗时2秒）,改为先显示列表后陆续加载
     * 另外，将其他耗时操作也在此进行，每30条通知一次更新
     */
    private fun loadTimeWaste(items: List<RankItem<Record?>>?): Observable<TimeWasteRange> {
        return Observable.create {
            var samePeriodMap = if (isSelectAllValid) {
                listOf()
            }
            else {
                getDatabase().getMatchDao().getSamePeriodRecordIds(currentPeriod.period, currentPeriod.orderInPeriod)
            }
            items?.let { list ->
                var count = 0
                var totalNotified = 0
                list.forEach { item ->
                    // 每30条通知一次
                    var url = ImageProvider.getRecordRandomPath(item.bean?.name, null)
                    item.imageUrl = url
                    // 是否可选
                    if (isSelectMode && samePeriodMap.contains(item.id)) {
                        item.canSelect = false
                    }

                    count ++
                    if (count % 30 == 0) {
                        it.onNext(TimeWasteRange(count - 30, count))
                        totalNotified = count
                    }
                }
                if (totalNotified != list.size) {
                    it.onNext(TimeWasteRange(totalNotified, list.size - totalNotified))
                }
            }
            it.onComplete()
        }
    }

    private fun createDetail(bean: RankLevelCount, map: MutableMap<Long, MatchRankDetail?>): MatchRankDetail? {
        var detail = map[bean.recordId]
        if (detail == null) {
            detail = MatchRankDetail(bean.recordId, 0, null, 0, 0, 0, 0, 0)
            map[bean.recordId] = detail
        }
        detail?.apply {
            when(bean.level) {
                MatchConstants.MATCH_LEVEL_GS -> gsCount = bean.count
                MatchConstants.MATCH_LEVEL_GM1000 -> gm1000Count = bean.count
                MatchConstants.MATCH_LEVEL_GM500 -> gm500Count = bean.count
                MatchConstants.MATCH_LEVEL_GM250 -> gm250Count = bean.count
                MatchConstants.MATCH_LEVEL_LOW -> lowCount = bean.count
                else -> {}
            }
            // studio
            orderRepository.getRecordStudio(bean.recordId)?.let { studio ->
                studioId = studio.id!!
                studioName = studio.name
            }
            return this
        }
        return null
    }

    /**
     * 当数据表数据量达到80W+时
     * studio获取较快，1300左右的item耗时在1s以内
     * 逐个统计level则非常耗时，全部下来要30秒左右
     */
    @Deprecated("太耗时")
    private fun createDetail(recordId: Long): MatchRankDetail {
        var detail = MatchRankDetail(recordId, 0, null, 0, 0, 0, 0, 0)
        // studio
        orderRepository.getRecordStudio(recordId)?.let { studio ->
            detail.studioId = studio.id!!
            detail.studioName = studio.name
        }
        // studio获取不算耗时，全部下来仅在1秒内，但是对level的统计非常耗时
        var items = rankRepository.getRecordCurRankRangeMatches(recordId)
        for (item in items) {
            getDatabase().getMatchDao().getMatchPeriod(item)?.match?.let { match ->
                when(match.level) {
                    MatchConstants.MATCH_LEVEL_GS -> detail.gsCount++
                    MatchConstants.MATCH_LEVEL_GM1000 -> detail.gm1000Count++
                    MatchConstants.MATCH_LEVEL_GM500 -> detail.gm500Count++
                    MatchConstants.MATCH_LEVEL_GM250 -> detail.gm250Count++
                    MatchConstants.MATCH_LEVEL_LOW -> detail.lowCount++
                    else -> {}
                }
            }
        }
        return detail
    }

    /**
     * 给1000+条加载match level更加耗时,单列出来异步加载
     */
    @Deprecated("实时统计当前参加level数量的情况。属于非常耗时的操作，改为create rank时创建新表，加载的时候从新表中连接查询")
    private fun loadTimeWaste1(items: List<RankItem<Record?>>?): Observable<TimeWasteRange> {
        return Observable.create {
            var count = 0
            var totalNotified = 0
            items?.let { list ->
                list.forEach { item ->
                    // 每30条通知一次
                    // match level
                    if (isSelectMode) {
                        var items = rankRepository.getRecordCurRankRangeMatches(item.bean!!.id!!)
                        var count = 0
                        for (item in items) {
                            getDatabase().getMatchDao().getMatchPeriod(item)?.match?.let { match ->
                                if (match.level == mMatchSelectLevel) {
                                    count ++
                                }
                            }
                        }
                        item.levelMatchCount = "${MatchConstants.MATCH_LEVEL[mMatchSelectLevel]} $count"
                    }
                    count ++
                    if (count % 30 == 0) {
                        it.onNext(TimeWasteRange(count - 30, count))
                        totalNotified = count
                    }
                }
                if (totalNotified != list.size) {
                    it.onNext(TimeWasteRange(totalNotified, list.size - totalNotified))
                }
            }
            it.onComplete()
        }
    }

    private fun toMatchRankStars(list: List<ScoreCount>): ObservableSource<List<MatchRankStarWrap>> {
        return ObservableSource {
            var result = mutableListOf<MatchRankStarWrap>()
            list.forEachIndexed { index, scoreCount ->
                var star = getDatabase().getStarDao().getStar(scoreCount.id)
                result.add(MatchRankStarWrap(
                    MatchRankStar(0, 0, 0,
                        scoreCount.id, index + 1, scoreCount.score, scoreCount.matchCount), star
                ))
            }
            it.onNext(result)
            it.onComplete()
        }
    }

    private fun toStarList(list: List<MatchRankStarWrap>): ObservableSource<List<RankItem<Star?>>> {
        return ObservableSource {
            var result = mutableListOf<RankItem<Star?>>()
            list.forEach { bean ->
                var url = ImageProvider.getStarRandomPath(bean.star?.name, null)
                var item = RankItem(bean.star, bean.bean.starId, bean.bean.rank, ""
                    , url, bean.star?.name, bean.bean.score, bean.bean.matchCount)
                result.add(item)
            }
            it.onNext(result)
            it.onComplete()
        }
    }

    private fun checkRecordLastNext() {
        // last
        val last = if (isPeriodFinalRank) {
            rankRepository.getLastPeriodFinal(showPeriod)
        }
        else {
            rankRepository.getLastPeriod(showPeriod)
        }
        if (last.period > 0 && getDatabase().getMatchDao().countRecordRankItems(last.period, last.orderInPeriod) > 0) {
            periodLastVisibility.set(View.VISIBLE)
        }
        else {
            periodLastVisibility.set(View.INVISIBLE)
        }
        // next
        val next = if (isPeriodFinalRank) {
            rankRepository.getNextPeriodFinal(showPeriod)
        }
        else {
            rankRepository.getNextPeriod(showPeriod)
        }
        if (next.period == currentPeriod.period && next.orderInPeriod == currentPeriod.orderInPeriod ||
            getDatabase().getMatchDao().countRecordRankItems(next.period, next.orderInPeriod) > 0) {
            periodNextVisibility.set(View.VISIBLE)
        }
        else {
            periodNextVisibility.set(View.INVISIBLE)
        }
    }

    private fun checkStarLastNext() {
        // last
        var last = rankRepository.getLastPeriod(showPeriod)
        if (last.period > 0 && getDatabase().getMatchDao().countStarRankItems(last.period, last.orderInPeriod) > 0) {
            periodLastVisibility.set(View.VISIBLE)
        }
        else {
            periodLastVisibility.set(View.INVISIBLE)
        }
        // next
        var next = rankRepository.getNextPeriod(showPeriod)
        if (next.period == currentPeriod.period && next.orderInPeriod == currentPeriod.orderInPeriod ||
            getDatabase().getMatchDao().countStarRankItems(next.period, next.orderInPeriod) > 0) {
            periodNextVisibility.set(View.VISIBLE)
        }
        else {
            periodNextVisibility.set(View.INVISIBLE)
        }
    }

    fun createRankRecord() {
        insertRankRecordList()
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<Boolean>(getComposite()) {
                override fun onNext(t: Boolean?) {
                    messageObserver.value = "create rank success"
                    createRankDetails()
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                    messageObserver.value = e?.message
                }
            })
    }

    fun createRankStar() {
        insertRankStarList()
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<Boolean>(getComposite()) {
                override fun onNext(t: Boolean?) {
                    messageObserver.value = "success"
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                    messageObserver.value = e?.message
                }
            })
    }

    private fun insertRankRecordList(): Observable<Boolean> {
        return Observable.create {
            rankRepository.getRankPeriodPack().matchPeriod?.let { matchPeriod ->
                getDatabase().getMatchDao().deleteMatchRankRecords(matchPeriod.period, matchPeriod.orderInPeriod)
                var insertList = mutableListOf<MatchRankRecord>()
                recordRanksObserver.value?.forEachIndexed { index, rankItem ->
                    val bean = MatchRankRecord(0, matchPeriod.period, matchPeriod.orderInPeriod,
                        rankItem.id, index + 1, rankItem.score, rankItem.matchCount)
                    insertList.add(bean)
                }
                getDatabase().getMatchDao().insertMatchRankRecords(insertList)
            }
            it.onNext(true)
            it.onComplete()
        }
    }

    fun createRankDetails() {
        detailProgressing.value = 0
        insertDetailsProgress()
            .compose(applySchedulers())
            .subscribe(object : Observer<Int> {
                override fun onSubscribe(d: Disposable) {
                    addDisposable(d)
                }

                override fun onNext(t: Int) {
                    detailProgressing.value = t
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                    detailProgressError.value = true
                    messageObserver.value = e?.message?:""
                }

                override fun onComplete() {
                    detailProgressing.value = 100
                    // 刷新列表
                    initPeriod()
                    loadData()
                }
            })
    }

    private fun insertDetailsProgress(): Observable<Int> {
        return Observable.create {
            var insertDetailList = mutableListOf<MatchRankDetail>()
            val total = recordRanksObserver.value?.size?:0
            val insertPart = 1// insert预留1%作为最后一步
            var progress = 0

            var map = mutableMapOf<Long, MatchRankDetail?>()
            // 废弃逐个统计level的方法，太耗时
//            recordRanksObserver.value?.forEachIndexed { index, rankItem ->
//                val detail = createDetail(rankItem.id)
//                insertDetailList.add(detail)
//                val curProgress = ((index.toDouble() + 1)/(total.toDouble() + insertPart) * 100).toInt()
//                if (curProgress != progress) {
//                    progress = curProgress
//                    it.onNext(progress)
//                }
//            }

            // 直接SQL统计RTF周期内，recordId->level->count，大大降低耗时，从先前的30秒直接降到1秒左右
            rankRepository.getRankLevelCount().forEachIndexed { index, bean ->
                val detail = createDetail(bean, map)
                detail?.let { d ->
                    insertDetailList.add(d)
                }
                val curProgress = ((index.toDouble() + 1)/(total.toDouble() + insertPart) * 100).toInt()
                if (curProgress != progress) {
                    progress = curProgress
                    it.onNext(progress)
                }
            }
            // orderInPeriod为1，重置所有record的level count全为0
            if (currentPeriod.orderInPeriod == 1) {
                getDatabase().getMatchDao().resetRankDetails()
            }
            // 积分周期内有参赛的record，新增或修改detail
            getDatabase().getMatchDao().insertOrReplaceMatchRankDetails(insertDetailList)
            it.onComplete()
        }
    }

    fun createRankDetailItems() {
        detailProgressing.value = 0
        insertDetailItemsProgress()
            .compose(applySchedulers())
            .subscribe(object : Observer<Int> {
                override fun onSubscribe(d: Disposable) {
                    addDisposable(d)
                }

                override fun onNext(t: Int) {
                    detailProgressing.value = t
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                    detailProgressError.value = true
                    messageObserver.value = e?.message?:""
                }

                override fun onComplete() {
                    detailProgressing.value = 100
                    // 刷新列表
                    initPeriod()
                    loadData()
                }
            })
    }

    private fun insertDetailItemsProgress(): Observable<Int> {
        return Observable.create {
            var allRecords = getDatabase().getRecordDao().getAllBasicRecords()
            var insertDetailList = mutableListOf<MatchRankDetail>()
            val total = allRecords.size
            val insertPart = 1// insert预留1%作为最后一步
            var progress = 0

            allRecords.forEachIndexed { index, record ->
                val studio = orderRepository.getRecordStudio(record.id!!)
                val studioId = studio?.id?:0
                val studioName = studio?.name?:""
                insertDetailList.add(MatchRankDetail(record.id!!, studioId, studioName, 0, 0, 0, 0, 0))

                val curProgress = ((index.toDouble() + 1)/(total.toDouble() + insertPart) * 100).toInt()
                if (curProgress != progress) {
                    progress = curProgress
                    it.onNext(progress)
                }
            }

            // 新增或修改detail
            getDatabase().getMatchDao().insertOrReplaceMatchRankDetails(insertDetailList)
            it.onComplete()
        }
    }

    private fun insertRankStarList(): Observable<Boolean> {
        return Observable.create {
            rankRepository.getRankPeriodPack().matchPeriod?.let { matchPeriod ->
                getDatabase().getMatchDao().deleteMatchRankStars(matchPeriod.period, matchPeriod.orderInPeriod)
                var insertList = mutableListOf<MatchRankStar>()
                starRanksObserver.value?.forEachIndexed { index, rankItem ->
                    val bean = MatchRankStar(0, matchPeriod.period, matchPeriod.orderInPeriod,
                        rankItem.id, index + 1, rankItem.score, rankItem.matchCount)
                    insertList.add(bean)
                }
                getDatabase().getMatchDao().insertMatchRankStars(insertList)
            }
            it.onNext(true)
            it.onComplete()
        }
    }

    fun targetPeriod(period: Int, orderInPeriod: Int) {
        showPeriod = if (isPeriodFinalRank) {
            ShowPeriod(period, showPeriod.orderInPeriod)
        }
        else {
            ShowPeriod(period, orderInPeriod)
        }
        loadData()
    }

    fun nextPeriod() {
        showPeriod = if (isPeriodFinalRank) {
            rankRepository.getNextPeriodFinal(showPeriod)
        }
        else {
            rankRepository.getNextPeriod(showPeriod)
        }
        loadData()
    }

    fun lastPeriod() {
        showPeriod = if (isPeriodFinalRank) {
            rankRepository.getLastPeriodFinal(showPeriod)
        }
        else {
            rankRepository.getLastPeriod(showPeriod)
        }
        loadData()
    }

    private fun getStudios(): Observable<Boolean> {
        return Observable.create {
            val studio = getDatabase().getFavorDao().getRecordOrderByName(AppConstants.ORDER_STUDIO_NAME)
            studio?.let { parent ->
                var sqlBuffer = StringBuffer("select * from favor_order_record where PARENT_ID=");
                sqlBuffer.append(parent.id).append(" order by NAME")
                studioList = getDatabase().getFavorDao().getRecordOrdersBySql(SimpleSQLiteQuery(sqlBuffer.toString()))
            }

            studioTextList.add("All")
            studioList.forEach { studio ->
                studioTextList.add(studio.name?:"zzz_unknown")
            }
            it.onNext(true)
            it.onComplete()
        }
    }

    fun filterByStudio(position: Int) {
        if (position == -1) {
            studiosObserver.value = studioTextList
        }
        else {
            var studioName = studioList[position].name
            var list = recordRankList?.filter { it.studioName == studioName }
            recordRanksObserver.value = list
        }
    }

    fun findStudioPosition(studioId: Long): Int {
        val studio = getDatabase().getFavorDao().getFavorRecordOrderBy(studioId)
        studiosObserver.value?.forEachIndexed { index, name ->
            if (name == studio?.name) {
                return index
            }
        }
        return 0
    }

    fun loadPeriodFinalRank() {
        isPeriodFinalRank = true
        rtfNextVisibility.set(View.INVISIBLE)
        showPeriod = findLastPeriodFinal()
        loadData()
    }

    private fun findLastPeriodFinal(): ShowPeriod {
        val pack = rankRepository.getCompletedPeriodPack()
        return if (pack.endPIO == MatchConstants.MAX_ORDER_IN_PERIOD) {
            ShowPeriod(pack.endPeriod, pack.endPIO)
        } else {
            ShowPeriod(pack.endPeriod - 1, MatchConstants.MAX_ORDER_IN_PERIOD)
        }
    }

}