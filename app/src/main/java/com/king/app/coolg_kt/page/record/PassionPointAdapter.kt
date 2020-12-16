package com.king.app.coolg_kt.page.record

import android.graphics.Color
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
    private var list: List<PassionPoint>? = null
    fun setList(list: List<PassionPoint>?) {
        this.list = list
    }

    override fun getItemCount(): Int {
        return if (list == null) 0 else list!!.size
    }

    override fun getPointColor(position: Int): Int {
        return ColorUtil.randomWhiteTextBgColor()
    }

    override fun getTextColor(position: Int): Int {
        return Color.WHITE
    }

    override fun getText(position: Int): String {
        return """
            ${list!![position].key}
            ${list!![position].content}
            """.trimIndent()
    }
}