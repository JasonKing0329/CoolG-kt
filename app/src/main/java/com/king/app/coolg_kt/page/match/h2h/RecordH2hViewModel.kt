package com.king.app.coolg_kt.page.match.h2h

import android.app.Application
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.model.image.ImageProvider
import com.king.app.coolg_kt.model.repository.RankRepository
import com.king.app.coolg_kt.page.match.RecordH2hItem
import com.king.app.coolg_kt.page.match.TimeWasteRange
import com.king.app.gdb.data.entity.Record
import com.king.app.gdb.data.relation.RecordWrap
import kotlinx.coroutines.Job

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/6/8 14:47
 */
class RecordH2hViewModel(application: Application): BaseViewModel(application) {

    var listObserver = MutableLiveData<List<RecordH2hItem>>()

    var imageChanged = MutableLiveData<TimeWasteRange>()

    var recordNameText = ObservableField<String>()

    var currentRankText = ObservableField<String>()

    var highRankText = ObservableField<String>()

    var highRankWeekText = ObservableField<String>()

    var cptNumText = ObservableField<String>()

    var winLoseText = ObservableField<String>()

    var recordImage: String? = null

    var rankRepository = RankRepository()

    lateinit var recordWrap: RecordWrap

    var h2hJob: Job? = null

    fun loadInfo(recordId: Long) {
        getDatabase().getRecordDao().getRecord(recordId)?.let {
            recordWrap = it

            // basic info
            recordNameText.set(it.bean.name)
            recordImage = ImageProvider.getRecordRandomPath(it.bean.name, null)

            currentRankText.set(rankRepository.getRecordCurrentRank(recordId).toString())
            val high = getDatabase().getMatchDao().getRecordHighestRank(recordId)
            getDatabase().getMatchDao().getRecordRankFirstTime(recordId, high)?.let { mrr ->
                highRankText.set("$high(P${mrr.period}-W${mrr.orderInPeriod})")
            }
            val highWeeks = getDatabase().getMatchDao().getRecordRankWeeks(recordId, high)
            if (highWeeks > 1) {
                highRankWeekText.set("$highWeeks weeks")
            }
            else {
                highRankWeekText.set("$highWeeks week")
            }
            countWinLose()

            // players
            loadPlayers()
        }
    }

    private fun countWinLose() {
        var win = 0
        var lose = 0
        val items = rankRepository.getRecordMatchItemsRange(recordWrap.bean.id!!, rankRepository.getAllTimePeriodPack())
        items.forEach { item ->
            if (item.winnerId == recordWrap.bean.id!!) {
                win ++
            }
            else {
                lose ++
            }
        }
        winLoseText.set("${win}胜${lose}负")
    }

    private fun loadPlayers() {
        h2hJob?.cancel()
        h2hJob = basicAndTimeWaste(
            blockBasic = { getRecordCompetitors(recordWrap.bean) },
            onCompleteBasic = {
                cptNumText.set("${it.size} competitors")
                listObserver.value = it
            },
            blockWaste = { _, it ->  handleItem(it) },
            wasteNotifyCount = 3,
            onWasteRangeChanged = { start, count -> imageChanged.value = TimeWasteRange(start, count) },
            withBasicLoading = true
        )
    }

    /**
     * 加载所有的competitor，并按总交手次数降序排序
     * 这里只加载record与排序权重（总交手次数）
     * 加载图片，统计具体的win, lose属于耗时操作，后续异步加载
     */
    private fun getRecordCompetitors(record: Record): List<RecordH2hItem> {
        var result = mutableListOf<RecordH2hItem>()
        getDatabase().getMatchDao().getRecordCompetitors(record.id!!)?.forEach { cpt ->
            // img, win, lose属于耗时操作，不在这里加载
            result.add(RecordH2hItem(record, null, cpt.record, null, "", 0, 0, cpt.num))
        }
        result.sortByDescending { item -> item.sortValue }
        return result
    }

    private fun handleItem(item: RecordH2hItem) {
        // image
        item.recordImg1 = recordImage
        var url = ImageProvider.getRecordRandomPath(item.record2.name, null)
        item.recordImg2 = url

        // win, lose
        val h2hItems = getDatabase().getMatchDao().getH2hItems(item.record1.id!!, item.record2.id!!)
        var win = 0
        var lose = 0
        h2hItems.forEach { h2h ->
            if (h2h.bean.winnerId != null) {
                if (h2h.bean.winnerId == item.record1.id!!) {
                    win ++
                }
                else {
                    lose ++
                }
            }
        }
        item.win = win
        item.lose = lose

        // rank
        val rank = rankRepository.getRecordCurrentRank(item.record2.id!!)
        item.record2Rank = rank.toString()
    }

}