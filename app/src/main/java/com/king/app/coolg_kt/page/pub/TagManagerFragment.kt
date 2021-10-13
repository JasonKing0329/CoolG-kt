package com.king.app.coolg_kt.page.pub

import android.content.DialogInterface
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.king.app.coolg_kt.CoolApplication
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.ViewModelFactory
import com.king.app.coolg_kt.base.adapter.HeadChildBindingAdapter
import com.king.app.coolg_kt.databinding.ActivityTagManagerBinding
import com.king.app.coolg_kt.model.bean.TagGroupItem
import com.king.app.coolg_kt.utils.ScreenUtils
import com.king.app.coolg_kt.view.dialog.DraggableContentFragment
import com.king.app.coolg_kt.view.dialog.DraggableDialogFragment
import com.king.app.coolg_kt.view.dialog.SimpleDialogs
import com.king.app.coolg_kt.view.widget.flow_rc.FlowLayoutManager
import com.king.app.gdb.data.entity.Tag
import com.king.app.gdb.data.entity.TagClass

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/7/5 16:03
 */
@Deprecated("replace with TagManagerActivity")
class TagManagerFragment: DraggableContentFragment<ActivityTagManagerBinding>() {

    private lateinit var mModel: TagViewModel
    private var adapter = TagManagerAdapter()
    var tagType = 0

    var onTagSelectListener: OnTagSelectListener? = null

    var idealHeight = ScreenUtils.getScreenHeight() * 3 / 4

    override fun getBinding(inflater: LayoutInflater): ActivityTagManagerBinding = ActivityTagManagerBinding.inflate(inflater)

    override fun initData() {
        mModel = ViewModelProvider(this, ViewModelFactory(CoolApplication.instance)).get(TagViewModel::class.java)
        mModel.tagType = tagType

        var manager = FlowLayoutManager(context, false)
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
                    onTagSelectListener?.onSelectTag(data.item)
                }
            }
        }
        adapter.onHeadClickListener = object : HeadChildBindingAdapter.OnHeadClickListener<TagClass> {
            override fun onClickHead(view: View, position: Int, data: TagClass) {
                editTagClass(position, data)
            }
        }
        mBinding.rvList.adapter = adapter

//        mBinding.tvAddClass.setOnClickListener { newTag() }

        mModel.tagList.observe(this, Observer {
            adapter.list = it
            adapter.notifyDataSetChanged()
        })
        mModel.loadTags()
    }

    private fun newTag() {
        SimpleDialogs().openInputDialog(
            context,
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
            context,
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
            context,
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
        fragment.tagType = tagType
        val dialogFragment = DraggableDialogFragment()
        dialogFragment.contentFragment = fragment
        dialogFragment.setTitle("Select tag")
        dialogFragment.setBackgroundColor(resources.getColor(R.color.dlg_tag_bg))
        dialogFragment.dismissListener = DialogInterface.OnDismissListener { mModel.loadTags() }
        dialogFragment.show(childFragmentManager, "TagFragment")
    }

    interface OnTagSelectListener {
        fun onSelectTag(tag: Tag)
    }
}