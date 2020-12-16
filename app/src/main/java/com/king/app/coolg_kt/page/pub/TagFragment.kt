package com.king.app.coolg_kt.page.pub

import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.databinding.FragmentTagBinding
import com.king.app.coolg_kt.model.http.observer.SimpleObserver
import com.king.app.coolg_kt.model.repository.TagRepository
import com.king.app.coolg_kt.model.setting.SettingProperty
import com.king.app.coolg_kt.page.pub.BaseTagAdapter.OnItemSelectListener
import com.king.app.coolg_kt.view.dialog.DraggableContentFragment
import com.king.app.coolg_kt.view.dialog.SimpleDialogs
import com.king.app.gdb.data.entity.Tag
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class TagFragment : DraggableContentFragment<FragmentTagBinding>() {

    private var adapter: BaseTagAdapter<Tag>? = null
    private var tagList: List<Tag> = listOf()
    var tagType = 0
    private var repository = TagRepository()
    var onTagSelectListener: OnTagSelectListener? = null

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
        adapter = object : BaseTagAdapter<Tag>() {
            override fun getText(data: Tag): String {
                return data.name?:""
            }

            override fun getId(data: Tag): Long {
                return data.id!!
            }

            override fun isDisabled(item: Tag): Boolean {
                return false
            }
        }
        adapter!!.setOnItemSelectListener(object : OnItemSelectListener<Tag> {
            override fun onSelectItem(data: Tag) {
                onTagSelectListener?.onSelectTag(data)
                dismissAllowingStateLoss()
            }

            override fun onUnSelectItem(tag: Tag) {}
        })
        adapter!!.setOnItemLongClickListener { data: Tag -> preDelete(data) }
        refreshTags()
    }

    private fun editMode() {}

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
        tagList = repository.loadTags(tagType)
        return repository.sortTags(SettingProperty.getTagSortType(), tagList)
    }

    private fun addTag() {
        SimpleDialogs().openInputDialog(context, "Tag name") { name: String? ->
            if (repository!!.addTag(name!!, tagType)) {
                refreshTags()
            }
        }
    }

    private fun refreshTags() {
        loadTags()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe(object :
                SimpleObserver<List<Tag>>(CompositeDisposable()) {

                override fun onNext(tagList: List<Tag>) {
                    adapter?.setData(tagList)
                    adapter?.bindFlowLayout(mBinding.flowTags)
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                    showMessageShort(e?.message!!)
                }
            })
    }

    interface OnTagSelectListener {
        fun onSelectTag(tag: Tag)
    }
}