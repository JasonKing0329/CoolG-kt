package com.king.app.coolg_kt.base

import android.app.Application
import android.content.res.Resources
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.CoolApplication
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
}
