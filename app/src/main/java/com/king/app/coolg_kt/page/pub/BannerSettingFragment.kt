package com.king.app.coolg_kt.page.pub

import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.widget.CompoundButton
import com.king.app.coolg_kt.databinding.FragmentBannerSettingBinding
import com.king.app.coolg_kt.utils.BannerHelper.BannerParams
import com.king.app.coolg_kt.view.dialog.AlertDialogFragment
import com.king.app.coolg_kt.view.dialog.DraggableContentFragment
import com.king.lib.banner.BannerFlipStyleProvider

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/8/8 15:34
 */
class BannerSettingFragment : DraggableContentFragment<FragmentBannerSettingBinding>() {
    
    var onAnimSettingListener: OnAnimSettingListener? = null
    val isHideAnimType = false
    var params = BannerParams()
    
    override fun getBinding(inflater: LayoutInflater): FragmentBannerSettingBinding = FragmentBannerSettingBinding.inflate(inflater)

    override fun initData() {
        mBinding.rbFixed.setOnClickListener { showAnimationSelector() }
        mBinding.rbRandom.setOnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            params.isRandom = isChecked
            if (isChecked) {
                onAnimSettingListener?.onParamsUpdated(params)
            }
        }
        mBinding.rbFixed.setOnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            params.isRandom = false
            if (isChecked) {
                onAnimSettingListener?.onParamsUpdated(params)
            }
        }
        if (params.isRandom) {
            mBinding.rbRandom.isChecked = true
            mBinding.rbFixed.text = "Fixed"
        } else {
            mBinding.rbFixed.isChecked = true
            try {
                mBinding.rbFixed.text = formatFixedText(
                    BannerFlipStyleProvider.ANIM_TYPES[params.type]
                )
            } catch (e: Exception) {
                e.printStackTrace()
                mBinding.rbFixed.text = formatFixedText(BannerFlipStyleProvider.ANIM_TYPES[0])
            }
        }
        mBinding.etTime.setText(params.duration.toString())
        mBinding.tvOk.setOnClickListener { v: View? -> onSave() }
        if (isHideAnimType) {
            mBinding.groupAnim.visibility = View.GONE
            mBinding.tvAnimTitle.visibility = View.GONE
        }
    }

    private fun formatFixedText(type: String): String {
        return "Fixed ($type)"
    }

    private fun showAnimationSelector() {
        AlertDialogFragment()
            .setTitle(null)
            .setItems(BannerFlipStyleProvider.ANIM_TYPES) { dialog: DialogInterface?, which: Int ->
                mBinding.rbFixed.text = formatFixedText(
                    BannerFlipStyleProvider.ANIM_TYPES[which]
                )
                params.type = which
                onAnimSettingListener?.onParamsUpdated(params)
            }.show(childFragmentManager, "AlertDialogFragment")
    }

    private fun onSave() {
        var time = try {
            mBinding.etTime.text.toString().toInt()
        } catch (e: Exception) { 0 }
        // 至少2S
        if (time < 2000) {
            time = 2000
        }
        params.duration = time
        onAnimSettingListener?.onParamsSaved(params)
        dismissAllowingStateLoss()
    }

    interface OnAnimSettingListener {
        fun onParamsUpdated(params: BannerParams)
        fun onParamsSaved(params: BannerParams)
    }
}