package com.king.app.coolg_kt.page.video.server

import android.app.Application
import android.text.TextUtils
import android.view.View
import androidx.databinding.ObservableInt
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.conf.PreferenceValue
import com.king.app.coolg_kt.model.http.AppHttpClient
import com.king.app.coolg_kt.model.http.HttpConstants
import com.king.app.coolg_kt.model.http.bean.data.FileBean
import com.king.app.coolg_kt.model.http.bean.request.FolderRequest
import com.king.app.coolg_kt.model.http.bean.request.PathRequest
import com.king.app.coolg_kt.model.http.bean.response.OpenFileResponse
import com.king.app.coolg_kt.model.http.observer.SimpleObserver
import com.king.app.coolg_kt.model.setting.SettingProperty
import com.king.app.coolg_kt.page.video.player.PlayListInstance
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableSource
import java.util.*

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2019/11/11 9:10
 */
class VideoServerViewModel(application: Application) : BaseViewModel(application) {
    var upperVisibility = ObservableInt(View.GONE)
    var listObserver = MutableLiveData<MutableList<FileBean>>()
    private var mFolderList: MutableList<FileBean> = mutableListOf()
    private val mFolderStack = Stack<FileBean>()
    private var mFilterText: String? = null
    private var mSortType = SettingProperty.getVideoServerSortType()

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

    fun loadNewFolder(folder: FileBean?) {
        loadItems(true, folder, false, null)
    }

    fun loadItems(isNew: Boolean, folder: FileBean?, isBack: Boolean, popFolder: FileBean?) {
        val request = FolderRequest()
        request.type = HttpConstants.FOLDER_TYPE_FOLDER
        if (folder == null) {
            request.folder = HttpConstants.FOLDER_TYPE_ALL
        } else {
            request.folder = folder.path
        }
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
                    messageObserver.value = e?.message
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

    private fun sortFiles(list: MutableList<FileBean>): ObservableSource<MutableList<FileBean>> {
        return ObservableSource {
            when (mSortType) {
                PreferenceValue.VIDEO_SERVER_SORT_DATE -> {
                    list.sortByDescending { bean -> bean.lastModifyTime }
                }
                PreferenceValue.VIDEO_SERVER_SORT_SIZE -> {
                    list.sortByDescending { bean -> bean.size }
                }
                else -> {
                    list.sortBy { bean -> bean.name }
                }
            }
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

    fun createPlayList(url: String?) {
        PlayListInstance.getInstance().addUrl(url)
    }

}