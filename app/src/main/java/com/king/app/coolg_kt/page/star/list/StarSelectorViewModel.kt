package com.king.app.coolg_kt.page.star.list

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.model.bean.SelectStar
import com.king.app.coolg_kt.model.image.ImageProvider
import com.king.app.coolg_kt.model.module.StarIndexEmitter
import com.king.app.coolg_kt.model.repository.StarRepository
import com.king.app.gdb.data.relation.StarWrap
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableSource
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import java.util.*

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/1/7 16:38
 */
class StarSelectorViewModel(application: Application): BaseViewModel(application) {

    var starsObserver: MutableLiveData<List<SelectStar>> = MutableLiveData()

    var indexObserver: MutableLiveData<String> = MutableLiveData()

    var indexBarObserver: MutableLiveData<Boolean> = MutableLiveData()

    private val indexEmitter = StarIndexEmitter()

    private var mSelectedStar: SelectStar? = null

    var bSingleSelect = false

    var mLimitMax = 0

    private val repository = StarRepository()

    private var originList = listOf<StarWrap>()

    fun loadStars() {
        loadingObserver.value = true
        repository.getAllStarsOrderByName()
            .flatMap {
                originList = it
                toViewItems(it)
            }
            .flatMap {
                starsObserver.postValue(it)
                createIndexes()
            }
            .compose(applySchedulers())
            .subscribe(object : Observer<String> {
                override fun onSubscribe(d: Disposable) {
                    addDisposable(d)
                }

                override fun onNext(index: String) {
                    indexObserver.value = index
                }

                override fun onComplete() {
                    loadingObserver.value = false
                    indexBarObserver.value = true
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                    loadingObserver.value = false
                    messageObserver.value = e?.message
                }
            })
    }

    private fun toViewItems(list: List<StarWrap>): ObservableSource<List<SelectStar>> {
        return ObservableSource {
            var result = mutableListOf<SelectStar>()
            list.forEach { star ->
                star.imagePath = ImageProvider.getStarRandomPath(star.bean.name, null)
                var bean = SelectStar()
                bean.star = star
                bean.observer = selectObserver
                result.add(bean)
            }
            it.onNext(result)
            it.onComplete()
        }
    }

    private fun createIndexes(): Observable<String> {
        return Observable.create {
            indexEmitter.clear()
            indexEmitter.createNameIndex(it, originList)
            it.onComplete()
        }
    }

    private var selectObserver = object : SelectObserver<SelectStar> {
        override fun onSelect(data: SelectStar) {
            onSelectStar(data)
        }
    }

    private fun onSelectStar(data: SelectStar) {
        when {
            bSingleSelect -> {
                mSelectedStar?.let {
                    it.isChecked = false
                }
                data.isChecked = true
                mSelectedStar = data
            }
            mLimitMax > 0 -> {
                var count = starsObserver.value?.filter { it.isChecked }?.size?:0
                val targetCheck: Boolean = !data.isChecked
                if (targetCheck && count >= mLimitMax) {
                    messageObserver.value = "You can select at most $mLimitMax"
                    return
                }
                data.isChecked = targetCheck
            }
            else -> {
                data.isChecked = !data.isChecked
            }
        }
    }

    fun getSelectedItems(): ArrayList<CharSequence> {
        var result = arrayListOf<CharSequence>()
        var list = starsObserver.value?.filter { it.isChecked }
        list?.forEach {
            result.add(it.star!!.bean.id!!.toString())
        }
        return result
    }

    fun getLetterPosition(letter: String?): Int {
        kotlin.runCatching {
            return indexEmitter.playerIndexMap[letter]?.start?:0
        }.let {
            return 0
        }
    }
}