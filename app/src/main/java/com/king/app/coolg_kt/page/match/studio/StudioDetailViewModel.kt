package com.king.app.coolg_kt.page.match.studio

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.conf.MatchConstants
import com.king.app.coolg_kt.model.image.ImageProvider
import com.king.app.coolg_kt.model.repository.RankRepository
import com.king.app.coolg_kt.page.match.StudioItem
import com.king.app.coolg_kt.page.match.StudioTitle
import com.king.app.coolg_kt.page.match.TimeWasteRange
import com.king.app.gdb.data.entity.Record
import com.king.app.gdb.data.entity.match.TempHighRank
import com.king.app.gdb.data.relation.RankItemWrap
import com.king.app.gdb.data.relation.StudioChampionWrap

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/11/24 10:31
 */
class StudioDetailViewModel(application: Application): BaseViewModel(application) {

    companion object {
        val FLAG_TITLE_CUR_HIGH = "cur_high"
        val FLAG_TITLE_HISTORY_TOP = "history_top"
        val FLAG_TITLE_CHAMPION_GS = "champions_gs"
        val FLAG_TITLE_CHAMPION_GM1000 = "champions_gm1000"
        val FLAG_TITLE_CHAMPION = "champions"
    }

    var studioId: Long = 0
    val rankRepository = RankRepository()

    var data = MutableLiveData<List<Any>>()
    var rangeObserver = MutableLiveData<TimeWasteRange>()
    var imageMap = mutableMapOf<Long, String>()

    fun loadStudioData(studioId: Long) {
        this.studioId = studioId
        loadingObserver.value = true
        basicAndTimeWaste(
            blockBasic = { createData() },
            onCompleteBasic = { data.value = it },
            blockWaste = { _, it ->  handleItem(it) },
            wasteNotifyCount = 3,
            onWasteRangeChanged = { start, count -> rangeObserver.value = TimeWasteRange(start, count) },
            withBasicLoading = true
        )
    }

    private fun getImageUrl(record: Record?): String? {
        record?.let {
            var url = imageMap[it.id]
            if (url == null) {
                url = ImageProvider.getRecordRandomPath(it.name, null)
            }
            return url
        }
        return null
    }

    private fun createData(): List<Any> {
        val result = mutableListOf<Any>()
        val curRanks = rankRepository.getStudioRankPeriodRecordRanks(studioId)
        if (curRanks.isNotEmpty()) {
            // current high
            result.add(StudioTitle(FLAG_TITLE_CUR_HIGH, " Current Top ", true))
            val item = curRanks[0]
            var studioItem = StudioItem(FLAG_TITLE_CUR_HIGH, 3, item, item.record)
            result.add(studioItem)
            studioItem.currentRank = "R-${item.bean.rank}"
        }
        // 显示9个历史最高。先取20个，处理出现并列的情况可超过9个
        val historyHighItems = getDatabase().getMatchDao().getStudioHighRank(studioId, 20)
        if (historyHighItems.isNotEmpty()) {
            result.add(StudioTitle(FLAG_TITLE_HISTORY_TOP, " History Top ", false))
            var count = 0
            var lastRank = 0
            for (item in historyHighItems) {
                getDatabase().getRecordDao().getRecordBasic(item.recordId)?.let { record ->
                    var studioItem = StudioItem(FLAG_TITLE_HISTORY_TOP, 1, item, record)
                    result.add(studioItem)
                }
                count ++
                // 达到9个，且没有并列排名了，跳出
                if (count >= 9 && item.high != lastRank) {
                    break
                }
                lastRank = item.high
            }
        }

        val champions = getDatabase().getMatchDao().queryStudioRecordLevelChampions(studioId)
        // GS champion
        val gs = champions.filter { item -> item.level == MatchConstants.MATCH_LEVEL_GS }.sortedByDescending { it.levelCount }
        if (gs.isNotEmpty()) {
            result.add(StudioTitle(FLAG_TITLE_CHAMPION_GS, " Grand Slam ", false))
            gs.forEachIndexed { index, wrap ->
                val column = if (gs.size == 1) 3 else 1
                var studioItem = StudioItem(FLAG_TITLE_CHAMPION_GS, column, wrap, wrap.record)
                result.add(studioItem)
            }
        }
        // GM1000 champion
        val gm1000 = champions.filter { item -> item.level == MatchConstants.MATCH_LEVEL_GM1000 }.sortedByDescending { it.levelCount }
        if (gm1000.isNotEmpty()) {
            result.add(StudioTitle(FLAG_TITLE_CHAMPION_GM1000, " GM1000 ", false))
            gm1000.forEach { wrap ->
                val column = if (gm1000.size == 1) 3 else 1
                var studioItem = StudioItem(FLAG_TITLE_CHAMPION_GM1000, column, wrap, wrap.record)
                result.add(studioItem)
            }
        }

        val allChampions = getDatabase().getMatchDao().queryStudioRecordChampions(studioId).sortedByDescending { it.levelCount }
        // all champions
        if (allChampions.isNotEmpty()) {
            result.add(StudioTitle(FLAG_TITLE_CHAMPION, " All Champions ", false))
            allChampions.forEach { wrap ->
                val column = if (allChampions.size == 1) 3 else 1
                var studioItem = StudioItem(FLAG_TITLE_CHAMPION, column, wrap, wrap.record)
                result.add(studioItem)
            }
        }
        return result
    }

    fun getDetailRankDetails(recordId: Long, rank: Int): Int {
        return getDatabase().getMatchDao().getRecordRanks(recordId, rank).size
    }

    private fun handleItem(data: Any) {
        if (data is StudioItem) {
            val url = getImageUrl(data.record)
            data.imageUrl = url
            when(data.parentFlag) {
                FLAG_TITLE_CUR_HIGH -> {
                    val item = data.dataBean as RankItemWrap
                    val weeks = getDetailRankDetails(item.record?.id?:0, item.bean.rank)
                    data.detail = "$weeks weeks"
                }
                FLAG_TITLE_HISTORY_TOP -> {
                    val item = data.dataBean as TempHighRank
                    val curRank = rankRepository.getRecordCurrentRank(item.recordId)
                    val weeks = getDetailRankDetails(item.recordId, item.high)
                    data.currentRank = "Now $curRank"
                    data.detail = "Top ${item.high}(${weeks} weeks)"
                }
                FLAG_TITLE_CHAMPION_GS, FLAG_TITLE_CHAMPION_GM1000, FLAG_TITLE_CHAMPION -> {
                    val wrap = data.dataBean as StudioChampionWrap
                    val curRank = rankRepository.getRecordCurrentRank(wrap.record.id!!)
                    data.currentRank = "R-$curRank"
                    data.detail = "${wrap.levelCount}"
                }
            }
        }
    }

}