package com.king.app.coolg_kt.page.match.draw

import android.app.Application
import android.view.View
import androidx.databinding.ObservableInt
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.conf.AppConstants
import com.king.app.coolg_kt.conf.MatchConstants
import com.king.app.coolg_kt.conf.RoundPack
import com.king.app.coolg_kt.model.http.observer.SimpleObserver
import com.king.app.coolg_kt.model.image.ImageProvider
import com.king.app.coolg_kt.model.repository.DrawRepository
import com.king.app.coolg_kt.model.repository.RankRepository
import com.king.app.coolg_kt.page.match.DrawData
import com.king.app.coolg_kt.page.match.DrawItem
import com.king.app.gdb.data.entity.match.Match
import com.king.app.gdb.data.entity.match.MatchItem
import com.king.app.gdb.data.entity.match.MatchRecord
import com.king.app.gdb.data.relation.MatchPeriodWrap
import com.king.app.gdb.data.relation.MatchRecordWrap
import io.reactivex.rxjava3.core.Observable

/**
 * @description:
 * @author：Jing
 * @date: 2021/1/10 14:47
 */
class DrawViewModel(application: Application): BaseViewModel(application) {

    lateinit var matchPeriod: MatchPeriodWrap

    var qualifyVisibility = ObservableInt(View.VISIBLE)
    var previousVisibility = ObservableInt(View.INVISIBLE)
    var nextVisibility = ObservableInt(View.VISIBLE)

    var roundPosition = 0

    var setRoundPosition = MutableLiveData<Int>()
    var roundList = MutableLiveData<List<RoundPack>>()
    var itemsObserver = MutableLiveData<List<DrawItem>>()
    var newDrawCreated = MutableLiveData<Boolean>()
    var cancelConfirmCancelStatus = MutableLiveData<Boolean>()
    var saveEditSuccess = MutableLiveData<Boolean>()

    var drawType = MatchConstants.DRAW_MAIN

    var drawRepository = DrawRepository()
    var rankRepository = RankRepository()

    var createdDrawData: DrawData? = null

    var mToSetWildCard: DrawItem? = null
    var mToSetWildCardRecord: MatchRecordWrap? = null
    var mToSetWildCardPosition: Int? = null

    fun loadMatch(matchPeriodId: Long) {
        matchPeriod = getDatabase().getMatchDao().getMatchPeriod(matchPeriodId)
        getMatchRound(matchPeriod.match)
    }

    fun getMatchId(): Long {
        return matchPeriod.bean.matchId
    }

    fun onDrawTypeChanged() {
        getMatchRound(matchPeriod.match)
    }

    private fun getMatchRound(match: Match) {
        val rounds = drawRepository.getMatchRound(match, drawType)
        if (match.level == 1) {
            qualifyVisibility.set(View.GONE)
        }
        roundList.value = rounds
    }

    fun onClickNext() {
        roundList.value?.let {
            var target = roundPosition + 1
            if (target < it.size) {
                roundPosition = target
            }
            checkNextLast()
            setRoundPosition.value = roundPosition
        }
    }

    fun isFirstRound(): Boolean {
        return roundPosition == 0
    }

    fun onClickPrevious() {
        roundList.value?.let {
            var target = roundPosition - 1
            if (roundPosition >= 0) {
                roundPosition = target
            }
            checkNextLast()
            setRoundPosition.value = roundPosition
        }
    }

    private fun checkNextLast() {
        var roundSize = roundList.value?.size?:0
        if (roundSize == 0) {
            nextVisibility.set(View.INVISIBLE)
            previousVisibility.set(View.INVISIBLE)
        }
        else {
            when (roundPosition) {
                0 -> {
                    nextVisibility.set(View.VISIBLE)
                    previousVisibility.set(View.INVISIBLE)
                }
                roundSize - 1 -> {
                    nextVisibility.set(View.INVISIBLE)
                    previousVisibility.set(View.VISIBLE)
                }
                else -> {
                    nextVisibility.set(View.VISIBLE)
                    previousVisibility.set(View.VISIBLE)
                }
            }
        }
    }

    fun onRoundPositionChanged(position: Int) {
        roundPosition = position
        checkNextLast()
        reloadRound()
    }

    private fun reloadRound() {
        roundList.value?.let {
            loadRound(it[roundPosition])
        }
    }

    private fun loadRound(roundPack: RoundPack) {
        if (createdDrawData == null) {
            loadRoundFromTable(roundPack)
        }
        else {
            loadRoundFromCreate(roundPack)
        }
    }

    private fun loadRoundFromCreate(roundPack: RoundPack) {
        (roundList.value?.get(0)?.id == roundPack.id)?.let { isFirstRound ->
            createdDrawData?.let {
                if (isFirstRound) {
                    if (drawType == MatchConstants.DRAW_MAIN) {
                        itemsObserver.value = it.mainItems
                    }
                    else {
                        itemsObserver.value = it.qualifyItems
                    }
                }
            }
        }
    }

