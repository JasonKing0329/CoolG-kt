package com.king.app.coolg_kt.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.king.app.coolg_kt.CoolApplication

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2020/1/22 14:22
 */
abstract class BaseFragment<T : ViewDataBinding, VM: BaseViewModel>: BindingFragment<T>() {

    lateinit var mModel: VM

    fun generateViewModel(vm: Class<VM>) = ViewModelProvider(this, ViewModelFactory(CoolApplication.instance)).get(vm)

    fun emptyViewModel(): EmptyViewModel = ViewModelProvider(this, ViewModelFactory(CoolApplication.instance)).get(EmptyViewModel::class.java)

    fun <AVM: AndroidViewModel> getActivityViewModel(vm: Class<AVM>): AVM = ViewModelProvider(requireActivity().viewModelStore, ViewModelFactory(CoolApplication.instance)).get(vm)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        mModel = createViewModel()
        mModel.loadingObserver.observe(this, Observer { show ->
            if (show) {
                showProgress("loading...")
            } else {
                dismissProgress()
            }
        })
        mModel.messageObserver.observe(this, Observer { message -> showMessageShort(message) })

        val view = mBinding.root
        initView(view)
        return mBinding.root
    }

    protected abstract fun createViewModel(): VM

    abstract fun initView(view: View)

    override fun onDestroyView() {
        mModel?.onDestroy()
        super.onDestroyView()
    }
}