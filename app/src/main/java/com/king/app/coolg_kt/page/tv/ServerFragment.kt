package com.king.app.coolg_kt.page.tv

import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.king.app.coolg_kt.base.BaseFragment
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.databinding.FragmentTvServerBinding
import com.king.app.coolg_kt.model.setting.SettingProperty
import com.king.app.coolg_kt.model.udp.ServerBody
import com.king.app.coolg_kt.utils.ScreenUtils
import com.king.app.coolg_kt.view.dialog.SimpleDialogs

/**
 * @description:
 * @author：Jing
 * @date: 2021/2/13 17:46
 */
class ServerFragment: BaseFragment<FragmentTvServerBinding, ServerViewModel>() {

    val adapter = ServerAdapter()

    override fun createViewModel(): ServerViewModel = generateViewModel(ServerViewModel::class.java)

    override fun getBinding(inflater: LayoutInflater): FragmentTvServerBinding = FragmentTvServerBinding.inflate(inflater)

    override fun initView(view: View) {
        mBinding.rvList.layoutManager = GridLayoutManager(requireContext(), 4)
        mBinding.rvList.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                val position = parent.getChildLayoutPosition(view)
                outRect.top = if (position / 4 == 0) {
                    0
                }
                else {
                    ScreenUtils.dp2px(10f)
                }
                outRect.left = if (position % 4 == 0) {
                    0
                }
                else {
                    ScreenUtils.dp2px(10f)
                }
            }
        })

        mBinding.ivEdit.setOnClickListener {
            SimpleDialogs().openInputDialog(requireContext(), "Set server IP", mModel.getManuelServer()
            ) { name -> mModel.updateServerIp(name) }
        }
        adapter.setOnItemClickListener(object : BaseBindingAdapter.OnItemClickListener<ServerBody> {
            override fun onClickItem(view: View, position: Int, data: ServerBody) {
                mModel.connectToServer(data)
            }
        })
        mBinding.rvList.adapter = adapter
    }

    override fun initData() {
        mModel.connectSuccess.observe(this, Observer {
            getActivityViewModel(TvViewModel::class.java).goToServer.value = true
        })
        mModel.serversObserver.observe(this, Observer {
            adapter.list = it
            adapter.notifyDataSetChanged()
        })
        // 先加载本地保存的
        mModel.loadServers()
        // 接受服务端广播IP，更新或添加IP地址
        mModel.onReceiveIp()
    }

}