package com.king.app.coolg_kt.page.studio

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BaseFragment
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.conf.AppConstants
import com.king.app.coolg_kt.databinding.FragmentStudioListBinding
import com.king.app.coolg_kt.utils.ScreenUtils
import com.king.app.coolg_kt.view.dialog.AlertDialogFragment
import com.king.app.coolg_kt.view.dialog.SimpleDialogs
import com.king.app.gdb.data.entity.FavorRecordOrder

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2020/12/25 16:08
 */
class StudioListFragment: BaseFragment<FragmentStudioListBinding, StudioViewModel>() {

    var holder: StudioHolder? = null

    var isDeleting = false

    companion object {

        val ARG_SELECT_MODE = "select_mode"
        val ARG_SELECT_AS_MATCH = "select_as_match"

        fun newInstance(selectMode: Boolean, selectAsMatch: Boolean): StudioListFragment {
            val fragment = StudioListFragment()
            val bundle = Bundle()
            bundle.putBoolean(ARG_SELECT_MODE, selectMode)
            bundle.putBoolean(ARG_SELECT_AS_MATCH, selectAsMatch)
            fragment.arguments = bundle
            return fragment
        }
    }

    private var gridAdapter = StudioGridAdapter()
    private var simpleAdapter = StudioSimpleAdapter()
    private var richAdapter = StudioRichAdapter()

    override fun getBinding(inflater: LayoutInflater): FragmentStudioListBinding = FragmentStudioListBinding.inflate(inflater)

    override fun createViewModel(): StudioViewModel = generateViewModel(StudioViewModel::class.java)

    override fun initView(view: View) {

        if (isSelectMode() && ScreenUtils.isTablet()) {
            mBinding.rvList.layoutManager = GridLayoutManager(activity, 3)
        } else {
            mBinding.rvList.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        }

        initMenu()

        simpleAdapter.onEditListener = object : StudioSimpleAdapter.OnEditListener {
            override fun onEditItem(position: Int, bean: StudioSimpleItem) {
                modifyStudioName(position, bean)
            }
        }
        simpleAdapter.setOnItemClickListener(object : BaseBindingAdapter.OnItemClickListener<StudioSimpleItem>{
            override fun onClickItem(view: View, position: Int, data: StudioSimpleItem) {
                if (isDeleting) {
                    warningDelete(data.order)
                }
                else {
                    onClickOrder(data.order)
                }
            }
        })
        gridAdapter.onEditListener = object : StudioGridAdapter.OnEditListener {
            override fun onEditItem(position: Int, bean: StudioSimpleItem) {
                modifyStudioName(position, bean)
            }
        }
        gridAdapter.setOnItemClickListener(object : BaseBindingAdapter.OnItemClickListener<StudioSimpleItem>{
            override fun onClickItem(view: View, position: Int, data: StudioSimpleItem) {
                if (isDeleting) {
                    warningDelete(data.order)
                }
                else {
                    onClickOrder(data.order)
                }
            }
        })
        richAdapter.setOnItemClickListener(object : BaseBindingAdapter.OnItemClickListener<StudioRichItem>{
            override fun onClickItem(view: View, position: Int, data: StudioRichItem) {
                if (isDeleting) {
                    warningDelete(data.order)
                }
                else {
                    onClickOrder(data.order)
                }
            }
        })

    }

    private fun warningDelete(order: FavorRecordOrder) {
        showConfirmCancelMessage("This action will delete all video relationships under selected studio, do you want to continue?",
            DialogInterface.OnClickListener { dialog, which ->  mModel.deleteStudio(order)},
            null)
    }

