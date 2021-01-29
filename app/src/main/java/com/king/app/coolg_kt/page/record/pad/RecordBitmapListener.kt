package com.king.app.coolg_kt.page.record.pad

import android.graphics.Bitmap
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.palette.graphics.Palette
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.king.app.coolg_kt.model.palette.BitmapRepository
import com.king.app.coolg_kt.model.palette.ViewColorBound
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers

/**
 * Desc: integration of BitmapPaletteListener and TargetViewListener
 * Glide.xxx.listener().listener()只有最后一个会收到回调，因此用这个listener来处理两个listener处理的内容
 *
 * @author：Jing Yang
 * @date: 2018/8/2 15:53
 */
abstract class RecordBitmapListener(
    private val viewList: List<View>,
    lifecycle: Lifecycle
) : RequestListener<Bitmap?>, LifecycleObserver {

    init {
        lifecycle.addObserver(this)
    }

    private var disposable: Disposable? = null

    override fun onLoadFailed(
        e: GlideException?,
        model: Any,
        target: Target<Bitmap?>,
        isFirstResource: Boolean
    ): Boolean {
        return false
    }

    override fun onResourceReady(
        resource: Bitmap?,
        model: Any,
        target: Target<Bitmap?>,
        dataSource: DataSource,
        isFirstResource: Boolean
    ): Boolean {
        val repository = BitmapRepository()
        repository.createViewColorBound(viewList, resource)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe(object :
                Observer<List<ViewColorBound>?> {
                override fun onSubscribe(d: Disposable) {
                    disposable = d
                }

                override fun onNext(bounds: List<ViewColorBound>?) {
                    onBoundsCreated(bounds)
                }

                override fun onError(e: Throwable) {
                    e.printStackTrace()
                }

                override fun onComplete() {}
            })
        repository.createPalette(resource)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe(object : Observer<Palette?> {
                override fun onSubscribe(d: Disposable) {
                    disposable = d
                }

                override fun onNext(palette: Palette?) {
                    onPaletteCreated(palette)
                }

                override fun onError(e: Throwable) {
                    e.printStackTrace()
                }

                override fun onComplete() {}
            })
        return false
    }

    protected abstract fun onPaletteCreated(palette: Palette?)
    protected abstract fun onBoundsCreated(bounds: List<ViewColorBound>?)

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onLifeDestroy() {
        disposable?.dispose()
    }
}