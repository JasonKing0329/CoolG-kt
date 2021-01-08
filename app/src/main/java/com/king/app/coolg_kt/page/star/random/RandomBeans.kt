package com.king.app.coolg_kt.page.star.random

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/1/8 14:06
 */
class RandomRule {

    var isExcludeFromMarked = false

    /**
     * 0 all, 1 top, 2 bottom, 3 half
     */
    var starType = 0

    var sqlRating: String? = null

}
class RandomData {

    var name: String? = null

    var candidateList: MutableList<Long> = mutableListOf()

    var markedList: MutableList<Long> = mutableListOf()

    var randomRule = RandomRule()

}