    private fun initMenu() {
        holder?.getJActionBar()?.setOnMenuItemListener { menuId ->
            when (menuId) {
                R.id.menu_mode -> chooseDisplayMode()
                R.id.menu_add -> {
                    showConfirmCancelMessage("App端修改Studio不会与服务端同步，需要在服务端手动修改，是否继续？",
                        { dialog, which -> addNewStudio() },
                        null
                    )
                }
                R.id.menu_delete -> {
                    showConfirmCancelMessage("App端修改Studio不会与服务端同步，需要在服务端手动修改，是否继续？",
                        { dialog, which ->
                            isDeleting = true
                            holder?.getJActionBar()?.showConfirmStatus(menuId)
                        },
                        null
                    )
                }
            }
        }
        holder?.getJActionBar()?.setOnConfirmListener {
            isDeleting = false
            true
        }
        holder?.getJActionBar()?.setOnCancelListener {
            isDeleting = false
            true
        }

        holder?.getJActionBar()?.registerPopupMenuOn(
            R.id.menu_sort,
            R.menu.studios_sort
        ) {
            when (it.itemId) {
                R.id.menu_sort_name -> mModel.onSortTypeChanged(AppConstants.STUDIO_LIST_SORT_NAME)
                R.id.menu_sort_items -> mModel.onSortTypeChanged(AppConstants.STUDIO_LIST_SORT_NUM)
                R.id.menu_sort_create_time -> mModel.onSortTypeChanged(AppConstants.STUDIO_LIST_SORT_CREATE_TIME)
                R.id.menu_sort_update_time -> mModel.onSortTypeChanged(AppConstants.STUDIO_LIST_SORT_UPDATE_TIME)
            }
            true
        }
    }

    private fun chooseDisplayMode() {
        var items = arrayOf("Simple List", "Simple Grid", "Rich List")
        AlertDialogFragment()
            .setItems(items
            ) { dialog, which -> mModel.toggleListType(which) }
            .show(childFragmentManager, "chooseDisplayMode")
    }

    private fun modifyStudioName(position: Int, simpleItem: StudioSimpleItem) {

        showConfirmCancelMessage("App端修改Studio不会与服务端同步，需要在服务端手动修改，是否继续？",
            { dialog, which ->
                SimpleDialogs().openInputDialog(
                    requireContext(),
                    "Modify studio's name",
                    simpleItem.name
                ) {
                    if (it.trim().isEmpty()) {
                        showMessageShort("Empty words")
                    }
                    else {
                        mModel.updateStudioName(simpleItem.order, it)
                        simpleItem.name = it
                        commonAdapterFunc().notifyItemChanged(position)
                    }
                }
            },
            null
        )
    }

    private fun addNewStudio() {
        SimpleDialogs().openInputDialog(
            requireContext(),
            "Input studio's name"
        ) { mModel.addNewStudio(it) }
    }

    override fun initData() {
        mModel.simpleObserver.observe(this, Observer{ list -> showSimpleList(list) })
        mModel.richObserver.observe(this, Observer{ list -> showRichList(list) })

        mModel.isSelectAsMatch = isSelectAsMatch()
        mModel.loadStudios()
    }

    private fun isSelectMode(): Boolean {
        return requireArguments().getBoolean(ARG_SELECT_MODE)
    }

    private fun isSelectAsMatch(): Boolean {
        return requireArguments().getBoolean(ARG_SELECT_AS_MATCH)
    }

    fun resetMenu() {
        holder?.getJActionBar()?.setMenu(R.menu.studios)
        initMenu()
    }

    private fun showSimpleList(list: List<StudioSimpleItem>) {
        if (mModel.isGridType()) {
            mBinding.rvList.layoutManager = GridLayoutManager(context, 3)
            gridAdapter.list = list
            mBinding.rvList.adapter = gridAdapter
        }
        else {
            mBinding.rvList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            simpleAdapter.list = list
            mBinding.rvList.adapter = simpleAdapter
        }
    }

    private fun showRichList(list: List<StudioRichItem>) {
        mBinding.rvList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        richAdapter.list = list
        mBinding.rvList.adapter = richAdapter
    }

    private fun commonAdapterFunc(): RecyclerView.Adapter<*> {
        return when {
            mModel.isRichType() -> richAdapter
            mModel.isGridType() -> gridAdapter
            else -> simpleAdapter
        }
    }

    private fun onClickOrder(order: FavorRecordOrder) {
        if (isSelectMode()) {
            holder?.sendSelectedOrderResult(order.id)
        } else {
            holder?.showStudioPage(order.id!!, order.name)
        }
    }

}