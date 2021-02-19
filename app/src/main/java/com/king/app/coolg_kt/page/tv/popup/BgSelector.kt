package com.king.app.coolg_kt.page.tv.popup

import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.databinding.FragmentTvBgSelectorBinding
import com.king.app.coolg_kt.view.dialog.TvDialogContentFragment

/**
 * @description:
 * @author：Jing
 * @date: 2021/2/17 12:25
 */
class BgSelector: TvDialogContentFragment<FragmentTvBgSelectorBinding>() {

    val adapter = BgSelectorAdapter()

    var onSelectBgListener: OnSelectBgListener? = null

    var list: List<String>? = null

    override fun getBinding(inflater: LayoutInflater): FragmentTvBgSelectorBinding = FragmentTvBgSelectorBinding.inflate(inflater)

    override fun initData() {
        mBinding.rvImages.layoutManager = GridLayoutManager(requireContext(), 4)
        list?.let {
            adapter.list = it
        }
        mBinding.rvImages.adapter = adapter

        adapter.setOnItemClickListener(object : BaseBindingAdapter.OnItemClickListener<String>{
            override fun onClickItem(view: View, position: Int, data: String) {
                onSelectBgListener?.onSelectBg(data)
                dismissAllowingStateLoss()
            }
        })
    }

    override fun onConfirm(): Boolean = false

    override fun onCancel(): Boolean = false

    interface OnSelectBgListener {
        fun onSelectBg(url: String)
    }
}