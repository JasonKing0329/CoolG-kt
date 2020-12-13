package com.king.app.coolg_kt.page.download

import android.os.Bundle
import android.view.View
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModelProvider
import com.king.app.coolg_kt.CoolApplication
import com.king.app.coolg_kt.base.BindingFragment
import com.king.app.coolg_kt.base.ViewModelFactory

/**
 * @description:
 * @author：Jing
 * @date: 2020/12/13 15:18
 */
abstract class AbsDownloadChildFragment<T: ViewDataBinding>: BindingFragment<T>() {

    lateinit var mainViewModel:DownloadViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mainViewModel = ViewModelProvider(requireParentFragment().viewModelStore, ViewModelFactory(CoolApplication.instance)).get(DownloadViewModel::class.java)
        super.onViewCreated(view, savedInstanceState)
    }
}