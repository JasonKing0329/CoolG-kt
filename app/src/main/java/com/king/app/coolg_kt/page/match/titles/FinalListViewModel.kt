package com.king.app.coolg_kt.page.match.titles

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.conf.MatchConstants
import com.king.app.coolg_kt.model.http.observer.SimpleObserver
import com.king.app.coolg_kt.model.image.ImageProvider
import com.king.app.coolg_kt.model.repository.RankRepository
import com.king.app.coolg_kt.page.match.FinalListItem
import com.king.app.coolg_kt.page.match.TimeWasteRange
import com.king.app.coolg_kt.page.match.TitleCountItem
import com.king.app.gdb.data.relation.MatchRecordWrap
import io.reactivex.rxjava3.core.Observable

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/1/22 13:09
 */
class FinalListViewModel(application: Application): BaseViewModel(application) {

    var dataObserver = MutableLiveData<List<FinalListItem>>()
    var imageChanged = MutableLiveData<TimeWasteRange>()

    var titlesCountObserver = MutableLiveData<List<Any>>()

    private var mFilterLevel = MatchConstants.MATCH_LEVEL_ALL
    private var rankRepository = RankRepository()

    fun loadData() {
        getFinals(mFilterLevel)
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<List<FinalListItem>>(getComposite()){
                override fun onNext(t: List<FinalListItem>) {
                    dataObserver.value = t
                    // 加载耗时操作
                    loadTimeWaste(t)
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                    messageObserver.value = e?.message
                }
            })
    }

    private fun getFinals(level: Int): Observable<List<FinalListItem>> {
        return Observable.create {
            val list = mutableListOf<FinalListItem>()
            val matchItems = if (level == MatchConstants.MATCH_LEVEL_ALL) {
                getDatabase().getMatchDao().getMatchItemsByRound(MatchConstants.ROUND_ID_F)
            }
            else {
                getDatabase().getMatchDao().getMatchItemsByRoundLevel(MatchConstants.ROUND_ID_F, level)
            }
            matchItems.forEach { wrap ->
                val matchPeriod = getDatabase().getMatchDao().getMatchPeriod(wrap.bean.matchId)
                val winner = wrap.recordList.first { it.recordId == wrap.bean.winnerId }
                val loser = wrap.recordList.first { it.recordId != wrap.bean.winnerId }
                val winnerRecord = getDatabase().getRecordDao().getRecordBasic(winner.recordId)
                val loserRecord = getDatabase().getRecordDao().getRecordBasic(loser.recordId)
                val r = MatchRecordWrap(winner, winnerRecord)
                val l = MatchRecordWrap(loser, loserRecord)
                // 加载image属于耗时操作，后面再加载
                list.add(FinalListItem(matchPeriod, r, l))
            }
            it.onNext(list)
            it.onComplete()
        }
    }

    private fun loadTimeWaste(list: List<FinalListItem>) {
        finalItemsTimeWaste(list)
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<TimeWasteRange>(getComposite()){
                override fun onNext(t: TimeWasteRange) {
                    imageChanged.value = t
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                }
            })
    }

    /**
     * 加载耗时操作
     */
    private fun finalItemsTimeWaste(list: List<FinalListItem>): Observable<TimeWasteRange> {
        return Observable.create {
            var count = 0
            var totalNotified = 0
            list.forEach { item ->
                item.recordWin.imageUrl = ImageProvider.getRecordRandomPath(item.recordWin.record?.name, null)
                item.recordLose.imageUrl = ImageProvider.getRecordRandomPath(item.recordLose.record?.name, null)
                count ++
                // 每20个通知一次
                if (count % 20 == 0) {
                    it.onNext(TimeWasteRange(count - 20, count))
                    totalNotified = count
                }
            }
            if (totalNotified != list.size) {
                it.onNext(TimeWasteRange(totalNotified, list.size - totalNotified))
            }
            it.onComplete()
        }
    }

    fun filterByLevel(levelIndex: Int) {
        mFilterLevel = levelIndex
        loadData()
    }

    fun filterTitlesCountByLevel(levelIndex: Int) {
        mFilterLevel = levelIndex
        loadTitlesCount()
    }

    fun loadTitlesCount() {
        getTitlesCount()
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<List<Any>>(getComposite()) {
                override fun onNext(t: List<Any>) {
                    titlesCountObserver.value = t
                    loadTitlesCountTimeWaste(t)
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                    messageObserver.value = e?.message?:""
                }

            })
    }

    private fun getTitlesCount(): Observable<List<Any>> {
        return Observable.create {
            var list = mutableListOf<Any>()
            val items = if (mFilterLevel == MatchConstants.MATCH_LEVEL_ALL) {
                getDatabase().getMatchDao().countRecordRound(MatchConstants.ROUND_ID_F)
            }
            else {
                getDatabase().getMatchDao().countRecordRoundByLevel(MatchConstants.ROUND_ID_F, mFilterLevel)
            }
            var lastCount = 0
            var countMap = mutableMapOf<Int, Int>()
            items.forEach { item ->
                if (item.num != lastCount) {
                    list.add("${item.num} Titles")
                    lastCount = item.num
                }
                getDatabase().getRecordDao().getRecordBasic(item.winnerId)?.let { record ->
                    countMap[item.num] = (countMap[item.num] ?:0) + 1
                    // rank, imageUrl都可以放到耗时操作里后续加载
                    list.add(TitleCountItem(record, item.num, 0, false))
                }
            }
            // 根据countMap确认item是否在分组下唯一
            list.forEach { obj ->
                if (obj is TitleCountItem) {
                    val count = countMap[obj.titles]?:0
                    if (count == 1) {
                        obj.isOnlyOne = true
                    }
                }
            }

            it.onNext(list)
            it.onComplete()
        }
    }

    private fun loadTitlesCountTimeWaste(list: List<Any>) {
        titlesCountTimeWaste(list)
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<TimeWasteRange>(getComposite()){
                override fun onNext(t: TimeWasteRange) {
                    imageChanged.value = t
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                }
            })
    }

    /**
     * 加载耗时操作
     */
    private fun titlesCountTimeWaste(list: List<Any>): Observable<TimeWasteRange> {
        return Observable.create {
            var count = 0
            var totalNotified = 0
            list.forEach { item ->
                if (item is TitleCountItem) {
                    // image
                    item.imageUrl = ImageProvider.getRecordRandomPath(item.record.name, null)
                    // rank
                    item.rank = rankRepository.getRecordCurrentRank(item.record.id!!)
                    // details
                    titleDetails(item)
                }
                count ++
                // 每20个通知一次
                if (count % 20 == 0) {
                    it.onNext(TimeWasteRange(count - 20, count))
                    totalNotified = count
                }
            }
            if (totalNotified != list.size) {
                it.onNext(TimeWasteRange(totalNotified, list.size - totalNotified))
            }
            it.onComplete()
        }
    }

    private fun titleDetails(item: TitleCountItem) {
        val finalList = if (mFilterLevel == MatchConstants.MATCH_LEVEL_ALL) {
            getDatabase().getMatchDao().getRecordMatchItemsByRound(item.record.id!!, MatchConstants.ROUND_ID_F)
        }
        else {
            getDatabase().getMatchDao().getRecordMatchItemsByRoundLevel(item.record.id!!, MatchConstants.ROUND_ID_F, mFilterLevel)
        }
        val buffer = StringBuffer()
        val countMap = mutableMapOf<String, Int>()
        finalList.forEach {
            if (it.bean.winnerId == item.record.id!!) {
                val mp = getDatabase().getMatchDao().getMatchPeriod(it.bean.matchId).bean
                val match = getDatabase().getMatchDao().getMatch(mp.matchId)
                when(mFilterLevel) {
                    // 按level统计
                    MatchConstants.MATCH_LEVEL_ALL -> {
                        countMap[match.level.toString()] = (countMap[match.level.toString()]?:0) + 1
                    }
                    // 按match统计
                    MatchConstants.MATCH_LEVEL_GS, MatchConstants.MATCH_LEVEL_GM1000 -> {
                        countMap[match.id.toString()] = (countMap[match.id.toString()]?:0) + 1
                    }
                    // 其他都按period统计
                    else -> {
                        countMap[mp.period.toString()] = (countMap[mp.period.toString()]?:0) + 1
                    }
                }
            }
        }
        when(mFilterLevel) {
            // 按level统计
            MatchConstants.MATCH_LEVEL_ALL -> {
                countMap.keys.sortedBy { it.toInt() }
                    .forEach { buffer.append("\n").append(MatchConstants.MATCH_LEVEL[it.toInt()]).append("(").append(countMap[it]).append(")") }
            }
            // 按match统计
            MatchConstants.MATCH_LEVEL_GS -> {
                countMap.keys
                    .map {
                        val matchId = it.toLong()
                        val match = getDatabase().getMatchDao().getMatch(matchId)
                        match
                    }
                    .sortedBy { it.orderInPeriod }
                    .forEach {
                        buffer.append("\n").append(it.name).append("(").append(countMap[it.id.toString()]).append(")")
                    }
            }
            // 按match统计
            MatchConstants.MATCH_LEVEL_GM1000 -> {
                countMap.keys
                    .map {
                        val matchId = it.toLong()
                        val match = getDatabase().getMatchDao().getMatch(matchId)
                        match
                    }
                    .sortedBy { it.orderInPeriod }
                    .forEach {
                        // 取大写字母
                        buffer.append("  ").append(getMatchShortName(it.name)).append("(").append(countMap[it.id.toString()]).append(")")
                    }
            }
            // 其他都按period统计
            else -> {
                countMap.keys.sortedBy { it.toInt() }
                    .forEach { buffer.append("  P").append(it.toInt()).append("(").append(countMap[it]).append(")") }
            }
        }
        var details = buffer.toString()
        if (details.isNotEmpty()) {
            details = details.substring(1)
        }
        item.details = details
    }

    private fun getMatchShortName(name: String): String {
        val buffer = StringBuffer()
        for (char in name) {
            if (char in 'A'..'Z') {
                buffer.append(char)
            }
        }
        return buffer.toString()
    }
}