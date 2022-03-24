package com.king.app.coolg_kt.page.match.item

import android.app.Application
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.conf.MatchConstants
import com.king.app.coolg_kt.model.image.ImageProvider
import com.king.app.coolg_kt.model.repository.RankRepository
import com.king.app.coolg_kt.page.match.*
import com.king.app.coolg_kt.page.match.detail.CareerMatchViewModel
import com.king.app.gdb.data.bean.DataForRoundCount
import com.king.app.gdb.data.entity.match.MatchItem
import kotlinx.coroutines.Job
import java.text.SimpleDateFormat
import java.util.*

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/4/26 14:55
 */
class MatchViewModel(application: Application): BaseViewModel(application) {

    var matchId: Long = 0

    var matchImage = ObservableField<String>()
    var matchName = ObservableField<String>()
    var matchLevel = ObservableField<String>()

    var itemsObserver = MutableLiveData<List<MatchSemiPack>>()
    var semiItemsRange = MutableLiveData<TimeWasteRange>()
    var repository = RankRepository()
    var dateFormat = SimpleDateFormat("yyyy-MM-dd")

    var countItemsObserver = MutableLiveData<List<Any>>()
    var countItemsRange = MutableLiveData<TimeWasteRange>()

    var roundItemsObserver = MutableLiveData<List<Any>>()
    var roundItemsRange = MutableLiveData<TimeWasteRange>()

    private var semiItemsJob: Job? = null
    private var timesJob: Job? = null
    private var bestJob: Job? = null

    fun loadItems() {
        var match = getDatabase().getMatchDao().getMatch(matchId)
        matchImage.set(ImageProvider.parseCoverUrl(match.imgUrl))
        matchName.set(match.name)
        matchLevel.set("${MatchConstants.MATCH_LEVEL[match.level]} ")

        cancelAll()
        semiItemsJob = basicAndTimeWaste(
            blockBasic = { querySemiItemsUpgrade() },
            onCompleteBasic = { itemsObserver.value = it },
            blockWaste = { index, it ->  handleSemiWaste(index, it) },
            wasteNotifyCount = 1,
            onWasteRangeChanged = { start, count -> semiItemsRange.value = TimeWasteRange(start, count) },
            withBasicLoading = true
        )
    }

    private fun cancelAll() {
        semiItemsJob?.cancel()
        timesJob?.cancel()
        bestJob?.cancel()
    }

//    @Deprecated(
//        "repository.getMatchSemiItems的方式太耗时，当match_record膨胀到40W+后，耗时达到5秒以上",
//        replaceWith = ReplaceWith("querySemiItemsUpgrade", "")
//    )
//    private fun querySemiItems(): Observable<List<MatchSemiPack>> {
//        return Observable.create {
//            var list = mutableListOf<MatchSemiPack>()
//            var matches = getDatabase().getMatchDao().getMatchPeriods(matchId)
//            matches.forEach { mp ->
//                var items = mutableListOf<MatchSemiItem>()
//                var pack = MatchSemiPack(mp.id, "P${mp.period}", dateFormat.format(Date(mp.date)), items)
//                list.add(pack)
//                var records = repository.getMatchSemiItems(mp)
//                if (records.size < 4) {
//                    // 4个以内表示未完成
//                }
//                else {
//                    records.forEach { record ->
//                        var rank = if (record.recordSeed?:0 > 0) {
//                            "[${record.recordSeed}]/${record.recordRank}"
//                        }
//                        else {
//                            record.recordRank?.toString()?:""
//                        }
//                        // imageUrl与rankNow属于耗时操作，不在这里加载
//                        var item = MatchSemiItem(record.recordId, 0, rank)
//                        items.add(item)
//                    }
//                }
//                // imageUrl与rankNow属于耗时操作，不在这里加载
//            }
//            it.onNext(list)
//            it.onComplete()
//        }
//    }

    /**
     * 通过Sql语句先查询出matchId对应的所有sf, f的记录，再对记录进行period分组与轮次排序，大大缩减了时间（40W+数据也能在500ms以内）
     */
    private fun querySemiItemsUpgrade(): List<MatchSemiPack> {
        var list = mutableListOf<MatchSemiPack>()
        getDatabase().getMatchDao().queryMatchSemiRecords(matchId).forEach { msr ->
            var pack = list.firstOrNull { pack -> pack.matchPeriodId == msr.matchPeriodId }
            if (pack == null) {
                pack = MatchSemiPack(msr.matchPeriodId, "P${msr.period}", dateFormat.format(Date(msr.matchPeriodDate)), mutableListOf())
                list.add(pack)
            }
            var rank = if (msr.recordSeed > 0) {
                "[${msr.recordSeed}]/${msr.recordRank}"
            }
            else {
                msr.recordRank.toString()
            }
            if (msr.round == MatchConstants.ROUND_ID_F) {
                // 根据SemiGroup的规则，0是冠军，1是亚军，2,3是四强
                // sql里已按round排序，决赛在前，四强在后，但是冠亚军还需要区别一下，冠军作为第0个插入
                if (msr.recordId == msr.winnerId) {
                    pack.items.add(0, MatchSemiItem(msr.recordId, msr.round, rank))
                }
                else {
                    // 亚军在冠军之后，四强之前
                    if (pack.items.size > 0) {
                        if (pack.items[0].round == MatchConstants.ROUND_ID_F) {
                            pack.items.add(1, MatchSemiItem(msr.recordId, msr.round, rank))
                        }
                        else {
                            pack.items.add(0, MatchSemiItem(msr.recordId, msr.round, rank))
                        }
                    }
                    else {
                        pack.items.add(MatchSemiItem(msr.recordId, msr.round, rank))
                    }
                }
            }
            // SF只加输掉的一方
            else {
                if (msr.recordId != msr.winnerId) {
                    pack.items.add(MatchSemiItem(msr.recordId, msr.round, rank))
                }
            }
        }
        return list
    }

