package com.king.app.coolg_kt.model.repository

import com.king.app.gdb.data.entity.FavorRecord
import com.king.app.gdb.data.entity.FavorRecordOrder
import com.king.app.gdb.data.entity.FavorStar
import com.king.app.gdb.data.entity.FavorStarOrder
import io.reactivex.rxjava3.core.Observable
import java.util.*

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2020/12/15 16:37
 */
class OrderRepository:BaseRepository() {

    fun getRecordOrders(recordId: Long): Observable<List<FavorRecordOrder>> {
        return Observable.create {
            it.onNext(getDatabase().getFavorDao().getRecordOrders(recordId))
            it.onComplete()
        }
    }
    fun addFavorRecord(orderId: Long, recordId: Long): Observable<FavorRecord> {
        return Observable.create {
            var bean = getDatabase().getFavorDao().getFavorRecordBy(recordId, orderId)
            if (bean == null) {
                // insert to favor_record
                var time = Date().time
                bean = FavorRecord(null, orderId, recordId, time, time)
                var list = mutableListOf<FavorRecord>()
                list.add(bean)
                getDatabase().getFavorDao().insertFavorRecords(list)
                // update number in favor_record_order
                var order = getDatabase().getFavorDao().getFavorRecordOrderBy(orderId)
                order?.let { fOrder ->
                    fOrder.number += 1
                    getDatabase().getFavorDao().updateFavorRecordOrder(fOrder)
                }
                it.onNext(bean)
                it.onComplete()
            }
            else {
                it.onError(Exception("Target is already in order"))
            }
        }
    }

    fun getStarOrders(recordId: Long): Observable<List<FavorStarOrder>> {
        return Observable.create {
            it.onNext(getDatabase().getFavorDao().getStarOrders(recordId))
            it.onComplete()
        }
    }
    fun addFavorStar(orderId: Long, starId: Long): Observable<FavorStar> {
        return Observable.create {
            var bean = getDatabase().getFavorDao().getFavorStarBy(starId, orderId)
            if (bean == null) {
                // insert to favor_star
                var time = Date().time
                bean = FavorStar(null, orderId, starId, time, time)
                var list = mutableListOf<FavorStar>()
                getDatabase().getFavorDao().insertFavorStars(list)
                // update number in favor_star_order
                var order = getDatabase().getFavorDao().getFavorStarOrderBy(orderId)
                order?.let { fOrder ->
                    fOrder.number += 1
                    getDatabase().getFavorDao().updateFavorStarOrder(fOrder)
                }
                it.onNext(bean)
                it.onComplete()
            }
            else {
                it.onError(Exception("Target is already in order"))
            }
        }
    }
}