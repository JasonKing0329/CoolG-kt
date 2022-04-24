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
import com.king.app.coolg_kt.model.extension.printCostTime
import com.king.app.coolg_kt.model.image.ImageProvider
import com.king.app.coolg_kt.model.repository.OrderRepository
import com.king.app.coolg_kt.model.repository.RankRepository
import com.king.app.coolg_kt.page.match.RankItem
import com.king.app.coolg_kt.page.match.ShowPeriod
import com.king.app.coolg_kt.page.match.TimeWasteRange
import com.king.app.coolg_kt.utils.DebugLog
import com.king.app.gdb.data.bean.RankLevelCount
import com.king.app.gdb.data.bean.ScoreCount
import com.king.app.gdb.data.entity.FavorRecordOrder
import com.king.app.gdb.data.entity.Record
import com.king.app.gdb.data.entity.Star
import com.king.app.gdb.data.entity.match.MatchRankDetail
import com.king.app.gdb.data.entity.match.MatchRankRecord
import com.king.app.gdb.data.relation.RankItemWrap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext

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

    var periodOrRtf = 0 //0 period, 1 RTF
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

    var samePeriodMap: List<Long>? = null

    var rankPeriodJob: Job? = null
    var rtfJob: Job? = null

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
        launchSingle(
            { getStudios() },
            withLoading = false
        ) {
            studiosObserver.value = studioTextList
        }
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

    fun loadRanks() {
        loadData()
    }

    private fun loadData() {
        if (periodOrRtf == 0) {
            periodGroupVisibility.set(View.VISIBLE)
            loadRecordRankPeriod()
        }
        else {
            periodGroupVisibility.set(View.GONE)
            loadRecordRaceToFinal()
        }
    }

    fun isLastRecordRankCreated(): Boolean {
        return rankRepository.isRecordRankCreated()
    }

    private fun cancelAll() {
        rtfJob?.cancel()
        rankPeriodJob?.cancel()
    }

    /**
     * 给1000+条加载图片路径属于耗时操作（经测试1200个record耗时2秒）,改为先显示列表后陆续加载
     * 另外，将其他耗时操作也在此进行，每30条通知一次更新
     */
    private fun loadRecordRankPeriod() {
        cancelAll()
        rankPeriodJob = basicAndTimeWaste(
            blockBasic = {
                val allList = recordRankPeriodRx()
                var viewList = toRecordList(allList)
                if (mOnlyStudioId > 0) {
                    viewList = filterOnlyStudio(viewList, mOnlyStudioId)
                }
                viewList
            },
            onCompleteBasic = {
                checkRecordLastNext()
                recordRankList = it
                recordRanksObserver.value = it
            },
            withBasicLoading = true,
            blockWaste = { index, it ->  handleRankWaste(index, it) },
            wasteNotifyCount = 30,
            onWasteRangeChanged = { start, count -> imageChanged.value = TimeWasteRange(start, count)}
        )
    }

    private fun handleRankWaste(index: Int, item: RankItem<Record?>) {
        if (samePeriodMap == null) {
            samePeriodMap = if (isSelectAllValid) {
                listOf()
            }
            else {
                getDatabase().getMatchDao().getSamePeriodRecordIds(currentPeriod.period, currentPeriod.orderInPeriod)
            }
        }
        var url = ImageProvider.getRecordRandomPath(item.bean?.name, null)
        item.imageUrl = url
        // 是否可选
        if (isSelectMode && samePeriodMap?.contains(item.id) == true) {
            item.canSelect = false
        }
    }

    private fun filterOnlyStudio(items: List<RankItem<Record?>>, mOnlyStudioId: Long): List<RankItem<Record?>> {
        return items.filter { it.studioId == mOnlyStudioId }
    }

    private fun recordRankPeriodRx(): List<RankItemWrap> {
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
                val scores = rankRepository.getRankPeriodRecordScores()
                toMatchRankRecords(scores, false)
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
        cancelAll()
        rtfJob = basicAndTimeWaste(
            blockBasic = {
                val scores = rankRepository.getRTFRecordScores()
                var allList = toMatchRankRecords(scores, isSelectMode)
                var viewList = toRecordList(allList)
                if (mOnlyStudioId > 0) {
                    viewList = filterOnlyStudio(viewList, mOnlyStudioId)
                }
                viewList
            },
            onCompleteBasic = {
                checkRecordLastNext()
                recordRankList = it
                recordRanksObserver.value = it
            },
            withBasicLoading = true,
            blockWaste = { index, it ->  handleRankWaste(index, it) },
            wasteNotifyCount = 30,
            onWasteRangeChanged = { start, count -> imageChanged.value = TimeWasteRange(start, count)}
        )
    }

    private fun toMatchRankRecords(list: List<ScoreCount>, loadDetail: Boolean): List<RankItemWrap> {
        var result = mutableListOf<RankItemWrap>()
        printCostTime("toMatchRankRecords") {
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
        }
        return result
    }

    private fun toRecordList(list: List<RankItemWrap>): List<RankItem<Record?>> {
        var result = mutableListOf<RankItem<Record?>>()
        printCostTime("toRecordList") {
            var lastRanks = listOf<RankItemWrap>()
            // period加载变化
            if (periodOrRtf == 0) {
                // 当前period的上一站
                val lp = rankRepository.getLastPeriod(showPeriod)
                lastRanks = rankRepository.getSpecificPeriodRecordRanks(lp.period, lp.orderInPeriod)
            }
            list.forEach { bean ->
                // studioName, levelMatchCount从detail表里取（该表的数据在create rank时生成）
                val studioName = bean.studioName?:""
                val studioId = bean.studioId?:0
                // 只有select模式显示levelMatchCount
                var levelMatchCountText = ""
                var levelMatchCount = 0
                if (isSelectMode) {
                    bean.details?.let { detail ->
                        val count = when(mMatchSelectLevel) {
                            MatchConstants.MATCH_LEVEL_GS -> detail.gsCount
                            MatchConstants.MATCH_LEVEL_GM1000 -> detail.gm1000Count
                            MatchConstants.MATCH_LEVEL_GM500 -> detail.gm500Count
                            MatchConstants.MATCH_LEVEL_GM250 -> detail.gm250Count
                            MatchConstants.MATCH_LEVEL_LOW -> detail.lowCount
                            MatchConstants.MATCH_LEVEL_MICRO -> detail.microCount
                            else -> 0
                        }
                        levelMatchCount = count
                        levelMatchCountText = "${MatchConstants.MATCH_LEVEL[mMatchSelectLevel]} $count"
                    }
                }
                // 加载图片路径属于耗时操作，不在这里进行，由后续异步加载
                var item = RankItem(bean.record, bean.bean.recordId, bean.bean.rank, "",
                    null, bean.record?.name, bean.bean.score, bean.bean.matchCount, bean.unAvailableScore,
                    studioId, studioName, true, levelMatchCount, levelMatchCountText)
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
        }
        return result
    }

    private fun createDetail(bean: RankLevelCount, map: MutableMap<Long, MatchRankDetail?>): MatchRankDetail {
        var detail = map[bean.recordId]
        if (detail == null) {
            detail = MatchRankDetail(bean.recordId, 0, null, 0, 0, 0, 0, 0, 0)
            map[bean.recordId] = detail
        }
        detail?.apply {
            when(bean.level) {
                MatchConstants.MATCH_LEVEL_GS -> gsCount = bean.count
                MatchConstants.MATCH_LEVEL_GM1000 -> gm1000Count = bean.count
                MatchConstants.MATCH_LEVEL_GM500 -> gm500Count = bean.count
                MatchConstants.MATCH_LEVEL_GM250 -> gm250Count = bean.count
                MatchConstants.MATCH_LEVEL_LOW -> lowCount = bean.count
                MatchConstants.MATCH_LEVEL_MICRO -> microCount = bean.count
                else -> {}
            }
            // studio
            orderRepository.getRecordStudio(bean.recordId)?.let { studio ->
                studioId = studio.id!!
                studioName = studio.name
            }
            return this
        }
        return detail
    }

    /**
     * 当数据表数据量达到80W+时
     * studio获取较快，1300左右的item耗时在1s以内
     * 逐个统计level则非常耗时，全部下来要30秒左右
     */
    @Deprecated("太耗时")
    private fun createDetail(recordId: Long): MatchRankDetail {
        var detail = MatchRankDetail(recordId, 0, null, 0, 0, 0, 0, 0, 0)
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
                    MatchConstants.MATCH_LEVEL_MICRO -> detail.microCount++
                    else -> {}
                }
            }
        }
        return detail
    }

    /**
     * 给1000+条加载match level更加耗时,单列出来异步加载
     */
