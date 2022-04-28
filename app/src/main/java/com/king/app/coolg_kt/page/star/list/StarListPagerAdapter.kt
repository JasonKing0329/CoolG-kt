package com.king.app.coolg_kt.page.star.list

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2020/12/28 13:22
 */
@Deprecated("")
class StarListPagerAdapter(activity: FragmentActivity, val list: List<StarListFragment>): FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = list.size

    override fun createFragment(position: Int): Fragment {
        return list[position]
    }

    fun onViewModeChanged() {
        list.forEach { it.onViewModeChanged() }
    }
}