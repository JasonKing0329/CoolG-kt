package com.king.app.coolg_kt.page.match.draw

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.conf.MatchConstants
import com.king.app.coolg_kt.databinding.AdapterMatchRecordBinding
import com.king.app.coolg_kt.model.extension.ImageBindingAdapter
import com.king.app.coolg_kt.page.match.DrawItem
import com.king.app.gdb.data.relation.MatchRecordWrap
import kotlin.math.abs

/**
 * @description:
 * @author：Jing
 * @date: 2021/1/10 14:38
 */
class DrawAdapter: BaseBindingAdapter<AdapterMatchRecordBinding, DrawItem>() {

    var onDrawListener: OnDrawListener? = null
    var isEditing = false

    override fun onCreateBind(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): AdapterMatchRecordBinding = AdapterMatchRecordBinding.inflate(inflater, parent, false)

    override fun onBindItem(binding: AdapterMatchRecordBinding, position: Int, bean: DrawItem) {
        var firstIndex = bean.matchItem.order * 2
        var seed1 = 0
        bean.matchRecord1?.bean?.recordSeed?.let { seed1 = it }
        var seed2 = 0
        bean.matchRecord2?.bean?.recordSeed?.let { seed2 = it }
        var seedWinner = 0
        bean.winner?.bean?.recordSeed?.let { seedWinner = it }
        binding.tvIndex1.text = (firstIndex + 1).toString()
        binding.tvIndex2.text = (firstIndex + 2).toString()

        var type1 = bean.matchRecord1?.bean?.type
        if (type1 == MatchConstants.MATCH_RECORD_BYE) {
            binding.tvBye1.visibility = View.VISIBLE
            binding.tvSeed1.text = ""
            binding.tvRank1.text = ""
            binding.tvQ1.text = ""
        }
        else {
            binding.tvBye1.visibility = View.GONE
            binding.tvSeed1.text = if (seed1 > 0) { "[$seed1]" } else { "" }
            binding.tvRank1.text = "R ${bean.matchRecord1?.bean?.recordRank}"
            binding.tvQ1.text = when(type1) {
                MatchConstants.MATCH_RECORD_QUALIFY -> "[Q]"
                MatchConstants.MATCH_RECORD_WILDCARD -> "[WC]"
                else -> ""
            }
            binding.tvQ1.visibility = when(type1) {
                MatchConstants.MATCH_RECORD_QUALIFY, MatchConstants.MATCH_RECORD_WILDCARD -> View.VISIBLE
                else -> View.GONE
            }
        }

        var type2 = bean.matchRecord2?.bean?.type
        if (type2 == MatchConstants.MATCH_RECORD_BYE) {
            binding.tvBye2.visibility = View.VISIBLE
            binding.tvSeed2.text = ""
            binding.tvRank2.text = ""
            binding.tvQ2.text = ""
        }
        else {
            binding.tvBye2.visibility = View.GONE
            binding.tvSeed2.text = if (seed2 > 0) { "[$seed2]" } else { "" }
            binding.tvRank2.text = "R ${bean.matchRecord2?.bean?.recordRank}"
            binding.tvQ2.text = when(type2) {
                MatchConstants.MATCH_RECORD_QUALIFY -> "[Q]"
                MatchConstants.MATCH_RECORD_WILDCARD -> "[WC]"
                else -> ""
            }
            binding.tvQ2.visibility = when(type2) {
                MatchConstants.MATCH_RECORD_QUALIFY, MatchConstants.MATCH_RECORD_WILDCARD -> View.VISIBLE
                else -> View.GONE
            }
        }

        var typeWinner = bean.winner?.bean?.type
        binding.tvSeedWinner.text = if (seedWinner > 0) { "[$seedWinner]" } else { "" }
        binding.tvRankWinner.text = "R ${bean.winner?.bean?.recordRank}"
        binding.tvQWinner.text = when(typeWinner) {
            MatchConstants.MATCH_RECORD_QUALIFY -> "[Q]"
            MatchConstants.MATCH_RECORD_WILDCARD -> "[WC]"
            else -> ""
        }
        binding.tvQWinner.visibility = when(typeWinner) {
            MatchConstants.MATCH_RECORD_QUALIFY, MatchConstants.MATCH_RECORD_WILDCARD -> View.VISIBLE
            else -> View.GONE
        }

        ImageBindingAdapter.setRecordUrl(binding.ivPlayer1, bean.matchRecord1?.imageUrl)
        ImageBindingAdapter.setRecordUrl(binding.ivPlayer2, bean.matchRecord2?.imageUrl)
        ImageBindingAdapter.setRecordUrl(binding.ivWinner, bean.winner?.imageUrl)

        binding.ivPlayer1.setOnClickListener { onDrawListener?.onClickPlayer(position, bean, bean.matchRecord1) }
        binding.ivPlayer2.setOnClickListener { onDrawListener?.onClickPlayer(position, bean, bean.matchRecord2) }
        binding.ivPlayer1.setOnLongClickListener {
            onDrawListener?.onPlayerWin(position, bean, bean.matchRecord1)
            true
        }
        binding.ivPlayer2.setOnLongClickListener {
            onDrawListener?.onPlayerWin(position, bean, bean.matchRecord2)
            true
        }
        binding.tvH2h.setOnClickListener { onDrawListener?.onClickH2H(position, bean) }
        binding.ivEdit1.setOnClickListener { onDrawListener?.onEditPlayer(binding.ivEdit1, position, bean, bean.matchRecord1) }
        binding.ivEdit2.setOnClickListener { onDrawListener?.onEditPlayer(binding.ivEdit1, position, bean, bean.matchRecord2) }
        binding.ivDetail1.setOnClickListener { onDrawListener?.onPlayerDetail(position, bean, bean.matchRecord1) }
        binding.ivDetail2.setOnClickListener { onDrawListener?.onPlayerDetail(position, bean, bean.matchRecord2) }

        if (isEditing) {
            binding.ivPlayer1.setOnTouchListener(ItemTouchListener(binding.ivWinner))
            binding.ivPlayer2.setOnTouchListener(ItemTouchListener(binding.ivWinner))
        }
        else {
            binding.ivPlayer1.setOnTouchListener(null)
            binding.ivPlayer2.setOnTouchListener(null)
        }
    }

