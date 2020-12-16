package com.king.app.coolg_kt.page.image

import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import androidx.databinding.ViewDataBinding
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.model.GlideApp
import com.king.app.coolg_kt.model.bean.ImageBean

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2020/8/4 10:28
 */
abstract class AbsImageAdapter<V : ViewDataBinding> : BaseBindingAdapter<V, ImageBean>() {

    private var isSelectMode = false

    fun setSelectMode(selectMode: Boolean) {
        isSelectMode = selectMode
        notifyDataSetChanged()
    }

    protected fun setCheckVisibility(checkBox: CheckBox) {
        checkBox.visibility = if (isSelectMode) View.VISIBLE else View.GONE
    }

    protected fun setImage(imageView: ImageView, bean: ImageBean) {
        GlideApp.with(imageView.context)
            .load(bean.url)
            .error(R.drawable.def_small)
            .into(imageView)
    }

    override fun onClickItem(v: View, position: Int, bean: ImageBean) {
        if (isSelectMode) {
            bean.isSelected = !bean.isSelected
        } else {
            super.onClickItem(v, position, bean)
        }
    }
}