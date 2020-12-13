package com.king.app.coolg_kt.model.repository

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/8/7 10:09
 */
class PropertyRepository : BaseRepository() {

    fun getVersion(): String {
        return getDatabase().getPropertyDao().getVersion()
    }
}