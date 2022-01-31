package com.king.app.coolg_kt.page.match.detail

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.king.app.coolg_kt.utils.DebugLog

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2020/12/28 13:22
 */
class DetailPagerAdapter(activity: FragmentActivity, val list: List<AbsDetailChildFragment<*, *>>): FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = list.size

    override fun createFragment(position: Int): Fragment {
        DebugLog.e("$position")
        return list[position]
    }
}