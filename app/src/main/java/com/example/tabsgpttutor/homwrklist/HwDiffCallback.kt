package com.example.tabsgpttutor.homwrklist

import androidx.recyclerview.widget.DiffUtil
import com.example.tabsgpttutor.data_base.Homework

class HwDiffCallback(private val oldList: List<Homework>,
                     private val newList: List<Homework>
) : DiffUtil.Callback() {

    override fun getOldListSize() = oldList.size
    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(old: Int, new: Int): Boolean {
        return oldList[old].id == newList[new].id // compare by unique ID
    }

    override fun areContentsTheSame(old: Int, new: Int): Boolean {
        return oldList[old] == newList[new] // data class or deep compare
    }
}