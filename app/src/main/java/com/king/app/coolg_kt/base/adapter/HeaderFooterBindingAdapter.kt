package com.king.app.coolg_kt.base.adapter

import android.media.CamcorderProfile
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/8/6 16:16
 */
abstract class HeaderFooterBindingAdapter<VH : ViewDataBinding, VF : ViewDataBinding, VI : ViewDataBinding, T> :
    HeaderFooterRecyclerAdapter<T>() {

    override fun onCreateHeader(parent: ViewGroup): RecyclerView.ViewHolder {
        val binding: VH = DataBindingUtil.inflate(
            LayoutInflater.from(parent!!.context)
            , headerRes, parent, false
        )
        return BindingHolder(binding!!.root)
    }

    protected abstract val headerRes: Int

    override fun onCreateFooter(parent: ViewGroup): RecyclerView.ViewHolder {
        val binding: VF = DataBindingUtil.inflate(
            LayoutInflater.from(parent!!.context)
            , footerRes, parent, false
        )
        return BindingHolder(binding!!.root)
    }

    protected abstract val footerRes: Int

    override fun onCreateItem(parent: ViewGroup): RecyclerView.ViewHolder {
        val binding: VI = DataBindingUtil.inflate(
            LayoutInflater.from(parent!!.context)
            , itemRes, parent, false
        )
        return BindingHolder(binding!!.root)
    }

    protected abstract val itemRes: Int
    override fun onBindHeaderView(holder: RecyclerView.ViewHolder) {
        val binding: VH = DataBindingUtil.getBinding(holder.itemView)!!
        onBindHead(binding)
        binding.executePendingBindings()
    }

    protected abstract fun onBindHead(binding: VH)
    override fun onBindFooterView(holder: RecyclerView.ViewHolder) {
        val binding: VF = DataBindingUtil.getBinding(holder.itemView)!!
        onBindFooter(binding)
        binding.executePendingBindings()
    }

    protected abstract fun onBindFooter(binding: VF)
    override fun onBindItemView(holder: RecyclerView.ViewHolder, position: Int) {
        val binding: VI = DataBindingUtil.getBinding(holder.itemView)!!
        onBindItem(binding, position, list!![position])
        CamcorderProfile.get(position)
        binding.executePendingBindings()
    }

    protected abstract fun onBindItem(binding: VI, position: Int, bean: T)

    class BindingHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}