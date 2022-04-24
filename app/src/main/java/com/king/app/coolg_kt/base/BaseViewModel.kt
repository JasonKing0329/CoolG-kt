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
import kotlin.coroutines.EmptyCoroutineContext

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

    fun dispatchCommonError(e: Throwable, errorTitle: String? = null) {
        loadingObserver.postValue(false)
        messageObserver.postValue(errorTitle?:"Load error: " + e.message)
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

    /**
     * 统一以弹提示的方式处理协程里的异常
     */
    private val commonHandler = CoroutineExceptionHandler{context, e ->
        dispatchCommonError(e)
    }


    /**
     * 在主线程中启动协程
     */
    fun launchMain(
        exceptionHandler: CoroutineExceptionHandler? = commonHandler,
        block: suspend CoroutineScope.() -> Unit
    ): Job {
        val context = exceptionHandler?: EmptyCoroutineContext
        return mainScope.launch(context) { block() }
    }

    /**
     * 在子线程中启动协程
     */
    fun launchThread(
        exceptionHandler: CoroutineExceptionHandler? = commonHandler,
        block: suspend CoroutineScope.() -> Unit
    ): Job {
        val context = exceptionHandler?: EmptyCoroutineContext
        return fixedScope.launch(context) { block() }
    }

    /**
     * 主线程启动协程，并完全处理loading, 异步, error事件
     */
    fun<T> launchSingle(
        block: suspend () -> T,
        exceptionHandler: CoroutineExceptionHandler? = commonHandler,
        withLoading: Boolean = false,
        onComplete: (T) -> Unit
    ): Job {
        return launchMain(exceptionHandler) {
            if (withLoading) {
                loadingObserver.value = true
            }
            val result = block()
            if (withLoading) {
                loadingObserver.value = false
            }
            if (isActive) {
                onComplete(result)
            }
        }
    }

    /**
     * 主线程启动协程，子线程处理异步，主线程处理loading, error事件
     */
    fun<T> launchSingleThread(
        block: suspend () -> T,
        exceptionHandler: CoroutineExceptionHandler? = commonHandler,
        withLoading: Boolean = false,
        onComplete: (T) -> Unit
    ): Job {
        return launchMain(exceptionHandler) {
            if (withLoading) {
                loadingObserver.value = true
            }
            val response = withContext(fixedPool) { block() }
            if (withLoading) {
                loadingObserver.value = false
            }
            if (isActive) {
                onComplete(response)
            }
        }
    }

    /**
     * 主线程启动flow，完全处理loading, 异步, error事件
     */
    fun<T> launchFlow(
        flow: Flow<T>,
        errorHandler: (Throwable) -> Unit? = { dispatchCommonError(it) },
        withLoading: Boolean = false,
        action: suspend (value: T) -> Unit
    ) {
        launchMain {
            flowCurrent(
                flow,
                errorHandler = errorHandler,
                withLoading = withLoading
            )
                .collect(action)
        }
    }

    /**
     * 主线程启动flow，子线程处理flow异步，主线程处理loading, error事件
     */
    fun<T> launchFlowThread(
        flow: Flow<T>,
        errorHandler: (Throwable) -> Unit? = { dispatchCommonError(it) },
        withLoading: Boolean = false,
        action: suspend (value: T) -> Unit
    ) {
        launchMain {
            flowThread(
                flow,
                errorHandler = errorHandler,
                withLoading = withLoading
            )
                .collect(action)
        }
    }

    /**
     * 当前线程协程处理flow任务，loading, error也统一交由当前线程处理
     */
    fun<T> flowCurrent(
        flow: Flow<T>,
        errorHandler: (Throwable) -> Unit? = { dispatchCommonError(it) },
        withLoading: Boolean = false
    ): Flow<T> {
        return flow
            .onStart {
                if (withLoading) {
                    loadingObserver.value = true
                }
            }
            .catch {
                errorHandler?.invoke(it)
            }
            .onCompletion {
                if (withLoading) {
                    loadingObserver.value = false
                }
            }
    }

    /**
     * 异步线程处理flow任务，loading, error统一交由当前线程处理
     */
    fun<T> flowThread(
        flow: Flow<T>,
        errorHandler: (Throwable) -> Unit? = { dispatchCommonError(it) },
        withLoading: Boolean = false
    ): Flow<T> {
        return flow
            .flowOn(fixedPool)
            .onStart {
                if (withLoading) {
                    loadingObserver.value = true
                }
            }
            .catch {
                errorHandler?.invoke(it)
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
     * @param withBasicLoading 加载basic期间是否展示等待框（waste属于数据后台更新，肯定不转圈）
     */
    fun<T> basicAndTimeWaste(
        blockBasic: suspend () -> List<T>,
        onCompleteBasic: (List<T>) -> Unit,
        blockWaste: suspend (Int, T) -> Unit,
        wasteNotifyCount: Int,
        onWasteRangeChanged: (Int, Int) -> Unit,
        withBasicLoading: Boolean = false,
    ): Job {
        if (withBasicLoading) {
            loadingObserver.value = true
        }
        return launchThread {
            DebugLog.e("basic start")
            val basic = blockBasic()
            withContext(Dispatchers.Main) {
                DebugLog.e("onCompleteBasic")
                if (withBasicLoading) {
                    loadingObserver.value = false
                }
                if (isActive) {
                    onCompleteBasic(basic)
                }
            }
            var index = 0
            DebugLog.e("waste start")
            while (isActive && index < basic.size) {
                blockWaste(index, basic[index])
                index ++

                // 每处理完wasteNotifyCount组数据通知UI变化
                if (index % wasteNotifyCount == 0) {
                    val start = index - wasteNotifyCount
                    withContext(Dispatchers.Main) {
                        DebugLog.e("onWasteRangeChanged start=$start, count=$wasteNotifyCount")
                        onWasteRangeChanged(start, wasteNotifyCount)
                    }
                }
            }
            // 还有剩余没通知的，最后通知
            if (index % wasteNotifyCount != 0) {
                val count = index - index / wasteNotifyCount * wasteNotifyCount
                val start = index - count
                DebugLog.e("onWasteRangeChanged start=$start, count=$count")
                withContext(Dispatchers.Main) {
                    onWasteRangeChanged(start, count)
                }
            }
        }
    }

}
