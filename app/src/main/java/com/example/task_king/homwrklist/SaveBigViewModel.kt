package com.example.task_king.homwrklist

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.task_king.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.flow.MutableStateFlow

class SaveBigViewModel : ViewModel() {

    private val btsNote = MutableStateFlow<String>("")
    private val btsLesson = MutableStateFlow<String>("")
    private val btsDate = MutableStateFlow<String>("")
    private val btsAct = MutableStateFlow<Boolean>(false)
    private val btsState = MutableStateFlow<Int>(BottomSheetBehavior.STATE_HIDDEN)

    private val shareRadio = MutableStateFlow<Int>(R.id.share_all)
    private val shareAct = MutableStateFlow<Boolean>(false)

    private val selectedIds = MutableStateFlow<List<String>>(emptyList())
    private val selectedMode = MutableStateFlow<Boolean>(false)
    private val editAct = MutableStateFlow<Boolean>(false)

    private val isDefault = MutableStateFlow<Boolean>(true)

    fun getDefault() : Boolean = isDefault.value

    fun saveDefault(default: Boolean){
        isDefault.value = default
    }

    fun saveEditAct(act: Boolean){ editAct.value = act}

    fun getSelected() : List<String> = selectedIds.value
    fun getSelectedMode() : Boolean = selectedMode.value

    fun saveSelectedMode(active: Boolean){
        selectedMode.value = active
    }

    fun saveSelected(ids: List<String>){
        selectedIds.value = ids
    }

    fun saveShare(whichOpt: Int){
        shareRadio.value = whichOpt
    }

    fun saveShareAct(act: Boolean){
        shareAct.value = act
    }

    fun getShareRadio() = shareRadio.value
    fun getShareAct() = shareAct.value

    fun save(note: String, lesson: String, date: String, state: Int){
        btsNote.value = note
        btsLesson.value = lesson
        btsDate.value = date
        btsState.value = state
    }
    fun saveAct(act: Boolean){
        btsAct.value = act
    }

    fun getBTSAct() : Boolean = btsAct.value
    fun getLesson() : String = btsLesson.value
    fun getDate() : String = btsDate.value
    fun getNote() : String = btsNote.value
    fun getState() : Int = btsState.value
    fun getEdit() : Boolean = editAct.value

}