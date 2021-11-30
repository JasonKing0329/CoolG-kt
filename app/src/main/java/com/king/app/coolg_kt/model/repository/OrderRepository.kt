package com.king.app.coolg_kt.model.repository

import com.king.app.gdb.data.entity.*
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
                it.onNext(insertFavorRecord(orderId, recordId))
                it.onComplete()
            }
            else {
                it.onError(Exception("Target is already in order"))
            }
        }
    }

    private fun insertFavorRecord(orderId: Long, recordId: Long): FavorRecord {
        // insert to favor_record
        var time = Date().time
        var bean = FavorRecord(null, orderId, recordId, time, time)
        var list = mutableListOf<FavorRecord>()
        list.add(bean)
        getDatabase().getFavorDao().insertFavorRecords(list)
        // update number in favor_record_order
        increaseFavorRecordOrder(orderId)
        return bean
    }

    private fun increaseFavorRecordOrder(orderId: Long) {
        var order = getDatabase().getFavorDao().getFavorRecordOrderBy(orderId)
        order?.let { fOrder ->
            fOrder.number += 1
            getDatabase().getFavorDao().updateFavorRecordOrder(fOrder)
        }
    }

    fun getRecordStudio(record: Record?): FavorRecordOrder? {
        record?.let {
            return getDatabase().getFavorDao().getStudioById(it.studioId)
        }
        return null
    }

    fun getRecordStudio(recordId: Long): FavorRecordOrder? {
        return getDatabase().getFavorDao().getStudioByRecord(recordId)
    }

    /**
     * record的studio为唯一对应关系，如果已存在需要替换
     */
    fun addRecordToStudio(studioId: Long, record: Record): Observable<Boolean> {
        return Observable.create {
            var oldStudioId = record.studioId
            record.studioId = studioId
            getDatabase().getRecordDao().updateRecord(record)
            // 更新后修改原有studio与新studio的数量统计
            val count = getDatabase().getRecordDao().getStudioCount(studioId)
            getDatabase().getFavorDao().getStudioById(studioId)?.let { studio ->
                studio.number = count
                getDatabase().getFavorDao().updateFavorRecordOrder(studio)
            }
            if (oldStudioId != 0L && oldStudioId != studioId) {
                val count = getDatabase().getRecordDao().getStudioCount(oldStudioId)
                getDatabase().getFavorDao().getStudioById(oldStudioId)?.let { studio ->
                    studio.number = count
                    getDatabase().getFavorDao().updateFavorRecordOrder(studio)
                }
            }
            it.onNext(true)
            it.onComplete()
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