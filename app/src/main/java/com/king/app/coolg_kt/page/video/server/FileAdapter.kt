package com.king.app.coolg_kt.page.video.server

import android.view.LayoutInflater
import android.view.ViewGroup
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.databinding.AdapterServerFilesBinding
import com.king.app.coolg_kt.model.http.bean.data.FileBean
import com.king.app.coolg_kt.utils.ScreenUtils

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2019/11/11 9:50
 */
class FileAdapter : BaseBindingAdapter<AdapterServerFilesBinding, FileBean>() {

    private var isTablet = ScreenUtils.isTablet()
    var onActionListener: OnActionListener? = null

    override fun onCreateBind(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): AdapterServerFilesBinding = AdapterServerFilesBinding.inflate(inflater, parent, false)

    override fun onBindItem(binding: AdapterServerFilesBinding, position: Int, bean: FileBean) {
        binding.bean = bean
        binding.isTablet = isTablet
        if (bean.isFolder) {
            binding.ivFolder.setImageResource(R.drawable.ic_folder_yellow_700_36dp)
        } else {
            binding.ivFolder.setImageResource(R.drawable.ic_play_circle_outline_3f51b5_36dp)
        }
        binding.ivOpen.setOnClickListener { onActionListener?.onOpenServer(bean) }
    }

    interface OnActionListener {
        fun onOpenServer(bean: FileBean)
    }
}