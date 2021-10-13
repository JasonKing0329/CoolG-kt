package com.king.app.coolg_kt.page.pub

import android.content.DialogInterface
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.databinding.FragmentTagBinding
import com.king.app.coolg_kt.model.http.observer.SimpleObserver
import com.king.app.coolg_kt.model.repository.TagRepository
import com.king.app.coolg_kt.model.setting.SettingProperty
import com.king.app.coolg_kt.utils.ScreenUtils
import com.king.app.coolg_kt.view.dialog.DraggableContentFragment
import com.king.app.coolg_kt.view.dialog.SimpleDialogs
import com.king.app.coolg_kt.view.widget.flow_rc.FlowLayoutManager
import com.king.app.gdb.data.entity.Tag
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers

class TagFragment : DraggableContentFragment<FragmentTagBinding>() {

    private var adapter = TagAdapter()
    private var tagList: List<Tag> = listOf()
    var tagType = 0
    private var repository = TagRepository()
    var onTagSelectListener: OnTagSelectListener? = null

    var isOnlyShowUnClassified = false

    private var isEditMode = false

    override fun getBinding(inflater: LayoutInflater): FragmentTagBinding = FragmentTagBinding.inflate(inflater)
    override fun initData() {
        repository = TagRepository()
        dialogHolder?.let {
            val view = it.inflateToolbar(R.layout.layout_toolbar_tag)
            view.findViewById<View>(R.id.iv_add)
                .setOnClickListener { addTag() }
            view.findViewById<View>(R.id.iv_edit)
                .setOnClickListener { editMode() }
        }
        adapter.onDeleteListener = object : TagAdapter.OnDeleteListener {
            override fun onDelete(position: Int, bean: Tag) {
                preDelete(bean)
            }
        }
        adapter.setOnItemClickListener(object : BaseBindingAdapter.OnItemClickListener<Tag> {

            override fun onClickItem(view: View, position: Int, data: Tag) {
                if (isEditMode) {
                    editTagItem(position, data)
                }
                else {
                    onTagSelectListener?.onSelectTag(data)
                    dismissAllowingStateLoss()
                }
            }
        })
        mBinding.rvList.layoutManager = FlowLayoutManager(context, false)
        mBinding.rvList.addItemDecoration(object : RecyclerView.ItemDecoration(){
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                outRect.top = ScreenUtils.dp2px(8f)
                outRect.left = ScreenUtils.dp2px(8f)
            }
        })
        mBinding.rvList.adapter = adapter
        refreshTags()
    }

    private fun editMode() {
        isEditMode = !isEditMode
        adapter.showDelete = isEditMode
        adapter.notifyDataSetChanged()
    }

    private fun preDelete(data: Tag) {
        var count = repository.getTagItemCount(tagType, data.id)
        val msg: String
        val okListener: DialogInterface.OnClickListener
        if (count > 0) {
            msg = "'" + data.name + "' is related to items, delete all related items too?"
            okListener = DialogInterface.OnClickListener { dialog: DialogInterface?, which: Int ->
                repository.deleteTagAndRelations(data)
                refreshTags()
                showMessageShort("success")
            }
        } else {
            msg = "'" + data.name + "' will be deleted by this operation, continue?"
            okListener = DialogInterface.OnClickListener { dialog: DialogInterface?, which: Int ->
                repository.deleteTag(data)
                refreshTags()
                showMessageShort("success")
            }
        }
        SimpleDialogs().showConfirmCancelDialog(context, msg, okListener, null)
    }

    private fun loadTags(): Observable<List<Tag>> {
        tagList = if (isOnlyShowUnClassified) {
            repository.loadUnClassifiedTags(tagType)
        }
        else {
            repository.loadTags(tagType)
        }
        return repository.sortTags(SettingProperty.getTagSortType(), tagList)
    }

    private fun addTag() {
        SimpleDialogs().openInputDialog(context, "Tag name") { name: String? ->
            name?.let {
                if (repository.addTag(it, tagType)) {
                    refreshTags()
                }
                else {
                    showMessageShort("Name is already existed")
                }
            }
        }
    }

    fun editTagItem(position: Int, tag: Tag) {
        SimpleDialogs().openInputDialog(
            context,
            "Edit Tag",
            tag.name
        ) { name ->
            name?.let {
                if (repository.editTag(tag, it)) {
                    showMessageShort("success")
                    adapter.notifyItemChanged(position)
                }
                else {
                    showMessageShort("Target name is already existed")
                }
            }
        }
    }

    private fun refreshTags() {
        loadTags()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe(object :
                SimpleObserver<List<Tag>>(compositeDisposable) {

                override fun onNext(tagList: List<Tag>) {
                    adapter.list = tagList
                    adapter.notifyDataSetChanged()
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                    showMessageShort(e?.message?:"")
                }
            })
    }

    interface OnTagSelectListener {
        fun onSelectTag(tag: Tag)
    }
}