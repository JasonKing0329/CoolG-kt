package com.king.app.gdb.data

/**
 * 描述:
 *
 * 作者：景阳
 *
 * 创建时间: 2018/2/9 14:58
 */
object DataConstants {
    const val VALUE_RECORD_TYPE_1V1 = 1
    const val VALUE_RECORD_TYPE_3W = 2
    const val VALUE_RECORD_TYPE_MULTI = 3
    const val VALUE_RECORD_TYPE_LONG = 4
    const val VALUE_RELATION_TOP = 1
    const val VALUE_RELATION_BOTTOM = 2
    const val VALUE_RELATION_MIX = 3

    /**
     * related to scoreNoCond parameter in RecordSingleScene
     */
    const val BAREBACK = 20
    const val STAR_UNKNOWN = "Unknown"
    const val RECORD_UNKNOWN = "Unknown"
    const val STAR_MODE_ALL = "star_all"
    const val STAR_MODE_TOP = "star_top"
    const val STAR_MODE_BOTTOM = "star_bottom"
    const val STAR_MODE_HALF = "star_half"
    const val RECORD_NR = "NR"
    const val RECORD_HD_NR = 0
    const val STAR_3W_FLAG_TOP = "top"
    const val STAR_3W_FLAG_BOTTOM = "bottom"
    const val STAR_3W_FLAG_MIX = "mix"
    const val DEPRECATED = 1
    const val CATEGORY_TYPE_LEVEL = 0
    const val CATEGORY_TYPE_LIMIT_LEVEL = 1
    const val TAG_TYPE_RECORD = 0
    const val TAG_TYPE_STAR = 1
    fun getTextForType(type: Int): String {
        return when (type) {
            VALUE_RELATION_TOP -> STAR_3W_FLAG_TOP
            VALUE_RELATION_BOTTOM -> STAR_3W_FLAG_BOTTOM
            VALUE_RELATION_MIX -> STAR_3W_FLAG_MIX
            else -> STAR_3W_FLAG_MIX
        }
    }
}