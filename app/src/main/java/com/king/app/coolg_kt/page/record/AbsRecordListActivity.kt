package com.king.app.coolg_kt.page.record

import android.view.View
import android.widget.PopupMenu
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BaseActivity
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.model.setting.SettingProperty
import com.king.app.coolg_kt.page.record.popup.RecommendBean
import com.king.app.coolg_kt.page.record.popup.RecommendFragment
import com.king.app.coolg_kt.page.record.popup.SortDialogContent
import com.king.app.coolg_kt.utils.ScreenUtils
import com.king.app.coolg_kt.view.dialog.AlertDialogFragment
import com.king.app.coolg_kt.view.dialog.DraggableDialogFragment
import com.king.app.coolg_kt.view.dialog.SimpleDialogs
import com.king.app.gdb.data.entity.Record
import com.king.app.gdb.data.relation.RecordWrap
import com.king.app.jactionbar.JActionbar

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2020/12/15 9:44
 */
abstract class AbsRecordListActivity<T: ViewDataBinding, VM: RecordListViewModel>: BaseActivity<T, VM>() {

    protected val REQUEST_VIDEO_ORDER = 1603
    private var recordAdapter: RecordGridAdapter = RecordGridAdapter()

    override fun initData() {
        recordAdapter.popupListener = object : OnPopupListener {
            override fun onPopupRecord(view: View, position: Int, record: RecordWrap) {
                showEditPopup(view, record.bean)
            }
        }
        recordAdapter.setOnItemClickListener(object : BaseBindingAdapter.OnItemClickListener<RecordWrap> {
            override fun onClickItem(view: View, position: Int, data: RecordWrap) {
                onClickRecord(view, position, data)
            }
        })
        getRecordRecyclerView().adapter = recordAdapter

        mModel.tagsObserver.observe(this, Observer{ tags -> showTags(tags) })
        mModel.recordsObserver.observe(this, Observer{ list -> showRecords(list) })
        mModel.moreObserver.observe(this, Observer{ offset -> showMoreList(offset) })
        mModel.scrollPositionObserver.observe(
            this,
            Observer{ offset -> getRecordRecyclerView().scrollToPosition(offset) })
        mModel.focusTagPosition.observe(this, Observer{ position -> focusOnTag(position) })

        mModel.loadHead()
    }

    open fun initActionBar(actionbar: JActionbar) {
        actionbar.setOnBackListener { onBackPressed() }
        actionbar.setOnMenuItemListener { menuId: Int ->
            when (menuId) {
                R.id.menu_sort -> changeSortType()
                R.id.menu_filter -> changeFilter()
                R.id.menu_offset -> showSetOffset()
                R.id.menu_tag_sort_mode -> setTagSortMode()
                R.id.menu_tag_type -> setTagType()
            }
        }
        if (isHideTagBar()) {
            actionbar.updateMenuItemVisible(R.id.menu_tag_sort_mode, false)
            actionbar.updateMenuItemVisible(R.id.menu_tag_type, false)
        }
    }

    open fun onClickRecord(view: View, position: Int, data: RecordWrap) {
        goToRecordPage(data.bean)
    }

    protected abstract fun isHideTagBar(): Boolean

    protected abstract fun getRecordRecyclerView(): RecyclerView

    protected abstract fun showTags(tags: List<RecordTag>)

    protected abstract fun focusOnTag(position: Int)

    protected abstract fun goToRecordPage(record: Record)

    protected abstract fun addToPlayOrder(data: Record)

    open fun setTagSortMode() {
        var arrays = if (SettingProperty.getRecordListTagType() == 1) {
            resources.getStringArray(R.array.scene_sort_mode)
        }
        else {
            resources.getStringArray(R.array.tag_sort_mode)
        }
        AlertDialogFragment()
            .setTitle(null)
            .setItems(arrays) { dialog, which ->
                SettingProperty.setTagSortType(which)
                mModel.onTagSortChanged()
                mModel.startSortTag()
            }.show(supportFragmentManager, "AlertDialogFragment")
    }

    open fun setTagType() {
        AlertDialogFragment()
            .setTitle(null)
            .setItems(resources.getStringArray(R.array.record_list_tag_type)) { dialog, which ->
                SettingProperty.setRecordListTagType(which)
                mModel.onTagTypeChanged()
                mModel.loadHead()
            }.show(supportFragmentManager, "AlertDialogFragment")
    }

    open fun showRecords(list: List<RecordWrap>) {
        recordAdapter.list = list
        recordAdapter.notifyDataSetChanged()
    }

    open fun showEditPopup(view: View, data: Record) {
        val menu = PopupMenu(this, view)
        menu.menuInflater.inflate(R.menu.popup_record_edit, menu.getMenu())
        menu.menu.findItem(R.id.menu_set_cover).isVisible = false
        menu.menu.findItem(R.id.menu_delete).isVisible = false
        menu.setOnMenuItemClickListener { item ->
            when (item.getItemId()) {
                R.id.menu_add_to_order -> {
                }
                R.id.menu_add_to_play_order -> addToPlayOrder(data)
            }
            false
        }
        menu.show()
    }

    private fun showMoreList(offset: Int) {
        recordAdapter.notifyItemInserted(offset)
    }

    open fun changeSortType() {
        val content = SortDialogContent()
        content.mDesc = SettingProperty.isRecordSortDesc()
        content.mSortType = (SettingProperty.getRecordSortType())
        content.onSortListener = object : SortDialogContent.OnSortListener{
            override fun onSort(desc: Boolean, sortMode: Int) {
                SettingProperty.setRecordSortType(sortMode)
                SettingProperty.setRecordSortDesc(desc)
                mModel.onSortTypeChanged()
                mModel.loadRecordsByTag()
            }
        }
        val dialogFragment = DraggableDialogFragment()
        dialogFragment.contentFragment = content
        dialogFragment.setTitle("Sort")
        dialogFragment.show(supportFragmentManager, "SortDialogContent")
    }

    open fun changeFilter() {
        val content = RecommendFragment()
        content.mBean = mModel.getNotNullRecommendBean()
        //        content.setFixedType(ftRecords.getCurrentItem());
        content.onRecommendListener = object : RecommendFragment.OnRecommendListener {
            override fun onSetSql(bean: RecommendBean) {
                mModel.mRecommendBean = bean
                mModel.loadRecordsByTag()
            }
        }
        val dialogFragment = DraggableDialogFragment()
        dialogFragment.setTitle("Recommend Setting")
        dialogFragment.contentFragment = content
        dialogFragment.maxHeight = ScreenUtils.getScreenHeight() * 2 / 3
        dialogFragment.show(supportFragmentManager, "RecommendFragment")
    }

    open fun showSetOffset() {
        SimpleDialogs().openInputDialog(this, "set offset") { name ->
            kotlin.runCatching {
                val offset = name.toInt()
                if (offset < mModel.getOffset()) {
                    getRecordRecyclerView().scrollToPosition(offset)
                } else {
                    mModel.setOffset(offset)
                }
            }
        }
    }

}