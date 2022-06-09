package com.king.app.coolg_kt.page.pub

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Rect
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BaseActivity
import com.king.app.coolg_kt.base.adapter.HeadChildBindingAdapter
import com.king.app.coolg_kt.databinding.ActivityTagManagerBinding
import com.king.app.coolg_kt.model.bean.TagGroupItem
import com.king.app.coolg_kt.utils.ScreenUtils
import com.king.app.coolg_kt.view.dialog.DraggableDialogFragment
import com.king.app.coolg_kt.view.dialog.SimpleDialogs
import com.king.app.coolg_kt.view.widget.flow_rc.FlowLayoutManager
import com.king.app.gdb.data.entity.Tag
import com.king.app.gdb.data.entity.TagClass

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/7/7 9:14
 */
class TagManagerActivity: BaseActivity<ActivityTagManagerBinding, TagViewModel>() {

    private var adapter = TagManagerAdapter()

    companion object {
        val RESP_TAG_ID = "tag_id"
        val EXTRA_TAG_TYPE = "tag_type"
        fun startPage(context: Activity, requestCode: Int, tagType: Int) {
            var intent = Intent(context, TagManagerActivity::class.java)
            intent.putExtra(EXTRA_TAG_TYPE, tagType)
            context.startActivityForResult(intent, requestCode)
        }
        fun startPage(fragment: Fragment, requestCode: Int, tagType: Int) {
            var intent = Intent(fragment.requireContext(), TagManagerActivity::class.java)
            intent.putExtra(EXTRA_TAG_TYPE, tagType)
            fragment.startActivityForResult(intent, requestCode)
        }
    }

    override fun getContentView(): Int = R.layout.activity_tag_manager

    override fun createViewModel(): TagViewModel = generateViewModel(TagViewModel::class.java)

    private fun getTagType(): Int {
        return intent.getIntExtra(EXTRA_TAG_TYPE, 0)
    }

    override fun initView() {
        mModel.tagType = getTagType()

        var manager = FlowLayoutManager(this, false)
        manager.setCustomFlow {
            adapter.isHead(it)
        }
        mBinding.rvList.layoutManager = manager
        mBinding.rvList.addItemDecoration(object : RecyclerView.ItemDecoration(){
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                outRect.top = ScreenUtils.dp2px(4f)
                outRect.right = ScreenUtils.dp2px(4f)
            }
        })

        adapter.onTagClassListener = object : TagManagerAdapter.OnTagClassListener {
            override fun onAddItem(position: Int, bean: TagClass) {
                addTagItem(bean)
            }

            override fun onDeleteItem(position: Int, bean: TagClass) {
                showConfirmCancelMessage("It will delete all relations, continue?",
                    DialogInterface.OnClickListener { dialog, which ->
                        mModel.deleteTagClass(bean)
                        mModel.loadTags()
                    },
                    null)
            }
        }
        adapter.onTagItemListener = object : TagManagerAdapter.OnTagItemListener {
            override fun onDeleteItem(position: Int, item: TagGroupItem) {
                mModel.deleteTagItem(item)
                mModel.loadTags()
            }
        }
        adapter.onItemClickListener = object : HeadChildBindingAdapter.OnItemClickListener<TagGroupItem> {
            override fun onClickItem(view: View, position: Int, data: TagGroupItem) {
                if (data.isEditing) {
                    editTagItem(position, data.item)
                }
                else {
                    val intent = Intent()
                    intent.putExtra(RESP_TAG_ID, data.item.id!!)
                    setResult(RESULT_OK, intent)
                    finish()
                }
            }
        }
        adapter.onHeadClickListener = object : HeadChildBindingAdapter.OnHeadClickListener<TagClass> {
            override fun onClickHead(view: View, position: Int, data: TagClass) {
                editTagClass(position, data)
            }
        }
        mBinding.rvList.adapter = adapter

        mBinding.actionbar.setOnBackListener { onBackPressed() }
        mBinding.actionbar.setOnMenuItemListener {
            when(it) {
                R.id.menu_add -> newTag()
            }
        }

    }

    override fun initData() {
        mModel.tagList.observe(this, Observer {
            adapter.list = it
            adapter.notifyDataSetChanged()
        })
        mModel.loadTags()
    }

    private fun newTag() {
        SimpleDialogs().openInputDialog(
            this,
            "New Tag Class"
        ) { name ->
            name?.let {
                mModel.newTagClass(it)
                mModel.loadTags()
            }
        }
    }

    private fun editTagClass(position: Int, bean: TagClass) {
        SimpleDialogs().openInputDialog(
            this,
            "Edit Tag Class",
            bean.name
        ) { name ->
            name?.let {
                mModel.editTagClass(bean, it)
                adapter.notifyItemChanged(position)
            }
        }
    }

    private fun editTagItem(position: Int, tag: Tag) {
        SimpleDialogs().openInputDialog(
            this,
            "Edit Tag",
            tag.name
        ) { name ->
            name?.let {
                mModel.editTagItem(tag, it)
                adapter.notifyItemChanged(position)
            }
        }
    }

    private fun addTagItem(bean: TagClass) {
        val fragment = TagFragment()
        fragment.onTagSelectListener = object : TagFragment.OnTagSelectListener{
            override fun onSelectTag(tag: Tag) {
                mModel.newTagClassItem(bean.id, tag)
            }
        }
        fragment.isOnlyShowUnClassified = true
        fragment.tagType = getTagType()
        val dialogFragment = DraggableDialogFragment()
        dialogFragment.minHeight = ScreenUtils.dp2px(300f)
        dialogFragment.contentFragment = fragment
        dialogFragment.setTitle("Select tag")
        dialogFragment.setBackgroundColor(resources.getColor(R.color.dlg_tag_bg))
        dialogFragment.dismissListener = DialogInterface.OnDismissListener { mModel.loadTags() }
        dialogFragment.show(supportFragmentManager, "TagFragment")
    }

}