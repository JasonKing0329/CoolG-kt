package com.king.app.coolg_kt.page.tv

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.databinding.AdapterServerFilesBinding
import com.king.app.coolg_kt.databinding.AdapterTvFilesBinding
import com.king.app.coolg_kt.model.http.bean.data.FileBean
import com.king.app.coolg_kt.utils.ScreenUtils

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2019/11/11 9:50
 */
class TvItemAdapter : BaseBindingAdapter<AdapterTvFilesBinding, FileBean>() {

    override fun onCreateBind(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): AdapterTvFilesBinding = AdapterTvFilesBinding.inflate(inflater, parent, false)

    override fun onBindItem(binding: AdapterTvFilesBinding, position: Int, bean: FileBean) {
        binding.bean = bean
        if (bean.isFolder) {
            binding.ivFolder.setImageResource(R.drawable.ic_baseline_folder_24)
            binding.ivFolder.visibility = View.VISIBLE
            binding.tvDate.visibility = View.GONE
            binding.tvSize.visibility = View.GONE
        } else {
            binding.ivFolder.visibility = View.GONE
            binding.tvDate.visibility = View.VISIBLE
            binding.tvSize.visibility = View.VISIBLE
        }
    }
}