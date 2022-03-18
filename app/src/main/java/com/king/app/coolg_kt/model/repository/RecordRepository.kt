package com.king.app.coolg_kt.model.repository

import android.text.TextUtils
import androidx.sqlite.db.SimpleSQLiteQuery
import com.king.app.coolg_kt.conf.AppConstants
import com.king.app.coolg_kt.conf.PreferenceValue
import com.king.app.coolg_kt.model.bean.PassionPoint
import com.king.app.coolg_kt.model.bean.RecordComplexFilter
import com.king.app.coolg_kt.model.bean.TitleValueBean
import com.king.app.coolg_kt.model.image.ImageProvider
import com.king.app.coolg_kt.page.record.popup.RecommendBean
import com.king.app.coolg_kt.utils.DebugLog
import com.king.app.gdb.data.DataConstants
import com.king.app.gdb.data.RecordCursor
import com.king.app.gdb.data.entity.RecordType1v1
import com.king.app.gdb.data.entity.RecordType3w
import com.king.app.gdb.data.relation.RecordStarWrap
import com.king.app.gdb.data.relation.RecordWrap
import io.reactivex.rxjava3.core.Observable
import java.util.*

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2020/12/15 10:37
 */
class RecordRepository: BaseRepository() {

    fun getRecord(recordId: Long): Observable<RecordWrap> {
        return Observable.create {
            it.onNext(getDatabase().getRecordDao().getRecord(recordId))
            it.onComplete()
        }
    }
    
    fun getRecordStars(recordId: Long): List<RecordStarWrap> {
        var list = getDatabase().getRecordDao().getRecordStars(recordId)
        // load image url
        list.forEach {
            it.imageUrl = ImageProvider.getStarRandomPath(it.star.name, null)
        }
        return list
    }

    fun getRecordFilter(
        sortMode: Int,
        desc: Boolean,
        mRecordType: Int,
        starId: Long,
        orderId: Long,
        tagId: Long,
        cursor: RecordCursor,
        filterBean: RecommendBean? = null,
        like: String? = null,
        whereScene: String? = null,
        outOfRank: Boolean = false,
        filterBlackList: Boolean = false
    ): Observable<RecordComplexFilter> {
        return Observable.create {
            val filter = RecordComplexFilter()
            var scene: String? = whereScene
            if (AppConstants.KEY_SCENE_ALL == whereScene) {
                scene = null
            }
            filter.scene = scene
            filter.cursor = cursor
            filter.sortType = sortMode
            filter.desc = desc
            filter.nameLike = like
            filter.recordType = mRecordType
            filter.filter = filterBean
            filter.starId = starId
            filter.studioId = orderId
            filter.tagId = tagId
            filter.outOfRank = outOfRank
            filter.filterBlacklist = filterBlackList
            it.onNext(filter)
            it.onComplete()
        }
    }

    fun getRecordsOutOfRank(isFilterBlacklist: Boolean): Observable<List<RecordWrap>> {
        return Observable.create {
            val pack = getCompletedPeriodPack()
            var result = listOf<RecordWrap>()
            pack.matchPeriod?.let { matchPeriod ->
                result = getDatabase().getRecordDao().getRecordsOutOfRank(matchPeriod.period, matchPeriod.orderInPeriod)
            }
            if (isFilterBlacklist) {
                val blacklist = getBlacklistIds()
                result = result.filter { item -> !blacklist.contains(item.bean.id!!) }
            }
            it.onNext(result)
            it.onComplete()
        }
    }

    fun getBlacklistIds(): List<Long> {
        val ids = mutableListOf<Long>()
        val blacklist = getDatabase().getMatchDao().getBlackList()
        blacklist.mapTo(ids) { item ->
            item.recordId
        }
        return ids
    }

