package com.king.app.coolg_kt.page.tv.popup

import android.view.LayoutInflater
import com.king.app.coolg_kt.conf.AppConstants
import com.king.app.coolg_kt.databinding.FragmentTvPlayerSettingBinding
import com.king.app.coolg_kt.model.setting.SettingProperty
import com.king.app.coolg_kt.view.dialog.TvDialogContentFragment

/**
 * @description:
 * @author：Jing
 * @date: 2021/2/19 11:03
 */
class PlayerSetting: TvDialogContentFragment<FragmentTvPlayerSettingBinding>() {

    var timeUnit = 0

    override fun getBinding(inflater: LayoutInflater): FragmentTvPlayerSettingBinding = FragmentTvPlayerSettingBinding.inflate(inflater)

    override fun initData() {
        timeUnit = SettingProperty.getForwardUnit()
        mBinding.tvTime.text = AppConstants.timeParams[timeUnit]

        mBinding.cbLast.isChecked = SettingProperty.isRememberTvPlayTime()
        mBinding.ivTimePlus.setOnClickListener {
            val target = timeUnit + 1
            if (target < AppConstants.timeParamValues.size) {
                timeUnit = target
                mBinding.tvTime.text = AppConstants.timeParams[timeUnit]
            }
        }
        mBinding.ivTimeReduce.setOnClickListener {
            val target = timeUnit - 1
            if (target >= 0) {
                timeUnit = target
                mBinding.tvTime.text = AppConstants.timeParams[timeUnit]
            } }
    }

    override fun onCancel(): Boolean = false

    override fun onConfirm(): Boolean {
        SettingProperty.setForwardUnit(timeUnit)
        SettingProperty.setRememberTvPlayTime(mBinding.cbLast.isChecked)
        return false
    }
}