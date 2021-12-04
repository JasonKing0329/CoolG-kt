package com.king.app.coolg_kt.page.image

import android.app.Application
import android.graphics.BitmapFactory
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.model.bean.ImageBean
import com.king.app.coolg_kt.model.http.observer.SimpleObserver
import com.king.app.coolg_kt.model.image.ImageProvider
import com.king.app.coolg_kt.utils.FileUtil
import com.king.app.coolg_kt.utils.ScreenUtils
import io.reactivex.rxjava3.core.Observable
import java.io.File

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2020/8/4 9:40
 */
class ImageViewModel(application: Application) : BaseViewModel(application) {

    var imageList = MutableLiveData<MutableList<ImageBean>>()

    private val mStaggerImageWidth: Int
    var titleText = ObservableField<String>()
    private var mUrlToSetCover: String? = null

    fun loadStarImages(starId: Long) {
        val star = getDatabase().getStarDao().getStar(starId)
        titleText.set(star?.name)
        val list = ImageProvider.getStarPathList(star?.name)
        convertToImages(list)
    }

    fun loadRecordImages(recordId: Long) {
        val record = getDatabase().getRecordDao().getRecord(recordId)
        record?.let {
            titleText.set(it.bean.name)
            val list = ImageProvider.getRecordPathList(it.bean.name)
            convertToImages(list)
        }
    }

    private fun convertToImages(list: List<String>) {
        toImageBean(list, true)
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<MutableList<ImageBean>>(getComposite()) {
                override fun onNext(t: MutableList<ImageBean>) {
                    imageList.value = t
                }
                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                }
            })
    }

    private fun toImageBean(list: List<String>, loadSize: Boolean): Observable<MutableList<ImageBean>> {
        return Observable.create { e ->
            val result = mutableListOf<ImageBean>()
            for (path in list!!) {
                val bean = ImageBean()
                bean.url = path
                if (loadSize) {
                    calcImageSize(bean)
                }
                result.add(bean)
            }
            e.onNext(result)
            e.onComplete()
        }
    }

    private fun calcImageSize(bean: ImageBean) {
        // 无图按16:9
        //缩放图片的实际宽高
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(bean.url, options)
        var height = options.outHeight
        val width = options.outWidth
        val ratio = mStaggerImageWidth.toFloat() / width.toFloat()
        bean.width = mStaggerImageWidth
        height = (height * ratio).toInt()
        bean.height = height
    }

    fun onSelectAll(select: Boolean) {
        imageList.value?.let {
            it.forEach { bean ->
                bean.isSelected = select
            }
        }
    }

    fun deleteSelectedItems() {
        imageList.value?.let {
            val delList: MutableList<ImageBean> =
                ArrayList()
            for (bean in it) {
                if (bean.isSelected) {
                    FileUtil.deleteFile(File(bean.url))
                    delList.add(bean)
                }
            }
            for (delBean in delList) {
                it.remove(delBean)
            }
        }
    }

    fun setUrlToSetCover(path: String?) {
        mUrlToSetCover = path
    }

    fun setPlayOrderCover(list: ArrayList<CharSequence>?) {
        savePlayOrderCover(list)
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<Boolean>(getComposite()) {
                override fun onNext(aBoolean: Boolean) {
                    messageObserver.value = "Set successfully"
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                    messageObserver.value = e?.message
                }
            })
    }

    private fun savePlayOrderCover(list: ArrayList<CharSequence>?): Observable<Boolean> {
        return Observable.create { e ->
            list?.let { list ->
                for (sequence in list) {
                    val orderId = sequence.toString().toLong()
                    val order = getDatabase().getPlayOrderDao().getPlayOrder(orderId)
                    order?.let {
                        it.coverUrl = mUrlToSetCover
                        getDatabase().getPlayOrderDao().updatePlayOrder(it)
                    }
                }
            }
            e.onNext(true)
        }
    }

    fun setMatchCover(matchId: Long) {
        val match = getDatabase().getMatchDao().getMatch(matchId)
        match.imgUrl = mUrlToSetCover!!
        getDatabase().getMatchDao().updateMatch(match)
    }

    fun setStudioCover(studioId: Long) {
        getDatabase().getFavorDao().getFavorRecordOrderBy(studioId)?.apply {
            coverUrl = mUrlToSetCover!!
            getDatabase().getFavorDao().updateFavorRecordOrder(this)
        }
    }

    init {
        val margin = ScreenUtils.dp2px(1f)
        val column = 2
        mStaggerImageWidth = ScreenUtils.getScreenWidth() / column - margin
    }
}