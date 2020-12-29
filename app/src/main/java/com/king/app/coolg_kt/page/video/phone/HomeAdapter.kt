package com.king.app.coolg_kt.page.video.phone

import android.view.View
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.adapter.HeaderFooterBindingAdapter
import com.king.app.coolg_kt.databinding.AdapterFooterMoreBinding
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
class HomeAdapter :
    HeaderFooterBindingAdapter<AdapterVideoHeadBinding, AdapterFooterMoreBinding, AdapterVideoHomeItemBinding, PlayItemViewBean>() {
    var onListListener: OnListListener? = null
    var onHeadActionListener: OnHeadActionListener? = null
    var headData = VideoHeadData()
    var onPlayEmptyUrlListener: OnPlayEmptyUrlListener? = null
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd")

    override val headerRes: Int
        get() = R.layout.adapter_video_head

    override val footerRes: Int
        get() = R.layout.adapter_footer_more

    override val itemRes: Int
        get() = R.layout.adapter_video_home_item

    override fun onBindHead(binding: AdapterVideoHeadBinding) {
        binding.data = headData
        binding.ivRefreshGuys.setOnClickListener { onHeadActionListener?.onRefreshGuy() }
        binding.tvGuys.setOnClickListener {
            onHeadActionListener?.onGuy()
        }
        binding.ivStar0.setOnClickListener {
            onHeadActionListener?.onClickGuy(headData.getGuy(0)!!)
        }
        binding.ivStar1.setOnClickListener {
            onHeadActionListener?.onClickGuy(headData.getGuy(1)!!)
        }
        binding.ivStar2.setOnClickListener {
            onHeadActionListener?.onClickGuy(headData.getGuy(2)!!)
        }
        binding.ivStar3.setOnClickListener {
            onHeadActionListener?.onClickGuy(headData.getGuy(3)!!)
        }
        binding.ivSetPlayList.setOnClickListener { onHeadActionListener?.onSetPlayList() }
        binding.tvPlayList.setOnClickListener { onHeadActionListener?.onPlayList() }
        binding.ivList0.setOnClickListener {
            onHeadActionListener?.onClickPlayList(headData.getPlayList(0)!!)
        }
        binding.ivList1.setOnClickListener {
            onHeadActionListener?.onClickPlayList(headData.getPlayList(1)!!)
        }
        binding.ivList2.setOnClickListener {
            onHeadActionListener?.onClickPlayList(headData.getPlayList(2)!!)
        }
        binding.ivList3.setOnClickListener {
            onHeadActionListener?.onClickPlayList(headData.getPlayList(3)!!)
        }
    }

    override fun onBindFooter(binding: AdapterFooterMoreBinding) {
        binding.groupMore.setOnClickListener {
            onListListener?.onLoadMore()
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
        if (position == 0 || isNotSameDay(bean.record, list!![position - 1].record)) {
            binding.tvDate.visibility = View.VISIBLE
            binding.tvDate.text = dateFormat.format(Date(bean.record.bean.lastModifyTime))
        } else {
            binding.tvDate.visibility = View.GONE
        }
        bean.record.countRecord?.let {
            binding.tvRank.text = "R-${it.rank}"
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

}