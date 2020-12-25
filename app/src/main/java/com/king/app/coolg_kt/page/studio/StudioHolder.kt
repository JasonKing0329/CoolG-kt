package com.king.app.coolg_kt.page.studio

import com.king.app.jactionbar.JActionbar

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/9/18 13:39
 */
interface StudioHolder {
    fun getJActionBar(): JActionbar
    fun showStudioPage(studioId: Long, name: String?)
    fun backToList()
    fun sendSelectedOrderResult(id: Long?)
}