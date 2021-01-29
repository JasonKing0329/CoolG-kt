package com.king.app.coolg_kt.page.record.pad

import android.view.View
import android.widget.ImageView
import androidx.lifecycle.Lifecycle
import androidx.palette.graphics.Palette
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.model.GlideApp
import com.king.app.coolg_kt.model.palette.ViewColorBound
import com.king.lib.banner.CoolBannerAdapter

/**
 * Created by Administrator on 2018/8/25 0025.
 */
class RecordPagerAdapter(private val lifecycle: Lifecycle) :
    CoolBannerAdapter<String?>() {
    var viewList: List<View>? = null
    var onHolderListener: OnHolderListener? = null

    override fun getLayoutRes(): Int {
        return R.layout.adapter_record_banner_item_pad
    }

    override fun onBindView(
        view: View,
        position: Int,
        bean: String?
    ) {
        val imageView =
            view.findViewById<ImageView>(R.id.iv_image)
        GlideApp.with(imageView.context)
            .asBitmap()
            .load(bean) // listener只能添加一个，所以用RecordBitmapListener包含BitmapPaletteListener and TargetViewListener的处理
            .listener(object : RecordBitmapListener(viewList!!, lifecycle) {
                override fun onPaletteCreated(palette: Palette?) {
                    onHolderListener?.onPaletteCreated(position, palette)
                }

                override fun onBoundsCreated(bounds: List<ViewColorBound>?) {
                    onHolderListener?.onBoundsCreated(position, bounds)
                }
            })
            .into(imageView)
    }

    interface OnHolderListener {
        fun onPaletteCreated(position: Int, palette: Palette?)
        fun onBoundsCreated(
            position: Int,
            bounds: List<ViewColorBound>?
        )
    }

}