package com.king.app.coolg_kt.model.repository

import com.king.app.coolg_kt.conf.AppConstants
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

    fun getRecordStudio(recordId: Long): FavorRecordOrder? {
        var studioParentId = getDatabase().getFavorDao().getRecordOrderByName(AppConstants.ORDER_STUDIO_NAME)?.id
        studioParentId?.let {
            return getDatabase().getFavorDao().getStudioByRecord(recordId, it)
        }
        return null
    }

    /**
     * record的studio为唯一对应关系，如果已存在需要替换
     */
    fun addRecordToStudio(studioId: Long, recordId: Long): Observable<FavorRecord> {
        return Observable.create {
            var studioParentId = getDatabase().getFavorDao().getRecordOrderByName(AppConstants.ORDER_STUDIO_NAME)?.id
            var list = getDatabase().getFavorDao().getStudioRelationByRecord(recordId, studioParentId!!)
            if (list.isEmpty()) {
                it.onNext(insertFavorRecord(studioId, recordId))
            }
            // 已存在，替换
            else {
                // 历史脏数据可能存在多条，删除并重新插入。正常情况是1条，直接替换
                if (list.size > 1) {
                    // 先修改原order的数量统计
                    list.forEach { wrap ->
                        wrap.order?.let { order ->
                            order.number --
                            getDatabase().getFavorDao().updateFavorRecordOrder(order)
                        }
                        // 删除
                        getDatabase().getFavorDao().deleteFavorRecord(wrap.bean)
                    }
                    // 插入新的对应关系
                    it.onNext(insertFavorRecord(studioId, recordId))
                }
                else {
                    var bean = list.first()
                    // 与原studio不一样才修改
                    if (bean.bean.orderId != studioId) {
                        // 先修改原order的数量统计
                        bean.order?.let { order ->
                            order.number --
                            getDatabase().getFavorDao().updateFavorRecordOrder(order)
                        }
                        // 修改关联关系
                        bean.bean.orderId = studioId
                        getDatabase().getFavorDao().updateFavorRecord(bean.bean)
                        // 修改数量统计
                        increaseFavorRecordOrder(studioId)
                    }
                    it.onNext(bean.bean)
                }
            }
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