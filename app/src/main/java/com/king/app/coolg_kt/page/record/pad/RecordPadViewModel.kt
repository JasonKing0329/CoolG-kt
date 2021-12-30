package com.king.app.coolg_kt.page.record.pad

import android.app.Application
import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import androidx.palette.graphics.Palette
import com.king.app.coolg_kt.model.bean.PassionPoint
import com.king.app.coolg_kt.model.bean.TitleValueBean
import com.king.app.coolg_kt.model.http.observer.SimpleObserver
import com.king.app.coolg_kt.model.image.ImageProvider
import com.king.app.coolg_kt.model.image.ImageProvider.getRecordRandomPath
import com.king.app.coolg_kt.model.module.VideoModel
import com.king.app.coolg_kt.model.palette.ViewColorBound
import com.king.app.coolg_kt.model.repository.RecordRepository
import com.king.app.coolg_kt.page.record.RecordViewModel
import com.king.app.gdb.data.entity.Tag
import com.king.app.gdb.data.relation.RecordStarWrap
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableSource
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/8/22 13:46
 */
class RecordPadViewModel(application: Application) : RecordViewModel(application) {

    var imageList = listOf<String>()
    private val repository = RecordRepository()
    var videoPathObserver: MutableLiveData<String?> = MutableLiveData()
    var scoreObserver: MutableLiveData<MutableList<TitleValueBean>> = MutableLiveData()
    var paletteObserver: MutableLiveData<Palette> = MutableLiveData()
    var viewBoundsObserver: MutableLiveData<List<ViewColorBound>> = MutableLiveData()
    private val paletteMap: MutableMap<Int, Palette> = mutableMapOf()
    private val viewBoundsMap: MutableMap<Int, List<ViewColorBound>?> = mutableMapOf()

    override fun loadRecord(recordId: Long) {
        repository.getRecord(recordId)
            .flatMap {
                mRecord = it
                recordObserver.postValue(it)
                videoPathObserver.postValue(VideoModel.getVideoPath(it.bean.name))
                loadRelations()
            }
            .flatMap { relations ->
                starsObserver.postValue(relations)
                loadScores()
            }
            .flatMap { scores ->
                scoreObserver.postValue(scores)
                loadPassionPoints()
            }
            .flatMap { passions ->
                passionsObserver.postValue(passions)
                loadTags()
            }
            .flatMap { tags ->
                tagsObserver.postValue(tags)
                loadImages()
            }
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<List<String>>(getComposite()) {

                override fun onNext(strings: List<String>) {
                    imagesObserver.value = strings
                    checkPlayable()
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                }
            })
    }

    private fun loadRelations(): ObservableSource<List<RecordStarWrap>> {
        return ObservableSource {
            var list = listOf<RecordStarWrap>()
            mRecord?.let { record ->
                list = repository.getRecordStars(record.bean.id!!)
            }
            it.onNext(list)
            it.onComplete()
        }
    }

    private fun loadPassionPoints(): ObservableSource<List<PassionPoint>> {
        return ObservableSource { it ->
            var list = listOf<PassionPoint>()
            mRecord?.let { record ->
                list = repository.getPassions(record)
            }
            it.onNext(list)
            it.onComplete()
        }
    }

    private fun loadScores(): ObservableSource<MutableList<TitleValueBean>> {
        return ObservableSource { observer ->
            observer.onNext(scoreDetails)
            observer.onComplete()
        }
    }

    private fun loadImages(): Observable<List<String>> {
        return Observable.create { observer ->
            if (ImageProvider.hasRecordFolder(mRecord?.bean.name)) {
                imageList = ImageProvider.getRecordPathList(mRecord?.bean.name)
            } else {
                val list = mutableListOf<String>()
                val path = getRecordRandomPath(mRecord?.bean.name, null)
                path?.let { path ->
                    list.add(path)
                }
            }
            imageList = imageList.shuffled()
            observer.onNext(imageList)
            observer.onComplete()
        }
    }

    private fun loadTags(): ObservableSource<List<Tag>> {
        return ObservableSource { observer ->
            observer.onNext(getTags(mRecord))
            observer.onComplete()
        }
    }

    fun deleteImage(path: String?) {
        if (!TextUtils.isEmpty(path)) {
            val file = File(path)
            if (file.exists()) {
                file.delete()
            }
            refreshImages()
        }
    }

    protected fun refreshImages() {
        loadImages()
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<List<String>>(getComposite()) {

                override fun onNext(strings: List<String>) {
                    imagesObserver.value = strings
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                }

            })
    }

    private val scoreDetails: MutableList<TitleValueBean>
        get() {
            val list = mutableListOf<TitleValueBean>()
            val sdf = SimpleDateFormat("yyyy-MM-dd")
            mRecord.let {
                newValue(list, sdf.format(Date(it.bean.lastModifyTime)))
                newTitleValue(list, "HD level", it.bean.hdLevel)
                newTitleValue(list, "Feel", it.bean.scoreFeel)
                newTitleValue(list, "Stars", it.bean.scoreStar)
                newTitleValue(list, "Passion", it.bean.scorePassion)
                newTitleValue(list, "Cum", it.bean.scoreCum)
                newTitleValue(list, "Special", it.bean.scoreSpecial)
                newValue(list, it.bean.specialDesc)
                it.recordType1v1?.let { recordType1v1 ->
                    newTitleValue(list, "BJob", recordType1v1.scoreBjob)
                    newTitleValue(list, "Scene", recordType1v1.scoreScene)
                    newTitleValue(list, "CShow", recordType1v1.scoreCshow)
                    newTitleValue(list, "Rhythm", recordType1v1.scoreRhythm)
                    newTitleValue(list, "Story", recordType1v1.scoreStory)
                    newTitleValue(list, "Rim", recordType1v1.scoreRim)
                    newTitleValue(list, "Foreplay", recordType1v1.scoreForePlay)
                }
                it.recordType3w?.let { recordType3w ->
                    newTitleValue(list, "BJob", recordType3w.scoreBjob)
                    newTitleValue(list, "Scene", recordType3w.scoreScene)
                    newTitleValue(list, "CShow", recordType3w.scoreCshow)
                    newTitleValue(list, "Rhythm", recordType3w.scoreRhythm)
                    newTitleValue(list, "Story", recordType3w.scoreStory)
                    newTitleValue(list, "Rim", recordType3w.scoreRim)
                    newTitleValue(list, "Foreplay", recordType3w.scoreForePlay)
                }
            }
            return list
        }

    private fun newValue(list: MutableList<TitleValueBean>, value: String?) {
        value?.let {
            val bean = TitleValueBean()
            bean.value = value
            bean.isOnlyValue = true
            list.add(bean)
        }
    }

    private fun newTitleValue(list: MutableList<TitleValueBean>, title: String?, value: Int) {
        if (value > 0) {
            list.add(TitleValueBean(title, value.toString()))
        }
    }

    fun refreshBackground(position: Int) {
        paletteMap[position]?.let {
            paletteObserver.value = it
        }
        viewBoundsMap[position]?.let {
            viewBoundsObserver.value = it
        }
    }

    fun cachePalette(position: Int, palette: Palette) {
        paletteMap[position] = palette
    }

    fun cacheViewBounds(position: Int, bounds: List<ViewColorBound>?) {
        viewBoundsMap[position] = bounds
    }

    fun getCurrentImage(currentPage: Int): String? {
        return try {
            imageList[currentPage]
        } catch (e: Exception) {
            null
        }
    }

}