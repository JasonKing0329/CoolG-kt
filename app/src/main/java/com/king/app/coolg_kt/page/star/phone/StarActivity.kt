package com.king.app.coolg_kt.page.star.phone

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BaseActivity
import com.king.app.coolg_kt.databinding.ActivityStarPhoneBinding
import com.king.app.coolg_kt.model.extension.ImageBindingAdapter
import com.king.app.coolg_kt.model.image.ImageProvider
import com.king.app.coolg_kt.model.setting.SettingProperty
import com.king.app.coolg_kt.model.setting.ViewProperty
import com.king.app.coolg_kt.page.image.ImageManagerActivity
import com.king.app.coolg_kt.page.pub.BannerSettingFragment
import com.king.app.coolg_kt.page.pub.TagManagerActivity
import com.king.app.coolg_kt.page.record.phone.RecordActivity
import com.king.app.coolg_kt.page.record.popup.RecommendBean
import com.king.app.coolg_kt.page.record.popup.RecommendFragment
import com.king.app.coolg_kt.page.record.popup.SortDialogContent
import com.king.app.coolg_kt.page.star.StarViewModel
import com.king.app.coolg_kt.page.star.phone.StarAdapter.OnListListener
import com.king.app.coolg_kt.page.star.phone.StarHeader.OnHeadActionListener
import com.king.app.coolg_kt.utils.BannerHelper
import com.king.app.coolg_kt.utils.ScreenUtils
import com.king.app.coolg_kt.view.dialog.DraggableDialogFragment
import com.king.app.gdb.data.DataConstants
import com.king.app.gdb.data.entity.Star
import com.king.app.gdb.data.entity.Tag
import com.king.app.gdb.data.relation.RecordWrap
import com.king.app.gdb.data.relation.StarRelationship
import com.king.app.gdb.data.relation.StarWrap

/**
 * Created by Administrator on 2018/8/12 0012.
 */
class StarActivity : BaseActivity<ActivityStarPhoneBinding, StarViewModel>() {

    companion object {
        const val EXTRA_STAR_ID = "key_star_id"
        fun startPage(context: Context, starId: Long) {
            var intent = Intent(context, StarActivity::class.java)
            intent.putExtra(EXTRA_STAR_ID, starId)
            context.startActivity(intent)
        }
    }

    private val REQUEST_ADD_ORDER = 1602
    private val REQUEST_ADD_TAG = 1603

    private var adapter = StarAdapter()
    private var mFilter: RecommendBean? = null

    override fun getContentView(): Int = R.layout.activity_star_phone

