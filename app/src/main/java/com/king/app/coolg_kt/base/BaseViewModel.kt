package com.king.app.coolg_kt.base

import android.app.Application
import android.content.res.Resources
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.CoolApplication
import com.king.app.coolg_kt.utils.DebugLog
import com.king.app.gdb.data.AppDatabase
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.ObservableTransformer
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * 描述:
 *
 * 作者：景阳
 *
 * 创建时间: 2020/1/21 15:02
 */
abstract class BaseViewModel(application: Application) : AndroidViewModel(application) {

    var compositeDisposable: CompositeDisposable = CompositeDisposable()

    var loadingObserver = MutableLiveData<Boolean>()
    var messageObserver = MutableLiveData<String>()

    val fixedPool = newFixedThreadPoolContext(5, "Fixed")
    val fixedScope = CoroutineScope(SupervisorJob() + fixedPool)
    val mainScope = MainScope()

    fun addDisposable(disposable: Disposable) {
        compositeDisposable.add(disposable)
    }

    fun dispatchCommonError(e: Throwable) {
        messageObserver.value = "Load error: " + e.message
    }

    fun dispatchCommonError(errorTitle: String, e: Throwable) {
        messageObserver.value = errorTitle + ": " + e.message
    }

    fun getComposite() = compositeDisposable

    open fun onDestroy() {
        compositeDisposable.clear()
        mainScope.cancel()
        fixedScope.cancel()
    }

    fun showLoading(show: Boolean){
        loadingObserver.value=show
    }

    val former = ObservableTransformer<Any, Any> { upstream ->
        upstream
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun <T> applySchedulers(): ObservableTransformer<T, T> {
        return former as ObservableTransformer<T, T>
    }

    fun getResource(): Resources = getApplication<Application>().resources

    fun getDatabase(): AppDatabase {
        mainScope.launch {  }
        return CoolApplication.instance.database!!
    }

    fun launchMain(block: suspend CoroutineScope.() -> Unit) {
        mainScope.launch { block() }
    }

    fun launchThread(block: suspend CoroutineScope.() -> Unit) {
        fixedScope.launch { block() }
    }

    /**
     * 主线程协程完全处理flow事件与统一的loading, error事件
     */
    fun<T> launchSingle(
        block: suspend () -> T,
        withLoading: Boolean = false,
        onComplete: (T) -> Unit) {
        launchMain {
            if (withLoading) {
                loadingObserver.value = true
            }
            onComplete(block())
            if (withLoading) {
                loadingObserver.value = false
            }
        }
    }

    /**
     * 主线程协程完全处理flow事件与统一的loading, error事件
     */
    fun<T> launchSingleThread(
        block: suspend () -> T,
        withLoading: Boolean = false,
        onComplete: (T) -> Unit) {
        launchMain {
            if (withLoading) {
                loadingObserver.value = true
            }
            val response = withContext(fixedPool) { block() }
            onComplete(response)
            if (withLoading) {
                loadingObserver.value = false
            }
        }
    }

    /**
     * 主线程协程完全处理flow事件与统一的loading, error事件
     */
    fun<T> launchFlow(
        flow: Flow<T>,
        withLoading: Boolean = false,
        action: suspend (value: T) -> Unit) {
        launchMain {
            flowCurrent(flow, withLoading)
                .collect(action)
        }
    }

    /**
     * 主线程协程，线程池处理flow，主线程处理统一的loading, error事件
     */
    fun<T> launchFlowThread(
        flow: Flow<T>,
        withLoading: Boolean = false,
        action: suspend (value: T) -> Unit) {
        launchMain {
            flowThread(flow, withLoading)
                .collect(action)
        }
    }

    /**
     * 异步线程处理flow任务，loading, error统一交由当前线程处理
     */
    fun<T> flowThread(flow: Flow<T>, withLoading: Boolean = false): Flow<T> {
        return flow
            .flowOn(fixedPool)
            .onStart {
                if (withLoading) {
                    loadingObserver.value = true
                }
            }
            .catch {
                it.printStackTrace()
                messageObserver.value = it.message
            }
            .onCompletion {
                if (withLoading) {
                    loadingObserver.value = false
                }
            }
    }

    /**
     * 当前线程协程处理flow任务，loading, error也统一交由当前线程处理
     */
    fun<T> flowCurrent(flow: Flow<T>, withLoading: Boolean = false): Flow<T> {
        return flow
            .onStart {
                if (withLoading) {
                    loadingObserver.value = true
                }
            }
            .catch {
                it.printStackTrace()
                messageObserver.value = it.message
            }
            .onCompletion {
                if (withLoading) {
                    loadingObserver.value = false
                }
            }
    }

    /**
     * 先加载基础列表List<T>通知UI加载列表，
     * 然后异步更新每一个item比较耗时的数据，并指定每加载完wasteNotifyCount个时更新
     * @param blockBasic 子线程加载基础List<T>
     * @param onCompleteBasic 基础List加载完成，Main线程通知UI更新
     * @param blockWaste 子线程加载每个item对应的耗时任务
     * @param wasteNotifyCount 指定每隔几个通知UI更新
     * @param onWasteRangeChanged 耗时数据加载完，通知UI List待更新的范围，(start, count)
     */
    fun<T> basicAndTimeWaste(
        blockBasic: suspend () -> List<T>,
        onCompleteBasic: (List<T>) -> Unit,
        blockWaste: suspend (T) -> Unit,
        wasteNotifyCount: Int,
        onWasteRangeChanged: (Int, Int) -> Unit
    ) {
        launchThread {
            val basic = blockBasic()
            withContext(Dispatchers.Main) {
                DebugLog.e("onCompleteBasic")
                onCompleteBasic(basic)
            }
            var index = 0
            while (isActive && index < basic.size) {
                blockWaste(basic[index])

                // 每处理完wasteNotifyCount组数据通知UI变化
                if ((index + 1) % wasteNotifyCount == 0) {
                    withContext(Dispatchers.Main) {
                        DebugLog.e("onWasteRangeChanged start=$index, count=$wasteNotifyCount")
                        onWasteRangeChanged(index, wasteNotifyCount)
                    }
                }
                index ++
            }
        }
    }

}
