package com.king.app.coolg_kt.page.record.popup

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.TextView
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.conf.PreferenceValue
import com.king.app.coolg_kt.databinding.FragmentDialogSortRecordBinding
import com.king.app.coolg_kt.utils.ScreenUtils
import com.king.app.coolg_kt.view.dialog.DraggableContentFragment

/**
 * Created by Administrator on 2018/8/11 0011.
 */
class SortDialogContent : DraggableContentFragment<FragmentDialogSortRecordBinding>() {

    private var itemAdapter = ItemAdapter()

    var mDesc = false
    var mSortType = 0
    var onSortListener: OnSortListener? = null

    private val items = arrayOf(
        SortItem("None", PreferenceValue.GDB_SR_ORDERBY_NONE)
        , SortItem(PreferenceValue.SORT_COLUMN_KEY_NAME, PreferenceValue.GDB_SR_ORDERBY_NAME)
        , SortItem(PreferenceValue.SORT_COLUMN_KEY_DATE, PreferenceValue.GDB_SR_ORDERBY_DATE)
        , SortItem(PreferenceValue.SORT_COLUMN_KEY_SCORE, PreferenceValue.GDB_SR_ORDERBY_SCORE)
        , SortItem(PreferenceValue.SORT_COLUMN_KEY_PASSION, PreferenceValue.GDB_SR_ORDERBY_PASSION)
        , SortItem(PreferenceValue.SORT_COLUMN_KEY_CUM, PreferenceValue.GDB_SR_ORDERBY_CUM)
        , SortItem(PreferenceValue.SORT_COLUMN_KEY_FEEL, PreferenceValue.GDB_SR_ORDERBY_SCOREFEEL)
        , SortItem(PreferenceValue.SORT_COLUMN_KEY_SPECIAL, PreferenceValue.GDB_SR_ORDERBY_SPECIAL)
        , SortItem(PreferenceValue.SORT_COLUMN_KEY_STAR, PreferenceValue.GDB_SR_ORDERBY_STAR)
        , SortItem(PreferenceValue.SORT_COLUMN_KEY_BODY, PreferenceValue.GDB_SR_ORDERBY_BODY)
        , SortItem(PreferenceValue.SORT_COLUMN_KEY_COCK, PreferenceValue.GDB_SR_ORDERBY_COCK)
        , SortItem(PreferenceValue.SORT_COLUMN_KEY_ASS, PreferenceValue.GDB_SR_ORDERBY_ASS)
    )
    private var textPadding = 0
    private var focusColor = 0

    override fun getBinding(inflater: LayoutInflater)
            : FragmentDialogSortRecordBinding = FragmentDialogSortRecordBinding.inflate(inflater)

    override fun initData() {
        textPadding = ScreenUtils.dp2px(20f)
        focusColor = resources.getColor(R.color.redC93437)
        itemAdapter = ItemAdapter()
        // 初始化升序/降序
        if (!mDesc) {
            mBinding!!.rbAsc.isChecked = true
        }
        // 初始化当前排序类型
        for (i in items.indices) {
            if (mSortType == items[i].value) {
                itemAdapter!!.setSelection(i)
                break
            }
        }
        mBinding!!.gridView.adapter = itemAdapter
        mBinding!!.gridView.onItemClickListener =
            AdapterView.OnItemClickListener { adapterView: AdapterView<*>?, view: View?, position: Int, l: Long ->
                itemAdapter!!.setSelection(position)
                itemAdapter!!.notifyDataSetChanged()
            }
        mBinding!!.tvOk.setOnClickListener { view: View? -> onSave() }
    }

    fun onSave() {
        if (onSortListener != null) {
            onSortListener!!.onSort(
                mBinding!!.rbDesc.isChecked,
                items[itemAdapter!!.selectedIndex].value
            )
        }
        dismissAllowingStateLoss()
    }

    private inner class SortItem(var name: String, var value: Int)

    private inner class ItemAdapter : BaseAdapter() {
        var selectedIndex = 0
            private set

        fun setSelection(position: Int) {
            selectedIndex = position
        }

        override fun getCount(): Int {
            return items.size
        }

        override fun getItem(position: Int): Any {
            return items[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(
            position: Int,
            convertView: View,
            parent: ViewGroup
        ): View {
            val textView = TextView(context)
            textView.text = items[position].name
            textView.gravity = Gravity.CENTER
            textView.setPadding(0, textPadding, 0, textPadding)
            if (position == selectedIndex) {
                textView.setBackgroundColor(focusColor)
            } else {
                textView.background = null
            }
            return textView
        }
    }

    interface OnSortListener {
        fun onSort(desc: Boolean, sortMode: Int)
    }
}