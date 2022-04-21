package com.king.app.coolg_kt.page.star.phone

import android.view.View
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.adapter.HeaderFooterBindingAdapter
import com.king.app.coolg_kt.databinding.AdapterRecordItemGridBinding
import com.king.app.coolg_kt.databinding.AdapterStarPhoneFooterBinding
import com.king.app.coolg_kt.databinding.AdapterStarPhoneHeaderBinding
import com.king.app.coolg_kt.page.record.RecordItemGridBinder
import com.king.app.gdb.data.entity.Star
import com.king.app.gdb.data.entity.Tag
import com.king.app.gdb.data.relation.RecordWrap
import com.king.app.gdb.data.relation.StarRelationship
import com.king.app.gdb.data.relation.StarStudioTag

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/8/6 16:34
 */
class StarAdapter : HeaderFooterBindingAdapter<AdapterStarPhoneHeaderBinding, AdapterStarPhoneFooterBinding, AdapterRecordItemGridBinding, RecordWrap>() {
    var onListListener: OnListListener? = null
    var onHeadActionListener: StarHeader.OnHeadActionListener? = null
    private val header = StarHeader()
    private val recordBinder = RecordItemGridBinder()
    var mSortMode = 0
    lateinit var star: Star
    var mRelationships: MutableList<StarRelationship> = mutableListOf()
    var mStudioList: List<StarStudioTag> = listOf()
    var mTagList: List<Tag> = listOf()

    override val headerRes = R.layout.adapter_star_phone_header

    override val footerRes = R.layout.adapter_star_phone_footer

    override val itemRes = R.layout.adapter_record_item_grid

    override fun onBindHead(binding: AdapterStarPhoneHeaderBinding) {
        header.onHeadActionListener = onHeadActionListener
        header.bind(binding, star, mRelationships, mStudioList, mTagList)
    }

    override fun onBindFooter(binding: AdapterStarPhoneFooterBinding) {}
    override fun onBindItem(
        binding: AdapterRecordItemGridBinding,
        position: Int,
        record: RecordWrap
    ) {
        record.canSelect = true
        recordBinder.mSortMode = mSortMode
        recordBinder.bind(binding, position, record)
        binding.root.setOnClickListener {
                onListListener?.onClickItem(it, record)
            }
    }

    fun onResume() {}
    fun onStop() {}

    fun getSpanSize(position: Int): Int {
        return when {
            isItem(position) -> 1
            else -> 2
        }
    }

    interface OnListListener {
        fun onClickItem(view: View, record: RecordWrap)
    }
}