    override fun initView() {
        immersiveTopDarkFont(mBinding.toolbar)

        mBinding.actionbar.setOnBackListener { finish() }
        val manager = GridLayoutManager(this, 2)
        manager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return adapter.getSpanSize(position)
            }
        }
        mBinding.rvList.layoutManager = manager
        mBinding.rvList.addItemDecoration(object : ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                val position = parent.getChildAdapterPosition(view)
                // items 与 header之间的间隔
                if (position in 1..2) {
                    outRect.top = ScreenUtils.dp2px(10f)
                }
            }
        })

        mBinding.actionbar.setOnMenuItemListener { menuId: Int ->
            when (menuId) {
                R.id.menu_banner_setting -> showSettings()
                R.id.menu_sort -> changeSortType()
                R.id.menu_filter -> changeFilter()
            }
        }
        mBinding.ivMore.setOnClickListener {
            var intent = Intent(this@StarActivity, ImageManagerActivity::class.java)
            intent.putExtra(ImageManagerActivity.EXTRA_TYPE, ImageManagerActivity.TYPE_STAR)
            intent.putExtra(ImageManagerActivity.EXTRA_DATA, mModel.mStar.bean.id!!)
            startActivity(intent)
        }
        initAdapters()
    }

    private fun initAdapters() {
        adapter.onListListener = object : OnListListener {
            override fun onClickItem(view: View, record: RecordWrap) {
                goToRecordPage(record.bean.id!!)
            }
        }
        adapter.onHeadActionListener = object : OnHeadActionListener {
            override fun onClickRelationStar(relationship: StarRelationship) {
                goToStarPage(relationship.star.id!!)
            }

            override fun addStarToOrder(star: Star) {
                selectOrderToAddStar()
            }

            override fun onFilterStudio(studioId: Long) {
                mModel.mStudioId = studioId
                mModel.loadStarRecords()
            }

            override fun onCancelFilterStudio(studioId: Long) {
                // all records
                mModel.mStudioId = 0
                mModel.loadStarRecords()
            }

            override fun onAddTag() {
                addTag()
            }

            override fun onDeleteTag(bean: Tag) {
                mModel.deleteTag(bean)
            }
        }
    }

    override fun createViewModel(): StarViewModel = generateViewModel(StarViewModel::class.java)

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        mModel.loadStar(getIntent().getLongExtra(EXTRA_STAR_ID, -1))
    }
    override fun initData() {
        mModel.starObserver.observe(this, Observer{ star -> showStar(star) })
        mModel.recordsObserver.observe(this, Observer{ list -> showRecords(list) })
        mModel.addOrderObserver.observe(this, Observer{ mModel.loadStarOrders() })
        mModel.onlyRecordsObserver.observe(this, Observer{ list ->
            val oldCount = adapter.itemCount - 2
            // size发生变化（变少）用notifyItemRangeChanged会抛出error导致崩溃，必须全部刷新
            if (oldCount != list.size) {
                adapter.list = list
                adapter.notifyDataSetChanged()
            } else {
                adapter.list = list
                adapter.notifyItemRangeChanged(1, adapter!!.itemCount - 2)
            }
        })
        mModel.tagsObserver.observe(this, Observer{ tags ->
            adapter.mTagList = tags
            adapter.notifyDataSetChanged()
        })
        mModel.loadStar(starId)
    }

    private val starId: Long
        private get() = intent.getLongExtra(EXTRA_STAR_ID, -1)

    private fun showStar(star: StarWrap) {
        mBinding.actionbar.setTitle(star.bean.name)
        mBinding.tvVideo.text = star.bean.records.toString() + "个视频文件"
        val buffer = StringBuffer()
        if (star.bean.betop > 0) {
            buffer.append(star.bean.betop).append(" Top")
        }
        if (star.bean.bebottom > 0) {
            if (buffer.toString().isNotEmpty()) {
                buffer.append(", ")
            }
            buffer.append(star.bean.bebottom).append(" Bottom")
        }
        mBinding.tvTb.text = buffer.toString()
        star.countStar?.let {
            mBinding.tvRank.text = "R-${it.rank}"
        }
        ImageBindingAdapter.setStarUrl(mBinding.ivStar, ImageProvider.getStarRandomPath(star.bean.name, null))
    }

    private fun showRecords(list: List<RecordWrap>) {
        adapter.star = mModel.mStar.bean
        adapter.mRelationships = mModel.relationList
        adapter.mStudioList = mModel.studioList
        adapter.mTagList = mModel.tagList
        adapter.mSortMode = SettingProperty.getStarRecordsSortType()
        adapter.list = list
        if (mBinding.rvList.adapter == null) {
            mBinding.rvList.adapter = adapter
        }
        else {
            adapter.notifyDataSetChanged()
        }
    }

    private fun addTag() {
        TagManagerActivity.startPage(this, REQUEST_ADD_TAG, DataConstants.TAG_TYPE_STAR)
    }

    private fun showSettings() {
        val content = BannerSettingFragment()
        content.params = ViewProperty.getStarBannerParams()
        content.onAnimSettingListener = object : BannerSettingFragment.OnAnimSettingListener {
            override fun onParamsUpdated(params: BannerHelper.BannerParams) {}
            override fun onParamsSaved(params: BannerHelper.BannerParams) {
                ViewProperty.setStarBannerParams(params)
                // 只刷新头部
                adapter.notifyItemChanged(0)
            }
        }
        val dialogFragment = DraggableDialogFragment()
        dialogFragment.contentFragment = content
        dialogFragment.setTitle("Banner Setting")
        dialogFragment.show(supportFragmentManager, "BannerSettingFragment")
    }

    private fun changeSortType() {
        val content = SortDialogContent()
        content.mDesc = SettingProperty.isStarRecordsSortDesc()
        content.mSortType = SettingProperty.getStarRecordsSortType()
        content.onSortListener = object : SortDialogContent.OnSortListener{
            override fun onSort(desc: Boolean, sortMode: Int) {
                SettingProperty.setStarRecordsSortType(sortMode)
                SettingProperty.setStarRecordsSortDesc(desc)
                adapter.mSortMode = sortMode
                mModel.loadStarRecords()
            }
        }
        val dialogFragment = DraggableDialogFragment()
        dialogFragment.contentFragment = content
        dialogFragment.setTitle("Sort")
        dialogFragment.show(supportFragmentManager, "SortDialogContent")
    }

    private fun changeFilter() {
        val content = RecommendFragment()
        mFilter?.let {
            content.mBean = it
        }
        content.onRecommendListener = object : RecommendFragment.OnRecommendListener{
            override fun onSetSql(bean: RecommendBean) {
                mFilter = bean
                mModel.mRecordFilter = bean
                mModel.loadStarRecords()
            }

        }
        val dialogFragment = DraggableDialogFragment()
        dialogFragment.setTitle("Recommend Setting")
        dialogFragment.contentFragment = content
        dialogFragment.maxHeight = ScreenUtils.getScreenHeight() * 2 / 3
        dialogFragment.show(supportFragmentManager, "RecommendFragment")
    }

    private fun goToRecordPage(recordId: Long) {
        RecordActivity.startPage(this, recordId)
    }

    private fun goToStarPage(starId: Long) {
        var intent = Intent(this, StarActivity::class.java)
        intent.putExtra(EXTRA_STAR_ID, starId)
        startActivity(intent)
    }

    private fun selectOrderToAddStar() {
//        Router.build("OrderPhone")
//            .with(OrderPhoneActivity.EXTRA_SELECT_MODE, true)
//            .with(OrderPhoneActivity.EXTRA_SELECT_STAR, true)
//            .requestCode(REQUEST_ADD_ORDER)
//            .go(this)
        TODO()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            REQUEST_ADD_ORDER -> {
                if (resultCode == Activity.RESULT_OK) {
//                val orderId = data!!.getLongExtra(AppConstants.RESP_ORDER_ID, -1)
//                mModel.addToOrder(orderId)
                    TODO()
                }
            }
            REQUEST_ADD_TAG -> {
                if (resultCode == Activity.RESULT_OK) {
                    val tagId = data?.getLongExtra(TagManagerActivity.RESP_TAG_ID, -1)
                    tagId?.let {
                        mModel.addTag(it)
                    }
                }
            }
        }
    }

    override fun onResume() {
        adapter.onResume()
        super.onResume()
    }

    override fun onStop() {
        adapter.onStop()
        super.onStop()
    }

}