package com.king.app.coolg_kt.page.match.h2h

import android.app.Application
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.model.http.observer.SimpleObserver
import com.king.app.coolg_kt.model.image.ImageProvider
import com.king.app.coolg_kt.model.repository.H2hRepository
import com.king.app.coolg_kt.model.repository.RankRepository
import com.king.app.coolg_kt.page.match.H2hItem
import com.king.app.gdb.data.relation.RecordWrap
import io.reactivex.rxjava3.core.ObservableSource

/**
 * @description:
 * @author：Jing
 * @date: 2021/1/17 11:35
 */
class H2hViewModel(application: Application): BaseViewModel(application) {

    var player1Name = ObservableField<String>()
    var player2Name = ObservableField<String>()
    var player1Rank = ObservableField<String>()
    var player2Rank = ObservableField<String>()
    var player1Win = ObservableField<String>()
    var player2Win = ObservableField<String>()
    var player1WinColor = ObservableInt()
    var player2WinColor = ObservableInt()
    var player1ImageUrl = ObservableField<String>()
    var player2ImageUrl = ObservableField<String>()

    var h2hObserver = MutableLiveData<List<H2hItem>>()

    val h2hRepository = H2hRepository()
    val rankRepository = RankRepository()

    var player1: RecordWrap? = null

    var player2: RecordWrap? = null

    var indexToReceivePlayer = -1

    fun loadH2h(id1: Long, id2: Long) {
        player1 = getDatabase().getRecordDao().getRecord(id1)
        onPlayer1Changed()
        player2 = getDatabase().getRecordDao().getRecord(id2)
        onPlayer2Changed()
    }

    private fun onPlayer1Changed() {
        player1?.let {
            player1Name.set(it.bean.name)
            player1ImageUrl.set(ImageProvider.getRecordRandomPath(it.bean.name, null))
            var rank = rankRepository.getRecordCurrentRank(it.bean.id!!)
            if (rank == -1) {
                player1Rank.set("R${it.countRecord?.rank}")
            }
            else {
                player1Rank.set("r$rank")
            }
            onH2hChanged()
        }
    }

    private fun onPlayer2Changed() {
        player2?.let {
            player2Name.set(it.bean.name)
            player2ImageUrl.set(ImageProvider.getRecordRandomPath(it.bean.name, null))
            var rank = rankRepository.getRecordCurrentRank(it.bean.id!!)
            if (rank == -1) {
                player2Rank.set("R${it.countRecord?.rank}")
            }
            else {
                player2Rank.set("r$rank")
            }
            onH2hChanged()
        }
    }

    private fun onH2hChanged() {
        if (player1 != null && player2 != null) {
            h2hRepository.getH2hItems(player1!!.bean.id!!, player2!!.bean.id!!)
                .flatMap { calculateWin(it, player1!!.bean.id!!, player2!!.bean.id!!) }
                .compose(applySchedulers())
                .subscribe(object : SimpleObserver<List<H2hItem>>(getComposite()){
                    override fun onNext(t: List<H2hItem>) {
                        h2hObserver.value = t
                    }

                    override fun onError(e: Throwable) {
                        e?.printStackTrace()
                        messageObserver.value = e?.message
                    }
                })
        }
    }

    private fun calculateWin(list: List<H2hItem>, player1Id: Long, player2Id: Long): ObservableSource<List<H2hItem>> {
        return ObservableSource {
            var player1ItemBg: Int = getResource().getColor(R.color.h2h_bg_more)
            var player2ItemBg: Int = getResource().getColor(R.color.h2h_bg_less)
            var win1 = 0
            var win2 = 0
            list.forEach { item ->
                if (item.matchItem.bean.winnerId == player1Id) {
                    win1 ++
                }
                else {
                    win2 ++
                }
            }
            when {
                win1 > win2 -> {
                    player1WinColor.set(getResource().getColor(R.color.redC93437))
                    player2WinColor.set(getResource().getColor(R.color.text_sub))
                }
                win2 > win1 -> {
                    player2WinColor.set(getResource().getColor(R.color.redC93437))
                    player1WinColor.set(getResource().getColor(R.color.text_sub))
                    player2ItemBg = getResource().getColor(R.color.h2h_bg_more);
                    player1ItemBg = getResource().getColor(R.color.h2h_bg_less);
                }
                else -> {
                    player1WinColor.set(getResource().getColor(R.color.text_sub))
                    player2WinColor.set(getResource().getColor(R.color.text_sub))
                }
            }
            player1Win.set(win1.toString())
            player2Win.set(win2.toString())
            list.forEach { item ->
                item.bgColor = if (item.matchItem.bean.winnerId == player1Id) {
                    player1ItemBg
                }
                else {
                    player2ItemBg
                }
            }
            it.onNext(list)
            it.onComplete()
        }
    }

    fun loadReceivePlayer(playerId: Long) {
        var player = getDatabase().getRecordDao().getRecord(playerId)
        if (indexToReceivePlayer == 1) {
            player1 = player
            onPlayer1Changed()
        }
        else {
            player2 = player
            onPlayer2Changed()
        }
    }
}