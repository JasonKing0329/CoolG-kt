package com.king.app.coolg_kt.page.match.detail

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.adapter.HeadChildBindingAdapter
import com.king.app.coolg_kt.databinding.AdapterMatchMajorRankBinding
import com.king.app.coolg_kt.databinding.AdapterMatchMajorRoundBinding
import com.king.app.coolg_kt.page.match.MajorRank
import com.king.app.coolg_kt.page.match.MajorRound

class MajorAdapter: HeadChildBindingAdapter<AdapterMatchMajorRankBinding, AdapterMatchMajorRoundBinding, MajorRank, MajorRound>() {
    override val itemClass: Class<*>
        get() = MajorRound::class.java

    override fun onCreateHeadBind(
        from: LayoutInflater,
        parent: ViewGroup
    ): AdapterMatchMajorRankBinding = AdapterMatchMajorRankBinding.inflate(from, parent, false)

    override fun onCreateItemBind(
        from: LayoutInflater,
        parent: ViewGroup
    ): AdapterMatchMajorRoundBinding = AdapterMatchMajorRoundBinding.inflate(from, parent, false)

    override fun onBindHead(binding: AdapterMatchMajorRankBinding, position: Int, head: MajorRank) {
        binding.bean = head
        if (head.major.isEmpty()) {
            binding.tvMajor.visibility = View.GONE
        }
        else {
            updateBackground(binding.tvMajor, head.majorColor)
            binding.tvMajor.visibility = View.VISIBLE
        }
        if (head.majorHigh.isEmpty()) {
            binding.tvMajorHigh.visibility = View.GONE
        }
        else {
            updateBackground(binding.tvMajorHigh, binding.tvMajorHigh.resources.getColor(R.color.bg_top1))
            binding.tvMajorHigh.visibility = View.VISIBLE
        }
    }

    private fun updateBackground(view: View, color: Int) {
        val background = view.background as GradientDrawable
        background.setColor(color)
        view.background = background
    }

    override fun onBindItem(
        binding: AdapterMatchMajorRoundBinding,
        position: Int,
        item: MajorRound
    ) {
        binding.bean = item
        updateBackground(binding.tvMajorLevel, item.majorColor)
        if (item.major.isEmpty()) {
            binding.tvMajorLevel.visibility = View.GONE
        }
        else {
            updateBackground(binding.tvMajorLevel, item.majorColor)
            binding.tvMajorLevel.visibility = View.VISIBLE
        }
        if (item.majorRound.isEmpty()) {
            binding.tvMajorRound.visibility = View.GONE
        }
        else {
            updateBackground(binding.tvMajorRound, item.majorColor)
            binding.tvMajorRound.visibility = View.VISIBLE
        }
    }
}