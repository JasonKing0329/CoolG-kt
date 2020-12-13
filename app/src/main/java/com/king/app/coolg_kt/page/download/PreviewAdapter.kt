package com.king.app.coolg_kt.page.download

import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.databinding.AdapterDownloadPreviewItemBinding
import com.king.app.coolg_kt.model.GlideApp
import com.king.app.coolg_kt.model.http.bean.data.DownloadItem

/**
 * @description:
 * @author：Jing
 * @date: 2020/12/13 14:12
 */
class PreviewAdapter: BaseBindingAdapter<AdapterDownloadPreviewItemBinding, DownloadItem>() {

    private val checkMap: SparseBooleanArray = SparseBooleanArray()

    override fun onCreateBind(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): AdapterDownloadPreviewItemBinding = AdapterDownloadPreviewItemBinding.inflate(inflater, parent, false)

    override fun onBindItem(
        binding: AdapterDownloadPreviewItemBinding,
        position: Int,
        bean: DownloadItem
    ) {
        if (bean.key != null) {
            binding.tvName.text = "${bean.key}/${bean.name}"
        } else {
            binding.tvName.text = bean.name
        }
        binding.cbCheck.isChecked = checkMap[position]

        GlideApp.with(binding.ivImage.context)
            .load(bean.path)
            .error(R.drawable.def_small)
            .into(binding.ivImage)
    }

    override fun onClickItem(v: View, position: Int, bean: DownloadItem) {

        if (checkMap[position]) {
            checkMap.put(position, false)
        } else {
            checkMap.put(position, true)
        }
        notifyItemChanged(position)
    }

    fun getCheckedItems(): List<DownloadItem> {
        var result = list?.filterIndexed { index, downloadItem -> checkMap[index] }
        return result ?: listOf()
    }

    fun selectAll() {
        list?.forEachIndexed { index, downloadItem -> checkMap.put(index, true) }
    }

    fun unSelectAll() {
        list?.forEachIndexed { index, downloadItem -> checkMap.put(index, false) }
    }

}