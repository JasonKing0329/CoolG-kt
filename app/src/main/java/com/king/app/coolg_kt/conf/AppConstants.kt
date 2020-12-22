package com.king.app.coolg_kt.conf

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2020/12/15 9:40
 */
object AppConstants {

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
    const val TAG_SORT_RANDOM = 1
    const val TAG_SORT_NAME = 2
    val TAG_SORT_MODE_TEXT = arrayOf(
        "No order",
        "By Random",
        "By Name"
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
    const val STAR_SORT_RANDOM = 9

    const val STAR_RATING_SORT_COMPLEX = 0
    const val STAR_RATING_SORT_FACE = 1
    const val STAR_RATING_SORT_BODY = 2
    const val STAR_RATING_SORT_DK = 3
    const val STAR_RATING_SORT_SEX = 4
    const val STAR_RATING_SORT_PASSION = 5
    const val STAR_RATING_SORT_VIDEO = 6
    const val STAR_RATING_SORT_PREFER = 7

    const val TAG_STAR_GRID = 0
    const val TAG_STAR_STAGGER = 1

}