    private fun getRecordsByFilter(filter: RecordComplexFilter): Observable<List<RecordWrap>> {
        return Observable.create {
            val buffer: StringBuffer = getComplexFilterBuilder(filter)
            filter.cursor?.let { cursor ->
                buffer.append(" LIMIT ").append(cursor.offset).append(",")
                    .append(cursor.number)
            }
            val sql = "select T.* from record T $buffer"
            DebugLog.e(sql)
            val list = getDatabase().getRecordDao().getRecordsBySql(SimpleSQLiteQuery(sql))
            it.onNext(list)
            it.onComplete()
        }
    }

    fun getRecords(filter: RecordComplexFilter): Observable<List<RecordWrap>> {
        return getRecordsByFilter(filter)
    }

    fun getRecordsImage(list: List<RecordWrap>): Observable<List<RecordWrap>> {
        return Observable.create {
            list.forEach {  record ->
                record.imageUrl = ImageProvider.getRecordRandomPath(record.bean.name, null)
            }
            it.onNext(list)
            it.onComplete()
        }
    }

    private fun getComplexFilterBuilder(filter: RecordComplexFilter): StringBuffer {
        val buffer = StringBuffer()
        val whereBuffer = StringBuffer()
        // 先处理JOIN
        filter.filter?.let {
            if (it.isOnlyType1v1 && !TextUtils.isEmpty(it.sql1v1)) {
                buildQueryFrom1v1(it, buffer)
            }
            else if (it.isOnlyType3w && !TextUtils.isEmpty(it.sql3w)) {
                buildQueryFrom3w(it, buffer)
            }
        }
        if (filter.starId != 0.toLong()) {
            buildQueryJoinStar(filter.starId, buffer)
        }
        if (filter.tagId !== 0.toLong()) {
            buildQueryJoinTag(filter.tagId, buffer)
        }
        // 再处理WHERE
        if (filter.studioId != 0.toLong()) {
            appendWhere(whereBuffer, "studioId=").append(filter.studioId)
        }
        filter.filter?.let {
            if (it.isOnline) {
                appendWhere(whereBuffer, "T.deprecated=0")
            }
            if (!TextUtils.isEmpty(it.sql)) {
                appendWhere(whereBuffer, it.sql)
            }
        }

        if (!TextUtils.isEmpty(filter.nameLike)) {
            appendWhere(whereBuffer, "NAME LIKE '%").append(filter.nameLike).append("%'")
        }
        if (!TextUtils.isEmpty(filter.scene)) {
            appendWhere(whereBuffer, "SCENE='").append(filter.scene).append("'")
        }
        // 以RecommendBean里的type为准
        if (filter.recordType == null) {
            filter.filter?.let {
                appendType(whereBuffer, it)
            }
        } else {
            // 0代表全部
            if (filter.recordType!! != 0) {
                appendWhere(whereBuffer, "TYPE=").append(filter.recordType!!)
            }
        }
        buffer.append(whereBuffer.toString())
        sortByColumn(buffer, filter.sortType, filter.desc)
        return buffer
    }

    fun getRecordsBy(bean: RecommendBean): Observable<List<RecordWrap>> {
        return Observable.create {

            val buffer = StringBuffer()
            if (isBuildType1v1(bean)) {
                buildQueryFrom1v1(bean, buffer)
            } else if (isBuildType3w(bean)) {
                buildQueryFrom3w(bean, buffer)
            }
            val where = StringBuffer()
            if (bean.isOnline) {
                appendWhere(where, "T.deprecated=0")
            }
            if (!TextUtils.isEmpty(bean.sql)) {
                appendWhere(where, bean.sql)
            }
            // record type
            // record type
            appendType(where, bean)

            buffer.append(where.toString())

            buffer.append(" ORDER BY RANDOM()")
            if (bean.number > 0) {
                buffer.append(" LIMIT ").append(bean.number)
            }
            val sql = "select * from record T ${buffer.toString()}"
            DebugLog.e(sql)
            val list = getDatabase().getRecordDao().getRecordsBySql(SimpleSQLiteQuery(sql))
            it.onNext(list)
            it.onComplete()
        }
    }

