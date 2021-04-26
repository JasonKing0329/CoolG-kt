package com.king.app.coolg_kt.page.match.item

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.databinding.AdapterMatchDetailItemBinding
import com.king.app.coolg_kt.model.extension.ImageBindingAdapter
import com.king.app.coolg_kt.page.match.MatchSemiItem
import com.king.app.coolg_kt.page.match.MatchSemiPack
import com.king.app.coolg_kt.view.widget.SemiGroup

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/4/26 14:42
 */
class MatchSemiAdapter: BaseBindingAdapter<AdapterMatchDetailItemBinding, MatchSemiPack>() {

    var onRecordListener: OnRecordListener? = null

    override fun onCreateBind(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): AdapterMatchDetailItemBinding = AdapterMatchDetailItemBinding.inflate(inflater, parent, false)

    override fun onBindItem(
        binding: AdapterMatchDetailItemBinding,
        position: Int,
        bean: MatchSemiPack
    ) {
        binding.tvPeriod.text = bean.period
        binding.tvDate.text = bean.date
        binding.clTop.setOnClickListener { listenerClick?.onClickItem(binding.clTop, position, bean) }
        createSemiAdapter(binding.semiGroup, bean.items)
    }

    private fun createSemiAdapter(semiGroup: SemiGroup, items: List<MatchSemiItem>) {
        if (items.size < 4) {
            semiGroup.visibility = View.GONE
        }
        else {
            semiGroup.visibility = View.VISIBLE
            val adapter = object : SemiGroup.SemiAdapter() {
                override fun getView(position: Int): View {
                    val view = LayoutInflater.from(semiGroup.context).inflate(R.layout.layout_match_semi_item, null)
                    view.findViewById<TextView>(R.id.tv_title).text = items[position].rank
                    val ivImage = view.findViewById<ImageView>(R.id.iv_image)
                    ivImage.setOnClickListener { onRecordListener?.onClickRecord(items[position]) }
                    ImageBindingAdapter.setRecordUrl(ivImage, items[position].imageUrl)
                    val ivCup = view.findViewById<ImageView>(R.id.iv_cup)
                    ivCup.visibility = if (position == 0) View.VISIBLE else View.GONE
                    return view
                }
            }
            semiGroup.setAdapter(adapter)
        }
    }

    /**
     * 点击事件交给clTop
     */
    override fun onClickItem(v: View, position: Int, bean: MatchSemiPack) {

    }

    interface OnRecordListener {
        fun onClickRecord(semiItem: MatchSemiItem)
    }
}