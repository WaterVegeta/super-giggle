package com.example.task_king.shcedule.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.task_king.shcedule.DayFragment
import com.example.task_king.shcedule.ScheduleFrag
import java.time.LocalDate

class DayPagerAdapter(activity: ScheduleFrag) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 2000

    override fun createFragment(position: Int): Fragment {
        val offset = position - 1000
        val date = LocalDate.now().plusDays(offset.toLong())
        return DayFragment.Companion.newInstance(date)
    }

    fun getFragmentTag(position: Int): String {
        return "f$position"
    }
}