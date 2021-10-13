package com.king.app.coolg_kt.page.match.draw

import android.view.LayoutInflater
import androidx.recyclerview.widget.GridLayoutManager
import com.king.app.coolg_kt.databinding.FragmentDialogMatchWildcardBinding
import com.king.app.coolg_kt.page.match.WildcardBean
import com.king.app.coolg_kt.view.dialog.DraggableContentFragment

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/6/30 9:04
 */
class WildcardDialog: DraggableContentFragment<FragmentDialogMatchWildcardBinding>() {

    val adapter = WildcardAdapter()

    var dataList = mutableListOf<WildcardBean>()

    var wildcardListener: WildCardListener? = null

    var mEditPosition = -1

    override fun getBinding(inflater: LayoutInflater): FragmentDialogMatchWildcardBinding = FragmentDialogMatchWildcardBinding.inflate(inflater)

    override fun initData() {

        mBinding.tvTitle.text = "Wildcards (${dataList.size})"

        mBinding.rvList.layoutManager = GridLayoutManager(context, 3)
        adapter.list = dataList
        mBinding.rvList.adapter = adapter

        adapter.onWildcardListener = object : WildcardAdapter.OnWildcardListener {
            override fun onEdit(position: Int, bean: WildcardBean) {
                mEditPosition = position
                wildcardListener?.selectRecord()
            }

            override fun onDelete(position: Int, bean: WildcardBean) {
                dataList[position].imageUrl = null
                dataList[position].rank = 0
                dataList[position].recordId = 0
                adapter.notifyItemChanged(position)
            }
        }

        mBinding.tvOk.setOnClickListener {
            wildcardListener?.confirm(dataList)
            dismissAllowingStateLoss()
        }

        mBinding.tvRandom.setOnClickListener {
            dataList.shuffle()
            adapter.notifyDataSetChanged()
        }
    }

    fun setSelectedRecord(bean: WildcardBean) {
        kotlin.runCatching {
            dataList[mEditPosition] = bean
            adapter.notifyItemChanged(mEditPosition)
        }.let { it.exceptionOrNull()?.printStackTrace() }
    }

    interface WildCardListener {
        fun selectRecord()
        fun confirm(dataList: List<WildcardBean>)
    }
}