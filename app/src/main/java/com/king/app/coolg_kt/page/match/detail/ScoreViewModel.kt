package com.king.app.coolg_kt.page.match.detail

import android.app.Application
import android.view.View
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.conf.MatchConstants
import com.king.app.coolg_kt.model.http.observer.SimpleObserver
import com.king.app.coolg_kt.model.image.ImageProvider
import com.king.app.coolg_kt.model.repository.RankRepository
import com.king.app.coolg_kt.page.match.*
import com.king.app.coolg_kt.page.match.rank.ScoreModel
import com.king.app.gdb.data.entity.match.Match
import com.king.app.gdb.data.entity.match.MatchScoreRecord
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
    private var scoreModel = ScoreModel()

    var recordWrap: RecordWrap? = null
    var curPeriodPack: PeriodPack? = null
    var scoreHead = ScoreHead()

    private val recordId: Long
        get() = recordWrap?.bean?.id?:0

    fun loadRankPeriod() {
        curPeriodPack = rankRepository.getRankPeriodPack()
        convertRecordScores(scoreModel.countScoreWithClassifiedResult(recordId, curPeriodPack!!))
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

    fun loadRaceToFinal() {
        curPeriodPack = rankRepository.getRTFPeriodPack()
        var period = curPeriodPack?.startPeriod?:0
        curPeriodPack = rankRepository.getSpecificPeriodPack(period)
        loadPeriodScores(recordId)
    }

    fun loadPeriod(period: Int) {
        curPeriodPack = rankRepository.getSpecificPeriodPack(period)
        loadPeriodScores(recordId)
    }

    private fun loadPeriodScores(recordId: Long) {
        convertRecordScores(scoreModel.countScoreWithClassifiedResult(recordId, curPeriodPack!!))
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

    data class SPack (
        var scoreBean: ScoreBean,
        var match: Match
    )

    private fun toScoreBean(bean: MatchScoreRecord): SPack {
        var matchItem = getDatabase().getMatchDao().getMatchItem(bean.matchItemId)!!
        var matchPeriod = getDatabase().getMatchDao().getMatchPeriod(bean.matchId)
        val match = matchPeriod.match
        val isWinner = matchItem.winnerId == recordId
        var isCompleted = false
        curPeriodPack?.matchPeriod?.let { curPeriod ->
            isCompleted = matchPeriod.bean.orderInPeriod <= curPeriod.orderInPeriod
        }
        val isChampion = isWinner && matchItem.round == MatchConstants.ROUND_ID_F
        var scoreBean = ScoreBean(bean.score, match.name, MatchConstants.roundResultShort(matchItem.round, isWinner),
            isCompleted, isChampion, matchPeriod.bean, matchItem, match)
        return SPack(scoreBean, match)
    }

    private fun convertRecordScores(scorePack: ScorePack): Observable<List<Any>> {
        return Observable.create {
            var result = mutableListOf<Any>()

            // countList按level归类
            var map = mutableMapOf<Int, MutableList<ScoreBean>?>()
            scorePack.countList?.forEachIndexed { index, bean ->
                var spack = toScoreBean(bean)
                var items = map[spack.match.level]
                if (items == null) {
                    items = mutableListOf()
                    map[spack.match.level] = items
                }
                items.add(spack.scoreBean)
            }

            scoreHead.scoreText = "Total:  ${scorePack.countBean.score}"
            scorePack.countBean.unavailableScore?.let { us ->
                scoreHead.scoreText = "${scoreHead.scoreText}($us not count)"
            }
            scoreHead.periodSpecificText = "Period X"
            result.add(scoreHead)
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
            // replace list
            scorePack.replaceList?.let { list ->
                if (list.isNotEmpty()) {
                    result.add(ScoreTitle("Replace", getResource().getColor(R.color.match_level_low)))
                    list.forEach { bean ->
                        var spack = toScoreBean(bean)
                        result.add(spack.scoreBean)
                    }
                }
            }

            it.onNext(result)
            it.onComplete()
        }
    }
}