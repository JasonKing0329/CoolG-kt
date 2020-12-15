package com.king.app.coolg_kt.model.repository

import android.text.TextUtils
import androidx.sqlite.db.SimpleSQLiteQuery
import com.king.app.coolg_kt.conf.AppConstants
import com.king.app.coolg_kt.conf.PreferenceValue
import com.king.app.coolg_kt.model.bean.RecordComplexFilter
import com.king.app.coolg_kt.page.record.popup.RecommendBean
import com.king.app.coolg_kt.utils.DebugLog
import com.king.app.gdb.data.DataConstants
import com.king.app.gdb.data.RecordCursor
import com.king.app.gdb.data.relation.RecordWrap
import io.reactivex.rxjava3.core.Observable
import java.util.*

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2020/12/15 10:37
 */
class RecordRepository: BaseRepository() {

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
        whereScene: String? = null
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
            it.onNext(filter)
            it.onComplete()
        }
    }

    fun getRecords(filter: RecordComplexFilter): Observable<List<RecordWrap>> {
        return Observable.create {
            val buffer: StringBuffer = getComplexFilterBuilder(filter)
            filter.cursor?.let { cursor ->
                buffer.append(" LIMIT ").append(cursor.offset).append(",")
                    .append(cursor.number)
            }
            val sql = "select * from record T ${buffer.toString()}"
            DebugLog.e(sql)
            val list = getDatabase().getRecordDao().getRecordsBySql(SimpleSQLiteQuery(sql))
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
        if (filter.studioId != 0.toLong()) {
            buildQueryJoinStudio(filter.studioId, buffer)
        }
        if (filter.tagId !== 0.toLong()) {
            buildQueryJoinTag(filter.tagId, buffer)
        }
        // 再处理WHERE
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

    private fun buildQueryJoinStudio(studioId: Long, buffer: StringBuffer) {
        buffer.append(" JOIN favor_record FR ON FR.RECORD_ID=T._id AND FR.ORDER_ID=").append(studioId)
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

}