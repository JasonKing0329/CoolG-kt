package com.king.app.coolg_kt.page.star.random

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.CompoundButton
import com.king.app.coolg_kt.databinding.FragmentDialogRandomSettingBinding
import com.king.app.coolg_kt.view.dialog.DraggableContentFragment

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2020/9/7 15:32
 */
class RandomSettingFragment : DraggableContentFragment<FragmentDialogRandomSettingBinding>() {
    
    var randomRule = RandomRule()
    var onSettingListener: OnSettingListener? = null
    
    override fun getBinding(inflater: LayoutInflater): FragmentDialogRandomSettingBinding = FragmentDialogRandomSettingBinding.inflate(inflater)
    
    override fun initData() {
        mBinding.cbRating.setOnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            mBinding.groupRating.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
        mBinding.cbExclude.isChecked = randomRule.isExcludeFromMarked
        mBinding.rbAll.isChecked = randomRule.starType == 0
        mBinding.rbTop.isChecked = randomRule.starType == 1
        mBinding.rbBottom.isChecked = randomRule.starType == 2
        mBinding.rbHalf.isChecked = randomRule.starType == 3
        val isJoinRating = !TextUtils.isEmpty(randomRule.sqlRating)
        mBinding.cbRating.isChecked = isJoinRating
        mBinding.groupRating.visibility = if (isJoinRating) View.VISIBLE else View.GONE
        if (isJoinRating) {
            mBinding.etSqlRating.setText(randomRule.sqlRating)
        }
        mBinding.tvOk.setOnClickListener { v: View? -> saveSetting() }
    }

    private fun saveSetting() {
        randomRule.isExcludeFromMarked = mBinding.cbExclude.isChecked
        when {
            mBinding.rbAll.isChecked -> {
                randomRule.starType = 0
            }
            mBinding.rbTop.isChecked -> {
                randomRule.starType = 1
            }
            mBinding.rbBottom.isChecked -> {
                randomRule.starType = 2
            }
            mBinding.rbHalf.isChecked -> {
                randomRule.starType = 3
            }
        }
        if (mBinding.cbRating.isChecked) {
            randomRule.sqlRating = mBinding.etSqlRating.text.toString()
        } else {
            randomRule.sqlRating = null
        }
        onSettingListener?.onSetRule(randomRule)
        dismissAllowingStateLoss()
    }

    interface OnSettingListener {
        fun onSetRule(randomRule: RandomRule)
    }
}