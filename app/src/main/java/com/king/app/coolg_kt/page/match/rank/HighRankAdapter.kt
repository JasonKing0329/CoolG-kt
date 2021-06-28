package com.king.app.coolg_kt.page.match.rank

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.king.app.coolg_kt.base.adapter.HeadChildBindingAdapter
import com.king.app.coolg_kt.databinding.AdapterMatchTitlesGroupBinding
import com.king.app.coolg_kt.databinding.AdapterMatchTitlesItemBinding
import com.king.app.coolg_kt.model.extension.ImageBindingAdapter
import com.king.app.coolg_kt.page.match.HighRankItem
import com.king.app.coolg_kt.page.match.HighRankTitle
import com.king.app.coolg_kt.utils.DebugLog

/**
 * @description:
 * @author：Jing
 * @date: 2021/5/16 11:26
 */
class HighRankAdapter:
    HeadChildBindingAdapter<AdapterMatchTitlesGroupBinding, AdapterMatchTitlesItemBinding, HighRankTitle, HighRankItem>() {

    override val itemClass: Class<*>
        get() = HighRankItem::class.java

    var groupList: List<HighRankTitle>? = null
        set(value) {
            field = value
            createDataList()
        }

    private fun createDataList() {
        var result = mutableListOf<Any>()
        groupList?.forEach { title ->
            result.add(title)
            title.items.forEach { item -> result.add(item) }
        }
        list = result
    }

    override fun onCreateHeadBind(
        from: LayoutInflater,
        parent: ViewGroup
    ): AdapterMatchTitlesGroupBinding = AdapterMatchTitlesGroupBinding.inflate(from, parent, false)

    override fun onCreateItemBind(
        from: LayoutInflater,
        parent: ViewGroup
    ): AdapterMatchTitlesItemBinding = AdapterMatchTitlesItemBinding.inflate(from, parent, false)

    override fun onBindHead(binding: AdapterMatchTitlesGroupBinding, position: Int, head: HighRankTitle) {
        binding.tvName.text = "Top ${head.rank} (${head.items.size} records)"
    }

    override fun onBindItem(
        binding: AdapterMatchTitlesItemBinding,
        position: Int,
        item: HighRankItem
    ) {
        binding.tvRank.text = item.curRank
        binding.tvName.text = item.bean.record.name
        if (item.details == null || item.details!!.isEmpty()) {
            binding.tvDetails.visibility = View.GONE
        }
        else {
            binding.tvDetails.text = item.details
            binding.tvDetails.visibility = View.VISIBLE
        }
        ImageBindingAdapter.setRecordUrl(binding.ivRecord, item.imageUrl)
    }

    fun getSpanSize(position: Int): Int {
        return if (isHead(position)) {
            2
        } else {
            1
        }
    }

    fun notifyGroupChanged(groupIndex: Int) {
        DebugLog.e("$groupIndex")
        // group内item可能进行了重新排序，重新创建list
        createDataList()
        // 只通知group及其items所对应的位置
        list?.let {
            for (i in it.indices) {
                if (it[i] === groupList!![groupIndex]) {
                    val count = groupList!![groupIndex].items.size + 1
                    DebugLog.e("notifyItemRangeChanged $i,$count")
                    notifyItemRangeChanged(i, count)
                    return
                }
            }
        }
    }
}