package com.king.app.coolg_kt.page.star.list

import com.king.app.gdb.data.entity.Star

/**
 * 描述:
 *
 * 作者：景阳
 *
 * 创建时间: 2017/7/12 10:39
 */
interface IStarListHolder {
    fun dispatchClickStar(star: Star): Boolean
    fun hideDetailIndex()
    fun updateDetailIndex(name: String)
    fun dispatchOnLongClickStar(star: Star): Boolean
}