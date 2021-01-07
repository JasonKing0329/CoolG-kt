package com.king.app.coolg_kt.model.bean

import android.view.View
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.king.app.coolg_kt.BR
import com.king.app.coolg_kt.page.star.list.SelectObserver
import com.king.app.gdb.data.entity.PlayOrder
import com.king.app.gdb.data.entity.Star
import com.king.app.gdb.data.relation.StarWrap

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2020/12/15 15:46
 */
class VideoPlayList : BaseObservable() {
    var imageUrl: String? = null
    var name: String? = null
    var playOrder: PlayOrder? = null

    @get:Bindable
    var isChecked = false
        set(checked) {
            field = checked
            notifyPropertyChanged(BR.checked)
        }

    @get:Bindable
    var visibility = 0
        set(visibility) {
            field = visibility
            notifyPropertyChanged(BR.visibility)
        }

    var videos = 0

}

class VideoGuy : BaseObservable() {
    var star: Star? = null
    var imageUrl: String? = null
    var videos = 0
    var width = 0
    var height = 0

    @get:Bindable
    var isChecked = false
        set(checked) {
            field = checked
            notifyPropertyChanged(BR.checked)
        }

    @get:Bindable
    var visibility = View.GONE
        set(visibility) {
            field = visibility
            notifyPropertyChanged(BR.visibility)
        }

}

class SelectStar : BaseObservable() {
    var star: StarWrap? = null

    var observer: SelectObserver<SelectStar>? = null

    @get:Bindable
    var isChecked = false
        set(checked) {
            field = checked
            notifyPropertyChanged(BR.checked)
        }
}