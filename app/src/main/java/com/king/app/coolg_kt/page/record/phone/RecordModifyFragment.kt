package com.king.app.coolg_kt.page.record.phone

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.king.app.coolg_kt.base.BaseFragment
import com.king.app.coolg_kt.databinding.FragmentRecordModifyBinding
import com.king.app.coolg_kt.model.http.bean.request.RecordUpdateStarItem
import com.king.app.coolg_kt.page.record.RecordViewModel
import com.king.app.coolg_kt.page.star.list.StarSelectorActivity
import com.king.app.gdb.data.DataConstants

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2022/6/10 9:23
 */
class RecordModifyFragment: BaseFragment<FragmentRecordModifyBinding, RecordModifyViewModel>() {

    private val REQUEST_STAR = 10

    private lateinit var mainViewModel: RecordViewModel
    private val starAdapter = ModifyStarAdapter()

    override fun createViewModel(): RecordModifyViewModel = generateViewModel(RecordModifyViewModel::class.java)

    override fun initView(view: View) {
        mainViewModel = getActivityViewModel(RecordViewModel::class.java)
        mBinding.rvStars.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        mBinding.ivAddCast.setOnClickListener { mModel.addNewStar() }
        mBinding.ivDeleteCast.setOnClickListener { starAdapter.toggleDelete() }
        starAdapter.onItemListener = object : ModifyStarAdapter.OnItemListener {
            override fun onDelete(position: Int, bean: RecordUpdateStarItem) {
                mModel.deleteStar(position)
            }

            override fun onClickImage(position: Int, bean: RecordUpdateStarItem) {
                mModel.mStarItemToSelect = bean
                StarSelectorActivity.startPage(this@RecordModifyFragment, true, requestCode = REQUEST_STAR)
            }
        }
        starAdapter.onDataChangedListener = object : ModifyStarAdapter.OnDataChangedListener {
            override fun onDataChanged() {
                mModel.isStarChanged = true
            }
        }
    }

    override fun getBinding(inflater: LayoutInflater): FragmentRecordModifyBinding = FragmentRecordModifyBinding.inflate(inflater)

    override fun initData() {
        mainViewModel.recordObserver.observe(this) { record ->
            mModel.mRecordWrap = record
            mModel.init()
            // basic
            mBinding.cbDeprecated.isChecked = record.bean.deprecated == 1
            mModel.createBasicList(requireContext()).forEach {
                mBinding.llBasic.addView(
                    it,
                    LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                )
            }
            // stars
            starAdapter.list = mModel.recordUpdateRequest.stars
            mBinding.rvStars.adapter = starAdapter

            // type
            mBinding.spTypes.setSelection(record.bean.type - 1)
            createTypes(record.bean.type)

            // setSelection会触发onItemSelected，所以初始化放在setSelection之后
            mBinding.spTypes.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val type = position + 1
                    if (mModel.isTypeChanged(type)) {
                        mBinding.llTypes.removeAllViews()
                        mModel.onTypeChanged(type)
                        createTypes(type)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }
            }
        }

        mModel.starObserver.observe(this) {
            starAdapter.list = it
            starAdapter.notifyDataSetChanged()
        }

        mModel.modifySuccess.observe(this) {
            mainViewModel.onRecordModified()
        }
    }

    private fun createTypes(type: Int) {
        when(type) {
            DataConstants.VALUE_RECORD_TYPE_1V1 -> {
                mModel.create1v1List(requireContext()).forEach {
                    mBinding.llTypes.addView(
                        it,
                        LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                    )
                }
            }
            else -> {
                mModel.create3wList(requireContext()).forEach {
                    mBinding.llTypes.addView(
                        it,
                        LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                    )
                }
            }
        }
    }

    fun isDataChanged(): Boolean {
        return mModel.isDataChanged(mBinding.cbDeprecated.isChecked, getSelectedType())
    }

    private fun getSelectedType(): Int {
        return mBinding.spTypes.selectedItemPosition + 1
    }

    fun executeModify(): Boolean {
        return if (isDataChanged()) {
            mModel.executeModify(mBinding.cbDeprecated.isChecked)
            true
        }
        else {
            false
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            REQUEST_STAR -> {
                data?.getCharSequenceArrayListExtra(StarSelectorActivity.RESP_SELECT_RESULT)?.apply {
                    mModel.updateStarToSelect(firstOrNull()?.toString()?.toLongOrNull()?:0)
                    mModel.mStarItemToSelect?.apply {
                        starAdapter.notifyItemChanged(this)
                    }
                }
            }
        }
    }
}