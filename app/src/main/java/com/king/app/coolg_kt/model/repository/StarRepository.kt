package com.king.app.coolg_kt.model.repository

import android.graphics.BitmapFactory
import androidx.sqlite.db.SimpleSQLiteQuery
import com.king.app.coolg_kt.conf.AppConstants
import com.king.app.coolg_kt.model.bean.LazyData
import com.king.app.coolg_kt.model.bean.StarBuilder
import com.king.app.coolg_kt.model.bean.StarDetailBuilder
import com.king.app.coolg_kt.model.image.ImageProvider
import com.king.app.coolg_kt.utils.DebugLog
import com.king.app.gdb.data.DataConstants
import com.king.app.gdb.data.relation.DebutStar
import com.king.app.gdb.data.relation.StarWrap
import io.reactivex.rxjava3.core.Observable

/**
 * @description:
 * @author：Jing
 * @date: 2020/12/20 11:34
 */
class StarRepository: BaseRepository() {

    fun getStar(id: Long): Observable<StarWrap> {
        return Observable.create {
            it.onNext(getDatabase().getStarDao().getStarWrap(id))
            it.onComplete()
        }
    }

    fun getAllStarsOrderByName(): Observable<List<StarWrap>> {
        return Observable.create {
            it.onNext(getDatabase().getStarDao().getAllStarsOrderByName())
            it.onComplete()
        }
    }

    fun queryStarWith(builder: StarBuilder): List<StarWrap> {

        var buffer = StringBuffer()
        // tables and joins
        if (builder.studioId == null) {
            buffer.append("select T.* from stars T ")
            builder.tagId?.let { tagId ->
                buffer.append("join tag_star TS on T._id=TS.STAR_ID and TS.TAG_ID=${tagId} ")
            }
        } else {
            // 有studioId的情况目前不支持tagId
            buffer.append(
                "select T.* from record r " +
                        "join record_star rs on r._id=rs.RECORD_ID and r.studioId=${builder.studioId} " +
                        "join stars T on rs.STAR_ID=T._id "
            )
        }
        // star_rating必须是left join，否则未评级的直接被过滤掉了
        if (builder.isSortByRating()) {
            buffer.append("left join star_rating SR on T._id=SR.STAR_ID ")
        }

        // where
        var where = ""
        var typeSql = when (builder.type) {
            DataConstants.STAR_MODE_TOP -> "T.BETOP>0 and T.BEBOTTOM=0 "
            DataConstants.STAR_MODE_BOTTOM -> "T.BEBOTTOM>0 and T.BETOP=0 "
            DataConstants.STAR_MODE_HALF -> "T.BEBOTTOM>0 and T.BETOP>0 "
            else -> ""
        }
        where = addToWhere(where, typeSql)
        if (where.isNotEmpty()) {
            buffer.append(where)
        }
        // group by 过滤重复项
        if (builder.studioId != null) {
            buffer.append("group by T._id ")
        }
        // order by
        when (builder.sortType) {
            AppConstants.STAR_SORT_NAME -> buffer.append("order by T.NAME COLLATE NOCASE ")// 名称不区分大小写
            AppConstants.STAR_SORT_RECORDS -> buffer.append("order by T.RECORDS desc ")
            AppConstants.STAR_SORT_RANDOM -> buffer.append("order by RANDOM() ")
            else -> buffer.append(convertSortRatingType(builder.sortType))
        }

        var sql = buffer.toString()
        DebugLog.e(sql)
        return getDatabase().getStarDao().getStarsBySql(SimpleSQLiteQuery(sql))
    }

    fun queryStarsBy(builder: StarBuilder): Observable<List<StarWrap>> {
        return Observable.create {
            it.onNext(queryStarWith(builder))
            it.onComplete()
        }
    }

    private fun addToWhere(where: String, condition: String): String {
        if (condition.isNotEmpty()) {
            return if (where.isEmpty()) "where $condition"
            else "$where and $condition"
        }
        return where
    }

