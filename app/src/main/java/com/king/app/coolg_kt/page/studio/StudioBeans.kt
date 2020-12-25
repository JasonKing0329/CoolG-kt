package com.king.app.coolg_kt.page.studio

import com.king.app.gdb.data.entity.FavorRecordOrder

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2020/12/25 9:57
 */
data class StudioSimpleItem(
    var order: FavorRecordOrder
) {
    var firstChar: String? = null
    var name: String? = null
    var number: String? = null

}
data class StudioRichItem(
    var order: FavorRecordOrder
) {
    var imageUrl: String? = null
    var name: String? = null
    var count: String? = null
    var high: String? = null
}