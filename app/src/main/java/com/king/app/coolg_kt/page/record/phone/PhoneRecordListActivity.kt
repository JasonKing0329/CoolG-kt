package com.king.app.coolg_kt.page.record.phone

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BaseActivity
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.conf.AppConstants
import com.king.app.coolg_kt.databinding.ActivityRecordTagBinding
import com.king.app.coolg_kt.model.setting.SettingProperty
import com.king.app.coolg_kt.page.record.NoStudioActivity
import com.king.app.coolg_kt.page.record.RecordTag
import com.king.app.coolg_kt.page.record.RecordsFragment
import com.king.app.coolg_kt.utils.ScreenUtils
import com.king.app.coolg_kt.view.dialog.AlertDialogFragment
import com.king.app.gdb.data.entity.TagClass
import com.king.app.gdb.data.relation.RecordWrap

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2020/12/15 9:49
 */
open class PhoneRecordListActivity: BaseActivity<ActivityRecordTagBinding, PhoneRecordListViewModel>() {

    companion object {
        val EXTRA_STUDIO_ID = "studio_id"
        val EXTRA_SELECT_MODE = "select_mode"
        val EXTRA_OUT_OF_RANK = "out_of_rank"
        val EXTRA_SELECT_AS_MATCH_ITEM = "select_as_match_item"
        val RESP_RECORD_ID = "record_id"
        fun startPage(context: Context) {
            var intent = Intent(context, PhoneRecordListActivity::class.java)
            context.startActivity(intent)
        }
        fun startPageToSelect(context: Activity, requestCode: Int) {
            var intent = Intent(context, PhoneRecordListActivity::class.java)
            intent.putExtra(EXTRA_SELECT_MODE, true)
            context.startActivityForResult(intent, requestCode)
        }
        fun startPageToSelectAsMatchItem(context: Activity, requestCode: Int) {
            var intent = Intent(context, PhoneRecordListActivity::class.java)
            intent.putExtra(EXTRA_SELECT_MODE, true)
            intent.putExtra(EXTRA_SELECT_AS_MATCH_ITEM, true)
            context.startActivityForResult(intent, requestCode)
        }
        fun startPageToSelectAsMatchItem(context: Activity, requestCode: Int, studioId: Long) {
            var intent = Intent(context, PhoneRecordListActivity::class.java)
            intent.putExtra(EXTRA_SELECT_MODE, true)
            intent.putExtra(EXTRA_SELECT_AS_MATCH_ITEM, true)
            intent.putExtra(EXTRA_STUDIO_ID, studioId)
            context.startActivityForResult(intent, requestCode)
        }
        fun startPageToSelectAsMatchItem(context: Activity, requestCode: Int, outOfRank: Boolean) {
            var intent = Intent(context, PhoneRecordListActivity::class.java)
            intent.putExtra(EXTRA_SELECT_MODE, true)
            intent.putExtra(EXTRA_SELECT_AS_MATCH_ITEM, true)
            intent.putExtra(EXTRA_OUT_OF_RANK, outOfRank)
            context.startActivityForResult(intent, requestCode)
        }
        fun startStudioPage(context: Context, studioId: Long) {
            var intent = Intent(context, PhoneRecordListActivity::class.java)
            intent.putExtra(EXTRA_STUDIO_ID, studioId)
            context.startActivity(intent)
        }
    }

    var tagAdapter = HeadTagAdapter()

    var tagClassAdapter = TagClassAdapter()

    val ftRecord = RecordsFragment()

    override fun getContentView(): Int = R.layout.activity_record_tag

    override fun createViewModel(): PhoneRecordListViewModel = generateViewModel(PhoneRecordListViewModel::class.java)

    override fun initView() {
        initPhone()
        initPub()
    }

    private fun initPhone() {
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
        mBinding.rvTagClass.adapter = tagClassAdapter
    }

