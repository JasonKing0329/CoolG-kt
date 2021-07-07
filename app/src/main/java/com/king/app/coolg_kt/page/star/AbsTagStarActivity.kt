package com.king.app.coolg_kt.page.star

import android.content.DialogInterface
import android.graphics.Rect
import android.view.MenuItem
import android.view.View
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BaseActivity
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.conf.AppConstants
import com.king.app.coolg_kt.model.bean.LazyData
import com.king.app.coolg_kt.model.setting.SettingProperty
import com.king.app.coolg_kt.page.star.random.StarRandomActivity
import com.king.app.coolg_kt.utils.ScreenUtils
import com.king.app.coolg_kt.view.dialog.AlertDialogFragment
import com.king.app.gdb.data.entity.Tag
import com.king.app.gdb.data.entity.TagClass
import com.king.app.gdb.data.relation.StarWrap
import com.king.app.jactionbar.JActionbar

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2020/6/29 14:04
 */
abstract class AbsTagStarActivity<T : ViewDataBinding> : BaseActivity<T, TagStarViewModel>() {
    private var staggerAdapter = StarStaggerAdapter()
    private var gridAdapter = StarGridAdapter()

    override fun createViewModel(): TagStarViewModel {
        return generateViewModel(TagStarViewModel::class.java)
    }

    override fun initData() {
        mModel.tagsObserver.observe(this, Observer { tags -> showTags(tags) })
        mModel.tagClassesObserver.observe(this, Observer { tags -> showTagClasses(tags) })
        mModel.lazyLoadObserver.observe(this,
            Observer { params ->
                if (mModel.viewType == AppConstants.TAG_STAR_STAGGER) {
                    refreshStaggerLazyData(params)
                } else {
                    refreshGridLazyData(params)
                }
            }
        )
        mModel.starsObserver.observe(this,
            Observer { list ->
                if (mModel.viewType == AppConstants.TAG_STAR_STAGGER) {
                    showStaggerStars(list)
                } else {
                    showGridStars(list)
                }
            }
        )
        mModel.focusTagPosition.observe(this, Observer { position: Int -> focusOnTag(position) })
        mModel.loadHead()
    }

    var tagDecoration = object : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            outRect.left = ScreenUtils.dp2px(10f)
            outRect.top = ScreenUtils.dp2px(5f)
            outRect.bottom = ScreenUtils.dp2px(5f)
        }
    }

    protected fun defineStarList(view: RecyclerView, type: Int, column: Int) {
        mModel.setListViewType(type, column)
        if (type == AppConstants.TAG_STAR_STAGGER) {
            val starManager = StaggeredGridLayoutManager(column, StaggeredGridLayoutManager.VERTICAL)
            starManager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_NONE
            view.layoutManager = starManager
        } else {
            view.layoutManager = GridLayoutManager(this, column)
        }
    }

    protected fun initActionBar(actionbar: JActionbar) {
        actionbar.setOnBackListener { onBackPressed() }
        actionbar.setOnMenuItemListener { menuId: Int ->
            when (menuId) {
                R.id.menu_classic -> goToClassicPage()
                R.id.menu_tag_sort_mode -> setTagSortMode()
                R.id.menu_tag_random -> showRandomPage()
            }
        }
        actionbar.registerPopupMenuOn(
            R.id.menu_sort,
            R.menu.player_sort
        ) {  item: MenuItem ->
            when (item.itemId) {
                R.id.menu_sort_name -> {
                    mModel.sortList(AppConstants.STAR_SORT_NAME)
                }
                R.id.menu_sort_records -> {
                    mModel.sortList(AppConstants.STAR_SORT_RECORDS)
                }
                R.id.menu_sort_rating -> {
                    mModel.sortList(AppConstants.STAR_SORT_RATING)
                }
                R.id.menu_sort_rating_face -> {
                    mModel.sortList(AppConstants.STAR_SORT_RATING_FACE)
                }
                R.id.menu_sort_rating_body -> {
                    mModel.sortList(AppConstants.STAR_SORT_RATING_BODY)
                }
                R.id.menu_sort_rating_dk -> {
                    mModel.sortList(AppConstants.STAR_SORT_RATING_DK)
                }
                R.id.menu_sort_rating_sexuality -> {
                    mModel.sortList(AppConstants.STAR_SORT_RATING_SEXUALITY)
                }
                R.id.menu_sort_rating_passion -> {
                    mModel.sortList(AppConstants.STAR_SORT_RATING_PASSION)
                }
                R.id.menu_sort_rating_video -> {
                    mModel.sortList(AppConstants.STAR_SORT_RATING_VIDEO)
                }
                R.id.menu_sort_rating_prefer -> {
                    mModel.sortList(AppConstants.STAR_SORT_RATING_PREFER)
                }
                R.id.menu_sort_random -> {
                    mModel.sortList(AppConstants.STAR_SORT_RANDOM)
                }
            }
            false
        }
    }

    private fun showRandomPage() {
        StarRandomActivity.startPage(this)
    }

    private fun setTagSortMode() {
        AlertDialogFragment()
            .setTitle(null)
            .setItems(AppConstants.TAG_SORT_MODE_TEXT) { dialog: DialogInterface?, which: Int ->
                SettingProperty.setTagSortType(which)
                mModel.onTagSortChanged()
                mModel.startSortTag(false)
            }.show(supportFragmentManager, "AlertDialogFragment")
    }

    protected abstract val starRecyclerView: RecyclerView
    protected abstract fun showTags(tags: List<Tag>)
    protected abstract fun showTagClasses(tags: List<TagClass>)
    protected abstract fun focusOnTag(position: Int)
    protected abstract fun goToClassicPage()
    protected abstract fun goToStarPage(starId: Long)

    private val onStarRatingListener = object : OnStarRatingListener{
        override fun onUpdateRating(position: Int, starId: Long) {
            showRatingDialog(position, starId)
        }
    }
    private val onItemClickListener = object : BaseBindingAdapter.OnItemClickListener<StarWrap> {
        override fun onClickItem(view: View, position: Int, data: StarWrap) {
            goToStarPage(data.bean.id!!)
        }
    }
    private fun showGridStars(list: List<StarWrap>) {
        if (starRecyclerView.adapter == null) {
            gridAdapter.onStarRatingListener = onStarRatingListener
            gridAdapter.setOnItemClickListener(onItemClickListener)
            gridAdapter.list = list
            starRecyclerView.adapter = gridAdapter
        } else {
            gridAdapter.list = list
            gridAdapter.notifyDataSetChanged()
        }
    }

    private fun refreshGridLazyData(params: LazyData<StarWrap>) {
        gridAdapter.notifyItemRangeChanged(params.start, params.count)
    }

    private fun showStaggerStars(list: List<StarWrap>) {
        if (starRecyclerView.adapter == null) {
            staggerAdapter.list = list
            staggerAdapter.onStarRatingListener = onStarRatingListener
            staggerAdapter.setOnItemClickListener(onItemClickListener)
            starRecyclerView.adapter = staggerAdapter
        } else {
            staggerAdapter.list = list
            staggerAdapter.notifyDataSetChanged()
        }
    }

    private fun refreshStaggerLazyData(params: LazyData<StarWrap>) {
        staggerAdapter.notifyItemRangeChanged(params.start, params.count)
    }

    private fun showRatingDialog(position: Int, starId: Long) {
        val dialog = StarRatingDialog()
        dialog.starId = starId
        dialog.onDismissListener = DialogInterface.OnDismissListener{ staggerAdapter.notifyItemChanged(position) }
        dialog.show(supportFragmentManager, "StarRatingDialog")
    }
}