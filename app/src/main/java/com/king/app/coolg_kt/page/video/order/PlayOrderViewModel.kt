package com.king.app.coolg_kt.page.video.order

import android.app.Application
import android.view.View
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.model.bean.VideoPlayList
import com.king.app.coolg_kt.model.http.observer.SimpleObserver
import com.king.app.coolg_kt.model.image.ImageProvider.parseCoverUrl
import com.king.app.gdb.data.entity.PlayOrder
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableSource

class PlayOrderViewModel(application: Application) : BaseViewModel(application) {
    var dataObserver = MutableLiveData<List<VideoPlayList>>()

    fun loadOrders() {
        orders
            .flatMap { toViewItems(it) }
            .flatMap { sort(it) }
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<List<VideoPlayList>>(getComposite()) {

                override fun onNext(videoPlayLists: List<VideoPlayList>) {
                    dataObserver.setValue(videoPlayLists)
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                    messageObserver.setValue(e?.message)
                }

            })
    }

    private val orders: Observable<List<PlayOrder>>
        private get() = Observable.create {
            it.onNext(getDatabase().getPlayOrderDao().getAllPlayOrders())
            it.onComplete()
        }

    private fun toViewItems(list: List<PlayOrder>): ObservableSource<List<VideoPlayList>> {
        return ObservableSource {
            val result = mutableListOf<VideoPlayList>()
            for (order in list) {
                val playList = VideoPlayList()
                playList.name = order.name
                playList.imageUrl = parseCoverUrl(order.coverUrl)
                playList.playOrder = order
                playList.visibility = View.GONE
                playList.videos = getDatabase().getPlayOrderDao().countOrderItems(order.id!!)
                result.add(playList)
            }
            it.onNext(result)
            it.onComplete()
        }
    }

    fun addPlayOrder(name: String) {
        val order = PlayOrder(null, name, null)
        getDatabase().getPlayOrderDao().insertPlayOrder(order)
        loadOrders()
    }

    fun executeDelete() {
        dataObserver.value?.filter { it.isChecked }
            ?.forEach { item ->
                getDatabase().runInTransaction {
                    var orderId = item.playOrder!!.id!!
                    // delete from play_order
                    getDatabase().getPlayOrderDao().deletePlayOrder(item.playOrder!!)
                    // delete from play_item
                    getDatabase().getPlayOrderDao().deleteItemsInPlayOrder(orderId)
                    // delete from video_cover_play_Order
                    getDatabase().getPlayOrderDao().deletePlayOrderCover(orderId)
                }
            }
        loadOrders()
    }

    val selectedItems: ArrayList<CharSequence>
        get() {
            val list = arrayListOf<CharSequence>()
            dataObserver.value?.filter { it.isChecked }
                ?.forEach {
                    list.add(it.playOrder!!.id.toString())
                }
            return list
        }

    fun updateOrderName(data: VideoPlayList, name: String) {
        data.name = name
        data.playOrder!!.name = name
        getDatabase().getPlayOrderDao().updatePlayOrder(data.playOrder!!)
    }

    private fun sort(list: List<VideoPlayList>): Observable<List<VideoPlayList>> {
        return Observable.create {
            var result = list.sortedBy { item -> item.name }
            it.onNext(result)
            it.onComplete()
        }
    }
}