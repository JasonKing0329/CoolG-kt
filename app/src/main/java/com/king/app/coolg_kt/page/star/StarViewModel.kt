package com.king.app.coolg_kt.page.star

import android.app.Application
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.conf.AppConstants
import com.king.app.coolg_kt.model.bean.RecordComplexFilter
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

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
    var relationList = mutableListOf<StarRelationship>()
    var tagList = listOf<Tag>()
    var mStudioId: Long = 0
    var mRelationshipId: Long = 0

    fun loadStar(starId: Long) {
        launchFlow(
            flow { emit(getDatabase().getStarDao().getStarWrap(starId)) }
                .map {
                    it?.apply {
                        mStar = this
                        starObserver.postValue(this)
                        toolbarText.set(bean.name)
                    }
                    loadStarImages(mStar.bean)
                }
                .map {
                    starImageList = it
                    getRelationships(mStar)
                }
                .map {
                    relationList = it
                    getStudioTagByStar(mStar)
                }
                .map {
                    studioList = it
                    getTags(mStar)
                }
                .map {
                    tagList = it
                    getComplexFilter()
                }
                .map {
                    recordRepository.getRecordsByFilter(it)
                }
                .map {
                    recordRepository.getRecordsImage(it)
                },
            withLoading = true
        ) {
            recordsObserver.value = it
        }
    }

    private fun getRelationships(star: StarWrap): MutableList<StarRelationship> {
        val relations = getDatabase().getStarDao().getStarRelationships(star.bean.id!!)
        relations.forEach { record ->
            record.imagePath = ImageProvider.getStarRandomPath(record.star.name, null)
        }
        relations.sortByDescending { ship -> ship.count }
        return relations
    }

    private fun getStudioTagByStar(star: StarWrap): List<StarStudioTag> {
        val studio = getDatabase().getFavorDao().getRecordOrderByName(AppConstants.ORDER_STUDIO_NAME)
        var list = listOf<StarStudioTag>()
        studio?.let { order ->
            list = getDatabase().getStarDao().getStarStudioTag(star.bean.id!!, order.id!!)
        }
        return list
    }

    private fun getComplexFilter(): RecordComplexFilter {
        val filter = RecordComplexFilter()
        filter.filter = mRecordFilter
        filter.desc = SettingProperty.isStarRecordsSortDesc()
        filter.sortType = SettingProperty.getStarRecordsSortType()
        filter.studioId = mStudioId
        filter.starId = mStar.bean.id!!
        filter.relationshipId = mRelationshipId
        if (AppConstants.KEY_SCENE_ALL != mScene) {
            filter.scene = mScene
        }
        return filter
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
        launchFlow(
            flow { emit(getComplexFilter()) }
                .map { recordRepository.getRecordsByFilter(it) }
                .map { recordRepository.getRecordsImage(it) },
            withLoading = false
        ) {
            onlyRecordsObserver.value = it
        }
    }

    fun loadStarOrders() {
        loadStar(mStar.bean.id!!)
    }

    fun loadStarOrders(starId: Long) {
        launchSingle(
            { orderRepository.getStarOrders(starId) },
            withLoading = false
        ) {
            ordersObserver.value = it
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
        launchSingle(
            { orderRepository.addFavorStar(orderId, mStar.bean.id!!) }
        ) {
            messageObserver.value = "Add successfully"
            addOrderObserver.value = it
        }
    }
}