package com.king.app.coolg_kt.page.match.rank

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.model.image.ImageProvider
import com.king.app.coolg_kt.model.repository.RankRepository
import com.king.app.coolg_kt.page.match.HighRankItem
import com.king.app.coolg_kt.page.match.HighRankTitle
import com.king.app.coolg_kt.page.match.TimeWasteRange

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/6/28 13:47
 */
class HighViewModel(application: Application): BaseViewModel(application) {

    private var rankRepository = RankRepository()
    var itemsObserver = MutableLiveData<List<HighRankTitle>>()
    var rangeChanged = MutableLiveData<TimeWasteRange>()

    fun loadHighestRanks() {
        basicAndTimeWaste(
            blockBasic = { highestRanks() },
            onCompleteBasic = { itemsObserver.value = it },
            blockWaste = { handleRankGroup(it) },
            wasteNotifyCount = 1,
            onWasteRangeChanged = { start, count -> rangeChanged.value = TimeWasteRange(start, count) }
        )
    }

    private fun highestRanks(): List<HighRankTitle> {

        val list = rankRepository.getHighestRankGroup()
        val result = mutableListOf<HighRankTitle>()
        var lastTitle = HighRankTitle(0, mutableListOf())
        list.forEach { item ->
            if (item.rank != lastTitle.rank) {
                lastTitle.rank
                lastTitle = HighRankTitle(item.rank, mutableListOf())
                result.add(lastTitle)
            }
            // curRank, image, detail都属于耗时操作，后续加载
            lastTitle.items.add(HighRankItem(item, lastTitle))
        }
        return result
    }

    private fun handleRankGroup(data: HighRankTitle) {
        data.items.forEach { item ->
            item.curRank = " R ${rankRepository.getRecordCurrentRank(item.bean.record.id!!)} "
            item.imageUrl = ImageProvider.getRecordRandomPath(item.bean.record.name, null)
            // details
            rankRepository.getHighRankDetail(item.bean)
            val buffer = StringBuffer()
            if (item.bean.weeks > 1) {
                buffer.append(item.bean.weeks).append(" weeks")
            }
            else {
                buffer.append(item.bean.weeks).append(" week")
            }
            buffer.append("\n").append("Highest score: ").append(item.bean.highestScore).append("(").append(item.bean.highestScoreTime).append(")")
                .append("\n").append("First time: ").append(item.bean.firstTime)
                .append("\n").append("Last time: ").append(item.bean.lastTime)
            item.details = buffer.toString()
        }

        // 三个关键字都是升序，可以用sortWith
        data.items.sortWith(compareBy({it.bean.rank}, {it.bean.firstPeriod}, {it.bean.firstPIO}))
        // 如果用week,score降序混排，用data class内实现Comparable接口来实现
        // 对items重新排序
//                data.items.sort()
    }
}