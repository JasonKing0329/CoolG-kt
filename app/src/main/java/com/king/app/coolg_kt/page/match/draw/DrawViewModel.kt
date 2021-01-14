package com.king.app.coolg_kt.page.match.draw

import android.app.Application
import android.view.View
import androidx.databinding.ObservableInt
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.conf.MatchConstants
import com.king.app.coolg_kt.conf.RoundPack
import com.king.app.coolg_kt.model.http.observer.SimpleObserver
import com.king.app.coolg_kt.model.image.ImageProvider
import com.king.app.coolg_kt.model.repository.DrawRepository
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

    var createdDrawData: DrawData? = null

    var mToSetWildCard: DrawItem? = null
    var mToSetWildCardRecord: MatchRecordWrap? = null
    var mToSetWildCardPosition: Int? = null

    fun loadMatch(matchPeriodId: Long) {
        matchPeriod = getDatabase().getMatchDao().getMatchPeriod(matchPeriodId)
        getMatchRound(matchPeriod.match)
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

    fun setWildCard(recordId: Long) {
        val wrap = getDatabase().getRecordDao().getRecord(recordId)
        mToSetWildCardRecord?.bean?.recordId = recordId
        mToSetWildCardRecord?.bean?.recordRank = wrap?.countRecord?.rank?:0
        mToSetWildCardRecord?.imageUrl = ImageProvider.getRecordRandomPath(wrap?.bean?.name, null)
        mToSetWildCard?.isChanged = true
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
                            if (drawItem.matchItem.order % 2 == 1) {
                                var winner1Item = itemsObserver.value!![drawItem.matchItem.order - 1].matchItem
                                var winner1Record = itemsObserver.value!![drawItem.matchItem.order - 1].winner
                                var winner2Item = drawItem.matchItem
                                var winner2Record = winner.bean
                                winner1Record?.bean?.let { winner1Record ->
                                    drawRepository.checkNextRound(winner1Item, winner1Record, winner2Item, winner2Record)
                                }
                            }
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

}