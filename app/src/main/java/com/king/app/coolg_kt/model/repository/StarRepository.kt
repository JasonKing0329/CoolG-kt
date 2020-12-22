package com.king.app.coolg_kt.model.repository

import android.graphics.BitmapFactory
import androidx.sqlite.db.SimpleSQLiteQuery
import com.king.app.coolg_kt.conf.AppConstants
import com.king.app.coolg_kt.model.bean.LazyData
import com.king.app.coolg_kt.model.bean.StarDetailBuilder
import com.king.app.coolg_kt.model.bean.StarSortBuilder
import com.king.app.coolg_kt.model.image.ImageProvider
import com.king.app.coolg_kt.utils.DebugLog
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

    fun queryStarsBy(builder: StarSortBuilder): Observable<List<StarWrap>> {
        return Observable.create {
            var buffer = StringBuffer("select T.* from stars T ")
            builder.tagId?.let { tagId ->
                buffer.append("join tag_star TS on T._id=TS.STAR_ID and TS.TAG_ID=").append(tagId).append(" ")
            }
            when {
                builder.isOrderByName -> buffer.append("order by T.NAME")
                builder.isOrderByRecords -> buffer.append("order by T.RECORDS")
                builder.isOrderByRandom -> buffer.append("order by RANDOM()")
                builder.orderByRatingType != -1 -> {
                    buffer.append("join star_rating SR on T._id=SR.STAR_ID ")
                        .append(convertSortRatingType(builder.orderByRatingType))
                }
            }
            var sql = buffer.toString()
            DebugLog.e(sql)
            var list = getDatabase().getStarDao().getStarsBySql(SimpleSQLiteQuery(sql))
            it.onNext(list)
            it.onComplete()
        }
    }

    private fun convertSortRatingType(type: Int): String {
        return when(type) {
            AppConstants.STAR_RATING_SORT_FACE -> "order by SR.FACE desc"
            AppConstants.STAR_RATING_SORT_BODY -> "order by SR.BODY desc"
            AppConstants.STAR_RATING_SORT_SEX -> "order by SR.SEXUALITY desc"
            AppConstants.STAR_RATING_SORT_DK -> "order by SR.DK desc"
            AppConstants.STAR_RATING_SORT_PASSION -> "order by SR.PASSION desc"
            AppConstants.STAR_RATING_SORT_VIDEO -> "order by SR.VIDEO desc"
            AppConstants.STAR_RATING_SORT_PREFER -> "order by SR.PREFER desc"
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

}