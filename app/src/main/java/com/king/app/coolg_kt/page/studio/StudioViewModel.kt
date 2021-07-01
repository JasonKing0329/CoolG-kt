package com.king.app.coolg_kt.page.studio

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.sqlite.db.SimpleSQLiteQuery
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.conf.AppConstants
import com.king.app.coolg_kt.model.http.observer.SimpleObserver
import com.king.app.coolg_kt.model.image.ImageProvider.parseCoverUrl
import com.king.app.coolg_kt.model.setting.SettingProperty
import com.king.app.gdb.data.entity.FavorRecordOrder
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableSource

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/9/18 11:15
 */
class StudioViewModel(application: Application) : BaseViewModel(application) {

    var simpleObserver = MutableLiveData<List<StudioSimpleItem>>()
    var richObserver = MutableLiveData<List<StudioRichItem>>()
    var listTypeMenuObserver = MutableLiveData<String>()

    /**
     * 在本页面范围内，修正过一次即可
     */
    private var isCountCorrected = false

    fun toggleListType() {
        if (AppConstants.STUDIO_LIST_TYPE_RICH === SettingProperty.getStudioListType()) {
            SettingProperty.setStudioListType(AppConstants.STUDIO_LIST_TYPE_SIMPLE)
        } else {
            SettingProperty.setStudioListType(AppConstants.STUDIO_LIST_TYPE_RICH)
        }
        loadStudios()
    }

    fun onSortTypeChanged(sortType: Int) {
        val curType: Int = SettingProperty.getStudioListSortType()
        if (sortType != curType) {
            SettingProperty.setStudioListSortType(sortType)
            loadStudios()
        }
    }

    fun loadStudios() {
        if (AppConstants.STUDIO_LIST_TYPE_RICH === SettingProperty.getStudioListType()) {
            listTypeMenuObserver.setValue("Simple List")
            loadRichItems()
        } else {
            listTypeMenuObserver.setValue("Rich List")
            loadSimpleItems()
        }
    }

