package com.king.app.coolg_kt.page.record.popup

import android.content.DialogInterface
import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import com.king.app.coolg_kt.conf.AppConstants
import com.king.app.coolg_kt.databinding.FragmentVideoRecommendBinding
import com.king.app.coolg_kt.utils.ScreenUtils
import com.king.app.coolg_kt.view.dialog.AlertDialogFragment
import com.king.app.coolg_kt.view.dialog.DraggableContentFragment
import com.king.app.gdb.data.DataConstants

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2019/2/26 17:47
 */
class RecommendFragment : DraggableContentFragment<FragmentVideoRecommendBinding>() {
    
    var onRecommendListener: OnRecommendListener? = null
    var mBean: RecommendBean = RecommendBean()
    var isHideOnline = false
    var mFixedType: Int? = null

    override fun getBinding(inflater: LayoutInflater): FragmentVideoRecommendBinding = FragmentVideoRecommendBinding.inflate(inflater)

    override fun initData() {
        val params = mBinding.scrollView.layoutParams
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
            && !ScreenUtils.isTablet()
        ) {
            params.height = ScreenUtils.dp2px(200f)
        } else {
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT
        }
        if (mBean == null) {
            mBean = RecommendBean()
        }
        if (mFixedType != null) {
            mBean.isTypeAll = false
            mBean.isTypeTogether = false
            mBean.isTypeMulti = false
            mBean.isType3w = false
            mBean.isType1v1 = false
            when (mFixedType) {
                DataConstants.VALUE_RECORD_TYPE_1V1 -> mBean.isType1v1 = true
                DataConstants.VALUE_RECORD_TYPE_3W -> mBean.isType3w = true
                DataConstants.VALUE_RECORD_TYPE_MULTI -> mBean.isTypeMulti = true
                DataConstants.VALUE_RECORD_TYPE_LONG -> mBean.isTypeTogether = true
                else -> {
                    mBean.isTypeAll = true
                    mBean.isTypeTogether = true
                    mBean.isTypeMulti = true
                    mBean.isType3w = true
                    mBean.isType1v1 = true
                }
            }
            mBinding.cbTypeAll.isEnabled = false
            mBinding.cbType1v1.isEnabled = false
            mBinding.cbTypeTogether.isEnabled = false
            mBinding.cbTypeMulti.isEnabled = false
            mBinding.cbType3w.isEnabled = false
        }
        mBinding.bean = mBean
        mBinding.cbTypeAll.setOnCheckedChangeListener(typeListener)
        mBinding.cbType1v1.setOnCheckedChangeListener(typeListener)
        mBinding.cbType3w.setOnCheckedChangeListener(typeListener)
        mBinding.cbTypeMulti.setOnCheckedChangeListener(typeListener)
        mBinding.cbTypeTogether.setOnCheckedChangeListener(typeListener)
        mBinding.btnOften.setOnClickListener { v: View? ->
            AlertDialogFragment()
                .setItems(
                    AppConstants.RECORD_SQL_EXPRESSIONS
                ) { dialog: DialogInterface?, which: Int ->
                    appendSql(AppConstants.RECORD_SQL_EXPRESSIONS.get(which))
                }
                .show(childFragmentManager, "AlertDialogFragment")
        }
        mBinding.btnOften1v1.setOnClickListener { v: View? ->
            AlertDialogFragment()
                .setItems(
                    AppConstants.RECORD_1v1_SQL_EXPRESSIONS
                ) { dialog: DialogInterface?, which: Int ->
                    appendSql1v1(AppConstants.RECORD_1v1_SQL_EXPRESSIONS.get(which))
                }
                .show(childFragmentManager, "AlertDialogFragment")
        }
        mBinding.btnOften3w.setOnClickListener { v: View? ->
            AlertDialogFragment()
                .setItems(
                    AppConstants.RECORD_3w_SQL_EXPRESSIONS
                ) { dialog: DialogInterface?, which: Int ->
                    appendSql3w(AppConstants.RECORD_3w_SQL_EXPRESSIONS.get(which))
                }
                .show(childFragmentManager, "AlertDialogFragment")
        }
        mBinding.tvOk.setOnClickListener { v: View? ->
            mBean.sql = mBinding.etSql.text.toString().trim { it <= ' ' }
            if (mBinding.etSql1v1.visibility == View.VISIBLE) {
                mBean.sql1v1 = mBinding.etSql1v1.text.toString().trim { it <= ' ' }
            }
            if (mBinding.etSql3w.visibility == View.VISIBLE) {
                mBean.sql3w = mBinding.etSql3w.text.toString().trim { it <= ' ' }
            }
            mBean.isTypeAll = mBinding.cbTypeAll.isChecked
            mBean.isType1v1 = mBinding.cbType1v1.isChecked
            mBean.isType3w = mBinding.cbType3w.isChecked
            mBean.isTypeMulti = mBinding.cbTypeMulti.isChecked
            mBean.isTypeTogether = mBinding.cbTypeTogether.isChecked
            mBean.isOnline = mBinding.cbOnline.isChecked
            if (!isTypeChecked) {
                setAllTypeChecked()
            }
            onRecommendListener!!.onSetSql(mBean)
            dismissAllowingStateLoss()
        }
        if (isHideOnline) {
            mBinding.cbOnline.visibility = View.GONE
        }
    }

    private fun setAllTypeChecked() {
        mBean.isTypeAll = true
        mBean.isType1v1 = true
        mBean.isType3w = true
        mBean.isTypeMulti = true
        mBean.isTypeTogether = true
    }

    private val typeListener =
        CompoundButton.OnCheckedChangeListener { compoundButton: CompoundButton, isChecked: Boolean ->
            if (compoundButton === mBinding.cbTypeAll) {
                mBinding.cbTypeAll.isChecked = isChecked
                mBinding.cbType3w.isChecked = isChecked
                mBinding.cbTypeMulti.isChecked = isChecked
                mBinding.cbTypeTogether.isChecked = isChecked
                mBinding.cbType1v1.isChecked = isChecked
                mBean.isTypeAll = isChecked
                mBean.isType1v1 = isChecked
                mBean.isType3w = isChecked
                mBean.isTypeMulti = isChecked
                mBean.isTypeTogether = isChecked
                mBinding.btnOften1v1.visibility = View.GONE
                mBinding.btnOften3w.visibility = View.GONE
                mBinding.etSql1v1.visibility = View.GONE
                mBinding.etSql3w.visibility = View.GONE
            } else {
                if (!isChecked) {
                    onSubTypeChangeAllType(false)
                } else {
                    if (mBinding.cbType1v1.isChecked && mBinding.cbType3w.isChecked && mBinding.cbTypeMulti.isChecked && mBinding.cbTypeTogether.isChecked) {
                        onSubTypeChangeAllType(true)
                    }
                }
                if (compoundButton === mBinding.cbType1v1) {
                    mBean.isType1v1 = isChecked
                } else if (compoundButton === mBinding.cbType3w) {
                    mBean.isType3w = isChecked
                } else if (compoundButton === mBinding.cbTypeMulti) {
                    mBean.isTypeMulti = isChecked
                } else if (compoundButton === mBinding.cbTypeTogether) {
                    mBean.isTypeTogether = isChecked
                }
                if (mBean.isOnlyType1v1) {
                    mBinding.btnOften1v1.visibility = View.VISIBLE
                    mBinding.etSql1v1.visibility = View.VISIBLE
                } else {
                    mBinding.btnOften1v1.visibility = View.GONE
                    mBinding.etSql1v1.visibility = View.GONE
                }
                if (mBean.isOnlyType3w) {
                    mBinding.btnOften3w.visibility = View.VISIBLE
                    mBinding.etSql3w.visibility = View.VISIBLE
                } else {
                    mBinding.btnOften3w.visibility = View.GONE
                    mBinding.etSql3w.visibility = View.GONE
                }
            }
        }

    private fun onSubTypeChangeAllType(check: Boolean) {
        mBinding.cbTypeAll.setOnCheckedChangeListener(null)
        mBinding.cbTypeAll.isChecked = check
        mBean.isTypeAll = check
        mBinding.cbTypeAll.setOnCheckedChangeListener(typeListener)
    }

    private val isTypeChecked: Boolean
        private get() = (mBinding.cbType1v1.isChecked || mBinding.cbType3w.isChecked || mBinding.cbTypeMulti.isChecked
                || mBinding.cbTypeTogether.isChecked)

    private fun appendSql(condition: String) {
        var sql = mBinding.etSql.text.toString()
        sql = if (sql.isEmpty()) {
            "T.$condition"
        } else {
            "$sql AND T.$condition"
        }
        mBinding.etSql.setText(sql)
        mBinding.etSql.setSelection(sql.length)
    }

    private fun appendSql1v1(condition: String) {
        var sql = mBinding.etSql1v1.text.toString()
        sql = if (sql.isEmpty()) {
            condition
        } else {
            "$sql AND $condition"
        }
        mBinding.etSql1v1.setText(sql)
        mBinding.etSql1v1.setSelection(sql.length)
    }

    private fun appendSql3w(condition: String) {
        var sql = mBinding.etSql3w.text.toString()
        sql = if (sql.isEmpty()) {
            condition
        } else {
            "$sql AND $condition"
        }
        mBinding.etSql3w.setText(sql)
        mBinding.etSql3w.setSelection(sql.length)
    }

    interface OnRecommendListener {
        fun onSetSql(bean: RecommendBean)
    }
}