    open fun initPub() {

        tagAdapter.setOnItemClickListener(object : BaseBindingAdapter.OnItemClickListener<RecordTag>{
            override fun onClickItem(view: View, position: Int, data: RecordTag) {
                if (data.type == 0) {
                    ftRecord.factor.tagId = data.id
                    ftRecord.factor.scene = AppConstants.KEY_SCENE_ALL
                }
                else {
                    ftRecord.factor.scene = data.name
                    ftRecord.factor.tagId = 0
                }
                ftRecord.onDataChanged()
            }
        })
        tagClassAdapter.setOnItemClickListener(object : BaseBindingAdapter.OnItemClickListener<TagClass>{
            override fun onClickItem(view: View, position: Int, data: TagClass) {
                mModel.mCurTagClassId = data.id
                mModel.loadTags()
            }
        })
        mBinding.rvTags.adapter = tagAdapter

        mBinding.fabTop.setOnClickListener { ftRecord.scrollTo(0) }

        // studio records page, hide tag bar and related menu
        if (getStudioId() != 0L) {
            mBinding.rvTags.visibility = View.GONE
            mBinding.actionbar.setTitle(mModel.loadStudioTitle(getStudioId()))
        }
        initActionBar()

        if (intent.getBooleanExtra(EXTRA_SELECT_MODE, false)) {
            ftRecord.selectAsMatchItem = isSelectAsMatchItem()
            ftRecord.overrideClickRecordListener = object : RecordsFragment.OnClickRecordListener {
                override fun onClickRecord(record: RecordWrap) {
                    if (isSelectAsMatchItem() && record.canSelect != true) {
                        showMessageShort("This record is already in draws of current week")
                    }
                    else {
                        val intent = Intent()
                        intent.putExtra(RESP_RECORD_ID, record.bean.id!!)
                        setResult(Activity.RESULT_OK, intent)
                        finish()
                    }
                }
            }
        }
    }

    private fun isSelectAsMatchItem(): Boolean {
        return intent.getBooleanExtra(EXTRA_SELECT_AS_MATCH_ITEM, false)
    }

    private fun getStudioId(): Long {
        return intent.getLongExtra(EXTRA_STUDIO_ID, 0)
    }

    private fun isOutOfRank(): Boolean {
        return intent.getBooleanExtra(EXTRA_OUT_OF_RANK, false)
    }

    override fun initData() {
        mModel.tagsObserver.observe(this, Observer{ tags -> showTags(tags) })
        mModel.tagClassesObserver.observe(this, Observer{ tags -> showTagClasses(tags) })
        mModel.focusTagPosition.observe(this, Observer{ position -> focusOnTag(position) })
        if (getStudioId() == 0L) {
            mModel.loadHead()
            tagClassVisibility()
        }

        ftRecord.factor.orderId = getStudioId()
        ftRecord.factor.outOfRank = isOutOfRank()
        supportFragmentManager.beginTransaction()
            .replace(R.id.ft_records, ftRecord, "RecordsFragment")
            .commit()
    }

    private fun tagClassVisibility() {
        mBinding.rvTagClass.visibility = if (mModel.isHeadScene()) {
            View.GONE
        }
        else {
            View.VISIBLE
        }
    }

    private fun initActionBar() {
        mBinding.actionbar.setOnBackListener { onBackPressed() }
        mBinding.actionbar.setOnMenuItemListener { menuId: Int ->
            when (menuId) {
                R.id.menu_sort -> ftRecord.changeSortType()
                R.id.menu_filter -> ftRecord.changeFilter()
                R.id.menu_offset -> ftRecord.showSetOffset()
                R.id.menu_tag_sort_mode -> setTagSortMode()
                R.id.menu_tag_type -> setTagType()
                R.id.menu_no_studio -> noStudioPage()
            }
        }
        if (getStudioId() != 0L) {
            mBinding.actionbar.updateMenuItemVisible(R.id.menu_tag_sort_mode, false)
            mBinding.actionbar.updateMenuItemVisible(R.id.menu_tag_type, false)
        }
        mBinding.actionbar.setOnSearchListener { onSearch(it) }
    }

    private fun showTagClasses(tagClasses: List<TagClass>) {
        tagClassAdapter.list = tagClasses
        tagClassAdapter.notifyDataSetChanged()
    }

    private fun showTags(tags: List<RecordTag>) {
        tagAdapter.list = tags
        decorateTagList(tags)
        tagAdapter.notifyDataSetChanged()
    }

    fun decorateTagList(tags: List<RecordTag>) {
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

    private var tagDecoration = object : RecyclerView.ItemDecoration() {
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

    private fun focusOnTag(position: Int) {
        tagAdapter.selection = position
        tagAdapter.notifyDataSetChanged()
    }

    private fun onSearch(text: String) {
        ftRecord.factor.keyword = text
        ftRecord.onDataChanged()
    }

    private fun noStudioPage() {
        NoStudioActivity.startPage(this)
    }

    private fun setTagSortMode() {
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

    private fun setTagType() {
        AlertDialogFragment()
            .setTitle(null)
            .setItems(resources.getStringArray(R.array.record_list_tag_type)) { dialog, which ->
                SettingProperty.setRecordListTagType(which)
                mModel.onTagTypeChanged()
                mModel.loadHead()
                tagClassVisibility()
            }.show(supportFragmentManager, "AlertDialogFragment")
    }

}