    private fun loadSimpleItems() {
        loadingObserver.value = true
        getStudios()
            .flatMap { countStudioItems(it) }
            .flatMap { toSimpleItems(it) }
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<List<StudioSimpleItem>>(getComposite()) {

                override fun onNext(studioSimpleItems: List<StudioSimpleItem>) {
                    loadingObserver.value = false
                    simpleObserver.value = studioSimpleItems
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                    loadingObserver.value = false
                    messageObserver.value = e?.message
                }
            })
    }

    private fun loadRichItems() {
        loadingObserver.value = true
        getStudios()
            .flatMap { countStudioItems(it) }
            .flatMap { toRichItems(it) }
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<List<StudioRichItem>>(getComposite()) {

                override fun onNext(richItems: List<StudioRichItem>) {
                    loadingObserver.value = false
                    richObserver.value = richItems
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                    loadingObserver.value = false
                    messageObserver.value = e?.message
                }
            })
    }

    private fun countStudioItems(list: List<FavorRecordOrder>): Observable<List<FavorRecordOrder>> {
        return if (isCountCorrected) {
            Observable.create {
                it.onNext(list)
                it.onComplete()
            }
        }
        else {
            Observable.create {
                val correctList = mutableListOf<FavorRecordOrder>()
                list.forEach { order ->
                    // 更新order实际数量
                    var newNum = getDatabase().getFavorDao().countRecordOrderItems(order.id!!)
                    if (newNum != order.number) {
                        correctList.add(order)
                    }
                }
                // 修正实际数量
                if (correctList.isNotEmpty()) {
                    getDatabase().getFavorDao().updateFavorRecordOrders(correctList)
                }
                // 标记已修正过
                isCountCorrected = true
                it.onNext(list)
                it.onComplete()
            }
        }
    }

    private fun getStudios(): Observable<List<FavorRecordOrder>> {
        return Observable.create {
            var list = listOf<FavorRecordOrder>()
            val studio = getDatabase().getFavorDao().getRecordOrderByName(AppConstants.ORDER_STUDIO_NAME)
            studio?.let { parent ->
                val sortType: Int = SettingProperty.getStudioListSortType()
                var sqlBuffer = StringBuffer("select * from favor_order_record where PARENT_ID=");
                sqlBuffer.append(parent.id)
                when(sortType) {
                    AppConstants.STUDIO_LIST_SORT_NUM -> sqlBuffer.append(" order by NUMBER desc")
                    AppConstants.STUDIO_LIST_SORT_CREATE_TIME -> sqlBuffer.append(" order by CREATE_TIME desc")
                    AppConstants.STUDIO_LIST_SORT_UPDATE_TIME -> sqlBuffer.append(" order by UPDATE_TIME desc")
                    else -> sqlBuffer.append(" order by NAME")
                }
                list = getDatabase().getFavorDao().getRecordOrdersBySql(SimpleSQLiteQuery(sqlBuffer.toString()))
            }
            it.onNext(list)
            it.onComplete()
        }
    }

    private fun toSimpleItems(list: List<FavorRecordOrder>): ObservableSource<MutableList<StudioSimpleItem>> {
        return ObservableSource {
            val result = mutableListOf<StudioSimpleItem>()
            list.forEach { order ->
                val item = StudioSimpleItem(order)
                order.name?.let {  name ->
                    item.name = name
                    item.firstChar = name[0].toString()
                }
                item.number = order.number.toString()
                result.add(item)
            }
            it.onNext(result)
            it.onComplete()
        }
    }

    private fun toRichItems(list: List<FavorRecordOrder>): ObservableSource<List<StudioRichItem>> {
        return ObservableSource {
            val result = mutableListOf<StudioRichItem>()
            list.forEach { order ->
                val item = StudioRichItem(order)
                item.imageUrl = parseCoverUrl(order.coverUrl)
                item.name = order.name
                countRichInfo(order, item)
                result.add(item)
            }
            it.onNext(result)
            it.onComplete()
        }
    }

    private fun countRichInfo(order: FavorRecordOrder, item: StudioRichItem) {
        val videos = order.number
        val countStar = getDatabase().getFavorDao().countStudioStarNumber(order.id!!)
        var countHigh = getDatabase().getFavorDao().countRecordScoreOver(order.id!!, 600)
        item.count = "$videos Videos, $countStar Stars"
        if (countHigh > 0) {
            item.high = "600+ Videos: $countHigh"
        }
    }

    fun addNewStudio(name: String?) {
        if (name == null || name.isEmpty()) {
            messageObserver.value = "Empty name"
            return
        }
        val parent = getDatabase().getFavorDao().getRecordOrderByName(AppConstants.ORDER_STUDIO_NAME)
        parent?.let {
            var studio = getDatabase().getFavorDao().getStudioByName(name, it.id!!)
            if (studio == null) {
                val time = System.currentTimeMillis()
                studio = FavorRecordOrder(null, name, null, 0, 0, time, time, it.id!!)
                val list = listOf(studio)
                getDatabase().getFavorDao().insertFavorRecordOrders(list)
                messageObserver.value = "success"
                loadStudios()
            }
            else {
                messageObserver.value = "Studio '$name' is already existed"
            }
        }
    }

    fun deleteStudio(order: FavorRecordOrder) {
        getDatabase().runInTransaction {
            getDatabase().getFavorDao().deleteAllRecordsInOrder(order.id!!)
            getDatabase().getFavorDao().deleteFavorRecordOrder(order)
            messageObserver.value = "success"
            loadStudios()
        }
    }

    fun updateStudioName(order: FavorRecordOrder, name: String) {
        getDatabase().runInTransaction {
            order.name = name
            getDatabase().getFavorDao().updateFavorRecordOrder(order)
            messageObserver.value = "success"
        }
    }
}