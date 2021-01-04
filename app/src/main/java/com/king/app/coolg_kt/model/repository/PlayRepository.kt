package com.king.app.coolg_kt.model.repository

import com.king.app.coolg_kt.model.bean.PlayItemViewBean
import com.king.app.coolg_kt.model.image.ImageProvider
import com.king.app.gdb.data.entity.PlayDuration
import com.king.app.gdb.data.entity.PlayItem
import com.king.app.gdb.data.entity.PlayOrder
import io.reactivex.rxjava3.core.Observable
import java.util.*

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2020/12/15 16:37
 */
class PlayRepository:BaseRepository() {

    fun getRecordOrders(recordId: Long): Observable<List<PlayOrder>> {
        return Observable.create {
            it.onNext(getDatabase().getPlayOrderDao().getRecordOrders(recordId))
            it.onComplete()
        }
    }

    fun insertPlayItem(recordId: Long, playUrl: String?, orderIds: ArrayList<CharSequence>?): Observable<Boolean> {
        return Observable.create {
            orderIds?.let {
                orderIds.forEach {
                    kotlin.runCatching {
                        val orderId: Long = it.toString().toLong()
                        var count = getDatabase().getPlayOrderDao().countPlayItem(recordId, orderId)
                        if (count == 0) {
                            var item = PlayItem(null, orderId, recordId, playUrl)
                            var list = mutableListOf<PlayItem>()
                            list.add(item)
                            getDatabase().getPlayOrderDao().insertPlayItems(list)
                        }
                    }
                }
            }
            it.onNext(true)
            it.onComplete()
        }

    }

    fun getDuration(recordId: Long): Observable<PlayDuration> {
        return Observable.create {
            var duration = getDatabase().getPlayOrderDao().getDurationByRecord(recordId)
            if (duration == null) {
                duration = PlayDuration(null, recordId)
            }
            it.onNext(duration)
            it.onComplete()
        }
    }

    fun isExist(orderId: Long, recordId: Long): Boolean {
        return getDatabase().getPlayOrderDao().countPlayItem(recordId, orderId) > 0
    }

    fun getPlayOrderItems(orderId: Long): Observable<List<PlayItemViewBean>> {
        return Observable.create {
            val items = getDatabase().getPlayOrderDao().getPlayItemsBy(orderId)
            val list = mutableListOf<PlayItemViewBean>()
            items.forEach { playItem ->
                var record = getDatabase().getRecordDao().getRecord(playItem.recordId)
                record?.let { rec ->
                    val item = PlayItemViewBean(rec, playItem)
                    item.cover = ImageProvider.getRecordRandomPath(rec.bean.name, null)
                    item.playUrl = playItem.url
                    list.add(item)
                }
            }
            it.onNext(list)
            it.onComplete()
        }
    }

    fun getStarPlayItems(starId: Long): Observable<List<PlayItemViewBean>> {
        return Observable.create {
            val records = getDatabase().getRecordDao().getStarOnlineRecords(starId)
            val list = mutableListOf<PlayItemViewBean>()
            records.forEach {
                val item = PlayItemViewBean(it)
                item.cover = ImageProvider.getRecordRandomPath(it.bean.name, null)
                list.add(item)
            }
            it.onNext(list)
            it.onComplete()
        }
    }

}