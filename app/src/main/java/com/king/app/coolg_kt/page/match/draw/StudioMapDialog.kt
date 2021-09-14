package com.king.app.coolg_kt.page.match.draw

import android.view.LayoutInflater
import androidx.recyclerview.widget.LinearLayoutManager
import com.king.app.coolg_kt.databinding.FragmentDialogStudioMapBinding
import com.king.app.coolg_kt.model.http.observer.SimpleObserver
import com.king.app.coolg_kt.model.repository.OrderRepository
import com.king.app.coolg_kt.page.match.DrawItem
import com.king.app.coolg_kt.page.match.StudioMapItem
import com.king.app.coolg_kt.view.dialog.DraggableContentFragment
import com.king.app.gdb.data.relation.MatchRecordWrap
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/6/10 14:35
 */
class StudioMapDialog: DraggableContentFragment<FragmentDialogStudioMapBinding>() {

    var drawItems: List<DrawItem>? = null
    var orderRepository = OrderRepository()

    val UNKNOWN = "Unknown"

    override fun getBinding(inflater: LayoutInflater): FragmentDialogStudioMapBinding = FragmentDialogStudioMapBinding.inflate(inflater)

    override fun initData() {
        mBinding.rvList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        groupData()
    }

    private fun groupData() {
        getData()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe(object : SimpleObserver<List<StudioMapItem>>(compositeDisposable) {
                override fun onNext(t: List<StudioMapItem>) {
                    showGroupData(t)
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                }

            })
    }

    private fun showGroupData(t: List<StudioMapItem>) {
        val adapter = StudioMapAdapter()
        adapter.list = t
        mBinding.rvList.adapter = adapter
    }

    private fun getData(): Observable<List<StudioMapItem>> {
        return Observable.create {
            val items = mutableListOf<StudioMapItem>()
            drawItems?.forEach { drawItem ->
                drawItem.matchRecord1?.let { record -> checkRecord(record, items) }
                drawItem.matchRecord2?.let { record -> checkRecord(record, items) }
                drawItem.winner?.let { record -> checkWinner(record, items) }
            }
            items.sortByDescending { item -> item.count }
            it.onNext(items)
            it.onComplete()
        }
    }

    private fun checkRecord(record: MatchRecordWrap, items: MutableList<StudioMapItem>) {
        if (record.bean.recordId != 0.toLong()) {
            var studio = orderRepository.getRecordStudio(record.bean.recordId)?.name
            if (studio == null) {
                studio = UNKNOWN
            }
            var item = findStudio(studio, items)
            if (item == null) {
                items.add(StudioMapItem(studio, 1, 0))
            }
            else {
                item.count ++
            }
        }
    }

    private fun checkWinner(record: MatchRecordWrap, items: MutableList<StudioMapItem>) {
        if (record.bean.recordId != 0.toLong()) {
            var studio = orderRepository.getRecordStudio(record.bean.recordId)?.name
            if (studio == null) {
                studio = UNKNOWN
            }
            findStudio(studio, items)?.apply {
                winCount ++
            }
        }
    }

    private fun findStudio(name: String, items: MutableList<StudioMapItem>): StudioMapItem? {
        for (item in items) {
            if (item.studio == name) {
                return item
            }
        }
        return null
    }
}