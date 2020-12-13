package com.king.app.coolg_kt.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2020/1/22 14:22
 */
abstract class BindingFragment<T : ViewDataBinding>: RootFragment() {

    lateinit var mBinding: T

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = getBinding(inflater)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initData()
        super.onViewCreated(view, savedInstanceState)
    }

    protected abstract fun getBinding(inflater: LayoutInflater): T

    abstract fun initData()

}