    private fun sortByColumn(buffer: StringBuffer, sortValue: Int, desc: Boolean) {
        when (sortValue) {
            PreferenceValue.GDB_SR_ORDERBY_DATE -> {
                buffer.append(" ORDER BY T.LAST_MODIFY_TIME").append(if (desc) " DESC" else " ASC")
            }
            PreferenceValue.GDB_SR_ORDERBY_SCORE -> {
                buffer.append(" ORDER BY T.SCORE").append(if (desc) " DESC" else " ASC")
            }
            PreferenceValue.GDB_SR_ORDERBY_PASSION -> {
                buffer.append(" ORDER BY T.SCORE_PASSION").append(if (desc) " DESC" else " ASC")
            }
            PreferenceValue.GDB_SR_ORDERBY_CUM -> {
                buffer.append(" ORDER BY T.SCORE_CUM").append(if (desc) " DESC" else " ASC")
            }
            PreferenceValue.GDB_SR_ORDERBY_STAR -> {
                buffer.append(" ORDER BY T.SCORE_STAR").append(if (desc) " DESC" else " ASC")
            }
            PreferenceValue.GDB_SR_ORDERBY_BODY -> {
                buffer.append(" ORDER BY T.SCORE_BODY").append(if (desc) " DESC" else " ASC")
            }
            PreferenceValue.GDB_SR_ORDERBY_COCK -> {
                buffer.append(" ORDER BY T.SCORE_COCK").append(if (desc) " DESC" else " ASC")
            }
            PreferenceValue.GDB_SR_ORDERBY_ASS -> {
                buffer.append(" ORDER BY T.SCORE_ASS").append(if (desc) " DESC" else " ASC")
            }
            PreferenceValue.GDB_SR_ORDERBY_SCOREFEEL -> {
                buffer.append(" ORDER BY T.SCORE_FEEL").append(if (desc) " DESC" else " ASC")
            }
            PreferenceValue.GDB_SR_ORDERBY_SPECIAL -> {
                buffer.append(" ORDER BY T.SCORE_SPECIAL").append(if (desc) " DESC" else " ASC")
            }
            else -> { // sort by name
                buffer.append(" ORDER BY T.NAME").append(if (desc) " DESC" else " ASC")
            }
        }
    }

    private fun appendWhere(where: StringBuffer, condition: String?): StringBuffer {
        if (where == null || where.isEmpty()) {
            where.append(" WHERE ").append(condition)
        } else {
            where.append(" AND ").append(condition)
        }
        return where
    }

    private fun buildQueryFrom1v1(bean: RecommendBean, buffer: StringBuffer) {
        buffer.append(" JOIN record_type1 RT ON T.RECORD_DETAIL_ID=RT._id AND T.TYPE=")
            .append(DataConstants.VALUE_RECORD_TYPE_1V1)
        buffer.append(" AND ").append(bean.sql1v1)
    }

    private fun buildQueryFrom3w(bean: RecommendBean, buffer: StringBuffer) {
        buffer.append(" JOIN record_type3 RT ON T.RECORD_DETAIL_ID=RT._id AND T.TYPE=")
            .append(DataConstants.VALUE_RECORD_TYPE_3W)
        buffer.append(" AND ").append(bean.sql3w)
    }

    private fun buildQueryJoinStar(starId: Long, buffer: StringBuffer) {
        buffer.append(" JOIN record_star RS ON RS.RECORD_ID=T._id AND RS.STAR_ID=").append(starId)
    }

    private fun buildQueryJoinTag(tagId: Long, buffer: StringBuffer) {
        buffer.append(" JOIN tag_record TR ON TR.RECORD_ID=T._id AND TR.TAG_ID=").append(tagId)
    }

