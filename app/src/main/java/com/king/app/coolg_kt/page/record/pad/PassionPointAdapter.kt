package com.king.app.coolg_kt.page.record.pad

import android.graphics.Color
import androidx.palette.graphics.Palette.Swatch
import com.king.app.coolg_kt.model.bean.PassionPoint
import com.king.app.coolg_kt.utils.ColorUtil
import com.king.app.coolg_kt.view.widget.PointAdapter

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/8/8 17:26
 */
class PassionPointAdapter : PointAdapter() {
    var list: List<PassionPoint>? = null
    var swatches: List<Swatch>? = null

    override fun getItemCount(): Int = list?.size?:0

    override fun getPointColor(position: Int): Int {
        return if (swatches == null || swatches!!.isEmpty()) {
            ColorUtil.randomWhiteTextBgColor()
        } else {
            swatches!![position % swatches!!.size].rgb
        }
    }

    override fun getTextColor(position: Int): Int {
        return if (swatches == null || swatches!!.isEmpty()) {
            Color.WHITE
        } else {
            swatches!![position % swatches!!.size].bodyTextColor
        }
    }

    override fun getText(position: Int): String {
        return """
            ${list!![position].key}
            ${list!![position].content}
            """.trimIndent()
    }
}