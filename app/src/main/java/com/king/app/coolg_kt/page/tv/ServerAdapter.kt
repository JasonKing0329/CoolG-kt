package com.king.app.coolg_kt.page.tv

import android.view.LayoutInflater
import android.view.ViewGroup
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.databinding.AdapterTvServerBinding
import com.king.app.coolg_kt.model.udp.ServerBody

/**
 * @description:
 * @author：Jing
 * @date: 2021/2/15 23:17
 */
class ServerAdapter: BaseBindingAdapter<AdapterTvServerBinding, ServerBody>() {

    override fun onCreateBind(inflater: LayoutInflater, parent: ViewGroup): AdapterTvServerBinding =
        AdapterTvServerBinding.inflate(inflater, parent, false)

    override fun onBindItem(binding: AdapterTvServerBinding, position: Int, bean: ServerBody) {
        binding.tvName.text = bean.serverName
        binding.tvIp.text = bean.ip
        if (bean.isOnline) {
            binding.tvOnline.text = "online"
            binding.tvOnline.setTextColor(binding.tvOnline.resources.getColor(R.color.yellowF7D23E))
        }
        else {
            binding.tvOnline.text = "offline"
            binding.tvOnline.setTextColor(binding.tvOnline.resources.getColor(R.color.white))
        }
    }
}