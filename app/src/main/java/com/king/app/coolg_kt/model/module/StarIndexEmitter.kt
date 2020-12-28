package com.king.app.coolg_kt.model.module

import android.text.TextUtils
import com.king.app.coolg_kt.conf.AppConstants
import com.king.app.coolg_kt.model.bean.IndexRange
import com.king.app.coolg_kt.utils.StarRatingUtil
import com.king.app.gdb.data.relation.StarWrap
import io.reactivex.rxjava3.core.ObservableEmitter

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/5/21 10:15
 */
class StarIndexEmitter {

    val playerIndexMap = mutableMapOf<String, IndexRange>()

    fun clear() {
        playerIndexMap.clear()
    }

    private var mLastRange: IndexRange? = null

    fun getIndex(position: Int): String {
        val iterator: Iterator<String> = playerIndexMap.keys.iterator()
        var index = "Unknown"
        while (iterator.hasNext()) {
            val key = iterator.next()
            val range = playerIndexMap[key]
            if (range != null && position >= range.start && position <= range.end) {
                index = key
                break
            }
        }
        return index
    }

    private fun endCreate(lastIndex: Int) {
        mLastRange?.end = lastIndex
    }

    private fun addIndex(e: ObservableEmitter<String>, value: String, i: Int) {
        var range = playerIndexMap[value]
        // 新index出现
        if (range == null) {
            // 结束上一个range
            mLastRange?.end = i - 1
            range = IndexRange()
            range.start = i
            playerIndexMap[value] = range
            mLastRange = range
            e.onNext(value)
        }
    }

    fun createRecordsIndex(e: ObservableEmitter<String>, mList: List<StarWrap>) {
        // list查询出来已经是有序的
        mList.forEachIndexed { index, starWrap ->
            val number: Int = starWrap.recordList.size
            //1 2 3 4 5 6 7 8 9 10 12 15 20 25 30 35 40 40+
            var key = when {
                number > 40 -> "40+"
                number in 36..40 -> "40"
                number in 31..35 -> "35"
                number in 26..30 -> "30"
                number in 21..25 -> "25"
                number in 16..20 -> "20"
                number in 13..15 -> "15"
                number in 11..12 -> "12"
                else -> number.toString()
            }
            addIndex(e, key, index)
        }
        endCreate(mList.size - 1)
    }

    fun createRatingIndex(e: ObservableEmitter<String>, mList: List<StarWrap>, type: Int) {
        // list查询出来已经是有序的
        for (i in mList.indices) {
            val rating = StarRatingUtil.getRatingValue(getRatingValue(mList[i], type))
            addIndex(e, rating, i)
        }
        endCreate(mList.size - 1)
    }

    private fun getRatingValue(proxy: StarWrap, type: Int): Float {
        var value = 0f
        proxy.rating?.let {
            value = when (type) {
                AppConstants.STAR_SORT_RATING -> it.complex
                AppConstants.STAR_SORT_RATING_FACE -> it.face
                AppConstants.STAR_SORT_RATING_BODY -> it.body
                AppConstants.STAR_SORT_RATING_DK -> it.dk
                AppConstants.STAR_SORT_RATING_SEXUALITY -> it.sexuality
                AppConstants.STAR_SORT_RATING_PASSION -> it.passion
                AppConstants.STAR_SORT_RATING_VIDEO -> it.video
                AppConstants.STAR_SORT_RATING_PREFER -> it.prefer
                else -> 0f
            }
        }
        return value
    }

    fun createNameIndex(e: ObservableEmitter<String>, mList: List<StarWrap>) {
        // player list查询出来已经是有序的
        mList.forEachIndexed { index, starWrap ->
            val targetText = starWrap.bean.name
            var first = if (TextUtils.isEmpty(targetText)) {
                "#"
            } else {
                targetText!![0].toString()
            }
            addIndex(e, first, index)
        }
        endCreate(mList.size - 1)
    }
}