    private fun appendType(where: StringBuffer, bean: RecommendBean) {
        if (!bean.isTypeAll && !isBuildType1v1(bean) && !isBuildType3w(bean)) {
            val types: MutableList<Int> = ArrayList()
            if (bean.isType1v1) {
                types.add(DataConstants.VALUE_RECORD_TYPE_1V1)
            }
            if (bean.isType3w) {
                types.add(DataConstants.VALUE_RECORD_TYPE_3W)
            }
            if (bean.isTypeMulti) {
                types.add(DataConstants.VALUE_RECORD_TYPE_MULTI)
            }
            if (bean.isTypeTogether) {
                types.add(DataConstants.VALUE_RECORD_TYPE_LONG)
            }
            if (types.size > 0) {
                if (types.size == 1) {
                    appendWhere(where, "T.TYPE=").append(types[0])
                } else {
                    appendWhere(where, "(")
                    for (i in types.indices) {
                        if (i > 0) {
                            where.append(" OR ")
                        }
                        where.append("T.TYPE=").append(types[i])
                    }
                    where.append(")")
                }
            }
        }
    }

    private fun isBuildType1v1(bean: RecommendBean): Boolean {
        return bean.isOnlyType1v1 && !TextUtils.isEmpty(bean.sql1v1)
    }

    private fun isBuildType3w(bean: RecommendBean): Boolean {
        return bean.isOnlyType3w && !TextUtils.isEmpty(bean.sql3w)
    }

    fun getPassions(record: RecordWrap): List<PassionPoint> {
        val pointList = mutableListOf<PassionPoint>()
        if (record.recordType1v1 != null) {
            getPassionList(pointList, record.recordType1v1!!)
        } else if (record.recordType3w != null) {
            getPassionList(pointList, record.recordType3w!!)
        }
        return pointList
    }

    private fun getPassionList(pointList: MutableList<PassionPoint>, record: RecordType3w) {
        if (record.scoreFkType1 > 0) {
            val point = PassionPoint()
            point.key = "For Sit"
            point.content = record.scoreFkType1.toString() + ""
            pointList.add(point)
        }
        if (record.scoreFkType2 > 0) {
            val point = PassionPoint()
            point.key = "Back Sit"
            point.content = record.scoreFkType2.toString() + ""
            pointList.add(point)
        }
        if (record.scoreFkType3 > 0) {
            val point = PassionPoint()
            point.key = "For"
            point.content = record.scoreFkType3.toString() + ""
            pointList.add(point)
        }
        if (record.scoreFkType4 > 0) {
            val point = PassionPoint()
            point.key = "Back"
            point.content = record.scoreFkType4.toString() + ""
            pointList.add(point)
        }
        if (record.scoreFkType5 > 0) {
            val point = PassionPoint()
            point.key = "Side"
            point.content = record.scoreFkType5.toString() + ""
            pointList.add(point)
        }
        if (record.scoreFkType6 > 0) {
            val point = PassionPoint()
            point.key = "Double"
            point.content = record.scoreFkType6.toString() + ""
            pointList.add(point)
        }
        if (record.scoreFkType7 > 0) {
            val point = PassionPoint()
            point.key = "Sequence"
            point.content = record.scoreFkType7.toString() + ""
            pointList.add(point)
        }
        if (record.scoreFkType8 > 0) {
            val point = PassionPoint()
            point.key = "Special"
            point.content = record.scoreFkType8.toString() + ""
            pointList.add(point)
        }
    }

    private fun getPassionList(
        pointList: MutableList<PassionPoint>,
        record: RecordType1v1
    ) {
        if (record.scoreFkType1 > 0) {
            val point = PassionPoint()
            point.key = "For Sit"
            point.content = record.scoreFkType1.toString() + ""
            pointList.add(point)
        }
        if (record.scoreFkType2 > 0) {
            val point = PassionPoint()
            point.key = "Back Sit"
            point.content = record.scoreFkType2.toString() + ""
            pointList.add(point)
        }
        if (record.scoreFkType3 > 0) {
            val point = PassionPoint()
            point.key = "For Stand"
            point.content = record.scoreFkType3.toString() + ""
            pointList.add(point)
        }
        if (record.scoreFkType4 > 0) {
            val point = PassionPoint()
            point.key = "Back Stand"
            point.content = record.scoreFkType4.toString() + ""
            pointList.add(point)
        }
        if (record.scoreFkType5 > 0) {
            val point = PassionPoint()
            point.key = "Side"
            point.content = record.scoreFkType5.toString() + ""
            pointList.add(point)
        }
        if (record.scoreFkType6 > 0) {
            val point = PassionPoint()
            point.key = "Special"
            point.content = record.scoreFkType6.toString() + ""
            pointList.add(point)
        }
    }

