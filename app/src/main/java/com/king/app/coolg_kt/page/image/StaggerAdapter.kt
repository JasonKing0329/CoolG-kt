package com.king.app.coolg_kt.page.image

import android.view.LayoutInflater
import android.view.ViewGroup
import com.king.app.coolg_kt.databinding.AdapterImageItemStaggerBinding
import com.king.app.coolg_kt.model.bean.ImageBean

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2020/8/04 10:04
 */
class StaggerAdapter : AbsImageAdapter<AdapterImageItemStaggerBinding>() {

    override fun onCreateBind(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): AdapterImageItemStaggerBinding = AdapterImageItemStaggerBinding.inflate(inflater, parent, false)

    override fun onBindItem(
        binding: AdapterImageItemStaggerBinding,
        position: Int,
        bean: ImageBean
    ) {
        binding.bean = bean
        // 瀑布流必须给item设置具体的宽高，否则会严重错位
        var params = binding.group.layoutParams
        params.height = bean.height
        params.width = bean.width
        binding.group.layoutParams = params
        params = binding.ivImage.layoutParams
        params.height = bean.height
        params.width = bean.width
        binding.ivImage.layoutParams = params
        setCheckVisibility(binding.cbCheck)
        setImage(binding.ivImage, bean)
    }
}