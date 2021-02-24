package com.king.app.coolg_kt.page.match.detail

import android.os.Bundle
import android.view.View
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModelProvider
import com.king.app.coolg_kt.CoolApplication
import com.king.app.coolg_kt.base.BaseFragment
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.base.ViewModelFactory

/**
 * @description:
 * @author：Jing
 * @date: 2021/2/24 9:47
 */
abstract class AbsDetailChildFragment<T : ViewDataBinding, VM: BaseViewModel>: BaseFragment<T, VM>() {

    lateinit var mainViewModel: DetailViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mainViewModel = ViewModelProvider(requireActivity().viewModelStore, ViewModelFactory(
            CoolApplication.instance)
        ).get(DetailViewModel::class.java)
        super.onViewCreated(view, savedInstanceState)
    }
}