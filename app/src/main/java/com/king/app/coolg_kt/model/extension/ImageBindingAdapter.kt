package com.king.app.coolg_kt.model.extension

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.model.GlideApp
import com.king.app.coolg_kt.view.CoverView

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2020/12/15 16:07
 */
object ImageBindingAdapter {

    @JvmStatic
    @BindingAdapter("app:recordUrl")
    fun setRecordUrl(view: ImageView, url: String?) {
        GlideApp.with(view.context)
            .load(url)
            .error(R.drawable.def_small)
            .into(view)
    }

    @JvmStatic
    @BindingAdapter("app:recordLargeUrl")
    fun setRecordLargeUrl(view: ImageView, url: String?) {
        GlideApp.with(view.context)
            .load(url)
            .error(R.drawable.def_small)
            .into(view)
    }

    @JvmStatic
    @BindingAdapter("app:starUrl")
    fun setStarUrl(view: ImageView, url: String?) {
        GlideApp.with(view.context)
            .load(url)
            .error(R.drawable.def_person)
            .into(view)
    }

    @JvmStatic
    @BindingAdapter("app:coverRecordUrl")
    fun setCoverRecordUrl(view: CoverView, url: String?) {
        GlideApp.with(view.context)
            .load(url)
            .error(R.drawable.def_small)
            .into(view.imageView)
    }

    @JvmStatic
    @BindingAdapter("app:coverStarUrl")
    fun setCoverStarUrl(view: CoverView, url: String?) {
        GlideApp.with(view.context)
            .load(url)
            .error(R.drawable.def_person)
            .into(view.imageView)
    }

}