package com.king.app.coolg_kt.page.star

import android.app.Application
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.conf.AppConstants
import com.king.app.coolg_kt.model.bean.RecordComplexFilter
import com.king.app.coolg_kt.model.http.observer.SimpleObserver
import com.king.app.coolg_kt.model.image.ImageProvider
import com.king.app.coolg_kt.model.repository.OrderRepository
import com.king.app.coolg_kt.model.repository.RecordRepository
import com.king.app.coolg_kt.model.repository.StarRepository
import com.king.app.coolg_kt.model.setting.SettingProperty
import com.king.app.coolg_kt.page.record.popup.RecommendBean
import com.king.app.gdb.data.entity.*
import com.king.app.gdb.data.relation.RecordWrap
import com.king.app.gdb.data.relation.StarRelationship
import com.king.app.gdb.data.relation.StarStudioTag
import com.king.app.gdb.data.relation.StarWrap
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableSource

/**
 * @description:
 * @author：Jing
 * @date: 2020/12/20 12:17
 */
class StarViewModel(application: Application): BaseViewModel(application) {

    var starObserver = MutableLiveData<StarWrap>()
    var ordersObserver = MutableLiveData<List<FavorStarOrder>>()
    var recordsObserver = MutableLiveData<List<RecordWrap>>()
    var onlyRecordsObserver = MutableLiveData<List<RecordWrap>>()
    var tagsObserver = MutableLiveData<List<Tag>>()
    var addOrderObserver = MutableLiveData<FavorStar>()

    var toolbarText = ObservableField<String>()

    val starRepository = StarRepository()
    val orderRepository = OrderRepository()
    val recordRepository = RecordRepository()

    var mScene = AppConstants.KEY_SCENE_ALL
    var mRecordFilter: RecommendBean? = null

    lateinit var mStar: StarWrap
    private var mSingleImagePath: String? = null
    var starImageList = listOf<String>()
    var studioList = listOf<StarStudioTag>()
    var relationList = listOf<StarRelationship>()
    var tagList = listOf<Tag>()
    var mStudioId: Long = 0

    fun loadStar(starId: Long) {
        loadingObserver.value = true
        starRepository.getStar(starId)
            .flatMap {
                mStar = it
                starObserver.postValue(it)
                toolbarText.set(it.bean.name)
                getStarImages(mStar.bean)
            }
            .flatMap {
                starImageList = it
                getRelationships(mStar)
            }
            .flatMap {
                relationList = it
                getStudioTagByStar(mStar)
            }
            .flatMap {
                studioList = it
                getStarTags()
            }
            .flatMap {
                tagList = it
                getComplexFilter()
            }
            .flatMap { recordRepository.getRecords(it) }
            .flatMap { recordRepository.getRecordsImage(it) }
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<List<RecordWrap>>(getComposite()) {
                override fun onNext(t: List<RecordWrap>) {
                    loadingObserver.value = false
                    recordsObserver.value = t
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                    loadingObserver.value = false
                    messageObserver.value = e?.message
                }

            })
    }

    private fun getStarImages(star: Star): Observable<List<String>> {
        return Observable.create {
            it.onNext(loadStarImages(star))
            it.onComplete()
        }
    }

    private fun getRelationships(star: StarWrap): ObservableSource<List<StarRelationship>> {
        return ObservableSource {
            val relations = getDatabase().getStarDao().getStarRelationships(star.bean.id!!)
            relations.forEach { record ->
                record.imagePath = ImageProvider.getStarRandomPath(record.star.name, null)
            }
            var list = relations.sortedByDescending { ship -> ship.count }
            it.onNext(list)
            it.onComplete()
        }
    }

    private fun getStudioTagByStar(star: StarWrap): ObservableSource<List<StarStudioTag>> {
        return ObservableSource {
            val studio = getDatabase().getFavorDao().getRecordOrderByName(AppConstants.ORDER_STUDIO_NAME)
            var list = listOf<StarStudioTag>()
            studio?.let { order ->
                list = getDatabase().getStarDao().getStarStudioTag(star.bean.id!!, order.id!!)
            }
            it.onNext(list)
            it.onComplete()
        }
    }

    private fun getComplexFilter(): Observable<RecordComplexFilter> {
        return Observable.create {
            val filter = RecordComplexFilter()
            filter.filter = mRecordFilter
            filter.desc = SettingProperty.isStarRecordsSortDesc()
            filter.sortType = SettingProperty.getStarRecordsSortType()
            filter.studioId = mStudioId
            filter.starId = mStar.bean.id!!
            if (AppConstants.KEY_SCENE_ALL != mScene) {
                filter.scene = mScene
            }
            it.onNext(filter)
            it.onComplete()
        }
    }

    private fun loadStarImages(star: Star): List<String> {
        var list = mutableListOf<String>()
        if (ImageProvider.hasStarFolder(star.name)) {
            list.addAll(ImageProvider.getStarPathList(star.name))
            if (list.size == 1) {
                mSingleImagePath = list[0]
            }
        } else {
            val path = ImageProvider.getStarRandomPath(star.name, null)
            mSingleImagePath = path
            path?.let {
                list.add(it)
            }
        }
        return list
    }

    fun loadStarRecords() {
        getComplexFilter()
            .flatMap { recordRepository.getRecords(it) }
            .flatMap { recordRepository.getRecordsImage(it) }
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<List<RecordWrap>>(getComposite()) {
                override fun onNext(t: List<RecordWrap>) {
                    loadingObserver.value = false
                    onlyRecordsObserver.value = t
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                    loadingObserver.value = false
                    messageObserver.value = e?.message
                }

            })
    }

    fun loadStarOrders() {
        loadStar(mStar.bean.id!!)
    }

    fun loadStarOrders(starId: Long) {
        orderRepository.getStarOrders(starId)
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<List<FavorStarOrder>>(getComposite()) {
                override fun onNext(t: List<FavorStarOrder>) {
                    ordersObserver.value = t
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                }

            })
    }

    fun getStarTags(): ObservableSource<List<Tag>> {
        return ObservableSource {
            it.onNext(getTags(mStar))
            it.onComplete()
        }
    }

    fun deleteOrderOfStar(orderId: Long, starId: Long) {
        getDatabase().getFavorDao().deleteStarFromOrder(starId, orderId)
    }

    fun addTag(tagId: Long) {
        var count = getDatabase().getTagDao().countStarTag(mStar.bean.id!!, tagId)
        if (count == 0) {
            var list = mutableListOf<TagStar>()
            list.add(TagStar(null, tagId, mStar.bean.id!!))
            getDatabase().getTagDao().insertTagStars(list)
            refreshTags()
        }
    }

    fun deleteTag(bean: Tag) {
        getDatabase().getTagDao().deleteTagStarBy(bean.id!!, mStar.bean.id!!)
        refreshTags()
    }

    fun refreshTags() {
        tagsObserver.postValue(getTags(mStar))
    }

    private fun getTags(star: StarWrap): List<Tag> {
        return getDatabase().getTagDao().getStarTags(star.bean.id!!)
    }

    fun addToOrder(orderId: Long) {
        orderRepository.addFavorStar(orderId, mStar.bean.id!!)
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<FavorStar>(getComposite()) {
                override fun onNext(t: FavorStar) {
                    messageObserver.value = "Add successfully"
                    addOrderObserver.value = t
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                    messageObserver.value = e?.message
                }

            })
    }
}