package com.king.app.coolg_kt.page.match.score

import android.app.Application
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.conf.MatchConstants
import com.king.app.coolg_kt.model.http.observer.SimpleObserver
import com.king.app.coolg_kt.model.image.ImageProvider
import com.king.app.coolg_kt.model.repository.RankRepository
import com.king.app.coolg_kt.page.match.PeriodPack
import com.king.app.coolg_kt.page.match.ScoreBean
import com.king.app.coolg_kt.page.match.ScoreTitle
import com.king.app.gdb.data.relation.MatchScoreRecordWrap
import com.king.app.gdb.data.relation.RecordWrap
import io.reactivex.rxjava3.core.Observable

/**
 * @description:
 * @author：Jing
 * @date: 2021/1/16 15:39
 */
class ScoreViewModel(application: Application): BaseViewModel(application) {

    var scoresObserver = MutableLiveData<List<Any>>()
    private var rankRepository = RankRepository()

    var recordImageUrl = ObservableField<String>()
    var scoreText = ObservableField<String>()
    var scoreNotCountText = ObservableField<String>()
    var rankText = ObservableField<String>()
    var nameText = ObservableField<String>()
    var totalMatchesText = ObservableField<String>()

    var recordWrap: RecordWrap? = null
    var curPeriodPack: PeriodPack? = null

    fun loadRankPeriod(recordId: Long) {
        curPeriodPack = rankRepository.getCompletedPeriodPack()
        loadRecord(recordId)
            .flatMap { convertRecordScores(recordId, rankRepository.getRecordRankPeriodScores(recordId)) }
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<List<Any>>(getComposite()) {
                override fun onNext(t: List<Any>) {
                    scoresObserver.value = t
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                    messageObserver.value = e?.message
                }
            })
    }

    fun loadRaceToFinal(recordId: Long) {
        curPeriodPack = rankRepository.getRTFPeriodPack()
        var period = curPeriodPack?.startPeriod?:0
        loadRecord(recordId)
            .flatMap { convertRecordScores(recordId, rankRepository.getRecordPeriodScores(recordId, period)) }
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<List<Any>>(getComposite()) {
                override fun onNext(t: List<Any>) {
                    scoresObserver.value = t
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                    messageObserver.value = e?.message
                }
            })
    }

    private fun loadRecord(recordId: Long): Observable<RecordWrap> {
        return Observable.create {
            recordWrap = getDatabase().getRecordDao().getRecord(recordId)
            recordWrap?.let { wrap ->
                recordImageUrl.set(ImageProvider.getRecordRandomPath(wrap.bean.name, null))
                nameText.set(wrap.bean.name)
                var rankTxt: String? = null
                wrap.countRecord?.let { countRecord ->
                    val curRank = rankRepository.getRecordRankToDraw(wrap.bean.id!!)
                    rankTxt = if (MatchConstants.RANK_OUT_OF_SYSTEM == curRank) {
                        "R-${countRecord.rank}"
                    }
                    else {
                        "$curRank (R-${countRecord.rank})"
                    }
                }
                rankTxt?.let { text -> rankText.set(text) }
            }
            it.onNext(recordWrap)
            it.onComplete()
        }
    }

    /**
     * @param list list按score降序排列
     */
    private fun convertRecordScores(recordId: Long, list: List<MatchScoreRecordWrap>): Observable<List<Any>> {
        totalMatchesText.set("${list.size} Matches")
        return Observable.create {
            var result = mutableListOf<Any>()
            var score = 0
            var scoreNotCount = 0
            // 按level归类
            var map = mutableMapOf<Int, MutableList<ScoreBean>?>()

            list.forEachIndexed { index, wrap ->
                var match = getDatabase().getMatchDao().getMatch(wrap.matchRealId)
                var items = map[match.level]
                if (items == null) {
                    items = mutableListOf()
                    map[match.level] = items
                }
                val isWinner = wrap.matchItem.winnerId == recordId
                val matchPeriod = getDatabase().getMatchDao().getMatchPeriod(wrap.matchItem.matchId)
                var isCompleted = false
                curPeriodPack?.matchPeriod?.let { curPeriod ->
                    isCompleted = matchPeriod.bean.orderInPeriod <= curPeriod.orderInPeriod
                }
                val isChampion = isWinner && wrap.matchItem.round == MatchConstants.ROUND_ID_F
                val isNotCount = index >= MatchConstants.MATCH_COUNT_SCORE
                var scoreBean = ScoreBean(wrap.bean.score, match.name, MatchConstants.roundResultShort(wrap.matchItem.round, isWinner),
                    isCompleted, isChampion, isNotCount, matchPeriod.bean, wrap.matchItem, match)
                items.add(scoreBean)

                if (isNotCount) {
                    scoreNotCount += wrap.bean.score
                }
                else {
                    score += wrap.bean.score
                }
            }
            // 按level排序
            val keys = map.keys.sortedBy { level -> level }
            keys.forEach { level ->
                val color = when(level) {
                    MatchConstants.MATCH_LEVEL_GS -> getResource().getColor(R.color.match_level_gs)
                    MatchConstants.MATCH_LEVEL_FINAL -> getResource().getColor(R.color.match_level_final)
                    MatchConstants.MATCH_LEVEL_GM1000 -> getResource().getColor(R.color.match_level_gm1000)
                    MatchConstants.MATCH_LEVEL_GM500 -> getResource().getColor(R.color.match_level_gm500)
                    MatchConstants.MATCH_LEVEL_GM250 -> getResource().getColor(R.color.match_level_gm250)
                    else -> getResource().getColor(R.color.match_level_low)
                }
                result.add(ScoreTitle(MatchConstants.MATCH_LEVEL[level], color))
                // item按orderInPeriod归类
                map[level]?.sortBy { item -> item.matchPeriod.orderInPeriod }
                result.addAll(map[level]!!)
            }

            scoreText.set(score.toString())
            if (scoreNotCount > 0) {
                scoreNotCountText.set("(${scoreNotCount})")
            }
            it.onNext(result)
            it.onComplete()
        }
    }
}