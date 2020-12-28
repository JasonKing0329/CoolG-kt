package com.king.app.coolg_kt.page.star

import android.app.Application
import androidx.databinding.ObservableInt
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.CoolApplication
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.conf.AppConstants
import com.king.app.coolg_kt.model.bean.LazyData
import com.king.app.coolg_kt.model.bean.StarBuilder
import com.king.app.coolg_kt.model.bean.StarDetailBuilder
import com.king.app.coolg_kt.model.http.observer.SimpleObserver
import com.king.app.coolg_kt.model.module.StarIndexEmitter
import com.king.app.coolg_kt.model.repository.StarRepository
import com.king.app.coolg_kt.model.repository.TagRepository
import com.king.app.coolg_kt.model.setting.SettingProperty
import com.king.app.coolg_kt.utils.ScreenUtils
import com.king.app.gdb.data.DataConstants
import com.king.app.gdb.data.entity.Tag
import com.king.app.gdb.data.relation.StarWrap

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2020/6/29 13:19
 */
class TagStarViewModel(application: Application) : BaseViewModel(application) {
    var tagsObserver = MutableLiveData<List<Tag>>()
    var starsObserver = MutableLiveData<List<StarWrap>>()
    var lazyLoadObserver = MutableLiveData<LazyData<StarWrap>>()
    var focusTagPosition = MutableLiveData<Int>()
    private var mStarList: List<StarWrap> = listOf()
    private var mTagId: Long? = null
    private var mTagSortType = SettingProperty.getTagSortType()
    private var dataTagList: List<Tag> = listOf()
    private val tagRepository = TagRepository()
    private val starRepository = StarRepository()

    // see AppConstants.STAR_SORT_XXX
    var mSortType = AppConstants.STAR_SORT_RATING
    /**
     * 瀑布流模式下item宽度
     */
    private var mStaggerColWidth = 0
    var viewColumn = 0
    // see AppConstants.TAG_STAR_XXX
    var viewType = 0

    var indexBarVisibility = ObservableInt()
    var indexEmitter = StarIndexEmitter()

    fun setListViewType(type: Int, column: Int) {
        viewType = type
        viewColumn = column
        if (viewType == AppConstants.TAG_STAR_STAGGER) {
            val margin = ScreenUtils.dp2px(1f)
            mStaggerColWidth = ScreenUtils.getScreenWidth() / column - margin
            if (ScreenUtils.isTablet()) {
                val extra: Int = getApplication<CoolApplication>().resources
                    .getDimensionPixelSize(R.dimen.tag_star_pad_tag_width)
                mStaggerColWidth = (ScreenUtils.getScreenWidth() - extra) / column - margin
            }
        }
    }

    fun loadTags() {
        dataTagList = tagRepository.loadTags(DataConstants.TAG_TYPE_STAR)
        startSortTag(true)
    }

    fun startSortTag(loadAll: Boolean) {
        tagRepository.sortTags(mTagSortType, dataTagList)
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<List<Tag>>(getComposite()) {

                override fun onNext(tagList: List<Tag>) {
                    val allList = addTagAll(tagList)
                    tagsObserver.value = allList
                    if (loadAll) {
                        loadTagStars(allList[0].id)
                    } else {
                        focusToCurrentTag(allList)
                    }
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                    messageObserver.setValue(e?.message)
                }
            })
    }

    private fun focusToCurrentTag(allList: List<Tag>) {
        for (i in allList.indices) {
            if (mTagId === allList[i].id) {
                focusTagPosition.setValue(i)
                break
            }
        }
    }

    private fun addTagAll(tagList: List<Tag>): List<Tag> {
        val tags: MutableList<Tag> = mutableListOf()
        val all = Tag(null, "All")
        tags.add(all)
        tags.addAll(tagList)
        return tags
    }

    private fun loadTagStars() {
        loadTagStars(mTagId)
    }

    fun loadTagStars(tagId: Long?) {
        mTagId = tagId
        val builder = StarBuilder()
            .setTagId(tagId)
            .setSortType(mSortType)
        val detailBuilder = StarDetailBuilder()
            .setLoadImagePath(true)
            .setLoadRating(true)
            .setLoadImageSize(viewType == AppConstants.TAG_STAR_STAGGER, mStaggerColWidth)
        starRepository.queryStarsBy(builder)
            .flatMap { list -> starRepository.lazyLoad(list, 30, detailBuilder) }
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<LazyData<StarWrap>>(getComposite()) {
                override fun onNext(params: LazyData<StarWrap>) {
                    mStarList = params.list
                    // 第一批加载完的开始显示
                    if (params.start === 0) {
                        starsObserver.setValue(mStarList)
                    } else {
                        lazyLoadObserver.setValue(params)
                    }
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                    messageObserver.setValue("Load records error: " + e?.message)
                }
            })
    }

    fun sortList(sortType: Int) {
        if (sortType == mSortType && sortType != AppConstants.STAR_SORT_RANDOM) {
            return
        }
        mSortType = sortType
        loadTagStars()
    }

    fun onTagSortChanged() {
        mTagSortType = SettingProperty.getTagSortType()
    }

}