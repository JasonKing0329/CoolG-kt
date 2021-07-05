package com.king.app.coolg_kt.page.pub

import android.view.LayoutInflater
import androidx.lifecycle.ViewModelProvider
import com.king.app.coolg_kt.CoolApplication
import com.king.app.coolg_kt.base.ViewModelFactory
import com.king.app.coolg_kt.databinding.FragmentTagManagerBinding
import com.king.app.coolg_kt.view.dialog.DraggableContentFragment
import com.king.app.coolg_kt.view.dialog.SimpleDialogs
import com.king.app.coolg_kt.view.widget.flow_rc.FlowLayoutManager

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/7/5 16:03
 */
class TagManagerFragment: DraggableContentFragment<FragmentTagManagerBinding>() {

    private var mModel = ViewModelProvider(this, ViewModelFactory(CoolApplication.instance)).get(TagViewModel::class.java)
    private var adapter = TagManagerAdapter()

    override fun getBinding(inflater: LayoutInflater): FragmentTagManagerBinding = FragmentTagManagerBinding.inflate(inflater)

    override fun initData() {
        var manager = FlowLayoutManager(context, false)
        manager.setCustomFlow {
            adapter.isHead(it)
        }
        mBinding.rvList.layoutManager = manager
        mBinding.rvList.adapter = adapter

        mBinding.tvAddClass.setOnClickListener { newTag() }

        mModel.loadTags()
    }

    private fun newTag() {
        SimpleDialogs().openInputDialog(
            context,
            "New Tag"
        ) { name -> name?.let { mModel.newTagClass(it) } }
    }
}