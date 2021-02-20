package com.king.app.coolg_kt.page.tv

import android.app.Application
import android.text.TextUtils
import android.view.View
import androidx.databinding.ObservableInt
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.model.http.AppHttpClient
import com.king.app.coolg_kt.model.http.HttpConstants
import com.king.app.coolg_kt.model.http.bean.data.FileBean
import com.king.app.coolg_kt.model.http.bean.request.FolderRequest
import com.king.app.coolg_kt.model.http.bean.request.PathRequest
import com.king.app.coolg_kt.model.http.bean.response.OpenFileResponse
import com.king.app.coolg_kt.model.http.observer.SimpleObserver
import com.king.app.coolg_kt.model.setting.SettingProperty
import com.king.app.coolg_kt.utils.PinyinUtil
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableSource
import net.sourceforge.pinyin4j.PinyinHelper
import java.util.*
import kotlin.Comparator

/**
 * @description:
 * @author：Jing
 * @date: 2021/2/13 22:40
 */
class ContentViewModel(application: Application): BaseViewModel(application) {

    var ROOT = HttpConstants.FOLDER_TYPE_ALL
    var upperVisibility = ObservableInt(View.GONE)
    var listObserver = MutableLiveData<MutableList<FileBean>>()
    private var mFolderList: MutableList<FileBean> = mutableListOf()
    private val mFolderStack = Stack<FileBean>()
    private var mFilterText: String? = null

    val SORT_TYPE_NAME = 0
    val SORT_TYPE_DATE = 1
    val SORT_TYPE_SIZE = 2
    private var mSortType = SORT_TYPE_NAME

    var isSuperUser = false

    fun getCurrentFolder(): FileBean? {
        return mFolderStack.peek()
    }

    fun goUpper() {
        val curFolder = mFolderStack.pop()
        val parentFolder = mFolderStack.peek()
        loadItems(false, parentFolder, true, curFolder)
    }

    fun refresh() {
        loadItems(false, getCurrentFolder(), false, null)
    }

    fun loadRoot() {
        mFolderList.clear()
        mFolderStack.clear()
        upperVisibility.set(View.GONE)
        loadNewFolder(null)
    }

    fun loadNewFolder(folder: FileBean?) {
        loadItems(true, folder, false, null)
    }

    private fun loadItems(isNew: Boolean, folder: FileBean?, isBack: Boolean, popFolder: FileBean?) {
        val request = FolderRequest()
        request.type = HttpConstants.FOLDER_TYPE_FOLDER
        request.filterType = HttpConstants.FILE_FILTER_VIDEO
        request.isCountSize = false
        if (folder == null) {
            request.folder = ROOT
        } else {
            request.folder = folder.path
        }
        request.isGuest = !isSuperUser
        loadingObserver.value = true
        AppHttpClient.getInstance().getAppService().requestSurf(request)
            .flatMap { response ->
                mFolderList = response.fileList
                filterFiles()
            }
            .flatMap { list -> sortFiles(list) }
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<MutableList<FileBean>>(getComposite()) {
                override fun onNext(list: MutableList<FileBean>) {
                    listObserver.value = list
                    loadingObserver.value = false

                    // 进入子目录
                    if (isNew) {
                        mFolderStack.push(folder)
                    }
                    updateUpperVisibility()
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                    loadingObserver.value = false
                    messageObserver.value = e?.message?:""
                    // 返回上层目录
                    if (isBack) {
                        // 将popFolder重新入栈
                        mFolderStack.push(popFolder)
                    }
                }
            })
    }

    private fun updateUpperVisibility() {
        if (getCurrentFolder() == null) {
            upperVisibility.set(View.GONE)
        } else {
            upperVisibility.set(View.VISIBLE)
        }
    }

    fun backFolder(): Boolean {
        if (mFolderStack!!.empty() || getCurrentFolder() == null) {
            return false
        }
        goUpper()
        return true
    }

    private fun filterFiles(): Observable<MutableList<FileBean>> {
        return Observable.create {
            val result = mutableListOf<FileBean>()
            mFolderList.forEach { bean ->
                if (TextUtils.isEmpty(mFilterText) || mFilterText!!.trim().isEmpty()) {
                    result.add(bean)
                } else {
                    if (bean.name?.toLowerCase()?.contains(mFilterText!!.toLowerCase()) == true) {
                        result.add(bean)
                    }
                }
            }
            it.onNext(result)
            it.onComplete()
        }
    }

    /**
     * 降序
     */
    private fun compareLong(date1: Long, date2: Long): Int {
        val result = date2 - date1
        return when {
            result > 0 -> 1
            result < 0 -> -1
            else -> 0
        }
    }

    /**
     * 升序
     */
    private fun compareName(name1: String, name2: String): Int {
        return name1.toLowerCase().compareTo(name2.toLowerCase())
    }

    private fun sortFiles(list: MutableList<FileBean>): ObservableSource<MutableList<FileBean>> {
        return ObservableSource {
            // 生成名称拼音（耗时操作，只生成一次）
            list.forEach {bean ->
                bean.name?.let { name -> bean.namePinyin = PinyinUtil.toPinyinConcat(name) }
            }
            list.sortWith(object : Comparator<FileBean>{
                override fun compare(o1: FileBean, o2: FileBean): Int {
                    // folder先于file
                    if (o1.isFolder) {
                        // 都是文件夹，继续比较name或date，不支持size
                        if (o2.isFolder) {
                            return when(mSortType) {
                                SORT_TYPE_DATE -> compareLong(o1.lastModifyTime, o2.lastModifyTime)
                                else -> compareName(o1.namePinyin?:"", o2.namePinyin?:"")
                            }
                        }
                        else {
                            return -1
                        }
                    }
                    else {
                        if (o2.isFolder) {
                            return 1;
                        }
                        // 都是文件，继续比较name或date或size
                        else {
                            return when(mSortType) {
                                SORT_TYPE_DATE -> compareLong(o1.lastModifyTime, o2.lastModifyTime)
                                SORT_TYPE_SIZE -> compareLong(o1.size, o2.size)
                                else -> compareName(o1.name?:"", o2.name?:"")
                            }
                        }
                    }
                }
            })
            it.onNext(list)
            it.onComplete()
        }
    }

    fun onFilterChanged(text: String?) {
        mFilterText = text
        filterFiles()
            .flatMap { list -> sortFiles(list) }
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<MutableList<FileBean>>(getComposite()) {
                override fun onNext(list: MutableList<FileBean>) {
                    listObserver.value = list
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                    messageObserver.value = e?.message
                }
            })
    }

    fun clearFilter() {
        mFilterText = null
    }

    fun openFile(bean: FileBean) {
        val request = PathRequest()
        request.path = bean.path
        openServerFile(request)
    }

    private fun openServerFile(request: PathRequest) {
        loadingObserver.value = true
        AppHttpClient.getInstance().getAppService().openFileOnServer(request)
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<OpenFileResponse>(getComposite()) {
                override fun onNext(response: OpenFileResponse) {
                    loadingObserver.value = false
                    if (response.isSuccess) {
                        messageObserver.value = "打开成功"
                    } else {
                        messageObserver.value = response.errorMessage
                    }
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                    loadingObserver.value = false
                    messageObserver.value = e?.message
                }
            })
    }

    fun onSortTypeChanged(sortType: Int) {
        mSortType = sortType
        SettingProperty.setVideoServerSortType(sortType)
        onFilterChanged(mFilterText)
    }

}