package com.king.app.coolg_kt.base

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.view.dialog.ProgressDialogFragment
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable

/**
 * 描述:
 *
 * 作者：景阳
 *
 * 创建时间: 2020/1/22 13:26
 */
abstract class RootFragment : Fragment() {

    private var progressDialogFragment: ProgressDialogFragment? = null

    var compositeDisposable: CompositeDisposable = CompositeDisposable()

    fun addDisposable(disposable: Disposable) {
        compositeDisposable.add(disposable)
    }

    fun showConfirmMessage(msg: String, listener: DialogInterface.OnClickListener) {
        AlertDialog.Builder(activity)
            .setTitle(null)
            .setMessage(msg)
            .setPositiveButton(getString(R.string.ok), listener)
            .show()
    }

    fun showConfirmCancelMessage(
        msg: String,
        okListener: DialogInterface.OnClickListener,
        cancelListener: DialogInterface.OnClickListener?
    ) {
        AlertDialog.Builder(activity)
            .setTitle(null)
            .setMessage(msg)
            .setPositiveButton(getString(R.string.ok), okListener)
            .setNegativeButton(getString(R.string.cancel), cancelListener)
            .show()
    }

    fun showConfirmCancelMessage(
        msg: String,
        okText: String,
        okListener: DialogInterface.OnClickListener,
        cancelText: String,
        cancelListener: DialogInterface.OnClickListener
    ) {
        AlertDialog.Builder(activity)
            .setTitle(null)
            .setMessage(msg)
            .setPositiveButton(okText, okListener)
            .setNegativeButton(cancelText, cancelListener)
            .show()
    }

    fun showYesNoMessage(
        msg: String,
        okListener: DialogInterface.OnClickListener,
        cancelListener: DialogInterface.OnClickListener
    ) {
        AlertDialog.Builder(activity)
            .setTitle(null)
            .setMessage(msg)
            .setPositiveButton(getString(R.string.yes), okListener)
            .setNegativeButton(getString(R.string.no), cancelListener)
            .show()
    }

    fun showProgress(msg: String) {
        var msg = msg
        progressDialogFragment = ProgressDialogFragment()
        if (TextUtils.isEmpty(msg)) {
            msg = resources.getString(R.string.loading)
        }
        progressDialogFragment!!.setMessage(msg)
        progressDialogFragment!!.show(childFragmentManager, "ProgressDialogFragment")
    }

    fun dismissProgress() {
        if (progressDialogFragment != null) {
            progressDialogFragment!!.dismissAllowingStateLoss()
        }
    }

    fun showMessageShort(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }

    fun showMessageLong(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_LONG).show()
    }

    open fun<AC> startPage(target: Class<AC>, bundle: Bundle) {
        var intent = Intent().setClass(requireContext(), target)
        intent.putExtra(BaseActivity.KEY_BUNDLE, bundle)
        startActivity(intent)
    }
}