    private fun handleSemiWaste(index: Int, data: MatchSemiPack) {
        data.items.forEach { item ->
            val bean = getDatabase().getRecordDao().getRecordBasic(item.recordId)
            item.imageUrl = ImageProvider.getRecordRandomPath(bean?.name, null)
            // 加载currentRank非常耗时，需求上只需要第一个显示即可
            if (index == 0) {
                val rankNow = repository.getRecordCurrentRank(item.recordId)
                item.rankNow = if (rankNow == -1) {
                    "Now 9999"
                }
                else {
                    "Now $rankNow"
                }
            }
        }
    }

    fun loadCount() {
        cancelAll()
        timesJob = basicAndTimeWaste(
            blockBasic = { loadRecords() },
            onCompleteBasic = { countItemsObserver.value = it },
            blockWaste = { _, it ->  handleTimesWaste(it) },
            wasteNotifyCount = 5,
            onWasteRangeChanged = { start, count -> countItemsRange.value = TimeWasteRange(start, count) },
            withBasicLoading = true
        )
    }

    private fun loadRecords(): List<Any> {
        val allRecords = mutableListOf<MatchCountRecord>()
        val list = mutableListOf<Any>()
        val records = getDatabase().getMatchDao().getMatchParticipates(matchId)
        val map = mutableMapOf<Long, MatchCountRecord?>()
        records.forEach { record ->
            var bean = map[record.recordId]
            if (bean == null) {
                bean = MatchCountRecord(record.recordId)
                map[record.recordId] = bean
                allRecords.add(bean)
            }
            else {
                bean.count ++
            }
        }
        val countMap = mutableMapOf<Int, MutableList<MatchCountRecord>?>()
        allRecords.forEach { item ->
            var items = countMap[item.count]
            if (items == null) {
                items = mutableListOf()
                countMap[item.count] = items
            }
            items.add(item)
        }

        countMap.keys.sortedDescending().forEach { key ->

            // title
            list.add(MatchCountTitle("$key ${if (key == 1) "time" else "times"} in"))
            // items
            countMap[key]?.apply {
                list.addAll(this)
            }
        }
        return list
    }

    private fun handleTimesWaste(data: Any) {
        if (data is MatchCountRecord) {
            // image
            val record = getDatabase().getRecordDao().getRecordBasic(data.recordId)
            data.imgUrl = ImageProvider.getRecordRandomPath(record?.name, null)

            // rank，不参与排序，因为如果在basic里面加载太过耗时
            val rank = repository.getRecordCurrentRank(data.recordId)
            data.rankSeed = if (rank == -1) "9999" else rank.toString()

            val items = getDatabase().getMatchDao().getRecordMatchItems(data.recordId, matchId)
            var win = 0
            var lose = 0
            val periodMap = mutableMapOf<Long, MutableList<MatchItem>?>()
            items.forEach { item ->
                if (item.winnerId == data.recordId) {
                    win ++
                }
                else if (item.winnerId?:0L != 0L) {
                    lose ++
                }
                var list = periodMap[item.matchId]
                if (list == null) {
                    list = mutableListOf()
                    periodMap[item.matchId] = list
                }
                list.add(item)
            }
            // win lose
            data.winLose = "${win}胜${lose}负"
            // best，只取最好的2个轮次
            val countList = mutableListOf<CareerMatchViewModel.MatchCount>()
            periodMap.keys.forEach { matchPeriodId ->
                val count = toMatchCount(data.recordId, matchPeriodId, periodMap[matchPeriodId]!!)
                countList.add(count)
            }
            countList.sortByDescending { it.roundWeight }
            val bestMap = mutableMapOf<Int, MutableList<CareerMatchViewModel.MatchCount>?>()
            for (i in countList.indices) {
                var bm = bestMap[countList[i].roundWeight]
                if (bm == null) {
                    if (bestMap.keys.size == 2) {
                        break
                    }
                    bm = mutableListOf()
                    bestMap[countList[i].roundWeight] = bm
                }
                bm.add(countList[i])
            }
            bestMap.keys.sortedDescending().forEachIndexed { index, key ->
                if (index == 0) {
                    data.best = toPeriodRound(bestMap[key]!!)
                }
                else if (index == 1) {
                    data.second = toPeriodRound(bestMap[key]!!)
                }
            }
        }
    }

