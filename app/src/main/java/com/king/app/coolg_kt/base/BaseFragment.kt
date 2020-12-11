package com.king.app.coolg_kt.base

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.king.app.coolg_kt.CoolApplication
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2020/1/22 14:22
 */
abstract class BaseFragment<T : ViewDataBinding, VM: BaseViewModel>: RootFragment() {

    lateinit var mBinding: T

    lateinit var mModel: VM

    var compositeDisposable: CompositeDisposable = CompositeDisposable()

    fun addDisposable(disposable: Disposable) {
        compositeDisposable.add(disposable)
    }

    fun generateViewModel(vm: Class<VM>) = ViewModelProvider(this, ViewModelFactory(CoolApplication.instance)).get(vm)

    fun emptyViewModel(): EmptyViewModel = ViewModelProvider(this, ViewModelFactory(CoolApplication.instance)).get(EmptyViewModel::class.java)

    fun <AVM: AndroidViewModel> getActivityViewModel(vm: Class<AVM>): AVM = ViewModelProvider(requireActivity().viewModelStore, ViewModelFactory(CoolApplication.instance)).get(vm)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = getBinding(inflater)
        mModel = createViewModel()
        if (mModel != null) {
            mModel!!.loadingObserver.observe(viewLifecycleOwner, Observer { show ->
                if (show) {
                    showProgress("loading...")
                } else {
                    dismissProgress()
                }
            })
            mModel!!.messageObserver.observe(viewLifecycleOwner, Observer { message -> showMessageShort(message) })
        }
        val view = mBinding.root
        initView(view)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initData()
        super.onViewCreated(view, savedInstanceState)
    }

    protected abstract fun getBinding(inflater: LayoutInflater): T

    protected abstract fun createViewModel(): VM

    protected abstract fun initView(view: View)

    abstract fun initData()

    open fun<AC> startPage(target: Class<AC>, bundle: Bundle) {
        var intent = Intent().setClass(requireContext(), target)
        intent.putExtra(BaseActivity.KEY_BUNDLE, bundle)
        startActivity(intent)
    }

}