    fun createScoreItems(record: RecordWrap): Observable<List<TitleValueBean>> {
        return Observable.create {
            val list = mutableListOf<TitleValueBean>()
            when (record.bean.type) {
                DataConstants.VALUE_RECORD_TYPE_1V1 -> getScoreItems(record.recordType1v1, list)
                DataConstants.VALUE_RECORD_TYPE_3W, DataConstants.VALUE_RECORD_TYPE_MULTI, DataConstants.VALUE_RECORD_TYPE_LONG -> getScoreItems(record.recordType3w, list)
            }
            it.onNext(list)
            it.onComplete()
        }
    }

    private fun getScoreItems(record: RecordType3w?, list: MutableList<TitleValueBean>) {
        if (record == null) {
            return
        }
        if (record.scoreBjob > 0) {
            list.add(TitleValueBean("Bjob", record.scoreBjob.toString()))
        }
        if (record.scoreCshow > 0) {
            list.add(TitleValueBean("CShow", record.scoreCshow.toString()))
        }
        if (record.scoreForePlay > 0) {
            list.add(TitleValueBean("Foreplay", record.scoreForePlay.toString()))
        }
        if (record.scoreRhythm > 0) {
            list.add(TitleValueBean("Rhythm", record.scoreRhythm.toString()))
        }
        if (record.scoreRim > 0) {
            list.add(TitleValueBean("Rim", record.scoreRim.toString()))
        }
        if (record.scoreStory > 0) {
            list.add(TitleValueBean("Story", record.scoreStory.toString()))
        }
    }

    private fun getScoreItems(record: RecordType1v1?, list: MutableList<TitleValueBean>) {
        if (record == null) {
            return
        }
        if (record.scoreBjob > 0) {
            list.add(TitleValueBean("Bjob", record.scoreBjob.toString()))
        }
        if (record.scoreCshow > 0) {
            list.add(TitleValueBean("CShow", record.scoreCshow.toString()))
        }
        if (record.scoreForePlay > 0) {
            list.add(TitleValueBean("Foreplay", record.scoreForePlay.toString()))
        }
        if (record.scoreRhythm > 0) {
            list.add(TitleValueBean("Rhythm", record.scoreRhythm.toString()))
        }
        if (record.scoreRim > 0) {
            list.add(TitleValueBean("Rim", record.scoreRim.toString()))
        }
        if (record.scoreStory > 0) {
            list.add(TitleValueBean("Story", record.scoreStory.toString()))
        }
    }

    fun latestRecords(limitStart: Int, limitNum: Int): List<RecordWrap> {
        return getDatabase().getRecordDao().getLatestRecords(limitStart, limitNum)
    }

    fun getLatestRecords(limitStart: Int, limitNum: Int): Observable<List<RecordWrap>> {
        return Observable.create {
            var list = getDatabase().getRecordDao().getLatestRecords(limitStart, limitNum)
            it.onNext(list)
            it.onComplete()
        }
    }

    fun getLatestPlayableRecords(limitStart: Int, limitNum: Int): Observable<List<RecordWrap>> {
        return Observable.create {
            it.onNext(getDatabase().getRecordDao().getOnlineRecords(limitStart, limitNum))
            it.onComplete()
        }
    }

    fun getRecordsWithoutStudio(): Observable<List<RecordWrap>> {
        return Observable.create {
            val list = getDatabase().getRecordDao().getRecordsWithoutStudio()
            it.onNext(list)
            it.onComplete()
        }
    }
}