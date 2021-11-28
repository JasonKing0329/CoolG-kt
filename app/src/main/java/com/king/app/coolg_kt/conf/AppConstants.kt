package com.king.app.coolg_kt.conf

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2020/12/15 9:40
 */
object AppConstants {

    const val PORT_RECEIVE = 9002

    const val KEY_SCENE_ALL = "All"

    const val RESP_ORDER_ID = "order_id"

    const val ORDER_STUDIO_NAME = "Studio"

    val RECORD_SQL_EXPRESSIONS = arrayOf(
        "score > ",
        "score_bareback > 0",
        "score_passion > ",
        "score_body > ",
        "score_cock > ",
        "score_ass > ",
        "score_cum > ",
        "score_feel > ",
        "score_star > ",
        "hd_level = ",
        "scene = ''",
        "special_desc LIKE '%%'"
    )

    val RECORD_1v1_SQL_EXPRESSIONS = arrayOf(
        "RT.score_fk_type1 > 0",
        "RT.score_fk_type2 > 0",
        "RT.score_fk_type3 > 0",
        "RT.score_fk_type4 > 0",
        "RT.score_fk_type5 > 0",
        "RT.score_fk_type6 > 0",
        "RT.score_story > ",
        "RT.score_cshow > ",
        "RT.score_foreplay > ",
        "RT.score_bjob > "
    )

    val RECORD_3w_SQL_EXPRESSIONS = arrayOf(
        "RT.score_fk_type1 > 0",
        "RT.score_fk_type2 > 0",
        "RT.score_fk_type3 > 0",
        "RT.score_fk_type4 > 0",
        "RT.score_fk_type5 > 0",
        "RT.score_fk_type6 > 0",
        "RT.score_fk_type7 > 0",
        "RT.score_fk_type8 > 0",
        "RT.score_story > ",
        "RT.score_cshow > ",
        "RT.score_foreplay > ",
        "RT.score_bjob > "
    )

    const val TAG_SORT_NONE = 0
    const val TAG_SORT_NAME = 1
    const val TAG_SORT_RANDOM = 2
    const val TAG_SORT_NUMBER = 3
    val TAG_SORT_MODE_TEXT = arrayOf(
        "No order",
        "By Random",
        "By Name"
    )

    val STAR_LIST_TITLES = arrayOf(
        "All", "1", "0", "0.5"
    )

    const val STAR_SORT_NAME = 0
    const val STAR_SORT_RECORDS = 1
    const val STAR_SORT_RATING = 2
    const val STAR_SORT_RATING_FACE = 3
    const val STAR_SORT_RATING_BODY = 4
    const val STAR_SORT_RATING_DK = 5
    const val STAR_SORT_RATING_SEXUALITY = 6
    const val STAR_SORT_RATING_PASSION = 7
    const val STAR_SORT_RATING_VIDEO = 8
    const val STAR_SORT_RATING_PREFER = 9
    const val STAR_SORT_RANDOM = 10

    const val TAG_STAR_GRID = 0
    const val TAG_STAR_STAGGER = 1

    const val STUDIO_LIST_TYPE_SIMPLE = 0
    const val STUDIO_LIST_TYPE_GRID = 1
    const val STUDIO_LIST_TYPE_RICH = 2

    const val STUDIO_LIST_SORT_NAME = 0
    const val STUDIO_LIST_SORT_NUM = 1
    const val STUDIO_LIST_SORT_CREATE_TIME = 2
    const val STUDIO_LIST_SORT_UPDATE_TIME = 3

    const val STUDIO_RECORD_HEAD_RECENT = 0
    const val STUDIO_RECORD_HEAD_TOP = 1

    const val VIEW_TYPE_LIST = 0
    const val VIEW_TYPE_GRID = 1
    const val VIEW_TYPE_GRID_TAB = 2

    val timeParams = arrayOf("5秒", "10秒", "15秒", "20秒", "30秒", "1分钟", "5分钟", "10分钟", "20分钟", "30分钟")
    val timeParamValues = arrayOf(5, 10, 15, 20, 30, 60, 300, 600, 1200, 1800)
}