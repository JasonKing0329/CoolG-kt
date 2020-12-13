package com.king.app.coolg_kt.page.download

import android.view.LayoutInflater
import android.view.ViewGroup
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.databinding.AdapterDownloadListBinding
import com.king.app.coolg_kt.model.bean.DownloadItemProxy
import com.king.app.coolg_kt.utils.FileSizeUtil

/**
 * @description:
 * @author：Jing
 * @date: 2020/12/13 14:21
 */
class ListAdapter: BaseBindingAdapter<AdapterDownloadListBinding, DownloadItemProxy>() {
    override fun onCreateBind(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): AdapterDownloadListBinding = AdapterDownloadListBinding.inflate(inflater, parent, false)

    override fun onBindItem(
        binding: AdapterDownloadListBinding,
        position: Int,
        bean: DownloadItemProxy
    ) {
        if (bean.item.key != null) {
            binding.tvName.text = "${bean.item.key}/${bean.item.name}"
        } else {
            binding.tvName.text = bean.item.name
        }
        binding.tvSize.text = FileSizeUtil.convertFileSize(bean.item.size)
        binding.progress.progress = bean.progress
    }
}