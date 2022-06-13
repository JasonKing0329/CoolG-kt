package com.king.app.coolg_kt.page.record.phone

import android.app.Application
import android.content.Context
import android.text.InputType
import android.view.View
import android.widget.LinearLayout
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.model.bean.ModifyInputItem
import com.king.app.coolg_kt.model.http.AppHttpClient
import com.king.app.coolg_kt.model.http.bean.request.RecordUpdateRequest
import com.king.app.coolg_kt.model.http.bean.request.RecordUpdateStarItem
import com.king.app.coolg_kt.model.image.ImageProvider
import com.king.app.coolg_kt.model.repository.RecordRepository
import com.king.app.coolg_kt.utils.ScreenUtils
import com.king.app.coolg_kt.view.widget.KeyValueEditView
import com.king.app.gdb.data.DataConstants
import com.king.app.gdb.data.entity.Record
import com.king.app.gdb.data.entity.RecordType1v1
import com.king.app.gdb.data.entity.RecordType3w
import com.king.app.gdb.data.relation.RecordWrap

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2022/6/10 9:24
 */
class RecordModifyViewModel(application: Application): BaseViewModel(application) {

    private var allInputList = mutableListOf<ModifyInputItem>()

    private var computeScoreList = mutableListOf<ModifyInputItem>()

    private var scoreInputItem: ModifyInputItem? = null

    var starObserver = MutableLiveData<List<RecordUpdateStarItem>>()

    var modifySuccess = MutableLiveData<Boolean>()

    private val repository = RecordRepository()

    val inputPaddingHor = ScreenUtils.dp2px(16f)
    val inputPaddingVer = ScreenUtils.dp2px(8f)
    val inputTextSize = ScreenUtils.dp2px(14f)

    var initType = 0
    var initDeprecated = false
    var currentType = 0

    var mRecordWrap: RecordWrap? = null
    
    val recordUpdateRequest = RecordUpdateRequest()
    private var recordType1v1: RecordType1v1? = null
    private var recordType3w: RecordType3w? = null

    var isStarChanged = false

    var mStarItemToSelect: RecordUpdateStarItem? = null