    private fun toMatchCount(recordId: Long, matchPeriodId: Long, list: List<MatchItem>): CareerMatchViewModel.MatchCount {
        val matchPeriod = getDatabase().getMatchDao().getMatchPeriod(matchPeriodId)
        var maxWeight = -9999
        var maxRoundItem: MatchItem? = null
        list.forEach {
            var weight = MatchConstants.getRoundSortValue(it.round)
            // Win要跟F分开算
            if (it.round == MatchConstants.ROUND_ID_F && it.winnerId == recordId) {
                weight ++
            }
            if (weight > maxWeight) {
                maxWeight = weight
                maxRoundItem = it
            }
        }
        val roundShort = MatchConstants.roundResultShort(maxRoundItem!!.round, maxRoundItem!!.winnerId == recordId)
        return CareerMatchViewModel.MatchCount(
            matchPeriodId,
            matchPeriod.bean.period,
            roundShort,
            maxWeight
        )
    }

    private fun toPeriodRound(list: List<CareerMatchViewModel.MatchCount>): String {
        val buffer = StringBuffer()
        buffer.append(list[0].roundShort).append("(")
        list.forEachIndexed { index, it ->
            if (index > 0) {
                buffer.append(",")
            }
            buffer.append("P").append(it.period)
        }
        buffer.append(")  ")
        return buffer.toString()
    }

    fun loadRoundItems() {
        cancelAll()
        bestJob = basicAndTimeWaste(
            blockBasic = { loadRoundRecords() },
            onCompleteBasic = { roundItemsObserver.value = it },
            blockWaste = { _, it ->  handleBestWaste(it) },
            wasteNotifyCount = 5,
            onWasteRangeChanged = { start, count -> roundItemsRange.value = TimeWasteRange(start, count) },
            withBasicLoading = true
        )
    }

    private fun loadRoundRecords(): List<Any> {
        val list = mutableListOf<Any>()
        // 只统计R16及以上的轮次
        val finals = getDatabase().getMatchDao().getMatchRoundItems(matchId, MatchConstants.ROUND_ID_F)
        list.add(MatchCountTitle("Winner"))
        list.addAll(toRoundItems(finals, true))
        list.add(MatchCountTitle("Runner-up"))
        list.addAll(toRoundItems(finals, false))
        val semis = getDatabase().getMatchDao().getMatchRoundItems(matchId, MatchConstants.ROUND_ID_SF)
        list.add(MatchCountTitle(MatchConstants.roundFull(MatchConstants.ROUND_ID_SF)))
        list.addAll(toRoundItems(semis, false))
        val qfs = getDatabase().getMatchDao().getMatchRoundItems(matchId, MatchConstants.ROUND_ID_QF)
        list.add(MatchCountTitle(MatchConstants.roundFull(MatchConstants.ROUND_ID_QF)))
        list.addAll(toRoundItems(qfs, false))
        val r16s = getDatabase().getMatchDao().getMatchRoundItems(matchId, MatchConstants.ROUND_ID_16)
        list.add(MatchCountTitle(MatchConstants.roundFull(MatchConstants.ROUND_ID_16)))
        list.addAll(toRoundItems(r16s, false))
        return list
    }

    private fun toRoundItems(items: List<DataForRoundCount>, isWinner: Boolean): List<MatchRoundRecord> {
        val result = mutableListOf<MatchRoundRecord>()
        items
            .filter {
                if (isWinner) it.recordId == it.winnerId
                else it.winnerId != null && it.recordId != it.winnerId
            }
            .forEach {
                var item = result.firstOrNull { item -> item.recordId == it.recordId }
                if (item == null) {
                    item = MatchRoundRecord(it.recordId)
                    result.add(item)
                }
                item.periodList.add(it.period)
            }
        // sort by times and format text
        result.sortByDescending { it.periodList.size }
        result.forEach {
            val buffer = StringBuffer()
            it.periodList.forEachIndexed { index, period ->
                if (index > 0) {
                    buffer.append(",")
                }
                buffer.append("P").append(period)
            }
            it.times = if (it.periodList.size == 1) "1 time"
            else "${it.periodList.size} times"
            it.periods = buffer.toString()
        }
        return result
    }

    private fun handleBestWaste(data: Any) {
        if (data is MatchRoundRecord) {
            // image
            val record = getDatabase().getRecordDao().getRecordBasic(data.recordId)
            data.imgUrl = ImageProvider.getRecordRandomPath(record?.name, null)

            // rank，不参与排序，因为如果在basic里面加载太过耗时
            val rank = repository.getRecordCurrentRank(data.recordId)
            data.rankSeed = if (rank == -1) "9999" else rank.toString()
        }
    }
}