package com.king.app.coolg_kt.page.match.draw

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.conf.MatchConstants
import com.king.app.coolg_kt.model.image.ImageProvider
import com.king.app.coolg_kt.model.repository.DrawRepository
import com.king.app.coolg_kt.page.match.DrawItem
import com.king.app.coolg_kt.page.match.FinalDrawData
import com.king.app.coolg_kt.page.match.FinalRound
import com.king.app.gdb.data.entity.match.MatchItem
import com.king.app.gdb.data.relation.MatchPeriodWrap
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

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
    var saveEditSuccess = MutableLiveData<Boolean>()

    var dataObserver = MutableLiveData<List<Any>>()

    var createdDrawData: FinalDrawData? = null
    var presentDrawData: FinalDrawData? = null

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

    fun getMatchId(): Long {
        return matchPeriod.bean.matchId
    }

    private fun loadMatch() {
        launchFlowThread(
            flow { emit(drawRepository.getFinalDrawData(matchPeriod)) }
                .map { toViewList(it) },
            withLoading = true
        ) {
            dataObserver.value = it
        }
    }

    private fun toViewList(finalDrawData: FinalDrawData): List<Any> {
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
            list.add(FinalRound(roundSf))
            items.forEach { item ->
                item.matchRecord1?.let { record -> record.imageUrl = imageMap[record.record?.name] }
                item.matchRecord2?.let { record -> record.imageUrl = imageMap[record.record?.name] }
                item.winner?.let { record -> record.imageUrl = imageMap[record.record?.name] }
            }
            list.addAll(items)
        }
        // F
        finalDrawData.roundMap[roundF]?.let { items ->
            list.add(FinalRound(roundF))
            items.forEach { item ->
                item.matchRecord1?.let { record -> record.imageUrl = imageMap[record.record?.name] }
                item.matchRecord2?.let { record -> record.imageUrl = imageMap[record.record?.name] }
                item.winner?.let { record -> record.imageUrl = imageMap[record.record?.name] }
            }
            list.addAll(items)
        }
        return list
    }

    fun createDraw() {
        launchFlowThread(
            flow { emit(drawRepository.createFinalDraw(matchPeriod)) }
                .map { toViewList(it) },
            withLoading = true
        ) {
            newDrawCreated.value = true
            dataObserver.value = it
        }
    }

    fun saveDraw() {
        createdDrawData?.let {
            launchSingle(
                { drawRepository.saveFinalDraw(it) },
                withLoading = true
            ) {
                cancelConfirmCancelStatus.value = true
                createdDrawData = null
                reloadMatch()
            }
        }
    }

    fun saveEdit() {
        launchSingle(
            { saveEditRx() },
            withLoading = true
        ) {
            messageObserver.value = "success"
            saveEditSuccess.value = true
            reloadMatch()
        }
    }

    private fun saveEditRx() {
        val updateMatchItems = mutableListOf<MatchItem>()
        // RR需要等待全部完成才能决定次轮，SF需要等待两场均完赛决定F
        var isRRChanged = false
        var isRRFinished = true
        var isSfChanged = false
        var isSfFinished = true
        dataObserver.value
            ?.filterIsInstance<DrawItem>()
            ?.forEach { drawItem ->
                when(drawItem.matchItem.round) {
                    MatchConstants.ROUND_ID_GROUP -> {
                        isRRChanged = drawItem.isChanged
                        if (drawItem.winner == null) {
                            isRRFinished = false
                        }
                    }
                    MatchConstants.ROUND_ID_SF -> {
                        isSfChanged = drawItem.isChanged
                        if (drawItem.winner == null) {
                            isSfFinished = false
                        }
                    }
                }
                drawItem.winner?.let { winner ->
                    if (drawItem.isChanged) {
                        drawItem.matchItem.winnerId = winner.bean.recordId
                        updateMatchItems.add(drawItem.matchItem)
                    }
                }
            }
        // 修改胜负情况
        getDatabase().getMatchDao().updateMatchItems(updateMatchItems)
        // 判定sf晋级情况
        if (isSfChanged && isSfFinished) {
            val sfList = presentDrawData!!.roundMap[MatchConstants.roundFull(MatchConstants.ROUND_ID_SF)]!!
            drawRepository.checkFinalSf(matchPeriod.bean.id, sfList[0].winner!!.bean, sfList[1].winner!!.bean)
        }
        // 判定group晋级情况
        if (isRRChanged && isRRFinished) {
            val firstRound = presentDrawData!!.roundMap[MatchConstants.roundFull(MatchConstants.ROUND_ID_GROUP)]!!
            drawRepository.checkFinalGroup(matchPeriod.bean.id, firstRound, presentDrawData!!.scoreAList, presentDrawData!!.scoreBList)
        }
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
        reloadMatch()
    }

    fun createScore() {
        launchSingle(
            { drawRepository.createFinalScore(matchPeriod) },
            withLoading = true
        ) {
            messageObserver.value = "success"
        }
    }
}