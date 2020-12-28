package com.king.app.coolg_kt.model.bean

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.king.app.coolg_kt.BR
import com.king.app.gdb.data.entity.PlayOrder
import com.king.app.gdb.data.entity.Star

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

}