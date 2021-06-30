package com.king.app.coolg_kt.page.match.draw

import android.view.LayoutInflater
import android.view.ViewGroup
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.databinding.AdapterMatchWildcardBinding
import com.king.app.coolg_kt.page.match.WildcardBean

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/6/30 9:01
 */
class WildcardAdapter: BaseBindingAdapter<AdapterMatchWildcardBinding, WildcardBean>() {

    var onWildcardListener: OnWildcardListener? = null

    override fun onCreateBind(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): AdapterMatchWildcardBinding = AdapterMatchWildcardBinding.inflate(inflater, parent, false)

    override fun onBindItem(
        binding: AdapterMatchWildcardBinding,
        position: Int,
        bean: WildcardBean
    ) {
        binding.bean = bean
        binding.tvRank.text = " R ${bean.rank} "
        binding.ivEdit.setOnClickListener { onWildcardListener?.onEdit(position, bean) }
        binding.ivDelete.setOnClickListener { onWildcardListener?.onDelete(position, bean) }
    }

    interface OnWildcardListener {
        fun onEdit(position: Int, bean: WildcardBean)
        fun onDelete(position: Int, bean: WildcardBean)
    }
}