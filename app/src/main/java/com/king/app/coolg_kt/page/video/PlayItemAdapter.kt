package com.king.app.coolg_kt.page.video

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.databinding.AdapterPlayItemBinding
import com.king.app.coolg_kt.model.bean.PlayItemViewBean
import com.king.app.coolg_kt.model.extension.ImageBindingAdapter
import com.king.app.coolg_kt.view.widget.video.OnPlayEmptyUrlListener

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/1/4 10:50
 */
class PlayItemAdapter: BaseBindingAdapter<AdapterPlayItemBinding, PlayItemViewBean>() {

    var enableDelete = true

    var onPlayItemListener: OnPlayItemListener? = null

    var onPlayEmptyUrlListener: OnPlayEmptyUrlListener? = null

    var itemHeight: Int = 0

    override fun onCreateBind(inflater: LayoutInflater, parent: ViewGroup): AdapterPlayItemBinding
        = AdapterPlayItemBinding.inflate(inflater, parent, false)

    override fun onBindItem(
        binding: AdapterPlayItemBinding,
        position: Int,
        bean: PlayItemViewBean
    ) {
        var param = binding.videoView.layoutParams
        param.height = itemHeight
        binding.videoView.layoutParams = param

        binding.tvName.text = bean.record.bean.name
        binding.ivDelete.visibility = if (enableDelete) View.VISIBLE else View.GONE
        binding.videoView.setUp(bean.playUrl, "")
        binding.videoView.setIndexInList(position)
        binding.videoView.setOnPlayEmptyUrlListener(onPlayEmptyUrlListener)
        ImageBindingAdapter.setRecordUrl(binding.videoView.posterImageView, bean.cover)
        binding.ivDelete.setOnClickListener { onPlayItemListener?.onDeleteItem(position, bean) }
        binding.ivPlay.setOnClickListener { onPlayItemListener?.onPlayItem(position, bean) }
    }

    interface OnPlayItemListener {
        fun onPlayItem(position: Int, bean: PlayItemViewBean)
        fun onDeleteItem(position: Int, bean: PlayItemViewBean)
    }
}