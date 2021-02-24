package com.king.app.coolg_kt.page.match.detail

import android.view.LayoutInflater
import android.view.View
import com.king.app.coolg_kt.base.EmptyViewModel
import com.king.app.coolg_kt.databinding.FragmentMatchDetailChampionBinding

/**
 * @description:
 * @author：Jing
 * @date: 2021/2/24 16:36
 */
class ChampionFragment: AbsDetailChildFragment<FragmentMatchDetailChampionBinding, EmptyViewModel>() {

    override fun createViewModel(): EmptyViewModel = emptyViewModel()

    override fun getBinding(inflater: LayoutInflater): FragmentMatchDetailChampionBinding = FragmentMatchDetailChampionBinding.inflate(inflater)

    override fun initView(view: View) {

    }

    override fun initData() {

    }
}