//    @Deprecated("实时统计当前参加level数量的情况。属于非常耗时的操作，改为create rank时创建新表，加载的时候从新表中连接查询")
//    private fun loadTimeWaste1(items: List<RankItem<Record?>>?): Observable<TimeWasteRange> {
//        return Observable.create {
//            var count = 0
//            var totalNotified = 0
//            items?.let { list ->
//                list.forEach { item ->
//                    // 每30条通知一次
//                    // match level
//                    if (isSelectMode) {
//                        var items = rankRepository.getRecordCurRankRangeMatches(item.bean!!.id!!)
//                        var count = 0
//                        for (item in items) {
//                            getDatabase().getMatchDao().getMatchPeriod(item)?.match?.let { match ->
//                                if (match.level == mMatchSelectLevel) {
//                                    count ++
//                                }
//                            }
//                        }
//                        item.levelMatchCount = "${MatchConstants.MATCH_LEVEL[mMatchSelectLevel]} $count"
//                    }
//                    count ++
//                    if (count % 30 == 0) {
//                        it.onNext(TimeWasteRange(count - 30, count))
//                        totalNotified = count
//                    }
//                }
//                if (totalNotified != list.size) {
//                    it.onNext(TimeWasteRange(totalNotified, list.size - totalNotified))
//                }
//            }
//            it.onComplete()
//        }
//    }

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

    private suspend fun postProgress(progress: Int) = withContext(Dispatchers.Main) {
        detailProgressing.value = progress
    }

    private suspend fun postFinish() = withContext(Dispatchers.Main) {
        detailProgressing.value = 100
        // 刷新列表
        initPeriod()
        loadData()
    }

    /**
     * 进度更新的协程有如下问题：
     * 前提：异步任务本身就耗时又要频繁地通知UI更新进度
     * 如果完全是在main线程的协程下，UI无法达到更新进度显示，会一直卡在0%，甚至进度框都不会显示。
     * 因此，只能将异步任务放在子线程，通知进度更新切换到主线程。
     * 那么这就要两种实现方式：
     * 1.协程创建在main线程下，异步任务切换到子线程
     * 2.协程创建在子线程下，异步任务切换到主线程
     * 最终，考虑到业务与更新进度的结构，最好的实现是协程创建在子线程下，异步任务切换到主线程
     */
    fun createRankRecord(onlyCreateDetails: Boolean = false) {
        detailProgressing.value = 0
        launchThread {
            kotlin.runCatching {
                var startProgress = 0
                if (!onlyCreateDetails) {
                    DebugLog.e("insertRankRecordList")
                    // 先插入rank列表（不耗时)
                    insertRankRecordList()
                    postProgress(10)
                    startProgress = 10
                }
                DebugLog.e("insertDetailsProgress")
                // 再插入rank详情列表(耗时操作，拆分进度)
                insertDetailsProgress(startProgress)
                postFinish()
            }.onFailure {
                it.printStackTrace()
                // onFailure也在thread下
                detailProgressError.postValue(true)
                messageObserver.postValue(it?.message?:"")
            }
        }
    }

    fun createRankDetails() {
        createRankRecord(onlyCreateDetails = true)
    }

    private fun insertRankRecordList() {
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
    }

    private suspend fun insertDetailsProgress(startProgress: Int) {
        var insertDetailList = mutableListOf<MatchRankDetail>()
        val insertPart = 5// insert预留5%作为最后一步
        val highPart = 5// create high预留5%作为最后一步
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

        val totalProgress = 100 - startProgress
        val list = rankRepository.getRankLevelCount()
        val total = list.size
        // 直接SQL统计RTF周期内，recordId->level->count，大大降低耗时，从先前的30秒直接降到1秒左右
        list.forEachIndexed { index, bean ->
            val detail = createDetail(bean, map)
            insertDetailList.add(detail)
            val curProgress = startProgress + ((index.toDouble() + 1.0)/(total.toDouble() + insertPart + highPart) * totalProgress).toInt()
            if (curProgress != progress) {
                progress = curProgress
                DebugLog.e("progress=$progress")
                postProgress(progress)
            }
        }
        // orderInPeriod为1，重置所有record的level count全为0
        if (currentPeriod.orderInPeriod == 1) {
            getDatabase().getMatchDao().resetRankDetails()
        }
        // 积分周期内有参赛的record，新增或修改detail
        getDatabase().getMatchDao().insertOrReplaceMatchRankDetails(insertDetailList)
        postProgress(95)
        // 更新最高排名
        getDatabase().getMatchDao().clearHighRanks()
        postProgress(98)
        getDatabase().getMatchDao().insertAllHighRanks()// 耗时操作
    }

    fun createRankDetailItems() {
        detailProgressing.value = 0
        launchThread {
            kotlin.runCatching {
                insertDetailItemsProgress()
                postFinish()
            }.onFailure {
                it.printStackTrace()
                // onFailure也在thread下
                detailProgressError.postValue(true)
                messageObserver.postValue(it?.message?:"")
            }
        }
    }

    private suspend fun insertDetailItemsProgress() {
        var allRecords = getDatabase().getRecordDao().getAllBasicRecords()
        var insertDetailList = mutableListOf<MatchRankDetail>()
        val total = allRecords.size
        val insertPart = 1// insert预留1%作为最后一步
        var progress = 0

        allRecords.forEachIndexed { index, record ->
            val studio = orderRepository.getRecordStudio(record)
            val studioId = studio?.id?:0
            val studioName = studio?.name?:""
            insertDetailList.add(MatchRankDetail(record.id!!, studioId, studioName, 0, 0, 0, 0, 0, 0))

            val curProgress = ((index.toDouble() + 1)/(total.toDouble() + insertPart) * 100).toInt()
            if (curProgress != progress) {
                progress = curProgress
                postProgress(progress)
            }
        }

        // 新增或修改detail
        getDatabase().getMatchDao().insertOrReplaceMatchRankDetails(insertDetailList)
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

    private fun getStudios() {
        studioList = orderRepository.getAllStudios()
        studioTextList.add("All")
        studioList.forEach { studio ->
            studioTextList.add(studio.name?:"zzz_unknown")
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