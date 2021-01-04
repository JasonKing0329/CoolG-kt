package com.king.app.coolg_kt.page.video.order

import android.app.Application
import android.text.TextUtils
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.model.bean.PlayItemViewBean
import com.king.app.coolg_kt.model.http.AppHttpClient
import com.king.app.coolg_kt.model.http.bean.request.PathRequest
import com.king.app.coolg_kt.model.http.observer.SimpleObserver
import com.king.app.coolg_kt.model.repository.PlayRepository
import com.king.app.coolg_kt.page.video.player.PlayListInstance
import com.king.app.coolg_kt.utils.ScreenUtils
import com.king.app.coolg_kt.utils.UrlUtil
import com.king.app.coolg_kt.view.widget.video.UrlCallback
import io.reactivex.rxjava3.core.Observable

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/1/4 10:32
 */
class PlayOrderItemsViewModel(application: Application): BaseViewModel(application) {

    var actionbarTitleText = ObservableField<String>()

    var totalText = ObservableField<String>()

    var itemsObserver = MutableLiveData<List<PlayItemViewBean>>()
    var playListCreated = MutableLiveData<Boolean>()
    var videoPlayOnReadyObserver = MutableLiveData<Boolean>()

    var repository = PlayRepository()

    var mOrderId: Long = -1
    var mStarId: Long = -1

    init {
        updateInfo("", "")
    }

    private fun updateInfo(name: String?, total: String) {
        var actionText = name
        if (ScreenUtils.isTablet() && !TextUtils.isEmpty(total)) {
            actionText = "$name ($total)"
        }
        actionbarTitleText.set(actionText)
        totalText.set(total)
    }

    fun definePage(orderId: Long, starId: Long) {
        if (orderId != -1L) {
            getOrderItems(orderId)
        }
        else if (starId != -1L) {
            getStarItems(starId)
        }
    }

    private fun getOrderItems(orderId: Long) {
        mOrderId = orderId
        var order = getDatabase().getPlayOrderDao().getPlayOrder(orderId)
        order?.let {
            updateInfo(order.name, "0")
            loadingObserver.value= true
            repository.getPlayOrderItems(orderId)
                .compose(applySchedulers())
                .subscribe(object : SimpleObserver<List<PlayItemViewBean>>(getComposite()) {
                    override fun onNext(t: List<PlayItemViewBean>) {
                        loadingObserver.value= false
                        itemsObserver.value = t
                        updateInfo(order.name, "${t.size} Videos")
                    }

                    override fun onError(e: Throwable?) {
                        e?.printStackTrace()
                        loadingObserver.value= false
                        messageObserver.value = e?.message
                    }
                })
        }
    }

    private fun getStarItems(starId: Long) {
        mStarId = starId
        var star = getDatabase().getStarDao().getStar(starId)
        star?.let {
            updateInfo(star.name, "0")
            loadingObserver.value= true
            repository.getStarPlayItems(starId)
                .compose(applySchedulers())
                .subscribe(object : SimpleObserver<List<PlayItemViewBean>>(getComposite()) {
                    override fun onNext(t: List<PlayItemViewBean>) {
                        loadingObserver.value= false
                        itemsObserver.value = t
                        updateInfo(star.name, "${t.size} Videos")
                    }

                    override fun onError(e: Throwable?) {
                        e?.printStackTrace()
                        loadingObserver.value= false
                        messageObserver.value = e?.message
                    }
                })
        }
    }

    fun clearOrder() {
        getDatabase().getPlayOrderDao().deletePlayItems()
        itemsObserver.value = mutableListOf()
    }

    fun createPlayList(clearCurrent: Boolean, isRandom: Boolean) {
        addToPlayList(itemsObserver.value, clearCurrent, isRandom)
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<Boolean>(getComposite()) {
                override fun onNext(t: Boolean) {
                    playListCreated.value = true;
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                    messageObserver.value = e?.message
                }
            });
    }

    private fun addToPlayList(playItems: List<PlayItemViewBean>?, clearCurrent: Boolean, isRandom: Boolean): Observable<Boolean> {
        return Observable.create {
            if (clearCurrent) {
                PlayListInstance.getInstance().clearPlayList()
            }
            playItems?.let { list ->
                PlayListInstance.getInstance().updatePlayMode(if (isRandom) 1 else 0)
                PlayListInstance.getInstance().addPlayItems(list)
            }
            it.onNext(true)
            it.onComplete()
        }
    }

    fun playItem(item: PlayItemViewBean) {
        // 将视频url添加到临时播放列表的末尾
        PlayListInstance.getInstance().addPlayItemViewBean(item)
        PlayListInstance.getInstance().setPlayIndexAsLast()
        videoPlayOnReadyObserver.value = true
    }

    fun deleteItem(position: Int) {
        itemsObserver.value?.get(position)?.playItem?.let {
            getDatabase().getPlayOrderDao().deletePlayItem(it)
        }
    }

    fun getPlayUrl(position: Int, callback: UrlCallback) {
        itemsObserver.value?.get(position)?.let { bean ->
            val request = PathRequest()
            request.name = bean.record.bean.name
            request.path = bean.record.bean.directory
            loadingObserver.value = true
            AppHttpClient.getInstance().getAppService().getVideoPath(request)
                .flatMap { response -> UrlUtil.toVideoUrl(response) }
                .compose(applySchedulers())
                .subscribe(object : SimpleObserver<String>(getComposite()) {
                    override fun onNext(t: String) {
                        loadingObserver.value = false
                        bean.playUrl = t
                        callback.onReceiveUrl(t)
                    }

                    override fun onError(e: Throwable?) {
                        e?.printStackTrace()
                        loadingObserver.value = false
                        messageObserver.value = e?.message
                    }

                })
        }
    }

}