    private fun convertSortRatingType(type: Int): String {
        return when(type) {
            AppConstants.STAR_SORT_RATING_FACE -> "order by SR.FACE desc"
            AppConstants.STAR_SORT_RATING_BODY -> "order by SR.BODY desc"
            AppConstants.STAR_SORT_RATING_SEXUALITY -> "order by SR.SEXUALITY desc"
            AppConstants.STAR_SORT_RATING_DK -> "order by SR.DK desc"
            AppConstants.STAR_SORT_RATING_PASSION -> "order by SR.PASSION desc"
            AppConstants.STAR_SORT_RATING_VIDEO -> "order by SR.VIDEO desc"
            AppConstants.STAR_SORT_RATING_PREFER -> "order by SR.PREFER desc"
            else -> "order by SR.COMPLEX desc"
        }
    }

    /**
     * 每加载指定个数时通知
     * @param list
     * @param num
     * @return
     */
    fun lazyLoad(list: List<StarWrap>, num: Int, builder: StarDetailBuilder): Observable<LazyData<StarWrap>> {
        return Observable.create {
            var count = 0
            var start = 0
//            CostTimeUtil.start()
            list.forEach { proxy ->
                // 设置图片路径（耗时操作）
                if (builder.isLoadImageSize) {
                    proxy.imagePath = ImageProvider.getStarRandomPath(proxy.bean.name, null)
                }
                // 瀑布流还要计算图片合适尺寸（耗时操作）
                if (builder.isLoadImageSize) {
                    calcImageSize(proxy, builder)
                }
                // 加载rating数据（第一次执行lazyLoads属于耗时操作，之后就属于GreenDao的缓存中了，变为非耗时操作）
//                if (builder.isLoadRating) {
//                    proxy.rating
//                }
                count++
                // 满足num个进行通知
                if (count == num) {
                    it.onNext(LazyData(start, count, list))
                    start = count
                    count = 0
//                    CostTimeUtil.end("onNext")
//                    CostTimeUtil.start()
                }
            }
            // 处理最后一批不足num的情况
            // 处理最后一批不足num的情况
            if (count != num) {
                it.onNext(LazyData(start, count, list))
            }
            it.onComplete()

        }
    }

    private fun calcImageSize(bean: StarWrap, builder: StarDetailBuilder) {
        // 无图按16:9
        if (bean.imagePath == null) {
            bean.width = builder.sizeBaseWidth
            bean.height = builder.sizeBaseWidth * 9 / 16
        } else {
            //缩放图片的实际宽高
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(bean.imagePath, options)
            var height = options.outHeight
            val width = options.outWidth
            val ratio = builder.sizeBaseWidth.toFloat() / width.toFloat()
            bean.width = builder.sizeBaseWidth
            bean.height = (height * ratio).toInt()
        }
    }

    /**
     *
     * @param type 0 All, 1 Top, 2 bottom, 3 half
     * @param conditions eg. {"complex > 3", "face > 4.2"}
     * @return
     */
    fun queryStar(type: Int, conditions: Array<String>?): List<StarWrap> {
        val buffer = StringBuffer("select T.* from stars T ")
        // rating
        conditions?.let {
            buffer.append("join star_rating SR on T._id=SR.STAR_ID ")
            it.forEach { condition ->
                buffer.append(" AND SR.").append(condition)
            }
        }
        when (type) {
            1 -> buffer.append(" WHERE T.BETOP>0 AND T.BEBOTTOM=0")
            2 -> buffer.append(" WHERE T.BEBOTTOM>0 AND T.BETOP=0")
            3 -> buffer.append(" WHERE T.BETOP>0 AND T.BEBOTTOM>0")
        }
        val sql = buffer.toString()
        DebugLog.e(sql)
        return getDatabase().getStarDao().getStarsBySql(SimpleSQLiteQuery(sql))
    }

    fun getDebutStars(): List<DebutStar> {
        return getDatabase().getStarDao().getDebutStar()
    }
}