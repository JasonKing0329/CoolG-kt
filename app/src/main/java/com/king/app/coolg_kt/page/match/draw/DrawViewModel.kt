package com.king.app.coolg_kt.page.match.draw

import android.app.Application
import android.view.View
import androidx.databinding.ObservableInt
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.conf.AppConstants
import com.king.app.coolg_kt.conf.MatchConstants
import com.king.app.coolg_kt.conf.RoundPack
import com.king.app.coolg_kt.model.extension.applyMeasureTimeLog
import com.king.app.coolg_kt.model.image.ImageProvider
import com.king.app.coolg_kt.model.repository.DrawRepository
import com.king.app.coolg_kt.model.repository.RankRepository
import com.king.app.coolg_kt.page.match.DrawData
import com.king.app.coolg_kt.page.match.DrawItem
import com.king.app.coolg_kt.page.match.TimeWasteRange
import com.king.app.coolg_kt.page.match.WildcardBean
import com.king.app.gdb.data.entity.match.Match
import com.king.app.gdb.data.relation.MatchPeriodWrap
import com.king.app.gdb.data.relation.MatchRecordWrap
import kotlinx.coroutines.Job
import java.util.*
import kotlin.math.abs

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
    var imageChanged = MutableLiveData<TimeWasteRange>()

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
    var availableWildcard = MutableLiveData<WildcardBean>()

    var preApplyList = mutableListOf<WildcardBean>()

    var loadDrawJob: Job? = null

    val random = Random()

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
        if (match.level == MatchConstants.MATCH_LEVEL_MICRO) {
            qualifyVisibility.set(View.INVISIBLE)
        }
        roundList.value = rounds
    }

    fun warningIfModified(): Boolean {
        if (isModified()) {
            messageObserver.value = "Please save or drop current edit first"
            return true
        }
        return false
    }

    fun onClickNext() {
        if (warningIfModified()) {
            return
        }
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
        if (warningIfModified()) {
            return
        }
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
        if (warningIfModified()) {
            return
        }
        roundPosition = position
        checkNextLast()
        reloadRound()
    }

    fun getCurrentRound(): Int {
        return roundList.value?.get(roundPosition)?.id?:0
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

    /**
     * 采用matchItemWrap（包含List<MatchRecord>），从数据库直接加载出来，比一个个单独加载MatchRecordWrap更省时
     * 但由于DrawItem的结构已定，许多地方都引用了MatchRecordWrap，所以保留该结构，将record与imageUrl延迟加载（因为逐个加载时这两都属于耗时操作）
     * 如此一来，以GS R128为例，加载速度从原来的5000毫秒+ 直接降低到了50毫秒内
     */
    private fun loadRoundFromTable(roundPack: RoundPack) {
        loadDrawJob?.cancel()
        loadDrawJob = basicAndTimeWaste(
            blockBasic = { drawRepository.getDrawItems(matchPeriod.bean.id, matchPeriod.bean.matchId, roundPack.id) },
            onCompleteBasic = { itemsObserver.value = it },
            blockWaste = { _, item -> loadWaste(item)},
            wasteNotifyCount = 1,
            onWasteRangeChanged = { start, count -> imageChanged.value = TimeWasteRange(start, count)},
            withBasicLoading = false
        )
    }

    private fun loadWaste(data: DrawItem) {
        data.matchRecord1?.let {
            it.record = getDatabase().getRecordDao().getRecordBasic(it.bean.recordId)
            it.imageUrl = ImageProvider.getRecordRandomPath(it.record?.name, null)
        }
        data.matchRecord2?.let {
            it.record = getDatabase().getRecordDao().getRecordBasic(it.bean.recordId)
            it.imageUrl = ImageProvider.getRecordRandomPath(it.record?.name, null)
        }
    }

    fun isDrawExist(): Boolean {
        return drawRepository.isDrawExist(matchPeriod.bean.id)
    }

    fun createDraw(drawStrategy: DrawStrategy) {
        launchSingleThread(
            {
                // 浅拷贝一份，以便cancel create后还能继续保留
                drawStrategy.preAppliers = preApplyList.toMutableList()
                drawRepository.createDraw(matchPeriod, drawStrategy)
            },
            withLoading = true
        ) {
            createdDrawData = it
            newDrawCreated.value = true
            // 未被安排进入签表的pre appliers，排进wildcards
            if (drawStrategy.preAppliers.size > 0) {
                arrangeWildcards(it.mainItems, drawStrategy.preAppliers)
            }
            itemsObserver.value = it.mainItems
        }
    }

    fun saveDraw() {
        createdDrawData?.apply {
            launchSingleThread(
                { drawRepository.saveDraw(this) },
                withLoading = true
            ) {
                cancelConfirmCancelStatus.value = true
                createdDrawData = null
                reloadRound()
            }
        }
    }

    fun cancelSaveDraw() {
        cancelConfirmCancelStatus.value = true
        createdDrawData = null
        reloadRound()
    }

    fun setChangedRecord(recordId: Long): Boolean {
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

    fun setWildCard(recordId: Long): WildcardBean? {
        // 先查询是否已存在当前period的签表中
        val existCount = getDatabase().getMatchDao().countMatchRecord(matchPeriod.bean.period, matchPeriod.bean.orderInPeriod, recordId)
        return if (existCount > 0) {
            messageObserver.value = "This record is already in current period"
            null
        } else {
            val wrap = getDatabase().getRecordDao().getRecord(recordId)
            // 查询排名
            val rank = rankRepository.getRecordRankToDraw(recordId)
            val imageUrl = ImageProvider.getRecordRandomPath(wrap?.bean?.name, null)
            WildcardBean(recordId, rank, imageUrl)
        }
    }

    fun setPreApply(recordId: Long): WildcardBean? {
        val wrap = getDatabase().getRecordDao().getRecord(recordId)
        // 查询排名
        val rank = rankRepository.getRecordRankToDraw(recordId)
        val imageUrl = ImageProvider.getRecordRandomPath(wrap?.bean?.name, null)
        return WildcardBean(recordId, rank, imageUrl)
    }

    fun isModified(): Boolean {
        var modifiedItem = itemsObserver.value?.filter { it.isChanged }?.size?:0
        return modifiedItem > 0
    }

    fun saveEdit() {
        launchSingleThread(
            { updateDraw() },
            withLoading = true
        ) {
            messageObserver.value = "success"
            saveEditSuccess.value = true
            reloadRound()
        }
    }

    fun cancelEdit() {
        cancelConfirmCancelStatus.value = true
        reloadRound()
    }

    private fun getChangedDrawItems(): List<DrawItem>? {
        return when(getCurrentRound()) {
            MatchConstants.ROUND_ID_Q3, MatchConstants.ROUND_ID_F -> itemsObserver.value?.filter { it.isChanged }
            else -> {
                // 影响下一轮matchItem的要保证配对的两个drawItem都一起进入修改
                itemsObserver.value?.filterIndexed { index, drawItem ->
                    if (drawItem.isChanged) {
                        true
                    } else {// 配对的是否为true
                        val coupleIndex = if (index%2 == 0) {
                            index + 1
                        }
                        else {
                            index - 1
                        }
                        var result = false
                        kotlin.runCatching {
                            result = itemsObserver.value?.get(coupleIndex)?.isChanged == true
                        }
                        result
                    }
                }
            }
        }
    }

    private fun updateDraw() {
        applyMeasureTimeLog("updateDraw") {
            drawRepository.updateDrawByRound(getCurrentRound(), getChangedDrawItems())
        }
    }

    fun createScore() {
        itemsObserver.value?.apply {
            launchSingleThread(
                { drawRepository.createScore(matchPeriod) },
                withLoading = true
            ) {
                messageObserver.value = "success"
            }
        }
    }

    fun getSelectFocusToRank(): Int {
        return if (matchPeriod.match.level == MatchConstants.MATCH_LEVEL_GS) {
            getLowestRankOfPage()
        } else {
            getLowestSeedRankOfPage()
        }
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

    private fun getLowestRankOfPage(): Int {
        var result = 0
        itemsObserver.value?.forEach {
            var rank = it.matchRecord1?.bean?.recordRank?:0
            if (rank > result) {
                result = rank
            }
            rank = it.matchRecord2?.bean?.recordRank?:0
            if (rank > result) {
                result = rank
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

    fun getWildcardList(): MutableList<WildcardBean> {
        var wildcards = mutableListOf<WildcardBean>()
        itemsObserver.value?.forEach {
            if (isWildcard(it.matchRecord1)) {
                val recordId = it.matchRecord1?.bean?.recordId?:0
                val rank = it.matchRecord1?.bean?.recordRank?:0
                wildcards.add(WildcardBean(recordId, rank, it.matchRecord1?.imageUrl))
            }
            if (isWildcard(it.matchRecord2)) {
                val recordId = it.matchRecord2?.bean?.recordId?:0
                val rank = it.matchRecord2?.bean?.recordRank?:0
                wildcards.add(WildcardBean(recordId, rank, it.matchRecord2?.imageUrl))
            }
        }
        return wildcards
    }

    fun arrangeWildcards(dataList: List<WildcardBean>) {
        arrangeWildcards(itemsObserver.value, dataList)
        itemsObserver.value = itemsObserver.value
    }

    private fun arrangeWildcards(drawItems: List<DrawItem>?, dataList: List<WildcardBean>) {
        var index = 0
        drawItems?.forEach {
            if (index >= dataList.size) {
                return
            }
            if (isWildcard(it.matchRecord1)) {
                it.matchRecord1?.imageUrl = dataList[index].imageUrl
                it.matchRecord1?.bean?.recordId = dataList[index].recordId
                it.matchRecord1?.bean?.recordRank = dataList[index].rank
                it.isChanged = true
                index ++
            }
            if (index >= dataList.size) {
                return
            }
            if (isWildcard(it.matchRecord2)) {
                it.matchRecord2?.imageUrl = dataList[index].imageUrl
                it.matchRecord2?.bean?.recordId = dataList[index].recordId
                it.matchRecord2?.bean?.recordRank = dataList[index].rank
                it.isChanged = true
                index ++
            }
        }
    }

    fun isNotSupportPreApply(): Boolean {
        return matchPeriod.match.level == MatchConstants.MATCH_LEVEL_LOW || matchPeriod.match.level == MatchConstants.MATCH_LEVEL_FINAL
    }

    fun randomWin(priority: Int) {
        itemsObserver.value?.forEach {
            it.winner = when {
                it.matchRecord1?.bean?.type == MatchConstants.MATCH_RECORD_BYE -> {
                    it.matchRecord2
                }
                it.matchRecord2?.bean?.type == MatchConstants.MATCH_RECORD_BYE -> {
                    it.matchRecord1
                }
                else -> {
                    when(priority) {
                        1, 2, 3, 4 -> maxBetween(priority, it.matchRecord1, it.matchRecord2)
                        else -> {
                            if (abs(random.nextInt()) % 2 == 0) {
                                it.matchRecord1
                            } else {
                                it.matchRecord2
                            }
                        }
                    }
                }
            }
            it.isChanged = true
        }
    }

    private fun maxBetween(priority: Int, rec1: MatchRecordWrap?, rec2: MatchRecordWrap?): MatchRecordWrap? {
        var num1 = 0
        var num2 = 0
        when(priority) {
            1 -> {
                num1 = rec1?.record?.scoreFeel?:0
                num2 = rec2?.record?.scoreFeel?:0
            }
            2 -> {
                num1 = rec1?.record?.scoreStar?:0
                num2 = rec2?.record?.scoreStar?:0
            }
            3 -> {
                num1 = rec1?.record?.scoreBody?:0
                num2 = rec2?.record?.scoreBody?:0
            }
            4 -> {
                num1 = rec1?.record?.scorePassion?:0
                num2 = rec2?.record?.scorePassion?:0
            }
        }
        return if (num1 > num2) {
            rec1
        }
        else if (num1 < num2) {
            rec2
        }
        else {
            if (abs(random.nextInt()) % 2 == 0) {
                rec1
            } else {
                rec2
            }
        }
    }

    fun randomPriority(count: Int): Int {
        return abs(random.nextInt()) % count
    }
}