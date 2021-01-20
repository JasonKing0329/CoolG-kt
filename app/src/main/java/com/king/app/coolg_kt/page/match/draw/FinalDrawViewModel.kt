package com.king.app.coolg_kt.page.match.draw

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.conf.MatchConstants
import com.king.app.coolg_kt.model.http.observer.SimpleObserver
import com.king.app.coolg_kt.model.image.ImageProvider
import com.king.app.coolg_kt.model.repository.DrawRepository
import com.king.app.coolg_kt.page.match.DrawItem
import com.king.app.coolg_kt.page.match.FinalDrawData
import com.king.app.coolg_kt.page.match.FinalRound
import com.king.app.gdb.data.relation.MatchPeriodWrap
import io.reactivex.rxjava3.core.ObservableSource

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/1/19 16:11
 */
class FinalDrawViewModel(application: Application): BaseViewModel(application) {

    val drawRepository = DrawRepository()

    lateinit var matchPeriod: MatchPeriodWrap

    var newDrawCreated = MutableLiveData<Boolean>()
    var cancelConfirmCancelStatus = MutableLiveData<Boolean>()

    var dataObserver = MutableLiveData<List<Any>>()

    var createdDrawData: FinalDrawData? = null

    fun isDrawExist(): Boolean {
        return drawRepository.isDrawExist(matchPeriod.bean.id)
    }

    fun reloadMatch() {
        loadMatch()
    }

    fun loadMatch(matchPeriodId: Long) {
        matchPeriod = getDatabase().getMatchDao().getMatchPeriod(matchPeriodId)
        loadMatch()
    }

    private fun loadMatch() {
        loadingObserver.value = true
        drawRepository.getFinalDrawData(matchPeriod)
            .flatMap { toViewList(it) }
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<List<Any>>(getComposite()) {
                override fun onNext(t: List<Any>) {
                    loadingObserver.value = false
                    dataObserver.value = t
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                    loadingObserver.value = false
                    messageObserver.value = e?.message
                }
            })
    }

    private fun toViewList(finalDrawData: FinalDrawData): ObservableSource<List<Any>> {
        return ObservableSource {
            val imageMap = mutableMapOf<String?, String?>()
            var list = mutableListOf<Any>()
            // head
            finalDrawData.head.groupAList.forEach { item ->
                imageMap[item.record.bean.name] = ImageProvider.getRecordRandomPath(item.record.bean.name, null)
                item.record.imageUrl = imageMap[item.record.bean.name]
            }
            finalDrawData.head.groupBList.forEach { item ->
                imageMap[item.record.bean.name] = ImageProvider.getRecordRandomPath(item.record.bean.name, null)
                item.record.imageUrl = imageMap[item.record.bean.name]
            }
            list.add(finalDrawData.head)
            // score
            for (i in finalDrawData.scoreAList.indices) {
                finalDrawData.scoreAList[i].record.imageUrl = imageMap[finalDrawData.scoreAList[i].record.bean.name]
                finalDrawData.scoreBList[i].record.imageUrl = imageMap[finalDrawData.scoreBList[i].record.bean.name]
                list.add(finalDrawData.scoreAList[i])
                list.add(finalDrawData.scoreBList[i])
            }
            val roundRobinText = MatchConstants.roundFull(MatchConstants.ROUND_ID_GROUP)
            val roundSf = MatchConstants.roundFull(MatchConstants.ROUND_ID_SF)
            val roundF = MatchConstants.roundFull(MatchConstants.ROUND_ID_F)
            // RR 按分组排
            val rr = finalDrawData.roundMap[roundRobinText]
            val groupA = rr?.filter { item -> item.matchItem.groupFlag == 0 }
            val groupB = rr?.filter { item -> item.matchItem.groupFlag == 1 }
            groupA?.let { items ->
                list.add(FinalRound("Group A"))
                items.forEach { item ->
                    item.matchRecord1?.let { record -> record.imageUrl = imageMap[record.record?.name] }
                    item.matchRecord2?.let { record -> record.imageUrl = imageMap[record.record?.name] }
                    item.winner?.let { record -> record.imageUrl = imageMap[record.record?.name] }
                }
                list.addAll(items)
            }
            groupB?.let { items ->
                list.add(FinalRound("Group B"))
                items.forEach { item ->
                    item.matchRecord1?.let { record -> record.imageUrl = imageMap[record.record?.name] }
                    item.matchRecord2?.let { record -> record.imageUrl = imageMap[record.record?.name] }
                    item.winner?.let { record -> record.imageUrl = imageMap[record.record?.name] }
                }
                list.addAll(items)
            }
            // SF
            finalDrawData.roundMap[roundSf]?.let { items ->
                list.add(FinalRound(roundRobinText))
                list.addAll(items)
            }
            // F
            finalDrawData.roundMap[roundF]?.let { items ->
                list.add(FinalRound(roundRobinText))
                list.addAll(items)
            }
            it.onNext(list)
            it.onComplete()
        }
    }

    fun createDraw() {
        loadingObserver.value = true
        drawRepository.createFinalDraw(matchPeriod)
            .flatMap {
                createdDrawData = it
                toViewList(it)
            }
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<List<Any>>(getComposite()) {
                override fun onNext(t: List<Any>) {
                    loadingObserver.value = false
                    newDrawCreated.value = true
                    dataObserver.value = t
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                    loadingObserver.value = false
                    messageObserver.value = e?.message
                }
            })
    }

    fun saveDraw() {
        createdDrawData?.let {
            loadingObserver.value = true
            drawRepository.saveFinalDraw(it)
                .compose(applySchedulers())
                .subscribe(object : SimpleObserver<FinalDrawData>(getComposite()) {
                    override fun onNext(t: FinalDrawData?) {
                        loadingObserver.value = false
                        cancelConfirmCancelStatus.value = true
                        createdDrawData = null
                        reloadMatch()
                    }

                    override fun onError(e: Throwable?) {
                        e?.printStackTrace()
                        loadingObserver.value = false
                        messageObserver.value = e?.message
                    }
                })
        }
    }

    fun saveEdit() {

    }

    fun cancelSaveDraw() {
        cancelConfirmCancelStatus.value = true
        createdDrawData = null
        reloadMatch()
    }

    fun isModified(): Boolean {
        var modifiedItem = dataObserver.value?.filter { it is DrawItem && it.isChanged }?.size?:0
        return modifiedItem > 0
    }

    fun cancelEdit() {

    }

    fun createScore() {

    }
}