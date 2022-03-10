package com.king.app.coolg_kt.page.star.timeline

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.databinding.AdapterStarTimelineBinding
import com.king.app.coolg_kt.model.bean.TimelineStar
import com.king.app.coolg_kt.utils.ColorUtil
import com.king.app.coolg_kt.utils.ScreenUtils
import com.king.app.gdb.data.DataConstants

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2022/3/8 13:36
 */
class StarTimelineAdapter: BaseBindingAdapter<AdapterStarTimelineBinding, TimelineStar>() {

    private val imageMargin = ScreenUtils.dp2px(4f)
    private val nameMargin = ScreenUtils.dp2px(4f)
    private val dateMargin = ScreenUtils.dp2px(4f)

    var isEditing = false
    var isShowHidden = false

    var onHiddenChangedListener: OnHiddenChangedListener? = null

    override fun onCreateBind(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): AdapterStarTimelineBinding = AdapterStarTimelineBinding.inflate(inflater, parent, false)

    override fun onBindItem(
        binding: AdapterStarTimelineBinding,
        position: Int,
        bean: TimelineStar
    ) {
        updateConstraint(binding, bean)
        binding.bean = bean
        binding.tvVideos.text = "${bean.star.records} videos"

        if (isEditing || isShowHidden && bean.isHidden) {
            binding.ivHide.visibility = View.VISIBLE
            if (bean.isHidden) {
                ColorUtil.updateIconColor(binding.ivHide, binding.ivHide.resources.getColor(R.color.redC93437))
            }
            else {
                ColorUtil.updateIconColor(binding.ivHide, binding.ivHide.resources.getColor(R.color.grey_600))
            }
        }
        else {
            binding.ivHide.visibility = View.GONE
        }

        binding.ivImage.setOnClickListener { listenerClick?.onClickItem(it, position, bean) }
        binding.tvName.setOnClickListener { listenerClick?.onClickItem(it, position, bean) }
        binding.ivHide.setOnClickListener { onHiddenChangedListener?.onHiddenChanged(position, bean.star.id!!, !bean.isHidden) }
    }

    private fun updateConstraint(binding: AdapterStarTimelineBinding, bean: TimelineStar) {
        val imageParam = binding.ivImage.layoutParams as ConstraintLayout.LayoutParams
        val nameParam = binding.groupName.layoutParams as ConstraintLayout.LayoutParams
        val dateParam = binding.tvDate.layoutParams as ConstraintLayout.LayoutParams
        // 重置水平方向的约束
        resetHor(imageParam)
        resetHor(nameParam)
        resetHor(dateParam)
        // 根据type修改约束
        when(bean.type) {
            DataConstants.VALUE_RELATION_BOTTOM -> {
                imageParam.startToEnd = binding.divider.id
                nameParam.startToEnd = binding.ivImage.id
                dateParam.endToStart = binding.divider.id
                imageParam.marginStart = imageMargin
                nameParam.marginStart = nameMargin
                dateParam.marginEnd = dateMargin
                binding.groupName.gravity = Gravity.LEFT
            }
            DataConstants.VALUE_RELATION_MIX -> {
                imageParam.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                imageParam.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                nameParam.startToEnd = binding.ivImage.id
                dateParam.endToStart = binding.ivImage.id
                nameParam.marginStart = nameMargin
                dateParam.marginEnd = dateMargin
                binding.groupName.gravity = Gravity.LEFT
            }
            else -> {
                imageParam.endToStart = binding.divider.id
                nameParam.endToStart = binding.ivImage.id
                dateParam.startToEnd = binding.divider.id
                imageParam.marginEnd = imageMargin
                nameParam.marginEnd = nameMargin
                dateParam.marginStart = dateMargin
                binding.groupName.gravity = Gravity.RIGHT
            }
        }
        binding.ivImage.layoutParams = imageParam
        binding.groupName.layoutParams = nameParam
        binding.tvDate.layoutParams = dateParam
    }

    /**
     * 只重置horizontal方向上的约束
     */
    private fun resetHor(param: ConstraintLayout.LayoutParams) {
        val topToTop = param.topToTop
        val bottomToBottom = param.bottomToBottom
        param.startToStart = -1
        param.startToEnd = -1
        param.endToEnd = -1
        param.endToStart = -1
        param.marginStart = 0
        param.marginEnd = 0
        param.topToTop = topToTop
        param.bottomToBottom = bottomToBottom
    }

    override fun onClickItem(v: View, position: Int, bean: TimelineStar) {
        // 覆盖父类的整体点击事件，将onClickItem只赋给image和name
    }

    interface OnHiddenChangedListener {
        fun onHiddenChanged(position: Int, starId: Long, hidden: Boolean)
    }
}