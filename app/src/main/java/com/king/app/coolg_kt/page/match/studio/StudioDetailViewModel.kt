package com.king.app.coolg_kt.page.match.studio

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.model.http.observer.SimpleObserver
import com.king.app.coolg_kt.model.image.ImageProvider
import com.king.app.coolg_kt.model.repository.RankRepository
import com.king.app.coolg_kt.page.match.StudioItem
import com.king.app.coolg_kt.page.match.StudioTitle
import io.reactivex.rxjava3.core.Observable

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/11/24 10:31
 */
class StudioDetailViewModel(application: Application): BaseViewModel(application) {

    companion object {
        val FLAG_TITLE_CUR_HIGH = "cur_high"
        val FLAG_TITLE_HISTORY_TOP = "history_top"
    }

    var studioId: Long = 0
    val rankRepository = RankRepository()

    var data = MutableLiveData<List<Any>>()

    fun loadStudioData(studioId: Long) {
        this.studioId = studioId
        loadingObserver.value = true
        createData()
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<List<Any>>(getComposite()){
                override fun onNext(t: List<Any>) {
                    loadingObserver.value = false
                    data.value = t
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                    loadingObserver.value = false
                    messageObserver.value = e?.message?:""
                }
            })
    }

    private fun createData(): Observable<List<Any>> {
        return Observable.create {
            val result = mutableListOf<Any>()
            val curRanks = rankRepository.getStudioRankPeriodRecordRanks(studioId)
            if (curRanks.isNotEmpty()) {
                // current high
                result.add(StudioTitle(FLAG_TITLE_CUR_HIGH, "Current Top", true))
                val item = curRanks[0]
                val url = ImageProvider.getRecordRandomPath(item.record?.name, null)
                val weeks = getDetailRankDetails(item.record?.id?:0, item.bean.rank)
                result.add(StudioItem(3, item.record, item.bean.rank, "R-${item.bean.rank}", "$weeks weeks", url))
            }
            // 显示9个历史最高。先取20个，处理出现并列的情况可超过9个
            val historyHighItems = getDatabase().getMatchDao().getStudioHighRank(studioId, 20)
            if (historyHighItems.isNotEmpty()) {
                result.add(StudioTitle(FLAG_TITLE_HISTORY_TOP, "History Top", false))
                var count = 0
                var lastRank = 0
                for (item in historyHighItems) {
                    getDatabase().getRecordDao().getRecordBasic(item.recordId)?.let { record ->
                        val curRank = rankRepository.getRecordCurrentRank(item.recordId)
                        val url = ImageProvider.getRecordRandomPath(record.name, null)
                        val weeks = getDetailRankDetails(item.recordId, item.high)
                        result.add(StudioItem(1, record, item.high, "Now $curRank", "Top ${item.high}(${weeks} weeks)", url))
                    }
                    count ++
                    // 达到9个，且没有并列排名了，跳出
                    if (count >= 9 && item.high != lastRank) {
                        break
                    }
                    lastRank = item.high
                }
            }
            // GS champion

            it.onNext(result)
            it.onComplete()
        }
    }

    fun getDetailRankDetails(recordId: Long, rank: Int): Int {
        return getDatabase().getMatchDao().getRecordRanks(recordId, rank).size
    }

}