    fun init() {
        if (mRecordWrap == null) {
            recordUpdateRequest.record = Record(0, "", "", "", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
            recordUpdateRequest.stars = mutableListOf()
            // 新增默认是1v1
            initType = DataConstants.VALUE_RECORD_TYPE_1V1
            newType1v1()
        }
        else {
            initType = mRecordWrap!!.bean.type
            recordUpdateRequest.record = mRecordWrap!!.bean
            recordType1v1 = mRecordWrap!!.recordType1v1
            recordType3w = mRecordWrap!!.recordType3w
            recordUpdateRequest.stars = mRecordWrap!!.recordStars.map {
                RecordUpdateStarItem().apply {
                    starId = it.starId
                    starName = getDatabase().getStarDao().getStar(it.starId)?.name
                    score = it.score
                    scoreC = it.scoreC
                    type = it.type
                    imageUrl = ImageProvider.getStarRandomPath(starName, null)
                }
            }.toMutableList()
        }
        currentType = initType
    }

    fun onTypeChanged(type: Int) {
        when(type) {
            DataConstants.VALUE_RECORD_TYPE_1V1 -> {
                if (recordType1v1 == null) {
                    newType1v1()
                }
                recordType3w = null
            }
            else -> {
                if (recordType3w == null) {
                    newType3w()
                }
                recordType1v1 = null
            }
        }
        currentType = type
    }

    private fun newType1v1() {
        recordType1v1 = RecordType1v1(0)
    }

    private fun newType3w() {
        recordType3w = RecordType3w(0)
    }

    fun createBasicList(context: Context): List<View> {
        val viewList = mutableListOf<View>()
        recordUpdateRequest.record?.apply {
            val path = ModifyInputItem(
                newKeyValueEditView(context, "Path", directory, inputWidth = ScreenUtils.dp2px(270f))
            ) { directory = it }
            allInputList.add(path)
            viewList.add(path.edit)
            val name = ModifyInputItem(
                newKeyValueEditView(context, "Name", name, inputWidth = ScreenUtils.dp2px(270f))
            ) { name = it }
            allInputList.add(name)
            viewList.add(name.edit)
            val scene = ModifyInputItem(
                newKeyValueEditView(context, "Scene", scene, inputWidth = ScreenUtils.dp2px(80f))
            ) { scene = it }
            allInputList.add(scene)
            val hd = ModifyInputItem(
                newKeyValueEditView(context, "HD", hdLevel.toString(), InputType.TYPE_CLASS_NUMBER)
            ) { hdLevel = it.toIntOrNull()?:0 }
            allInputList.add(hd)
            viewList.add(newRow(context, scene.edit, hd.edit))

            ModifyInputItem(
                newKeyValueEditView(context, "Score", score.toString(), InputType.TYPE_CLASS_NUMBER)
            ) { score = it.toIntOrNull()?:0 }.apply {
                allInputList.add(this)
                viewList.add(edit)
                scoreInputItem = this
            }
            val feel = ModifyInputItem(
                newKeyValueEditView(context, "Feel", scoreFeel.toString(), InputType.TYPE_CLASS_NUMBER)
            ) { scoreFeel = it.toIntOrNull()?:0 }
            allInputList.add(feel)
            computeScoreList.add(feel)
            val passion = ModifyInputItem(
                newKeyValueEditView(context, "Passion", scorePassion.toString(), InputType.TYPE_CLASS_NUMBER)
            ) { scorePassion = it.toIntOrNull()?:0 }
            allInputList.add(passion)
            computeScoreList.add(passion)
            viewList.add(newRow(context, feel.edit, passion.edit))
            val star = ModifyInputItem(
                newKeyValueEditView(context, "Star", scoreStar.toString(), InputType.TYPE_CLASS_NUMBER)
            ) { scoreStar = it.toIntOrNull()?:0 }
            allInputList.add(star)
            computeScoreList.add(star)
            val body = ModifyInputItem(
                newKeyValueEditView(context, "Body", scoreBody.toString(), InputType.TYPE_CLASS_NUMBER)
            ) { scoreBody = it.toIntOrNull()?:0 }
            allInputList.add(body)
            computeScoreList.add(body)
            viewList.add(newRow(context, star.edit, body.edit))
            val cum = ModifyInputItem(
                newKeyValueEditView(context, "Cum", scoreCum.toString(), InputType.TYPE_CLASS_NUMBER)
            ) { scoreCum = it.toIntOrNull()?:0 }
            allInputList.add(cum)
            computeScoreList.add(cum)
            val bare = ModifyInputItem(
                newKeyValueEditView(context, "Bareback", scoreBareback.toString(), InputType.TYPE_CLASS_NUMBER)
            ) { scoreBareback = it.toIntOrNull()?:0 }
            allInputList.add(bare)
            computeScoreList.add(bare)
            viewList.add(newRow(context, cum.edit, bare.edit))
            val cock = ModifyInputItem(
                newKeyValueEditView(context, "Cock", scoreCock.toString(), InputType.TYPE_CLASS_NUMBER)
            ) { scoreCock = it.toIntOrNull()?:0 }
            allInputList.add(cock)
            computeScoreList.add(cock)
            val ass = ModifyInputItem(
                newKeyValueEditView(context, "Ass", scoreAss.toString(), InputType.TYPE_CLASS_NUMBER)
            ) { scoreAss = it.toIntOrNull()?:0 }
            allInputList.add(ass)
            computeScoreList.add(ass)
            viewList.add(newRow(context, cock.edit, ass.edit))
            val special = ModifyInputItem(
                newKeyValueEditView(context, "Special", scoreSpecial.toString(), InputType.TYPE_CLASS_NUMBER)
            ) { scoreSpecial = it.toIntOrNull()?:0 }
            allInputList.add(special)
            computeScoreList.add(special)
            viewList.add(special.edit)
            val desc = ModifyInputItem(
                newKeyValueEditView(context, "Special Desc.", specialDesc, inputWidth = ScreenUtils.dp2px(200f))
            ) { specialDesc = it }
            allInputList.add(desc)
            viewList.add(desc.edit)
        }

        computeScoreList.forEach {
            it.edit.listenInput { onScoreChanged() }
        }
        return viewList
    }

    private fun onScoreChanged() {
        scoreInputItem?.edit?.setValue(computeScoreList.sumOf { it.edit.editValue?.toIntOrNull()?:0 }.toString())
    }

    fun create1v1List(context: Context): List<View> {
        val viewList = mutableListOf<View>()
        recordType1v1?.let { record ->
            val t1 = ModifyInputItem(
                newKeyValueEditView(context, "SitFront", record.scoreFkType1.toString(), InputType.TYPE_CLASS_NUMBER)
            ) { record.scoreFkType1 = it.toIntOrNull()?:0 }
            allInputList.add(t1)
            val t2 = ModifyInputItem(
                newKeyValueEditView(context, "SitBack", record.scoreFkType2.toString(), InputType.TYPE_CLASS_NUMBER)
            ) { record.scoreFkType2 = it.toIntOrNull()?:0 }
            allInputList.add(t2)
            viewList.add(newRow(context, t1.edit, t2.edit))
            val t3 = ModifyInputItem(
                newKeyValueEditView(context, "StandFront", record.scoreFkType3.toString(), InputType.TYPE_CLASS_NUMBER)
            ) { record.scoreFkType3 = it.toIntOrNull()?:0 }
            allInputList.add(t3)
            val t4 = ModifyInputItem(
                newKeyValueEditView(context, "StandBack", record.scoreFkType4.toString(), InputType.TYPE_CLASS_NUMBER)
            ) { record.scoreFkType4 = it.toIntOrNull()?:0 }
            allInputList.add(t4)
            viewList.add(newRow(context, t3.edit, t4.edit))
            val t5 = ModifyInputItem(
                newKeyValueEditView(context, "Side", record.scoreFkType5.toString(), InputType.TYPE_CLASS_NUMBER)
            ) { record.scoreFkType5 = it.toIntOrNull()?:0 }
            allInputList.add(t5)
            val t6 = ModifyInputItem(
                newKeyValueEditView(context, "Special", record.scoreFkType6.toString(), InputType.TYPE_CLASS_NUMBER)
            ) { record.scoreFkType6 = it.toIntOrNull()?:0 }
            allInputList.add(t6)
            viewList.add(newRow(context, t5.edit, t6.edit))
        }
        return viewList
    }

    fun create3wList(context: Context): List<View> {
        val viewList = mutableListOf<View>()
        recordType3w?.let { record ->
            val t1 = ModifyInputItem(
                newKeyValueEditView(context, "SitFront", record.scoreFkType1.toString(), InputType.TYPE_CLASS_NUMBER)
            ) { record.scoreFkType1 = it.toIntOrNull()?:0 }
            allInputList.add(t1)
            val t2 = ModifyInputItem(
                newKeyValueEditView(context, "SitBack", record.scoreFkType2.toString(), InputType.TYPE_CLASS_NUMBER)
            ) { record.scoreFkType2 = it.toIntOrNull()?:0 }
            allInputList.add(t2)
            viewList.add(newRow(context, t1.edit, t2.edit))
            val t3 = ModifyInputItem(
                newKeyValueEditView(context, "StandFront", record.scoreFkType3.toString(), InputType.TYPE_CLASS_NUMBER)
            ) { record.scoreFkType3 = it.toIntOrNull()?:0 }
            allInputList.add(t3)
            val t4 = ModifyInputItem(
                newKeyValueEditView(context, "StandBack", record.scoreFkType4.toString(), InputType.TYPE_CLASS_NUMBER)
            ) { record.scoreFkType4 = it.toIntOrNull()?:0 }
            allInputList.add(t4)
            viewList.add(newRow(context, t3.edit, t4.edit))
            val t5 = ModifyInputItem(
                newKeyValueEditView(context, "Side", record.scoreFkType5.toString(), InputType.TYPE_CLASS_NUMBER)
            ) { record.scoreFkType5 = it.toIntOrNull()?:0 }
            allInputList.add(t5)
            val t6 = ModifyInputItem(
                newKeyValueEditView(context, "Double", record.scoreFkType6.toString(), InputType.TYPE_CLASS_NUMBER)
            ) { record.scoreFkType6 = it.toIntOrNull()?:0 }
            allInputList.add(t6)
            viewList.add(newRow(context, t5.edit, t6.edit))
            val t7 = ModifyInputItem(
                newKeyValueEditView(context, "Sequence", record.scoreFkType7.toString(), InputType.TYPE_CLASS_NUMBER)
            ) { record.scoreFkType7 = it.toIntOrNull()?:0 }
            allInputList.add(t7)
            val t8 = ModifyInputItem(
                newKeyValueEditView(context, "Special", record.scoreFkType8.toString(), InputType.TYPE_CLASS_NUMBER)
            ) { record.scoreFkType8 = it.toIntOrNull()?:0 }
            allInputList.add(t8)
            viewList.add(newRow(context, t7.edit, t8.edit))
        }
        return viewList
    }

    fun isDataChanged(isDeprecated: Boolean, type: Int): Boolean {
        if (isDeprecated != initDeprecated) {
            return true
        }
        if (type != initType) {
            return true
        }
        if (isStarChanged) {
            return true
        }
        return allInputList.any { it.isChanged() }
    }

    fun executeModify(isDeprecated: Boolean) {
        mRecordWrap?.apply {
            bean.deprecated = if (isDeprecated) 1 else 0
            bean.type = currentType
            allInputList.forEach { it.confirm() }
            recordUpdateRequest.recordType1v1 = this@RecordModifyViewModel.recordType1v1
            recordUpdateRequest.recordType3w = this@RecordModifyViewModel.recordType3w
            launchThread {
                AppHttpClient.getInstance().getAppServiceCoroutine().apply {
                    val isOnline = kotlin.runCatching { isServerOnline().isOnline }.getOrDefault(false)
                    // 服务端在线交由服务端直接修改
                    val isServeModify = isOnline &&
                            kotlin.runCatching {
                                modifyRecord(recordUpdateRequest)
                                true
                            }.getOrDefault(false)
                    // 服务端不在线或服务端修改失败，将本次修改记录到本地，下次服务端在线时再提交
                    if (!isServeModify) {
                        repository.saveLocalModify(recordUpdateRequest)
                        messageObserver.postValue("saved to local")
                    }
                    // 最后都要修改本地record数据
                    repository.modifyRecord(recordUpdateRequest)
                }
                modifySuccess.postValue(true)
            }
        }
    }

    private fun newKeyValueEditView(
        context: Context,
        key: String, initValue: String?,
        inputType: Int? = null,
        inputWidth: Int? = null
    ): KeyValueEditView {
        return KeyValueEditView(context).apply {
            setKeyText(key)
            inputTextSize = this@RecordModifyViewModel.inputTextSize
            setValue(initValue)
            setPadding(inputPaddingHor, inputPaddingVer, inputPaddingHor, inputPaddingVer)
            inputType?.let { setInputType(it) }
            inputWidth?.let { this.inputWidth = it }
        }
    }

    private fun newRow(context: Context, child1: View, child2: View): LinearLayout {
        return  LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            addView(child1, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT).apply { weight = 0.5f })
            addView(child2, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT).apply { weight = 0.5f })
        }
    }

    fun isTypeChanged(type: Int): Boolean {
        return type != currentType
    }

    fun deleteStar(position: Int) {
        isStarChanged = true
        recordUpdateRequest.stars?.removeAt(position)
        starObserver.value = recordUpdateRequest.stars
    }

    fun addNewStar() {
        recordUpdateRequest.stars?.add(RecordUpdateStarItem())
        starObserver.value = recordUpdateRequest.stars
    }

    fun updateStarToSelect(starId: Long) {
        getDatabase().getStarDao().getStar(starId)?.apply {
            mStarItemToSelect?.starName = name
            mStarItemToSelect?.starId = starId
            mStarItemToSelect?.imageUrl = ImageProvider.getStarRandomPath(name, null)
        }
    }
}