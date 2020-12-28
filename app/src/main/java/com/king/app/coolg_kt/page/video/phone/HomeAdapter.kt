package com.king.app.coolg_kt.page.video.phone

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.king.app.coolg_kt.base.adapter.HeadChildBindingAdapter
import com.king.app.coolg_kt.databinding.AdapterVideoHeadBinding
import com.king.app.coolg_kt.databinding.AdapterVideoHomeItemBinding
import com.king.app.coolg_kt.model.bean.PlayItemViewBean
import com.king.app.coolg_kt.model.bean.VideoGuy
import com.king.app.coolg_kt.model.bean.VideoPlayList
import com.king.app.coolg_kt.model.extension.ImageBindingAdapter
import com.king.app.coolg_kt.page.video.VideoHeadData
import com.king.app.coolg_kt.view.widget.video.OnPlayEmptyUrlListener
import com.king.app.gdb.data.relation.RecordWrap
import java.text.SimpleDateFormat
import java.util.*

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2019/2/22 16:20
 */
class HomeAdapter : HeadChildBindingAdapter<AdapterVideoHeadBinding, AdapterVideoHomeItemBinding, VideoHeadData, PlayItemViewBean>() {
    var onListListener: OnListListener? = null
    var onHeadActionListener: OnHeadActionListener? = null
    var onPlayEmptyUrlListener: OnPlayEmptyUrlListener? = null
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd")
    
    override fun onCreateHeadBind(
        from: LayoutInflater,
        parent: ViewGroup
    ): AdapterVideoHeadBinding = AdapterVideoHeadBinding.inflate(from, parent, false)

    override fun onCreateItemBind(
        from: LayoutInflater,
        parent: ViewGroup
    ): AdapterVideoHomeItemBinding = AdapterVideoHomeItemBinding.inflate(from, parent, false)

    override fun onBindHead(binding: AdapterVideoHeadBinding, position: Int, head: VideoHeadData) {
        binding.data = head
        binding.ivRefreshGuys.setOnClickListener { onHeadActionListener?.onRefreshGuy() }
        binding.tvGuys.setOnClickListener {
            onHeadActionListener?.onGuy()
        }
        binding.ivStar0.setOnClickListener {
            onHeadActionListener?.onClickGuy(head.getGuy(0)!!)
        }
        binding.ivStar1.setOnClickListener { 
            onHeadActionListener?.onClickGuy(head.getGuy(1)!!)
        }
        binding.ivStar2.setOnClickListener {
            onHeadActionListener?.onClickGuy(head.getGuy(2)!!)
        }
        binding.ivStar3.setOnClickListener {
            onHeadActionListener?.onClickGuy(head.getGuy(3)!!)
        }
        binding.ivSetPlayList.setOnClickListener { onHeadActionListener?.onSetPlayList() }
        binding.tvPlayList.setOnClickListener { onHeadActionListener?.onPlayList() }
        binding.ivList0.setOnClickListener {
            onHeadActionListener?.onClickPlayList(head.getPlayList(0)!!)
        }
        binding.ivList1.setOnClickListener {
            onHeadActionListener?.onClickPlayList(head.getPlayList(1)!!)
        }
        binding.ivList2.setOnClickListener {
            onHeadActionListener?.onClickPlayList(head.getPlayList(2)!!)
        }
        binding.ivList3.setOnClickListener {
            onHeadActionListener?.onClickPlayList(head.getPlayList(3)!!)
        }
    }
    
    override fun onBindItem(binding: AdapterVideoHomeItemBinding, position: Int, bean: PlayItemViewBean) {
        binding.bean = bean
        ImageBindingAdapter.setRecordUrl(binding.videoView.posterImageView, bean.cover)
        binding.videoView.setOnPlayEmptyUrlListener(onPlayEmptyUrlListener)
        binding.videoView.setUp(bean.playUrl, "")
        binding.ivAdd.setOnClickListener {
            onListListener?.onAddToVideoOrder(bean)
        }
        binding.tvName.setOnClickListener {
            onListListener?.onClickItem(position, bean)
        }

        // 第一个位置以及与上一个位置日期不同的，显示日期
        if (position == 1 || isNotSameDay(bean.record!!, (list!![position - 1]!! as PlayItemViewBean).record!!)) {
            binding.tvDate.visibility = View.VISIBLE
            binding.tvDate.text = dateFormat.format(Date(bean.record!!.bean.lastModifyTime))
        } else {
            binding.tvDate.visibility = View.GONE
        }
        if (bean.record!!.countRecord != null) {
            binding.tvRank.text = "R-" + bean.record!!.countRecord!!.rank
        }
    }

    private fun isNotSameDay(curRecord: RecordWrap, lastRecord: RecordWrap): Boolean {
        val curDay = dateFormat.format(Date(curRecord.bean.lastModifyTime))
        val lastDay = dateFormat.format(Date(lastRecord.bean.lastModifyTime))
        return curDay != lastDay
    }

    interface OnListListener {
        fun onLoadMore()
        fun onClickItem(position: Int, bean: PlayItemViewBean)
        fun onAddToVideoOrder(bean: PlayItemViewBean)
    }

    interface OnHeadActionListener {
        fun onSetPlayList()
        fun onPlayList()
        fun onClickPlayList(order: VideoPlayList)
        fun onRefreshGuy()
        fun onGuy()
        fun onClickGuy(guy: VideoGuy)
    }

    override val itemClass: Class<*>
        get() = PlayItemViewBean::class.java
}