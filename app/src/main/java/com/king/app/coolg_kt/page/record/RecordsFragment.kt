package com.king.app.coolg_kt.page.record

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupMenu
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BaseFragment
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.conf.AppConstants
import com.king.app.coolg_kt.databinding.FragmentRecordsBinding
import com.king.app.coolg_kt.model.setting.SettingProperty
import com.king.app.coolg_kt.page.record.pad.RecordPadActivity
import com.king.app.coolg_kt.page.record.phone.RecordActivity
import com.king.app.coolg_kt.page.record.popup.RecommendBean
import com.king.app.coolg_kt.page.record.popup.RecommendFragment
import com.king.app.coolg_kt.page.record.popup.SortDialogContent
import com.king.app.coolg_kt.page.video.order.PlayOrderActivity
import com.king.app.coolg_kt.utils.ScreenUtils
import com.king.app.coolg_kt.view.dialog.DraggableDialogFragment
import com.king.app.coolg_kt.view.dialog.SimpleDialogs
import com.king.app.gdb.data.entity.Record
import com.king.app.gdb.data.relation.RecordWrap

/**
 * @description:
 * @author：Jing
 * @date: 2021/2/5 16:09
 */
class RecordsFragment: BaseFragment<FragmentRecordsBinding, RecordListViewModel>() {

    var selectAsMatchItem = false

    private val REQUEST_VIDEO_ORDER = 1603
    
    var overrideClickRecordListener: OnClickRecordListener? = null

    private var recordAdapter: RecordGridAdapter = RecordGridAdapter()

    var factor = Factor()

    override fun createViewModel(): RecordListViewModel = generateViewModel(RecordListViewModel::class.java)

    override fun getBinding(inflater: LayoutInflater): FragmentRecordsBinding = FragmentRecordsBinding.inflate(inflater)

    override fun initView(view: View) {
        val span = if (ScreenUtils.isTablet()) 3 else 2
        mBinding.rvRecords.layoutManager = GridLayoutManager(requireContext(), span)
        mBinding.rvRecords.setEnableLoadMore(true)
        mBinding.rvRecords.setOnLoadMoreListener { mModel.loadMoreRecords() }

        if (selectAsMatchItem) {
            mBinding.cbBlack.visibility = View.VISIBLE
            mBinding.cbBlack.setOnCheckedChangeListener { buttonView, isChecked ->
                mModel.toggleBlacklist(isChecked)
            }
        }
        recordAdapter.popupListener = object : OnPopupListener {
            override fun onPopupRecord(view: View, position: Int, record: RecordWrap) {
                showEditPopup(view, record.bean)
            }
        }
        recordAdapter.setOnItemClickListener(object : BaseBindingAdapter.OnItemClickListener<RecordWrap> {
            override fun onClickItem(view: View, position: Int, data: RecordWrap) {
                if (overrideClickRecordListener == null) {
                    onClickRecord(view, position, data)
                }
                else {
                    overrideClickRecordListener!!.onClickRecord(data)
                }
            }
        })
        mBinding.rvRecords.adapter = recordAdapter
    }

    override fun initData() {

        mModel.selectAsMatchItem = selectAsMatchItem
        mModel.recordsObserver.observe(this, Observer{ list -> showRecords(list) })
        mModel.rangeChangedObserver.observe(this, Observer{ recordAdapter.notifyItemRangeChanged(it.start, it.count) })
        mModel.moreObserver.observe(this, Observer{ offset -> showMoreList(offset) })
        mModel.scrollPositionObserver.observe(this, Observer{ offset -> scrollTo(offset) })
        onDataChanged()
    }

    private fun showRecords(list: List<RecordWrap>) {
        recordAdapter.list = list
        recordAdapter.notifyDataSetChanged()
    }

    private fun showMoreList(offset: Int) {
        recordAdapter.notifyItemInserted(offset)
    }

    open fun showEditPopup(view: View, data: Record) {
        val menu = PopupMenu(requireContext(), view)
        menu.menuInflater.inflate(R.menu.popup_record_edit, menu.menu)
        menu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_add_to_play_order -> addToPlayOrder(data)
                R.id.menu_detail -> goToRecordPage(data)
            }
            false
        }
        menu.show()
    }

    private fun onClickRecord(view: View, position: Int, data: RecordWrap) {
        goToRecordPage(data.bean)
    }

    private fun addToPlayOrder(data: Record) {
        mModel.saveRecordToPlayOrder(data)
        PlayOrderActivity.startPageToSelect(this, REQUEST_VIDEO_ORDER)
    }

    private fun goToRecordPage(record: Record) {
        if (ScreenUtils.isTablet()) {
            RecordPadActivity.startPage(requireContext(), record.id!!)
        }
        else {
            RecordActivity.startPage(requireContext(), record.id!!)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_VIDEO_ORDER) {
            if (resultCode == Activity.RESULT_OK) {
                val list = data?.getCharSequenceArrayListExtra(PlayOrderActivity.RESP_SELECT_RESULT)
                mModel.addToPlay(list)
            }
        }
    }

    fun onDataChanged() {
        mModel.updateFactors(factor)
        mModel.reloadRecords()
    }

    fun scrollTo(position: Int) {
        mBinding.rvRecords.scrollToPosition(position)
    }

    fun changeSortType() {
        val content = SortDialogContent()
        content.mDesc = SettingProperty.isRecordSortDesc()
        content.mSortType = (SettingProperty.getRecordSortType())
        content.onSortListener = object : SortDialogContent.OnSortListener{
            override fun onSort(desc: Boolean, sortMode: Int) {
                SettingProperty.setRecordSortType(sortMode)
                SettingProperty.setRecordSortDesc(desc)
                mModel.onSortTypeChanged()
                onDataChanged()
            }
        }
        val dialogFragment = DraggableDialogFragment()
        dialogFragment.contentFragment = content
        dialogFragment.setTitle("Sort")
        dialogFragment.show(childFragmentManager, "SortDialogContent")
    }

    fun changeFilter() {
        val content = RecommendFragment()
        content.mBean = mModel.getNotNullRecommendBean()
        //        content.setFixedType(ftRecords.getCurrentItem());
        content.onRecommendListener = object : RecommendFragment.OnRecommendListener {
            override fun onSetSql(bean: RecommendBean) {
                mModel.mRecommendBean = bean
                onDataChanged()
            }
        }
        val dialogFragment = DraggableDialogFragment()
        dialogFragment.setTitle("Recommend Setting")
        dialogFragment.contentFragment = content
        dialogFragment.maxHeight = ScreenUtils.getScreenHeight() * 2 / 3
        dialogFragment.show(childFragmentManager, "RecommendFragment")
    }

    fun showSetOffset() {
        SimpleDialogs().openInputDialog(requireContext(), "set offset") { name ->
            kotlin.runCatching {
                val offset = name.toInt()
                if (offset < mModel.getOffset()) {
                    scrollTo(offset)
                } else {
                    mModel.setOffset(offset)
                }
            }
        }
    }

    interface OnClickRecordListener {
        fun onClickRecord(record: RecordWrap)
    }

    class Factor {
        var keyword: String? = null
        val recordType: Int = 0
        var orderId: Long = 0
        var starId: Long = 0
        var tagId: Long = 0
        var scene: String? = AppConstants.KEY_SCENE_ALL
        var outOfRank: Boolean = false
    }
}