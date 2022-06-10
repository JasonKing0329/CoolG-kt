package com.king.app.coolg_kt.view.widget

import android.content.Context
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.setPadding
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.utils.ScreenUtils

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2022/6/9 15:30
 */
class KeyValueEditView: LinearLayout {

    private var key = ""

    lateinit var tvKey: TextView
    lateinit var etInput: EditText

    var initValue: String? = ""
    var editValue: String? = ""

    var keyTextSize = ScreenUtils.dp2px(16f)
        set(value) {
            field = value
            tvKey.setTextSize(TypedValue.COMPLEX_UNIT_PX, value.toFloat())
        }

    var keyTextColor = Color.parseColor("#333333")
        set(value) {
            field = value
            tvKey.setTextColor(value)
        }

    var inputWidth = ScreenUtils.dp2px(50f)
        set(value) {
            field = value
            etInput.layoutParams.apply {
                width = value
                etInput.layoutParams = this
            }
        }

    var inputTextSize = ScreenUtils.dp2px(14f)
        set(value) {
            field = value
            etInput.setTextSize(TypedValue.COMPLEX_UNIT_PX, value.toFloat())
        }

    constructor(context: Context?) : super(context) {
        init(null)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(attrs)
    }

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        attrs?.apply {
            val a = context.obtainStyledAttributes(attrs, R.styleable.KeyValueEditView)
            keyTextSize = a.getDimensionPixelSize(R.styleable.KeyValueEditView_kv_keyTextSize, keyTextSize)
            inputTextSize = a.getDimensionPixelSize(R.styleable.KeyValueEditView_kv_inputTextSize, inputTextSize)
            inputWidth = a.getDimensionPixelSize(R.styleable.KeyValueEditView_kv_inputWidth, inputWidth)
            keyTextColor = a.getColor(R.styleable.KeyValueEditView_kv_keyTextColor, keyTextColor)
            key = a.getString(R.styleable.KeyValueEditView_kv_keyText)?:""
        }

        gravity = Gravity.CENTER_VERTICAL
        TextView(context).apply {
            tvKey = this
            setTextSize(TypedValue.COMPLEX_UNIT_PX, keyTextSize.toFloat())
            setTextColor(keyTextColor)
            text = key
            val param = LayoutParams(0, LayoutParams.WRAP_CONTENT)
            param.weight = 1F
            addView(this, param)
        }
        EditText(context).apply {
            etInput = this
            maxLines = 1
            isSingleLine = true
            setPadding(ScreenUtils.dp2px(2f))
            setBackgroundResource(R.drawable.shape_input_border_gray)
            addTextChangedListener(inputWatcher)
            val param = LayoutParams(inputWidth, LayoutParams.WRAP_CONTENT)
            addView(this, param)
        }
    }

    fun setKeyText(text: String) {
        tvKey.text = text
    }

    fun setValue(text: String?) {
        initValue = text
        editValue = text
        etInput.removeTextChangedListener(inputWatcher)
        etInput.setText(text)
        etInput.addTextChangedListener(inputWatcher)
    }

    fun setInputType(type: Int) {
        etInput.inputType = type
    }

    fun getInputValue(): String {
        return etInput.text.toString()
    }

    fun isInputChanged(): Boolean {
        return editValue != initValue
    }

    private var inputWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            editValue = s.toString()
        }

        override fun afterTextChanged(s: Editable?) {

        }
    }
}