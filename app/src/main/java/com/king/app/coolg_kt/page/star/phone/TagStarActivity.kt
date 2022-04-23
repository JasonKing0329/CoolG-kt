package com.king.app.coolg_kt.page.star.phone

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.conf.AppConstants
import com.king.app.coolg_kt.databinding.ActivityStarTagBinding
import com.king.app.coolg_kt.page.pub.TagAdapter
import com.king.app.coolg_kt.page.record.phone.TagClassAdapter
import com.king.app.coolg_kt.page.star.AbsTagStarActivity
import com.king.app.coolg_kt.page.star.TagStarViewModel
import com.king.app.coolg_kt.page.star.timeline.StarTimelineActivity
import com.king.app.coolg_kt.utils.ScreenUtils
import com.king.app.gdb.data.entity.Tag
import com.king.app.gdb.data.entity.TagClass

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2020/6/29 14:04
 */
class TagStarActivity : AbsTagStarActivity<ActivityStarTagBinding>() {

    companion object {
        fun startPage(context: Context) {
            var intent = Intent(context, TagStarActivity::class.java)
            context.startActivity(intent)
        }
    }

    private var tagAdapter = TagAdapter()

    var tagClassAdapter = TagClassAdapter()

    override fun getContentView(): Int = R.layout.activity_star_tag

    override fun createViewModel(): TagStarViewModel = generateViewModel(TagStarViewModel::class.java)

    override fun initView() {
        super.initActionBar(mBinding.actionbar)
        mBinding.rvTagClass.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        mBinding.rvTagClass.addItemDecoration(object : RecyclerView.ItemDecoration() {
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
        })
        tagClassAdapter.setOnItemClickListener(object : BaseBindingAdapter.OnItemClickListener<TagClass>{
            override fun onClickItem(view: View, position: Int, data: TagClass) {
                mModel.mCurTagClassId = data.id
                mModel.loadTags()
            }
        })
        mBinding.rvTagClass.adapter = tagClassAdapter

//        defineStarList(mBinding.rvStars, AppConstants.TAG_STAR_GRID, 2);// grid type
        defineStarList(mBinding.rvStars, AppConstants.TAG_STAR_STAGGER, 2) // stagger type
        mBinding.fabTop.setOnClickListener { mBinding.rvStars.scrollToPosition(0) }
    }

    override val starRecyclerView: RecyclerView
        get() = mBinding.rvStars

    override fun focusOnTag(position: Int) {
        tagAdapter.selection = position
        tagAdapter.notifyDataSetChanged()
    }

    override fun showTags(tags: List<Tag>) {
        if (mBinding.rvTags.adapter == null) {
            tagAdapter.selection = 0
            tagAdapter.setOnItemClickListener(object : BaseBindingAdapter.OnItemClickListener<Tag> {
                override fun onClickItem(view: View, position: Int, data: Tag) {
                    mModel.loadTagStars(data.id)
                }
            })
            tagAdapter.list = tags
            decorateTagList(tags)
            mBinding.rvTags.adapter = tagAdapter
        } else {
            tagAdapter.list = tags
            decorateTagList(tags)
            tagAdapter.notifyDataSetChanged()
        }
    }

    fun decorateTagList(tags: List<Tag>) {
        val spanCount = when {
            tags.size < 5 -> 1
            tags.size < 20 -> 2
            else -> 3
        }
        mBinding.rvTags.removeItemDecoration(tagDecoration)
        val manager = StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.HORIZONTAL)
        mBinding.rvTags.layoutManager = manager
        mBinding.rvTags.addItemDecoration(tagDecoration)
    }

    override fun showTagClasses(tags: List<TagClass>) {
        tagClassAdapter.list = tags
        tagClassAdapter.notifyDataSetChanged()
    }
    override fun goToClassicPage() {
        StarsPhoneActivity.startPage(this)
    }

    override fun goToTimelinePage() {
        StarTimelineActivity.startPage(this)
    }

    override fun goToRelationPage() {
        StarRelationActivity.startPage(this)
    }

    override fun goToStarPage(starId: Long) {
        StarActivity.startPage(this, starId)
    }
}