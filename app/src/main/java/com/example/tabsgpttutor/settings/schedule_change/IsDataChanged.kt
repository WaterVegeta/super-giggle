package com.example.tabsgpttutor.settings.schedule_change

object IsDataChanged {
    var isDataChanged = false
    var switchState = false

    fun dataChanged(){
       isDataChanged = true
    }
    fun dataNotChanged(){
        isDataChanged = false
    }

    fun getChanged(): Boolean{
        return isDataChanged
    }
}