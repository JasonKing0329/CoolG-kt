package com.king.app.coolg_kt.view.dialog

import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.os.CancellationSignal
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BindingDialogFragment
import com.king.app.coolg_kt.databinding.DialogFingerprintBinding
import com.king.app.coolg_kt.model.fingerprint.OnFingerResultListener
import com.king.app.coolg_kt.utils.DebugLog
import javax.crypto.Cipher

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2019/3/1 14:30
 */
class FingerprintDialog : BindingDialogFragment<DialogFingerprintBinding>() {

    private var fingerprintManager: FingerprintManager? = null
    private var mCancellationSignal: CancellationSignal? = null
    var onFingerPrintListener: OnFingerResultListener? = null
    var cipher: Cipher ? = null

    private var isSelfCancelled // 标识是否是用户主动取消的认证。
            = false

    override fun getBinding(inflater: LayoutInflater) = DialogFingerprintBinding.inflate(inflater)

    override fun onResume() {
        super.onResume()
        // 在xml里或initView里调用不起作用
        setWidth(resources.getDimensionPixelSize(R.dimen.dlg_fingerprint_width))
        setHeight(resources.getDimensionPixelSize(R.dimen.dlg_fingerprint_height))
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun initView(view: View) {
        fingerprintManager =
            requireContext().getSystemService(FingerprintManager::class.java)
        startListening(cipher!!)
    }

    override fun onPause() {
        super.onPause()
        stopListening()
    }
    @RequiresApi(Build.VERSION_CODES.M)
    private fun startListening(cipher: Cipher) {
        isSelfCancelled = false
        mCancellationSignal = CancellationSignal()
        fingerprintManager?.authenticate(
            FingerprintManager.CryptoObject(cipher),
            mCancellationSignal,
            0,
            object : FingerprintManager.AuthenticationCallback() {
                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    DebugLog.e("errorCode:$errorCode errString:$errString")
                    if (!isSelfCancelled) {

                        when(errorCode) {
                            FingerprintManager.FINGERPRINT_ERROR_HW_UNAVAILABLE -> {
                                onFingerPrintListener?.unSupport()
                                dismissAllowingStateLoss()
                            }
                            FingerprintManager.FINGERPRINT_ERROR_LOCKOUT -> {
                                //Toast.makeText(mActivity, errString, Toast.LENGTH_SHORT).show();
                                Toast.makeText(context, errString.toString() + "", Toast.LENGTH_SHORT).show()
                                onFingerPrintListener?.fingerResult(false)
                                dismiss()
                            }
                        }
                    }
                }

                override fun onAuthenticationHelp(
                    helpCode: Int,
                    helpString: CharSequence
                ) {
                    DebugLog.e("onAuthenticationHelp:$helpString")
                }

                override fun onAuthenticationSucceeded(result: FingerprintManager.AuthenticationResult) {
                    DebugLog.e("onAuthenticationSucceeded")
                    // ToastUtil.showToast(mActivity, "指纹认证成功");
                    onFingerPrintListener?.fingerResult(true)
                    dismiss()
                    //mActivity.onAuthenticated();
                }

                override fun onAuthenticationFailed() {
                    DebugLog.e("onAuthenticationFailed")
                    onFingerPrintListener?.fingerResult(false)
                }
            },
            null
        )
    }

    private fun stopListening() {
        mCancellationSignal?.cancel()
        mCancellationSignal = null
        isSelfCancelled = true
    }

}