    class ItemTouchListener(private val targetView: View): View.OnTouchListener {
        private var startTime = 0L
        private var startX = 0f
        private var startY = 0f
        private var targetDistance = 0
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            when(event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startTime = System.currentTimeMillis()
                    startX = event.rawX
                    startY = event.rawY
                    // 计算move to target需要移动的距离
                    val locV = IntArray(2)
                    val locT = IntArray(2)
                    v.getLocationInWindow(locV)
                    targetView.getLocationInWindow(locT)
                    val distance = locT[0] - locV[0]
                    // x方向上移动3/5就算移动到目的地
                    targetDistance = distance * 3 / 5
                    v.parent.requestDisallowInterceptTouchEvent(true)
                }
                MotionEvent.ACTION_MOVE -> {
                    var x = event.rawX
                    var y = event.rawY
                    v.translationX = x - startX
                    v.translationY = y - startY
                }
                MotionEvent.ACTION_UP -> {
                    var time = System.currentTimeMillis()
                    var x = event.rawX
                    var y = event.rawY
                    if (time - startTime <= 200 &&
                            abs(x - startX) <= 30 &&
                            abs(y - startY) <= 30) {
                        v.performClick()
                    }
                    else {
                        if (x - startX >= targetDistance &&
                                abs(y - startY) < v.height) {
                            v.performLongClick()
                        }
                    }
                    v.translationX = 0f
                    v.translationY = 0f
                    v.parent.requestDisallowInterceptTouchEvent(false)
                }
                MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_OUTSIDE -> {
                    v.parent.requestDisallowInterceptTouchEvent(false)
                }
            }
            return true
        }
    }

    interface OnDrawListener {
        fun onClickPlayer(position: Int, drawItem: DrawItem, bean: MatchRecordWrap?)
        fun onEditPlayer(anchorView: View, position: Int, drawItem: DrawItem, bean: MatchRecordWrap?)
        fun onPlayerDetail(position: Int, drawItem: DrawItem, bean: MatchRecordWrap?)
        fun onPlayerWin(position: Int, drawItem: DrawItem, bean: MatchRecordWrap?)
        fun onClickH2H(position: Int, drawItem: DrawItem)
    }
}