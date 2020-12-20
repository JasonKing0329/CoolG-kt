package com.king.app.coolg_kt.model.repository

import com.king.app.gdb.data.relation.StarWrap
import io.reactivex.rxjava3.core.Observable

/**
 * @description:
 * @author：Jing
 * @date: 2020/12/20 11:34
 */
class StarRepository: BaseRepository() {

    fun getStar(id: Long): Observable<StarWrap> {
        return Observable.create {
            it.onNext(getDatabase().getStarDao().getStarWrap(id))
            it.onComplete()
        }
    }
}