    private fun loadRoundFromTable(roundPack: RoundPack) {
        loadingObserver.value = true
        drawRepository.getDrawItems(matchPeriod.bean.id, matchPeriod.bean.matchId, roundPack.id)
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<List<DrawItem>>(getComposite()) {
                override fun onNext(t: List<DrawItem>) {
                    loadingObserver.value = false
                    itemsObserver.value = t
                }

                override fun onError(e: Throwable?) {
                    loadingObserver.value = false
                    e?.printStackTrace()
                    messageObserver.value = e?.message
                }

            })
    }

    fun isDrawExist(): Boolean {
        return drawRepository.isDrawExist(matchPeriod.bean.id)
    }

    fun createDraw() {
        drawRepository.createDraw(matchPeriod)
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<DrawData>(getComposite()) {
                override fun onNext(t: DrawData) {
                    createdDrawData = t
                    newDrawCreated.value = true
                    itemsObserver.value = t.mainItems
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                    messageObserver.value = e?.message
                }
            })
    }

    fun saveDraw() {
        createdDrawData?.let {
            drawRepository.saveDraw(it)
                .compose(applySchedulers())
                .subscribe(object : SimpleObserver<DrawData>(getComposite()) {
                    override fun onNext(t: DrawData?) {
                        cancelConfirmCancelStatus.value = true
                        createdDrawData = null
                        reloadRound()
                    }

                    override fun onError(e: Throwable?) {
                        e?.printStackTrace()
                    }
                })
        }
    }

    fun cancelSaveDraw() {
        cancelConfirmCancelStatus.value = true
        createdDrawData = null
        reloadRound()
    }

    fun setWildCard(recordId: Long): Boolean {
        // 先查询是否已存在当前period的签表中
        val existCount = getDatabase().getMatchDao().countMatchRecord(matchPeriod.bean.period, matchPeriod.bean.orderInPeriod, recordId)
        return if (existCount > 0) {
            messageObserver.value = "This record is already in current period"
            false
        } else {
            val wrap = getDatabase().getRecordDao().getRecord(recordId)
            mToSetWildCardRecord?.bean?.recordId = recordId
            // 查询排名
            mToSetWildCardRecord?.bean?.recordRank = rankRepository.getRecordRankToDraw(recordId)
            mToSetWildCardRecord?.imageUrl = ImageProvider.getRecordRandomPath(wrap?.bean?.name, null)
            mToSetWildCard?.isChanged = true
            true
        }
    }

    fun isModified(): Boolean {
        var modifiedItem = itemsObserver.value?.filter { it.isChanged }?.size?:0
        return modifiedItem > 0
    }

    fun saveEdit() {
        saveEditRx()
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<Boolean>(getComposite()) {
                override fun onNext(t: Boolean?) {
                    messageObserver.value = "success"
                    saveEditSuccess.value = true
                    reloadRound()
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                    messageObserver.value = e?.message
                }
            })
    }

    fun cancelEdit() {
        cancelConfirmCancelStatus.value = true
        reloadRound()
    }

    private fun saveEditRx(): Observable<Boolean> {
        return Observable.create {
            val updateMatchRecords = mutableListOf<MatchRecord>()
            val updateMatchItems = mutableListOf<MatchItem>()
            itemsObserver.value?.filter { it.isChanged }?.forEach { drawItem ->
                drawItem.winner?.let {  winner ->
                    drawItem.matchItem.winnerId = winner.bean.recordId
                    updateMatchItems.add(drawItem.matchItem)

                    when(drawItem.matchItem.round) {
                        // Q3，胜者填补正赛签位
                        MatchConstants.ROUND_ID_Q3 -> {
                            setQualifyToMainDraw(winner.bean)
                        }
                        // Final，决定冠军
                        MatchConstants.ROUND_ID_F -> {

                        }
                        // 其他，判断进入到下一轮
                        else -> {
                            // 一对签位，在第二个item检查下一轮
                            // 检查下一轮
                            drawRepository.toggleNextRound(drawItem.matchItem, winner)
                        }
                    }
                }
                drawItem.matchRecord1?.let { updateMatchRecords.add(it.bean) }
                drawItem.matchRecord2?.let { updateMatchRecords.add(it.bean) }
            }
            getDatabase().getMatchDao().updateMatchItems(updateMatchItems)
            getDatabase().getMatchDao().updateMatchRecords(updateMatchRecords)

            it.onNext(true)
            it.onComplete()
        }
    }

    private fun setQualifyToMainDraw(bean: MatchRecord) {
        var qualify = getDatabase().getMatchDao().getUndefinedQualifies(bean.matchId).shuffled().first()
        qualify.recordId = bean.recordId
        qualify.recordRank = bean.recordRank
        qualify.recordSeed = 0
        getDatabase().getMatchDao().updateMatchRecords(listOf(qualify))
    }

    fun createScore() {
        drawRepository.createScore(matchPeriod)
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<Boolean>(getComposite()) {
                override fun onNext(t: Boolean) {
                    messageObserver.value = "success"
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                    messageObserver.value = e?.message
                }
            })
    }

    fun getLowestSeedRankOfPage(): Int {
        var result = 0
        itemsObserver.value?.forEach {
            val seed1 = it.matchRecord1?.bean?.recordSeed?:0
            if (seed1 > 0) {
                val rank = it.matchRecord1?.bean?.recordRank?:0
                if (rank > result) {
                    result = rank
                }
            }
            val seed2 = it.matchRecord2?.bean?.recordSeed?:0
            if (seed2 > 0) {
                val rank = it.matchRecord2?.bean?.recordRank?:0
                if (rank > result) {
                    result = rank
                }
            }
        }
        return result
    }

    fun findStudioId(): Long {
        val parent = getDatabase().getFavorDao().getRecordOrderByName(AppConstants.ORDER_STUDIO_NAME)
        parent?.let {
            var studio = getDatabase().getFavorDao().getStudioByName(matchPeriod.match.name, it.id!!)
            studio?.let { s ->
                return s.id!!
            }
        }
        return 0
    }

    data class SeedPack (
        var matchRecord: MatchRecordWrap?,
        var drawItem: DrawItem,
        var index: Int
    )

    fun insertSeed(recordId: Long): Boolean {
        kotlin.runCatching {
            var seeds = mutableListOf<SeedPack>()
            var wildcards = mutableListOf<SeedPack>()
            itemsObserver.value?.forEach {
                if (isSeed(it.matchRecord1)) {
                    seeds.add(SeedPack(it.matchRecord1, it, 0))
                }
                else if (isWildcard(it.matchRecord1)) {
                    wildcards.add(SeedPack(it.matchRecord1, it, 0))
                }
                if (isSeed(it.matchRecord2)) {
                    seeds.add(SeedPack(it.matchRecord2, it, 1))
                }
                else if (isWildcard(it.matchRecord2)) {
                    wildcards.add(SeedPack(it.matchRecord2, it, 1))
                }
            }
            seeds.sortBy { it.matchRecord!!.bean.recordSeed }

            var rank = rankRepository.getRecordCurrentRank(recordId)
            for (i in seeds.indices) {
                if (seeds[i].matchRecord!!.bean.recordRank!! > rank) {
                    // 最后一个填补进wildcards中
                    var last = seeds.last().matchRecord!!
                    var canSet = wildcards.filter { it.matchRecord?.bean?.recordId?:0 == 0L }.shuffled()
                    if (canSet.isNotEmpty()) {
                        val wc = canSet[0].matchRecord!!
                        wc.record = last.record
                        wc.imageUrl = last.imageUrl
                        wc.bean.recordId = last.bean.recordId
                        wc.bean.recordRank = last.bean.recordRank
                        canSet[0].drawItem.isChanged = true
                    }
                    // 当前位置后面一位开始，全部向后挪一位
                    for (n in seeds.size - 1 downTo i + 1) {
                        var lastMr = seeds[n - 1].matchRecord!!
                        var mr = if (seeds[n].index == 0) {
                            seeds[n].drawItem.matchRecord1!!
                        }
                        else {
                            seeds[n].drawItem.matchRecord2!!
                        }
                        mr.record = lastMr.record
                        mr.imageUrl = lastMr.imageUrl
                        mr.bean.recordId = lastMr.bean.recordId
                        mr.bean.recordRank = lastMr.bean.recordRank
                        mr.bean.recordSeed = lastMr.bean.recordSeed!! + 1
                        seeds[n].drawItem.isChanged = true
                    }
                    // 最后替换当前位置
                    val wrap = getDatabase().getRecordDao().getRecord(recordId)
                    seeds[i].matchRecord?.bean?.recordId = recordId
                    seeds[i].matchRecord?.bean?.recordRank = rank
                    seeds[i].matchRecord?.imageUrl = ImageProvider.getRecordRandomPath(wrap?.bean?.name, null)
                    seeds[i].drawItem.isChanged = true
                    break
                }
            }
            return true
        }
        return false
    }

    private fun isSeed(matchRecord: MatchRecordWrap?): Boolean {
        return matchRecord?.bean?.recordSeed?:0 > 0
    }

    private fun isWildcard(matchRecord: MatchRecordWrap?): Boolean {
        return matchRecord?.bean?.type == MatchConstants.MATCH_RECORD_WILDCARD
    }
}