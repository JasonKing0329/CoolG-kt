package com.king.app.coolg_kt.page.match.draw

import android.app.Application
import android.view.View
import androidx.databinding.ObservableInt
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.conf.MatchConstants
import com.king.app.coolg_kt.conf.RoundPack
import com.king.app.coolg_kt.model.http.observer.SimpleObserver
import com.king.app.coolg_kt.model.repository.DrawRepository
import com.king.app.coolg_kt.page.match.DrawItem
import com.king.app.gdb.data.entity.match.Match
import com.king.app.gdb.data.relation.MatchPeriodWrap

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

    var drawType = 1 // 0:qualify, 1:main

    var drawRepository = DrawRepository()

    fun loadMatch(matchPeriodId: Long) {
        matchPeriod = getDatabase().getMatchDao().getMatchPeriod(matchPeriodId)
        getRoundByLevel(matchPeriod.match)
    }

    fun onDrawTypeChanged() {
        getRoundByLevel(matchPeriod.match)
    }

    private fun getRoundByLevel(match: Match) {
        // master final
        val rounds = if (match.level == 1) {
            qualifyVisibility.set(View.GONE)
            MatchConstants.ROUND_ROBIN
        }
        else {
            if (drawType == 1) {
                when(match.draws) {
                    128 -> MatchConstants.ROUND_MAIN_DRAW128
                    64 -> MatchConstants.ROUND_MAIN_DRAW64
                    else -> MatchConstants.ROUND_MAIN_DRAW32
                }
            }
            else {
                MatchConstants.ROUND_QUALIFY
            }
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
        roundList.value?.let {
            loadRound(it[roundPosition])
        }
    }

    private fun loadRound(roundPack: RoundPack) {
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

    fun createDraw() {

    }

}