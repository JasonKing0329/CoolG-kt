package com.king.app.coolg_kt.page.star.list

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/1/7 16:35
 */
interface SelectObserver<T> {
    fun onSelect(data: T)
}